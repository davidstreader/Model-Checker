package mc.processmodels.conversion;

import java.util.*;
import java.util.stream.Collectors;


import lombok.SneakyThrows;
import mc.Constant;
import mc.exceptions.CompilationException;

import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.operations.PetrinetParallelFunction;
import mc.processmodels.automata.operations.SequentialInfixFun;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
import mc.processmodels.petrinet.operations.PetrinetReachability;
import mc.util.expr.MyAssert;
import mc.processmodels.ProcessModel;

//import mc.operations.impl.PetrinetParallelFunction;
public class OwnersRule {

    private static Map<AutomatonNode, Map<String, PetriNetPlace>> aN2Marking = new HashMap<>();
    private static todoStack todo = new todoStack();
    private static Map<String, AutomatonEdge> processed = new HashMap<>(); // No new Peri transition needed

    private static Set<AutomatonNode> ndProcessed;

    private static void clean() {
        aN2Marking = new HashMap<>();
        todo = new todoStack();
        ndProcessed = new HashSet<>();
        processed = new HashMap<>();
    }

    /**
     * This converts an automata to a petrinet.
     * For each owner O1 project the automata to a SLICE automata(Net)
     * i.e. a net built from  edges with owners containing O1
     * <p>
     * Build the final net  as the parallel composition of the Petri Net slices!
     * Note slices must distinguish which edge each transition comes from
     * <p>
     * Internal choice introduces multiple start states NOT EASY to code directly
     * So
     * 1. to automata A add initial event S*=>A now only one start state.
     * 2. proceed as before
     * 3. remove S* to reveal the multiple start states.
     * <p>
     * For synchronised broadcast events petri edges (each edge has unique owner)
     * may be "optional"
     * the automata edges built from them must have a list of optional owners
     * <p>
     * automata events are marked as optional if they are to be ignored in
     * by the ownerRule as a more general transition exists.
     * <p>
     * Broadcast - non blocking send amendment.
     * Owners rule works for handshake  with same name synchronisation +
     * The Token rule builds edges that represents the execution of a Transition
     * from a particuular  marking.
     * Taking an optional edge partions the owners into those changed and those not
     * After the optional edge is taken then  any other edge that is either all taken or
     * all needed not be added. With all broadcast the only edges that can stradle take,
     * not taken are broadcast sync events.
     * <p>
     * For each transition the Token rule records the set of maximum markings,
     * no superset of the marking executes this transition.
     * Only the edges representing the executions of a transition from one of its
     * maximum markings are NOT optional.
     * <p>
     * The owners rule can not ignore the optional edges of an automata.
     * but must only consider them for the marked owners
     * PROBLEM   HS tagedd same name
     * BC detagged same name
     *
     * @param ain The automaton to be converted
     * @return a petrinet representation fo the automaton
     * Map<String, ProcessModel> processMap
     */
    public static Petrinet ownersRule(Automaton ain) {
        Map<String, ProcessModel> processMap = new HashMap<>();
        return ownersRule(ain, processMap);
    }

    @SneakyThrows({CompilationException.class})
    public static Petrinet ownersRule(Automaton ain, Map<String, ProcessModel> processMap) {
        //if (ain==null) //System.out.println("Owners null");
        //System.out.println("*********\n OwnersRule initial automata " + ain.myString() + "*START ");
        //Throwable t = new Throwable(); t.printStackTrace();
        clean();
        //MyAssert.myAssert(ain.validateAutomaton("Owners Rule input "+ain.getId()+" vlaid = "), "Owners Rule Failure");
        MyAssert.validate(ain, "Owners Rule input ");
        //assert ain.validateAutomaton("Owners Automaton"): "Owners Rule precondition";
        // 1. to automata A add initial event S*=>A now only one start state.
        Automaton star = Automaton.singleEventAutomata("single", "S*");
        SequentialInfixFun sif = new SequentialInfixFun();
        Automaton ai = sif.compose(ain.getId(), star, ain);
        //System.out.println("*********\n OwnersRule  S* "+ai.myString());
        // 2. proceed as before
        Automaton autom = ai.copy(); // (cloner) smaller Ids make debugging easier
        //filter out the optional edges - set in Token Rule
        // List<AutomatonEdge> edgeToGo = autom.getEdges().stream().filter(ed -> ed.getOptionalEdge()).collect(Collectors.toList());
        /*System.out.println("edge cnt = "+autom.getEdges().size());
        for (AutomatonEdge ed : edgeToGo) { autom.removeEdge(ed);  }
        */
        //System.out.println("Oeners 3 "+autom.myString());
        System.out.println("edge  \n" + autom.getEdges().stream().filter(x -> x.getLabel().endsWith(Constant.BROADCASTSoutput)).map(x -> x.myString() + ",\n").collect(Collectors.joining()));
        //filter ends
        autom.tagEvents();
        //System.out.println("\nOWNERS Stared Rule " + autom.myString());

        AutomatonNode root = null;

//       There is only one Root node (because of S*)
        root = autom.getRoot().iterator().next();

        Stack<Petrinet> subNets = new Stack<>();
/*
   Build,one for each owner,  projection mappings from nodes to a  SLICE
    */
        System.out.println("Owners START " + autom.getOwners() + "\n");
        for (String own : autom.getOwners()) {
            System.out.println(" >>>>>>>Owner " + own);
            if (own.equals("_default"))
                throw new CompilationException(ain.getClass(), "Owners Failure in Owners Rule " + ain.myString());
            Petrinet petri = new Petrinet(autom.getId(), false);
            TreeSet<String> os = new TreeSet<>();
            os.add(own);
            petri.setOwners(os);
            Stack<String> toDo = new Stack<>();
            Stack<String> processed = new Stack<>();

            toDo.add(root.getId());
            int endcnt = 1;  //for setting endNos on NetPlace  - Net will be set from Place
            //BUILD the nd2Pl Mapp
            Map<String, PetriNetPlace> nd2Pl = new HashMap<>();
            boolean first = true;
            int sliceCnt = 1;
            while (!toDo.isEmpty()) {
                AutomatonNode nd = autom.getNode(toDo.pop());
                System.out.println("     >>>>>>>>>OWNStart nd " + nd.myString());
                if (processed.contains(nd.getId())) continue;
                processed.add(nd.getId());
                if (!nd2Pl.containsKey(nd)) {
                    PetriNetPlace addedPlace = petri.addPlace();
                    System.out.println("Added "+addedPlace.myString());
                    TreeSet<String> owns = new TreeSet<>();
                    owns.add(own);
                    addedPlace.setOwners(owns);
                    if (nd.isStopNode()) {
                        addedPlace.addEndNo(endcnt++);
                    }

                    if (first) {
                        petri.addRoot(Collections.singleton(addedPlace.getId()));
                        addedPlace.setStart(true);
                        addedPlace.getStartNos().add(1);
                        first = false;
                        System.out.println("Addied Root "+addedPlace.getId());
                    }

                    //System.out.println("PreClump \n"+ain.myString());
// a clump of nodes all map to one Place in the Slice Petri net
// that is all nodes reachable from node nd without owner own doing anything
                    Set<AutomatonNode> clump = clump(ain, nd, own);
                    //System.out.println("PostClump \n"+ain.myString());
                    //System.out.println("Clump " + clump.stream().map(x -> x.getId() + ", ").collect(Collectors.joining()));
                    for (AutomatonNode n : clump) {
                        if (!nd2Pl.containsKey(n.getId())) nd2Pl.put(n.getId(), addedPlace);
                        if (n.isTerminal() && n.isSTOP()) {
                            addedPlace.setTerminal("STOP");
                            if (addedPlace.getEndNos().size() == 0) addedPlace.addEndNo(endcnt++);
                        }
                        //System.out.println("added "+added.myString());
                    }
                }

                Set<AutomatonNode> next = nd.getIncomingEdges()
                    .stream()
                    .filter(ed -> !(ed.getLabel().equals(Constant.DEADLOCK)))
                    .map(ed -> ed.getFrom()).collect(Collectors.toSet());
                next.addAll(nd.getOutgoingEdges().stream().map(ed -> ed.getTo()).
                    filter(x->!processed.contains(x.getId())).collect(Collectors.toSet()));
                toDo.addAll(next.stream().map(x->x.getId()).collect(Collectors.toSet()));
                next.clear();
                //System.out.println("Next " + next.stream().map(x -> x.getId() + ", ").collect(Collectors.joining()));
            }
            //Use the nd2Pl Mapp to build the projected automaton
            toDo.clear();
            processed.clear();
            toDo.push(root.getId());
            //System.out.println("Half way "+petri.myString());
            while (!toDo.isEmpty()) {
                AutomatonNode nd = autom.getNode(toDo.pop());
                System.out.println("Start 2 nd " + nd.getId());
                if (processed.contains(nd.getId())) {
                    System.out.println("Skipped");
                    continue;
                }
                processed.add(nd.getId());

                for (AutomatonEdge ed : nd.getOutgoingEdges()) {
                    System.out.println(" Start 2 " + ed.myString() + "      own " + own);
                    if (ed.getLabel().equals(Constant.DEADLOCK)) continue;
                    toDo.push(ed.getTo().getId());

                    //System.out.println("    Start 2 " + ed.myString() + " own " + own);

                    if ((!ed.getOptionalEdge()) && ed.getEdgeOwners().contains(own) ||
                        ((ed.getOptionalEdge()) && ed.getMarkedOwners().contains(own))) {
                        PetriNetTransition tran = petri.addTransition(ed.getLabel());
                        boolean opt = false;
                        if (ed.getOptionalOwners().contains(own)) {
                            opt = true;
                        }
                        //System.out.println("    " + ed.getId()+ "  "+opt);
                        //              to,  from
                        petri.addEdge(tran, nd2Pl.get(ed.getFrom().getId()), opt);
                        petri.addEdge(nd2Pl.get(ed.getTo().getId()), tran, opt);
                        TreeSet<String> O = new TreeSet<>();
                        O.add(own);
                        tran.setOwners(O);
                        if ((ed.getOptionalEdge()) && ed.getMarkedOwners().contains(own)) {
                            System.out.println("    *Adding " + tran.myString());
                        }
                    } else {
                        //System.out.println("par edge " + ed.myString());
                    }
                }
            }

            //System.out.println("\nSlice Net = " + petri.myString());
            //petri = PetrinetReachability.removeUnreachableStates(petri).copy();
            //System.out.println("\npushing "+petri.myString());
            petri.setEndFromPlace();

            subNets.push(petri);
            //System.out.println(" SLICE Net \n"+petri.myString("edge")+ "\n SLICE Net ");
            Petrinet debug = petri.copy();
            debug = PetrinetReachability.removeUnreachableStates(debug, false);
            debug.setId("debug-" + own + "-slice");

            processMap.put(debug.getId(), debug);
            System.out.println(debug.myString());

        }
        Petrinet build;
        //System.out.println("\n   OWNERS Rule Stacked "+subNets.size()+"    *********");
        if (subNets.size() > 0) {
            build = subNets.pop();
            while (!subNets.isEmpty()) {
                //System.out.println(subNets.size()+" Adding");
                //build = PetrinetParallelMergeFunction.compose(build, subNets.pop());  //Debuging
                build = PetrinetParallelFunction.compose(build, subNets.pop(), new HashSet<String>());
                //  build = subNets.pop();  //for debugging
                //System.out.println("Build " + build.myString());
            }

            build.deTagTransitions();  //use ":" tonot mess up with Galois  (undo Automaton  tagEvents() )
            System.out.println("  BEFORE reach  " + build.myString());
            build = PetrinetReachability.removeUnreachableStates(build, false);


            //System.out.println("reach *END "+build.myString());

            //3. remove S* to reveal the multiple start states.
            //System.out.println("  OWNERS Rule Strip* start" + build.myString()+"\n*start");
            stripStar(build);
            //System.out.println("  OWNERS Rule Strip* END  " + build.myString()+"\n*END");
            //assert(build.validatePNet(): "OwnersRule End");
        } else {
            build = Petrinet.startNet();
        }
        // build = PetrinetReachability.removeUnreachableStates(build);

        build.reown("");
        build.setSequential(ain.isSequential());
        //build.validatePNet();
        MyAssert.validate(build, "Owners Rule output ");
        //assert build.validatePNet("Owners Net"): "Owners Rule Failure";
        System.out.println("\n\n    Owners end with " + build.myString());
        return build;
    }

    private static Petrinet stripStar(Petrinet pout) throws CompilationException {
        //System.out.println("\n Strip Star pout "+pout.myString());
        Set<String> roots = pout.getRoots().stream().
            flatMap(Set::stream).collect(Collectors.toSet());
        //System.out.println("stripStar roots "+roots);
        pout.getRootPlacess().clear();
        String rs = "";
        Set<PetriNetTransition> toStrip = new HashSet<>();
        if (roots.size() != 1) {
            //System.out.println("\nWARNING  root size = " + roots.size() + " other than for testing should be 1");
            Throwable t = new Throwable();
            t.printStackTrace();
    /*System.out.println("Owner build failed. Strip start failure "+pout.myString());
     throw new CompilationException(pout.getClass(),
       "Owner build failed. Strip start failure "+pout.myString()); */
            Set<String> allRoots = pout.getAllRoots().stream().map(x -> x.getId()).collect(Collectors.toSet());
            toStrip = allRoots.stream().map(x -> pout.getPlaces().get(x)).map(x -> x.post()).
                flatMap(Set::stream).collect(Collectors.toSet());
        } else {
            rs = roots.iterator().next();
            toStrip = pout.getPlaces().get(rs).post();
            //System.out.println("OR Post S* "+toStrip.size()+ " \n " +toStrip.stream().
            //       map(x->x.post().stream().map(x1->x1.myString()+"\n ").collect(Collectors.joining())).collect(Collectors.joining()));
        }
        //System.out.println("rs = "+rs);
        List<Set<String>> newRoots = new ArrayList<>();
        Integer rindx = 1;
        for (PetriNetTransition tr : toStrip) {
            //System.out.println("trans "+tr.myString());
            Set<String> rtpost = tr.post().stream().map(x -> x.getId()).collect(Collectors.toSet());
            newRoots.add(rtpost);
            pout.removeTransition(tr);
            for (String plname : rtpost) {
                PetriNetPlace pl = pout.getPlaces().get(plname);
                //System.out.println("OR new Root "+pl.myString());
                pl.addStartNo(rindx);
                pl.setStart(true);
            }
            rindx++;
        }
        //System.out.println("rs = "+rs);
        if (pout.getPlaces().keySet().contains(rs)) pout.removePlace(pout.getPlaces().get(rs));
        pout.setRoots(newRoots);
        //pout.setRootFromNet();
        //System.out.println("StripStar OUT"+pout.myString()+"\n");
        return pout;
    }

    private static String printVisited(Map<AutomatonNode, PetriNetPlace> v) {
        String out = "Visited {";
        for (AutomatonNode nd : v.keySet()) {
            out = out + " " + nd.getId() + "->" + v.get(nd).getId() + ",";
        }
        return out + "}";
    }

    private static String printPhase2(Map<AutomatonNode, AutomatonNode> v) {
        String out = "PHASE2 {";
        for (AutomatonNode nd : v.keySet()) {
            out = out + " " + nd.getId() + "->" + v.get(nd).getId() + " T " + v.get(nd).isTerminal() + ",";
        }
        return out + "}";
    }


    private static String mark2String(Map<String, PetriNetPlace> mark) {
        String x = "{";
        for (String k : mark.keySet()) {
            x = x + " " + k + "->" + mark.get(k).getId();
        }
        return x + " }";
    }


    private static void printaN2Marking() {
        System.out.println("aN2Marking");
        for (AutomatonNode nd : aN2Marking.keySet()) {
            System.out.println("  " + nd.getId() + " =>> ");
            for (String k : aN2Marking.get(nd).keySet()) {
                System.out.println("    " + k + " own2place " + aN2Marking.get(nd).get(k).myString());
                ;
            }
        }
    }


    /**
     * Build the set of nodes reachable by undirected edges
     * edges filtered by not containing owner own
     * clump for projection onto own process
     *          (collapse edges orthoganal to own)
     *  ONE
     *  If  y
     *      |
     *      | b! own
     *      |
     *      nd --x-Orth(own) ->
     * then y-x-Orth(own)->
     *                     \
     *                      \ b! (own,Orth(own)
     *                       \
     *                        ndClump
     *   TWO
     * if    ---x-own-->nd
     *      |
     *      | b Orth(own)
     *      |
     *      y
     *
     *  then y--x-own-->ndClump
     *
     * @param a
     * @param ndi
     * @param own
     * @return
     */

    private static Set<AutomatonNode> clump(Automaton a, AutomatonNode ndi, String own) {
        Set<AutomatonNode> processed = new HashSet<>();
        System.out.println("CLUMP "+ndi.getId()+" "+own);
        Stack<AutomatonNode> sofar = new Stack<>();
        sofar.push(ndi);

        while (!sofar.isEmpty()) {
            AutomatonNode nd = sofar.pop();
  System.out.println("clumpfrom "+nd.getId());
            if (processed.contains(nd)) continue;
            processed.add(nd);
            Set<AutomatonNode> oneStep = new TreeSet<>(nd.getOutgoingEdges().stream().
                filter(ed -> (!ed.getEdgeOwners().contains(own))).
                // ||  (ed.getOptionalEdge() && !ed.getOptionalOwners().contains(own)))).
                    map(e -> e.getTo()).collect(Collectors.toSet()));
/*    ONE
     *  If  y
     *      |
     *      | b! own
     *      |
     *      nd --x-Orth(own) ->
     *
     * then y-x-Orth(own)->
     *                     \
     *                      \ b! (own,Orth(own)
     *                       \
     *                        ndClump

 */
        for(AutomatonEdge bCastEdge: nd.getIncomingEdges()) {
                 if (bCastEdge.getLabel().endsWith(Constant.BROADCASTSoutput) &&
                     bCastEdge.getEdgeOwners().contains(own)) {
               System.out.println("One bCast "+bCastEdge.myString());
                     for (AutomatonEdge edge : nd.getOutgoingEdges()) {
                         boolean orthoganal = bCastEdge.getMarkedOwners().stream().
                             map(x -> !edge.getEdgeOwners().contains(x)).
                             reduce(true, (x, y) -> x && y);
                         if (orthoganal) {
                             for (AutomatonEdge top : bCastEdge.getFrom().getOutgoingEdges()) {
             System.out.println("One orth "+top.myString());
                               if (top.getEdgeOwners().contains(own)&&
                                   top.getLabel().equals(edge.getLabel())) {
                                   for (AutomatonEdge cast : top.getFrom().getOutgoingEdges()) {
                                       if (cast.getLabel().equals(bCastEdge.getLabel())) {
                                           oneStep.add(cast.getTo());
                          System.out.println("ONE "+nd.getId()+" + "+ cast.getTo());
                                       }
                                   }
                               }
                             }
                         }
                     }
                 }
             }

/*
 * if    ---x-own-->nd
 *      |
 *      | b Orth(own)
 *      |
 *      y
 *
 *  then y--x-own-->ndClump
 */
            for(AutomatonEdge edge: nd.getIncomingEdges()) {
                if (edge.getEdgeOwners().contains(own)) {
             System.out.println("Two "+edge.myString());
                    for (AutomatonEdge bCast : edge.getFrom().getOutgoingEdges()) {
              System.out.println("  Two bcast?");
                        if (!bCast.getLabel().endsWith(Constant.BROADCASTSoutput)) continue;
              System.out.println("  Two bcast "+bCast.myString());
                        boolean orthoganal = bCast.getMarkedOwners().stream().
                            map(x -> !edge.getEdgeOwners().contains(x)).
                            reduce(true, (x, y) -> x && y);
                        if (orthoganal) {
             System.out.println("    Two orth "+bCast.myString());
                            for (AutomatonEdge bottom : bCast.getTo().getOutgoingEdges()) {
             System.out.println("    Two bottom "+bottom.myString());
                                if (bottom.getEdgeOwners().contains(own)&&
                                    bottom.getLabel().equals(edge.getLabel())) {
                                            oneStep.add(bottom.getTo());
                        System.out.println("TWO "+nd.getId()+" + "+bottom.getTo().getId());
                                }
                            }
                        }
                    }
                }
            }

            oneStep.addAll(nd.getIncomingEdges().stream().
                filter(ed -> (!ed.getEdgeOwners().contains(own))).
                // || (ed.getOptionalEdge() && !ed.getOptionalOwners().contains(own)))).
                    map(e -> e.getFrom()).collect(Collectors.toSet()));

            //System.out.println("All "+ union.stream().map(x->x.getId()+", ").collect(Collectors.joining()));
            sofar.addAll(oneStep);
        }

        return processed;
    }


    private static class todoStack {
        private Stack<AutomatonNode> stackit = new Stack<>();

        public void todoPush(AutomatonNode nd) {
            if (stackit.contains(nd)) {
                //System.out.println("stackit Duplicate " + nd.myString());
                return;
            } else {
                stackit.push(nd);
            }
        }

        public int todoSize() {
            return stackit.size();
        }

        public AutomatonNode todoPop() {
            return stackit.pop();
        }

        public boolean todoIsEmpty() {
            return stackit.isEmpty();
        }
    }

}
