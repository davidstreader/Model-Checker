package mc.operations.functions;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.microsoft.z3.Context;

import java.sql.SQLOutput;
import java.util.*;
import java.util.stream.Collectors;

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


public class AbstractionFunction implements IProcessFunction {

  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)})
   *
   * @return the name of the function
   */
  @Override
  public String getFunctionName() {
    return "abs";
  }

  /**
   * Get the available flags for the function described by this interface (e.g. {@code unfair} in
   * {@code abs{unfair}(A)}
   *
   * @return a collection of available flags (note, no variables may be flags)
   */
  @Override
  public Collection<String> getValidFlags() {
    return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR, Constant.CONGURENT);
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

  private Multimap<AutomatonNode,AutomatonNode> buildSCCgraph(Automaton ain) {
    Multimap<AutomatonNode,AutomatonNode> graph = ArrayListMultimap.create();
    for (AutomatonEdge ed: ain.getEdges()){
      if (ed.getLabel().equals(Constant.HIDDEN)){
        graph.put(ed.getFrom(),ed.getTo());
      }
    }
    return graph;
  }
  /**
   * Execute the abstraction on automata.  tau loops in input expected and removed
   * CCS style keep n-tau->m where TauEnd(m)
   * TauEnd(x) <=> x-/-tau-> OR
   * x-tau->y and y in TauLoop and all n in TauLoop n-a->m => n-a->m in TauLoop
   * <p>
   * Abstraction for cong ==> keep taus that bridge the gap between internal and external nodes
   * not cong ==> remove all taus
   * use mutiple roots and remove bridging taus but only in pruning nodes
   * Both need to upgrade all process relations)
   *
   * It would be desirable to perform observational simplification on the UNsaturted automata
   * abstraction both removes nodes + saturates the automata
   * for simplicity only 2 cycles are computed  hence saturation may introduce 2 cycles and
   *  a second round of removing 2 cycles is performed
   *  This makes it dificult to lift the node removal to the unsturated automata
   *
   * @param id       the id of the resulting automaton
   * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context  the z3 context to access the stuff
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   */
  @Override
  public Automaton compose(String id, Set<String> flags,
                           Context context, Automaton... automata) throws CompilationException {
    if (automata.length != getNumberArguments()) {
      throw new CompilationException(this.getClass(), null);
    }
    Automaton startA = automata[0].copy(); //Deep Clone
//int nodeLable
    boolean isFair = flags.contains(Constant.FAIR) || !flags.contains(Constant.UNFAIR);
    boolean cong = flags.contains(Constant.CONGURENT);
    System.out.println("\n***ABSTRACTION flags " + flags+ "\n"+ startA.myString());
    //System.out.println("\n\nautomata Abs start "+ startA.myString()+ " flags "+flags+ " cong "+cong);
    startA.validateAutomaton("ABS 1");
    //reduce the statspace and remove all loops
    Automaton abstraction =  absMerge(flags,context, startA);
    abstraction.validateAutomaton("ABS 2");

    System.out.println("\n******\nABS no DUP no Loop\n" + abstraction.myString()+"\n******\n");

    observationalSemantics(flags, abstraction, context);
    abstraction.validateAutomaton("ABS 3");
    System.out.println("\n*****Abs final \n*****Abs final\n" + abstraction.myString()+"\n");
    return abstraction;
  }

  public Automaton observationalSemantics(Set<String> flags, Automaton abstraction, Context context) throws CompilationException {
    // retrieve the unobservable edges from the specified automaton
    boolean isFair = flags.contains(Constant.FAIR) || !flags.contains(Constant.UNFAIR);
    boolean cong = flags.contains(Constant.CONGURENT);
    List<AutomatonEdge> hiddenEdges = abstraction.getEdges().stream()
      .filter(ed->ed.isHidden() && !ed.getFrom().equalId(ed.getTo()))
      .collect(Collectors.toList());
    //Construct  edges to replace the unobservable edges
    Set<AutomatonEdge> processesed = new HashSet<>();
    while (!hiddenEdges.isEmpty()) {
      AutomatonEdge hiddenEdge = hiddenEdges.get(0);
      System.out.println("**processing " + hiddenEdge.myString());
      if (processesed.contains(hiddenEdge)) {
        //System.out.println("WHY already processed"+ hiddenEdge.myString());
        hiddenEdges.remove(hiddenEdge);
        System.out.println("Skip");
        continue;
      }
      processesed.add(hiddenEdge); // ensures termination
// for congruence keep taus that bridge internal and external nodes
      //System.out.println("hiddenEdge "+hiddenEdge.myString());
      //hiddenEdges.stream().forEach(x-> System.out.println(" in List "+x.myString()));
      hiddenEdges.remove(hiddenEdge);
      if (cong && hiddenEdge.stateObservable()) {
        System.out.println("SKIP");
        continue; //Do not remove these taus
      }
      //System.out.println(abstraction.myString());
      List<AutomatonEdge> temp = new ArrayList<AutomatonEdge>();

      //FALL through only straight taus and no loops in graph!

      try {
        if (hiddenEdge.getTo().isSTOP()) {
          hiddenEdge.getFrom().setStopNode(true);
        } // else hiddenEdge.getFrom().setStopNode(false);
        if (hiddenEdge.getTo().isERROR()) {
          hiddenEdge.getFrom().setErrorNode(true);
        }// else hiddenEdge.getFrom().setErrorNode(false);
        if (hiddenEdge.getFrom().isStartNode()) {
          hiddenEdge.getTo().setStartNode(true);
          //System.out.println("hTo "+hiddenEdge.getTo().getId());
          //System.out.println("r1 "+abstraction.getRoot().stream().map(x->x.getId()+" ").collect(Collectors.joining()));
          abstraction.addRoot(hiddenEdge.getTo());
          System.out.println("r2 " + abstraction.getRoot().stream().map(x -> x.getId() + " ").collect(Collectors.joining()));
        }
        //System.out.println("tau = " + hiddenEdge.myString());
        //System.out.println("tauTo   = " + hiddenEdge.getTo().myString());
        //System.out.println("tauFrom = " + hiddenEdge.getFrom().myString());
        //abstraction is both In and OUT
        temp.addAll(
          constructOutgoingEdges(abstraction, hiddenEdge, context));
        temp.addAll(
          constructIncomingEdges(abstraction, hiddenEdge, context));
        abstraction.removeEdge(hiddenEdge);
        //Add new tau s if not already processed
        boolean skip = false;
        for (AutomatonEdge ed : temp) {
          for (AutomatonEdge edge : processesed) {
            if (ed.getLabel().equals(edge.getLabel())
              && ed.getFrom().equalId(edge.getFrom())
              && ed.getTo().equalId(edge.getTo())) {
              skip = true;
              break;
            }
          }
          if (!skip) hiddenEdges.add(ed);
        }
// Worry might this loop forever ?
      } catch (InterruptedException ignored) {
        throw new CompilationException(this.getClass(), null);
      }
      System.out.println("finished with " + hiddenEdge.myString());
    }
    // 1  loops may have been added.
    divergence(abstraction, isFair);
    System.out.println("hidden cnt "+processesed.size());
    // abstraction might remove some locations
    Set<String> ow = new TreeSet<>();
    for (AutomatonEdge edge : abstraction.getEdges()) {
      ow.addAll(edge.getOwnerLocation());
    }
    abstraction.setOwners(ow);
    abstraction.cleanNodeLables();
    return abstraction;
  }


  public void divergence(Automaton abstraction, boolean isFair) throws CompilationException {
    //System.out.println("Divergence fair "+isFair);
    List<AutomatonEdge> hiddenEdges = abstraction.getEdges().stream()
      .filter(AutomatonEdge::isHidden)
      .collect(Collectors.toList());
    for (AutomatonEdge hiddenEdge : hiddenEdges) {
      if (hiddenEdge.getFrom().equals(hiddenEdge.getTo())) {
        System.out.println("Diverge "+hiddenEdge.myString());
        if (!isFair) {
          System.out.println("unFair");
          AutomatonNode deadlockNode = abstraction.addNode();
          deadlockNode.setErrorNode(true);

          AutomatonEdge added = abstraction.addEdge(Constant.DEADLOCK, hiddenEdge.getFrom(),
            deadlockNode, null, false, false);
          added.setEdgeOwners(hiddenEdge.getOwnerLocation());
        }
        abstraction.removeEdge(hiddenEdge);
        System.out.println("Gone");
      }
    }
  }




  /**
   * This method reduces the state space of the input automata
   * @param flags
   * @param context
   * @param startA input AND output
   * @return
   * @throws CompilationException
   */
  public Automaton absMerge(Set<String> flags, Context context, Automaton startA)
    throws CompilationException {
    boolean isFair = flags.contains(Constant.FAIR) || !flags.contains(Constant.UNFAIR);
    boolean cong = flags.contains(Constant.CONGURENT);
    //Build  tau connected graph
    SCCTarjan sccTarjan = new SCCTarjan();
    Multimap<AutomatonNode,AutomatonNode> graph = buildSCCgraph(startA);
    //Build  strongly tau connected components
    List<List<String>> components = sccTarjan.scc(graph);
    SimpFunction sf = new SimpFunction();
    //merge all strongly tau connected components
    sf.mergeNodes(startA,components, context);
    startA.validateAutomaton("absmergs 1");
    //System.out.println("ABS MERGED " + startA.myString());
    startA.removeDuplicateEdges();
    // 1  loops may have been added AND must be removed before pruning
    divergence(startA, isFair);
    startA.validateAutomaton("absMerge 2");

    //pruning is well defined on failure semantics not bisimulation
    Automaton abstraction = pruneHiddenNodes(context, startA, cong);
    //System.out.println("ABS PRUNED " + abstraction.myString());
    startA.validateAutomaton("abdMerge 3");
    abstraction.removeDuplicateEdges();
    abstraction.setEndFromNodes();
    return abstraction;
  }
  /**
   * @param abstraction automaton
   * @param hiddenEdge  to be removed
   * @param context     Symbolic
   * @return list of new hidden edges
   * @throws CompilationException
   * @throws InterruptedException adds  n-a->m when  n-a->x and x-tau->m to abstraction
   *                              note if n-a->m and m-tau->n  then n-a->n will be added
   *                              and m-a->n is added
   */
  private List<AutomatonEdge> constructOutgoingEdges(Automaton abstraction, AutomatonEdge hiddenEdge,
                                                     Context context)
    throws CompilationException, InterruptedException {
    boolean symbolic = abstraction.isSymbolic();
    Guard hiddenGuard = hiddenEdge.getGuard();
    List<AutomatonEdge> incomingEdges = hiddenEdge.getFrom().getIncomingEdges();
    List<AutomatonEdge> hiddenAdded = new ArrayList<>();
//System.out.println(abstraction.getId()+ " "+ hiddenEdge.getId()+" "+
//             "incoming "+incomingEdges.size());
    AutomatonNode to = hiddenEdge.getTo();
    for (AutomatonEdge edge : incomingEdges) {

      //System.out.println("\tedge "+edge.myString());
      AutomatonNode from = edge.getFrom();

      Guard fromGuard = edge.getGuard();
      //System.out.println("Edge Guard "+ from.getGuard());
      Guard outGuard;

      AutomatonEdge added = null;
      if (symbolic) {
        Guard newAbstractionEdgeGuard;
        if (fromGuard != null && hiddenGuard != null) {
          outGuard = Expression.automataPreConditionHoareLogic(fromGuard, hiddenGuard, context);
        } else if (fromGuard != null) {
          outGuard = fromGuard;
        } else {
          outGuard = hiddenGuard;
        }

        added = abstraction.addEdge(edge.getLabel(), from, to, outGuard, false, edge.getOptionalEdge());
        abstraction.addOwnersToEdge(added, hiddenEdge.getOwnerLocation());
        abstraction.addOwnersToEdge(added, edge.getOwnerLocation());

      } else { // Atomic automaton
        if (edge.isHidden() && from.getId().equals(to.getId())) {
          //System.out.println("ERROR hidden loops should NOT be added!");
          continue;
        }
        //if new edge exists it will not be added by addEdge

        boolean skip = false;
        for (AutomatonEdge ed : abstraction.getEdges()) {
          if (ed.getLabel().equals(edge.getLabel())
            && ed.getFrom().equalId(from)
            && ed.getTo().equalId(to)) {
            skip = true;
            break;
          }
        }
        if (skip) continue;

        added = abstraction.addEdge(edge.getLabel(), from, to, null, false, edge.getOptionalEdge());
        abstraction.addOwnersToEdge(added, hiddenEdge.getOwnerLocation());
        abstraction.addOwnersToEdge(added, edge.getOwnerLocation());
        if (added.isHidden()) { hiddenAdded.add(added);  }
   System.out.println("Out ADDED "+added.myString());
        // n->tau->m m-a->n n-tau->m one tau used twice!
        if (from.getId().equals(to.getId()) && !edge.isHidden()) {
          added = abstraction.addEdge(edge.getLabel(), hiddenEdge.getFrom(), hiddenEdge.getTo(), null, false, edge.getOptionalEdge());
          abstraction.addOwnersToEdge(added, hiddenEdge.getOwnerLocation());
          abstraction.addOwnersToEdge(added, edge.getOwnerLocation());
        }

      }
      //System.out.println("symb = "+symbolic+" a->tau-> added " + added.myString());

      //System.out.println("Outgoing add "+added.myString());
    }

    //System.out.println("endof Outgoing "+hiddenAdded.myString());
    return hiddenAdded;
  }

  /**
   * @param abstraction automaton   IN+OUT
   * @param hiddenEdge  to be removed
   * @param context     to do with symbolic events
   * @return list of new hidden edges
   * @throws CompilationException
   * @throws InterruptedException adds  n-a->m when  n-tau->x and x-a->m  to abstraction
   */
  private List<AutomatonEdge> constructIncomingEdges(Automaton abstraction,
                                                     AutomatonEdge hiddenEdge,
                                                     Context context)
    throws CompilationException, InterruptedException {
    boolean symbolic = abstraction.isSymbolic();
    Guard hiddenGuard = hiddenEdge.getGuard();
    List<AutomatonEdge> outgoingEdges = hiddenEdge.getTo().getOutgoingEdges();
//System.out.println(abstraction.getId()+ " "+ hiddenEdge.getId()+" "+
    //   " outgoing "+outgoingEdges.size());
    List<AutomatonEdge> hiddenAdded = new ArrayList<>();
    AutomatonNode from = hiddenEdge.getFrom();
    for (AutomatonEdge edge : outgoingEdges) {

      //System.out.println("\tedge "+edge.myString());
      AutomatonNode to = edge.getTo();
      Guard toGuard = edge.getGuard();
//.println("Edge Guard "+ edge.getGuard());

      AutomatonEdge added = null;
      if (symbolic) {
        Guard newAbstractionEdgeGuard;
        if (toGuard != null && hiddenGuard != null) {
          newAbstractionEdgeGuard = Expression.automataPreConditionHoareLogic(hiddenGuard, toGuard, context);
        } else if (toGuard != null) {
          newAbstractionEdgeGuard = toGuard;
        } else {
          newAbstractionEdgeGuard = hiddenGuard;
        }
        added = abstraction.addEdge(edge.getLabel(), from, to, newAbstractionEdgeGuard, false, edge.getOptionalEdge());
        abstraction.addOwnersToEdge(added, hiddenEdge.getOwnerLocation());
        abstraction.addOwnersToEdge(added, edge.getOwnerLocation());
      } else {  // Atomic edge
        // if loop do nothing
        if (edge.isHidden() && from.getId().equals(to.getId())) {
          continue;
        }
        // if edge already in automaton  add edge willnot add it!
        boolean skip = false;
        for (AutomatonEdge ed : abstraction.getEdges()) {
          if (ed.getLabel().equals(edge.getLabel())
            && ed.getFrom().equalId(from)
            && ed.getTo().equalId(to)) {
            skip = true;
            break;
          }
        }
        if (skip) continue;

        added = abstraction.addEdge(edge.getLabel(), from, to, null, false, edge.getOptionalEdge());
  System.out.println("In ADDED "+added.myString());

        abstraction.addOwnersToEdge(added, hiddenEdge.getOwnerLocation());
        abstraction.addOwnersToEdge(added, edge.getOwnerLocation());
        added.getOwnerLocation().addAll(hiddenEdge.getOwnerLocation());
        //System.out.println("Incoming add "+added.myString());
        // n->tau->m m-a->n n-tau->m one tau used twice!
        if (added.isHidden()) { hiddenAdded.add(added);}
        if (from.getId().equals(to.getId()) && !edge.isHidden()) {
          added = abstraction.addEdge(edge.getLabel(), hiddenEdge.getFrom(), hiddenEdge.getTo(), null, false, edge.getOptionalEdge());
          abstraction.addOwnersToEdge(added, hiddenEdge.getOwnerLocation());
          abstraction.addOwnersToEdge(added, edge.getOwnerLocation());

        }

      }
      //System.out.println("symb = "+symbolic+" tau->a-> added " + added.myString());

      //System.out.println("Incoming add "+added.myString());
    }
    //String x = hiddenAdded.stream().map(e->e.myString()).collect(Collectors.joining());
    //System.out.println("endof Incoming "+x);

    return hiddenAdded;
  }

  /**
   * ALL -x->nd  ==> x==tau AND all nd-x-> ==> x==tau
   * OR NO -x->nd AND all  nd-x-> ==> x==tau
   * set nm where nd-tau->nm to be Root remove nd-tau->nm
   * remove node nd
   *
   * @param context The structure linking to z3
   * @param autoIN  The automaton to prune
   * @throws CompilationException prunes any Internal node that is only connected by hidden events
   *                              This method solely acts as an accelerator and is only valid for
   *                              Testing / Failure  semamtics
   */

  private Automaton pruneHiddenNodes(Context context, Automaton autoIN, boolean cong)
    throws CompilationException {
    Automaton abstraction = autoIN; //.copy();
//System.out.println("prune "+ abstraction.myString());
    List<AutomatonNode> nodes = abstraction.getNodes();

    for (AutomatonNode n : nodes) {
      boolean del = true;
      for (AutomatonEdge e : Iterables.concat(n.getIncomingEdges(), n.getOutgoingEdges())) {
        if (!e.isHidden() || e.stateObservable()) {
          del = false;
          break;
        }
      }
      if (n.isStartNode() || n.getOutgoingEdges().size() == 0) {
        del = false;
      }
      if (del) {
   System.out.println("PRUNING "+n.myString());
        try {
          for (AutomatonEdge second : n.getOutgoingEdges()) {
            for (AutomatonEdge first : n.getIncomingEdges()) {
              if (first.getFrom().equals(second.getTo())) continue; //do not add 1-loop
              AutomatonEdge ne = abstraction.addEdge(Constant.HIDDEN, first.getFrom(), second.getTo(),
                cbGuards(first, second, context), true, first.getOptionalEdge());
              ne.setEdgeOwners(first.getEdgeOwners());
    System.out.println("Prune adds " + ne.myString());
            }
          }
   System.out.println("Prune del " + n.myString());
          abstraction.removeNode(n);  // tidies up all the edges
        } catch (InterruptedException ignored) {
          throw new CompilationException(this.getClass(), null);
        }
      }
    }
    return abstraction;
  }


  private void mergeSCCNodes(Automaton ain, List<List<AutomatonNode>> components){
    for(List<AutomatonNode> nds: components) {
      if (nds.size()>1){

      }
    }
  }

 /*
 if (edge.isHidden() &&
          abstraction.getEdge(Constant.HIDDEN, to, from) == null) {
          System.out.println("combining "+ edge.getTo()+" and "+edge.getFrom());
          abstraction.combineNodes(edge.getFrom(), edge.getTo(), context);
        } else {
  */







  private Guard cbGuards(AutomatonEdge from, AutomatonEdge to, Context context)

    throws CompilationException, InterruptedException {
    Guard outGuard;
    Guard fromGuard = from.getGuard();
    Guard toGuard = to.getGuard();
    if (context == null) {
      //System.out.println("Context = null");
      return null;
    }
    if (fromGuard != null && toGuard != null) {
      outGuard = Expression.automataPreConditionHoareLogic(toGuard, fromGuard, context);
    } else if (toGuard != null) {
      outGuard = toGuard;
    } else {
      outGuard = fromGuard;
    }


    return outGuard;
  }


  /**
   * TODO:
   * Execute the function on one or more petrinet.
   *
   * @param id        the id of the resulting petrinet
   * @param flags     the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context
   * @param petrinets the variable number of petrinets taken in by the function
   * @return the resulting petrinet of the operation
   * @throws CompilationException when the function fails
   */
  @Override
  public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {
    assert petrinets.length == 1;
    System.out.println("ABS NET 2 NET START");
    Petrinet petri = petrinets[0].reId("");
    Automaton aut = TokenRule.tokenRule(petri);
    Automaton[] as = new Automaton[1];
    as[0] = aut;
    Automaton a = compose(id, flags, context, as);
    Petrinet p = OwnersRule.ownersRule(a);
    System.out.println(p.myString());
    System.out.println("ABS NET 2 NET END ");
    return p;
  }

  public Petrinet composeX(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {
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

  /*
     Galois connections renames events with suffix  .t! and .t?  as tau that need to be abstracted away
     prior to computing quiescent trace refinement.
     This is called from TraceWorks
        TraceWorks has parameter stating which trace refinement to compute
   */
  public Automaton GaloisBCabs(String id, Set<String> flags, Context context, Automaton ain)
    throws CompilationException {
    //System.out.println("GaloisBCabs START "+ain.myString());
    Automaton a = ain.copy();
    int newLabelNode = a.getNodeCount() + 1;
    //System.out.println("GaloisBCabs COPY "+ain.myString());
    for (AutomatonEdge ed : a.getEdges()) {
     if (ed.getLabel().endsWith(".t!") || ed.getLabel().endsWith(".r!") ||
       ed.getLabel().endsWith(".t?") || ed.getLabel().endsWith(".r?")
      // || (ed.getLabel().endsWith("?") && ed.getFrom().equalId(ed.getTo()))
       ) {
        ed.setLabel(Constant.HIDDEN);
        //System.out.println("GalAbs renamed "+ed.myString());
      }
    /*   if (ed.getLabel().endsWith(".t?") || ed.getLabel().endsWith(".r?") ) {
        ed.setLabel(Constant.HIDDEN);
        //System.out.println("GalAbs renamed "+ed.myString());
      } */
    }
    Automaton[] as = new Automaton[1];
    as[0] = a;
    //Galois must not be unfair NOR congruent
    Automaton out = this.compose(id, flags, context, as);

    //System.out.println("**** GaloisBCabs END \n"+out.myString());
    return out;
  }
}
