package mc.compiler.interpreters;

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
 *
 * @author Jacob Beal
 * @see AutomatonInterpreter
 * @see mc.processmodels.petrinet.Petrinet
 * @see PetriNetPlace
 * @see PetriNetTransition
 */
public class PetrinetInterpreter implements ProcessModelInterpreter {

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

    if (!petrinet.getId().equalsIgnoreCase(processNode.getIdentifier())) {
      petrinet.setId(processNode.getIdentifier());
    }

    if (processNode.hasRelabels()) {
      processRelabelling(petrinet, processNode.getRelabels());
    }

    if (processNode.hasHiding()) {
      processHiding(petrinet, processNode.getHiding());
    }

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
    interpretASTNode(astNode, petrinet, currentPlace);
    processStack.push(petrinet);
  }

  private void interpretASTNode(ASTNode currentNode, Petrinet petri, PetriNetPlace currentPlace)
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

    if (currentNode instanceof ProcessRootNode) {
      interpretProcessRoot((ProcessRootNode) currentNode, petri, currentPlace);
      return;
    }
    if (currentNode instanceof SequenceNode) {
      interpretSequence((SequenceNode) currentNode, petri, currentPlace);
      return;
    }
    if (currentNode instanceof TerminalNode) {
      interpretTerminal((TerminalNode) currentNode, petri, currentPlace);
      return;
    }
    if (currentNode instanceof ChoiceNode) {
      interpretChoice((ChoiceNode) currentNode, petri, currentPlace);
      return;
    }
    if (currentNode instanceof IdentifierNode) {
      interpretIdentifier((IdentifierNode) currentNode, petri, currentPlace);
      return;
    }
    if (currentNode instanceof CompositeNode) {
      interpretComposite((CompositeNode) currentNode, petri, currentPlace);
      return;
    }
    if (currentNode instanceof FunctionNode) {
      interpretFunction((FunctionNode) currentNode, petri, currentPlace);
      return;
    }
    if (currentNode instanceof ConversionNode) {
      interpretConversion((ConversionNode) currentNode, petri, currentPlace);
    }

  }


  private void interpretSequence(SequenceNode seq, Petrinet petri, PetriNetPlace currentPlace)
      throws CompilationException, InterruptedException {
    String action = seq.getFrom().getAction();

    Set<String> fakeOwner = new HashSet<>();
    fakeOwner.add(Petrinet.DEFAULT_OWNER);

    if (seq.getTo() instanceof ReferenceNode) {
      ReferenceNode ref = (ReferenceNode) seq.getTo();
      Collection<PetriNetPlace> nextPlaces = referenceMap.get(ref.getReference());

      if (nextPlaces == null) {
        throw new CompilationException(getClass(),
            "The petrinet attempted to enter an invalid reference", seq.getTo().getLocation());
      }

      for (PetriNetPlace nextPlace : nextPlaces) {
        PetriNetTransition transition = petri.addTransition(action);


        petri.addEdge(transition, currentPlace, fakeOwner);
        petri.addEdge(nextPlace, transition, fakeOwner);
      }

      //do something
    } else {
      PetriNetPlace nextPlace = petri.addPlace();
      PetriNetTransition transition = petri.addTransition(action);

      petri.addEdge(transition, currentPlace, fakeOwner);
      petri.addEdge(nextPlace, transition, fakeOwner);
      interpretASTNode(seq.getTo(), petri, nextPlace);
    }
  }

  private void interpretTerminal(TerminalNode term, Petrinet petri, PetriNetPlace currentPlace) {
    currentPlace.setTerminal(term.getTerminal());
  }

  private void interpretChoice(ChoiceNode choice, Petrinet petri, PetriNetPlace currentPlace)
      throws CompilationException, InterruptedException {
    interpretASTNode(choice.getFirstProcess(), petri, currentPlace);
    interpretASTNode(choice.getSecondProcess(), petri, currentPlace);
  }

  private void interpretProcessRoot(ProcessRootNode processRoot, Petrinet petri,
                                    PetriNetPlace currentPlace)
      throws CompilationException, InterruptedException {
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
    ProcessModel model = processMap.get(identifier.getIdentifier());
    if (!(model instanceof Petrinet)) {
      throw new CompilationException(getClass(), "Unable to find petrinet for identifier: "
          + identifier.getIdentifier(), identifier.getLocation());
    }
    Petrinet copy = ((Petrinet) model).copy();
    addPetrinet(currentPlace, copy, petri);
  }

  private void interpretComposite(CompositeNode composite, Petrinet petri,
                                  PetriNetPlace currentPlace)
      throws CompilationException, InterruptedException {
    interpretProcess(composite.getFirstProcess(), petri.getId() + ".pc1");
    interpretProcess(composite.getSecondProcess(), petri.getId() + ".pc2");

    ProcessModel model2 = processStack.pop();
    ProcessModel model1 = processStack.pop();

    if (model1 == null || model2 == null) {
      throw new CompilationException(getClass(), "Expecting a petrinet in composite "
          + petri.getId(), composite.getLocation());
    }


    Petrinet comp = instantiateClass(infixFunctions.get(composite.getOperation()))
        .compose(model1.getId() + composite.getOperation() + model2.getId(), (Petrinet) model1,
            (Petrinet) model2);

    addPetrinet(currentPlace, comp, petri);
  }

  private void interpretFunction(FunctionNode func, Petrinet petri, PetriNetPlace currentPlace)
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

    addPetrinet(currentPlace, processed, petri);

  }

  private void interpretConversion(ConversionNode conv, Petrinet petri,
                                   PetriNetPlace currentPlace)
      throws CompilationException, InterruptedException {
    ProcessType to = ProcessType.valueOf(conv.to.toUpperCase());
    ProcessType from = ProcessType.valueOf(conv.from.toUpperCase());

    ProcessModel pm = new Interpreter().interpret(conv.from, conv.getProcess(),
        petri.getId() + ".pc" + subProcessCount++, processMap, context);

    addPetrinet(currentPlace, petri, pm.getProcessType().convertTo(to, pm));
  }

  private void addPetrinet(PetriNetPlace currentPlace, Petrinet petrinetToAdd, Petrinet master)
      throws CompilationException {
    List<String> references = new ArrayList<>();
    //TODO: References
    if (currentPlace.getReferences() != null) {
      references.addAll(currentPlace.getReferences());
    }

    System.out.println(petrinetToAdd);
    System.out.println("==============");

    Set<PetriNetPlace> places = master.addPetrinet(petrinetToAdd);

    Set<PetriNetPlace> newStart = master.gluePlaces(Collections.singleton(currentPlace), places);

    places.stream()
        .map(PetriNetPlace::getReferences)
        .filter(Objects::nonNull)
        .forEach(references::addAll);

    references.forEach(id -> referenceMap.replace(id, newStart));

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
