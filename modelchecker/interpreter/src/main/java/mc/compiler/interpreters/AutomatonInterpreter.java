package mc.compiler.interpreters;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.rits.cloning.Cloner;
import mc.Constant;
import mc.compiler.Guard;
import mc.compiler.Interpreter;
import mc.compiler.ast.*;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.plugins.IProcessInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.operations.AutomataLabeller;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static mc.processmodels.ProcessType.AUTOMATA;
import static mc.util.Utils.instantiateClass;

//import mc.compiler.LocalCompiler;
/**
 * When refactored BEWARE of stack of pointers not stack of MODELS
 */

/**
 * Builds automata from AST assumes
 *   non symbolic (non hidden) variables have been expanded in the AST
 */
public class AutomatonInterpreter implements ProcessModelInterpreter {
  static String indent = "";
//TODO  Interpreter is  HACK  to be completely refactored
  static Map<String, Class<? extends IProcessFunction>> functions = new HashMap<>();
  static Map<String, Class<? extends IProcessInfixFunction>> infixFunctions = new HashMap<>();

  private Map<String, ProcessModel> processMap;
  private Multimap<String, AutomatonNode> referenceMap;
  private Stack<ProcessModel> processStack;
  private VariableSetNode variables;
  //private LocalCompiler compiler;
  private Set<String> variableList;
  private Context context;
  private int subProcessCount = 0;
  private Set<AutomatonNode> subProcessStartNodes;

  public AutomatonInterpreter() {
    reset();
  }

  @Override
  public ProcessModel interpret(ProcessNode processNode,
                                Map<String, ProcessModel> processMap,
                                Context context,
                                Set<String> alpha,
                                Map<String,Expr> globalVarMap,
                                boolean symb)
      throws CompilationException, InterruptedException, ExecutionException {
   //System.out.println("START interp ");
    reset();
    this.context = context;
    //this.compiler = compiler;
    this.variableList = new HashSet<>();
    this.processMap = processMap;
    String identifier = processNode.getIdentifier();
    this.variables = processNode.getSymbolicVariables();

    interpretProcess(processNode.getProcess(), identifier);

    ProcessModel pm = processStack.pop();
    Automaton automaton = ((Automaton) pm.getProcessType().convertTo(AUTOMATA, pm)).copy();
   //System.out.println("START interp "+automaton.myString());
    //Set the id correctly if there is a processes like this: C = B., otherwise it just takes B's id.
    if (!automaton.getId().equals(processNode.getIdentifier())) {
      automaton.setId(processNode.getIdentifier());
    }

   //System.out.println(">>XX>> lab start "+automaton.myString());
    if (processNode.hasRelabels()) {
      processRelabelling(automaton, processNode.getRelabels());
    }

   //System.out.println(">>XX>> lab end   "+automaton.myString());

    if (processNode.hasHiding()) {
      processHiding(automaton, processNode.getHiding());
    }
   //System.out.println(">>XX>> hide end  "+automaton.myString());

    automaton = labelAutomaton(automaton); //ok

   //System.out.println(">>XX>> ???? end  "+automaton.myString());

    return automaton;
  }

  public ProcessModel interpretEvalOp(ASTNode astNode,
                                String identifier,
                                Map<String, ProcessModel> processMap,
                                Context context,
                                Set<String> alpha)
      throws CompilationException, InterruptedException, ExecutionException {
    //System.out.println("Start Automata interpret "+ identifier);
    reset();
    this.context = context;
    this.processMap = processMap;

    interpretProcess(astNode, identifier);

    ProcessModel pm = processStack.pop();
    //System.out.println("Convert  P->A");
    Automaton auto = ((Automaton) pm.getProcessType().convertTo(AUTOMATA, pm));
    Automaton automaton = auto.copy();
    //automaton.getEdges().forEach(e -> System.out.println("owners of " + e.getId() + " are " + e.getEdgeOwners()));
    //System.out.println("AutomatonInterperter");
    //System.out.println(automaton.toString());
    return labelAutomaton(automaton);
  }

  /**
   * saves  automata - models - to the processStack
   * @param astNode
   * @param identifier
   * @throws CompilationException
   * @throws InterruptedException
   */
  private void interpretProcess(ASTNode astNode, String identifier)
      throws CompilationException, InterruptedException, ExecutionException {
    Cloner cloner=new Cloner();
   //System.out.println("interpretProcess in AutInt astNode "+astNode.toString());
    if (astNode instanceof IdentifierNode) {
     //System.out.println("astNode IdentifierNode");
      String reference = ((IdentifierNode) astNode).getIdentifier()+":"+
                         ((IdentifierNode) astNode).getDomain();
          ProcessModel model = processMap.get(reference);
          processStack.push(cloner.deepClone(model));

    } else if (astNode instanceof ProcessRootNode) {
     //System.out.println("astNode ProcessRootNode");


      // automata already on stact so pop it off  relabel and push back on stack
      ProcessRootNode root = (ProcessRootNode) astNode;

      interpretProcess(root.getProcess(), identifier);

      ProcessModel pm = processStack.pop();
      Automaton automaton = ((Automaton) pm.getProcessType().convertTo(AUTOMATA, pm)).copy();

     //System.out.println("Aut Built0 "+ automaton.myString());
      automaton = processLabellingAndRelabelling(automaton, root);
      if (root.hasHiding()) {
        processHiding(automaton, root.getHiding());
    //    automaton.setHiding(root.getHiding());
      }


      processStack.push(cloner.deepClone(automaton));
      //processStack.push(automaton);
     //System.out.println("Aut Built1 "+ automaton.myString());
    } else {
     System.out.println("Building Automaton "+identifier);
      Automaton automaton = new Automaton(identifier);
      if (variables != null) {
        automaton.setHiddenVariables(variables.getVariables());
        automaton.setHiddenVariablesLocation(variables.getLocation());
      }

      automaton.setVariables(variableList);
      automaton.setVariablesLocation(astNode.getLocation());

      interpretNode(astNode, automaton, new ArrayList<>(automaton.getRoot()).get(0));
    //System.out.println("pushing "+automaton.myString());
      processStack.push(cloner.deepClone(automaton));
  //System.out.println("Aut Built2 "+ automaton.myString());
    }

  }

  /**
   * Recursivly interpret AST nodes
   * @param astNode
   * @param automaton
   * @param currentNode
   * @throws CompilationException
   * @throws InterruptedException
   */
  private void interpretNode(ASTNode astNode, Automaton automaton, AutomatonNode currentNode)
      throws CompilationException, InterruptedException, ExecutionException {
    if (Thread.currentThread().isInterrupted()) {
      throw new InterruptedException();
    }
    String info = "";
    AutomatonInterpreter.indent = AutomatonInterpreter.indent.concat("-");
    String className = astNode.getClass().getSimpleName();
    //System.out.println("Aut " + PetrinetInterpreter.indent + className );

    //System.out.println("WHAT "+className);
    // check if the current ast node has a reference attached
    if (astNode.hasReferences()) {
      for (String reference : astNode.getReferences()) {
        referenceMap.replaceValues(reference, Collections.singletonList(currentNode));
      }

      currentNode.setReferences(astNode.getReferences());
    }
    if (astNode.getModelVariables() != null && !astNode.getModelVariables().keySet().isEmpty()) {
      Map<String, Object> varMap = astNode.getModelVariables();
      varMap.keySet().stream()
          .map(s -> s.substring(1))
          .forEach(variableList::add);
      currentNode.setVariables(varMap);
    }
    currentNode.copyPropertiesFromASTNode(astNode);

    if (astNode instanceof ProcessRootNode) {
      interpretProcessRoot((ProcessRootNode) astNode, automaton, currentNode);
      info = ((ProcessRootNode) astNode).getLabel();
    } else if (astNode instanceof SequenceNode) {
      interpretSequence((SequenceNode) astNode, automaton, currentNode);
      info = "=>";
    } else if (astNode instanceof ChoiceNode) {
      interpretChoice((ChoiceNode) astNode, automaton, currentNode);
      info = "[]";
    } else if (astNode instanceof CompositeNode) {
      interpretComposite((CompositeNode) astNode, automaton, currentNode);
      info = ((CompositeNode) astNode).getOperation();
    } else if (astNode instanceof IdentifierNode) {
      interpretIdentifier((IdentifierNode) astNode, automaton, currentNode);
      info = ((IdentifierNode) astNode).getIdentifier();
    } else if (astNode instanceof FunctionNode) {
      interpretFunction((FunctionNode) astNode, automaton, currentNode);
      info = ((FunctionNode) astNode).getFunction();
    } else if (astNode instanceof TerminalNode) {
      info = ((TerminalNode) astNode).getTerminal();
      interpretTerminalNode((TerminalNode) astNode, automaton, currentNode);
    } else if (astNode instanceof ConversionNode) {
      interpretConversion((ConversionNode) astNode, automaton, currentNode);
      info = ((ConversionNode) astNode).from +" *> "+ ((ConversionNode) astNode).to;
    }
    if (AutomatonInterpreter.indent.length() > 0)
      AutomatonInterpreter.indent = AutomatonInterpreter.indent.substring(1);
    System.out.println("Aut<" + AutomatonInterpreter.indent + className+ " info "+info);
  }

  private void interpretProcessRoot(ProcessRootNode astProcessRootNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException, InterruptedException, ExecutionException {
    interpretProcess(astProcessRootNode.getProcess(), automaton.getId() + "." + ++subProcessCount);
    Automaton model = ((Automaton) processStack.pop()).copy();
    processLabellingAndRelabelling(model, astProcessRootNode);

    if (astProcessRootNode.hasHiding()) {
      processHiding(model, astProcessRootNode.getHiding());
    }


    Set<AutomatonNode> oldRoot = automaton.addAutomaton(model);
    Set<AutomatonNode> nodes = automaton.combineNondeterministic(currentNode, oldRoot, context);
    subProcessStartNodes = nodes;
  }
/*
  Sequenc node is astNode and has guard set on it
  Only place Edges are added  Guard is on AST node SequenceNode
  Only place Nodes are added
 */

  private void interpretSequence(SequenceNode sequence, Automaton automaton,
                                 AutomatonNode currentNode)
      throws CompilationException, InterruptedException, ExecutionException {

    String action = sequence.getEventLabel().getAction();
//  if (currentNode.getGuard()!=null && sequence.getGuard()!= null && currentNode.getGuard()==sequence.getGuard()){
//    System.out.println("OPPS!");
//  }
//    if (currentNode !=null && currentNode.getGuard()!=null){
//System.out.print("interpretSequence  current"+currentNode.getGuard().myString()+"\n");
//    } else {System.out.print("interpretSequence current = null\n");}
//    if (sequence!=null && (Guard) sequence.getGuard()!= null){
//System.out.print("interpretSequence  sequence" + ((Guard) sequence.getGuard()).myString() + "\n");
//    } else {System.out.print("interpretSequence sequence = null\n");}

    AutomatonNode nextNode;
    Guard foundGuard = null;
    if (currentNode.getGuard() != null) {  // WHY do this?
      foundGuard = (Guard) sequence.getGuard();
      currentNode.setGuard(null);
    }
    // check if the next ast node is a reference node
    // edge from currentNode to nextNode
    if (sequence.getTo() instanceof ReferenceNode) {
      ReferenceNode reference = (ReferenceNode) sequence.getTo();
      Collection<AutomatonNode> nextNodes = referenceMap.get(reference.getReference());
      for (AutomatonNode node : nextNodes) {
        automaton.addEdge(action, currentNode, node, foundGuard, true,false);
      }

    } else {

      nextNode = automaton.addNode();
      automaton.addEdge(action, currentNode, nextNode, foundGuard, true,false);

      interpretNode(sequence.getTo(), automaton, nextNode);
    }
  }

  //STRANGE  Hack
  private void interpretChoice(ChoiceNode astChoiceNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException, InterruptedException, ExecutionException {

  //System.out.println(astChoiceNode.getFirstProcess());
  //System.out.println("XX "+automaton.toString());
    interpretNode(astChoiceNode.getFirstProcess(), automaton, currentNode);
  //System.out.println("XX XX "+automaton.toString());


    //This is a special case whereby the currentNode is deleted by adding a process that destroys
    //the value of the currentNode
    if (!automaton.getNodes().contains(currentNode)) {

      for (AutomatonNode automatonNode : subProcessStartNodes) {

        interpretNode(astChoiceNode.getSecondProcess(), automaton, automatonNode);
   //System.out.println("X??XX "+automaton.toString());
      }
      return;
    }
  //System.out.println(astChoiceNode.getSecondProcess());
    interpretNode(astChoiceNode.getSecondProcess(), automaton, currentNode);
  //System.out.println("XX XX "+automaton.toString());

  }

  /**
   * All infixed notation  || | => +
   * @param astCompositeNode
   * @param automaton
   * @param currentNode
   * @throws CompilationException
   * @throws InterruptedException
   */
  private void interpretComposite(CompositeNode astCompositeNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException, InterruptedException, ExecutionException {
    //System.out.println("interpretComposite \n\n");
    interpretProcess(astCompositeNode.getFirstProcess(), automaton.getId() + ".pc1");
    interpretProcess(astCompositeNode.getSecondProcess(), automaton.getId() + ".pc2");

    ProcessModel model2 = processStack.pop();
    ProcessModel model1 = processStack.pop();

    model1 = model1.getProcessType().convertTo(AUTOMATA, model1);

    model2 = model2.getProcessType().convertTo(AUTOMATA, model2);


    if (!(model1 instanceof Automaton) || !(model2 instanceof Automaton)) {
      if (model1 == null || model2 == null){ // They were not set to be constructed as anything
        throw new CompilationException(getClass(), "Expecting an automaton in composite " + automaton.getId(), astCompositeNode.getLocation());
      } else {
        throw new CompilationException(getClass(), "Expecting an automaton, received: " + model1.getClass().getSimpleName() + "," + model2.getClass().getSimpleName(), astCompositeNode.getLocation());
      }
    }
    Automaton.relabelOwners((Automaton) model1,"._1");
    Automaton.relabelOwners((Automaton) model2,"._2");

    //System.out.println(astCompositeNode.getOperation());
    Automaton comp = instantiateClass(infixFunctions.get(astCompositeNode.getOperation()))
        .compose(model1.getId() + astCompositeNode.getOperation() + model2.getId(), (Automaton) model1, (Automaton) model2);


    Set<AutomatonNode> oldRoot = automaton.addAutomaton(comp);
    subProcessStartNodes = automaton.combineNondeterministic(currentNode, oldRoot, context);

  }

  private void interpretIdentifier(IdentifierNode identifierNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException, InterruptedException, ExecutionException {
    // check that the reference is to an automaton
    ProcessModel model = processMap.get(identifierNode.getIdentifier()+":"+
                                        identifierNode.getDomain());
    Automaton next = model.getProcessType().convertTo(AUTOMATA, model);

    addAutomaton(currentNode, automaton, next);
  }

  private void interpretFunction(FunctionNode astFunctionNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException, InterruptedException, ExecutionException {
    List<ProcessModel> models = new ArrayList<>();
    for (ASTNode p : astFunctionNode.getProcesses()) {
      interpretProcess(p, automaton.getId() + ".fn");
      models.add(processStack.pop());
    }

    if (models.isEmpty()) {
      throw new CompilationException(getClass(), "Expecting an automaton, received an undefined process.", astFunctionNode.getLocation());
    }

    for (ProcessModel model : models) {
      if (!(model instanceof Automaton)) {
        throw new CompilationException(getClass(), "Expecting an automaton, received a: " + model.getClass().getSimpleName(), astFunctionNode.getLocation());
      }
    }

    Automaton[] aut = models.stream().map(Automaton.class::cast).toArray(Automaton[]::new);
    Automaton processed = instantiateClass(functions.get(astFunctionNode.getFunction()))
        .compose(automaton.getId() + ".fn", astFunctionNode.getFlags(), context, aut);

    addAutomaton(currentNode, automaton, processed);
  }

  private void interpretTerminalNode(TerminalNode astNode, Automaton automaton, AutomatonNode currentNode) {
    if (astNode.getTerminal().equals(Constant.STOP)) currentNode.setStopNode(true);
    else currentNode.setStopNode(false);
    if (astNode.getTerminal().equals(Constant.ERROR)) currentNode.setErrorNode(true);
    else currentNode.setErrorNode(false);
    //currentNode.setTerminal(astNode.getTerminal());
  }

  private void interpretConversion(ConversionNode conv, Automaton automaton,
                                   AutomatonNode currentNode)
      throws CompilationException, InterruptedException, ExecutionException {
  //System.out.println(" interpretConversion A->P start "+automaton.toString());
    ProcessType to = ProcessType.valueOf(conv.to.toUpperCase());
    ProcessType from = ProcessType.valueOf(conv.from.toUpperCase());

    ProcessModel pm = new Interpreter().interpretForOperations(conv.from,conv.getProcess(),
            automaton.getId() + ".pc" + subProcessCount++,processMap,context, new TreeSet<String>());
    addAutomaton(currentNode, automaton, pm.getProcessType().convertTo(to,pm));
  }

  private Automaton processLabellingAndRelabelling(Automaton automaton, ProcessRootNode astProcessRootNode) throws CompilationException {
    if (astProcessRootNode.hasLabel()) {
      automaton = AutomataLabeller.labelAutomaton(automaton, astProcessRootNode.getLabel());
    }
    if (astProcessRootNode.hasRelabelSet()) {
      processRelabelling(automaton, astProcessRootNode.getRelabelSet());
    }

    return automaton;
  }

  private void addAutomaton(AutomatonNode currentNode, Automaton automaton1, Automaton automaton2) throws CompilationException, InterruptedException, ExecutionException {

    List<String> references = new ArrayList<>();


    if (currentNode.getReferences() != null) {
      references.addAll(currentNode.getReferences());
    }

    Set<AutomatonNode> oldRoots = automaton1.addAutomaton(automaton2);

    subProcessStartNodes = automaton1.combineNondeterministic(currentNode, oldRoots, context);

    for (AutomatonNode oldRoot : oldRoots) {
      if (oldRoot.getReferences() != null) {
        references.addAll(oldRoot.getReferences());
      }
    }

    //Combining nodes removes the inital currentNode and oldRoot, as we are still using currentNode,
    //copy the properties of the newly created node across

    //TODO:

    references.forEach(id -> referenceMap.replaceValues(id, new ArrayList<>(subProcessStartNodes)
        .subList(0, 0)));


  }

  private void processRelabelling(Automaton automaton, RelabelNode relabels) {
    automaton.setAlphabetBeforeHiding(new HashSet<>(automaton.getAlphabet()));
    automaton.setRelabels(relabels.getRelabels());

    for (RelabelElementNode element : relabels.getRelabels()) {
      automaton.relabelEdges(element.getOldLabel(), element.getNewLabel());
    }

  }

  private void processHiding(Automaton automaton, HideNode hiding) throws CompilationException {
    Set<String> alphabet = automaton.getAlphabet();
    if (automaton.getAlphabetBeforeHiding() == null) {
      automaton.setAlphabetBeforeHiding(new HashSet<>(automaton.getAlphabet()));
    }

    Set<String> hidden = new HashSet<>(hiding.getSet().getSet());
    String type = hiding.getType();
    if (type.equals("includes")) {
      for (String action : hidden) {
        if (alphabet.contains(action)) {
          automaton.relabelEdges(action, Constant.HIDDEN);
        } else {
          throw new CompilationException(AutomatonInterpreter.class, "Unable to find action " + action + " for hiding.", hiding.getLocation());
        }
      }
    } else if (type.equals("excludes")) {
      new ArrayList<>(alphabet).stream().filter(action -> !hidden.contains(action)).forEach(action -> {
        automaton.relabelEdges(action, Constant.HIDDEN);
      });
    }
  }

  private static Automaton labelAutomaton(Automaton automaton) {
    Set<String> visited = new HashSet<>();

    Queue<AutomatonNode> fringe = new LinkedList<>();
    automaton.getRoot().forEach(fringe::offer);

    int label = 0;
    while (!fringe.isEmpty()) {
      AutomatonNode current = fringe.poll();

      if (visited.contains(current.getId())) {
        continue;
      }


      current.getOutgoingEdges().forEach(edge -> fringe.offer(edge.getTo()));
      current.setLabelNumber(label++);

      visited.add(current.getId());
    }

    return automaton;
  }

  private void reset() {
    this.referenceMap = MultimapBuilder.hashKeys().hashSetValues().build();
    this.processStack = new Stack<>();
  }

}
