package mc.compiler.interpreters;

import static mc.util.Utils.instantiateClass;

import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;

import mc.Constant;
import mc.compiler.Interpreter;
import mc.compiler.ast.ASTNode;
import mc.compiler.ast.ChoiceNode;
import mc.compiler.ast.CompositeNode;
import mc.compiler.ast.ConversionNode;
import mc.compiler.ast.FunctionNode;
import mc.compiler.ast.HidingNode;
import mc.compiler.ast.IdentifierNode;
import mc.compiler.ast.ProcessNode;
import mc.compiler.ast.ProcessRootNode;
import mc.compiler.ast.ReferenceNode;
import mc.compiler.ast.RelabelElementNode;
import mc.compiler.ast.RelabelNode;
import mc.compiler.ast.SequenceNode;
import mc.compiler.ast.TerminalNode;
import mc.compiler.ast.VariableSetNode;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.plugins.IProcessInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.operations.ChoiceFun;
import mc.processmodels.automata.operations.SequentialInfixFun;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
import mc.processmodels.petrinet.utils.PetrinetLabeller;

/**
 * This class interprets a process, and returns a petrinet model as a result.
 * Refactored to work on the return (processing after recursive call)
 * Recurse to the leaves of the AST at leaves use
 *        Primitive one Place Zero transitions for STOP and RROR
 * Action prefixing ->   uses
 *       Primitive two Place one transition for each action then converts -> inot =>
 *
 *  All branches accept nets from below and return nets to above!
 *
 *     Local Refetences are added in referenceNode
 *     References are transfered from astNode to Places and a TREE net built
 *    later
 *

 * @author Jacob Beal  David and others
 * @see AutomatonInterpreter
 * @see mc.processmodels.petrinet.Petrinet
 * @see PetriNetPlace
 * @see PetriNetTransition
 */


public class PetrinetInterpreter implements ProcessModelInterpreter {
  static int sid = 0;
  static String indent = "";
  static Map<String, Class<? extends IProcessInfixFunction>> infixFunctions = new HashMap<>();
  static Map<String, Class<? extends IProcessFunction>> functions = new HashMap<>();
  Context context;
  /* referenceSet = local references including root of current Process
     processMap = Global prcesses
  Y = b->STOP.
  Z =  a->X|c->Y,
       X = x->X.
  processMap Y->     referenceSet   Z->   X->
  */
  Set<String> referenceSet = new HashSet<>();
  //Set<String> fromReferenceSet = new HashSet<>();
  Map<String, ProcessModel> processMap = new HashMap<>();
  Stack<Petrinet> processStack = new Stack<>();
  //LocalCompiler compiler;
  Set<String> variableList;
  int subProcessCount = 0;
  VariableSetNode variables;

  public String myString(Map<String, Set<PetriNetPlace>> ref){
   return ref.keySet().stream().
      map(x-> x+" "+
         ref.get(x).stream().map(y->y.getId()).reduce((z,w)-> z+" "+w)+": ").
      reduce((x,y)-> x+" "+y)+"";
  }
  public String myPro(Map<String, ProcessModel> pro){
    return pro.keySet().stream().
      map(x-> x+" "+pro.get(x).getId() ).
      reduce((x,y)-> x+" "+y)+"";
  }

  /**
   * TODO  Document or remove Design  HACK1  see Interpreter
   * Called from interpreter called from COMPILER
   * Executed once at the start of building each process
   * @param processNode
   * @param processMap
   * @param context
   * @return
   * @throws CompilationException
   * @throws InterruptedException
   */
  @Override
  public ProcessModel interpret(ProcessNode processNode,
                                Map<String, ProcessModel> processMap,
                                //  LocalCompiler localCompiler,
                                Context context)
      throws CompilationException, InterruptedException {
    reset();
    //called onec per Global process
    System.out.println("interpret START "+ processNode.getIdentifier()+" pMap "+ processMap.keySet());
    this.context = context;
    variableList = new HashSet<>();
    this.processMap = processMap;
    String identifier = processNode.getIdentifier();
    this.variables = processNode.getVariables();

    interpretProcess(processNode.getProcess(), identifier);

    Petrinet petrinet = processStack.pop().copy();

    //System.out.println("Just Built "+petrinet.myString());
    //System.out.println("Ref "+ referenceSet.toString());

   // System.out.println("Poped "+petrinet.myString());
    if (!petrinet.getId().equalsIgnoreCase(processNode.getIdentifier())) {
      petrinet.setId(processNode.getIdentifier());
    }
    //System.out.println("Poped1 "+petrinet.myString());

    if (processNode.hasRelabels()) {
      processRelabelling(petrinet, processNode.getRelabels());
    }
    //System.out.println("Poped2 "+petrinet.myString());

    if (processNode.hasHiding()) {
      processHiding(petrinet, processNode.getHiding());
    }
    System.out.println("interpret ENDS "+ processNode.getIdentifier() +
                           " "+processMap.keySet().toString());

      //   System.out.println("Interpretor "+petrinet.myString());
    return petrinet;
  }

  /**
   *  called from interpreter that has been called from OUTide of complier
   * @param astNode
   * @param identifier
   * @param processMap
   * @param context
   * @return
   * @throws CompilationException
   * @throws InterruptedException
   */
  @Override
  public ProcessModel interpret(ASTNode astNode, String identifier,
                                Map<String, ProcessModel> processMap, Context context)
      throws CompilationException, InterruptedException {
    reset();
    System.out.println("INTerpret START "+identifier+" pMap "+ processMap.keySet());
      this.context = context;
    this.processMap = processMap;

    interpretProcess(astNode, identifier);

    Petrinet petrinet = ((Petrinet) processStack.pop()).copy();
    System.out.println("INTerpret END  "+ identifier);
    return petrinet;
  }

  /**
   *   Processes are pushed onto the stack FROM processMap
   * @param astNode
   * @param identifier
   * @throws CompilationException
   * @throws InterruptedException
   */
  private void interpretProcess(ASTNode astNode, String identifier)
      throws CompilationException, InterruptedException {
    //prity print AST
    PetrinetInterpreter.indent = PetrinetInterpreter.indent.concat("-");
    String className = astNode.getClass().getSimpleName();
    System.out.println("iPro "+ PetrinetInterpreter.indent + className);
//  go and get the process  but if process is not of the correct type CRASH?
    if (astNode instanceof IdentifierNode) {
      System.out.println("*** interpretProcess IdentifierNode");
      String reference = ((IdentifierNode) astNode).getIdentifier();


  System.out.println("stack petri "+processMap.get(reference).getId()+" "+
   processMap.get(reference).getProcessType().toString());
  //extract the net from the MULTI_
  if (processMap.get(reference).getProcessType().equals(ProcessType.MULTI_PROCESS)) {
    processStack.push( processMap.get(reference).getProcessType().
       convertTo(ProcessType.PETRINET, processMap.get(reference))); //What a way to extact  a net
  } else {
    processStack.push((Petrinet) processMap.get(reference));
  }
    }else if (astNode instanceof ProcessRootNode) {
      System.out.println("*** interpretProcess ProcessRootNode");
      ProcessRootNode root = (ProcessRootNode) astNode;

      interpretProcess(root.getProcess(), identifier);
      Petrinet petrinet = processStack.pop();
      petrinet = processLabellingAndRelabelling(petrinet, root);

      if (root.hasHiding()) {
        processHiding(petrinet, root.getHiding());
      }
      System.out.println("stack petri "+petrinet.getId());
      processStack.push(petrinet);

    }  else {
      System.out.println("*** interpretProcess ELSE");
      Petrinet petrinet = new Petrinet(identifier, true);

      PetriNetPlace currentPlace = new ArrayList<>(petrinet.getRoots()).get(0);
      //   System.out.println("WARNING  ADDING root node "+ currentPlace.getId());

      if (variables != null) {
        petrinet.setHiddenVariables(variables.getVariables());
        petrinet.setHiddenVariablesLocation(variables.getLocation());
      }
      if (variableList == null) {
        variableList = new HashSet<>();
      }
      petrinet.getVariables().addAll(variableList);

      petrinet.setVariablesLocation(astNode.getLocation());

      //Interpret Node
      petrinet = interpretASTNode(astNode, petrinet, currentPlace);
      if (astNode.getReferences() != null) {
        for (PetriNetPlace pl : petrinet.getRoots()) {
          pl.addRefefances(astNode.getReferences());
        }
      }
      System.out.println("*** "+petrinet.myString());
      petrinet = tree2net(petrinet);
      System.out.println("stack petri "+petrinet.getId());
      processStack.push(petrinet);
    }
if (astNode.getReferences() == null) System.out.println("astNode.getReferences() = null");
 else   System.out.println("iPro<"+ PetrinetInterpreter.indent + className+" "+ " ref " +
      astNode.getReferences().toString());
    if (PetrinetInterpreter.indent.length()> 1)
      PetrinetInterpreter.indent = PetrinetInterpreter.indent.substring(1);

    return;
  }

  private Petrinet tree2net(Petrinet petri) throws CompilationException {
    //Map<Set<PetriNetPlace>,Set<PetriNetPlace>> marking2Marking = new HashMap<>();
    //Map<PetriNetPlace,PetriNetPlace> marking2Marking = new HashMap<>();
    // filter ref or fromRef places
    //System.out.println("Tree2Net"+petri.myString());
    Set<PetriNetPlace> pls =
      petri.getPlaces().values().stream().
            filter(x->x.getReferences().size()>0 ||
                      x.getFromReferences().size()>0).collect(Collectors.toSet());
    //System.out.println("GOgo"+pls.stream().map(x->x.getId()).reduce("** ",(x,y)->x+" "+y));
    // glue pairs of places where one has a ref and the other the same fromRef
    for(PetriNetPlace pl1: pls){
      if (pl1.getReferences().size()==0 ) continue;
      //System.out.println(pl1.myString());
      for(PetriNetPlace pl2: pls){
        if (pl2.getFromReferences().size()==0 ) continue;

        //System.out.println(pl2.myString());
        if(pl1.getReferences().containsAll(pl2.getFromReferences())){
          System.out.println("Joining "+pl1.getId()+" "+pl2.getId());
          pl2.setTerminal("");
          petri.gluePlaces(Collections.singleton(pl1), Collections.singleton(pl2));
        }
      }
    }
    //System.out.println("Over");
    //
    return petri;
  }
  private Petrinet interpretASTNode(ASTNode currentNode)
    throws CompilationException, InterruptedException {
    Petrinet fake = new Petrinet("FAKE");
    PetriNetPlace fakePl  = new PetriNetPlace("fake");
    return interpretASTNode(currentNode, fake,fakePl);
  }
  private Petrinet interpretASTNode(ASTNode currentNode, Petrinet petri, PetriNetPlace currentPlace)
      throws CompilationException, InterruptedException {

    if (Thread.currentThread().isInterrupted()) {
      throw new InterruptedException();
    }

   // if (currentNode.hasReferences()) {
      // currentPlace.addRefefances(currentNode.getReferences());
     // System.out.println("Node refs "+ currentNode.getReferences());
  //  }

//prity print AST
    PetrinetInterpreter.indent = PetrinetInterpreter.indent.concat("-");
    String className = currentNode.getClass().getSimpleName();
    if (currentNode.getReferences() != null) {
      System.out.println("AST " + PetrinetInterpreter.indent + className +
        " refs " + currentNode.getReferences());
    } else {
      System.out.println("AST " + PetrinetInterpreter.indent + className);
    }
//
//UNprefixed use of Global Processes W in Z = W/{w}  NOT in Z = a->W;
    if (currentNode instanceof ProcessRootNode) {
      interpretProcessRoot((ProcessRootNode) currentNode, petri, currentPlace);
    }
    // current node is in petri and is set to being terminal
    else if (currentNode instanceof TerminalNode) {
      petri = interpretTerminal((TerminalNode) currentNode);
    }
    else if (currentNode instanceof SequenceNode) {
      petri = interpretSequence((SequenceNode) currentNode);
    }
    //this is for  |
    else if (currentNode instanceof ChoiceNode) {
      petri = interpretChoice((ChoiceNode) currentNode);
    }
    //this is for => and || and +
    else if (currentNode instanceof CompositeNode) {
      petri = interpretComposite((CompositeNode) currentNode, petri);
    }
    //Use of Global Processes BOTH  W in Z = W/{w} and Z = a->W;
    else if (currentNode instanceof IdentifierNode) {
      interpretIdentifier((IdentifierNode) currentNode, petri, currentPlace);
    }
    //functions  nfa2dfa, prune, simp, abs,  ....
    else if (currentNode instanceof FunctionNode) {
      petri = interpretFunction((FunctionNode) currentNode, petri, currentPlace);
    }
    // tokenRule and ownersRule
    else if (currentNode instanceof ConversionNode) {
      //System.out.println("ConversionNode START "+petri.myString());
      petri = interpretConversion((ConversionNode) currentNode, petri, currentPlace);
      //System.out.println("OUTSIDE of interpretConversion petri "+petri.myString());

    }
    // Build Stop node with FromRef  (Ref addded by RefReplacer)
    // At end of interpretation tree2net will glued FromRef to Ref
    else if (currentNode instanceof ReferenceNode) {
      String ref = ((ReferenceNode)currentNode).getReference();
      System.out.println("RRef "+ref);
      if (ref.length()>0)  {
        petri = Petrinet.stopNet(ref);
      }
    }


    //prity print
    if (PetrinetInterpreter.indent.length()> 1)
      PetrinetInterpreter.indent = PetrinetInterpreter.indent.substring(1);
    if (currentNode.getReferences()!=null) {
      System.out.println("AST<" + PetrinetInterpreter.indent + className + " ref " +
        currentNode.getReferences().toString());

      for (PetriNetPlace pl : petri.getRoots()) {
        pl.addRefefances(currentNode.getReferences());
      }
      //System.out.println(petri.myString());
    }
    else
      System.out.println("AST<"+ PetrinetInterpreter.indent + className);
    // + petri.myString());

 return  petri;
  }

  // current place is in petri Just set it as terminal
  private Petrinet interpretTerminal(TerminalNode term) {
    if (term.getTerminal().equals("STOP") )
      return Petrinet.stopNet();
    else
      return Petrinet.errorNet();
  }


  /**
   * Called for A = a->STOP    NOT => (see SequentialInfixFunction
   * currenPlace - label -> nextPlace(new currentPlace)
   * @param seq
   * @throws CompilationException
   * @throws InterruptedException
   */
  private Petrinet interpretSequence(SequenceNode seq)
      throws CompilationException, InterruptedException {

   String lab =  seq.getFrom().getAction();
   Petrinet ev =  Petrinet.oneEventNet(lab);
   Petrinet petri =  interpretASTNode(seq.getTo());  //initially the STOP net

    SequentialInfixFun sif = new SequentialInfixFun();
   Petrinet ret = sif.compose("net."+lab, ev,petri);
//System.out.println("SEQUENCE end "+ ret.myString());
    return ret;
  }


  /**
   *
   * @param choice
   * @throws CompilationException
   * @throws InterruptedException
   * @returns a new petrinet
   */
  private Petrinet interpretChoice(ChoiceNode choice)
      throws CompilationException, InterruptedException {


    Petrinet op2 = interpretASTNode(choice.getSecondProcess());
    //temp now hold the first petri Net
    Petrinet  op1 = interpretASTNode(choice.getFirstProcess());
    ChoiceFun sif = new ChoiceFun();
    Petrinet ret = sif.compose("net:"+sid++ +".", op1,op2);
    //System.out.println("CHOICE end "+ ret.myString());
return ret;
  }

  private void interpretProcessRoot(ProcessRootNode processRoot, Petrinet petri,
                                    PetriNetPlace currentPlace)
      throws CompilationException, InterruptedException {
  //  currentPlace = petri.getPlaces().get(currentPlace.getId());//only match on "id"
    interpretProcess(processRoot.getProcess(), petri.getId() + "." + ++subProcessCount);
    Petrinet model = ((Petrinet) processStack.pop()).copy();
    model = processLabellingAndRelabelling(model, processRoot);

    if (processRoot.hasHiding()) {
      processHiding(model, processRoot.getHiding());
    }

    addPetrinet(currentPlace, model, petri);
  }

  private void interpretIdentifier(IdentifierNode identifier, Petrinet petri,
                                   PetriNetPlace currentPlace)
      throws CompilationException, InterruptedException {
    System.out.println("IDENTIFIER  for "+ identifier.getFromReferences() +
                       " get "+ identifier.getIdentifier());
  //  currentPlace = petri.getPlaces().get(currentPlace.getId());//only match on "id"
    ProcessModel model = processMap.get(identifier.getIdentifier());
    if (!(model instanceof Petrinet)) {
      throw new CompilationException(getClass(), "Unable to find petrinet for identifier: "
          + identifier.getIdentifier(), identifier.getLocation());
    }
    Petrinet copy = ((Petrinet) model).copy();
    addPetrinet(currentPlace, copy, petri);
  }

  /**
   *
   * @param composite  Could be "||" or "=>" or
   * @param petri
   * @return
   * @throws CompilationException
   * @throws InterruptedException
   */

  private Petrinet interpretComposite(CompositeNode composite, Petrinet petri)
      throws CompilationException, InterruptedException {

    System.out.println("interpretCOMPOSITE "+composite.toString());
    interpretProcess(composite.getFirstProcess(), petri.getId() + ".pc1");
    interpretProcess(composite.getSecondProcess(), petri.getId() + ".pc2");

    ProcessModel model2 = processStack.pop();
    ProcessModel model1 = processStack.pop();

    if (model1 == null || model2 == null) {
      throw new CompilationException(getClass(), "Expecting a petrinet in composite "
          + petri.getId(), composite.getLocation());
    }
    //System.out.println("OPERATION " +composite.getOperation());
//comp is the correct Net
    Petrinet comp = instantiateClass(infixFunctions.get(composite.getOperation()))
        .compose(model1.getId() + composite.getOperation() + model2.getId(),
            (Petrinet) model1,
            (Petrinet) model2);
    return comp;
    //addPetrinet(currentPlace, comp, petri);
  }

  private Petrinet interpretFunction(FunctionNode func, Petrinet petri, PetriNetPlace currentPlace)
      throws CompilationException, InterruptedException {
    List<Petrinet> models = new ArrayList<>();
    for (ASTNode p : func.getProcesses()) {
      interpretProcess(p, petri.getId() + ".fn");
      models.add(processStack.pop());
    }

    if (models.isEmpty()) {
      throw new CompilationException(getClass(),
          "Expecting a petrinet, received an undefined process.", func.getLocation());
    }

    Petrinet[] petris = models.stream().map(Petrinet.class::cast).toArray(Petrinet[]::new);

    Petrinet processed = instantiateClass(functions.get(func.getFunction()))
        .compose(petri.getId() + ".fn", func.getFlags(), context, petris);
 //   System.out.println("processed FUNCTION "+processed.myString());
    //addPetrinet(currentPlace, processed, petri);
    //System.out.println("interpret FUNCTION "+petri.myString());
    return processed;
  }

  private Petrinet interpretConversion(ConversionNode conv, Petrinet petri,
                                   PetriNetPlace currentPlace)
      throws CompilationException, InterruptedException {
  //  currentPlace = petri.getPlaces().get(currentPlace.getId());//only match on "id"

    //System.out.println("interpretConversion start "+petri.myString());
    ProcessType to = ProcessType.valueOf(conv.to.toUpperCase());
    ProcessType from = ProcessType.valueOf(conv.from.toUpperCase());

    ProcessModel pm = new Interpreter().interpret(conv.from, conv.getProcess(),
      petri.getId() + ".pc" + subProcessCount++, processMap, context);
    Petrinet temp = new Petrinet(petri.getId(), false);

    Petrinet p = addPetrinet(currentPlace,
      pm.getProcessType().convertTo(to, pm), temp).copy();
    //System.out.println("interpretConversion  ends "+p.myString());  //GOOD
    return p;

  }

  private Petrinet addPetrinet(PetriNetPlace currentPlace,
                               Petrinet petrinetToAdd, Petrinet master)
      throws CompilationException {
    //Throwable t = new Throwable();
    //t.printStackTrace();

    //System.out.println("currentPlace "+currentPlace.getId());
    //System.out.println("toAdd "+petrinetToAdd.myString());
    //System.out.println("master "+ master.myString());
    //assert  master.hasPlace(currentPlace) : "addPetrinet "+currentPlace.getId();
    List<String> references = new ArrayList<>();
    //TODO: References
    if (currentPlace.getReferences() != null) {
      references.addAll(currentPlace.getReferences());
    }
    //System.out.println("====addPetrinet======== "+ petrinetToAdd.getId());
    //System.out.println("toAdd "+ petrinetToAdd.myString());
    petrinetToAdd.validatePNet();
    //System.out.println("master "+master.myString());
    master.validatePNet();
    //System.out.println("====addPetrinet======== "+ petrinetToAdd.getId());

    //System.out.println("====masterPetrinet======== "+ master.getId());
    Set<PetriNetPlace> places = master.addPetrinet(petrinetToAdd);
    //System.out.println(master.myString());
    //System.out.println("====masterPetrinet======== "+ master.getId());
master.validatePNet();

    places.stream()
      .map(PetriNetPlace::getReferences)
      .filter(Objects::nonNull)
      .forEach(references::addAll);

    //references.forEach(id -> referenceSet.replace(id, newStart));
    return master;
  }

  private Petrinet processLabellingAndRelabelling(Petrinet petri, ProcessRootNode processRoot)
      throws CompilationException {
    if (processRoot.hasLabel()) {
      petri = PetrinetLabeller.labelPetrinet(petri, processRoot.getLabel());
    }

    if (processRoot.hasRelabelSet()) {
      processRelabelling(petri, processRoot.getRelabelSet());
    }

    return petri;
  }

  private void processRelabelling(Petrinet petri, RelabelNode relabelSet)
      throws CompilationException {
    for (RelabelElementNode r : relabelSet.getRelabels()) {
      if (!petri.getAlphabet().keySet().contains(r.getOldLabel())) {
        throw new CompilationException(getClass(), "Cannot find action" + r.getOldLabel()
            + "to relabel.", relabelSet.getLocation());
      }
      petri.relabelTransitions(r.getOldLabel(), r.getNewLabel());
    }
  }

  private void processHiding(Petrinet petri, HidingNode hiding) throws CompilationException {
    //Includes syntax (\)
    if (hiding.getType().equalsIgnoreCase("includes")) {
      for (String hidden : hiding.getSet().getSet()) {
        if (petri.getAlphabet().keySet().contains(hidden)) {
          petri.relabelTransitions(hidden, Constant.HIDDEN);
        } else {
          throw new CompilationException(getClass(), "Could not find " + hidden + " action to hide",
              hiding.getLocation());
        }

      }
      //excludes syntax (@)
    } else {
      new ArrayList<>(petri.getAlphabet().keySet()).stream()
          .filter(k -> !hiding.getSet().getSet().contains(k))
          .forEach(a -> petri.relabelTransitions(a, Constant.HIDDEN));
    }
  }

  public void reset() {
    referenceSet.clear();
    processStack.clear();
    sid = 0;
    indent = "";
    subProcessCount = 0;
  }
}
