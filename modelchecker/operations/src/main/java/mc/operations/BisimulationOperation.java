package mc.operations;

import static mc.processmodels.automata.util.ColouringUtil.ColourComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    Map<Integer, List<ColourComponent>> colourMap = new HashMap<>();
    int rootColour = Integer.MIN_VALUE;

    ColouringUtil colourer = new ColouringUtil();
    for (Automaton automaton : automata) {
      if (Thread.currentThread().isInterrupted()) {
        return false;
      }
      colourer.performColouring(automaton, colourMap);

      Set<AutomatonNode> root = automaton.getRoot();

      List<ColourComponent> colourSet = root.stream()
          .map(AutomatonNode::getColour)
          .map(colourMap::get)
          .filter(Objects::nonNull)
          .flatMap(List::stream)
          .distinct()
          .sorted()
          .collect(Collectors.toList());

//      System.out.println(automaton.getId());
//      System.out.println(colourSet);

      int col = Integer.MIN_VALUE;

      for (Map.Entry<Integer, List<ColourComponent>> colour : colourMap.entrySet()) {
        List<ColourComponent> colSet = new ArrayList<>(colour.getValue());
        Collections.sort(colSet);

        if (colSet.equals(colourSet)) {
          col = colour.getKey();
          break;
        }
      }
//      System.out.println(col);

      if (col == Integer.MIN_VALUE) {
        col = colourer.getNextColourId();
        colourMap.put(col, colourSet);
      }

      if (rootColour == Integer.MIN_VALUE) {
        rootColour = col;
      } else if (rootColour != col) {
        return false;
      }
    }

    return true;
  }

}
