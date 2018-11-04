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
  public boolean evaluate(Set<String> alpha, Set<String> flags,    Context context, Stack<String> trace, Collection<ProcessModel> processModels) throws CompilationException {
    ProcessModel[] pms = processModels.toArray(new ProcessModel[processModels.size()]);
    //System.out.println("TraceRefinement "+ alpha +" "+flags+ " "+ pms[0].getId()+ " "+pms[1].getId());
    TraceWork tw = new TraceWork();
    return tw.evaluate(flags,context, processModels,
      TraceType.Failure,
       trace,
      this::buildAsets,
      this::AcceptancePass
     // (a1, a2, cong, error) -> AcceptancePass(a1, a2, cong, error)
    );
  }

  /*
     function returns the union of the ready sets to be added to the dfa
   */
  public List<Set<String>>  buildAsets(Set<AutomatonNode> nodes, boolean cong) {
    List<Set<String>> acceptance = new LinkedList<>();
    for (AutomatonNode n : nodes) {// get the nfa nodes making this dfa node
      Set<String> as = n.readySet(cong);
      if (!acceptance.contains(as)) {
        acceptance.add(as);
      }
    }
    return acceptance; //correction(acceptance);
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
    for failure subset you need trace equality + AcceptanceSubSet
    all that need to be tested is refusal subset in reverse direction + AcceptanceSubSet
   */
  public  boolean AcceptancePass(List<Set<String>> a1, List<Set<String>> a2, boolean cong, ErrorMessage error) {
    if (refusalSubSet( a1, a2, cong, error))
       return AcceptanceSubSet( a1, a2, cong,  error);
    else return false;
  }

  private  boolean equivExternal(Set<String> s1,Set<String> s2) {
    Set<String> ex1 =  s1.stream().filter(Constant::observable).collect(Collectors.toSet());
    Set<String> ex2 =  s2.stream().filter(Constant::observable).collect(Collectors.toSet());
    return ex1.containsAll(ex2)&& ex2.containsAll(ex1);
  }
/*
  trace subset - takes the union of the ready sets as the dfa ready set
  needed to compute trace equality  2 a subset of 1
 */
  private  boolean refusalSubSet(List<Set<String>> a1, List<Set<String>> a2, boolean cong, ErrorMessage error) {

    System.out.println("\nrefusalSubSet");

    Set<String> a1Union = new TreeSet<>();
    for (Set<String> s: a1) {
      a1Union.addAll(s);
    }
    a1Union = a1Union.stream().distinct().collect(Collectors.toSet());
    Set<String> a2Union = new TreeSet<>();
    for (Set<String> s: a2) {
      a2Union.addAll(s);
    }
    a1Union = a1Union.stream().distinct().collect(Collectors.toSet());
    a2Union = a2Union.stream().distinct().collect(Collectors.toSet());
    Set<String> alpha = new TreeSet<>();
    alpha.addAll(a1Union);
    alpha.addAll(a2Union);

    System.out.println("is a2U "+a2Union+"  a sub set of a1U "+a1Union);
    if (!a1Union.containsAll(a2Union)) {
      a2Union.removeAll(a1Union);    //the problem Acceptance set
      alpha.removeAll(a2Union);      //the problem Refusal set
      error.error = "S"+alpha.toString();
      System.out.println("failing "+alpha);
      return false;
    }
   // if (cong && !equivExternal(a1Union,a2Union)) return false;
    return true;
  }

  /*  a2 subRefusal a1
   B refines into A
   Failure refinement => fail(A) subset fail(B)
         -> Complement(fail(A)) in Accept(A)
         => forall a in Accept(A) then exists b in Accept(B) where  b is a subset a

   set of sets  A>>B  means a > b where b is a set in A  and b in B
 */
  private  boolean AcceptanceSubSet(List<Set<String>> a1, List<Set<String>> a2, boolean cong, ErrorMessage error) {

    System.out.println(" START AcceptanceSuperSet " + a2 + " a Refusal Subset of " + a1 + "  ?");
    boolean ok = true;
    Set<String> unionA12 = new TreeSet<>(); // used in error message
    for (Set<String> as2 : a2) {       //FOR ALL as2 is in a2 then    (A)
      unionA12.addAll(as2);
      ok = false;
      System.out.println(" as2= "+as2);
      Set<String> as1 = new TreeSet<>();
      breakto:
      for (Set<String> aa1 : a1) {     // exists as1 in a1 such that   (B)

        if (!cong) {
          as1 = aa1.stream().filter(x->!Constant.externalOrEND(x)).collect(Collectors.toSet());
        } else{
          as1 = aa1;
        }
        unionA12.addAll(as1);
        System.out.println("   is as2 " + as2 + " superset of   as1 " + as1);
        if (as2.containsAll(as1)) {    //  as1 is a  subset of as2
          ok = true;                   // Ref(as2)subset Ref(as1)
          System.out.println("      as2 " + as2 + " is superset as1 " + as1);
          break breakto;
        }
      }

      if (ok == false) {           // as2 not subset any as1
        unionA12 = unionA12.stream().distinct().collect(Collectors.toSet());
        unionA12.removeAll(as2);
        error.error = "Ref"+unionA12.toString(); // complement of acceptance set as1
        break;
      } //if one inner false then outer false
    }  //outer only true if all inner loops true
    System.out.println(" a2 " + a2 + " a Refusal Subset of " + a1 + "  returns " + ok);
    return ok;
  }

}


