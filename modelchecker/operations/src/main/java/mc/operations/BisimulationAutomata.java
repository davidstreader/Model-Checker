

    package mc.operations;

import static mc.processmodels.automata.util.ColouringUtil.ColourComponent;

import java.util.*;
import java.util.stream.Collectors;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.util.ColouringUtil;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.utils.PetriColouring;

    public class BisimulationAutomata implements IOperationInfixFunction {


        /**
         * A method of tracking the function.
         *
         * @return The Human-Readable form of the function name
         */
        @Override
        public String getFunctionName() {
            return "AutomatonBisimulation";
        }

        /**
         * The form which the function will appear when composed in the text.
         *
         * @return the textual notation of the infix function
         */
        @Override
        public String getNotation() {
            return "~";
        }
        @Override
        public String getOperationType(){
            return "automata";
        }
        public String hell(){return "automata";}

        /**                                      automaton
         * Evaluate the function.  we can pass the function auto OR petri
         *
         * @param processModels the list of automata / PetriNets being compared
         * @return the resulting automaton of the operation
         */
        @Override
        public boolean evaluate(Collection<ProcessModel> processModels) throws CompilationException {
            System.out.println("Bisimulation on Automaton "+processIds(processModels));
            if (processModels.iterator().next() instanceof Automaton) {

                ArrayList<AutomatonEdge> edges = new ArrayList<>();
                ArrayList<AutomatonNode> nodes = new ArrayList<>();

                final int BASE_COLOUR = 1;
                //System.out.println("Bisim evaluate");
                int i = 0; String firstId = "";
                for (ProcessModel pm : processModels) {
                    if (i==0) firstId = pm.getId();
                    else if (firstId.equals(pm.getId())) {
                        //System.out.println("automata bisim same ids "+firstId);
                        return true;
                    }
                    Automaton a = (Automaton) pm;
                    //System.out.println(i++ +" "+ a.toString());
                    edges.addAll(a.getEdges());
                    nodes.addAll(a.getNodes());
                }
                Set<Integer> root_colors = new TreeSet<Integer>();
                Set<Integer> first_colors = new TreeSet<Integer>();
                Map<Integer, List<ColourComponent>> colourMap = new HashMap<>();
                //int rootColour = Integer.MIN_VALUE;

                // set up the initial colouring ( on the nodes)
                ColouringUtil colourer = new ColouringUtil();
                colourer.performInitialColouring(nodes);
                colourer.doColouring(edges, nodes); // uses initial colouring on nodes


                i = 0;
                for (ProcessModel pm : processModels) {
                    Automaton automaton = (Automaton) pm;
                    //System.out.println("bisim ~ "+ automaton.myString());
                    Set<AutomatonNode> root = automaton.getRoot();

                    if (i == 0) {
                        for (AutomatonNode n : root) {
                            first_colors.add(n.getColour());
                        }
                        //System.out.println("Aut "+ automaton.getId()+ " first col "+ first_colors);
                        i++;
                    } else {
                        for (AutomatonNode n : root) {
                            root_colors.add(n.getColour());
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
            System.out.println("\nBisimulation expecting automaton But found " + processModels.iterator().next().getClass()+"\n");
            System.out.println(processModels.iterator().next().getId());
            Throwable t = new Throwable();
            t.printStackTrace();
            return false;
        }

        private String processIds(Collection<ProcessModel> pms) {
            return pms.stream().map(x->x.getId()).collect(Collectors.joining(" "));
        }
    }


