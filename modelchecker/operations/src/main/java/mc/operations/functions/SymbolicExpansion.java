package mc.operations.functions;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.microsoft.z3.Context;
import mc.compiler.Guard;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

import java.util.*;
import java.util.stream.Collectors;

public class SymbolicExpansion implements IProcessFunction {
  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)}).
   *
   * @return the name of the function
   */
  @Override
  public String getFunctionName() {
    return "expand";
  }

  /**
   * Get the available flags for the function described by this interface (e.g. {@code unfair} in
   * {@code abs{unfair}(A)}.
   *
   * @return a collection of available flags (note, no variables may be flags)
   */
  @Override
  public Collection<String> getValidFlags() {
    return Collections.singleton("*");
  }

  /**
   * Gets the number of automata to parse into the function.
   *
   * @return the number of arguments
   */
  @Override
  public int getNumberArguments() {
    return 1;
  }

  Map<String, String> variable2Owner = new TreeMap<>();

  /**
   * Execute the function on automata.
   *
   * @param id       the id of the resulting automaton
   * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   */
  @Override
  public Automaton compose(String id, Set<String> flags, Context context, Automaton... automata)
    throws CompilationException {


    return null;
  }

  /**
   * SymbolicExpansion   replaces a variable with a finite set of nodes this may be an
   *  approximation when the type of the variable is a not an enumerated set.
   * Note a totaly symbolic Petri Net has a single node and is no more that a set of transitions
   * just like an Event-B machine. Reachablility is hidden with this representation.
   *
   * When expanding some of the  variables  mimic the TokenRule as below.
   * New style Marking options
   * stateExpansion:
   *     o_x place expanded to single {pn_o, eval_o} eval_o an evaluation
   *     of var x owned by o_x. This expands the state space of o_x
   * tokenExpansion:
   *     o_x place split into two owners - places pn_o, eval_o.
   *     This preserved the statspace of each owner - expands the number of owners
   * Start by build new style Root   from the root evaluation and pushing to pending Stack
   *     while pending not empty
   *        pop pending
   *        perform finite state expansion over the required varaibles
   *        foreach symbolicNext Transition  tr
   *          step1 using the predicate defining the finite state expansion as tr post conditions
   *          step2 apply Hoare logice to compute the tr pre condition
   *                add any existing conditions on pre-tr Places and simplify
   *          step3 for any precondition not equal to false add the transition
   *          step4 compute a new style post marking and push to pending
   *            computing the post tr for un expanded variables is not needed.
   *          Use Expression.
   *
   *
   * With a finite state expansion into n-states will result in n^2 transitions being considered for
   * each transition. The transitions with precondition false can be dropped. At certain points
   * unreachable states may be eliminated hence m^2 transitions where m <n need to be considered.
   *
   * Symbolic execution is not (easily) computable for partial evaluations
   * HoarLogic is computable but runs backwards
   *
   * partial evaluation.
   * Finte state expanition of the variable {y=1,y=2,...} Then using these as post conditions apply Hoare logic to compute
   * the guard needed to get their.
   *
   *
   *
   * For symbolic Automata adjust the OwnersRule
   *
   * building the original Net plus a Net for the varibale
   * then compose them in parallel.
   * This adds one additional process per variable.
   *
   * @param id        the id of the resulting petrinet
   * @param flags     the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context
   * @param petrinets the variable number of petrinets taken in by the function
   * @return the resulting petrinet of the operation
   * @throws CompilationException when the function fails
   */
  @Override
  public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {

    System.out.println("SYMBOLICExpansion  " + id + " flags " + flags);
    Petrinet pin = petrinets[0].copy();
    Petrinet pout = new Petrinet(pin.getId() + flags, false);
    System.out.println("EXPAND input " + pin.myString("edge"));
    Map<Multiset<PetriNetPlace>, Multiset<PetriNetPlace>> markingToNewStyleMarking = new HashMap<>();
    variable2Owner = pin.getVariable2Owner();

    Stack<Multiset<PetriNetPlace>> markingStack = new Stack<>();
    List<Set<PetriNetPlace>> newRoots = new ArrayList<>();
    for (Set<PetriNetPlace> root : pin.getRootPlacess()) {
      Set<PetriNetPlace> newRoot = rootExpansion(pin.getRootEvaluation(), root, flags);
      markingStack.push(HashMultiset.create(newRoot));
      newRoots.add(newRoot);
      System.out.println(newRoot.stream().map(x -> x.getId() + " ").collect(Collectors.joining()));
    }
    while (!markingStack.isEmpty()) {
      Multiset<PetriNetPlace> currentMarking = markingStack.pop();
      Set<PetriNetTransition> nextTrans = Petrinet.postTransitions(currentMarking.elementSet());
      for(PetriNetTransition tr:nextTrans){
        Guard first = new Guard();
        for(PetriNetEdge edge: tr.getIncoming()){

        }

      }
    }
    pout.setRootsPl(newRoots);
    return pout;
  }

  @Override
  public MultiProcessModel compose(String id, Set<String> flags, Context context, MultiProcessModel... multiProcess) throws CompilationException {
    return null;
  }

  /**
   * Event Refinement can build multiset Markings!  Two Tokens with the same owner.
   * Treat each token as a seperate entity with a seperate variable.
   * <p>
   * New Root = oldRoot +one Place per evaluation of a variable in vars
   */
  private Set<PetriNetPlace> rootExpansion(Map<String, String> rootEvaluation, Set<PetriNetPlace> oldRoot, Set<String> vars) {
    Iterator<PetriNetPlace> iter = oldRoot.iterator();
    Set<PetriNetPlace> newRoot = new HashSet<>();
    System.out.println("vars " + vars);
    System.out.println("rootEv " + rootEvaluation);
    Multiset<String> varsOwners = HashMultiset.create(vars.stream().map(x -> variable2Owner.get("$" + x)).collect(Collectors.toSet()));
    System.out.println("varOwners " + varsOwners.toString());

    while (iter.hasNext()) {
      PetriNetPlace pl = iter.next();
      newRoot.add(pl);
      System.out.println("pl " + pl.getId());
    }
    for (String var : vars) {

      if (rootEvaluation.containsKey(var)) {
        String val = rootEvaluation.get(var);
        PetriNetPlace pl = new PetriNetPlace(var + "." + val);
        newRoot.add(pl);
      } else {
        System.out.println("WARNING DATA INCOSISTENT");
      }
    }
    System.out.println("rootExpansion " + newRoot.stream().map(x -> x.myString() + "\n").collect(Collectors.joining()));
    return newRoot;
  }

  /**

   */
  private Multiset<PetriNetPlace> stateExpansion(Multiset<PetriNetEdge> oldEdges, Set<String> vars) {
    Iterator<PetriNetEdge> iter = oldEdges.iterator();
    Multiset<PetriNetPlace> newMarking = HashMultiset.create();
    System.out.println("vars " + vars);
    System.out.println("v2o " + variable2Owner.toString());
    Multiset<String> varsOwners = HashMultiset.create(vars.stream().map(x -> variable2Owner.get("$" + x)).collect(Collectors.toSet()));
    System.out.println("varOwners " + varsOwners.toString());


    return null;
  }

}

