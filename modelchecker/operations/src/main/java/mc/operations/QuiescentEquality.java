
package mc.operations;

  import java.util.*;
  import java.util.stream.Collectors;

  import com.google.common.collect.ImmutableSet;
  import com.microsoft.z3.Context;
  import mc.Constant;
  import mc.exceptions.CompilationException;
  import mc.plugins.IOperationInfixFunction;
  import mc.processmodels.ProcessModel;
  import mc.processmodels.automata.Automaton;
  import mc.processmodels.automata.AutomatonNode;

public class QuiescentEquality implements IOperationInfixFunction {
  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "QuiescentEquality";
  }

  /**
   * The form which the function will appear when composed in the text.
   *
   * @return the textual notation of the infix function
   */
  @Override
  public String getNotation() {
    return "=q";
  }
  @Override
  public String getOperationType(){return "automata";}
  @Override
  public Collection<String> getValidFlags(){
    return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR, Constant.CONGURENT);
  }
  /**
   * Evaluate the function.
   *
   * @param alpha
   * @param processModels automaton in the function (e.g. {@code A} in {@code A ~ B})
   * @return the resulting automaton of the operation
   */
  @Override
  public boolean evaluate(Set<String> alpha, Set<String> flags, Context context, Collection<ProcessModel> processModels) throws CompilationException {
    System.out.println("QUIESCENT= "+alpha);
    boolean cong = flags.contains(Constant.Quiescent);
    //ProcessModel[] pms =  processModels.toArray();
    Automaton a1 = (Automaton) processModels.toArray()[0]; // reference only
    Automaton a2 = (Automaton) processModels.toArray()[1];
    TraceEquivalentOperation teo = new TraceEquivalentOperation();
    TraceWork tw = new TraceWork();
    //tw.evaluate(flags,processModels, TraceType.QuiescentTrace);
    addQuiescentAndListeningLoops(alpha,a1,cong);
    addQuiescentAndListeningLoops(alpha,a2,cong);
    System.out.println("Q= a1 "+a1.myString());
    System.out.println("Q= a2 "+a2.myString());
    return  teo.evaluate(alpha,flags,context,processModels);
  }

  private void addQuiescentAndListeningLoops(Set<String> alphbet, Automaton a,boolean cong) throws CompilationException {
    System.out.println("addQuiescentAndListeningLoops");
    for(AutomatonNode nd : a.getNodes()){
      Set<String> notListening = nd.readySet(cong).stream().filter(x->!x.endsWith("?")).collect(Collectors.toSet());
      nd.setQuiescent(notListening.size()==0);
      for(String lab: alphbet) {
        if (!nd.readySet(cong).contains(lab)) {
          a.addEdge(lab,nd,nd,nd.getGuard(),false,false);
        }
      }
    }
  }

}

