package mc.operations.functions;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.microsoft.z3.Context;
import mc.Constant;
import mc.compiler.Guard;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.conversion.OwnersRule;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
import mc.util.expr.Expression;
import mc.util.expr.MyAssert;

import java.util.*;
import java.util.stream.Collectors;

public class PetriNetAbstraction implements IProcessFunction {

    /**
     * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)})
     *
     * @return the name of the function
     */
    @Override
    public String getFunctionName() {
        return "petrinetabs";
    }

    /**
     * Get the available flags for the function described by this interface (e.g. {@code unfair} in
     * {@code abs{unfair}(A)}
     *
     * @return a collection of available flags (note, no variables may be flags)
     */
    @Override
    public Collection<String> getValidFlags() {
        return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR,
            Constant.CONGURENT, Constant.OWNED,
            Constant.SEQUENTIAL, Constant.FORCED,
            Constant.CONCURRENT);
    }

    /**
     * Gets the number of automata to parse into the function.
     *
     * @return the number of arguments
     */
    @Override
    public int getNumberArguments() {
        return 1;
    }



    /**
     NOT TO BE USED
     */
    @Override
    public Automaton compose(String id, Set<String> flags,
                             Context context, Automaton... automata) throws CompilationException {

        //System.out.println("abs end "+abstraction.myString());
        return new Automaton("FAKEabstraction",false);
    }





    /* Pruning for Petri Nets
       Once a hidden transition tr, satisfying our criteria, is found it can be pruned.
       To prune the pre and post markings are glued together and the transition removed.
       Only then can another transition be searched for and pruned.
         Criteria no transition with overlaping preplaces can be enabled by any marking!
         Shorthand no transition with overlpping preplaces.

       In Interpretor multiple PlaceGluings might occur then only one OwnerGluing.

       Here multiple transition hidings might be needed but all for different but
       may be overlapping Owners!
       Let pre-tr and post-tr both have two places with owners o1 and o2
       Gluing pre-tr, post-tr gives both o1 = o1^o2 o1^o1 and  o1 = o2^o1 and o1^o1
       What would two gluings on the same set of owners return?

     */
    private Petrinet pruneHiddenNodes(Context context, Petrinet pNet, boolean cong)
        throws CompilationException {
        Petrinet pOut = pNet.copy();
        pOut.getTransitions().values().stream().filter(x -> x.isHidden()).forEach(x -> x.myString());
        for (PetriNetTransition tr : pOut.getTransitions().values()) {

        }

        return pOut;
    }




 /*
 if (edge.isHidden() &&
          abstraction.getEdge(Constant.HIDDEN, to, from) == null) {
          //System.out.println("combining "+ edge.getTo()+" and "+edge.getFrom());
          abstraction.combineNodes(edge.getFrom(), edge.getTo(), context);
        } else {
  */




    /*

     */
    @Override
    public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {
    /*
    For each tau transition t
      For each *t node n
         For each tr . n in tr*
           add tr;t
      Symetric
      remove t
     */
        //System.out.println("\n\nPetri abstraction! flags "+flags+ "\n");

        assert petrinets.length == 1;
        Petrinet petri = petrinets[0].reId("");
        //System.out.println("Start "+petri.myString());
        List<PetriNetTransition> hidden = petri.getTransitions().values().stream().
            filter(x -> x.getLabel().equals(Constant.HIDDEN)).collect(Collectors.toList());
        while (!hidden.isEmpty()) {
            hidetaus(hidden, petri, flags);
        }
        hidden = petri.getTransitions().values().stream().
            filter(x -> x.getLabel().equals(Constant.HIDDEN)).collect(Collectors.toList());

        petri.rebuildAlphabet();
        //System.out.println("Petri abs returns "+petri.myString());
        return petri;
    }

  /*
  hiding of ALL existing TAU TRANSITION
  hiding in one safe nets results in unreachable n-safe transitions!
  hiding of n-safe nets!
   */

    private void hidetaus(List<PetriNetTransition> hidden, Petrinet petri, Set<String> flags) throws CompilationException {
        //System.out.println("\nhiding "+hidden.size());
        boolean cong = flags.contains(Constant.CONGURENT);
        List<PetriNetTransition> next = new ArrayList<>();
        //adding -a->*-tau->
        for (PetriNetTransition t2 : hidden) {    // hide each existing tau (may add new taus for later hiding
            Set<String> original = new TreeSet<>();
            for (String id : petri.getTransitions().keySet()) {
                original.add(id);  //Beware of pointers do not simplify
            } //restrict abstraction to original
            //System.out.println("original "+ original+"\n");

            //System.out.println("START t2 = "+t2.myString());
            for (PetriNetPlace pretau : t2.pre()) {
                for (PetriNetTransition tr1 : pretau.pre()) {    //PRE transition
                    if (tr1.getId().equals(t2.getId())) continue;
                    if (!original.contains(tr1.getId())) continue;
                    //System.out.println("   tr1 = "+tr1.getId());
                    Set<PetriNetPlace> newpre = t2.pre();  //ORDER critical
                    newpre.removeAll(tr1.post());
                    if (newpre.removeAll(tr1.pre())) continue; //overlap implies newpre a multiset
                    newpre.addAll(tr1.pre());

                    Set<PetriNetPlace> newpost = tr1.post(); //ORDER critical
                    newpost.removeAll(t2.pre());
                    if (newpost.removeAll(t2.post())) continue;
                    newpost.addAll(t2.post());

                    if (petri.tranExists(newpre, newpost, tr1.getLabel())) {
                        continue;
                    }
                    PetriNetTransition newtr = petri.addTransition(tr1.getLabel());

                    //System.out.println("  newtr "+newtr.myString());

                    newtr.setOwners(tr1.getOwners());
                    for (PetriNetPlace pl : newpre) {
                        //System.out.println("edge "+pl.getId()+"<-"+tr.getLabel()+"-"+newtr.getId());
                        petri.addEdge(newtr, pl, false); // BEWARE NOT for broadcast
                    }
                    for (PetriNetPlace pl : newpost) {
                        //if (!pl.getId().equals(pretau.getId())) {
                        petri.addEdge(pl, newtr, false); // BEWARE NOT for broadcast
                        //System.out.println("edge "+ newtr.getId()+"<-"+tr.getLabel()+"-"+pl.getId());
                        //}
                    }
                    if (newtr.getLabel().equals(Constant.HIDDEN)) {
                        if (!next.contains(newtr)) next.add(newtr);
                    }
                    //System.out.println("  ADDed-pre "+newtr.myString());
                }
            }

            //  }
            //adding -tau->*-a->
            //System.out.println("POST "+original);
            //  for(PetriNetTransition t2: hidden) {
            //System.out.println("Post "+t2.getId());
            for (PetriNetPlace posttau : t2.post()) {
                for (PetriNetTransition tr2 : posttau.post()) {
                    if (tr2.getId().equals(t2.getId())) continue;
                    if (!original.contains(tr2.getId())) continue; //do not abstract against recently add transitions
                    //System.out.println("  tr2 "+tr2.getId());
                    Set<PetriNetPlace> newpre = tr2.pre();    //ORDER critical
                    newpre.removeAll(t2.post());
                    if (newpre.removeAll(t2.pre())) continue;
                    newpre.addAll(t2.pre());

                    Set<PetriNetPlace> newpost = t2.post();    //ORDER critical
                    newpost.removeAll(tr2.pre());
                    if (newpost.removeAll(tr2.post())) continue;
                    newpost.addAll(tr2.post());

                    if (petri.tranExists(newpre, newpost, tr2.getLabel())) continue;
                    //System.out.println("    "+tr.myString());
                    PetriNetTransition newtr = petri.addTransition(tr2.getLabel());
                    //System.out.println("newtr "+newtr.myString());

                    newtr.setOwners(tr2.getOwners());
                    for (PetriNetPlace pl : newpost) {
                        petri.addEdge(pl, newtr, false); // BEWARE NOT for broadcast
                        //System.out.println("edge "+ newtr.getId()+"<-"+tr.getLabel()+"-"+pl.getId());
                    }
                    for (PetriNetPlace pl : newpre) {
                        // if (!pl.getId().equals(posttau.getId())) {
                        petri.addEdge(newtr, pl, false); // BEWARE NOT for broadcast
                        //System.out.println("edge "+pl.getId()+"<-"+tr.getLabel()+"-"+newtr.getId());
                        // }
                    }
                    if (newtr.getLabel().equals(Constant.HIDDEN)) {
                        if (!next.contains(newtr)) next.add(newtr);
                    }
                    //System.out.println("  ADDed-post "+newtr.myString());
                }
            }

            // }
            //adding n-a->*  for loop n-tau->* and *-a->n
            // for(PetriNetTransition t1: hidden) {
            //System.out.println("Loop "+t2.getId());
            for (PetriNetPlace posttau : t2.post()) {
                for (PetriNetTransition tr2 : posttau.post()) {
                    //System.out.println("LOOP "+t1.myString()+ " "+tr2.myString());
                    if (tr2.getId().equals(t2.getId())) continue;
                    Set<PetriNetPlace> overlap = new HashSet<>();
                    overlap.addAll(t2.pre());
                    overlap.retainAll(tr2.post());
                    //System.out.println("overlap "+overlap.size());
                    if (overlap.size() != t2.pre().size()) continue;  // only add loop taus if pre=post
                    if (!original.contains(t2.getId())) continue;
                    if (!original.contains(tr2.getId())) continue; //do not abstract against recently add transitions
                    //System.out.println("  tr2 "+tr2.myString());
                    Set<PetriNetPlace> newpre = tr2.post();
                    Set<PetriNetPlace> newpost = tr2.pre();

                    //System.out.println(petri.myString());
                    if (petri.tranExists(newpre, newpost, tr2.getLabel())) continue;
                    //System.out.println("  **  ");
                    PetriNetTransition newtr = petri.addTransition(tr2.getLabel());
                    //System.out.println("newtr "+newtr.myString());

                    newtr.setOwners(tr2.getOwners());
                    for (PetriNetPlace pl : newpost) {
                        petri.addEdge(pl, newtr, false); // BEWARE NOT for broadcast
                        //System.out.println("edge "+ newtr.getId()+"<-"+tr.getLabel()+"-"+pl.getId());
                    }
                    for (PetriNetPlace pl : newpre) {
                        // if (!pl.getId().equals(posttau.getId())) {
                        petri.addEdge(newtr, pl, false); // BEWARE NOT for broadcast
                        //System.out.println("edge "+pl.getId()+"<-"+tr.getLabel()+"-"+newtr.getId());
                        // }
                    }
                    if (newtr.getLabel().equals(Constant.HIDDEN)) {
                        if (!next.contains(newtr)) next.add(newtr);
                    }
                    //System.out.println("ADDed-loop "+newtr.myString());
                }
            }

        }
        for (PetriNetTransition tr : hidden) {
            if (!cong ||
                (Petrinet.isMarkingExternal(tr.pre()) == Petrinet.isMarkingExternal(tr.post()))) {
                //System.out.println("Removing "+tr.myString());
                petri.removeTransition(tr);
            }
        }
        hidden.clear();
        for (PetriNetTransition tr : next) {
            //System.out.println("  Adding "+ tr.myString());
            hidden.add(tr);
        }
        //System.out.println("hidetaus returns "+hidden.size()+" in "+petri.getId());
        return;
    }

    @Override
    public MultiProcessModel compose(String id, Set<String> flags, Context context, MultiProcessModel... multiProcess) throws CompilationException {
        return null;
    }


}

