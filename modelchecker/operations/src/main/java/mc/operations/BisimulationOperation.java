package mc.operations;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
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
    Set<Integer> rootColour = Collections.emptySet();

    ColouringUtil colourer = new ColouringUtil();
    for (Automaton automaton : automata) {
      if (Thread.currentThread().isInterrupted()) {
        return false;
      }
      colourer.performColouring(automaton, colourMap);

      Set<AutomatonNode> root = automaton.getRoot();

      Set<Integer> colourSet = root.stream()
          .map(AutomatonNode::getColour)
          .collect(Collectors.toSet());

      if (rootColour.isEmpty()) {
        rootColour = colourSet;
      } else if (!rootColour.equals(colourSet)) {
        return false;
      }
    }

    return true;
  }

}
