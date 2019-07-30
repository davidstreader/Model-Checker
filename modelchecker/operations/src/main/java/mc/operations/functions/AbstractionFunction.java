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
    return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR,
                           Constant.CONGURENT, Constant.OWNED,
                           Constant.SEQUENTIAL,Constant.FORCED);
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

  private Multimap<AutomatonNode, AutomatonNode> buildSCCgraph(Automaton ain) {
    Multimap<AutomatonNode, AutomatonNode> graph = ArrayListMultimap.create();
    for (AutomatonEdge ed : ain.getEdges()) {
      if (ed.getLabel().equals(Constant.HIDDEN)) {
        graph.put(ed.getFrom(), ed.getTo());
      }
    }
    return graph;
  }

  /**
   * Execute the abstraction on automata.
   * <p>
   * First ALL Tau loops in input are removed  Uses Tarjen  algorithm to compute
   * Strongly Conectd Components, SCC
   * CCS style keep n-tau->m where TauEnd(m)
   * TauEnd(x) <=> x-/-tau-> OR
   * x-tau->y and y in TauLoop and all n in TauLoop n-a->m => n-a->m in TauLoop
   * <p>
   * Abstraction for cong ==> keep taus that bridge the gap between internal and external nodes
   * not cong ==> remove all taus
   * use mutiple roots and remove bridging taus but only in pruning nodes
   * Both need to upgrade all process relations)
   * <p>
   * It would be desirable to perform observational simplification on the UNsaturted automata
   * abstraction both removes nodes + saturates the automata
   *
   * The addition of Tarjens algorithm removes all tau loops and has
   * allowed a simplification of the saturation algorithm
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
    boolean sequential = flags.contains(Constant.SEQUENTIAL);
    System.out.println("***ABSTRACTION flags " + flags+ "\n"+ startA.myString());
   MyAssert.validate(startA, "Abstraction input ");

    //reduce the state space and remove all loops - Tarjen
    Automaton abstraction = absMerge(flags, context, startA);

    System.out.println("\n******\nABS no DUP no Loop\n" + abstraction.myString());
    if (sequential) {
      observationalSemantics(flags, abstraction, context);
      abstraction.setSequential(true);
    }
    //System.out.println("\n******\nABS no DUP no Loop\n" + abstraction.myString());
    //abstraction =  AutomataReachability.removeUnreachableNodes(abstraction);
    MyAssert.validate(abstraction, "Abstraction output ");
    System.out.println("\n*****Abs final \n*****Abs final\n" + abstraction.myString()+"\n");
    return abstraction;
  }

  /*
      Saturates the automata building the obervational semantics for Failures
      The Preprocessing with Targens algorithm removes tau loops of ALL sizes

      Note this may mess up concurrancy hence set all owners to "{seq}"
   */
  public Automaton observationalSemantics(Set<String> flags, Automaton abstraction, Context context) throws CompilationException {
    // retrieve the unobservable edges from the specified automaton
    boolean isFair = flags.contains(Constant.FAIR) || !flags.contains(Constant.UNFAIR);
    boolean cong = flags.contains(Constant.CONGURENT);
    //System.out.println(" obs "+abstraction.myString());
    List<AutomatonEdge> hiddenEdges = abstraction.getEdges().stream()
      .filter(ed -> ed.isHidden() && !ed.getFrom().equalId(ed.getTo()))
      .collect(Collectors.toList());
    //System.out.println("hiddenEdges "+hiddenEdges.size());
    //System.out.println("hiddenEdges = "+hiddenEdges.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
    //Construct  edges to replace the unobservable edges

    List<AutomatonEdge> newHidden = new ArrayList<>();
    //Set<AutomatonEdge> processesed = new HashSet<>();
    while (!hiddenEdges.isEmpty()) {
      //for loop adds new taus to newHidden then they are trnasfered to hiddenEdge
      // whild only termintes when nothing is added
      for (AutomatonEdge hiddenEdge : hiddenEdges) {


        if (cong && hiddenEdge.stateObservable()) {
          //System.out.println("SKIP");
          continue; //Do not remove these taus
        }
        List<AutomatonEdge> temp = new ArrayList<AutomatonEdge>();

        //FALL through only straight taus and no loops in graph!
        // //System.out.println(" obs "+abstraction.myString());
        try {
          if (hiddenEdge.getTo().isSTOP()) {
            hiddenEdge.getFrom().setStopNode(true);
            abstraction.addEnd(hiddenEdge.getFrom().getId());
            //System.out.println("setting stop "+hiddenEdge.getFrom().myString());
          } // else hiddenEdge.getFrom().setStopNode(false);
          if (hiddenEdge.getTo().isERROR()) {
            hiddenEdge.getFrom().setErrorNode(true);
          }// else hiddenEdge.getFrom().setErrorNode(false);
          if (hiddenEdge.getFrom().isStartNode()) {
            hiddenEdge.getTo().setStartNode(true);
            //System.out.println("hTo "+hiddenEdge.getTo().getId());
            //System.out.println("r1 "+abstraction.getRoot().stream().map(x->x.getId()+" ").collect(Collectors.joining()));
            abstraction.addRoot(hiddenEdge.getTo());
            //System.out.println("r2 " + abstraction.getRoot().stream().map(x -> x.getId() + " ").collect(Collectors.joining()));
          }
          //abstraction is both In and OUT
          //System.out.println("\n   abs 111 "+abstraction.myString());
          temp.addAll(
            constructOutgoingEdges(abstraction, hiddenEdge, context));
          temp.addAll(
            constructIncomingEdges(abstraction, hiddenEdge, context));
          abstraction.removeEdge(hiddenEdge);
          //Add new tau s if not already processed

          for(AutomatonEdge t:temp){
            if (t.getLabel().equals(Constant.HIDDEN)) newHidden.add(t);
          }

// Worry might this loop forever ?
        } catch (InterruptedException ignored) {
          throw new CompilationException(this.getClass(), null);
        }
        //System.out.println("finished with " + hiddenEdge.myString());
      }  //for loop ended
      hiddenEdges = new ArrayList<>();
      for(AutomatonEdge nh: newHidden) { hiddenEdges.add(nh); }
      newHidden = new ArrayList<>();
    }
    //System.out.println("\n abs 222 "+abstraction.myString());
    // 1  loops may have been added.
    divergence(abstraction, isFair);
    //System.out.println("hidden cnt "+processesed.size());
    // abstraction might remove some locations
    Set<String> ow = new TreeSet<>();
    for (AutomatonEdge edge : abstraction.getEdges()) {
      ow.addAll(edge.getEdgeOwners());
    }
    if (ow.size() == 0) ow.add("s1");  //  when out put is stopSTOP
    //System.out.println("abs end XXX "+abstraction.myString());
    abstraction.setAllOwnerstoSeq();
    abstraction.cleanNodeLables();

    //System.out.println("abs end "+abstraction.myString());
    return abstraction;
  }

  public void divergence(Automaton abstraction, boolean isFair) throws CompilationException {
    //System.out.println("Divergence fair "+isFair);
    List<AutomatonEdge> hiddenEdges = abstraction.getEdges().stream()
      .filter(AutomatonEdge::isHidden)
      .collect(Collectors.toList());
    for (AutomatonEdge hiddenEdge : hiddenEdges) {
      if (hiddenEdge.getFrom().equals(hiddenEdge.getTo())) {
        //System.out.println("Diverge "+hiddenEdge.myString());
        if (!isFair) {
          //System.out.println("unFair");
          AutomatonNode deadlockNode = abstraction.addNode();
          deadlockNode.setErrorNode(true);

          AutomatonEdge added = abstraction.addEdge(Constant.HIDDEN, hiddenEdge.getFrom(),
            deadlockNode, null, false, false);
          added.setEdgeOwners(hiddenEdge.getEdgeOwners());
        }
        abstraction.removeEdge(hiddenEdge);
        //System.out.println("Gone");
      }
    }

  }


  /**
   * This method reduces the state space of the input automata
   * Both compressing all tau Loops AND
   * removing nodes only connected to tau events
   *   An option to ignore orthoganal events  when building the set
   *   of next events.
   *
   * @param flags
   * @param context
   * @param startA  input AND output
   * @return
   * @throws CompilationException
   */
  public Automaton absMerge(Set<String> flags, Context context, Automaton startA)
    throws CompilationException {
    boolean isFair = flags.contains(Constant.FAIR) || !flags.contains(Constant.UNFAIR);
    boolean cong = flags.contains(Constant.CONGURENT);
    //Build  tau connected graph
    SCCTarjan sccTarjan = new SCCTarjan();
    Multimap<AutomatonNode, AutomatonNode> graph = buildSCCgraph(startA);
    //Build  strongly tau connected components
    List<List<String>> components = sccTarjan.scc(graph);
    SimpFunction sf = new SimpFunction();
    //merge all strongly tau connected components
    sf.mergeNodes(startA, components, context);
    //startA.validateAutomaton("");
    //System.out.println("ABS MERGED " + startA.myString());
    startA.removeDuplicateEdges();
    // 1  loops may have been added AND must be removed before pruning
    // May be redundent  now Tarjan working
    // divergence(startA, isFair);
    //startA.validateAutomaton("");

    //pruning is well defined on failure semantics not bisimulation
    Automaton abstraction = pruneHiddenNodes(context, startA, cong);
    //System.out.println("ABS PRUNED " + abstraction.myString());
    abstraction.removeDuplicateEdges();
    abstraction.setEndFromNodes();
    abstraction.setRootFromNodes();


    return abstraction;
  }

  /**
   * @param abstraction automaton
   * @param hiddenEdge  to be removed
   * @param context     Symbolic
   * @return list of new hidden edges
   * @throws CompilationException
   * @throws InterruptedException
   *     adds  n-a->m when  n-a->x and x-tau->m to abstraction
   *     note if n-a->m and m-tau->n  then n-a->n will be added
   *                              and m-a->n is added
   *   if a=tau then union of owners else just owners of a
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
        if (edge.isHidden()) {
          abstraction.addOwnersToEdge(added, hiddenEdge.getEdgeOwners());
          abstraction.addOwnersToEdge(added, edge.getEdgeOwners());
        } else {
          abstraction.addOwnersToEdge(added, edge.getEdgeOwners());
        }
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
        abstraction.addOwnersToEdge(added, hiddenEdge.getEdgeOwners());
        abstraction.addOwnersToEdge(added, edge.getEdgeOwners());
        if (added.isHidden()) {
          hiddenAdded.add(added);
        }
        //System.out.println("Out ADDED "+added.myString());
        // n->tau->m m-a->n n-tau->m one tau used twice!
        if (from.getId().equals(to.getId()) && !edge.isHidden()) {
          added = abstraction.addEdge(edge.getLabel(), hiddenEdge.getFrom(), hiddenEdge.getTo(), null, false, edge.getOptionalEdge());
          abstraction.addOwnersToEdge(added, hiddenEdge.getEdgeOwners());
          abstraction.addOwnersToEdge(added, edge.getEdgeOwners());
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
        abstraction.addOwnersToEdge(added, hiddenEdge.getEdgeOwners());
        abstraction.addOwnersToEdge(added, edge.getEdgeOwners());
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
        //System.out.println("In ADDED "+added.myString());
        if (added.isHidden()) {
          abstraction.addOwnersToEdge(added, hiddenEdge.getEdgeOwners());
          abstraction.addOwnersToEdge(added, edge.getEdgeOwners());
          hiddenAdded.add(added);
        } else {
          abstraction.addOwnersToEdge(added, edge.getEdgeOwners());
        }
        //? REDUNDENT added.getEdgeOwners().addAll(hiddenEdge.getEdgeOwners());
        //System.out.println("Incoming add "+added.myString());

        // NOT REDUNDENT as  n->tau->m m-a->n n-tau->m one tau used twice!
        if (from.getId().equals(to.getId()) && !edge.isHidden()) {
          added = abstraction.addEdge(edge.getLabel(), hiddenEdge.getFrom(), hiddenEdge.getTo(), null, false, edge.getOptionalEdge());
          abstraction.addOwnersToEdge(added, hiddenEdge.getEdgeOwners());
          abstraction.addOwnersToEdge(added, edge.getEdgeOwners());

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
   * @throws CompilationException
   * This method solely acts as an accelerator and is only valid for
   * Testing / Failure  semamtics
   *  OLD Prunes any Internal node that is only connected by hidden events
   *
   * This works in as much as it produces the correct Aut. But the owners are wrong
   * and the owners rule produces the wrong Petri Net
   * Will add new tau edges  with owner ship =  union of owners of each edge
   *
   * 1. Amend to only check outgoing edges +
   *    observable edges to keep ownership
   *
   * 2. Amended to  ignore observable edges that are orthoganal
   * to the set of Tau edge
   */

  private Automaton pruneHiddenNodes(Context context, Automaton autoIN, boolean cong)
    throws CompilationException {
    Automaton abstraction = autoIN; //.copy();
System.out.println("PRUNE start "+ abstraction.myString());
    List<AutomatonNode> nodes = abstraction.getNodes(); // New List

    for (AutomatonNode n : nodes) {

      if (n.getOutgoingEdges().stream().filter(x->x.isHidden()).collect(Collectors.toSet()).size() ==0) {
        continue;
      }
      //build the owners of the set of hidden edges leaving n
      Set<String> tauOwners = new TreeSet<>();
      List<AutomatonEdge> observableEdges = new ArrayList<>();
      List<AutomatonEdge> inoutEdges =
        new ArrayList<>(n.getIncomingEdges());
      inoutEdges.addAll(n.getOutgoingEdges());
      for(AutomatonEdge te: n.getOutgoingEdges()){
        if (te.isHidden()) {tauOwners.addAll(te.getEdgeOwners());}
        else {
         observableEdges.add(te);
        }
      }
      if (tauOwners.size() == 0) continue;

      System.out.println("pruneHidden "+ n.getId()+ "obs cnt "+observableEdges.size()+" tau Own "+ tauOwners);
      observableEdges.stream().forEach(x->System.out.println(" "+x.myString()));

      // if all output nodes hidden or orthoganal to taus then prune
      boolean del = true;
      if (cong) {
        for (AutomatonEdge e : observableEdges) {
          if (!e.isOrthoganal(tauOwners) || e.stateObservable()) {
            del = false;
            break;
          }
        }
      } else {
        for (AutomatonEdge e : observableEdges) {
    System.out.println("e "+ e.isOrthoganal(tauOwners) + "  " +e.myString());
          if (!e.isOrthoganal(tauOwners) ) {
            del = false;
            break;
          }
        }
      }
      if (n.isStartNode() || n.getOutgoingEdges().size() == 0) {
        del = false;
      }

      // Now do the pruning
      if (del) {
        System.out.println("PRUNING "+n.myString());
        try {
          for (AutomatonEdge second : n.getOutgoingEdges()) {
            for (AutomatonEdge first : n.getIncomingEdges()) {
              if (first.getFrom().equals(second.getTo())) continue; //do not add 1-loop
              if (!second.isHidden()) continue;
              AutomatonEdge ne = abstraction.addEdge(first.getLabel(), first.getFrom(), second.getTo(),
                cbGuards(first, second, context), true, first.getOptionalEdge());


              ne.setEdgeOwners(first.getEdgeOwners());
              if (first.isHidden()) {
                // Need to grow the hidden owners else subsequent failue of this method
                ne.getEdgeOwners().addAll(second.getEdgeOwners());
              }
              System.out.println("Prune adds " + ne.myString());
            }
          }

          /* Next glue Owners together  F
          Map.Entry<Set<String>,Multimap<String, String>> pair =
               Petrinet.buildGluedOwners(abstraction.getOwners(),owns1,owns2);
          abstraction.setOwners(pair.getKey());
          this only works for single transitions. Hence must be done on a Petrinet
          dstr */
          //System.out.println("Prune del " + n.myString());
          abstraction.removeNode(n);  // tidies up all the edges
        } catch (InterruptedException ignored) {
          throw new CompilationException(this.getClass(), null);
        }
      }
    }
    abstraction.reId("abs");
    return abstraction;
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
     ORINGINAL Pruning for  automaton
   */
  private Automaton OLDpruneHiddenNodes(Context context, Automaton autoIN, boolean cong)
    throws CompilationException {
    Automaton abstraction = autoIN; //.copy();
//System.out.println("prune "+ abstraction.myString());
    List<AutomatonNode> nodes = abstraction.getNodes();

    for (AutomatonNode n : nodes) {

      // Original if all input and output nodes hidden then prune
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


      // Now do the pruning
      if (del) {
        //System.out.println("PRUNING "+n.myString());
        try {
          for (AutomatonEdge second : n.getOutgoingEdges()) {
            for (AutomatonEdge first : n.getIncomingEdges()) {
              if (first.getFrom().equals(second.getTo())) continue; //do not add 1-loop
              AutomatonEdge ne = abstraction.addEdge(Constant.HIDDEN, first.getFrom(), second.getTo(),
                cbGuards(first, second, context), false, first.getOptionalEdge());
              System.out.println("Prune adds " + ne.myString());
            }
          }
          //System.out.println("Prune del " + n.myString());
          abstraction.removeNode(n);  // tidies up all the edges
        } catch (InterruptedException ignored) {
          throw new CompilationException(this.getClass(), null);
        }
      }
    }
    List<String> o = (new ArrayList<String>());
    o.add("seq");
    for(AutomatonEdge e: abstraction.getEdges()) {
      e.setEdgeOwners(o);
    }
    return abstraction;
  }


  private void mergeSCCNodes(Automaton ain, List<List<AutomatonNode>> components) {
    for (List<AutomatonNode> nds : components) {
      if (nds.size() > 1) {

      }
    }
  }

 /*
 if (edge.isHidden() &&
          abstraction.getEdge(Constant.HIDDEN, to, from) == null) {
          //System.out.println("combining "+ edge.getTo()+" and "+edge.getFrom());
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
   * Abstraction originaly defined on Automata. The results of which althugh
   * give correct councurrent semantics thay can not be converted back into
   * Petri Nets
   * Hence define abstraction on Petri Nets
   * First def.
   *   Remove a tau and gluing disjoint pre post
   * For Transition hd such that:
   *      lab(hd) = tau and  All tr  pre(tr) cup pre(hd) = {}
   *  then  glue(pre(hd)-post(hd) X post(hd)-pre(hd)
   *        apply glue owners
   *        and remove hd
   *  tidy Net and owners???
   *
   * Second def.
   *     1. saturate  by adding tau_post and  pre_tau trnsitions
   *     2. Colour Net and then
   *          3a. comp Net Bisim
   *          3b. OR  simplify by glueing together color equiv sets (multi_sets??).
   *
   * First def builds N-safe Nets and Second def 1-Safe Nets
   * First def has elegant compact representation of Nplace buffers.
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
    //System.out.println("ABS NET 2 NET START");
    Petrinet petri = petrinets[0].reId("");
    Automaton aut = TokenRule.tokenRule(petri);
    Automaton[] as = new Automaton[1];
    as[0] = aut;
    Automaton a = compose(id, flags, context, as);
    Petrinet p = OwnersRule.ownersRule(a);
    //System.out.println(p.myString());
    //System.out.println("ABS NET 2 NET END ");
    return p;
  }
/*

 */
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
    //System.out.println("GaloisBCabs START "+flags);
    flags = new TreeSet<>();
    //System.out.println("GaloisBCabs START "+flags);
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
