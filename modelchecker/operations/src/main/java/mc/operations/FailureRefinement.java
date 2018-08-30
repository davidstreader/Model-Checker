
package mc.operations;

import static mc.processmodels.automata.util.ColouringUtil.ColourComponent;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Context;
import mc.AcceptanceGraph;
//import mc.BuildAcceptanceGraphs;
import mc.Constant;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.util.ColouringUtil;

  /**
   * Failure refinement differs from failure equality only in the initial coloring used
   */
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
    public Collection<String> getValidFlags(){
      return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR, Constant.CONGURENT);
    }
    @Override
    public String getOperationType(){return "automata";}
    /**
     * Evaluate the function.
     *
     * @param processModels the list of automata being compared
     * @return the resulting automaton of the operation
     * <p>
     * Failure equality is II-bisimulation of acceptance graphs
     * 1. build the acceptance graphs for each automata
     * a dfa + node to set of acceptance sets map
     * 2. Color the nodes of the dfa acording to acceptance set equality
     * initialise bisimulation coloring with the newly built coloring
     */
    @Override
    public boolean evaluate(Set<String> flags, Context context, Collection<ProcessModel> processModels) throws CompilationException {
      FailureEquivalence fe = new FailureEquivalence();
      return fe.evaluate(processModels,false);
    }
  }
