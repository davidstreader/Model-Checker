package mc.plugins;

import java.util.Collection;
import java.util.Set;
import com.microsoft.z3.Context;
import mc.exceptions.CompilationException;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.petrinet.Petrinet;

public interface IOperationInfixFunction {

  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  String getFunctionName();

  /**
   * The form which the function will appear when composed in the text.
   *
   * @return the textual notation of the infix function
   */
  String getNotation();

  /**
   * Interpreter uses this now evaluate requires generic ProcedssModel
   * @return  the type of the operation
   */
  String getOperationType();

  /**
   * Evaluate the function.
   *
   * @param thing the automata or PetriNet in the function (e.g. {@code A} and {@code B} in {@code A ~ B})
   * @return whether or not the automata provided pass the testing semantics.
   */
  boolean  evaluate(Set<String> flags,
                    Context context,Collection<ProcessModel> thing/*automata*/) throws CompilationException;

  // boolean evaluate(Collection<Petrinet> petrinets) throws CompilationException;
}
