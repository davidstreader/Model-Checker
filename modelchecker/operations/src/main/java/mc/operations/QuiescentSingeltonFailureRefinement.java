package mc.operations;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Context;
import mc.Constant;
import mc.TraceType;
import mc.exceptions.CompilationException;
import mc.operations.functions.AbstractionFunction;
import mc.operations.functions.SimpFunction;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;

public class QuiescentSingeltonFailureRefinement implements IOperationInfixFunction {
  /**
   * A method of tracking the function.
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "QuiescentSingeltonFailureRefinement";
  }
  /**
   * The form which the function will appear when composed in the text.
   * @return the textual notation of the infix function
   */
  @Override
  public String getNotation() {
    return "<qsf";
  }
  @Override
  public String getOperationType(){return "automata";}
  @Override
  public Collection<String> getValidFlags(){
    return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR, Constant.CONGURENT, Constant.OBSEVATIONAL);
  }
  /**
   *
   * You can flatten a set of singleton failures, SF, into a single set. Then its complement is the
   * ready set R.  The ogiginal SF can be rebuilt fc(R) = SF.
   * Given two nodes ndR and ndT with ready sets R and T they have sf's fc(R) and fc(T). To compute
   * the sf's of a dfa node from the set of nfa nodes we need to combine sf's:
   * The sf's from either ndR or ndT is fc(R) cup fc(T). To compute this from R and T we note
   *                 fc(R cap T) = fc(R) cup fc(T)
   *
   * Evaluate the quiescent singeltonFailure  refinement function.
   *  we stick to OPTIONS  2. (see quiescent trace refinement)
   *     1.  augment automaton with listening loops and Quiescent events (like STOP events)
   *     2.  apply singelton failure refinement
   * @param alph
   * @param processModels automaton in the function (e.g. {@code A} in {@code A ~ B})
   * @return the resulting automaton of the operation
   */
  @Override
  public boolean evaluate(Set<String> alph, Set<String> flags, Context context,
                          Stack<String> trace, Collection<ProcessModel> processModels) throws CompilationException {
    System.out.println("\nQUIESCENT " + alph+" "+flags+" ");
    boolean cong = flags.contains(Constant.CONGURENT);
    //ProcessModel[] pms =  processModels.toArray();
    Automaton a1 = ((Automaton) processModels.toArray()[0]).copy();
    Automaton a2 = ((Automaton) processModels.toArray()[1]).copy();
    System.out.println("****Quiescent a1 "+a1.myString());
    System.out.println("****Quiescent a2 "+a2.myString());
    /*
     mapping from bc to ap to bc addes try reject loops
     */
   AbstractionFunction abs = new AbstractionFunction();
    a1 = abs.GaloisBCabs(a1.getId(), flags, context, a1);
    a2 = abs.GaloisBCabs(a2.getId(), flags, context, a2); //end states marked
    //System.out.println("*** Q a1  " + a1.readySets2String(cong));
    System.out.println("Gabs "+a1.myString());
    System.out.println("Gabs "+a2.myString());

  /*
        a?->b!->c?->STOP
    the a? event can be detected using contexts b?-STOP, a!->STOP
    The final c? event can never be detected! WITHOUT {cong}
    To remove all such c?  add listening loops and simp{}
    safe to to even with {cong} but should change nothing
*/
    Set<String> alpha = a1.getAlphabetFromEdges();
    alpha.addAll(a2.getAlphabetFromEdges());
    alpha = alpha.stream().filter(x->x.endsWith(Constant.BROADCASTSinput)).collect(Collectors.toSet());

    SimpFunction simpf = new SimpFunction();
    addListeningLoops(a1,alpha);
    List<List<String>> partition = simpf.buildPartition(flags, a1);
    simpf.mergeNodes(a1, partition, context);
    addListeningLoops(a2,alpha);
    partition = simpf.buildPartition(flags, a2);
    simpf.mergeNodes(a2, partition, context);

    System.out.println("a1 "+a1.myString());
    System.out.println("a2 "+a2.myString());
  /*
     Finally build dfa and evaluate
   */
    ArrayList<ProcessModel> pms = new ArrayList<>();
    pms.add(a1);
    pms.add(a2);

    SingeltonFailureRefinement sf = new SingeltonFailureRefinement();
    TraceWork tw = new TraceWork();
    //return tr.evaluate(alpha, flags, context, trace, processModels);
    return tw.evaluate(flags,context, pms,
      TraceType.QuiescentTrace,
      trace,
      this::refusalWrapped,
      (a11, a21, cong1, error) -> singeltonQPass(a11, a21, cong1, error));

  }

  private void addListeningLoops(Automaton ain,Set<String> listen)
    throws CompilationException {
    for(AutomatonNode nd: ain.getNodes()){
      Set<String> found = nd.getOutgoingEdges().stream().map(x->x.getLabel()).
        collect(Collectors.toSet());
      for (String lab: listen){
        if (!found.contains(lab)){
          ain.addEdge(lab, nd,nd, null, false,false);
        }
      }
    }

  }

/*
  Build the intersection of the ready sets from the nfa
     the singelton refusals can be infered.
  First filter out any non Quiescent nfa node
   if some Quiesctent  nodes the intersection will have Quiescent event
   and the nonQuiescent nodes will have been ignored.
   if all NonQuiescent then empth set will be empty.
 */

  private List<Set<String>> refusalWrapped(Set<AutomatonNode> nds, boolean cong){

    List<Set<String>> refusalWrap = new ArrayList<>();
    Set<String> refusal = new TreeSet<>();
    Set<String> ready = new TreeSet<>();
    Set<String> rQ = new TreeSet<>();
    rQ.add("!"+Constant.Quiescent);
    boolean first = true;
    for (AutomatonNode nd: nds){
      System.out.println(nd.getId()+"->"+ nd.getOutgoingEdges().stream().map(ed->ed.getLabel()+" ").collect(Collectors.joining()));
      // if node not Quiescent skip
      if (nd.getOutgoingEdges().stream().
        filter(e->e.getLabel().equals(Constant.Quiescent)).
        collect(Collectors.toSet()).size()==0) {
        //refusal = rQ;
        System.out.println("not Q");
        continue;  //if not quiescent do not build intersection
      }
      if (first) {
        first = false;
        refusal = nd.readySet(cong);
      }
      else refusal.retainAll(nd.readySet(cong));  //build intersection
      System.out.println("refusal = "+refusal);
    }
    nds.stream().map(x->x.readySet(cong)).forEach(s->ready.addAll(s));
    //Wrap set in first element of Lis
    refusalWrap.add(refusal.stream().distinct().collect(Collectors.toSet()));
    refusalWrap.add(ready.stream().distinct().collect(Collectors.toSet()));
    System.out.println("QSF refusalWrapped "+refusalWrap);

    return refusalWrap;
  }

  public  boolean singeltonQPass(List<Set<String>> a1, List<Set<String>> a2, boolean cong,ErrorMessage error) {
    return isQSFSubset( a1, a2, cong, error);

  }
  /**  this computes SF subset OR =
   * A state that is not Quiescent cannot refuse anything hene the FRIGs
   * for {cong} A STOP or Start state must take the worst options for Quiescence
   *
   * @param s2  Acceptance set
   * @param s1  Acceptance set
   *            s1 subset s2 => s2 refuses more that s1
   * @param cong
   * @param error
   * @return
   */
  private boolean isQSFSubset(List<Set<String>> s2, List<Set<String>> s1, boolean cong,ErrorMessage error) {
    boolean out = true;
    System.out.println("isSF s1 "+s1.get(0)+" s2 "+s2.get(0));
    //Set<String> small = s2.get(0).stream().filter(x->!Constant.external(x)).collect(Collectors.toSet());
    if (!s1.get(0).contains(Constant.Quiescent)) {  //s1 can refuse nothing
      System.out.println(" Quiescent FRIG  true");
      out = true;
    } else if (s1.get(0).contains(Constant.Quiescent) &&
      !s2.get(0).contains(Constant.Quiescent)) {  //s2 can refuse nothing
      System.out.println(" Quiescent FRIG false");
      out = false;
    } else {
      //System.out.println("small "+small);
      if (cong) {
        if ((s1.contains(Constant.STOP) && !s2.contains(Constant.STOP))
          ||(s1.contains(Constant.Start)&& !s2.contains(Constant.Start))  ) {
          s1.remove(Constant.Quiescent);  //worst interpretation
        }
        //out = (s1.get(0).containsAll(s2.get(0)));
        for (String lab : s2.get(0)) {
          if (!s1.get(0).contains(lab)) {
            error.error = "Ref{" + lab + "}";
            out = false;
            break;
          }
        }
      } else {
        for (String lab : s2.get(0)) {
          if (Constant.external(lab)) continue;
          if (!s1.get(0).contains(lab)) {
            error.error = "Ref{" + lab + "}";
            out = false;
            break;
          }
        }
      }
    }
    System.out.println("can  s2 "+s2.get(0)+" refuse more than (or =) s1 "+s1.get(0)+" "+out);
    return out;
  }



  /*
     You can flatten a set of singleton failures, SF, into a single set. Then its complement is the
     ready set R.  The ogiginal SF can be rebuilt fc(R) = SF.
     Given two nodes ndR and ndT with ready sets R and T they have sf's fc(R) and fc(T). To compute
     the sf's of a dfa node from the set of nfa nodes we need to combine sf's:
     The sf's from either ndR or ndT is fc(R) cup fc(T). To compute this from R and T we note
                fc(R cap T) = fc(R) cup fc(T)
   */
  public List<Set<String>> refusalQWrapped(Set<AutomatonNode> nds, boolean cong){

    List<Set<String>> refusalWrap = new ArrayList<>();
    Set<String> refusal = new TreeSet<>();  // the intersection of all the ready sets
    Set<String> ready = new TreeSet<>();
    boolean first = true;
    for (AutomatonNode nd: nds){
      if (first) {
        first = false;
        refusal = nd.readySet(cong);
      }
      else refusal.retainAll(nd.readySet(cong));
    }
    nds.stream().map(x->x.readySet(cong)).forEach(s->ready.addAll(s));
    //Wrap set in first element of Lis
    refusalWrap.add(refusal.stream().distinct().collect(Collectors.toSet()));
    refusalWrap.add(ready.stream().distinct().collect(Collectors.toSet()));
    //System.out.println("refusalWrapped "+refusalWrap);

    return refusalWrap;
  }
}