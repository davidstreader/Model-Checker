package mc.operations;

  import java.util.*;
  import java.util.stream.Collectors;

  import com.google.common.collect.ImmutableSet;
  import com.microsoft.z3.Context;
//import mc.BuildAcceptanceGraphs;
  import mc.Constant;
  import mc.TraceType;
  import mc.exceptions.CompilationException;
  import mc.plugins.IOperationInfixFunction;
  import mc.processmodels.ProcessModel;
  import mc.processmodels.automata.AutomatonNode;
  import mc.operations.TraceWork.Nd2NextMap;

/**
 * Failure refinement differs from failure equality only in the initial coloring used
 */
public class SingeltonFailureRefinement implements IOperationInfixFunction {


  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "SingeltonFailureRefinement";
  }

  /**
   * The form which the function will appear when composed in the text.
   *
   * @return the textual notation of the infix function
   */
  @Override
  public String getNotation() {
    return "<sf";
  }

  @Override
  public Collection<String> getValidFlags() {
    return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR, Constant.CONGURENT);
  }

  @Override
  public String getOperationType() {
    return Constant.AUTOMATA;
  }

  Nd2NextMap a1Next = new Nd2NextMap();
  Nd2NextMap a2Next = new Nd2NextMap();

  /**
   * Evaluate singeltonfailure Refinement.
   * Alphabet  {a,b,c,d,e}
   * resusal sets  {{a},{b},{c}}  equiv  {a,b,c}
   * Have node -> ready set {d,e}   can compute refusal sets from this
   *   Complement  Ready = Refusal   *{d,e} = {a,b,c}
   *
   *  dfa Node  = Union of Refusals = Complement of Intersection of Ready
   *  Hence store Intersection of Ready on dfa nodes "Ready"
   *
   *  =sf Initialise dfa bisim colouring with "Ready" equiv
   *  <sf Simulation Relation
   *
   *
   * @param alpha
   * @param processModels the list of automata being compared
   * @return the resulting automaton of the operation
   * <p>
   * 1. Build a  dfa (you can compute the sF from the dfa)
   * 2. Color the nodes of the dfa acording to singelton refusal equality
   * initialise bisimulation coloring with the newly built coloring
   */
  @Override
  public boolean evaluate(Set<String> alpha, Set<String> flags,
                          Context context,
                          Stack<String> trace,
                          Collection<ProcessModel> processModels) throws CompilationException {
    ProcessModel[] pms = processModels.toArray(new ProcessModel[processModels.size()]);
    //System.out.println("TraceRefinement "+ alpha +" "+flags+ " "+ pms[0].getId()+ " "+pms[1].getId());
    TraceWork tw = new TraceWork();
    //Void parameters used elsewhere to build Failure,Singelton Fail, ....
    //SubSetDataConstructor doNothing = (x,y) -> new ArrayList<>();
    //SubSetEval yes = (x,y,z) -> true;
    return tw.evaluate(flags,context, processModels,
      TraceType.SingeltonFailure,
      trace,
      this::refusalWrapped,
      (a1, a2, cong, error) -> singeltonPass(a1, a2, cong, error));
  }

  /*
     You can flatten a set of singleton failures, SF, into a single set. Then its complement is the
     ready set R.  The ogiginal SF can be rebuilt fc(R) = SF.
     Given two nodes ndR and ndT with ready sets R and T they have sf's fc(R) and fc(T). To compute
     the sf's of a dfa node from the set of nfa nodes we need to combine sf's:
     The sf's from either ndR or ndT is fc(R) cup fc(T). To compute this from R and T we note
                fc(R cap T) = fc(R) cup fc(T)
   */
  public List<Set<String>> refusalWrapped(Set<AutomatonNode> nds, boolean cong){

    List<Set<String>> refusalWrap = new ArrayList<>();
    Set<String> refusal = new TreeSet<>();
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
    System.out.println("SF refusalWrapped "+refusalWrap);

    return refusalWrap;
  }
/*
  inputs are the intersection of the ready sets - their complement being the
  flatened singleton Refusals
 */
  public  boolean singeltonPass(List<Set<String>> a1, List<Set<String>> a2, boolean cong,ErrorMessage error) {
    //if (dfaReadySubset( a1, a2, cong, error))  //Not sure about this but is in Failure Refinement
      return isSFSubset( a1, a2, cong, error);
   // else return false;
  }

  /*
  trace subset - takes flatened singelton failures use
  sub to compute trace sub
 */
  private  boolean equivExternal(Set<String> s1,Set<String> s2) {
    Set<String> ex1 =  s1.stream().filter(Constant::observable).collect(Collectors.toSet());
    Set<String> ex2 =  s2.stream().filter(Constant::observable).collect(Collectors.toSet());
    return ex1.containsAll(ex2)&& ex2.containsAll(ex1);
  }
  private  boolean dfaReadySubset(List<Set<String>> a1, List<Set<String>> a2, boolean cong,ErrorMessage error) {
    Set<String> small = a2.get(1).stream().filter(x->!Constant.externalOrEND(x)).collect(Collectors.toSet());

    boolean b =  a1.get(1).containsAll(small);
    //System.out.println(a2.get(1)+ " is dfaRedySubset of  "+a1.get(1) +" = " + b);
    a2.get(1).removeAll(a1.get(1));
    if (!b) error.error = "Rs{"+a2.get(1)+"}";
    return b;
  }

  /*
    function to be applied to the data output from readyWrapped
    returns superset  of Ready as equal to subset of fc(Ready)
   */

  private boolean isSFSubset(List<Set<String>> s2, List<Set<String>> s1, boolean cong,ErrorMessage error) {
    boolean out = true;
    //see returning to root in cribsheet
    //Set<String> small = s2.get(0).stream().filter(x->!Constant.external(x)).collect(Collectors.toSet());

    //System.out.println("small "+small);
    if (cong) out =  (s1.get(0).containsAll(s2.get(0)));
    else {
      for (String lab :s2.get(0)) {
        if (Constant.external(lab)) continue;
        if (!s1.get(0).contains(lab)) {
          error.error = "Ref{"+lab+"}";
          out = false;
          break;
        }
      }
    }
    System.out.println("can  s2 "+s2+" refuse more than s1 "+s1+" "+out);
    return out;
  }
}


