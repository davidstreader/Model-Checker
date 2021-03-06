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
    private static Stack todo = new Stack();
    private static Map<String, AutomatonEdge> processed = new HashMap<>(); // No new Peri transition needed

    private static Set<AutomatonNode> ndProcessed;

    private static void clean() {
        aN2Marking = new HashMap<>();
        todo = new Stack();
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
     * For broadcast events petri edges (have unique owner) and may be "optional"
     * the automata edges built from Transitions + Marking must have a list of optional owners
     * <p>
     *
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
     * The  o1-automaton edge is labeled with b? if o1 \in Opt(b) else label with b!
     * <p>
     * Islands can be linked by bridges
     * Bridge nd--ndclump  (when mapping in the other direction nd--ndx)
     * <p>
     * * y-x-Orth(own)---> ndx
     * * |                 \
     * * | b! own            \b! (own,Orth(own)
     * * |                     \
     * * z -x-Orth(own)->nd     ndClump
     * |         |
     * w         w
     * |         |
     * ndg1      ndg2
     * under the bridge are gaps  ndg1-- ndg2
     * gaps need to be filled by linking the island on each side of the gap
     * <p>
     * A degenerate case exists when the b? event (owner o1) is
     * both the first to be executed and is not part of a loop
     * Can be recognised by ndf-b!-{01,o2}->nd AND  ndp-b!-{o2}->nd
     * I.E. both optional and non optional execution lead to the same node.
     * bridge = ndf - ndp
     * When ng1-x-{o2}->ndf and ng2-x-{o2}0>ndp then
     * gap ng1 - ng2
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
        //System.out.println("\nOwners autom \n" + autom.myString() + "\n");
        //System.out.println("edge  \n" + autom.getEdges().stream().filter(x -> x.getLabel().endsWith(Constant.BROADCASTSoutput)).map(x -> x.myString() + ",\n").collect(Collectors.joining()));
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
        //System.out.println("Owners START " + autom.getOwners() + "\n");
        for (String own : autom.getOwners()) {
            //System.out.println("\nSTART FOR >>>>>>>Owner " + own);
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
            Map<String, PetriNetPlace> nd2Pl = new TreeMap<>();
            Set<Bridge> bridges = new TreeSet<>();
            Map<String, Set<String>> nd2Clump = new TreeMap<>();
            List<Set<String>> clumps = new ArrayList<>();
            boolean first = true;
            // Just build the nd2Pl mapping - use clumping
            while (!toDo.isEmpty()) {
                String ndId = toDo.pop();
                AutomatonNode nd = autom.getNode(ndId);
                //System.out.println("     >>>>>>>>>OWNStart nd " + ndId);
                /* process every reachable node */
                if (processed.contains(ndId)) continue;
                processed.add(ndId);

                Set<String> next = nd.getIncomingEdges()
                    .stream()
                    .filter(ed -> !(ed.getLabel().equals(Constant.DEADLOCK)))
                    .map(ed -> ed.getFrom().getId()).collect(Collectors.toSet());
                next.addAll(nd.getOutgoingEdges().stream().map(ed -> ed.getTo().getId()).
                    filter(x -> !processed.contains(x)).collect(Collectors.toSet()));
                toDo.addAll(next);
                next.clear();

// a clump of nodes all map to one Place in the Slice Petri net
// that is all nodes reachable from node nd without owner own doing anything
// That is project onto ony edges for process at own
                /* Build a clump only once */
                if (nd2Clump.containsKey(ndId)) continue;
                Set<String> clump = clump(autom, ndId, own, bridges);
                //System.out.println("PostClump " + ndId + " -> " + clump + " br.size " + bridges.size());
                //System.out.println("Clump " + clump.stream().map(x -> x.getId() + ", ").collect(Collectors.joining()));
                for (String n : clump) {
                    nd2Clump.put(n, clump);
                }
                clumps.add(clump);
            }
            /* At this point all clumps built connecting nodes linked by edges or Bridges.
             *  Gaps are to be reconciled using the bridges built by clumping. */

            //System.out.println("clumps before Gap filling " + clumps);
            for (Bridge bridge : bridges) {
                String ndId1 = bridge.getFirst();
                String ndId2 = bridge.getSecond();
                //System.out.println("Bridge " + ndId1 + "->" + ndId2);
                /* find all gaps under a  bridge  both clumps and nd2Clump are changed */
                gapFinder(ndId1, ndId2, clumps, autom, own, nd2Clump);
            }

            //System.out.println("CLUMPS after gap filling " + clumps);
            //Build places for clumps
            for (Set<String> clmp : clumps) {
                //System.out.println("building Places for " + clmp);
                PetriNetPlace addedPlace = petri.addPlace();
                //System.out.println("New Place " + addedPlace.myString());
                TreeSet<String> owns = new TreeSet<>();
                owns.add(own);
                addedPlace.setOwners(owns);


                if (first) {
                    petri.addRoot(Collections.singleton(addedPlace.getId()));
                    addedPlace.setStart(true);
                    addedPlace.getStartNos().add(1);
                    first = false;
                    //System.out.println("Addied Root "+addedPlace.getId());
                }
                for (String n : clmp) {
                    //System.out.println("clump  "+clmp+" has element = " + n);
                    //System.out.println(nd2Pl.keySet());
                    AutomatonNode ndc = autom.getNode(n);
                    if (ndc.isStopNode()) {
                        addedPlace.addEndNo(endcnt++);
                    }
                    if (!nd2Pl.containsKey(n)) {
                        nd2Pl.put(n, addedPlace);
                        //System.out.println("added nd2Pl " + n + " ->" + addedPlace.getId());
                    } else {
                        //System.out.println("NOT ADDED data CORRUPTION");
                    }
                    if (ndc.isTerminal() && ndc.isSTOP()) {
                        addedPlace.setTerminal("STOP");
                        if (addedPlace.getEndNos().size() == 0) addedPlace.addEndNo(endcnt++);
                    }

                }

                toDo.clear();
                processed.clear();
                toDo.push(root.getId());
            }

            //Use the nd2Pl Mapp to build the projected automaton
            //System.out.println("Half way "+petri.myString());
            while (!toDo.isEmpty()) {
                AutomatonNode nd = autom.getNode(toDo.pop());
                //System.out.println("Owners Building from nd " + nd.getId());
                if (processed.contains(nd.getId())) {
                    //System.out.println("Skipped");
                    continue;
                }
                processed.add(nd.getId());
                //System.out.println(nd2Pl.keySet());
                for (AutomatonEdge ed : nd.getOutgoingEdges()) {
                    if (ed.getLabel().equals(Constant.DEADLOCK)) continue;
                    toDo.push(ed.getTo().getId());
                    // if projected away only next node needed
                    if (!ed.getEdgeOwners().contains(own)||
                        (ed.getLabel().endsWith(Constant.BROADCASTSoutput) &&
                            !ed.getMarkedOwners().contains(own)))  continue;

                    String pNetToId = nd2Pl.get(ed.getTo().getId()).getId();
                    String pNetFromId = nd2Pl.get(ed.getFrom().getId()).getId();
                    // Do not add duplicates
                    if (petri.tranExists(pNetFromId,pNetToId,ed.getLabel()))  continue;


                    //System.out.println("  Start 2 " + ed.myString() + " own " + own);

                    //System.out.println(pNetFromId + " -> " + pNetToId);
                    boolean opt = false;
                    String lab = "";
                    if (ed.getLabel().endsWith(Constant.BROADCASTSoutput)) {
                        if (!ed.getOptionalOwners().contains(own)) {
                            lab = ed.getLabel();
                            opt = false;
                            //     lab = ed.getLabel().substring(0, ed.getLabel().length() - 1) + Constant.OwnersFixed; //
                        } else {
                            lab = ed.getLabel().substring(0, ed.getLabel().length() - 1) + Constant.BROADCASTSinput;
                            opt = false;
                        }
                    } else {
                        lab = ed.getLabel();
                    }


                    PetriNetTransition tran = petri.addTransition(lab);
                    //System.out.println("    " + ed.myString());
                    //              to,  from
                    petri.addEdge(tran, nd2Pl.get(ed.getFrom().getId()), opt);
                    petri.addEdge(nd2Pl.get(ed.getTo().getId()), tran, opt);
                    TreeSet<String> O = new TreeSet<>();
                    O.add(own);
                    tran.setOwners(O);
                    //System.out.println("From "+ed.myString());
                    //System.out.println("OwnersAdding "+tran.myString());
                      /*  if ((ed.getOptionalEdge()) && ed.getMarkedOwners().contains(own)) {
                            //System.out.println("    *Adding " + tran.myString());
                        }*/

                }
            }

            //System.out.println("\nSlice Net = " + petri.myString());
            //petri = PetrinetReachability.removeUnreachableStates(petri).copy();
            petri.setEndFromPlace();
            //System.out.println("\npushing " + petri.myString());
            subNets.push(petri);
            //System.out.println(" SLICE Net \n"+petri.myString()+ "\n SLICE Net ");
            Petrinet debug = petri.copy();
            debug = PetrinetReachability.removeUnreachableStates(debug, false);
            debug.setId("projection-" + own);

            processMap.put(debug.getId(), debug);
            //System.out.println(debug.myString());

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
            //System.out.println("  BEFORE reach  " + build.myString());
            build = PetrinetReachability.removeUnreachableStates(build, false);


            //System.out.println("reach *END "+build.myString());

            //3. remove S* to reveal the multiple start states.
            //System.out.println("  OWNERS Rule Strip* start" + build.myString() + "\n*start");
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
        //System.out.println("\n\n    Owners end with " + build.myString());
        clean();
        return build;
    }

    private static void gapFinder(String ndId1, String ndId2, List<Set<String>> clumps, Automaton autom, String own, Map<String, Set<String>> nd2Clump)
        throws CompilationException {
        //System.out.println("GAP starting with "+clumps);
        Map<String, String> found = new TreeMap<>();
        gapFinder(ndId1, ndId2, clumps, autom, own, nd2Clump, found);
    }

    private static void gapFinder(String ndId1, String ndId2, List<Set<String>> clumps, Automaton autom, String own, Map<String, Set<String>> nd2Clump, Map<String, String> oldGaps)
        throws CompilationException {
        //System.out.println("calling gapFinger on "+ndId1+" "+ndId2);
        /* Recersive call terminates when A cycles back to closed gap B no gap found*/
        for (AutomatonEdge g1ed : autom.getNode(ndId1).getOutgoingEdges()) {
            for (AutomatonEdge g2ed : autom.getNode(ndId2).getOutgoingEdges()) {
                //System.out.println(" g1ed " + g1ed.myString());
                //System.out.println(" g2ed " + g2ed.myString());
                String g1 = g1ed.getTo().getId();
                String g2 = g2ed.getTo().getId();
                if ((oldGaps.containsKey(g1) && oldGaps.get(g1).equals(g2)) ||
                    (oldGaps.containsKey(g2) && oldGaps.get(g2).equals(g1))) {
                    continue;
                }
                oldGaps.put(g1, g2);
                //System.out.println(g1ed.getTo().getId() + " -- " + g2ed.getTo().getId());
                if (g1ed.deTaggeEqualLabel(g2ed) &&
                    g1ed.getEdgeOwners().contains(own) &&
                    g2ed.getEdgeOwners().contains(own) &&
                    g1ed.getEdgeOwners().equals(g2ed.getEdgeOwners())) {
                    //System.out.println("  gap = " + g1 + "-" + g2);
                    if (nd2Clump.get(g1).equals(nd2Clump.get(g2))) continue;  // skip duplicate gaps
                    /* glue together the islands on each side of the gap*/
                    Set<String> old = new TreeSet<>();
                    for (Set<String> c1 : clumps) {
                        if (c1.contains(g1)) {
                            if (!c1.contains(g2)) {
                                for (Set<String> c2 : clumps) {
                                    if (c2.contains(g2)) {
                                        c1.addAll(c2);
                                        old = c2;
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    } /* remove clump now part of other clump*/
                    if (old.size() > 0) clumps.remove(old);
                    //System.out.println("new clumps "+clumps);
                    gapFinder(g1, g2, clumps, autom, own, nd2Clump, oldGaps);
                }

            }
        }
        //System.out.println("Gaps ends with "+clumps);
    }

    private static Petrinet stripStar(Petrinet pout) throws CompilationException {
        //System.out.println("\n Strip Star input " + pout.myString());
        if (pout.getRoots().size() != 1) {
            //System.out.println("\nWARNING  root size = " + roots.size() + " other than for testing should be 1");
            Throwable t = new Throwable();
            t.printStackTrace();
            System.out.println("Owner build failed. Strip start failure " + pout.myString());

        }
        Set<String> roots = pout.getRoots().stream().
            flatMap(Set::stream).collect(Collectors.toSet());
        //System.out.println("stripStar roots " + roots);
        String rs = "";
        //Set<PetriNetTransition> toStrip = new HashSet<>();


        rs = roots.iterator().next();
        Set<PetriNetTransition> toStripSstar = pout.getPlaces().get(rs).post();
        //System.out.println("OR Post S* "+toStrip.size()+ " \n " +toStrip.stream().
        //       map(x->x.post().stream().map(x1->x1.myString()+"\n ").collect(Collectors.joining())).collect(Collectors.joining()));
        //  }
        for (String rId : roots) {
            pout.removePlace(pout.getPlace(rId));
        }
        pout.getRootPlacess().clear();

        //System.out.println("rs = "+rs);
        List<Set<String>> newRoots = new ArrayList<>();
        Integer rindx = 1;
        for (PetriNetTransition tr : toStripSstar) {
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
        //System.out.println("StripStar OUT" + pout.myString() + "\n");
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
     * (collapse edges orthoganal to own)
     * ONE
     * If
     * y
     * |
     * | b! own
     * |
     * --x-Orth(own) -> nd
     * then y-x-Orth(own)->
     * \
     * \ b! (own,Orth(own)
     * \
     * ndClump
     * TWO  projecting in other direction
     * if
     * z---x-own-->nd
     * |
     * | b Orth(own)
     * |
     * y
     * <p>
     * then y--x-own-->ndClump
     *
     * @param a
     * @param ndi
     * @param own
     * @return
     */


/*
    There are 3(or 6) ways to add nodes to a clump looking at each way in turn  has
    the problem that any addition to the clump may enable a prviously use way to
    add additional nodes. Hence any element added to a clump must also be used to add
     further elements. This only stops when no new elements are added.

 OUTPUT both returned clump AND updataed bridge
 */
    private static Set<String> clump(Automaton a, String ndi, String own, Set<Bridge> bridge) throws CompilationException {
        Set<String> processed = new HashSet<>();
        Set<String> clump = new TreeSet<>();
        //System.out.println("CLUMP start" +  a.myString()); // + " " + own);
        Stack<String> sofar = new Stack<>();
        sofar.push(ndi);

        while (!sofar.isEmpty()) {
            String ndId = sofar.pop();
            //System.out.println("clump processing "+ndId+" by own "+own + " with remaining sofar "+sofar);
            if (processed.contains(ndId)) continue; // ONLY way to stop is to process all nodes in clu
            Set<String> oneStep = new TreeSet<>();  // to be added to clump and sofar
            AutomatonNode nd = a.getNode(ndId);
            processed.add(ndId);
            clump.add(ndId);
            /*forward */
            for (AutomatonEdge ed : nd.getOutgoingEdges()) {
                //System.out.println(ed.myString());
                if ((!ed.getEdgeOwners().contains(own)) ||
                    (ed.getNotMaximalOwnedEdge() && !ed.getMarkedOwners().contains(own))) {
                    oneStep.add(ed.getTo().getId());
                }
            }
            //System.out.println("oneStep "+oneStep);
        /*    oneStep.addAll(nd.getOutgoingEdges().stream().
                filter(ed -> (!ed.getEdgeOwners().contains(own))
                    || (ed.getOptionalEdge() && !ed.getMarkedOwners().contains(own))).
                map(e -> e.getTo().getId()).collect(Collectors.toSet()));
         */
            /* backward */
        /*    oneStep.addAll(nd.getIncomingEdges().stream().
                filter(ed -> (!ed.getEdgeOwners().contains(own))
                    || (ed.getOptionalEdge() && !ed.getMarkedOwners().contains(own))).
                map(e -> e.getFrom().getId()).collect(Collectors.toSet()));
          */
            for (AutomatonEdge ed : nd.getIncomingEdges()) {
                //System.out.println(ed.myString());
                if ((!ed.getEdgeOwners().contains(own)) ||
                    (ed.getNotMaximalOwnedEdge() && !ed.getMarkedOwners().contains(own))) {
                    oneStep.add(ed.getFrom().getId());
                }
            }
            //System.out.println("oneStep "+oneStep);
 /*  nd -b!-{o1,o2}->xnd AND ndClump -b!-{o1}->xnd
       then nd -- ndClump
  */
            for (AutomatonEdge ed : nd.getOutgoingEdges()) {
                //System.out.println(ed.myString());
                if (ed.getLabel().endsWith(Constant.BROADCASTSoutput) &&
                    ed.getNotMaximalOwnedEdge() &&
                    ed.getMarkedOwners().contains(own)) {  //not equal
                    for (AutomatonEdge other : ed.getTo().getIncomingEdges()) {
                        if (other.getLabel().equals(ed.getLabel()) &&
                            (!other.getNotMaximalOwnedEdge())) {
                            oneStep.add(other.getFrom().getId());
                            Bridge b = new Bridge(ndId, other.getFrom().getId());
                            bridge.add(b);
                            //System.out.println("triangle bridge1 "+b.myString());
                        }
                    }
                }

                if (ed.getLabel().endsWith(Constant.BROADCASTSoutput) &&
                    (!ed.getNotMaximalOwnedEdge())) {
                    for (AutomatonEdge other : ed.getTo().getIncomingEdges()) {
                        if (other.getLabel().equals(ed.getLabel()) &&
                            other.getNotMaximalOwnedEdge() &&
                            other.getMarkedOwners().contains(own)) {  //not equal

                            oneStep.add(other.getFrom().getId());
                            Bridge b = new Bridge(ndId, other.getFrom().getId());
                            bridge.add(b);
                            //System.out.println("triangle bridge2 "+b.myString());
                        }
                    }
                }
            }

// below is just for broadcast processes.
            /*    ONE
             *  If  y
             *      |
             *      | b! own
             *      |
             *        --x-Orth(own) -> ndClump
             *
             * then y--x-Orth(own)->      Own(x) subset Own(b!)
             *                      \
             *                       \ b! (own,Orth(own))
             *                        \
             *                         nd
             */
            //backward ndClump-> nd
            for (AutomatonEdge cast : nd.getIncomingEdges()) {
                if (cast.getLabel().endsWith(Constant.BROADCASTSoutput) &&
                    cast.getMarkedOwners().contains(own)) {  //not equal
                    //System.out.println("<-Own cast "+cast.myString());
                    Set<String> bcast = cast.getMarkedOwners().stream().
                        filter(o -> !o.equals(own)).collect(Collectors.toSet());
                    for (AutomatonEdge top : cast.getFrom().getIncomingEdges()) {
                        if (top.getEdgeOwners().containsAll(bcast) &&
                            !top.getLabel().endsWith(Constant.BROADCASTSoutput)) {
                            //System.out.println("<-Own top "+top.myString());
                            //then
                            for (AutomatonEdge down : top.getFrom().getOutgoingEdges()) {
                                //System.out.println("<-One down "+down.myString());
                                if (down.getLabel().endsWith(Constant.BROADCASTSoutput) &&
                                    down.getEdgeOwners().contains(own)) {
                                    for (AutomatonEdge bottom : down.getTo().getOutgoingEdges()) {
                                        //System.out.println("<-One bottom "+bottom.myString());
                                        if (!bottom.getEdgeOwners().contains(own)) {
                                            oneStep.add(bottom.getTo().getId());
                                            bridge.add(new Bridge(ndId, bottom.getTo().getId()));
                                            //System.out.println("<-ONE Adding " + nd.getId() + " + " + bottom.getTo().myString());
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }

            /*    ONE
             *  If  y
             *      |
             *      | b! own
             *      |
             *         --x-Orth(own) -> nd
             *
             * then y--x-Orth(own)->      Own(x) subset Own(b!)
             *                      \
             *                       \ b! (own,Orth(own))
             *                        \
             *                         ndClump
             */
            //forward  nd ->ndClump
            for (AutomatonEdge bottom : nd.getIncomingEdges()) {
                if (!bottom.getMarkedOwners().contains(own)) {
                    //System.out.println("->One bCast "+bCastEdge.myString());
                    for (AutomatonEdge bcEdge : bottom.getFrom().getIncomingEdges()) {
                        //System.out.println("->One bottom "+bottom.myString());
                        if (bcEdge.getEdgeOwners().contains(own) &&
                            bcEdge.getLabel().endsWith(Constant.BROADCASTSoutput)) {
                            for (AutomatonEdge top : bcEdge.getFrom().getOutgoingEdges()) {
                                //System.out.println("->One top "+top.myString());
                                if (!top.getEdgeOwners().contains(own) &&
                                    top.deTaggeEqualLabel(bottom)) {
                                    for (AutomatonEdge cast : top.getTo().getOutgoingEdges()) {
                                        //System.out.println("->One cast "+cast.myString());
                                        if (cast.deTaggeEqualLabel(bcEdge) &&
                                            cast.getMarkedOwners().containsAll(top.getEdgeOwners())) {
                                            oneStep.add(cast.getTo().getId());
                                            bridge.add(new Bridge(cast.getTo().getId(), ndId));
                                            //System.out.println("->ONE ADDing " + nd.getId() + " + " + cast.getTo().myString());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            /*
             * if    ---x-own-->ndClump
             *      |
             *      | b Orth(own
             *      |
             *      y
             *
             *  then y--x-own-->nd
             */
            //backward ndClump->nd
            for (AutomatonEdge bottom : nd.getIncomingEdges()) {
                if (bottom.getEdgeOwners().contains(own) &&
                    !bottom.getLabel().endsWith(Constant.BROADCASTSoutput)) {
                    //System.out.println("<-TWO bottom " + bottom.myString());

                    //then
                    for (AutomatonEdge bCast : bottom.getFrom().getIncomingEdges()) {
                        //System.out.println("<-TWO bCast " + bCast.myString());
                        if (bCast.getLabel().endsWith(Constant.BROADCASTSoutput) &&
                            !bCast.getMarkedOwners().contains(own)) {
                            for (AutomatonEdge top : bCast.getFrom().getOutgoingEdges()) {
                                //System.out.println("<-TWO top " + top.myString());
                                if (top.getEdgeOwners().contains(own) &&
                                    top.deTaggeEqualLabel(bottom)) {
                                    oneStep.add(top.getTo().getId());

                                    //bridge.put(top.getTo().getId(), ndId);
                                    bridge.add(new Bridge(top.getTo().getId(), ndId));
                                    //System.out.println("<-TWO Adding" + nd.getId() + " + " + top.getTo().getId());
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
            //forward nd->ndClump
            for (AutomatonEdge top : nd.getIncomingEdges()) {
                if (top.getEdgeOwners().contains(own) &&
                    !top.getLabel().endsWith(Constant.BROADCASTSoutput)) {
                    //System.out.println("->Two top "+top.myString());
                    for (AutomatonEdge bCast : top.getFrom().getOutgoingEdges()) {
                        //System.out.println("  Two bcast?");
                        if (bCast.getLabel().endsWith(Constant.BROADCASTSoutput) &&
                            !bCast.getMarkedOwners().contains(own)) {
                            //System.out.println("  ->Two bcast "+bCast.myString());
                            for (AutomatonEdge bottom : bCast.getTo().getOutgoingEdges()) {
                                //System.out.println("  ->Two bottom "+bottom.myString());
                                if (bottom.getEdgeOwners().contains(own) &&
                                    bottom.deTaggeEqualLabel(top)) {
                                    oneStep.add(bottom.getTo().getId());
                                    //bridge.put(ndId, bottom.getTo().getId());
                                    bridge.add(new Bridge(ndId, bottom.getTo().getId()));
                                    //System.out.println("->TWO " + nd.getId() + " + " + bottom.getTo().getId());
                                }
                            }

                        }
                    }
                }
            }


            sofar.addAll(oneStep);
            clump.addAll(oneStep);
            //System.out.println("for "+ndi+" oneStep is "+ oneStep+ " sofar "+sofar);
        }
        //System.out.println("Final Clump " + clump + " " + bridge.size());
        return clump;
    }


    private static class Bridge implements Comparable {
        private String first;
        private String second;

        public Bridge(String f, String s) {
            first = f;
            second = s;
        }

        public String getFirst() {
            return first;
        }

        public String getSecond() {
            return second;
        }


        public boolean equals(Object o) {
            return (o instanceof Bridge)
                && ((Bridge) o).getFirst().equals(first)
                && ((Bridge) o).getSecond().equals(second);
        }

        public String myString() {
            return "fst " + first + "  snd " + second;
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof Bridge)
                return compareTo(first + "**" + second);
            else return -99;
        }
    }

}
