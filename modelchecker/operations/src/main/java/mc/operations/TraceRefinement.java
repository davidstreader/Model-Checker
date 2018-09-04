package mc.operations;

import java.util.*;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Context;
import mc.Constant;
import mc.TraceType;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;

public class TraceRefinement implements IOperationInfixFunction {
  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "TraceRefinement";
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
    return "<t";
  }
  @Override
  public String getOperationType(){return "automata";}
  /**
   * Evaluate the function.
   *
   * @param processModels automaton in the function (e.g. {@code A} in {@code A ~ B})
   * @return the resulting automaton of the operation
   */
  @Override
  public boolean evaluate(Set<String> flags, Context context, Collection<ProcessModel> processModels) throws CompilationException {
    TraceWork tw = new TraceWork();
    return tw.evaluate(flags,processModels, TraceType.CompleteTrace);
  }
}

