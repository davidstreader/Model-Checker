package mc.processmodels.automata.operations;

import mc.exceptions.CompilationException;
import mc.plugins.IProcessInfixFunction;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This covers the "internal choice" function.
 * This is a way that introduces non-determinism into the processes.
 * <p>
 * If this is introduced without a prefix, this will lead to multiple start nodes.
 * <p>
 * e.g. {@code A = B+C.}
 * <p>
 * <pre>
 * B   C
 * </pre>
 * However, if there are transitions that lead into the internal choice, it duplicates these
 * connections, creating a nondeterministic choice between the two processes provided
 * <p>
 * e.g. {@code A = b->C+D.}
 * <p>
 * <pre>
 *           ROOT
 *          /    \
 *         b      b
 *        /       \
 *       C         D
 * </pre>
 *
 * @author Jacob Beal
 * @see Automaton
 *
 */
public class InternalChoiceInfixFun implements IProcessInfixFunction {

  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "internalChoice";
  }

  /**
   * The form which the function will appear when composed in the text.
   *
   * @return the textual notation of the infix function
   */
  @Override
  public String getNotation() {
    return "+";
  }

  /**
   * Execute the function
   *
   * @param id         the id of the resulting automaton
   * @param automaton1 the first  automaton in the function (e.g. {@code A} in {@code A||B})
   * @param automaton2 the second automaton in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting automaton of the operation
   */
  @Override
  public Automaton compose(String id, Automaton automaton1, Automaton automaton2)
      throws CompilationException {
   // System.out.println("COMPOSE +");//Never get clled with STOP+P
    Automaton choice = new Automaton(id, !Automaton.CONSTRUCT_ROOT);

    choice.addAutomaton(automaton1);

    Map<AutomatonNode, AutomatonNode> automaton2NodeMap = new HashMap<>();

    automaton2.getNodes().forEach(n -> {
    //  System.out.println("Adding "+ n.toString());
      AutomatonNode newN = choice.addNode();
      automaton2NodeMap.put(n, newN);
      newN.copyProperties(n);
      if (n.isStartNode()) {
        newN.setStartNode(true);
     //   System.out.println("new is start" + newN.toString());
      }
    });

    for (AutomatonEdge e : automaton2.getEdges()) {
      choice.addEdge(e.getLabel(), automaton2NodeMap.get(e.getFrom()),
          automaton2NodeMap.get(e.getTo()), e.getGuard() == null ? null : e.getGuard().copy(), false);
    }


    return choice;
  }

  /**
   * TODO:
   * Execute the function.
   *
   * @param id        the id of the resulting petrinet
   * @param net1 the first  petrinet in the function (e.g. {@code A} in {@code A||B})
   * @param net2 the second petrinet in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting petrinet of the operation
   */
  @Override
  public Petrinet compose(String id, Petrinet net1, Petrinet net2) throws CompilationException {

    //System.out.println("+PETRI1 "+net1.myString());
    net1.validatePNet();
    //System.out.println("+PETRI2 "+net2.myString());
    net2.validatePNet();
    Petrinet petrinet1 = net1.copy();
    Petrinet petrinet2 = net2.copy();
    System.out.println("+PETRI1 "+petrinet1.myString());
    System.out.println("+PETRI2 "+petrinet2.myString());
    if (petrinet1 == petrinet2) {
      System.out.println("\n SAME NETS PROBLEM");
    }
    for(PetriNetPlace pl1: petrinet1.getPlaces().values()){
      for(PetriNetPlace pl2: petrinet2.getPlaces().values()){
        if (pl1==pl2) System.out.println("\n SAME PLACES PROBLEM");
      }
    }
    Petrinet choice = new Petrinet(id, false);
    choice.joinPetrinet(petrinet1);
    choice.setRoots(net1.getRoots());
    choice.joinPetrinet(petrinet2);
    //add new root set to choice net find next choice No
    int nextRootNo = petrinet1.nextRootNo();
    System.out.println("next Root "+ nextRootNo);
    List<Set<String>> rots = new ArrayList<>( petrinet2.getRoots());
    petrinet2.clearRoots();
    for(int i = 0; i< rots.size();i++) {
      nextRootNo++;
      Set<String> rt = rots.get(i);
      choice.addRoot(rt);
      for(String pl : rt) {
        petrinet2.getPlace(pl).addStartNo(nextRootNo);
        System.out.println(pl+" rooted");
      }
    }
if (net1.terminates() && net2.terminates()) {
  Set<String> end1 = petrinet1.getPlaces().values().stream().
    filter(x -> x.isSTOP()).map(x -> x.getId()).collect(Collectors.toSet());
  Set<String> end2 = petrinet2.getPlaces().values().stream().
    filter(x -> x.isSTOP()).map(x -> x.getId()).collect(Collectors.toSet());
  choice.glueNames(end1, end2);
}
    choice.setRootFromStart();
    System.out.println("choice "+choice.myString());
    return choice;
  }
}
