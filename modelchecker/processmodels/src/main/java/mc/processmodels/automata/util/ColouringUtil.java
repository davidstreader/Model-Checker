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
   * @param colourMap  defines what a colour is and can be set by coloring one automata
   *                   then used when coloring a second automata
   * @return           again not sure if used
   *
   * The nodes of the automaton hold colour and these are set
   *   -- colourMap from Nodecolor to list of colorComponents
   *   -- nodeColours  maps colours to nodes
   *         COMPUTEs BISIMULATION COLORing
   */
  public Multimap<Integer, AutomatonNode> performColouring
                          (Automaton automaton,
                           Map<Integer, List<ColourComponent>> colourMap,
                           Map<AutomatonNode,Integer> initialColour) {
    int lastColourCount = 1;
    performInitialColouring(automaton, initialColour);
    Multimap<Integer, AutomatonNode> nodeColours = ArrayListMultimap.create();
    boolean runTwice = true;

  /*  System.out.println("Starting performColouring "+ automaton.toString());
System.out.print("ColorMap [\n");
for (Integer n: colourMap.keySet()) {
  System.out.print(n+" -> "+ colourMap.get(n).toString()+"\n ");
} System.out.println(" ]");
*/
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
//System.out.println(">>>col "+ colouring.toString());
        // check if this colouring already exists
        int colourId = Integer.MIN_VALUE;

        for (int id : colourMap.keySet()) {
          List<ColourComponent> oldColouring = colourMap.get(id);
 // System.out.print("oldcol "+ oldColouring.toString());
  //Beware .eualaity failed - might be to do with Collection List clash
  // So I hard coded colorEquality
          if (colorEquality(colouring,oldColouring)) {
            colourId = id;
            break;
          }
          //System.out.println("*No");
        }

        if (colourId == Integer.MIN_VALUE) {
          colourId = getNextColourId();
          colourMap.put(colourId, colouring);
//  System.out.println("new "+colourId+" -> "+ colouring.toString());
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
 //   System.out.println("Ending performColouring "+ automaton.toString());

    return nodeColours;
  }

  private void performInitialColouring(Automaton automaton,
                                       Map<AutomatonNode,Integer> initialColour) {
 //   System.out.println("performInitialColouring");
    if (initialColour ==null) {System.out.println("NULL");}
/*
    for (AutomatonNode n : initialColour.keySet()){
      System.out.println(" "+n.getId()+" "+initialColour.get(n).toString());
    }
*/
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
     Because the initial colouring need not be the total relation
     We need to check that the two nodes are initiall colour equal
   */
  public List<ColourComponent> constructColouring(AutomatonNode node) {
    Set<ColourComponent> colouringSet = new HashSet<>();
    colouringSet.add(new ColourComponent(node.getColour(), "****"));
    node.getOutgoingEdges()
        .forEach(edge -> {
          boolean add = true;
          ColourComponent newColC = new ColourComponent(edge.getTo().getColour(), edge.getLabel());
          for(ColourComponent cc :colouringSet) {
            if (cc.equals(newColC)){add = false;}
          }
          if (add) {
            colouringSet.add(newColC);
          }
        //System.out.println("To node "+ edge.getTo().getId()+
         //                  " col "+edge.getTo().getColour() );
        });
    List<ColourComponent> colouring = new ArrayList<>(colouringSet);
    Collections.sort(colouring);
    return colouring;
  }

  public int getNextColourId() {
    return nextColourId++;
  }

  public boolean colorEquality(List<ColourComponent> c1,List<ColourComponent> c2){

    if (c1.size() != c2.size()) {return false;}
    for(int ix = 0; ix<c1.size(); ix++) {
      if (c1.get(ix).to != c2.get(ix).to) {return false;}
      if (!c1.get(ix).action.equals(c2.get(ix).action)) {return false;}
    }
    return true;
  }

  @ToString
  //@AllArgsConstructor
  public static class ColourComponent implements Comparable<ColourComponent> {
    public int to;
    public String action;
    public ColourComponent(int toin, String actionin){
      to = toin;
      action = actionin;
    }
    public int compareTo(ColourComponent col) {
      if (to < col.to) {
        return -1;
      }
      if (to > col.to) {
        return 1;
      }
      return action.compareTo(col.action);
    }
    public boolean equ(ColourComponent col){
      return action.equals(col.action) && to==col.to;
    }
    public String myString(){
      return action+" "+to;
    }

    public boolean equals(ColourComponent col){
      boolean ok = action.equals(col.action) && to==col.to;
      //System.out.println("colcomp eq "+ ok);
      return ok;
    }
  }
}
