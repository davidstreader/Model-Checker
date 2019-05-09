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

public class QuiescentFailureRefinement implements IOperationInfixFunction {
  /**
   * A method of tracking the function.
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "QuiescentFailureRefinement";
  }
  /**
   * The form which the function will appear when composed in the text.
   * @return the textual notation of the infix function
   */
  @Override
  public String getNotation() {
    return "<qf";
  }
  @Override
  public String getOperationType(){return "automata";}
  @Override
  public Collection<String> getValidFlags(){
    return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR,
             Constant.CONGURENT,Constant.NOListeningLoops);
  }
  /**
   * Evaluate the quiescent singeltonFailure  refinement function.
   *  we stick to OPTIONS  2. (see quiescent trace refinement)
   *       augment automaton with listening loops and apply singelton failure refinement
   * @param alpha
   * @param processModels automaton in the function (e.g. {@code A} in {@code A ~ B})
   * @return the resulting automaton of the operation
   */
  @Override
  public boolean evaluate(Set<String> alpha, Set<String> flags, Context context,
                          Stack<String> trace, Collection<ProcessModel> processModels) throws CompilationException {
    //System.out.println("\nQUIESCENT " + alpha);
    boolean cong = flags.contains(Constant.CONGURENT);
    boolean noLL = flags.contains(Constant.NOListeningLoops);
    //ProcessModel[] pms =  processModels.toArray();
    Automaton a1 = ((Automaton) processModels.toArray()[0]).copy();
    Automaton a2 = ((Automaton) processModels.toArray()[1]).copy();
    //System.out.println("****Quiescent a1 "+a1.readySets2String(cong));
    AbstractionFunction abs = new AbstractionFunction();
    a1 = abs.GaloisBCabs(a1.getId(), flags, context, a1);
    a2 = abs.GaloisBCabs(a2.getId(), flags, context, a2); //end states marked
    //System.out.println("*** Q a1  " + a1.readySets2String(cong));

    //Build set of all listening events in both automata
    Set<String> alphabet = a1.getAlphabet().stream().collect(Collectors.toSet());
    alphabet.addAll(a2.getAlphabet().stream().collect(Collectors.toSet()));
    Set<String>  listeningAlphabet = alphabet.stream().distinct().
      filter(x->x.endsWith(Constant.BROADCASTSinput)).
      collect(Collectors.toSet());
    //System.out.println("\n new listeningAlphabet " + listeningAlphabet);

    ArrayList<ProcessModel> pms = new ArrayList<>();
    if (! noLL) {
      addListeningLoops(a1, listeningAlphabet);
      addListeningLoops(a2, listeningAlphabet);
    }
    //System.out.println(a1.myString());
    //System.out.println(a2.myString());
    pms.add(a1);
    pms.add(a2);

    FailureRefinement fr = new FailureRefinement();
    TraceWork tw = new TraceWork();
    //return tr.evaluate(alpha, flags, context, trace, processModels);
    return tw.evaluate(flags,context, pms,
      TraceType.QuiescentTrace,
      trace,
      fr::buildAsets,
      fr::AcceptancePass
    );
  }

  private void addListeningLoops(Automaton ain,  Set<String> alphain )
    throws CompilationException {
    QuiescentRefinement qr = new QuiescentRefinement();
    qr.addListeningLoops(ain,alphain);
  }
}