package mc.processmodels.conversion;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import mc.Constant;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

import java.util.*;
import java.util.stream.Collectors;

public class TokenRulePureFunctions {

    /*
                NO GLOBAL STATE WHAT SO EVER
     */

    /*
      For a given marking and satisfied transition build next marking
      Used both in the Token Rule and interactivly
    For a!  || a?->.. -a?->  need optionNum to distinguish listeners of single owner
            Multiset<PetriNetPlace> mkm = HashMultiset.create(mk);
     */
    public static List<Set<PetriNetPlace>> newMarking(Set<PetriNetPlace> currentMarking, PetriNetTransition transition) {
        Multiset<PetriNetPlace> mcM = TreeMultiset.create(currentMarking);
        List<Multiset<PetriNetPlace>> outMarkings = newMarking(mcM, transition);
        List<Set<PetriNetPlace>> outs = new ArrayList<>();
        for (Multiset<PetriNetPlace> out : outMarkings) {
            outs.add(out.elementSet());
        }

        return outs;
    }

    /*  Multiset Marking would be good for modeling event Refinement BUT
       the complexity they introduce for braodcast processes is OVERWHELMING!
       So do not go there.
      transition input MUST be ENABLED
      Build the new marking after executing THE transition from THE marking
      HandShake there is only one newMarking
      Broadcast
      Note a transition of a one safe Petri Net
         can have two optional edges that may originate from the same Place
    */
    public static List<Multiset<PetriNetPlace>> newMarking(Multiset<PetriNetPlace> currentMarking, PetriNetTransition transition) {
        //System.out.println("Transition "+ transition.myString());
        //System.out.println("With >>currentMarking " + currentMarking.stream().map(nd -> nd.getId() + ", ").collect(Collectors.joining()) + "\n  START tr " + transition.myString());
        List<Multiset<PetriNetPlace>> newMarkings = new ArrayList<>();
        //System.out.println("\ncurrentMarking " + currentMarking.stream().map(x -> x.getId() + " ").collect(Collectors.joining()));
        //System.out.println("Transition " + transition.myString() + "\n   currentMarking " + currentMarking.stream().map(x -> x.getId() + " ").collect(Collectors.joining()));
        //System.out.println("newMarking1 "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
        // Clear out the places in the current marking which are moving token

        /*   A) FOR deterministic processes
            One transition  may have optional edges to many slots in many owners
           thus may be executed in many distinct steps
         *   for each premarking a transition generates one Step  and one automata edge
         *
         *   B)  FOR processes with non deterministic listening
             One Place may have two edges, to the same transition, that only differ in their OptNum
         *   If such a place is marked which edge is used is chosen non deterministicly
         *   - there are as many steps for a givenmarking as there are edges.
         * */
        boolean processed = false;
        if (transition.getLabel().endsWith(Constant.BROADCASTSoutput)) {
            TreeSet<String> ownMarked = new TreeSet<>();
            TokenRule.Place2OptNums pl2nums = new TokenRule.Place2OptNums();
            // First process the optional edges
            /* get the owners+ optionNum of the marked Places */
            for (PetriNetEdge ed : transition.getIncoming()) {
                /* for each place only one  option number can be moved*/
                if (ed.getOptional()) {
                    pl2nums.add(((PetriNetPlace) ed.getFrom()).getId(), ed.getOptionNum());
                }
            }

            List<Map<String, Integer>> pl2n = pl2nums.place2ints();

            if (pl2n.size() > 0) {
                processed = true;
                //System.out.println("pl2nums " + pl2nums.myString());
                //System.out.println("pl2n " + pl2n);
                for (Map<String, Integer> pl2i : pl2n) {
                    Multiset<PetriNetPlace> newMarking = HashMultiset.create(currentMarking);

                    // at place fr only optNumber i  is to be performed
                    //System.out.println("pl2i " + pl2i);
                    for (PetriNetEdge ed : transition.getIncoming()) {
                        PetriNetPlace fr = (PetriNetPlace) ed.getFrom();
                        Integer optN = pl2i.get(fr.getId());
                        if (ed.getOptional()&& ed.getOptionNum().equals(optN)) {
                            String own = fr.getOwnersUid();
                            //System.out.println("from " + fr.getId() + " own " + own);
                            if (currentMarking.contains(fr)) {
                                ownMarked.addAll(fr.getOwners());
                                for (PetriNetEdge edo : transition.getOutgoing()) {

                                    if (edo.getOptional()) {
                                        //loop around optionNums
                                        //System.out.println(" edge out " + edo.myString());
                                        if (((PetriNetPlace) edo.getTo()).getOwnersUid().equals(own) &&
                                            edo.getOptionNum().equals(optN)) {
                                            newMarking.add((PetriNetPlace) edo.getTo());
                                            //System.out.println("   adding " + edo.getTo().getId());
                                        }
                                    }
                                }

                                newMarking.remove(fr); // remove marked pre places fr if it exists
                            }
                        }
                        // only enabled transitions
                        //if currentMarking does not contain fr  must be optional so forget
                    }
                    //System.out.println("  newMarking1/2 " + newMarking.stream().map(nd -> nd.getId() + ", ").collect(Collectors.joining()));
                    // Now process the not optional edges
                    for (PetriNetEdge ed : transition.getIncoming()) {
                        if (!ed.getOptional()) {
                            //System.out.println("not opt ed "+ed.myString());
                            PetriNetPlace fr = (PetriNetPlace) ed.getFrom();
                            if (currentMarking.contains(fr)) {
                                newMarking.remove(fr); // remove marked pre places
                                //System.out.println("Remove not opt "+fr.getId());
                            }
                        }
                    }
                    //System.out.println(transition.myString());
                    for (PetriNetEdge edo : transition.getOutgoing()) {
                        //System.out.println("Token out "+edo.myString());
                        if (!edo.getOptional()) {
                            newMarking.add((PetriNetPlace) edo.getTo());

                        }
                    }
                    //System.out.println("Final Marking "+newMarking.stream().map(x -> x.getId() + " ").collect(Collectors.joining()));
                    if (!newMarkings.contains(newMarking))
                        newMarkings.add(newMarking);
                }
            }
        }
        if (!processed) {  // Both not broadcast and broadcast with no optional edges
            // Process the not optional edges
            //System.out.println("NOT BROADCAST");
            Multiset<PetriNetPlace> newMarking = HashMultiset.create(currentMarking);

            for (PetriNetEdge ed : transition.getIncoming()) {
                if (!ed.getOptional()) {
                    //System.out.println("not opt ed "+ed.myString());
                    PetriNetPlace fr = (PetriNetPlace) ed.getFrom();
                    if (currentMarking.contains(fr)) {
                        newMarking.remove(fr); // remove marked pre places
                        //System.out.println("Remove not opt "+fr.getId());
                    }
                }
            }
            for (PetriNetEdge edo : transition.getOutgoing()) {
                if (!edo.getOptional()) {
                    //System.out.println("Token out "+edo.myString());
                    newMarking.add((PetriNetPlace) edo.getTo());

                }
            }
            //System.out.println("Final Marking "+newMarking.stream().map(x -> x.getId() + " ").collect(Collectors.joining()));
            if (!newMarkings.contains(newMarking))
                newMarkings.add(newMarking);
        }


        //System.out.println("Transition " + transition.myString() + "\n   newMarkings " + newMarkings.stream().
        //    map(m -> m.stream().map(x -> x.getId() + " ").collect(Collectors.joining()) + "; ").collect(Collectors.joining()));
        return newMarkings;
    }

    /*
  For a given marking return the set of satisfied transitions
  Used both in the Token Rule and interactivly
 */

    public static Set<PetriNetTransition> satisfiedTransitions(Multiset<PetriNetPlace> currentMarking) {
        Set<PetriNetTransition> out = post(currentMarking).stream() //88
            //  .filter(transition -> currentMarking.containsAll(transition.pre()))
            .filter(tr -> (!tr.getLabel().equals(Constant.DEADLOCK)))
            .filter(transition -> (currentMarking.containsAll(transition.preNotOptional()))
                //  ) // broadcast input only needs one place marked
            ).distinct()
            .collect(Collectors.toSet());
        //System.out.println("satisfied \n" + out.stream().map(x -> x.myString() + "\n ").collect(Collectors.joining()));



        return out;
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


}
