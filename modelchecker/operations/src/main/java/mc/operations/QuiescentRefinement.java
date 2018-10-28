package mc.operations;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Context;
import mc.Constant;
import mc.TraceType;
import mc.exceptions.CompilationException;
import mc.operations.functions.AbstractionFunction;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
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
  public String getOperationType(){return "automata";}
  @Override
  public Collection<String> getValidFlags(){
  return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR, Constant.CONGURENT);
  }
  /**
   * Evaluate the quescten refinement function.
   *
   * @param alpha
   * @param processModels automaton in the function (e.g. {@code A} in {@code A ~ B})
   * @return the resulting automaton of the operation
   */
  @Override
  public boolean evaluate(Set<String> alpha, Set<String> flags, Context context, Collection<ProcessModel> processModels) throws CompilationException {
    System.out.println("\nQUIESCENT "+alpha);
    boolean cong = flags.contains(Constant.CONGURENT);
    //ProcessModel[] pms =  processModels.toArray();
    Automaton a1 = ((Automaton) processModels.toArray()[0]).copy();
    Automaton a2 = ((Automaton) processModels.toArray()[1]).copy();
    System.out.println("****Quiescent a1 "+a1.readySets2String(cong));
    System.out.println("****Quiescent a2 "+a2.readySets2String(cong));
    //TraceRefinement teo = new TraceRefinement();

    AbstractionFunction abs = new AbstractionFunction();
    //tw.evaluate(flags,processModels, TraceType.QuiescentTrace);
    //setQuiescentAndAddListeningLoops(alpha,a1);
    //setQuiescent(a1,cong);
    //setQuiescent(a2,cong);
    a1 = abs.GaloisBCabs(a1.getId(),flags,context,a1);
    a2 = abs.GaloisBCabs(a2.getId(),flags,context,a2); //end states marked
    //a1 = simp.compose(a1.getId(),flags,context,a1);
    //a2 = simp.compose(a2.getId(),flags,context,a2);
    //AddListeningLoops(alpha,a1,cong);  //PROBLEMS with doing this NOW
    //AddListeningLoops(alpha,a2,cong);
    System.out.println("*** Q a1 before traceEval "+a1.readySets2String(cong));
    System.out.println("*** Q a2 before traceEval "+a2.readySets2String(cong));

    ArrayList<ProcessModel> pms = new ArrayList<>();;
    pms.add(a1);
    pms.add(a2);
    //return  teo.evaluate(alpha,flags,context,pms);
    TraceWork tw = new TraceWork();  // THIS builds a DFA and then trace subset
    return tw.evaluate(flags,context, pms, TraceType.QuiescentTrace,
           this::quiescentWrapped, this::isReadySubset);
  }

  /**
   * sets the boolean quiescent on the nodes.
   * @param a
   * @param cong
   * @throws CompilationException
   */
  public void setQuiescent( Automaton a,boolean cong) throws CompilationException {
    System.out.println("setQuiescent");
    for(AutomatonNode nd : a.getNodes()){
      Set<String> notListening = nd.quiescentReadySet(cong).stream().filter(x->!x.endsWith("?")).collect(Collectors.toSet());
      nd.setQuiescent(notListening.size()==0);
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
    System.out.println("quiescentWrapped "+readyWrap);
    return readyWrap;
  }

  /*
    function to be applied to the data output from readyWrapped
    returns subset
   */

  private boolean isReadySubset(List<Set<String>> s1,List<Set<String>> s2, boolean cong) {
    boolean out = true;
    if (cong) out =  s2.get(0).containsAll(s1.get(0));
    else {
      for (String lab :s1.get(0)) {
        if (Constant.external(lab)) continue;
        if (!s2.get(0).contains(lab)) {
          out = false;
          break;
        }
      }
    }
    return out;
  }
}

