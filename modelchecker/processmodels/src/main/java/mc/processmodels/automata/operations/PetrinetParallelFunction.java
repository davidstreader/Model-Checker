package mc.processmodels.automata.operations;

import lombok.SneakyThrows;
import mc.Constant;
import mc.exceptions.CompilationException;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
import mc.util.expr.MyAssert;

import java.util.*;
import java.util.stream.Collectors;

/*
   a->r?->b || x->r?->y   must set PetriNetEdges to optional
   (a->r?->b || x->r?->y) || p->r!->q  ~ a->r?->b || (x->r?->y || p->r!->q)
   dose match intuitive  behaviour.
   a-r?0->b || x-r!->y ==> a-OPT-r! x->r!  BUT
   multiple optional gives one transition not multiple transitions
     a-r?0->b, c-r?->d  || x-r!->y ==> a-OPT-r! a-OPT-r! x->r!
   Problem how do we connect the input to the correct output!
 */
public class PetrinetParallelFunction {
    /*
       Broadcast a! a?
       Brosadcast a? a?  acts like handshake but
            one a? should NOT be blocked by another process not listening
     */
    //private static Set<String> unsynchedActions;
    private static Set<String> synchronisedActions; // set of evnet names
    private static Map<Petrinet, Map<PetriNetPlace, PetriNetPlace>> petriPlaceMap;
    private static Map<PetriNetTransition, PetriNetTransition> petriTransMap;

    private static Map<String, Integer> labelToOptionalNumber = new TreeMap<>();
    private static Map<String, PetriNetTransition> labelToTransition = new TreeMap<>();

    //private static final String tag1 = ""; //"*P1";
    //private static final String tag2 = ""; //"*P2";
// Parallel comp on Petri nets call from Owners Rule
    public static Petrinet compose(Petrinet pi1, Petrinet pi2, Set<String> flags) throws CompilationException {
        clear();
        Petrinet p1 = pi1.reId("1");
        Petrinet p2 = pi2.reId("2");
        List<Set<String>> newEnds = buildEnds(p1.getEnds(), p2.getEnds());
        p1.rebuildAlphabet();
        p2.rebuildAlphabet();
        //MyAssert.myAssert(p1.validatePNet("Parallel || input "+p1.getId()+ " valid ="), "|| precondition Failure");
        //MyAssert.myAssert(p2.validatePNet("Parallel || input "+p2.getId()+ " valid ="), "|| precondition Failure");
        MyAssert.validate(p1, "|| precondition Failure");
        MyAssert.validate(p2, "|| precondition Failure");
        //System.out.println("     PETRINET PARALLELFUNCTION"+"    "+p1.getId()+"  ||"+flags+"    "+p2.getId());
        //System.out.println("     PETRINET PARALLELFUNCTION"+"\n    "+p1.myString()+"\n    ||"+flags+"\n    "+p2.myString());
        //builds synchronisedActions set
        setupActions(p1, p2, flags);
        //System.out.println("  synchronisedActions " + synchronisedActions);
        Petrinet composition = new Petrinet(p1.getId() + p2.getId(), false);
        composition.getOwners().clear();
        composition.getOwners().addAll(p1.getOwners());
        composition.getOwners().addAll(p2.getOwners());

        List<Set<String>> roots = buildRoots(p1, p2);

        petriTransMap.putAll(composition.addPetrinetNoOwner(p1, ""));  //Tag not needed as reId dose this
        //System.out.println("par "+ composition.myString());
        petriTransMap.putAll(composition.addPetrinetNoOwner(p2, "")); //adds unsynchronised transitions
        //System.out.println("newEnds "+newEnds);
        composition.setRoots(roots);
        composition.setEnds(newEnds);
        composition.setRootFromNet();
        composition.setEndFromNet();
        //System.out.println("BeforeSYNC  " + composition.myString("edge"));
        //do not merge places?
        setupSynchronisedActions(p1, p2, composition);
        //System.out.println("AfterSYNC  " + composition.myString("edge"));


    /*  This is buggy and should be redundent as tokenRule -> Owners Rule does this
    composition = PetrinetReachability.removeUnreachableStates(composition, false);
     */
        //System.out.println("  synced  \n "+ composition.myString("edges"));
        // One end of a pair removed
        composition.reId("");
        //MyAssert.myAssert(composition.validatePNet("Parallel || output "+composition.getId()+ " valid ="), "||  Failure");
        MyAssert.validate(composition, "|| output Failure");

        //System.out.println("Parallel composition "+ composition.getId()+ " transitions "+composition.getTransitions().size() + " places "+ composition.getPlaces().size());
        //System.out.println("Parallel composition END  " + composition.myString("edge"));
        return composition;
    }

    /**
     * @param net1
     * @param net2
     * @return the multiRoot for parallel composition of the nets
     */
    private static List<Set<String>> buildRoots(Petrinet net1, Petrinet net2) {
        //System.out.println("Building Roots");
        List<Set<String>> out = new ArrayList<>();
        for (Set<String> m1 : net1.getRoots()) {
            for (Set<String> m2 : net2.getRoots()) {
                out.add(buildMark(m1, m2));
            }
        }
        //System.out.println("New buildRoots "+out);
        return out;
    }

    private static List<Set<String>> buildEnds(List<Set<String>> p1, List<Set<String>> p2) {
        //System.out.println("Build Ends input "+p1+" "+p2);
        List<Set<String>> out = new ArrayList<>();
        for (Set<String> e1 : p1) {
            for (Set<String> e2 : p2) {
                Set<String> o = new HashSet<>();
                o.addAll(e1);
                o.addAll(e2);
                o = o.stream().distinct().sorted().collect(Collectors.toSet());
                out.add(o);
            }
        }
        //System.out.println("Build Ends returns  "+out);
        return out;
    }

    /*
         Adds actions to synchronisedActions.
     1.  _||_ same name sync
     2.  _||{flags}_  same name OR name in flags sync
         has the effect of blocking events in flags if they are only in one process
     */
    private static void setupActions(Petrinet p1, Petrinet p2, Set<String> flags) {
        //System.out.println("setupActions flags "+ flags);
        Set<String> actions1 = new TreeSet<>();
        p1.getAlphabet().keySet().stream().forEach(a -> actions1.add(a));
        Set<String> actions2 = new TreeSet<>();
        p2.getAlphabet().keySet().stream().forEach(a -> actions2.add(a));
        for (String f : flags) {
            actions1.add(f);
            actions2.add(f);
        }
        actions1.forEach(a -> setupAction(a, actions2));
        actions2.forEach(a -> setupAction(a, actions1));


        //System.out.println("setupAction "+synchronisedActions);
    }

    private static void setupAction(String action, Set<String> otherPetrinetActions) {
        //System.out.println("setupAction " + action + " " + otherPetrinetActions);
        if (action.endsWith(Constant.BROADCASTSoutput)) {
            if (containsReceiverOf(action, otherPetrinetActions)) {
                synchronisedActions.add(action);
            } else if (containsBroadcasterOf(action, otherPetrinetActions)) {
                synchronisedActions.add(action);
            }
        } else if (action.endsWith(Constant.BROADCASTSinput)) {
            if (containsReceiverOf(action, otherPetrinetActions)) {
                synchronisedActions.add(action);
            } else if (containsBroadcasterOf(action, otherPetrinetActions)) {
                synchronisedActions.add(action);
            }
        } else if (action.endsWith(Constant.ACTIVE)) {
            String passiveAction = action.substring(0, action.length() - 1);

            if (otherPetrinetActions.contains(passiveAction)) {
                synchronisedActions.add(action);
                //synchronisedActions.add(passiveAction);
            }
        } else if (otherPetrinetActions.contains(action)) {
            synchronisedActions.add(action);
        }
        //System.out.println("Sync " + action + " with " + synchronisedActions);
    }

    @SneakyThrows(value = {CompilationException.class})
    private static void setupSynchronisedActions(Petrinet p1, Petrinet p2, Petrinet comp) {
        //System.out.println("Start setupSynchronisedActions ");
        for (String action : synchronisedActions) { // event names
            Set<PetriNetTransition> p1P = new TreeSet<>();
            Set<PetriNetTransition> p2P = new TreeSet<>();
            //   List<PetriNetTransition> toGo = new ArrayList<>();
            //System.out.println("      action = "+action+ "");
            if (action.endsWith(Constant.BROADCASTSoutput)) {
                String sync = action.substring(0, action.length() - 1) + Constant.BROADCASTSinput;
                //System.out.println("Bcast sync = "+sync);
                p1P = p1.getAlphabet().get(action).stream()
                    .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());
                p2P = p2.getAlphabet().get(sync).stream()
                    .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());
                //    toGo.addAll(p2P.stream().collect(Collectors.toSet()));
                //    toGo.addAll(p1P.stream().collect(Collectors.toSet()));
                addSyncBcastActions(p1P, p2P, comp, action);  //p1P = out
                p1P = p1.getAlphabet().get(sync).stream()
                    .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());
                p2P = p2.getAlphabet().get(action).stream()
                    .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());
                //    toGo.addAll(p2P.stream().collect(Collectors.toSet()));
                //    toGo.addAll(p1P.stream().collect(Collectors.toSet()));
                addSyncBcastActions(p2P, p1P, comp, action); //p2P = out
                //System.out.println("Sync CHECK END " +comp.getEnds());
            } else if (action.endsWith(Constant.ACTIVE)) {
                String sync = action.substring(0, action.length() - 1);

                p1P = p1.getAlphabet().get(action).stream()
                    .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());
                p2P = p2.getAlphabet().get(sync).stream()
                    .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());
                //System.out.println("  alpa1 "+p1P+ "\n  alpa2 "+p2P);
                //   toGo.addAll(p2P.stream().collect(Collectors.toSet()));
                //   toGo.addAll(p1P.stream().collect(Collectors.toSet()));
                addSyncActions(p1P, p2P, comp, action);  //active in p1P
                p1P = p1.getAlphabet().get(sync).stream()
                    .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());
                p2P = p2.getAlphabet().get(action).stream()
                    .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());
                //  toGo.addAll(p2P.stream().collect(Collectors.toSet()));
                //  toGo.addAll(p1P.stream().collect(Collectors.toSet()));
                addSyncActions(p2P, p1P, comp, action);  //active in p2P
                //System.out.println("Sync replace END " +comp.myString());
            } else {
                //do nothing
            }
/*  //dstr 2019 Sept redundent - in setupSynchronisedHS
            //System.out.println("REMOVE "+toGo.stream().map(x->x.getId()).reduce((x,y)-> x+" "+y) );
            if (!action.endsWith(Constant.BROADCASTSinput)) {
                //System.out.println("ping");
                removeoldTrans(comp, toGo.stream().distinct().collect(Collectors.toSet()));
            }
            //System.out.println("Sync2 CHECK END " +comp.getEnds());

*/
        }
        //System.out.println("Sync END BC"+comp.myString());
        setupSynchronisedHS(p1, p2, comp);
        //System.out.println("Sync END HS"+comp.myString());

    }

    /*
      Replaces action pairs with synchronising event
     */
    @SneakyThrows(value = {CompilationException.class})
    private static void setupSynchronisedHS(Petrinet p1, Petrinet p2, Petrinet comp) {
        //System.out.println("Start setupSynchronisedHS ");


        for (String action : synchronisedActions) {
            //System.out.println("setupSyncHS action " + action);
            Set<PetriNetTransition> p1Pair = p1.getAlphabet().get(action).stream()
                .map(t -> petriTransMap.get(t)).  // filter now and addSync can fail
                //  filter(f->!(f.getLabel().endsWith(Constant.BROADCASTSinput) && f.postEqualsPre())).
                    collect(Collectors.toSet());
            Set<PetriNetTransition> p2Pair = p2.getAlphabet().get(action).stream()
                .map(t -> petriTransMap.get(t)).
                //     filter(f->!(f.getLabel().endsWith(Constant.BROADCASTSinput) && f.postEqualsPre())).
                    collect(Collectors.toSet());
            //System.out.println("p1Pair " + p1Pair.size());
            //    filter(f->!f.getLabel().endsWith(Constant.BROADCASTSinput)).
            Set<PetriNetTransition> toGo = p1Pair.stream().
                collect(Collectors.toSet());
            toGo.addAll(p2Pair.stream().
                collect(Collectors.toSet()));


            if (p1Pair.size() > 0 && p2Pair.size() > 0) {
                addSyncActions(p1Pair, p2Pair, comp, action); //handshake on label equality
            }

            //System.out.println("setupSynchronisedHS to Remove " + toGo.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
            removeoldTrans(comp, toGo);  // remove actions that synchronise

        }

    }

    /*
       For each pair of synchronising transitions add new transition with their combined transition
       outputs in p1_ and inputs in p2_

       NOT a! a?
     */
    private static void addSyncActions(Set<PetriNetTransition> p1_, Set<PetriNetTransition> p2_,
                                       Petrinet comp, String action)
        throws CompilationException {
        //optional is only true if one tran is b! and the other b?
        //only synced transitions passed into the method
        //if (p1_.size()==0 || p2_.size() ==0) return; // Must continue as delete transitions at end

        //System.out.println("p1! " + p1_.stream().map(x -> x.getLabel()).reduce("", (x, y) -> x + y + " "));
        //System.out.println("p2? " + p2_.stream().map(x -> x.getLabel()).reduce("", (x, y) -> x + y + " "));
        for (PetriNetTransition t1 : p1_) { //not e?  only e!
            for (PetriNetTransition t2 : p2_) {
                //System.out.println("addSyncActions\n      "+t1.myString()+"\n      "+t2.myString());

                //Build the outgoing and incoming edges skipping listening loops
                Set<PetriNetEdge> outgoingEdges = new LinkedHashSet<>();
                Set<PetriNetEdge> incomingEdges = new LinkedHashSet<>();
                PetriNetTransition newTrans = comp.addTransition(action);
                newTrans.clearOwners();
                if (!(t1.getLabel().endsWith(Constant.BROADCASTSinput) && t1.postEqualsPre())) {
                    outgoingEdges.addAll(t1.getOutgoing());  // pointer adding
                    incomingEdges.addAll(t1.getIncoming());
                    newTrans.addOwners(t1.getOwners());
                }
                if (!(t2.getLabel().endsWith(Constant.BROADCASTSinput) && t2.postEqualsPre())) {
                    incomingEdges.addAll(t2.getIncoming());
                    outgoingEdges.addAll(t2.getOutgoing());
                    newTrans.addOwners(t2.getOwners());
                }
                // incomingEdges and outgoingEdges  built

                //System.out.println("Half way "+newTrans.myString());
                //System.out.println("size "+incomingEdges.size()+" "+incomingEdges.size());
                //Set broadcast listening b? edges to optional
                for (PetriNetEdge outE : outgoingEdges) { // outgoing from transition
                    //System.out.println("out "+outE.myString());
                    PetriNetEdge newOut = comp.addEdge((PetriNetPlace) outE.getTo(), newTrans, outE.getOptional());
                    if (((PetriNetTransition) outE.getFrom()).getLabel().endsWith(Constant.BROADCASTSinput)) {
                        newOut.setOptional(true);
                        //System.out.println("  newOut= " + newOut.myString());
                    }
                    if (outE.getOptional()) newOut.setOptional(true); // set by owners rule
                    //System.out.println("  adding "+ed.myString());
                }
                //System.out.println("  newTran "+newTrans.myString());
                for (PetriNetEdge inE : incomingEdges) { // incoming to transition
                    //System.out.println("in  "+inE.myString());
                    PetriNetEdge newIn = comp.addEdge(newTrans, (PetriNetPlace) inE.getFrom(), inE.getOptional());
                    if (((PetriNetTransition) inE.getTo()).getLabel().endsWith(Constant.BROADCASTSinput)) {
                        newIn.setOptional(true);
                        //System.out.println("  newIn= " + newIn.myString());
                    }
                    if (inE.getOptional()) newIn.setOptional(true); // set by owners rule
                    //System.out.println("    adding "+ed.myString());
                }
         /*       if (t2.getLabel().endsWith(Constant.BROADCASTSinput) &&
                    t1.getLabel().endsWith(Constant.BROADCASTSinput)) {
                    listeningTogo.add(t1);
                    listeningTogo.add(t2);
                }

          */
                //System.out.println("  newTrans " + newTrans.myString());

            }
        }

        //need to add output when listening is implicit
        if (p2_.size() == 0) {
            for (PetriNetTransition t1 : p1_) {
                if (t1.getLabel().endsWith(Constant.BROADCASTSoutput)) {
                    PetriNetTransition newTrans = comp.addTransition(t1.getLabel());
                    newTrans.clearOwners();
                    newTrans.addOwners(t1.getOwners());

                    for (PetriNetEdge outE : t1.getOutgoing()) { // outgoing from transition
                        //System.out.println("out "+outE.myString());
                        PetriNetEdge ed = comp.addEdge((PetriNetPlace) outE.getTo(), newTrans, outE.getOptional());
                        //System.out.println("    *adding " + ed.myString());
                    }
                    for (PetriNetEdge inE : t1.getIncoming()) { // incoming to transition
                        //System.out.println("in  "+inE.myString());
                        PetriNetEdge ed = comp.addEdge(newTrans, (PetriNetPlace) inE.getFrom(), inE.getOptional());
                        //System.out.println("    *adding "+ed.myString());
                    }
                    //System.out.println("  *newTrans "+newTrans.myString());
                }
            }
        }

        //System.out.println(" XXXXXXXX   addSyncActions returns " );
        return;
    }

    private static void addSyncBcastActions(Set<PetriNetTransition> p1_, Set<PetriNetTransition> p2_,
                                            Petrinet comp, String action)
        throws CompilationException {
        //optional is only true if one tran is b! and the other b?
        //only synced transitions passed into the method
        //if (p1_.size()==0 || p2_.size() ==0) return; // Must continue as delete transitions at end

        //System.out.println("p1! " + p1_.stream().map(x -> x.getLabel()).reduce("", (x, y) -> x + y + " "));
        //System.out.println("p2? " + p2_.stream().map(x -> x.getLabel()).reduce("", (x, y) -> x + y + " "));
        for (PetriNetTransition t1 : p1_) { //only e!
            labelToOptionalNumber.clear();
            labelToTransition.clear(); // needed for more than one e!
            PetriNetTransition newTrans = comp.addTransition(action);
// one transition per Bcast output (not per pair)
            newTrans.clearOwners();
            for (PetriNetEdge outE : t1.getOutgoing()) { // outgoing from transition
                PetriNetEdge newOut = comp.addEdge((PetriNetPlace) outE.getTo(), newTrans, outE.getOptional());
                //System.out.println("  adding "+newOut.myString());
            }
            for (PetriNetEdge inE : t1.getIncoming()) { // incoming to transition
                PetriNetEdge newIn = comp.addEdge(newTrans, (PetriNetPlace) inE.getFrom(), inE.getOptional());
                //System.out.println("    adding "+newIn.myString());
            }
            newTrans.addOwners(t1.getOwners());
            for (PetriNetTransition t2 : p2_) {
                //System.out.println("addSyncActions   "+t1.myString()+"\n      "+t2.myString());
                if (t2.getLabel().endsWith(Constant.BROADCASTSinput) &&
                    t2.postEqualsPre()) continue;
                if (t2.getLabel().endsWith(Constant.BROADCASTSinput)) {
                    String lab = t2.getLabel();
                    if (labelToOptionalNumber.containsKey(lab)) {
                        labelToOptionalNumber.put(lab, labelToOptionalNumber.get(lab) + 1);
                    } else {
                        labelToOptionalNumber.put(lab, 1);
                        labelToTransition.put(lab, newTrans);
                    }

                    for (PetriNetEdge outE : t2.getOutgoing()) { // outgoing from transition
                        PetriNetEdge newOut = comp.addEdge((PetriNetPlace) outE.getTo(), labelToTransition.get(lab), outE.getOptional());
                        newOut.setOptional(true);
                        newOut.setOptionNum(labelToOptionalNumber.get(lab));
                        newTrans.addOwners(((PetriNetPlace) outE.getTo()).getOwners());
                        //System.out.println("  adding "+newOut.myString());
                    }
                    for (PetriNetEdge inE : t2.getIncoming()) { // incoming to transition
                        PetriNetEdge newIn = comp.addEdge(labelToTransition.get(lab), (PetriNetPlace) inE.getFrom(), inE.getOptional());
                        newIn.setOptional(true);
                        newIn.setOptionNum(labelToOptionalNumber.get(lab));
                        //System.out.println("    adding "+newIn.myString());
                    }
                }

    //System.out.println("  newTrans " + newTrans.myString());

            }
        }
        //need to add output when listening is implicit
        if (p2_.size() == 0) {
            for (PetriNetTransition t1 : p1_) {
                if (t1.getLabel().endsWith(Constant.BROADCASTSoutput)) {
                    PetriNetTransition newTrans = comp.addTransition(t1.getLabel());
                    newTrans.clearOwners();
                    newTrans.addOwners(t1.getOwners());

                    for (PetriNetEdge outE : t1.getOutgoing()) { // outgoing from transition
                        //System.out.println("out "+outE.myString());
                        PetriNetEdge ed = comp.addEdge((PetriNetPlace) outE.getTo(), newTrans, outE.getOptional());
                        //System.out.println("    *adding " + ed.myString());
                    }
                    for (PetriNetEdge inE : t1.getIncoming()) { // incoming to transition
                        //System.out.println("in  "+inE.myString());
                        PetriNetEdge ed = comp.addEdge(newTrans, (PetriNetPlace) inE.getFrom(), inE.getOptional());
                        //System.out.println("    *adding "+ed.myString());
                    }
                    //System.out.println("  *newTrans "+newTrans.myString());
                }
            }
        }

        //System.out.println(" XXXXXXXX   addSyncActions returns " );
        return;
    }


    private static void removeoldTrans(Petrinet comp, Set<PetriNetTransition> toGo)
        throws CompilationException {
        //System.out.println("Removing "+toGo.size());
        for (PetriNetTransition oldTrans : toGo) {
            //System.out.print(" id "+oldTrans.getId()+" ");
            if (comp.getTransitions().containsValue(oldTrans)) {
                //System.out.println("removing "+oldTrans.myString());
                comp.removeTransition(oldTrans);
            } else {
                //System.out.println("SKIPPING");
            }
        }
    }


    private static boolean containsReceiverOf(String broadcaster, Collection<String> otherPetrinet) {
        for (String reciever : otherPetrinet) {
            if (reciever.endsWith(Constant.BROADCASTSinput)) {
                if (reciever.substring(0, reciever.length() - 1).equals(broadcaster.substring(0, broadcaster.length() - 1))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean containsBroadcasterOf(String broadcaster, Set<String> otherPetrinet) {
        String broadcastAction = broadcaster.substring(0, broadcaster.length() - 1);
        for (String receiver : otherPetrinet) {
            if (receiver.endsWith(Constant.BROADCASTSoutput)) {
                String action = receiver.substring(0, receiver.length() - 1);
                if (action.equals(broadcastAction)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void clear() {
        //unsynchedActions = new HashSet<>();
        synchronisedActions = new HashSet<>();
        petriPlaceMap = new HashMap<>();
        petriTransMap = new HashMap<>();
        labelToOptionalNumber = new TreeMap<>();

    }

    @SneakyThrows(value = {CompilationException.class})
    public static List<Set<String>> addPetrinet(Petrinet addTo, Petrinet petriToAdd) {
        //addTo.validatePNet();
        //petriToAdd.validatePNet();
        //System.out.println("IN AddTo "+addTo.myString());
        //System.out.println("IN ToAdd "+petriToAdd.myString());
        List<Set<String>> roots = addTo.getRoots();
        //System.out.println("roots "+roots);
        Map<PetriNetPlace, PetriNetPlace> placeMap = new HashMap<>();
        Map<PetriNetTransition, PetriNetTransition> transitionMap = new HashMap<>();

        for (PetriNetPlace place : petriToAdd.getPlaces().values()) {
            PetriNetPlace newPlace = addTo.addPlace();
            newPlace.copyProperties(place);

            if (place.isStart()) {
                newPlace.setStart(true);
            }

            placeMap.put(place, newPlace);
        }
        for (PetriNetTransition transition : petriToAdd.getTransitions().values()) {
            PetriNetTransition newTransition = addTo.addTransition(transition.getLabel());
            transitionMap.put(transition, newTransition);
        }

        for (PetriNetEdge edge : petriToAdd.getEdges().values()) {
            //System.out.println(edge.myString());
            if (edge.getFrom() instanceof PetriNetPlace) {
                //System.out.println("tran "+transitionMap.get(edge.getTo()).myString());
                PetriNetEdge e = addTo.addEdge(transitionMap.get(edge.getTo()), placeMap.get(edge.getFrom()), edge.getOptional());
                e.setOptional(edge.getOptional());

            } else {
                //System.out.println("place "+placeMap.get(edge.getTo()).myString());
                PetriNetEdge e = addTo.addEdge(placeMap.get(edge.getTo()), transitionMap.get(edge.getFrom()), edge.getOptional());
                e.setOptional(edge.getOptional());
            }
        }
        //System.out.println("toAdd roots"+petriToAdd.getRoots());
        roots.addAll(petriToAdd.getRoots());
        //System.out.println("roots"+petriToAdd.getRoots());
        addTo.setRoots(roots);
        petriTransMap.putAll(transitionMap);
        petriPlaceMap.put(petriToAdd, placeMap);

        //addTo.validatePNet();
        //System.out.println("OUT AddedTo "+addTo.myString());
        return roots;
    }


    private static Set<String> buildMark(Set<String> m1, Set<String> m2) {
        Set<String> out = new HashSet<>();
        out.addAll(m1);
        out.addAll(m2);
        out = out.stream().sorted().collect(Collectors.toSet());
        //System.out.println("Next root "+out);
        return out;
    }
}

