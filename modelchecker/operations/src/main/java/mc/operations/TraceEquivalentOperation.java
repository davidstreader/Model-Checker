package mc.operations;

import java.util.*;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Context;
import mc.Constant;
import mc.exceptions.CompilationException;
import mc.operations.functions.Nfa2dfaHS;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;

public class TraceEquivalentOperation implements IOperationInfixFunction {
  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "TraceEquivalent";
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
    return "=t";
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
    int ii = 0; String firstId = "";
    for (ProcessModel pm : processModels) {
      //System.out.println("  Trace "+ii+"  "+pm.getId());
      if (ii==0) firstId = pm.getId();
      else if (firstId.equals(pm.getId())) {
        //System.out.println("automata Trace same ids "+firstId);
        return true;
      }
      ii++; //Need this check
    }

    if (processModels.iterator().next() instanceof Automaton) {
      Nfa2dfaHS func = new Nfa2dfaHS();

      ArrayList<ProcessModel> nfas = new ArrayList<>();
      for (ProcessModel pm : processModels) {
        Automaton a = (Automaton) pm;
        try {
          nfas.add(
                  func.compose(a.getId(), new HashSet<>(), null,  a)
          );
        } catch (CompilationException e) {
          //System.out.println("PINGO" + e.toString());
        }
      }
      BisimulationAutomata bo = new BisimulationAutomata();
      boolean r = bo.evaluate(new TreeSet<>(), flags,context,  nfas);

   /*
    return new BisimulationOperation().evaluate(automata.stream().map(a -> {
      try {
        return func.compose(a.getId(), new HashSet<>(), null, a);
      } catch (CompilationException e) {
        return null;
      }
    }).filter(Objects::nonNull).collect(Collectors.toList()));

*/

      return r;
    }
    System.out.printf("\nTrace semantics not defined for type " + processModels.iterator().next().getClass()+"\n");
    return false;
  }
}
