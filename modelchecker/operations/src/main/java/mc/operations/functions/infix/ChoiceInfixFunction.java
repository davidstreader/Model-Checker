package mc.operations.functions.infix;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessInfixFunction;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.operations.AutomataReachability;
import mc.processmodels.automata.operations.ChoiceFun;
import mc.processmodels.petrinet.Petrinet;

public class ChoiceInfixFunction implements IProcessInfixFunction {

  /**
   * A method of tracking the function
   *
   * @return The Human-Readable form of the function name
   */
  public String getFunctionName() {
    return "external choice";
  }
  public Collection<String> getValidFlags(){return new HashSet<>();}
  /**
   * The form which the function will appear when composed in the text
   *
   * @return the textual notation of the infix function
   */
  public String getNotation() {
    return "[]";
  }

  /**
   * Execute the function
   *
   * @param id         the id of the resulting automaton
   * @param automaton1 the first  automaton in the function (e.g. {@code A} in {@code A||B})
   * @param automaton2 the second automaton in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting automaton of the operation
   */
  public Automaton compose(String id, Automaton automaton1, Automaton automaton2)
      throws CompilationException {

    if (automaton1.getRoot().size() > 1 && automaton2.getRoot().size() > 1) {
      return new InternalChoiceInfixFunction().compose(id,automaton1,automaton2);
    }

    Automaton composition = new Automaton(id, !Automaton.CONSTRUCT_ROOT);
    Multimap<String,String> setOfOwners = AutomatonEdge.createIntersection(automaton1, automaton2);

    //store a map to the nodes so id can be ignored
    Multimap<String, AutomatonNode> automata1nodes = ArrayListMultimap.create();
    Multimap<String, AutomatonNode> automata2nodes = ArrayListMultimap.create();

    //copy node1 nodes across
    AutomataReachability.removeUnreachableNodes(automaton1).getNodes().forEach(node -> {
      try {
        AutomatonNode newNode = composition.addNode();
        newNode.copyProperties(node);
        automata1nodes.put(node.getId(), newNode);

        if (newNode.isStartNode()) {
          composition.addRoot(newNode);
        }

      } catch (CompilationException e) {
        e.printStackTrace();
      }
    });

    copyAutomataEdges(composition, automaton1, automata1nodes,setOfOwners);

    //get the stop nodes such that they can be replaced
    Collection<AutomatonNode> startNodes = composition.getNodes().stream()
        .filter(AutomatonNode::isStartNode)
        .collect(Collectors.toList());


    AutomataReachability.removeUnreachableNodes(automaton2).getNodes().forEach(node -> {
      if (node.isStartNode()) {
        automata2nodes.replaceValues(node.getId(),startNodes);
        return;
      }
      AutomatonNode newNode = composition.addNode();
      newNode.copyProperties(node);

      automata2nodes.put(node.getId(), newNode);
    });

    copyAutomataEdges(composition, automaton2, automata2nodes,setOfOwners);

    return composition;
  }

  /**
   * Execute the function.
   *
   * @param id        the id of the resulting petrinet
   * @param net1 the first  petrinet in the function (e.g. {@code A} in {@code A||B})
   * @param net2 the second petrinet in the function (e.g. {@code B} in {@code A||B})
   * @param flags
   * @return the resulting petrinet of the operation
   */
//  @Override
  public Petrinet compose(String id, Petrinet net1, Petrinet net2, Set<String> flags)
      throws CompilationException {
    ChoiceFun sf = new ChoiceFun();
    Petrinet choice = sf.compose(id,net1,net2);
    return choice;
  }

  /**
   * Copies the edges from one automata to another.
   *
   * @param writeAutomaton the automata that will have the edges copied to it
   * @param readAutomaton  the automata that will have the edges copied from it
   * @param nodeMap        the mapping of the ids to AutomatonNodes
   */
  private void copyAutomataEdges(Automaton writeAutomaton, Automaton readAutomaton,
                                 Multimap<String, AutomatonNode> nodeMap,
                                 Multimap<String,String> edgeOwnersMap) {
    readAutomaton.getEdges().forEach(e -> {
      try {
        for (AutomatonNode fromNode : nodeMap.get(e.getFrom().getId())) {
          for (AutomatonNode toNode : nodeMap.get(e.getTo().getId())) {
            writeAutomaton.addOwnersToEdge(
                writeAutomaton.addEdge(e.getLabel(), fromNode, toNode, e.getGuard(),
                    false,e.getOptionalEdge()), getEdgeOwnersFromProduct(e.getEdgeOwners(),edgeOwnersMap));
          }
        }
      } catch (CompilationException e1) {
        e1.printStackTrace();
      }
    });
  }

  private Set<String> getEdgeOwnersFromProduct(Set<String> edgeOwners,
                                               Multimap<String,String> productSpace) {
    return edgeOwners.stream().map(productSpace::get)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

}

