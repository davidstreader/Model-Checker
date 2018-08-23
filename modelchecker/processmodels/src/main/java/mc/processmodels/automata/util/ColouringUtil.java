package mc.processmodels.automata.util;

import com.google.common.collect.Multimap;

import java.util.*;

import lombok.ToString;
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

  /**
   * computs a bisimulation coloring. Starting from an initial coloring
   * Bisimulation starts with all nodes the same color
   * Failure equivalence uses acceptance set equality
   * Failure refinement uses acceptance subset
   *  @param nodes
   *
   */
  public Map<Integer, ColourPi> doColouring
  (List<AutomatonNode> nodes) throws Error {

    Map<AutomatonNode, Integer> nextCol = new TreeMap<AutomatonNode, Integer>();
    Map<Integer, ColourPi> col2pi = new TreeMap<>();
    Map<ColourPi, Integer> cpi = new TreeMap<ColourPi, Integer>();
    // Repeatedly
    int test = 0;
    boolean go = true;
    while (go) {
      if (test > 10) {
        System.out.println("ERROR COLOURING automaton");
        Throwable t = new Throwable();
        t.printStackTrace();
        throw (new Error("Automata Too Big"));
      }
      test++;
      Map<String, Integer> pi = new TreeMap<String, Integer>();


      // build fresh pi each iteration
      System.out.println("test "+test);
      //    for each node nd
      for (AutomatonNode nd : nodes) {
        List<ColourComponent> ndp = new ArrayList<ColourComponent>(buildpi(nd).pi);
        ColourPi ndpi =  new ColourPi(ndp);
        //System.out.println("nd "+nd.getId()+ " "+ndpi.myString());
        //      if nd_colorPI defined in PI
// pi is sorted hence String representaion may be used
        //System.out.println(" PI = "+ piToString(pi));
       // String ndpiString = CCSString(ndp);
        //System.out.println("For node "+ nd.getId()+" Look for "+ ndpiString);
        if (cpi.containsKey(ndpi)) {
          //        if nd_col = PI(nd_colPI)
          nextCol.put(nd, cpi.get(ndpi));
          System.out.println("found");
          //            continue
          continue;

        } else {
            //         newCol in nd and in PI
            nextCol.put(nd, getNextColourId());
            System.out.println("NOT found Adding to next "+ nd.getId() +"->"+ nextCol.get(nd)
              + ndpi.myString());
            // pi.put(ndpiString, nextCol.get(nd));
            col2pi.put(nextCol.get(nd), ndpi);
            System.out.println(""+nextCol.get(nd)+" -> "+ col2pi.get(nextCol.get(nd)).myString());
            cpi.put(ndpi, nextCol.get(nd));
            continue;
          }
        }

      //   apply the new colours to the nodes
      //System.out.println("REcolor Nodes");
      for (AutomatonNode nd : nextCol.keySet()) {
        nd.setColour(nextCol.get(nd));
      }

      System.out.println("Colouring before termination check\n" + this.col2piToString(col2pi));
      //if one of the old colours has more than one new color pi then keep going
      go = false;
      Map<Integer, ColourPi> reversepi = new TreeMap<Integer, ColourPi>();
      for (AutomatonNode nd : nodes) {
        if (reversepi.containsKey(nd.getColour())) {
          if (!reversepi.get(nd.getColour()).equals(buildpi(nd))) {
            go = true;
            System.out.println("Keep Going "+reversepi.get(nd.getColour()).myString()+" != "+CCSString(buildpi(nd).pi));
            break;
          }
        } else {
          reversepi.put(nd.getColour(), buildpi(nd));
          System.out.println("Termination Check Add "+nd.getColour()+"->"+
               reversepi.get(nd.getColour()).myString());
        }
      }
      System.out.println("**");

    }

    System.out.println("Colouring\n" + this.col2piToString(col2pi));
    return col2pi;
  }

  private ColourPi buildpi(AutomatonNode nd) {
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

    for (AutomatonEdge ed : nd.getOutgoingEdges()) {
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
    //System.out.println("Sorted ndpi "+ CCSString(ccs));
    return new ColourPi(ccs);
  }


  public Map<AutomatonNode, Integer> performInitialColouring(List<AutomatonNode> nodes) {
    //System.out.println("performInitialColouring");

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
//System.out.println("initialCol "+node.getId()+"->"+node.getColour());
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
          //System.out.println("Adding From "+ node.getId()+ " To "+ edge.getTo().getId()+
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

        if (to < col.to) return -1;
        if (to > col.to) return +1;
        if (to == col.to) {
          return (action.compareTo(col.action));
        } else {
          return 0;
        } // unreachable
    }


    public String myString() {
      return action + " " + to;
    }


    public boolean equals(ColourComponent col) {
      boolean ok = action.equals(col.action) && to == col.to;
      //System.out.println("colcomp eq "+ ok);
      return ok;
    }
  }

  public static class ColourPi  {
    public List<ColourComponent> pi = new ArrayList<>();

  /*  public static int compareTo(ColourPi c1, ColourPi c2) {
      return c1.compareTo(c2);
    }

    public int compareTo(ColourPi col) {
      int i = 0;
      for(ColourComponent cc : pi) {
        if (cc.compareTo(col.pi.get(i))==1) {
          return 1;
        } else if (cc.compareTo(col.pi.get(i))==-1) {
          return -1;
        } else {
          i++;
          continue;
        }
      }
      return 0;
    } */

    public boolean equals(ColourPi piin){
      int i = 0;
      System.out.println(this.myString()+  "=?=" + piin.myString());
      if (pi.size()!= piin.pi.size()) {
        System.out.println(pi.size()+" != "+ piin.pi.size());
        return false;
      }

      for(ColourComponent cc: pi){
        System.out.printf("i "+i);
        if (! piin.pi.get(i).action.equals(cc.action)) {
          System.out.println(piin.pi.get(i).action + "!= "+ cc.action);
          return false;}
        if ( (piin.pi.get(i).to != cc.to)) {
          System.out.println(piin.pi.get(i).to +" != "+ cc.to);
          return false;}
        i++;
      }
      return true;
    }

    public boolean subset(ColourPi piin){
      int i = 0;
      System.out.println(this.myString()+  "<=?" + piin.myString());
      if (pi.containsAll(piin.pi)) return true;
      else return false;
    }

    public ColourPi(List<ColourComponent> p){
      pi =p;
    }
    public String myString(){
      StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (ColourComponent cc : this.pi) {
          sb.append(cc.myString()+", ");
        }
       sb.append(")");
      return sb.toString();
    }
  }

    public String CCSString(List<ColourComponent> ccs) {
    String s = "{ ";
    for (ColourComponent cc : ccs) {
      s = s + cc.from + " " + cc.action + " " + cc.to + " ";
    }
    return s + " }";
  }



  public String col2piToString(Map<Integer, ColourPi> colpi) {

    StringBuilder sb = new StringBuilder();

    for (Integer i : colpi.keySet()) {
      sb.append(i + "->");
      sb.append(colpi.get(i).myString());
      sb.append("\n");
    }
    return sb.toString();
  }
}
