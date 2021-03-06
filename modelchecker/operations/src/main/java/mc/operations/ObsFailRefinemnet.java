package mc.operations;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Context;
import mc.Constant;
import mc.exceptions.CompilationException;
import mc.operations.functions.AbstractionFunction;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;

import java.util.*;


/**
   * Failure refinement differs from failure equality only in the initial coloring used
   */
  public class ObsFailRefinemnet implements IOperationInfixFunction {

  @Override
  public Collection<String> getValidFlags(){
    return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR, Constant.CONGURENT);
  }

    /**
     * A method of tracking the function.
     *
     * @return The Human-Readable form of the function name
     */
    @Override
    public String getFunctionName() {
      return "ObsevationalFailureRefinement";
    }

    /**
     * The form which the function will appear when composed in the text.
     *
     * @return the textual notation of the infix function
     */
    @Override
    public String getNotation() {
      return "<fo";
    }
    @Override
    public String getOperationType(){return "automata";}
    /**
     * Evaluate the function.
     *
     * @param alpha
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
    public boolean evaluate(Set<String> alpha, Set<String> flags, Context context,
                            Stack<String> trace,
                            Collection<ProcessModel> processModels) throws CompilationException {
      boolean cong = flags.contains(Constant.CONGURENT);

      FailureEquivalence fe = new FailureEquivalence();
      AbstractionFunction abs = new AbstractionFunction();
      List<ProcessModel> as = new ArrayList<>();
      for (ProcessModel pm : processModels) {
        Automaton in = ((Automaton) pm);
        Automaton a =  abs.compose(in.getId(),flags,context,  in);
        as.add(a);
      }

      return fe.evaluate(as,false,cong);
    }
  }

