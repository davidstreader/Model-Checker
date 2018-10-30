package mc.operations;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Context;
import mc.Constant;
import mc.TraceType;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;

public class FailureRefinement implements IOperationInfixFunction {
  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "FailureRefinement";
  }
  @Override
  public Collection<String> getValidFlags(){
    return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR, Constant.CONGURENT);
  }

  /**
   * The form which the function will appear when composed in the text.
   *
   * @return the textual notation of the infix function
   */
  @Override
  public String getNotation() {
    return "<f";
  }
  @Override
  public String getOperationType(){return "automata";}
  /**
   * Evaluate the function.
   *
   * @param alpha
   * @param processModels automaton in the function (e.g. {@code A} in {@code A ~ B})
   * @return the resulting automaton of the operation
   */
  @Override
  public boolean evaluate(Set<String> alpha, Set<String> flags, Context context, Collection<ProcessModel> processModels) throws CompilationException {
    ProcessModel[] pms = processModels.toArray(new ProcessModel[processModels.size()]);
    //System.out.println("TraceRefinement "+ alpha +" "+flags+ " "+ pms[0].getId()+ " "+pms[1].getId());
    TraceWork tw = new TraceWork();

    return tw.evaluate(flags,context, processModels,
      TraceType.Failure,
      this::buildAsets,
      this::AcceptancePass);
  }

  /*
     function returns the union of the ready sets to be added to the dfa
   */
  public List<Set<String>>  buildAsets(Set<AutomatonNode> nodes, boolean cong) {
    List<Set<String>> acceptance = new LinkedList<>();
    for (AutomatonNode n : nodes) {// get the nfa nodes making this dfa node
      Set<String> as = n.getOutgoingEdges().stream().
        distinct().
        map(AutomatonEdge::getLabel).
        collect(Collectors.toSet());
      if (cong) {
        if (n.isSTOP()) {
          as.add(Constant.STOP);
        }
        if (n.isStartNode()) {
          as.add(Constant.Start);
        }
      }
      if (!acceptance.contains(as)) {
        acceptance.add(as);
      }
    }
    return correction(acceptance);
  }
  private List<Set<String>> correction(List<Set<String>> in){
    List<Set<String>> asout = new ArrayList<>();
    for (Set<String> as : in) {
      if (as.size() > 1) {
        for (String a : as) {
          if (a.endsWith(Constant.ACTIVE)) {
            Set<String> s = new HashSet<String>();
            s.add(a);
            asout.add(s);
          }
        }
      }
      asout.add(as);
    }
    return asout;
  }

/*
   a2 subAcceptance a1
  To carry on their must be a subset of Acceptance sets +
   The union of the acceptance sets must be subset else the next move will fail!
 */
  public  boolean AcceptancePass(List<Set<String>> a1, List<Set<String>> a2, boolean cong) {
    Set<String> a1Union = new TreeSet<>();
    for (Set<String> s: a1) {
      a1Union.addAll(s);
    }
    a1Union = a1Union.stream().distinct().collect(Collectors.toSet());
    Set<String> a2Union = new TreeSet<>();
    for (Set<String> s: a2) {
      a2Union.addAll(s);
    }a2Union = a2Union.stream().distinct().collect(Collectors.toSet());
    //System.out.println("a2U "+a2Union+"  is a sub set of a1U "+a1Union);
    if (!a1Union.containsAll(a2Union)) {
      //System.out.println("failing");
      return false;
    }
    return AcceptanceSubSet( a1, a2, cong);
  }

  /*  a2 subRefusal a1
   B refines into A
   Failure refinement => fail(A) subset fail(B)
         -> Complement(fail(A)) in Accept(A)
         => forall a in Accept(A) then exists b in Accept(B) where  b is a subset a

   set of sets  A>>B  means a > b where b is a set in A  and b in B
 */
  public  boolean AcceptanceSubSet(List<Set<String>> a1, List<Set<String>> a2, boolean cong) {

    //System.out.println(" START AcceptanceSuperSet " + a2 + " a Refusal Subset of " + a1 + "  ?");
    boolean ok = true;

    for (Set<String> as2 : a2) {       //FOR ALL as2 is in a2 then    (A)
      ok = false;
      //System.out.println(" as2= "+as2);
      breakto:
      for (Set<String> aa1 : a1) {     // exists as1 in a1 such that   (B)
        //strip out external
        Set<String> as1 = aa1.stream().filter(x->!Constant.external(x)).collect(Collectors.toSet());
        //System.out.println("   is as2 " + as2 + " superset of   as1 " + as1);
        if (as2.containsAll(as1)) {    //  as1 is a  subset of as2
          ok = true;
          //System.out.println("      as2 " + as2 + " is superset as1 " + as1);
          break breakto;
        } else {
          //ok = false;
          //System.out.println("   as2 " + as2 + " is NOT superset as1 " + as1);
          //break breakto;
        }
      }

      if (ok == false) { break;} //if one inner false then outer false
    }  //outer only true if all inner loops true
    //System.out.println(" a2 " + a2 + " a Refusal Subset of " + a1 + "  returns " + ok);
    return ok;
  }

}


