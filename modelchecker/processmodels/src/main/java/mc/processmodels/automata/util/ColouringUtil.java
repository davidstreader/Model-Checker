package mc.processmodels.automata.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;

public class ColouringUtil {


  private static final int BASE_COLOUR = 1;
  private static final int STOP_COLOUR = 0;
  private static final int ERROR_COLOUR = -1;
  private int nextColourId = 1;

  /**
   *
   * @param automaton  input and output
   * @param colourMap  not sure if used? but makes sence if second automata to be colourd
   * @return           again not sure if used
   *
   * The nodes of the automaton hold colour and these are set
   *   -- colourMap from Nodecolor to list of colorComponents
   *   -- nodeColours  maps colours to nodes
   */
  public Multimap<Integer, AutomatonNode> performColouring
                          (Automaton automaton,
                           Map<Integer, List<ColourComponent>> colourMap,
                           Map<AutomatonNode,Integer> initialColour) {
    int lastColourCount = 1;
    performInitialColouring(automaton, initialColour);
    Multimap<Integer, AutomatonNode> nodeColours = ArrayListMultimap.create();
    boolean runTwice = true;

    while (runTwice || nodeColours.size() != lastColourCount && !Thread.currentThread().isInterrupted()) {
      if (nodeColours.size() == lastColourCount) {
        runTwice = false;
      }

      lastColourCount = nodeColours.size();
      nodeColours.clear();
      Set<String> visited = new HashSet<>();

      Queue<AutomatonNode> fringe = new LinkedList<>();

      automaton.getRoot().forEach(fringe::offer);

      while (!fringe.isEmpty() && !Thread.currentThread().isInterrupted()) {
        AutomatonNode current = fringe.poll();

        // check if the current node has been visited
        if (visited.contains(current.getId())) {
          continue;
        }

        // check if the current node is a terminal
        if (current.isTerminal()) {
          String terminal = current.getTerminal();
          if (terminal.equals("STOP")) {
            nodeColours.put(STOP_COLOUR, current);
          } else if (terminal.equals("ERROR")) {
            nodeColours.put(ERROR_COLOUR, current);
          }

          visited.add(current.getId());
          continue;
        }

        // construct a colouring for the current node
        List<ColourComponent> colouring = constructColouring(current);

        // check if this colouring already exists
        int colourId = Integer.MIN_VALUE;

        for (int id : colourMap.keySet()) {
          Collection<ColourComponent> oldColouring = colourMap.get(id);
          if (colouring.equals(oldColouring)) {
            colourId = id;
            break;
          }
        }

        if (colourId == Integer.MIN_VALUE) {
          colourId = getNextColourId();
          colourMap.put(colourId, colouring);
        }

        if (!nodeColours.containsKey(colourId)) {
          nodeColours.replaceValues(colourId, new ArrayList<>());
        }
        nodeColours.get(colourId).add(current);

        current.getOutgoingEdges().stream()
            .map(AutomatonEdge::getTo)
            .filter(node -> !visited.contains(node.getId()))
            .forEach(fringe::offer);

        visited.add(current.getId());
      }

      // apply colours to the nodes  end of each iteration
      for (int colourId : nodeColours.keySet()) {
        nodeColours.get(colourId).forEach(node -> node.setColour(colourId));
      }

    }

    return nodeColours;
  }

  private void performInitialColouring(Automaton automaton,
                                       Map<AutomatonNode,Integer> initialColour) {
    System.out.println("performInitialColouring");
    if (initialColour ==null) {System.out.println("NULL");}
    for (AutomatonNode n : initialColour.keySet()){
      System.out.println(" "+n.getId()+" "+initialColour.get(n).toString());
    }
    List<AutomatonNode> nodes = automaton.getNodes();
    for (AutomatonNode node : nodes) {
      if (node.isTerminal()) {
        String terminal = node.getTerminal();
        if (terminal.equals("STOP")) {
          node.setColour(STOP_COLOUR);
        } else if (terminal.equals("ERROR")) {
          node.setColour(ERROR_COLOUR);
        }
      } else {
//        node.setColour(BASE_COLOUR);
        node.setColour(initialColour.get(node));
      }
    }
  }

  /*
     uses the color held on the automaton nodes
   */
  private List<ColourComponent> constructColouring(AutomatonNode node) {
    Set<ColourComponent> colouringSet = new HashSet<>();

    node.getOutgoingEdges()
        .forEach(edge -> colouringSet.add(new ColourComponent(edge.getTo().getColour(), edge.getLabel(), node)));
    List<ColourComponent> colouring = new ArrayList<>(colouringSet);
    Collections.sort(colouring);
    return colouring;
  }

  public int getNextColourId() {
    return nextColourId++;
  }

  @ToString
  @AllArgsConstructor
  @EqualsAndHashCode(exclude = {"node"})
  public static class ColourComponent implements Comparable<ColourComponent> {
    public int to;
    public String action;
    public AutomatonNode node;

    public int compareTo(ColourComponent col) {
      if (to < col.to) {
        return -1;
      }
      if (to > col.to) {
        return 1;
      }
      return action.compareTo(col.action);
    }

  }
}
