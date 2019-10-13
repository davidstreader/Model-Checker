package mc.processmodels.conversion;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import lombok.Data;
import lombok.SneakyThrows;
import mc.Constant;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

import mc.util.expr.MyAssert;

/**
 * This holds static methods related to the generation of automata from petrinets.
 * <p>
 * This is to be used such that the conversion between the two can be completed, and for being able
 * to make petrinets and automata interoperable within process definitions.
 * <p>
 * For hand shake events the token rule is:
 * A transition t is fired, and produces an automata event, only when its pre places, *t, are all marked. When a transition
 * is fired the tokens on the preplaces *t are removed and places on the post places t* are added.
 * For brodcast events the behaviour of a send event b! can not be blocked by the lack of a synchronising receive event b?
 * Synchronised send-receive transitions are labeled b! and the edges added because of the synchronising b? event are marked as "optional"
 * Thus Let *t = {1,2} where edge 2->t is optional and  t* = {3,4} where edge t->4 is optional behaves:
 * If {1,2} is marked then after t is fired {3,4} is marked.
 * Elseif {1} is marked then after t is fired {3} is marked.
 * BEWARE  two transitions can have same name and (non-optional pre and post) but different onpional pre!
 * In this situation if either optional pre is marked ONLY the transition with the marked optional pre can fire!
 * <p>
 * <p>
 * For a!  || a?->.. -a?->  optionNum on Edge distinguish listeners of single owner
 * Hence if optional place is marked the token must go the place with the correct
 * owner and correct optionNum
 * <p>
 * Automata edge marked optional will be ignored by Owners Rule.
 * With one reachable a? event easy to compute
 * But with more (or less) than one a? event
 * <p>
 * each non Opt triple  Pre,label,Post find max reachable PreOpt and PostOpt
 * mark as optional if not  (Pre\cup MaxPreOpt),label,(Post\cup MaxPostOpt)
 *
 * @author Jordan Smith
 * @author David Streader
 * @author Jacob Beal
 * @see <a href="http://doi.org/10.1006/inco.2002.3117">
 * The Box Algebra = Petri Nets + Process Expressions
 * </a>
 * @see Petrinet
 * @see PetriNetPlace
 * @see Automaton
 * @see AutomatonNode
 */
public class TokenRule {

    private static int stateSizeBound = 10000;

    public static Automaton tokenRule(Petrinet convertFrom) {
        return tokenRule(convertFrom, new HashMap<>(), new HashMap<>());
    }

    /**
     * This method statically converts from a Petrinet to an Automaton visualisation of a given
     * process.
     *
     * @param cFrom            the petrinet that is converted from.
     * @param markingToNodeMap the mapping from the marking to an automaton node, used for display
     * @param nodeToMarkingMap the mapping from node to marking
     * @return The automaton that is equivalent to {@code convertFrom} petrinet.
     */
    @SneakyThrows(value = {CompilationException.class})
    public static Automaton tokenRule(Petrinet cFrom,
                                      Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNodeMap,
                                      Map<AutomatonNode, Multiset<PetriNetPlace>> nodeToMarkingMap) {
        //String nameOnly = convertFrom.getId().replaceAll("[0-9]*$", "");
        //keeps the id numbers low - op_eval resets Automaton.tagid
        Petrinet convertFrom = cFrom.copy();
        Automaton outputAutomaton = new Automaton(convertFrom.getId(), false);
        //System.out.println("TOKEN RULE  STARTING \n    " + convertFrom.myString("edge"));
        MyAssert.validate(convertFrom, "Token Rule precondition ");
        //assert convertFrom.validatePNet("GOT YOU"): "Token Rule precondition";
        Map<PetriNetTransition, Step> maxStep = new TreeMap<>();
        Map<Step, Set<AutomatonEdge>> step2AutEdge = new TreeMap<>();
        outputAutomaton.setOwners(convertFrom.getOwners());
        //System.out.println(convertFrom.getRoots());
        List<Set<PetriNetPlace>> rootsPlaces = new ArrayList<Set<PetriNetPlace>>();

        //
        for (Set<String> rnames : convertFrom.getRoots()) {
            //System.out.println("rnames "+ rnames);
            AutomatonNode root = outputAutomaton.addNode();
            //System.out.println("root "+root.myString());
            root.setStartNode(true);
            outputAutomaton.addRoot(root);
            Set<PetriNetPlace> rts = rnames.stream().
                map(x -> convertFrom.getPlaces().get(x)).collect(Collectors.toSet());
            rootsPlaces.add(rts);
            Multiset<PetriNetPlace> rtms = HashMultiset.create(rts);
            markingToNodeMap.put(rtms, root);
            nodeToMarkingMap.put(root, rtms);
            //System.out.println("root "+root.myString());
        }

        //System.out.println("rootsPlaces "+ rootsPlaces.stream().map(y->y.stream().map(x->x.getId()+" ").collect(Collectors.joining())+" * ").collect(Collectors.joining()));
        Stack<Multiset<PetriNetPlace>> toDo = new Stack<>();
        for (Set<PetriNetPlace> rs : rootsPlaces) {
            toDo.add(HashMultiset.create(rs));
        }
        int ownCnt = convertFrom.getOwners().size();
        Set<Multiset<PetriNetPlace>> previouslyVisitedPlaces = new HashSet<>();
        int nodesCreated = 1;
        int j = 0; //without these 2 LofC loop never terminates!
        while (!toDo.isEmpty()) {
            //System.out.println("toDo size "+toDo.size());
            if (j++ > stateSizeBound) {
                //System.out.println("\nTokenRule Failure " + convertFrom.getId() + "\nLooping = " + j + "\n");
                convertFrom.validatePNet();
                outputAutomaton.validateAutomaton();
                //System.out.println("TokenRule Failure " + outputAutomaton.myString() + "tf tf tf \n");
                throw new CompilationException(convertFrom.getClass(), "Token Rule Failure exceeds size bound " + stateSizeBound);

            } // second LofC  NEVER Called - looks redundent!
            Multiset<PetriNetPlace> currentMarking = toDo.pop();
            //System.out.println("currentMarking "+currentMarking.stream().map(x->x.getId()+", ").collect(Collectors.joining()));

            //System.out.print("\nStarting  prev " +previouslyVisitedPlaces.size()+" todo "+toDo.size()+  " \n");
    /*  for(PetriNetPlace pl :currentMarking){
         //System.out.print(pl.getId()+" ");
      } */
            //System.out.println("");

            if (previouslyVisitedPlaces.contains(currentMarking)) {
                //System.out.println("Visted!");
                continue;
            }

            //System.out.println("currentMarking "+currentMarking);
            Set<PetriNetTransition> satisfiedPostTransitions = satisfiedTransitions(currentMarking); //88
            //System.out.println("Processing "+Petrinet.marking2String(currentMarking)+
            //" trans "+satisfiedPostTransitions.size());
            if (currentMarking.stream().map(x -> x.isSTOP()).reduce(true, (x, y) -> x && y)) {
                markingToNodeMap.get(currentMarking).setStopNode(true);
                //System.out.println("Mark as STOP "+markingToNodeMap.get(currentMarking).getId());
                // outputAutomaton.getEndList().add(markingToNodeMap.get(currentMarking).getId());
            } else if (currentMarking.stream().map(x -> x.isERROR()).reduce(false, (x, y) -> x || y)) {
                //System.out.println("Mark as ERROR "+markingToNodeMap.get(currentMarking).getId());
                markingToNodeMap.get(currentMarking).setErrorNode(true);
            }
            //System.out.println("satisfiedPostTransitions "+ satisfiedPostTransitions.size());
            //Loops through all satisfied Transitions building a newMarking
            // updates markingToNodeMap + builds an Automata edge
            for (PetriNetTransition transition : satisfiedPostTransitions) {
  /*
    If more than one transition is equal except (same name) for optional places
      then ONLY the transition with the most places  can be fired!
  */
                if (transition.getLabel().endsWith(Constant.BROADCASTSoutput)) {
                    Set<PetriNetTransition> equNB = satisfiedPostTransitions.stream().filter(x -> transition.NonBlockingEqu(x)).collect(Collectors.toSet());
                    if (equNB.size() > 1) {
                        for (PetriNetTransition eqnb : equNB) {
                            if (transition.pre().size() < eqnb.pre().size()) continue;
                            //ignore transition as larger transition enabled
                        }
                    }
                }
                //We must be careful not to change the currentMarking
                //System.out.println("currentMarking "+currentMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));

/*          Multiset<PetriNetPlace> cloneMarking = TreeMultiset.create();
          for (Multiset.Entry<PetriNetPlace> ple : currentMarking.entrySet()) {
              cloneMarking.add(((PetriNetPlace) ple.getElement().copy()), ple.getCount());
          }
          //System.out.println("cloneMarking "+cloneMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
*/
                Multiset<PetriNetPlace> newMarking = HashMultiset.create(newMarking(currentMarking, transition));
                //System.out.println("newMarking "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));

                // updates markingToNodeMap for this transition
                if (!markingToNodeMap.containsKey(newMarking)) {
                    AutomatonNode newNode = outputAutomaton.addNode();
                    newNode.setLabelNumber(nodesCreated++);
                    //System.out.println(" add "+ Petrinet.marking2String(newMarking)+"->"+newNode.getId());
                    markingToNodeMap.put(newMarking, newNode);
                    nodeToMarkingMap.put(newNode, newMarking);
                    boolean isEr = false;
                    for (PetriNetPlace pl : newMarking.elementSet()) {
                        if (pl.isERROR()) {
                            // isEr = true;
                            break;
                        }
                    }
                    if (isEr) {
                        toDo.clear();
                    } else {
                        if (!toDo.contains(newMarking)) {
                            toDo.add(newMarking);
                            //System.out.println("   Add Marking "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
                            if (newMarking.size() > ownCnt) {
                                System.out.println("Token Rule Makring not 1 Safe = " + convertFrom.getOwners() + " " + cFrom.getId());
                                System.out.println("           newMarking " + newMarking.stream().map(x -> x.getId() + " ").collect(Collectors.joining()));
                                //  throw new CompilationException(convertFrom.getClass(), "Token Rule Makring to large ");
                            } //+ newMarking.toString());
                        }
                    }
                }

                AutomatonEdge ed = null;
                // this transition will be processed by OwnersRule but may have optional owners dstr Sept 2019
                // optional will be reset below
                if (transition.optionalOwners().size() > 0) {
                    ed = outputAutomaton.addEdge(transition.getLabel(), markingToNodeMap.get(currentMarking),
                        markingToNodeMap.get(newMarking), null,
                        transition.optionalOwners(), true);
                } else {
                    ed = outputAutomaton.addEdge(transition.getLabel(), markingToNodeMap.get(currentMarking),
                        markingToNodeMap.get(newMarking), null,
                        false, true);
                }
                if (transition.getLabel().endsWith(Constant.BROADCASTSoutput)) {
                    ed.setOptionalEdge(true);
                } else {
                    ed.setOptionalEdge(false);
                }
/*
  Not all places of a broadcast transition need be marked hence the ownership might
  be not all the owners of the transition (think Owners rule)
  One transition can be fired with different Steps.
    Only the automata edged built from the maximal Step should be not optional
    All optional edges are ignored by the Owners Rule
 */
                Set<String> own = transition.getOwners();
                ed.setEdgeOwners(own);
                if (transition.getLabel().endsWith(Constant.BROADCASTSoutput)) {
                    //System.out.println("transition.getLabel() " + transition.getLabel() + " " + transition.getOwners());
                    Multiset<PetriNetPlace> unMarkedPre = HashMultiset.create(transition.pre());
                    unMarkedPre.removeIf(x -> currentMarking.contains(x));
                    //System.out.println("unMarkedPre " + unMarkedPre.stream().map(x -> x.getOwners().toString() + " ").collect(Collectors.joining()));
                    Step tranStep = transition.tr2Step(currentMarking);
                    if (maxStep.containsKey(transition)) {
                        Set<String> more = maxStep.get(transition).hasMoreStep(tranStep);
                System.out.println(transition.getId()+" "+transition.getLabel()+
                                         " maxStep-> " +maxStep.get(transition).myString());
                        if (more.size() > 0) {// more to add
                            more.addAll(maxStep.get(transition).getPre());
                            for (AutomatonEdge edg:step2AutEdge.get(maxStep.get(transition))) {
                                edg.setOptionalEdge(true);
                                System.out.println("sub==1 "+edg.myString());
                            }
                            step2AutEdge.remove(maxStep.get(transition));
                            tranStep.setPre(more);

                            Set<AutomatonEdge> auEdSet = new TreeSet<>();
                            auEdSet.add(ed);
                            maxStep.put(transition,tranStep);
                            step2AutEdge.put(tranStep, auEdSet);
                            ed.setOptionalEdge(false);
                            System.out.println("sub==1 "+ed.myString());
                        } else if (sub == 0){ // is maximal
                            System.out.print(step2AutEdge.get(tranStep).size()+" *-->* ");
                            step2AutEdge.get(tranStep).add(ed);
                            System.out.println(step2AutEdge.get(tranStep).size());
                            ed.setOptionalEdge(false);
                            System.out.println("sub==0 "+ed.myString());
                        }
                    } else { // first time is maximal
                        Set<AutomatonEdge> auEdSet = new TreeSet<>();
                        auEdSet.add(ed);
                        maxStep.put(transition,tranStep);
                        step2AutEdge.put(tranStep, auEdSet);
                        ed.setOptionalEdge(false);
                        System.out.println("*firstTime "+ed.myString());
                    }
                }
                //System.out.println("  adding edge "+ed.myString());
            } // END of satisfiedTransitions processing


            //System.out.println("currentMarking2 "+currentMarking.stream().map(x->x.getId()+", ").collect(Collectors.joining()));

            if (!previouslyVisitedPlaces.contains(currentMarking)) {
                previouslyVisitedPlaces.add(currentMarking);
            }
            //System.out.println("loop end todo size "+toDo.size());
            //System.out.println("Add to Previous "+previouslyVisitedPlaces.size()+"  "+currentMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
        }


        //Built  Automata now set End list to be the same as the Petri EndList
        //Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNodeMap

        for (Set<String> mark : convertFrom.getEnds()) {
            Set<PetriNetPlace> mk = mark.stream().map(x -> convertFrom.getPlaces().get(x)).collect(Collectors.toSet());

            Multiset<PetriNetPlace> mkm = HashMultiset.create(mk);
            //System.out.print("\n** End Multiset ");mkm.stream().forEach(x->System.out.print(x.getId()+", "));System.out.println("");
            if (!markingToNodeMap.containsKey(mkm)) {
                //System.out.println(" Stop not reached "+ mark+ " NOT found ");
            } else {
                outputAutomaton.addEnd(markingToNodeMap.get(mkm).getId());
            }
        }
        //added 20Sept 2018 for Failure Testing
        for (AutomatonNode nd : outputAutomaton.getNodes()) {
            //System.out.println("SETERROR node "+nd.myString());
            if (nd.getOutgoingEdges().size() == 0 && !nd.isSTOP()) {
                nd.setErrorNode(true);
                //System.out.println("SETERROR node "+nd.myString());
            }
        }
        outputAutomaton.removeDuplicateEdges();  // may occur with broadcast
        outputAutomaton.setEndFromNodes();
        outputAutomaton.setSequential(cFrom.isSequential());
        //MyAssert.myAssert(outputAutomaton.validateAutomaton("Token Rule output "+outputAutomaton.getId()+" VALID = "), "Token Rule Failure");
        MyAssert.validate(outputAutomaton, "Token Rule output ");
        // assert outputAutomaton.validateAutomaton():"Token Rule Failure";
        System.out.println("Token Rule END \n"+outputAutomaton.myString()+"\n");
        return outputAutomaton;
    }

    /*
      For a given marking and satisfied transition build next marking
      Used both in the Token Rule and interactivly
    For a!  || a?->.. -a?->  need optionNum to distinguish listeners of single owner

     */
    public static Multiset<PetriNetPlace> newMarking(Multiset<PetriNetPlace> currentMarking, PetriNetTransition transition) {
        Multiset<PetriNetPlace> newMarking = HashMultiset.create(currentMarking);
        //System.out.println("\ncurrentMarking " + currentMarking.stream().map(x -> x.getId() + " ").collect(Collectors.joining()));
        //System.out.println("Transition " + transition.myString());
        //System.out.println("newMarking1 "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
        // Clear out the places in the current marking which are moving token

        /* get the owners+ optionNum of the marked Places */
        TreeSet<String> ownMarked = new TreeSet<>();
        Integer optN = new Integer(0);
        for (PetriNetEdge ed : transition.getIncoming()) {
            //System.out.println("Token in "+ed.myString());
            PetriNetPlace fr = (PetriNetPlace) ed.getFrom();
            if (currentMarking.contains(fr)) {
                newMarking.remove(fr); // remove marked pre places
                ownMarked.addAll(fr.getOwners());
                optN = ed.getOptionNum();
                for (PetriNetEdge edo : transition.getOutgoing()) {
                    //System.out.println("Token out "+edo.myString());
                    if (((PetriNetPlace) edo.getTo()).getOwners().equals(fr.getOwners()) &&
                        edo.getOptionNum().equals(optN)) {
                        newMarking.add((PetriNetPlace) edo.getTo());
                    }
                }
            }

        }

        //System.out.println("newMarking " + newMarking.stream().map(x -> x.getId() + " ").collect(Collectors.joining()));
        return newMarking;
    }
/*  Store code when optionNum did not exist
    public static Multiset<PetriNetPlace>  newMarking(Multiset<PetriNetPlace> currentMarking, PetriNetTransition transition) {
        Multiset<PetriNetPlace> newMarking = HashMultiset.create(currentMarking);
        //System.out.println("newMarking1 "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
        //System.out.println("newMarking1 "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
        // Clear out the places in the current marking which are moving token

        // get the owners+ optionNum of the optional Places not marked
        Multiset<PetriNetPlace> placesOptionalUnMarked = HashMultiset.create(transition.pre());
        placesOptionalUnMarked.removeAll(currentMarking);
        Set<String> ownPrePlaceUnMarker = placesOptionalUnMarked.stream()
            .map(x -> x.getOwners())
            .flatMap(x -> x.stream())
            .collect(Collectors.toSet());
        //System.out.println("  opOwn "+opOwn);
        for (PetriNetPlace pl : transition.pre()) {  // includes optional preplaces
            newMarking.remove(pl);
        }
        //System.out.println("newMarking "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));

        // filter out the post places of owners with  unmarked preplaces

        //System.out.println("outgoing "+transition.getOutgoing().size());
        //System.out.println("newMarking1 "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
        newMarking.addAll(transition.getOutgoing().stream()
            .filter(ed -> !((ed.getOptional() &&
                ownPrePlaceUnMarker.containsAll(((PetriNetPlace) ed.getTo()).getOwners()))))    //
            .map(PetriNetEdge::getTo)
            .map(PetriNetPlace.class::cast)
            .collect(Collectors.toList()));

        return newMarking;
    }
*/
/*
  For a given marking return the set of satisfied transitions
  Used both in the Token Rule and interactivly
 */

    public static Set<PetriNetTransition> satisfiedTransitions(Multiset<PetriNetPlace> currentMarking) {
        Set<PetriNetTransition> out = post(currentMarking).stream() //88
            //  .filter(transition -> currentMarking.containsAll(transition.pre()))
            .filter(tr -> (!tr.getLabel().equals(Constant.DEADLOCK)))
            .filter(transition -> currentMarking.containsAll(transition.preNotOptional())) // drops the optional preplaces
            .distinct()
            .collect(Collectors.toSet());
        //System.out.println(out.stream().map(x->x.getId()).reduce("satisfied ",(x,y)->x+y+" "));
        return out;
    }

    public static boolean allSatTransitions(PetriNetTransition tr, Multiset<PetriNetPlace> currentMarking) {
        return currentMarking.containsAll(tr.pre());
        //System.out.println(out.stream().map(x->x.getId()).reduce("satisfied ",(x,y)->x+y+" "));
    }

    public static boolean isSatisfied(Multiset<PetriNetPlace> currentMarking, PetriNetTransition tr) {

        return currentMarking.containsAll(tr.preNotOptional());

    }

    private static Set<PetriNetTransition> post(Multiset<PetriNetPlace> currentMarking) {
        if (currentMarking == null) return Collections.EMPTY_SET;
        Set<PetriNetTransition> out = new HashSet<>();
        for (PetriNetPlace pl : currentMarking) {
            //System.out.println(pl);
            //System.out.println(pl.post());
            if (pl.post() != null && pl.post().size() > 0)
                out.addAll(pl.post().stream().collect(Collectors.toSet()));
        }
        if (out == null) return Collections.EMPTY_SET;
        else return out;
    }

    /*
        set of prePlace id, label, set of postPlace ids
     */

}
