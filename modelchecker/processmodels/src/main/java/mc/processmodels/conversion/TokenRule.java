package mc.processmodels.conversion;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
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
 * Synchroanised send-receive transitions are labeled b! and the edges added reprenting synchronising b? event
 * are marked as "optional"
 * Thus Let *t = {1,2} where edge 2->t is optional and  t* = {3,4} where edge t->4 is optional behaves:
 * If {1,2} is marked then after t is fired {3,4} is marked.
 * Elseif {1} is marked then after t is fired {3} is marked.
 * Two transitions can NOT have same name and (non-optional pre and post)
 *    As a simple sequential process they would be indetical.
 *    Parallel composition never adds new Transition it just adds edges to existing transitions
 *
 * For a!  || a?->.. -a?->  optionNum on Edge distinguish listeners of single owner
 * The token on an  optional place must move to the place with the same owner and optionNum
 *
 * The Owners Rule uses marked owners to build the bridges used in the  clumping algorithm
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
        //PetrinetReachability.removeUnreachableStates(cFrom);
        Petrinet convertFrom = cFrom.copy();
        Step.reset();
        Automaton outputAutomaton = new Automaton(convertFrom.getId(), false);
        //System.out.println("TOKEN RULE  STARTING \n    " + convertFrom.myString());
        MyAssert.validate(convertFrom, "Token Rule precondition ");
        //assert convertFrom.validatePNet("GOT YOU"): "Token Rule precondition";
        Map<String, Set<Step>> maxSteps = new TreeMap<>();
        Map<String, Set<AutomatonEdge>> step2AutEdge = new TreeMap<>();
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
            //System.out.println("toDo size " + toDo.size());
            if (j++ > stateSizeBound) {
                //System.out.println("\nTokenRule Failure " + convertFrom.getId() + "\nLooping = " + j + "\n");
                convertFrom.validatePNet();
                outputAutomaton.validateAutomaton();
                //System.out.println("TokenRule Failure from " + convertFrom.myString() + "tf tf tf \n");
                throw new CompilationException(convertFrom.getClass(), "Token Rule Failure exceeds size bound " + stateSizeBound);

            } // second LofC  NEVER Called - looks redundent!
            Multiset<PetriNetPlace> currentMarking = toDo.pop();
            //System.out.println("currentMarking "+currentMarking.stream().map(x->x.getId()+", ").collect(Collectors.joining()));

            //System.out.print("\nStarting  prev " +previouslyVisitedPlaces.size()+" todo "+toDo.size()+  " \n");

            //System.out.println(previouslyVisitedPlaces.stream().map(mk->Petrinet.marking2String(mk)+" | ").collect(Collectors.joining()));
            if (previouslyVisitedPlaces.contains(currentMarking)) {
                //System.out.println("Visted!");
                continue;
            }

            //System.out.println("currentMarking "+currentMarking.stream().map(x->x.getId()+", ").collect(Collectors.joining()));
            Set<PetriNetTransition> satisfiedPostTransitions = TokenRulePureFunctions. satisfiedTransitions(currentMarking); //88
            //System.out.println("Processing Marking " + Petrinet.marking2String(currentMarking) +
             //   " with " + satisfiedPostTransitions.size() + " satisfied transitions  " + satisfiedPostTransitions.stream().map(tr -> tr.getId() + "-" + tr.getLabel()+", ").collect(Collectors.joining()));
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
                //System.out.println("  Satisfied trasnition " + transition.myString());
                // One transition MUST be visited more than once - different steps
                String transitionId = transition.getId();

                //System.out.println("Processing "+transitionId+" "+transition.getLabel());
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

                List<Multiset<PetriNetPlace>> newMarkings = TokenRulePureFunctions.newMarking(currentMarking, transition);

                //    List<  Multiset<PetriNetPlace>> newMarkings = HashMultiset.create(newMarking(currentMarking, transition));
/* Multiple new markings are very rare -
         non blocking send with nondeterministic listening
   They are equivanemt to multiple transitions
*/
                //System.out.println("#markings " + newMarkings.size());

                for (Multiset<PetriNetPlace> newMarking : newMarkings) {
                    // Multiset<PetriNetPlace> newMarking = newMarkings.get(0);
                    //System.out.println(" next newMarking " + newMarking.stream().map(x -> x.getId() + " ").collect(Collectors.joining()));

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
                                //System.out.println("   Add Marking to toDo " + newMarking.stream().map(x -> x.getId() + " ").collect(Collectors.joining()));
                             /*   if ((!transition.getLabel().endsWith(Constant.BROADCASTSoutput)) &&
                                    newMarking.size() > ownCnt) {
                                    //System.out.println("Token Rule Makring not 1 Safe = " + convertFrom.getOwners() + " " + cFrom.getId());
                                    //System.out.println("           newMarking " + newMarking.stream().map(x -> x.getId() + " ").collect(Collectors.joining()));
                                    //   throw new CompilationException(convertFrom.getClass(), "Token Rule Makring to large ");
                                } //+ newMarking.toString());
                                */
                            }
                        }
                    }

                    AutomatonEdge ed = null;
                    // this transition will be processed by OwnersRule but may have optional owners dstr Sept 2019
                    // optional will be reset below
                    if (transition.getOptionalOwners().size() > 0) {
                        ed = outputAutomaton.addEdge(transition.getLabel(), markingToNodeMap.get(currentMarking),
                            markingToNodeMap.get(newMarking), null,
                            transition.getOptionalOwners(), false);
                    } else {
                        ed = outputAutomaton.addEdge(transition.getLabel(), markingToNodeMap.get(currentMarking),
                            markingToNodeMap.get(newMarking), null,
                            false, false);
                    }
                //System.out.println("X ADDED "+ed.myString());
               //System.out.println("X TO    "+outputAutomaton.myString());
                    Set<String> own = transition.getOwners();
                    ed.setEdgeOwners(own);

                    if (transition.getLabel().endsWith(Constant.BROADCASTSoutput)) {

                        ed.setFromTran(transitionId);
                        ed.setMarkedOwners(getMarkedOwners(currentMarking, transition));
                        ed.setOptionalOwners(transition.getOptionalOwners());
                        // Can only be decided later
                       // if (ed.getEdgeOwners().size()> ed.getMarkedOwners().size()) ed.setNotMaximalOwnedEdge(true);
                       // else ed.setNotMaximalOwnedEdge(false);
                        //System.out.println("==ed "+ed.myString());

                    } else {
                        ed.setNotMaximalOwnedEdge(false);
                        ed.setFromTran("");
                    }
                    //System.out.println("Token building " + ed.myString());

                    //System.out.println("ed =3 "+ed.myString());

                    //System.out.println("Finished "+transitionId+" lab " + transition.getLabel() + " pre " + transition.pre().stream().map(x->x.getId()+" ").collect(Collectors.joining())+ " END \n");

                    //System.out.println("  End of Marking " + currentMarking.stream().map(nd -> nd.getId() + ", ").collect(Collectors.joining()));
                } //End of multiple new markings (Bcast nondet ?  only)
                //System.out.println("End of newMarkings " + newMarkings.size());
            } // END of satisfiedTransitions processing


            //System.out.println("currentMarking2 "+currentMarking.stream().map(x->x.getId()+", ").collect(Collectors.joining()));

            if (!previouslyVisitedPlaces.contains(currentMarking)) {
                previouslyVisitedPlaces.add(currentMarking);
                //System.out.println("adding to Visited "+currentMarking.stream().map(nd->nd.getId()+", ").collect(Collectors.joining()));
            }
            //System.out.println("loop end todo size "+toDo.size());
            //System.out.println("Add to Previous "+previouslyVisitedPlaces.size()+"  "+currentMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));

        }

        //System.out.println("Token Rule tidy " + outputAutomaton.myString());

        //Built  Automata now set End list to be the same as the Petri EndList
        //Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNodeMap

        for (
            Set<String> mark : convertFrom.getEnds()) {
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
        for (
            AutomatonNode nd : outputAutomaton.getNodes()) {
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
        //System.out.println("Token Rule END " + convertFrom.myString());
        //System.out.println("Token Rule END " + outputAutomaton.myString());
        return outputAutomaton;

    }





    public static Set<String> getMarkedOwners(Multiset<PetriNetPlace> currentMarking, PetriNetTransition transition) {
        //System.out.println("==getMarkedOwners currentMarking " + currentMarking.stream().map(x -> x.getId() + " ").collect(Collectors.joining())+
        //        " \ntran "+ transition.myString());
        /* get the owners+ optionNum of the marked Places */
        Set<String> ownMarked = new TreeSet<>();
        for (PetriNetEdge ed : transition.getIncoming()) {
            //System.out.println("  Token in "+ed.myString());
            PetriNetPlace fr = (PetriNetPlace) ed.getFrom();
            //System.out.println("  getMarkedOwners "+fr.getId()+ " "+ fr.getOwners());
            if (currentMarking.contains(fr)) {
                ownMarked.addAll(fr.getOwners());
                //System.out.println("  adding "+fr.getOwners());
            }

        }

        //System.out.println("==getMarkedOwners " + ownMarked);
        return ownMarked;
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

    public static boolean isSatisfied(Multiset<PetriNetPlace> currentMarking, PetriNetTransition tr) {

        return currentMarking.containsAll(tr.preNotOptional());

    }
    /*
        set of prePlace id, label, set of postPlace ids
     */
    public static class Place2OptNums {
        /* string must be unique String of set of owns*/
        Map<String, Set<Integer>> pl2ns = new TreeMap<>();

        public Place2OptNums() {
        }

        public String myString() {
            return pl2ns.keySet().stream().map(x -> x + "->" + pl2ns.get(x) + ",").collect(Collectors.joining());

        }

        public boolean contains(String o) {
            return pl2ns.containsKey(o);
        }

        public void add(String o, Integer optNum) {
            if (pl2ns.containsKey(o)) {
                pl2ns.get(o).add(optNum);
            } else {
                Set<Integer> nums = new TreeSet<>();
                nums.add(optNum);
                pl2ns.put(o, nums);
            }
        }

        public Set<Integer> getOptNums(String own) {
            return pl2ns.get(own);
        }

        public List<Map<String, Integer>> place2ints() {
            List<Map<String, Integer>> out = new ArrayList<>();
            List<Map<String, Integer>> dummy = new ArrayList<>();
            boolean first = true;
            for (String pl : pl2ns.keySet()) {
                for (Integer i : pl2ns.get(pl)) {
                    if (first) {
                        Map<String, Integer> msi = new TreeMap<>();
                        msi.put(pl, i);
                        out.add(msi);
                    } else {
                        out.clear();
                        dummy.stream().forEach(x -> {
                            x.put(pl, i);
                            out.add(x);
                        });
                    }

                }
                dummy.clear();
                out.stream().forEach(x -> dummy.add(x));
                first = false;
            }
            //System.out.println("return places2Ints "+ out);
            return out;
        }


    }

}
