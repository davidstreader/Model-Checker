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
  public boolean evaluate(Set<String> alpha, Set<String> flags, Context context, Collection<ProcessModel> processModels) throws CompilationException {
    ProcessModel[] pms = processModels.toArray(new ProcessModel[processModels.size()]);
    //System.out.println("TraceRefinement "+ alpha +" "+flags+ " "+ pms[0].getId()+ " "+pms[1].getId());
    TraceWork tw = new TraceWork();
    //Void parameters used elsewhere to build Failure,Singelton Fail, ....
    //SubSetDataConstructor doNothing = (x,y) -> new ArrayList<>();
    //SubSetEval yes = (x,y,z) -> true;
    return tw.evaluate(flags,context, processModels,
      TraceType.CompleteTrace,
      this::readyWrapped,
      this::isSFSubset);
  }

  /*
     A ready set is the complement of the flatened singelton failures
      fc(R) = SF
     function returns complement of the union of the failure sets  from the individual ready sets
      fc(R cup T) = fc(R) cap fc(T)
   */
  private List<Set<String>> readyWrapped(Set<AutomatonNode> nds, boolean cong){

    List<Set<String>> readyWrap = new ArrayList<>();
    Set<String> ready = new TreeSet<>();
    boolean first = true;
    for (AutomatonNode nd: nds){
      if (first) {
        first = false;
        ready = nd.readySet(cong);
      }
      else ready.retainAll(nd.readySet(cong));

    }

    //System.out.println("readyWrapped "+ready);
    readyWrap.add(ready.stream().distinct().collect(Collectors.toSet()));
    System.out.println("readyWrapped "+readyWrap);

    return readyWrap;
  }

  /*
    function to be applied to the data output from readyWrapped
    returns superset  of Ready as equal to subset of fc(Ready)
   */

  private boolean isSFSubset(List<Set<String>> s2, List<Set<String>> s1, boolean cong) {
    System.out.print("isSFSubset  s2 "+s2+"  s1 "+s1);
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
    System.out.println(" "+out);
    return out;
  }
}


