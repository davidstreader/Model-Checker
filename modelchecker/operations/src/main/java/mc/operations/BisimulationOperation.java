package mc.operations;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.util.Collection;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.util.ColouringUtil;

public class BisimulationOperation implements IOperationInfixFunction {

  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "BiSimulation";
  }

  /**
   * The form which the function will appear when composed in the text.
   *
   * @return the textual notation of the infix function
   */
  @Override
  public String getNotation() {
    return "~";
  }

  /**
   * Evaluate the function.
   *
   * @param automata the list of automata being compared
   * @return the resulting automaton of the operation
   */
  @Override
  public boolean evaluate(Collection<Automaton> automata) throws CompilationException {

    Multimap<Integer, ColouringUtil.Colour> colourMap = MultimapBuilder.hashKeys()
        .arrayListValues()
        .build();
    int rootColour = Integer.MIN_VALUE;

    ColouringUtil colourer = new ColouringUtil();
    for (Automaton automaton : automata) {
      if (Thread.currentThread().isInterrupted()) {
        return false;
      }
      colourer.performColouring(automaton, colourMap);

      AutomatonNode root = automaton.getRoot();
      int colour = root.getColour();

      if (rootColour == Integer.MIN_VALUE) {
        rootColour = colour;
      } else if (rootColour != colour) {

        return false;
      }
    }


    return true;
  }

}
