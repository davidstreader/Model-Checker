package mc.operations;

import java.util.*;
import java.util.stream.Collectors;

import lombok.Getter;
import mc.TraceType;
import mc.exceptions.CompilationException;
import mc.operations.functions.NFtoDFconvFunction;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.util.ColouringUtil;

public class QuiescentRefinement implements IOperationInfixFunction {
  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "TraceRefinement";
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
  /**
   * Evaluate the function.
   *
   * @param processModels automaton in the function (e.g. {@code A} in {@code A ~ B})
   * @return the resulting automaton of the operation
   */
  @Override
  public boolean evaluate(Collection<ProcessModel> processModels) throws CompilationException {
    TraceWork tw = new TraceWork();
    return tw.evaluate(processModels, TraceType.QuiescentTrace);
  }
}

