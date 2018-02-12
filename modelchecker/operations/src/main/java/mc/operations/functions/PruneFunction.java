package mc.operations.functions;

import com.microsoft.z3.Context;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.operations.AutomataReachability;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

public class PruneFunction implements IProcessFunction {
  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)}).
   *
   * @return the name of the function
   */
  @Override
  public String getFunctionName() {
    return "prune";
  }

  /**
   * Get the available flags for the function described by this interface (e.g. {@code unfair} in
   * {@code abs{unfair}(A)}.
   *
   * @return a collection of available flags (note, no variables may be flags)
   */
  @Override
  public Collection<String> getValidFlags() {
    return Collections.emptySet();
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

  /**
   * Execute the function on automata.
   *
   * @param id       the id of the resulting automaton
   * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context  the z3 context to execute guards
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   * @throws CompilationException when the function fails
   */
  @Override
  public Automaton compose(String id, Set<String> flags, Context context, Automaton... automata)
      throws CompilationException {
    // find the hidden edges within the automaton
    Automaton automaton = automata[0];
    List<AutomatonEdge> hiddenEdges = automaton.getEdges().stream()
        .filter(AutomatonEdge::isHidden)
        .collect(Collectors.toList());

    // if there are no hidden edges then there is nothing to prune
    if (hiddenEdges.isEmpty()) {
      return automaton;
    }

    for (AutomatonEdge hiddenEdge : hiddenEdges) {
      AutomatonNode from = hiddenEdge.getFrom();
      AutomatonNode to = hiddenEdge.getTo();

      List<AutomatonEdge> incomingHiddenEdges = from.getIncomingEdges().stream()
          .filter(AutomatonEdge::isHidden)
          .collect(Collectors.toList());

      // if there are incoming hidden edges then we cannot prune the current edge
      if (!incomingHiddenEdges.isEmpty()) {
        continue;
      }

      List<AutomatonEdge> outgoingHiddenEdges = to.getOutgoingEdges().stream()
          .filter(AutomatonEdge::isHidden)
          .collect(Collectors.toList());

      // if there are outgoing hidden edges then we cannot prune the current edge
      if (!outgoingHiddenEdges.isEmpty()) {
        continue;
      }

      // since there are no incoming or outgoing hidden edges we can merge the two nodes
      try {
        automaton.combineNodes(from, to, context);
      } catch (InterruptedException ignored) {
        throw new CompilationException(getClass(), ignored.getMessage());
      }
      automaton.removeEdge(hiddenEdge);
    }

    return AutomataReachability.removeUnreachableNodes(automaton);
  }

  /**
   * TODO:
   * Execute the function on one or more petrinet.
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
    assert petrinets.length == 1;
    Petrinet petri = petrinets[0].copy();

    Stack<Set<PetriNetPlace>> toDo = new Stack<>();
    toDo.push(petri.getRoots());

    Set<Set<PetriNetPlace>> previouslyVisitedPlaces = new HashSet<>();
    Set<PetriNetPlace> visitedPlaces = new HashSet<>();
    Set<PetriNetTransition> visitedTransitions = new HashSet<>();

    while (!toDo.isEmpty()) {
      Set<PetriNetPlace> currentMarking = toDo.pop();

      visitedPlaces.addAll(currentMarking);

      if (previouslyVisitedPlaces.contains(currentMarking)) {
        continue;
      }


      Set<PetriNetTransition> satisfiedPostTransitions = satisfiedTransitions(currentMarking);

      for (PetriNetTransition transition : satisfiedPostTransitions) {
        visitedTransitions.add(transition);

        Set<PetriNetPlace> newMarking = new HashSet<>(currentMarking);


        // Clear out the places in the current marking which are moving token
        newMarking.removeAll(transition.pre());

        newMarking.addAll(transition.getOutgoing().stream()
            .map(PetriNetEdge::getTo)
            .map(PetriNetPlace.class::cast)
            .collect(Collectors.toList()));

        if (!visitedPlaces.contains(newMarking)) {
          toDo.add(newMarking);
        }
      }
      previouslyVisitedPlaces.add(currentMarking);
    }
    Set<PetriNetPlace> placesToRemove = new HashSet<>(petri.getPlaces().values());
    placesToRemove.removeAll(visitedPlaces);
    Set<PetriNetTransition> transitionsToRemove = new HashSet<>(petri.getTransitions().values());
    transitionsToRemove.removeAll(visitedTransitions);

    for (PetriNetPlace p : placesToRemove) {
      petri.removePlace(p);
    }
    for (PetriNetTransition t : transitionsToRemove) {
      petri.removeTransititon(t);
    }
    System.out.println("PRUNED:");
    System.out.println(petri);
    System.out.println("=====");
    System.out.println(placesToRemove);
    System.out.println(transitionsToRemove);
    return petri;
  }

  private static Set<PetriNetTransition> satisfiedTransitions(Set<PetriNetPlace> currentMarking) {
    return post(currentMarking).stream()
        .filter(transition -> currentMarking.containsAll(transition.pre()))
        .distinct()
        .collect(Collectors.toSet());
  }


  private static Set<PetriNetTransition> post(Set<PetriNetPlace> currentMarking) {
    return currentMarking.stream()
        .map(PetriNetPlace::post)
        .flatMap(Set::stream)
        .distinct()
        .collect(Collectors.toSet());
  }
}
