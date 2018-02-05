package mc.operations;

import static mc.processmodels.automata.util.ColouringUtil.ColourComponent;

import java.util.*;
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

    final int BASE_COLOUR = 1;
    Map<Integer, List<ColourComponent>> colourMap = new HashMap<>();
    int rootColour = Integer.MIN_VALUE;

    ColouringUtil colourer = new ColouringUtil();
    // coloring first automata builds colourMap  that is used when coloring next automata
    for (Automaton automaton : automata) {
      if (Thread.currentThread().isInterrupted()) {
        return false;
      }
      Map<AutomatonNode,Integer> initialColour = new HashMap<AutomatonNode,Integer>();
      for(AutomatonNode n: automaton.getNodes()){
        initialColour.put(n,BASE_COLOUR);
      }
      colourer.performColouring(automaton, colourMap,initialColour);

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
      //System.out.println(automaton.getId()+"in Bisim Op "+
     //   automaton.getRoot().stream().map(n->((Integer)n.getColour()).toString()).
      //    collect(Collectors.joining(", ")));

      if (col == Integer.MIN_VALUE) {
        col = colourer.getNextColourId();
        colourMap.put(col, colourSet);
      }

      if (rootColour == Integer.MIN_VALUE) { //first time
        rootColour = col;
      } else if (rootColour != col) {  //second time
        return false;
      }
    }

    return true;
  }

}
