package mc.compiler.interpreters;

import static mc.util.Utils.instantiateClass;

import com.microsoft.z3.Context;
import com.google.common.collect.Multiset;
import com.google.common.collect.HashMultiset;
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
import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.operations.ChoiceFun;
import mc.processmodels.automata.operations.SequentialInfixFun;
import mc.processmodels.conversion.OwnersRule;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
import mc.processmodels.petrinet.operations.RefineFun;
import mc.processmodels.petrinet.utils.PetrinetLabeller;
import mc.processmodels.Mapping;
/**
 * This class interprets a process, and returns a petrinet model as a result.
 * Refactored to work on the return (processing after recursive call)
 * Recurse to the leaves of the AST at leaves use
 * Primitive one Place Zero transitions for STOP and RROR
 * Action prefixing ->   uses
 * Primitive two Place one transition for each action then converts -> inot =>
 * <p>
 * All branches accept nets from below and return nets to above!
 * <p>
 * Local Refetences are added in referenceNode
 * References are transfered from astNode to Places and a TREE net built
 * later
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

 public String myString(Map<String, Set<PetriNetPlace>> ref) {
  return ref.keySet().stream().
    map(x -> x + " " +
      ref.get(x).stream().map(y -> y.getId()).reduce((z, w) -> z + " " + w) + ": ").
    reduce((x, y) -> x + " " + y) + "";
 }

 public String myPro(Map<String, ProcessModel> pro) {
  return pro.keySet().stream().
    map(x -> x + " " + pro.get(x).getId()).
    reduce((x, y) -> x + " " + y) + "";
 }

 /**
  * TODO  Document or remove Design  HACK1  see Interpreter
  * Called from interpreter called from COMPILER
  * Executed once at the start of building each process
  *
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
 //System.out.println("***123 processNode " + processNode.getProcess().toString());

  if (processNode.getProcess() instanceof ConversionNode) {
   if (((ConversionNode) processNode.getProcess()).to.equals("petrinet")) {
    //  ownersRule(P);
    /*System.out.println("\n**Now**Calling Owners " +
      ((ConversionNode) processNode.getProcess()).getProcess()+ "\n"); */
    ProcessModel model =  new MultiProcessModel(processNode.getIdentifier());
    model.setLocation(processNode.getLocation());
//1. build petrinet P
       interpretProcess((((ConversionNode) processNode.getProcess()).getProcess()), identifier);
    //System.out.println("Ping");
    Petrinet modelPetri = processStack.pop().copy();
    ((MultiProcessModel) model).addProcess(modelPetri);
//2. build automata via TokenRule (may be redundent as may have been pre built)
    ProcessModel modelAut;
    HashMap<AutomatonNode, Multiset<PetriNetPlace>> nodeToMarking =
      new HashMap<>();
    HashMap<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = new HashMap<>();
    //System.out.println("Pong");
    modelAut = TokenRule.tokenRule(
      (Petrinet) ((MultiProcessModel) model)
        .getProcess(ProcessType.PETRINET), markingToNode, nodeToMarking);
    //System.out.println("First tokenRule ");

//3. NOW at start apply Owners Rule to build new PetriNet
     modelPetri =  OwnersRule.ownersRule((Automaton) modelAut);
    ((MultiProcessModel) model).addProcess(modelPetri);
//4. finally build new Automata vis Token Rule
    modelAut = TokenRule.tokenRule(
      (Petrinet) ((MultiProcessModel) model)
        .getProcess(ProcessType.PETRINET), markingToNode, nodeToMarking);
    //System.out.println("Built automata with tokenRule "+ modelAut.getId());
    ((MultiProcessModel) model).addProcess(modelAut);
    ((MultiProcessModel) model)
      .addProcessesMapping(new Mapping(nodeToMarking, markingToNode));
    //System.out.println("END of Owners");

 return model;
   } else if (((ConversionNode) processNode.getProcess()).to.equals("automata")) {
    //System.out.println("\n**???ed**Call Token " +processNode.toString()+"\n");
    ProcessModel model =  new MultiProcessModel(processNode.getIdentifier());
    model.setLocation(processNode.getLocation());

    interpretProcess((((ConversionNode) processNode.getProcess()).getProcess()), identifier);
    //System.out.println("Ping");
    Petrinet modelPetri = processStack.pop().copy();
    ((MultiProcessModel) model).addProcess(modelPetri);
    ProcessModel modelAut;
    HashMap<AutomatonNode, Multiset<PetriNetPlace>> nodeToMarking = new HashMap<>();
    HashMap<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = new HashMap<>();
      /*
        Token rule works here but can not be called from petrinetInterpreter
       */
    modelAut = TokenRule.tokenRule(
      (Petrinet) ((MultiProcessModel) model)
        .getProcess(ProcessType.PETRINET), markingToNode, nodeToMarking);
    //System.out.println("Built automata with tokenRule "+ modelAut.getId());
    ((MultiProcessModel) model).addProcess(modelAut);
    ((MultiProcessModel) model)
      .addProcessesMapping(new Mapping(nodeToMarking, markingToNode));
    //System.out.println("END of Token");
    return model;
   } else {
    System.out.println("\nOops! \n");
   }

  }

  interpretProcess(processNode.getProcess(), identifier);

  Petrinet petrinet = processStack.pop().copy();

 //System.out.println("Just Built "+petrinet.myString());
  //System.out.println("Ref "+ referenceSet.toString());

  ////System.out.println("Poped "+petrinet.myString());
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
  //System.out.println("interpret ENDS "+ processNode.getIdentifier() + " "+processMap.keySet().toString());

  //  //System.out.println("Interpretor "+petrinet.myString());
  return petrinet;
 }

 /**
  * called from interpreter that has been called from OUTide of complier
  *
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
  System.out.println("interpret START "+identifier+" pMap "+ processMap.keySet());
  this.context = context;
  this.processMap = processMap;

  interpretProcess(astNode, identifier);

  Petrinet petrinet = ((Petrinet) processStack.pop()).copy();
  //System.out.println("INTerpret END  "+ identifier);
  return petrinet;
 }

 /**
  * Processes are pushed onto the stack FROM processMap
  * Must be private as stricctly SIDE EFFECT method
  * @param astNode
  * @param identifier
  * @throws CompilationException
  * @throws InterruptedException
  */
 private void interpretProcess(ASTNode astNode, String identifier)
   throws CompilationException, InterruptedException {
  //prity print AST
  //System.out.println("interpretProcess (PN) astNode IS " +astNode.toString());
  PetrinetInterpreter.indent = PetrinetInterpreter.indent.concat("-");
  String className = astNode.getClass().getSimpleName();
 //System.out.println("iPro " + PetrinetInterpreter.indent + className);
//  go and get the process  but if process is not of the correct type CRASH?
  if (astNode instanceof IdentifierNode) {
   String reference = ((IdentifierNode) astNode).getIdentifier();
   //System.out.println("*** interpretProcess IdentifierNode "+ reference);
   if (processMap.get(reference).getProcessType().equals(ProcessType.MULTI_PROCESS)) {
    //System.out.println("MultiProcess -> PN");
    processStack.push(processMap.get(reference).getProcessType().
      convertTo(ProcessType.PETRINET, processMap.get(reference))); //What a way to extact  a net
   } else {
    processStack.push((Petrinet) processMap.get(reference));
   }
//System.out.println("got Net "+processStack.peek().getId()+" from Map");
  } else if (astNode instanceof ProcessRootNode) {
   //System.out.println("*** interpretProcess ProcessRootNode");
   ProcessRootNode root = (ProcessRootNode) astNode;

   interpretProcess(root.getProcess(), identifier);
   Petrinet petrinet = processStack.pop();
   petrinet = processLabellingAndRelabelling(petrinet, root);
  //System.out.println("TO stack petri " + petrinet.getId());
   if (root.hasHiding()) {
    processHiding(petrinet, root.getHiding());
   }
   //System.out.println("stack petri "+petrinet.getId());
   processStack.push(petrinet);

  } else {
   //System.out.println("*** interpretProcess ELSE");
   Petrinet petrinet = new Petrinet(identifier, true);
   PetriNetPlace currentPlace =  petrinet.getPlace(petrinet.getRoots().get(0).iterator().next());
   //PetriNetPlace currentPlace = new ArrayList<>(petrinet.getRoot()).get(0);
   //  //System.out.println("WARNING  ADDING root node "+ currentPlace.getId());

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
  //System.out.println("\n before 236*****Interpret " +petrinet.myString()+"\n");
   petrinet = interpretASTNode(astNode, petrinet, currentPlace);
  //System.out.println("236 *****Interpret "+astNode.toString());
  //System.out.println("\n*****Interpret " +petrinet.myString()+"\n");
   if (astNode.getReferences() != null) {
    for (PetriNetPlace pl : petrinet.getAllRoots()) {
     pl.addRefefances(astNode.getReferences());
    }
   }
   //System.out.println("*** "+petrinet.myString());
   petrinet = tree2net(petrinet);
   //System.out.println("stack petri "+petrinet.getId());
   processStack.push(petrinet);
  }
  if (astNode.getReferences() == null) {
   //System.out.println("astNode.getReferences() = null");
   //Throwable t = new Throwable(); t.printStackTrace();
  } else System.out.println("iPro<" + PetrinetInterpreter.indent + className + " " + " ref " +
    astNode.getReferences().toString());
  if (PetrinetInterpreter.indent.length() > 0)
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
      filter(x -> x.getReferences().size() > 0 ||
        x.getFromReferences().size() > 0).collect(Collectors.toSet());
  //System.out.println("GOgo"+pls.stream().map(x->x.getId()).reduce("** ",(x,y)->x+" "+y));
  // glue pairs of places where one has a ref and the other the same fromRef
  for (PetriNetPlace pl1 : pls) {
   if (pl1.getReferences().size() == 0) continue;
   //System.out.println(pl1.myString());
   for (PetriNetPlace pl2 : pls) {
    if (pl2.getFromReferences().size() == 0) continue;

    //System.out.println(pl2.myString());
    if (pl1.getReferences().containsAll(pl2.getFromReferences())) {
     //System.out.println("Joining "+pl1.getId()+" "+pl2.getId());
     pl2.setTerminal("");
     petri.gluePlaces(Collections.singleton(pl1), Collections.singleton(pl2));
    }
   }
  }
  petri.setRootFromStart() ;
 //System.out.println("Tree2Net "+petri.myString());
  //
  return petri;
 }

 private Petrinet interpretASTNode(ASTNode currentNode)
   throws CompilationException, InterruptedException {
  Petrinet fake = new Petrinet("FAKE");
  fake.setOwners(Collections.singleton(Petrinet.DEFAULT_OWNER));
  PetriNetPlace fakePl = new PetriNetPlace("fake");
  fakePl.setOwners(Collections.singleton(Petrinet.DEFAULT_OWNER));
  System.out.println("fake petri "+fake.myString());
  return interpretASTNode(currentNode, fake, fakePl);
 }

 private Petrinet interpretASTNode(ASTNode currentNode, Petrinet petri, PetriNetPlace currentPlace)
   throws CompilationException, InterruptedException {
System.out.println("ASTNode "+petri.myString()+" "+ currentPlace.myString());
  System.out.println("ASTNode " + currentNode.getClass().getSimpleName());

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
  } else if (currentNode instanceof SequenceNode) {
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
  System.out.println("PetriNetCoversion ConversionNode START "+petri.myString());
   petri = interpretConversion((ConversionNode) currentNode, petri, currentPlace);
   //Throwable t = new Throwable();
   //t.printStackTrace();
  //System.out.println("349 OUTSIDE of interpretConversion petri "+petri.myString());

  }
  // Build Stop node with FromRef  (Ref addded by RefReplacer)
  // At end of interpretation tree2net will glued FromRef to Ref
  else if (currentNode instanceof ReferenceNode) {
   String ref = ((ReferenceNode) currentNode).getReference();
   //System.out.println("RRef "+ref);
   if (ref.length() > 0) {
    petri = Petrinet.stopNet(ref);
   }
  }


  //prity print
  if (PetrinetInterpreter.indent.length() > 0)
   PetrinetInterpreter.indent = PetrinetInterpreter.indent.substring(1);
  if (currentNode.getReferences() != null) {
   System.out.println("AST<" + PetrinetInterpreter.indent + className + " ref " +
     currentNode.getReferences().toString());

   for (PetriNetPlace pl : petri.getAllRoots()) {
    pl.addRefefances(currentNode.getReferences());
   }
   //System.out.println(petri.myString());
  } else
   System.out.println("AST<" + PetrinetInterpreter.indent + className);
  // + petri.myString());

  return petri;
 }

 // current place is in petri Just set it as terminal
 private Petrinet interpretTerminal(TerminalNode term) {
  if (term.getTerminal().equals("STOP"))
   return Petrinet.stopNet();
  else
   return Petrinet.errorNet();
 }


 /**
  * Called for A = a->STOP    NOT => (see SequentialInfixFunction
  * currenPlace - label -> nextPlace(new currentPlace)
  *
  * @param seq
  * @throws CompilationException
  * @throws InterruptedException
  */
 private Petrinet interpretSequence(SequenceNode seq)
   throws CompilationException, InterruptedException {

  String lab = seq.getFrom().getAction(); // Now unique see Petrinet.netId
  Petrinet ev = Petrinet.oneEventNet(lab);
  Petrinet petri = interpretASTNode(seq.getTo());  //initially the STOP net
  System.out.println("petri "+petri.myString());
  System.out.println("ev "+ev.myString());
  SequentialInfixFun sif = new SequentialInfixFun();
  Petrinet ret = sif.compose(lab, ev, petri);
System.out.println("SEQUENCE end "+ ret.myString());
  return ret;
 }


 /**
  * @param choice
  * @throws CompilationException
  * @throws InterruptedException
  * @returns a new petrinet
  */
 private Petrinet interpretChoice(ChoiceNode choice)
   throws CompilationException, InterruptedException {


  Petrinet op2 = interpretASTNode(choice.getSecondProcess());
  //temp now hold the first petri Net
  Petrinet op1 = interpretASTNode(choice.getFirstProcess());
  ChoiceFun sif = new ChoiceFun();
  Petrinet ret = sif.compose(op1.getId() + "+" + op2.getId() + ".", op1, op2);
  //System.out.println("CHOICE end "+ ret.myString());
  return ret;
 }

 private void interpretProcessRoot(ProcessRootNode processRoot, Petrinet petri,
                                   PetriNetPlace currentPlace)
   throws CompilationException, InterruptedException {
  //System.out.println("Interpreting Root");
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
  //System.out.println("IDENTIFIER  for "+ identifier.getFromReferences() +" get "+ identifier.getIdentifier());
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
  * @param composite Could be "||" or "=>" or
  * @param petri
  * @return
  * @throws CompilationException
  * @throws InterruptedException
  */

 private Petrinet interpretComposite(CompositeNode composite, Petrinet petri)
   throws CompilationException, InterruptedException {

  //System.out.println("interpretCOMPOSITE "+composite.toString());
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
  //   //System.out.println("processed FUNCTION "+processed.myString());
  //addPetrinet(currentPlace, processed, petri);
  //System.out.println("interpret FUNCTION "+petri.myString());
  return processed;
 }

 /*
 Works for OwnersRule
  */
 private Petrinet interpretConversion(ConversionNode conv, Petrinet petri,
                                      PetriNetPlace currentPlace)
   throws CompilationException, InterruptedException {
  //  currentPlace = petri.getPlaces().get(currentPlace.getId());//only match on "id"

  //System.out.println("interpretConversion P->A start "+petri.myString());
  ProcessType to = ProcessType.valueOf(conv.to.toUpperCase());
  ProcessType from = ProcessType.valueOf(conv.from.toUpperCase());

  ProcessModel pm = new Interpreter().interpret(conv.from, conv.getProcess(),
    petri.getId() + ".pc" + subProcessCount++, processMap, context);

  Petrinet temp = new Petrinet(petri.getId(), false);

  Petrinet p = addPetrinet(currentPlace,
    pm.getProcessType().convertTo(to, pm), temp).copy();
  //System.out.println("interpretConversion  ends "+p.myString());  //not
  return p;

 }

 private Petrinet addPetrinet(PetriNetPlace currentPlace,
                              Petrinet petrinetToAdd, Petrinet master)
   throws CompilationException {
  //Throwable t = new Throwable(); t.printStackTrace();
  //System.out.println("currentPlace "+currentPlace.getId());
  List<String> references = new ArrayList<>();
  //TODO: References appear redundent when everything working remove all refs and check again
  if (currentPlace.getReferences() != null) {
   references.addAll(currentPlace.getReferences());
  }
   petrinetToAdd.validatePNet();
   master.validatePNet();
   //System.out.println("====masterPetrinet======== "+ master.getId());
  List<Set<String>> oldRoot = master.getRoots();
  master.addPetrinet(petrinetToAdd);
    //System.out.println("====masterPetrinet======== "+ master.getId());
  master.validatePNet();
  //System.out.println("master = "+master.myString());
  //add referances to Root Places ? adding to ArrayList I think redundent
  for (Set<String> oldrs : oldRoot) {
     oldrs.stream().map(x->master.getPlaces().get(x)).map(PetriNetPlace::getReferences)
    .filter(Objects::nonNull)
    .forEach(references::addAll);
  }
  return master;
 }

 private Petrinet processLabellingAndRelabelling(Petrinet petri, ProcessRootNode processRoot)
   throws CompilationException, InterruptedException {

  //System.out.println("HAS L " + processRoot.hasLabel() + " has P "+processRoot.hasNewProcess());
  if (processRoot.hasLabel()) {
   //System.out.println("processLabellingAndRelabelling "+processRoot.getLabel());
   petri = PetrinetLabeller.labelPetrinet(petri, processRoot.getLabel());
  }


  if (processRoot.hasRelabelSet()) {
//processes both renaming and refinement
   petri = processRelabelling(petri, processRoot.getRelabelSet());
  }
  return petri;
 }

 private Petrinet processRelabelling(Petrinet petri, RelabelNode relabelSet)
   throws CompilationException {
  //System.out.print("INTERPRET processRelabelling "+petri.myString());
  /*String s = "";
  for (String k : processMap.keySet()) {
   s = "processMap \n    " + k + "->" + processMap.get(k).getId() + "," + processMap.get(k).getProcessType();
  } //System.out.println(s); */
  for (RelabelElementNode r : relabelSet.getRelabels()) {
   //System.out.println("r = "+r.toString());
   if (r.getNewLabel() != null) { //event relabeling
    //System.out.println();
    if (!petri.getAlphabet().keySet().contains(r.getOldLabel())) {
     throw new CompilationException(getClass(), "Cannot find action" + r.getOldLabel()
       + "to relabel.", relabelSet.getLocation());
    }
    petri.relabelTransitions(r.getOldLabel(), r.getNewLabel());
   }
   if (r.getNewProcess() != null) { //event Refinement
        /*System.out.println("REFINEing from processRelabelling "+
            r.getNewProcess().getIdentifier()+"/"+r.getOldLabel() );
        //System.out.println(petri.myString()); */
    //First get the net to replace the events
    Petrinet newPet;
    if (processMap.get(r.getNewProcess().getIdentifier()).getProcessType().
      equals(ProcessType.MULTI_PROCESS)) {
     newPet = (processMap.get(r.getNewProcess().getIdentifier()).getProcessType().
       convertTo(ProcessType.PETRINET, processMap.get(r.getNewProcess().getIdentifier()))); //What a way to extact  a net
    } else {
     newPet = ((Petrinet) processMap.get(r.getNewProcess().getIdentifier()));
    }
    //Second  do the refinement
    RefineFun rf = new RefineFun();
    petri = rf.compose("Ref", r.getOldLabel(), petri, newPet); //
   }
  }
  return petri;
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
