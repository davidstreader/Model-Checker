package mc.operations.impl;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import mc.Constant;
import mc.compiler.Guard;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;

public class AutomataParallelFunction {

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
    setupActions(automaton1.getAlphabet(), automaton2.getAlphabet());

    List<AutomatonEdge> edges1 = automaton1.getEdges();
    List<AutomatonEdge> edges2 = automaton2.getEdges();

    //The edges here are meaning the labeled lines in each of the automata that we are composing
    processUnsyncedActions(edges1, edges2);

    processSyncedActions(edges1, edges2);

    //System.out.println("Par END "+automaton.myString());
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

        if(node1.isTerminal() && node1.getTerminal().equals("STOP") &&
          node2.isTerminal() && node2.getTerminal().equals("STOP"))
          node.setTerminal("STOP");


        if (node.isStartNode())
          automaton.addRoot(node);
// Errors are local not Global
        if ("ERROR".equals(node2.getTerminal()) && "ERROR".equals(node1.getTerminal()) )
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

  private void setupActions(Set<String> firstAutomataEdgeLabelsList,
                            Set<String> secondAutomataEdgeLabelsList) {
    for (String edgeLabel : firstAutomataEdgeLabelsList) {
      processAction(edgeLabel, secondAutomataEdgeLabelsList);
    }
    for (String edgeLabel : secondAutomataEdgeLabelsList) {
      processAction(edgeLabel, firstAutomataEdgeLabelsList);
    }
  }

  /**
   * Function produces two lists of unsynced and synced actions so we can later merge
   * diagram elements together.
   *
   * @param edgeLabel      The edge to test whether it is in the other automatas edgelist
   *                       (i.e what we can combine)
   * @param listEdgeLabels The edge list with which we are searching for things that match edgeLabel
   *                       so we can sync them.
   */
  private void processAction(String edgeLabel, Set<String> listEdgeLabels) {
    // if action is hidden or deadlocked it is always unsynced
    if (edgeLabel.equals(Constant.HIDDEN) || edgeLabel.equals(Constant.DEADLOCK)) {
      unsyncedActions.add(edgeLabel);

      // broadcasting actions are always unsynced
    } else if (edgeLabel.endsWith("!")) {
      if (containsReceiver(edgeLabel, listEdgeLabels)) {
        syncedActions.add(edgeLabel);
      }
      if (containsBroadcaster(edgeLabel, listEdgeLabels)) {
        syncedActions.add(edgeLabel);
      } else {
        unsyncedActions.add(edgeLabel);
      }
    } else if (edgeLabel.endsWith("?")) {
      if (!containsBroadcaster(edgeLabel, listEdgeLabels)) {
        if (containsReceiver(edgeLabel, listEdgeLabels)) {
          syncedActions.add(edgeLabel);
        }

        unsyncedActions.add(edgeLabel);
      }
    } else if (listEdgeLabels.contains(edgeLabel)) {
      syncedActions.add(edgeLabel);
    } else {
      unsyncedActions.add(edgeLabel);
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

        /* ERROR is local not Global
          if (from.get(i).isTerminal() && from.get(i).getTerminal().equals("ERROR"))
            continue; */

          // adds some !a and ?a edges that will be deleted in processSyncedActions
          AutomatonEdge newEdge = automaton.addEdge(edge.getLabel(), from.get(i), to.get(i),
              edge.getGuard(), false);

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


  private void processSyncedActions(List<AutomatonEdge> edges1, List<AutomatonEdge> edges2)
    throws CompilationException {
    //System.out.println("Synced 1"+ automaton1.getOwners());
    //System.out.println("Synced 2"+ automaton2.getOwners());

    for (String currentSyncEdgeLabel : syncedActions) {
      //System.out.println("Synced "+currentSyncEdgeLabel);
      List<AutomatonEdge> syncedEdges1 = edges1.stream()
        .filter(edge -> equals(currentSyncEdgeLabel, edge.getLabel()))
        .collect(Collectors.toList());

      List<AutomatonEdge> syncedEdges2 = edges2.stream()
        .filter(edge -> equals(currentSyncEdgeLabel, edge.getLabel()))
        .collect(Collectors.toList());
      for (AutomatonEdge edge1 : syncedEdges1) {
        for (AutomatonEdge edge2 : syncedEdges2) {
          AutomatonNode from = automaton.getNode(createId(edge1.getFrom(), edge2.getFrom()));
          //broadcast events that sync  (a! - a?)  or (a? - a?)
          if (edge1.getLabel().endsWith("!") || edge2.getLabel().endsWith("!")||
            (edge1.getLabel().endsWith("?") && edge2.getLabel().endsWith("?"))) {
            // any edges from the from node are broadcasted and should get replaced by the synced
            // transition. Remove any edges that have ! or ? at the end.
            from.getOutgoingEdges().stream()
              .filter(e -> ( e.getLabel().equals(edge1.getLabel()) ||
                e.getLabel().equals(edge2.getLabel()) ))
              .forEach(edge -> automaton.removeEdge(edge.getId()));
          }
          AutomatonNode to = automaton.getNode(createId(edge1.getTo(), edge2.getTo()));
          Guard guard = new Guard();
          if (edge1.getGuard() != null) {
            guard.mergeWith(edge1.getGuard());
          }
          if (edge2.getGuard() != null) {
            guard.mergeWith(edge2.getGuard());
          }

          AutomatonEdge newEdge = automaton.addEdge(currentSyncEdgeLabel, from, to,
            guard, false);
          Set<String> ownersToAdd = new HashSet<>();
          ownersToAdd.addAll(getOwners(edge1,automaton1));
          ownersToAdd.addAll(getOwners(edge2,automaton2));
          automaton.addOwnersToEdge(newEdge,ownersToAdd);
        }
      }
    }
    //System.out.println("SYNCED END");
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

  private boolean containsReceiver(String broadcaster, Set<String> receivers) {
    String broadcastAction = broadcaster.substring(0, broadcaster.length() - 1);
    for (String potentialReceiver : receivers) {
      if (potentialReceiver.endsWith("?")) {
        String action = potentialReceiver.substring(0, potentialReceiver.length() - 1);
        if (action.equals(broadcastAction)) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean containsBroadcaster(String broadcaster, Set<String> receivers) {
    String broadcastAction = broadcaster.substring(0, broadcaster.length() - 1);
    for (String receiver : receivers) {
      if (receiver.endsWith("!")) {
        String action = receiver.substring(0, receiver.length() - 1);
        if (action.equals(broadcastAction)) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean equals(String action1, String action2) {
    if (action1.equals(action2)) {
      return true;
    } else if (action1.endsWith("!")) {
      action1 = action1.substring(0, action1.length() - 1);
      action2 = action2.substring(0, action2.length() - 1);
      return action1.equals(action2);
    }

    return false;
  }

  private void setup(String id) {
    this.automaton = new Automaton(id, !Automaton.CONSTRUCT_ROOT);
    this.nodeMap = MultimapBuilder.hashKeys().arrayListValues().build();
    this.syncedActions = new HashSet<>();
    this.unsyncedActions = new HashSet<>();
  }
}
