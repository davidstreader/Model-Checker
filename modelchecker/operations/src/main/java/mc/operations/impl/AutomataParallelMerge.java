package mc.operations.impl;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import mc.Constant;
import mc.compiler.Guard;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;

import java.util.*;
import java.util.stream.Collectors;

public class AutomataParallelMerge {

  private Automaton automaton;
  private Multimap<String, AutomatonNode> nodeMap;
          // maps one input node to the set of pair nodes
  private Set<String> syncedActions;
  private Set<String> unsyncedActions;
  private Automaton automaton1;
  private Automaton automaton2;

  /**
   * This composes two processes together.
   *
   * @param id the id of the resulting automata
   * @param automaton1 one of the processes being composed
   * @param automaton2 one of the processes being composed
   * @return the two processes executed in parallel
   * @throws CompilationException if
   */
  public Automaton execute(String id, Automaton automaton1, Automaton automaton2)
                           throws CompilationException {

    setup(id);

    this.automaton1 = automaton1;
    this.automaton2 = automaton2;

    // construct the parallel composition of the states from both automata
    setupNodes(automaton1.getNodes(), automaton2.getNodes());

    // find the synchronous and non-synchronous actions in both alphabet sets
    // this means find the edge labels that are the same
    unsyncedActions.addAll(automaton1.getAlphabet());
    unsyncedActions.addAll(automaton2.getAlphabet());
    List<AutomatonEdge> edges1 = automaton1.getEdges();
    List<AutomatonEdge> edges2 = automaton2.getEdges();

    //The edges here are meaning the labeled lines in each of the automata that we are composing
    processUnsyncedActions(edges1, edges2);

    return automaton;
  }

  private void setupNodes(List<AutomatonNode> nodes1, List<AutomatonNode> nodes2)
                          throws CompilationException {
    //Setting up all the potentially valid nodes.
    //After this process we remove any inaccessable.

    for (AutomatonNode node1 : nodes1) {

      for (AutomatonNode node2 : nodes2) {
        String id = createId(node1, node2);
        AutomatonNode node = automaton.addNode(id);

        // create an intersection of both nodes
        node.copyProperties(node1.createIntersection(node2));
        node.setTerminal(null);

        if(node1.isTerminal() && node1.getTerminal().equals("STOP") && node2.isTerminal() && node2.getTerminal().equals("STOP"))
          node.setTerminal("STOP");


        if (node.isStartNode())
          automaton.addRoot(node);

        if ("ERROR".equals(node2.getTerminal()) || "ERROR".equals(node1.getTerminal()) )
          node.setTerminal("ERROR");


        HashMap<String, Object> variableMap = new HashMap<>();
        if (node1.getVariables() != null)
          variableMap.putAll(node1.getVariables());

        if (node2.getVariables() != null)
          variableMap.putAll(node2.getVariables());


        node.setVariables(variableMap);

        nodeMap.put(node1.getId(), node);
        nodeMap.put(node2.getId(), node);

      }
    }
  }

  private void processUnsyncedActions(List<AutomatonEdge> edges1, List<AutomatonEdge> edges2)
                                      throws CompilationException {
    List<AutomatonEdge> allEdges = new ArrayList<>(edges1);
    allEdges.addAll(edges2);

    for (String action : unsyncedActions) {
      List<AutomatonEdge> edges = allEdges.stream()
          .filter(edge -> action.equals(edge.getLabel())) // receivers never get executed
          .collect(Collectors.toList());
          //got the list of unsynced edges in edges
          //  nodeMap one input node to each  node-pair that contains it

      for (AutomatonEdge edge : edges) {
        List<AutomatonNode> from = new ArrayList<>(nodeMap.get(edge.getFrom().getId()));
        List<AutomatonNode> to = new ArrayList<>(nodeMap.get(edge.getTo().getId()));
        for (int i = 0; i < Math.min(from.size(), to.size()); i++) {
          //Dont set any links from terminal error nodes.
          if (from.get(i).isTerminal() && from.get(i).getTerminal().equals("ERROR"))
            continue;

          // adds some !a and ?a edges that will be deleted in processSyncedActions
          AutomatonEdge newEdge = automaton.addEdge(edge.getLabel(), from.get(i), to.get(i),
              edge.getGuard(), false,edge.getOptionalEdge());

          Collection<String> ownersToAdd;

          if (edges1.contains(edge)) {
            ownersToAdd = getOwners(edge,automaton1);
          } else {
            ownersToAdd = getOwners(edge,automaton2);
          }
          automaton.addOwnersToEdge(newEdge,ownersToAdd);

        }
      }
    }
  }

  private static Set<String> getOwners(AutomatonEdge edge, Automaton owner) {
    Set<String> ownersToAdd = new HashSet<>();
    if (edge.getOwnerLocation().contains(Automaton.DEFAULT_OWNER)) {
      ownersToAdd.add(owner.getId());

    } else {
      ownersToAdd.addAll(edge.getOwnerLocation());
    }
    return ownersToAdd;
  }

  private String createId(AutomatonNode node1, AutomatonNode node2) {
    return node1.getId() + "||" + node2.getId();
  }
  private void setup(String id) {
    this.automaton = new Automaton(id, !Automaton.CONSTRUCT_ROOT);
    this.nodeMap = MultimapBuilder.hashKeys().arrayListValues().build();
    this.syncedActions = new HashSet<>();
    this.unsyncedActions = new HashSet<>();
  }
}
