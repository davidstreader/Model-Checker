package mc.compiler.interpreters;

import static mc.util.Utils.getArch;
import static mc.util.Utils.instantiateClass;

import com.microsoft.z3.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
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
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
import mc.processmodels.petrinet.utils.PetrinetLabeller;

/**
 * This class interprets a process, and returns a petrinet model as a result.
 * THIS NEEDS refactoring  recursive decent over AST  EITHER
 *  1 (processing before recurive call)
 *    on the way down a net is built from start to STOP. parameter petri is both in and out
 *     currentNode is point to add next transitions
 *  2 (processing after recursive call)
 *     on the way up nets are composed  better to return net
 *     if spurious root nodes appear do not addPetriNet to petri just return the correct net
 *
 * Problem  code requires currentNode to be the actual Object in the petri net rebuilding the net
 * breaks this assumption. So reset currentNode by matching on ID
 *
 *  I think Referances can be removed BUT NOY CERTAIN!
 * @author Jacob Beal  (not all his design so do not point the finger!)
 * @see AutomatonInterpreter
 * @see mc.processmodels.petrinet.Petrinet
 * @see PetriNetPlace
 * @see PetriNetTransition
 */
public class PetrinetInterpreter implements ProcessModelInterpreter {
  static String indent = "";
  static Map<String, Class<? extends IProcessInfixFunction>> infixFunctions = new HashMap<>();
  static Map<String, Class<? extends IProcessFunction>> functions = new HashMap<>();
  Context context;
  Map<String, Set<PetriNetPlace>> referenceMap = new HashMap<>(); Map<String, ProcessModel> processMap = new HashMap<>(); Stack<Petrinet> processStack = new Stack<>();
  //LocalCompiler compiler;
  Set<String> variableList;
  int subProcessCount = 0;
  VariableSetNode variables;

  @Override
  public ProcessModel interpret(ProcessNode processNode,
                                Map<String, ProcessModel> processMap,
                                //  LocalCompiler localCompiler,
                                Context context)
      throws CompilationException, InterruptedException {
    reset();
    //this.compiler = compiler;
    this.context = context;
    variableList = new HashSet<>();
    this.processMap = processMap;
    String identifier = processNode.getIdentifier();
    this.variables = processNode.getVariables();

    interpretProcess(processNode.getProcess(), identifier);

    Petrinet petrinet = processStack.pop().copy();
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
 //   System.out.println("Interpretor "+petrinet.myString());
    return petrinet;
  }

  @Override
  public ProcessModel interpret(ASTNode astNode, String identifier,
                                Map<String, ProcessModel> processMap, Context context)
      throws CompilationException, InterruptedException {
    reset();
    this.context = context;
    this.processMap = processMap;

    interpretProcess(astNode, identifier);

    Petrinet petrinet = ((Petrinet) processStack.pop()).copy();

    return petrinet;
  }


  private void interpretProcess(ASTNode astNode, String identifier)
      throws CompilationException, InterruptedException {
    if (astNode instanceof IdentifierNode) {
      String reference = ((IdentifierNode) astNode).getIdentifier();
      processStack.push((Petrinet) processMap.get(reference));
      return;
    }

    if (astNode instanceof ProcessRootNode) {
      ProcessRootNode root = (ProcessRootNode) astNode;

      interpretProcess(root.getProcess(), identifier);
      Petrinet petrinet = processStack.pop();
      petrinet = processLabellingAndRelabelling(petrinet, root);

      if (root.hasHiding()) {
        processHiding(petrinet, root.getHiding());
      }
      processStack.push(petrinet);
      return;
    }
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
  //  System.out.println("interpretProcess petri "+petrinet.myString());
    processStack.push(petrinet);
  }

  private Petrinet interpretASTNode(ASTNode currentNode, Petrinet petri, PetriNetPlace currentPlace)
      throws CompilationException, InterruptedException {

    if (Thread.currentThread().isInterrupted()) {
      throw new InterruptedException();
    }

    if (currentNode.hasReferences()) {
      for (String ref : currentNode.getReferences()) {
        referenceMap.put(ref, new HashSet<>(Collections.singleton(currentPlace)));
      }

      currentPlace.setReferences(currentNode.getReferences());
    }

    PetrinetInterpreter.indent = PetrinetInterpreter.indent.concat("-");
    String className = currentNode.getClass().getSimpleName();
    System.out.println("AST "+ PetrinetInterpreter.indent + className);

    if (currentNode instanceof ProcessRootNode) {
      interpretProcessRoot((ProcessRootNode) currentNode, petri, currentPlace);
    }
     else if (currentNode instanceof SequenceNode) {
      interpretSequence((SequenceNode) currentNode, petri, currentPlace);
    }
    // current node is in petri and is set to being terminal
    else if (currentNode instanceof TerminalNode) {
      interpretTerminal((TerminalNode) currentNode, petri, currentPlace);
    }
    //this is for  |
    else if (currentNode instanceof ChoiceNode) {
      petri = interpretChoice((ChoiceNode) currentNode, petri, currentPlace);
    }
    else if (currentNode instanceof IdentifierNode) {
      interpretIdentifier((IdentifierNode) currentNode, petri, currentPlace);
    }
    //this is for => and || and +
    else if (currentNode instanceof CompositeNode) {
      petri = interpretComposite((CompositeNode) currentNode, petri);
    }
    else if (currentNode instanceof FunctionNode) {
      petri = interpretFunction((FunctionNode) currentNode, petri, currentPlace);
    }
    else if (currentNode instanceof ConversionNode) {
      //System.out.println("ConversionNode START "+petri.myString());
      petri = interpretConversion((ConversionNode) currentNode, petri, currentPlace);
      //System.out.println("OUTSIDE of interpretConversion petri "+petri.myString());

    }
    if (PetrinetInterpreter.indent.length()> 1)
      PetrinetInterpreter.indent = PetrinetInterpreter.indent.substring(1);
 return  petri;
  }

  /**
   * Called for A = a->STOP    NOT => (see SequentialInfixFunction
   * currenPlace - label -> nextPlace(new currentPlace)
   * @param seq
   * @param petri
   * @param currentPlace
   * @throws CompilationException
   * @throws InterruptedException
   */
  private void interpretSequence(SequenceNode seq, Petrinet petri, PetriNetPlace currentPlace)
      throws CompilationException, InterruptedException {
    //System.out.println("interpretSeq current "+ currentPlace.myString());
   // get action label from ast node

    currentPlace = petri.getPlaces().get(currentPlace.getId());//only match on "id"
    String action = seq.getFrom().getAction();
    //Throwable t = new Throwable();
    //t.printStackTrace();
    //System.out.println("SEQUENCE start "+petri.myString());
    //System.out.println("SEQUENCE start "+currentPlace.myString());
    Set<String> fakeOwner = new HashSet<>();
    fakeOwner.add(Petrinet.DEFAULT_OWNER);

    if (seq.getTo() instanceof ReferenceNode) {
      System.out.println("Reference?");
      ReferenceNode ref = (ReferenceNode) seq.getTo();
      Collection<PetriNetPlace> nextPlaces = referenceMap.get(ref.getReference());

      if (nextPlaces == null) {
        throw new CompilationException(getClass(),
            "The petrinet attempted to enter an invalid reference", seq.getTo().getLocation());
      }

      for (PetriNetPlace nextPlace : nextPlaces) {
        //System.out.println("Referance "+nextPlace.getId());
        PetriNetTransition transition = petri.addTransition(action);
        //System.out.println("New Transition " +transition.getId());
                     //  TO     <-    FROM
        petri.addEdge(transition, currentPlace, fakeOwner);
        petri.addEdge(nextPlace, transition, fakeOwner);
      }

      //do something
    } else {
//System.out.println("Not Reference ");
      PetriNetPlace nextPlace = petri.addPlace();
      PetriNetTransition transition = petri.addTransition(action);
//System.out.println("XX "+ petri.myString());
//System.out.println("new Place "+currentPlace.getId()+" new Tran "+ transition.getId());
      petri.addEdge(transition, currentPlace, fakeOwner);
      System.out.println("pingoSeq");
//System.out.println("new Place "+nextPlace.getId()+" new Tran "+ transition.getId());
      petri.addEdge(nextPlace, transition, fakeOwner);
 //System.out.println("petri "+petri.myString());
      interpretASTNode(seq.getTo(), petri, nextPlace);
    }
//System.out.println("SEQUENCE end "+ petri.myString());
  }
// current place is in petri Just set it as terminal
  private void interpretTerminal(TerminalNode term, Petrinet petri, PetriNetPlace currentPlace) {
    currentPlace.setTerminal(term.getTerminal());
  }

  /**
   *
   * @param choice
   * @param petri  all we need is the label
   * @param currentPlace
   * @throws CompilationException
   * @throws InterruptedException
   * @returns a new petrinet
   */
  private Petrinet interpretChoice(ChoiceNode choice, Petrinet petri, PetriNetPlace currentPlace)
      throws CompilationException, InterruptedException {
    currentPlace = petri.getPlaces().get(currentPlace.getId());//only match on "id"
    Petrinet temp = new Petrinet(petri.getId(), false);
System.out.println("PETRI Ch "+ temp.myString());
//System.out.println("current " + currentPlace.myString());

    interpretASTNode(choice.getSecondProcess(), temp, currentPlace);
    //temp now hold the first petri Net
System.out.println("PETRI Ch1 "+ temp.myString());
  Set<PetriNetPlace> roots1 = new HashSet<>();
    for(PetriNetPlace pl : temp.getRoots()){
    roots1.add(pl);
    }
System.out.println("root1 "+Petrinet.marking2String(roots1));
    Petrinet temp2 = new Petrinet(petri.getId(), false);
    interpretASTNode(choice.getFirstProcess(), temp2, currentPlace);
    // termo now hold both petriNets  -- the roots need to be glued together
    temp.setRoot2Start();
System.out.println("PETRI Ch2 "+ temp2.myString());

    boolean ok = true;
   Set<PetriNetPlace> roots2 = new HashSet<>();
   for (PetriNetPlace pl: temp2.getRoots()){
     System.out.println(pl.getId());
     ok = false;
     for (PetriNetPlace p: roots1) {
       if (pl.getId().equals(p.getId())) {
         ok = true;
         System.out.println("found "+p.getId());
         break;
       }
     }
     if (!ok) {roots2.add(pl);}
   }

    System.out.println(temp.myString());
    System.out.println("1 "+ Petrinet.marking2String(roots1));
    System.out.println("2 "+ Petrinet.marking2String(roots2));
    temp.gluePlaces(roots1,roots2);
    System.out.println("CHOICE end "+ temp.myString());
return temp;
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

  //  System.out.println("interpretCOMPOSITE "+composite.toString());
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
 /*   ProcessType to = ProcessType.valueOf(conv.to.toUpperCase());
    ProcessType from = ProcessType.valueOf(conv.from.toUpperCase());

    ProcessModel pm = new Interpreter().interpret(conv.from, conv.getProcess(),
        petri.getId() + ".pc" + subProcessCount++, processMap, context);

    addPetrinet(currentPlace, petri, pm.getProcessType().convertTo(to, pm));
    */
      //System.out.println("currentPlace "+currentPlace.getId());
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

    //references.forEach(id -> referenceMap.replace(id, newStart));
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
    referenceMap.clear();
    processStack.clear();
  }
}
