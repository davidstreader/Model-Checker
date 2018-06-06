package mc.processmodels.automata.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.*;

import com.google.common.collect.TreeMultimap;
import lombok.ToString;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;

public class ColouringUtil {


 private static final int BASE_COLOUR = 1;
 private static final int STOP_COLOUR = 0;
 private static final int ERROR_COLOUR = -1;
 private static final int ROOT_COLOUR = -2;
 private static final int ROOT_STOP_COLOUR = -3;
 private static final int ROOT_ERROR_COLOUR = -4;
 private int nextColourId = 1;
 //private Map<AutomatonNode,Integer> oldColours = new TreeMap<AutomatonNode,Integer>();

 public void doColouring
   (List<AutomatonEdge> edges,
    List<AutomatonNode> nodes) {

  Map<AutomatonNode, Integer> nextCol = new TreeMap<AutomatonNode, Integer>();


    /* Set up initial color on nodes
       Repeatedly color the nodes (in parallel) based on last color
                         DO NOT change color one node ta a time
        for each node nd compute nd_colPI
          if nd_colPI is defined in PI
            if nd_col = PI(nd_colPI)
                continue
            else
                ERROR!
          else
             newCol in nd and in PI

       stop only if each old color maps to only one colourPI
     */

  // Repeatedly
  int test = 0;
  boolean go = true;
  while (go && test < 10) {
   test++;
   Map<String, Integer> pi = new TreeMap<String, Integer>();
   // build fresh pi each iteration

   //    for each node nd
   for (AutomatonNode nd : nodes) {
 /*   System.out.print("nextCol= {");
    for(AutomatonNode ndd : nextCol.keySet()) {
      System.out.print(ndd.getId()+"->"+nextCol.get(ndd)+" ");
    } System.out.println("}");*/
    List<ColourComponent> ndpi = new ArrayList<ColourComponent>(buildpi(nd, edges, nodes));
    //      if nd_colorPI defined in PI

    // System.out.println(" PI = "+ piToString(pi));
    String ndpiString = CCSString(ndpi);
    // System.out.println("For node "+ nd.getId()+" Look for "+ ndpiString);
    if (pi.containsKey(ndpiString)) {
     //        if nd_col = PI(nd_colPI)
     nextCol.put(nd, pi.get(ndpiString));
     if (pi.get(ndpiString).equals(nextCol.get(nd))) {
      //            continue
      continue;
      //        else
     } else {
      //            ERROR!
     }
    } else {
     //         newCol in nd and in PI

     nextCol.put(nd, getNextColourId());
     //         System.out.println("NOT found Adding to next "+ nd.getId() +"->"+ nextCol.get(nd));
     //         System.out.println("                     pi "+ ndpiString);
     pi.put(ndpiString, nextCol.get(nd));
     continue;
    }
   }
   //   apply the new colours to the nodes
   //   System.out.println("REcolor Nodes");
   for (AutomatonNode nd : nextCol.keySet()) {
    nd.setColour(nextCol.get(nd));
   }
  /*   for(AutomatonNode nd : nextCol.keySet()){
      System.out.println("set node "+ nd.getId()+" col "+nd.getColour()+
        "   newPi "+CCSString(buildpi(nd, edges, nodes)));
     }*/

   //if one of the old colours has more than one new color pi then keep going
   go = false;
   Map<Integer, String> reversepi = new TreeMap<Integer, String>();
   for (AutomatonNode nd : nodes) {
    if (reversepi.containsKey(nd.getColour())) {
     if (!reversepi.get(nd.getColour()).equals(CCSString(buildpi(nd, edges, nodes)))) {
      go = true;
      //   System.out.println("Keep Going"+reversepi.get(nd.getColour())+" != "+CCSString(buildpi(nd, edges, nodes)));
      break;
     } else {
      //             System.out.println(nd.getId()+ " "+nd.getColour()+" "+CCSString(buildpi(nd, edges, nodes)));
     }
    } else {
     reversepi.put(nd.getColour(), CCSString(buildpi(nd, edges, nodes)));
     //   System.out.println("Termination Check Add "+nd.getColour()+"->"+
     //     CCSString(buildpi(nd, edges, nodes) ));
    }
   }

 /*    System.out.print("ReversePI {");
     for(Integer k: reversepi.keySet()){
      System.out.print(k+"->"+reversepi.get(k)+" ");
     } System.out.println("}"); */
  }

  return;
 }

 private List<ColourComponent> buildpi(AutomatonNode nd,
                                       List<AutomatonEdge> edges,
                                       List<AutomatonNode> nodes) {
  ArrayList<ColourComponent> ccs = new ArrayList<ColourComponent>();
  if (nd.isStartNode()) {
   ccs.add(new ColourComponent(nd.getColour(), "Start", 999));
  }
  if (nd.isTerminal()) {
   if (nd.getTerminal().equals("STOP")) {
    ccs.add(new ColourComponent(nd.getColour(), "STOP", 999));
   } else {
    ccs.add(new ColourComponent(nd.getColour(), "ERROR", 999));
   }
  }

  for (AutomatonEdge ed : edges) {
   if (ed.getFrom().equals(nd)) {
    ColourComponent cc = new ColourComponent(ed.getFrom().getColour(), ed.getLabel(), ed.getTo().getColour());
    boolean add = true;
    for (ColourComponent c : ccs) {
     if (c.action.equals(cc.action) && c.to == cc.to) {
      add = false;
      break;
     }
    }
    if (add) ccs.add(cc);
   }
  }

  Collections.sort(ccs);
  // System.out.println("Sorted ndpi "+ CCSString(ccs));
  return ccs;
 }


 public Map<AutomatonNode, Integer> performInitialColouring(List<AutomatonNode> nodes) {
  //   System.out.println("performInitialColouring");

  Map<AutomatonNode, Integer> initialColour = new HashMap<AutomatonNode, Integer>();
  for (AutomatonNode node : nodes) {
   // check if the current node is a terminal and or Start
   node.setColour(BASE_COLOUR);
   if (node.isTerminal()) {
    String terminal = node.getTerminal();
    if (terminal.equals("STOP")) {
     if (node.isStartNode()) {
      node.setColour(ROOT_STOP_COLOUR);
     } else {
      node.setColour(STOP_COLOUR);
     }
    }  //ERROR documentory only
   } else if (node.isStartNode()) {
    node.setColour(ROOT_COLOUR);
   }
  }
  for (AutomatonNode node : nodes) {
   initialColour.put(node, node.getColour());
//   System.out.println("initialCol "+node.getId()+"->"+node.getColour());
  }
  return initialColour;
 }


 /*
    uses the color held on the automaton nodes
    Because the initial colouring need not be the total relation
    We need to check that the two nodes are initial colour equal
    ONLY USED IN Failure equ
  */
 public List<ColourComponent> constructColouring(AutomatonNode node) {
  Set<ColourComponent> colouringSet = new HashSet<>();
  //colouringSet.add(new ColourComponent(node.getColour(), "****"));
  node.getOutgoingEdges()
    .forEach(edge -> {
     boolean add = true;
     ColourComponent newColC = new ColourComponent(edge.getFrom().getColour(), edge.getLabel(), edge.getTo().getColour());
     for (ColourComponent cc : colouringSet) {
      if (cc.equals(newColC)) {
       add = false;
      }
     }
     if (add) {
      colouringSet.add(newColC);
      //  System.out.println("Adding From "+ node.getId()+ " To "+ edge.getTo().getId()+
      //                     " col "+edge.getTo().getColour() );
     }
    });
  List<ColourComponent> colouring = new ArrayList<>(colouringSet);
  Collections.sort(colouring);
  return colouring;
 }

 private int getNextColourId() {
  return nextColourId++;
 }

 public boolean colorComponentEquality(List<ColourComponent> c1, List<ColourComponent> c2) {

  if (c1.size() != c2.size()) {
   return false;
  }
  for (int ix = 0; ix < c1.size(); ix++) {
   if (c1.get(ix).to != c2.get(ix).to) {
    return false;
   }
   if (!c1.get(ix).action.equals(c2.get(ix).action)) {
    return false;
   }
  }
  return true;
 }

 private Map<AutomatonNode, Integer> setOldColours(Multimap<Integer, AutomatonNode> nodeColours) {
  Map<AutomatonNode, Integer> oCols = new TreeMap<AutomatonNode, Integer>();
  for (Integer k : nodeColours.asMap().keySet()) {
   for (AutomatonNode n : nodeColours.get(k)) {
    oCols.put(n, k);
   }
  }
  return oCols;
 }

 private boolean colEquality(Map<AutomatonNode, Integer> old, Map<AutomatonNode, Integer> now) {
  /*  System.out.print("colEquality old { ");
    for(AutomatonNode n: old.keySet()) {
      System.out.print(n.getId()+"-"+old.get(n)+ " ");
    } System.out.println(" }");
    System.out.print("colEquality now { ");
    for(AutomatonNode n: now.keySet()) {
      System.out.print(n.getId()+"-"+now.get(n)+ " ");
    } System.out.println(" }");*/
  boolean b = true;
  for (AutomatonNode n : now.keySet()) {
   if (!now.containsKey(n) || old.get(n) != now.get(n)) {
    return false;
   }
  }
  return b;
 }


 //ColourComponent for an event is the triple <FromColour, label, ToColour>
 @ToString
 //@AllArgsConstructor
 public static class ColourComponent implements Comparable<ColourComponent> {
  public int from;
  public int to;
  public String action;

  public ColourComponent(int fromin, String actionin, int toin) {
   from = fromin;
   to = toin;
   action = actionin;
  }


  public static int compareTo(ColourComponent c1, ColourComponent c2) {
   return c1.compareTo(c2);
  }

  public int compareTo(ColourComponent col) {
   //System.out.print("PINGO");
   if (from < col.from) return -1;
   if (from > col.from) return +1;
   if (from == col.from) {
    if (to < col.to) return -1;
    if (to > col.to) return +1;
    if (to == col.to) {
     return (action.compareTo(col.action));
    } else {
     return 0;
    } // unreachable
   } else {
    return 0;
   }  // unreachable

  }

  public boolean equ(ColourComponent col) {
   return action.equals(col.action) && to == col.to && from == col.from;
  }

  public String myString() {
   return action + " " + to;
  }


  public boolean equals(ColourComponent col) {
   boolean ok = action.equals(col.action) && to == col.to && from == col.from;
   //System.out.println("colcomp eq "+ ok);
   return ok;
  }
 }

 public String CCSString(List<ColourComponent> ccs) {
  String s = "{ ";
  for (ColourComponent cc : ccs) {
   s = s + cc.from + " " + cc.action + " " + cc.to + " ";
  }
  return s + " }";
 }

 public String piToString(Map<String, Integer> pi) {
  String s = "{ ";
  for (String k : pi.keySet()) {
   s = s + k + "-> " + pi.get(k);
  }
  return s + " }";
 }
}
