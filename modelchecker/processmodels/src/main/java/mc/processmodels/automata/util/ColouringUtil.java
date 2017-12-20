package mc.processmodels.automata.util;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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

  public Multimap<Integer, AutomatonNode> performColouring(Automaton automaton, Multimap<Integer, Colour> colourMap) {
    int lastColourCount = 1;
    performInitialColouring(automaton);
    Multimap<Integer, AutomatonNode> nodeColours = MultimapBuilder.hashKeys().arrayListValues().build();
    boolean runTwice = true;

    while (runTwice || nodeColours.size() != lastColourCount && !Thread.currentThread().isInterrupted()) {
      if (nodeColours.size() == lastColourCount) {
        runTwice = false;
      }

      lastColourCount = nodeColours.size();
      nodeColours.clear();
      Set<String> visited = new HashSet<>();

      Queue<AutomatonNode> fringe = new LinkedList<>();
      fringe.offer(automaton.getRoot());

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
        List<Colour> colouring = constructColouring(current);

        // check if this colouring already exists
        int colourId = Integer.MIN_VALUE;

        for (int id : colourMap.keySet()) {
          Collection<Colour> oldColouring = colourMap.get(id);
          if (colouring.equals(oldColouring)) {
            colourId = id;
            break;
          }
        }

        if (colourId == Integer.MIN_VALUE) {
          colourId = getNextColourId();
          colourMap.replaceValues(colourId, colouring);
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

      // apply colours to the nodes
      for (int colourId : nodeColours.keySet()) {
        nodeColours.get(colourId).forEach(node -> node.setColour(colourId));
      }

    }

    return nodeColours;
  }

  private void performInitialColouring(Automaton automaton) {
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
        node.setColour(BASE_COLOUR);
      }
    }
  }

  private List<Colour> constructColouring(AutomatonNode node) {
    Set<Colour> colouringSet = new HashSet<>();

    node.getOutgoingEdges()
        .forEach(edge -> colouringSet.add(new Colour(edge.getTo().getColour(), edge.getLabel(), node)));
    List<Colour> colouring = new ArrayList<>(colouringSet);
    Collections.sort(colouring);
    return colouring;
  }

  private int getNextColourId() {
    return nextColourId++;
  }

  @ToString
  @AllArgsConstructor
  @EqualsAndHashCode(exclude = {"node"})
  public static class Colour implements Comparable<Colour> {
    public int to;
    public String action;
    public AutomatonNode node;

    public int compareTo(Colour col) {
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
