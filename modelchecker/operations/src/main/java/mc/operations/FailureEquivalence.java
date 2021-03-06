package mc.operations;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Context;
import mc.AcceptanceGraph;
import mc.Constant;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.util.ColouringUtil;

import java.util.*;

import static mc.processmodels.automata.util.ColouringUtil.ColourComponent;

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
    @Override
    public Collection<String> getValidFlags(){
        return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR, Constant.CONGURENT);
    }

    /**
     * The form which the function will appear when composed in the text.
     *  Currently must be lowercase
     * @return the textual notation of the infix function
     */
    @Override
    public String getNotation() {
        return "=f";
    } //must be lowercase
    @Override
    public String getOperationType(){return "automata";}
    /**
     * Evaluate the function.
     *
     * @param alpha
     * @param processModels the list of automata being compared
     * @return the resulting automaton of the operation
     * <p>
     * Failure equality is II-bisimulation of acceptance graphs
     * 1. build the acceptance graphs for each automata
     * a dfa + node to set of acceptance sets map
     * 2. Color the nodes of the dfa acording to acceptance set equality
     * initialise bisimulation coloring with the newly built coloring
     */
    @Override
    public boolean evaluate(Set<String> alpha, Set<String> flags, Context context,
                            Stack<String> trace,
                            Collection<ProcessModel> processModels) throws CompilationException {
        boolean cong = flags.contains(Constant.CONGURENT);
        return evaluate(processModels,true,cong);
    }


/*
   Called from Failurerefinemnet with equ=false and above with equ=true
   param equ controls if equality or refinement is being computed
   ONLY used in colouring of Acceptance graph
 */
        public boolean evaluate(Collection<ProcessModel> processModels,boolean equ,boolean cong)
          throws CompilationException {
            int ii = 0; String firstId = "";
            for (ProcessModel pm : processModels) {
                //System.out.println("  Fail "+ii+"  "+pm.getId());
                if (ii==0) firstId = pm.getId();
                else if (firstId.equals(pm.getId())) {
                    //System.out.println("automata Fail same ids "+firstId);
                    return true;
                }
                ii++; //Need this check
            }
            //System.out.println("Failure Equ Start "+ equ+" "+processModels.stream(). map(x->x.getId()).reduce((x,y)->x+" "+y));
            if (processModels.iterator().next() instanceof Automaton) {
            //BuildAcceptanceGraphs bag = new BuildAcceptanceGraphs();
            ArrayList<AcceptanceGraph> ags = new ArrayList<AcceptanceGraph>();
            final int BASE_COLOUR = 1;
            Integer color = BASE_COLOUR; // is updated  by ag.colorNodes
            //maps node colour to the set of color compnents
            Map<Integer, List<ColourComponent>> colourMap = new HashMap<>();
            int rootColour = Integer.MIN_VALUE;

            Map<AutomatonNode, Integer> initialColour = new HashMap<AutomatonNode, Integer>();

  /*
  Set up Acceptance Graphs and initial colouring on the automata nodes
  */
            //ColouringUtil colourer = new ColouringUtil();
            // Holds definition of what a color means
            Map<Integer, List<Set<String>>> cmap = new TreeMap<Integer, List<Set<String>>>();

            for (ProcessModel pm : processModels) {
                //System.out.println("Start auto "+ pm.getId());
                Automaton a = (Automaton) pm;
                //System.out.println("Start copy "+ a.toString());
                // build nfa and then dfa for second parameter "a"
                AcceptanceGraph ag = new AcceptanceGraph("dfa-" + a.getId(), a,cong);
        //System.out.println("Start ag "+equ + ag.toString());
                //Color the acceptance graph - result in cmap
                color = ag.colorNodes(cmap, ag.getNode2AcceptanceSets(), color, equ); //reuse of color map essential
         //System.out.println("Just colored acceptance"+ ag.getA().myString());

                //  this.printColorMap(cmap);
                // construct the initial coloring for the bisimulation
                ags.add(ag);
                for (AutomatonNode nd : ag.getA().getNodes()) {
                    initialColour.put(nd, nd.getColour());
                }
                //System.out.println("X"+ag.toString());
            }
            // //System.out.println("Failure initial color end");


            //System.out.println("Initial color consistent");

/*
   If the acceptance coloring is not a bisimulation look not further and fail
   Now compute the bisimulation coloring of the DFA in the Acceptance graphs
 */
            // build node and edge lists
            ArrayList<AutomatonEdge> edges = new ArrayList<>();
            ArrayList<AutomatonNode> nodes = new ArrayList<>();
            for (AcceptanceGraph a : ags) {
                //System.out.println(i++ +" "+ a.getId());
                edges.addAll(a.getA().getEdges());
                nodes.addAll(a.getA().getNodes());
            }
     /*  F.out.print("Initial node Color {");
        for(AutomatonNode nd : nodes) {
            System.out.print(nd.getId()+"->"+nd.getColour()+" ");
        } System.out.println("}"); */
            // set up the initial colouring ( on the nodes)
            ColouringUtil colourer = new ColouringUtil();
            //Computes a bsimulation coloring on the accepance graph
            //this elevates any inequality to to root hence only the root
            //coloring needs be checked at the end
            colourer.doColouring(nodes,cong); // uses initial colouring on node
          /*  if (!consistentColor(ags)) {
                System.out.println("**WARNING** inconsistent");
                    return false;
            } */
        /*System.out.print("Final node Color {");
        for(AutomatonNode nd : nodes) {
            System.out.print(nd.getId()+"->"+nd.getColour()+" ");
        } System.out.println("}");*/
            Set<Integer> root_colors = new TreeSet<Integer>();
            Set<Integer> first_colors = new TreeSet<Integer>();
            int i = 0;
            for (AcceptanceGraph a : ags) {
                //System.out.println("Y"+a.toString());
                Automaton automaton = a.getA();
                Set<AutomatonNode> root = automaton.getRoot();

                if (i == 0) {
                    for (AutomatonNode n : root) {
                        first_colors.add(n.getColour());
                        //System.out.println("1 "+n.getId()+" "+n.getColour());
                    }
                    //System.out.println("Aut "+ automaton.getId()+ " first col "+ first_colors);
                    i++;
                } else {
                    for (AutomatonNode n : root) {
                        root_colors.add(n.getColour());
                        //System.out.println("2 "+n.getId()+" "+n.getColour());
                    }
                    //System.out.println("Aut "+ automaton.getId()+ " root col "+ root_colors);
                    if (root_colors.equals(first_colors)) {   //comparison between this current automaton and the first
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            return true;
        }
        System.out.printf("\nFailure semantics not defined for type " + processModels.iterator().next().getClass()+"\n");
    return false;

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

        outerloop:
        for(AutomatonNode n : nodes) {
            List<ColourComponent> cc = cu.constructColouring(n);
            //System.out.print("node "+n.myString()); print_cc(cc);
            for(Integer colKey: colDef.keySet()){
                if (colDef.get(colKey).equals(cc)) {
                    if (!colKey.equals(n.getColour())) {
                        ok = false;
                        //System.out.print("failed "+n.getId()+" "+ colKey+ " not ");print_cc(cc);
                        break outerloop;}
                }
            }
            colDef.put(n.getColour(), cc);
            //System.out.print("colDef "+n.getColour() +" -> "); print_cc(cc);
        }
        return ok;
    }


}
