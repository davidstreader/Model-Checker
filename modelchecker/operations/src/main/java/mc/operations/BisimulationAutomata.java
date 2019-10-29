

    package mc.operations;

    import com.google.common.collect.ImmutableSet;
    import com.microsoft.z3.Context;
    import mc.Constant;
    import mc.exceptions.CompilationException;
    import mc.operations.functions.AbstractionFunction;
    import mc.plugins.IOperationInfixFunction;
    import mc.processmodels.ProcessModel;
    import mc.processmodels.automata.Automaton;
    import mc.processmodels.automata.AutomatonEdge;
    import mc.processmodels.automata.AutomatonNode;
    import mc.processmodels.automata.util.ColouringUtil;

    import java.util.*;
    import java.util.stream.Collectors;

    import static mc.processmodels.automata.util.ColouringUtil.ColourComponent;

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
        public Collection<String> getValidFlags(){
            return ImmutableSet.of( Constant.CONGURENT);
        }
        @Override
        public String getOperationType(){
            return "automata";
        }
        public String hell(){return "automata";}

        /**                                      automaton
         * Evaluate the function.  we can pass the function auto OR petri
         *
         * @param alpha
         * @param processModels the list of automata / PetriNets being compared
         * @return the resulting automaton of the operation
         */
        @Override
        public boolean evaluate(Set<String> alpha, Set<String> flags, Context context,
                                Stack<String> trace,
                                Collection<ProcessModel> processModels) throws CompilationException {

            //System.out.println("Bisimulation "+flags+ " on Automaton "+processIds(processModels) );
            boolean cong = flags.contains(Constant.CONGURENT);

            if (processModels.iterator().next() instanceof Automaton) {

                ArrayList<AutomatonEdge> edges = new ArrayList<>();
                ArrayList<AutomatonNode> nodes = new ArrayList<>();

                //System.out.println("Bisim evaluate");
                String firstId = "";
                Automaton[] auts = new Automaton[2];
                int j = 0;
                for (ProcessModel pm : processModels) {
                    if (flags.contains(Constant.FAIR)||flags.contains(Constant.UNFAIR)) {
                        AbstractionFunction absFun = new AbstractionFunction();
                        auts[j] = (Automaton)pm;
                        Automaton atemp = auts[j];
                        auts[j] = absFun.compose(pm.getId(),flags,context,atemp);
                    } else {
                        auts[j] = (Automaton)pm;
                    }
                    //System.out.println("  Bisim "+j+"  "+auts[j].myString());
                    if (j==0) firstId = auts[j].getId();
                    else if (firstId.equals(pm.getId())) {
                        //System.out.println("automata bisim same ids "+firstId);
                        return true;
                    }
                    edges.addAll(auts[j].getEdges());
                    nodes.addAll(auts[j].getNodes());
                    j++; //Need this check
                }
                Set<Integer> root_colors = new TreeSet<Integer>();
                Set<Integer> first_colors = new TreeSet<Integer>();
                Map<Integer, List<ColourComponent>> colourMap = new HashMap<>();
                //int rootColour = Integer.MIN_VALUE;

                // set up the initial colouring ( on the nodes)
                ColouringUtil colourer = new ColouringUtil();
                colourer.performInitialColouring(nodes,cong);

                colourer.doColouring(nodes,cong); // uses initial colouring on nodes


                int i = 0;
                for (Automaton automaton : auts) {
                    //for (ProcessModel pm : processModels) {
                        //Automaton automaton = (Automaton) pm;
                    //System.out.println("bisim ~ "+ automaton.myString());
                    Set<AutomatonNode> root = automaton.getRoot();

                    if (i == 0) {
                        for (AutomatonNode n : root) {
                            first_colors.add(n.getColour());
                            //System.out.println(i+" "+n.getId()+" -> "+n.getColour());
                        }
                        i++;
                    } else {
                        for (AutomatonNode n : root) {
                            root_colors.add(n.getColour());
                            //System.out.println(i+" "+n.getId()+" -> "+n.getColour());
                        }
                        boolean result = false;
                        if (root_colors.equals(first_colors)) {   //comparison between this current automaton and the first
                            result = true;
                        }
                        return result;
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

        public String myString(){
            return getNotation();
        }
    }


