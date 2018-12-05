package mc.operations;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Context;
import mc.Constant;
import mc.TraceType;
import mc.compiler.Guard;
import mc.exceptions.CompilationException;
import mc.operations.functions.AbstractionFunction;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;

public class QuiescentRefinement implements IOperationInfixFunction {
  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "QuiescentRefinement";
  }

  /**
   * The form which the function will appear when composed in the text.
   *
   * @return the textual notation of the infix function
   */
  @Override
  public String getNotation() {
    return "<q";
  }
  @Override
  public String getOperationType(){return Constant.AUTOMATA;}
  @Override
  public Collection<String> getValidFlags(){
  return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR, Constant.CONGURENT);
  }
  /**
   * Evaluate the quiescent trace  refinement function.
   * OPTIONS 1. apply algorithm directly to what is displayed
   *         2. augment automaton with listening loops and apply complete trace refinement
   * option 1 has proven very hard to implement - after several attempts am forced to
   * acknowledge that I can not define this algorithm
   *
   *
   *
   *
   * @param alpha
   * @param processModels automaton in the function (e.g. {@code A} in {@code A ~ B})
   * @return the resulting automaton of the operation
   */
  @Override
  public boolean evaluate(Set<String> alpha, Set<String> flags, Context context,
                          Stack<String> trace, Collection<ProcessModel> processModels) throws CompilationException {
    //System.out.println("\nQUIESCENT " +getNotation() +"  "+ alpha);
    boolean cong = flags.contains(Constant.CONGURENT);
    //ProcessModel[] pms =  processModels.toArray();
    Automaton a1 = ((Automaton) processModels.toArray()[0]).copy();
    Automaton a2 = ((Automaton) processModels.toArray()[1]).copy();
   //System.out.println("****Quiescent input a1 "+a1.myString());
   //System.out.println("****Quiescent input a2 "+a2.myString());
    AbstractionFunction abs = new AbstractionFunction();
  //  a1 = abs.GaloisBCabs(a1.getId(), flags, context, a1);
  //  a2 = abs.GaloisBCabs(a2.getId(), flags, context, a2); //end states marked
    //System.out.println("*** Q a1  " + a1.readySets2String(cong));

    //Build set of all listening events in both automata
    Set<String> alphabet = a1.getAlphabet().stream().collect(Collectors.toSet());
    alphabet.addAll(a2.getAlphabet().stream().collect(Collectors.toSet()));
    Set<String>  listeningAlphabet = alphabet.stream().distinct().
      filter(x->x.endsWith(Constant.BROADCASTSinput)).
      collect(Collectors.toSet());
    System.out.println("\n new QUIESCENT " + listeningAlphabet);

    ArrayList<ProcessModel> pms = new ArrayList<>();
    addListeningLoops(a1, listeningAlphabet);
    addListeningLoops(a2, listeningAlphabet);
    //System.out.println(a1.myString());
    //System.out.println(a2.myString());
    pms.add(a1);
    pms.add(a2);

    TraceRefinement tr = new TraceRefinement();
    TraceWork tw = new TraceWork();
    //return tr.evaluate(alpha, flags, context, trace, processModels);
    return tw.evaluate(flags,context, pms,
      TraceType.QuiescentTrace,
      trace,
      tr::readyWrapped,
      (s1, s2, cong1, error) -> {boolean b =tr.isReadySubset(s1, s2, cong1, error);
        //System.out.println("Q "+error.error);
        return b;}
        );

  /*  This is the failed Option 1
  TraceWork tw = new TraceWork();  // THIS builds a DFA and then trace subset
    return tw.evaluate(flags,context, pms,
      TraceType.QuiescentTrace,
      trace,
      this::quiescentWrapped,
      this::isReadySubset);
  }
   */
  }
/*
  alpha  set of input events
  for each node add loop if not already part of ready set.
  Ownership !!!
 */
  public void addListeningLoops(Automaton ain,  Set<String> alpha )
    throws CompilationException {

    System.out.println("LL alphabet = "+alpha);

    Map<String,Set<String>> a2o = ain.eventNames2Owner();

    for(AutomatonNode nd : ain.getNodes()) {
       Set<String> ready = nd.readySet(false);
      System.out.println("  "+nd.getId()+"->"+ready);
       for(String al:alpha) {
         System.out.println("  al "+al);
         if (!ready.contains(al))  {
           AutomatonEdge ed =  ain.addEdge(al,nd,nd,new Guard(),false,false);
           if (a2o.containsKey(al)) {
             ed.setEdgeOwners(a2o.get(al));
           } else {
             ed.setEdgeOwners(ain.getOwners());
           }
           System.out.println("  adding "+ed.myString("smiple"));
         }
       }
    }
  }





  /*
    function returns the qiescent union of the ready sets to be added to the dfa
    returns the empty set if not quiescent
  */
  public List<Set<String>> quiescentWrapped(Set<AutomatonNode> nds, boolean cong){

    List<Set<String>> readyWrap = new ArrayList<>();
    Set<String> ready = new TreeSet<>();
    nds.stream().map(x->x.quiescentReadySet(cong)).forEach(s->ready.addAll(s));

    readyWrap.add(ready.stream().distinct().collect(Collectors.toSet()));
    //System.out.println("quiescentWrapped "+readyWrap);
    return readyWrap;
  }

  /*
    function to be applied to the data output from readyWrapped
    returns subset
   */
  private  boolean equivExternal(List<Set<String>> s1,List<Set<String>> s2) {
    Set<String> ex1 =  s1.get(0).stream().filter(Constant::observable).collect(Collectors.toSet());
    Set<String> ex2 =  s2.get(0).stream().filter(Constant::observable).collect(Collectors.toSet());
    return ex1.containsAll(ex2)&& ex2.containsAll(ex1);
  }
/*
  To enforce complete traces we need to know we are at the END
  to check if at the end we need to look down a listening chain of events.
  checking A <q B  if B_trace->END then for some chain A_trace->END
 */
  private boolean isReadySubset(List<Set<String>> s1,List<Set<String>> s2, boolean cong, ErrorMessage error) {
    boolean out = true;
    if (cong) out =  (s1.get(0).containsAll(s2.get(0)) );
    else {
      for (String lab :s2.get(0)) {
        if (Constant.external(lab)) continue;
        if (lab.endsWith(Constant.BROADCASTSinput))  continue;  //implicit inputs
        if (!s1.get(0).contains(lab)) {
          error = new ErrorMessage("? "+lab);
          out = false;
          //System.out.println("inQ error "+error.error);
          break;
        }
      }
    }
    return out;
  }
}

