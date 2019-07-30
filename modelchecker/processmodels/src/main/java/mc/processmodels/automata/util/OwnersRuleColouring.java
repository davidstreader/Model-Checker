package mc.processmodels.automata.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import mc.processmodels.automata.Automaton;

import java.util.HashMap;
import java.util.Map;

public class OwnersRuleColouring {

  private static final int BASE_COLOUR = 1;
  private static int nextColour = BASE_COLOUR + 1;
  private static final Map<Integer, Map<Integer, Integer>> intersections = new HashMap<>();
  private static final Multimap<Integer, Integer> component = ArrayListMultimap.create();

  /**
   * Perform a colouring algorithm so that the Owners' Rule can differentiate NFA
   * @param a The automaton to be coloured.
   * @return the coloured automaton
   */
  public static Automaton colour(Automaton a) {
    nextColour = BASE_COLOUR + 1;
    a.getNodes().forEach(n -> n.setColour(BASE_COLOUR));
    return a;
  }
}
