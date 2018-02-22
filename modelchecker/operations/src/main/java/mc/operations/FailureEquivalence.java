package mc.operations;

import static mc.processmodels.automata.util.ColouringUtil.ColourComponent;

import java.util.*;
import java.util.stream.Collectors;

import mc.AcceptanceGraph;
import mc.BuildAcceptanceGraphs;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.util.ColouringUtil;

/**
 * Failure equality is II-bisimulation of acceptance graphs
 */
public class FailureEquivalence implements IOperationInfixFunction {


    /**
     * A method of tracking the function.
     *
     * @return The Human-Readable form of the function name
     */
    @Override
    public String getFunctionName() {
        return "FailureEquivalence";
    }

    /**
     * The form which the function will appear when composed in the text.
     *
     * @return the textual notation of the infix function
     */
    @Override
    public String getNotation() {
        return "*";
    }

    /**
     * Evaluate the function.
     *
     * @param automata the list of automata being compared
     * @return the resulting automaton of the operation
     *
     * Failure equality is II-bisimulation of acceptance graphs
     *   1. build the acceptance graphs for each automata
     *          a dfa + node to set of acceptance sets map
     *   2. Color the nodes of the dfa acording to acceptance set equality
     *     initialise bisimulation coloring with the newly built coloring
     */
    @Override
    public boolean evaluate(Collection<Automaton> automata) throws CompilationException {

        BuildAcceptanceGraphs bag = new BuildAcceptanceGraphs();
        ArrayList<AcceptanceGraph> ags = new ArrayList<AcceptanceGraph>();
        final int BASE_COLOUR = 1;
        Integer color = BASE_COLOUR; // is updated  by ag.colorNodes
        //maps node colour to the set of color compnents
        Map<Integer, List<ColourComponent>> colourMap = new HashMap<>();
        int rootColour = Integer.MIN_VALUE;

        Map<AutomatonNode,Integer> initialColour = new HashMap<AutomatonNode,Integer>();
      //  System.out.println("Failure start");
  /*
  Set up Acceptance Graphs and initial colouring on the automata nodes
  */
        //ColouringUtil colourer = new ColouringUtil();
        Map<Integer, List<Set<String>>> cmap = new TreeMap<Integer, List<Set<String>>>();

        for (Automaton automaton : automata) {
            //System.out.println("Start auto "+ automaton.toString());
            Automaton a = automaton.copy();
            //System.out.println("Start copy "+ a.toString());
            // build nfa and then dfa for second parameter "a"
            AcceptanceGraph ag = new AcceptanceGraph ("dfa-"+a.getId(), a);
            //System.out.println("Start ag "+ ag.toString());
            color = ag.colorNodes(cmap, ag.getNode2AcceptanceSets(),color); //reuse of color map essential
           // System.out.println("Just colored "+ ag.getA().toString());

          //  this.printColorMap(cmap);
            // System.out.println("Adding "+ ag.getA().getId());
            ags.add(ag);
            for(AutomatonNode nd : ag.getA().getNodes()){
                initialColour.put(nd,nd.getColour());
            }
            //System.out.println(ag.toString());
        }
        // System.out.println("Failure initial color end");
        if (!consistentColor(ags)){return false;};
        //System.out.println("Initial color consistent");

/*
   If the acceptance coloring is not a bisimulation look not further and fail
   Now compute the bisimulation coloring of the DFA in the Acceptance graphs
 */
        // build node and edge lists
        ArrayList<AutomatonEdge> edges = new ArrayList<>();
        ArrayList<AutomatonNode> nodes = new ArrayList<>();
        for(AcceptanceGraph a: ags){
            //System.out.println(i++ +" "+ a.getId());
            edges.addAll(a.getA().getEdges());
            nodes.addAll(a.getA().getNodes());
        }
        System.out.print("Initial node Color {");
        for(AutomatonNode nd : nodes) {
            System.out.print(nd.getId()+"->"+nd.getColour()+" ");
        } System.out.println("}");
        // set up the initial colouring ( on the nodes)
        ColouringUtil colourer = new ColouringUtil();
        //colourer.performInitialColouring(nodes);
        colourer.doColouring(edges, nodes); // uses initial colouring on nodes

        System.out.print("Final node Color {");
        for(AutomatonNode nd : nodes) {
            System.out.print(nd.getId()+"->"+nd.getColour()+" ");
        } System.out.println("}");
        Set<Integer> root_colors = new TreeSet<Integer>();
        Set<Integer> first_colors = new TreeSet<Integer>();
        int i = 0;
        for(AcceptanceGraph a: ags){
            Automaton automaton = a.getA();
            Set<AutomatonNode> root = automaton.getRoot();

            if (i ==0){
                for(AutomatonNode n : root) {
                    first_colors.add(n.getColour());
                }
                // System.out.println("Aut "+ automaton.getId()+ " first col "+ first_colors);
                i++;
            } else {
                for (AutomatonNode n : root) {
                    root_colors.add(n.getColour());
                }
                // System.out.println("Aut "+ automaton.getId()+ " root col "+ root_colors);
                if (root_colors.equals(first_colors)) {   //comparison between this current automaton and the first
                    return true;
                } else {
                    return false;
                }
            }
        }

        return true;
    }
    private void printColorMap(Map<Integer, List<Set<String>>> cmap) {
        for(Integer ckey : cmap.keySet()) {
            System.out.println(cmap.get(ckey).toString()+ ckey.toString());
        }
        System.out.println("colorMap end");
    }

    /**
     *
     * @param ags  set of acceptance graphs - dfa coloured by acceptance sets
     * @return  are the dfa coloring consistent with a bisimjulation
     * @throws CompilationException
     */
    public boolean consistentColor(Collection<AcceptanceGraph> ags) throws CompilationException {
        ColouringUtil cu = new ColouringUtil();
        List<AutomatonNode> nodes = new ArrayList<>();
        boolean ok = true;
        Map<Integer,List<ColourComponent>> colDef = new TreeMap<Integer,List<ColourComponent>>();
        for (AcceptanceGraph ag : ags) {
            nodes.addAll(ag.getA().getNodes());
        }
  /*for (AutomatonNode n : nodes) {
   System.out.print(n.myString()+" => ");
   for(AutomatonEdge e : n.getOutgoingEdges()) {
    System.out.print(e.getTo().myString()+" ");
   }
   System.out.println(".");
  } */


        outerloop:
        for(AutomatonNode n : nodes) {
          //  System.out.println("node "+n.getId()+" col "+ n.getColour());
            List<ColourComponent> cc = cu.constructColouring(n);
           // print_cc(cc);
            for(Integer colKey: colDef.keySet()){
                if (colDef.get(colKey).equals(cc)) {
                    if (!colKey.equals(n.getColour())) {
                        ok = false;
                        //System.out.println("failed "+ colKey+ " not "+cc.toString());
                        break outerloop;}
                }
            }
            colDef.put(n.getColour(), cc);
            //System.out.print("colDef "+n.getColour() +" ");
            //print_cc(cc);
        }
        return ok;
    }

    private void print_cc(List<ColourComponent> ccs){
        System.out.print("ccs = {");
        for(ColourComponent cc : ccs) {
            System.out.print(cc.myString()+", ");
        }
        System.out.println(" }");
    }
}