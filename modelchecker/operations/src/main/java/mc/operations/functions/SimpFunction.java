package mc.operations.functions;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.microsoft.z3.Context;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.util.ColouringUtil;

public class SimpFunction implements IProcessFunction {
  private static final int BASE_COLOUR = 1;
  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)}).
   *
   * @return the name of the function
   */
  @Override
  public String getFunctionName() {
    return "simp";
  }

  /**
   * Get the available flags for the function described by this interface (e.g. {@code unfair} in
   * {@code abs{unfair}(A)}.
   *
   * @return a collection of available flags (note, no variables may be flags)
   */
  @Override
  public Collection<String> getValidFlags() {
    return Collections.singletonList("*");
  }

  /**
   * Gets the number of automata to parse into the function.
   *
   * @return the number of arguments
   */
  @Override
  public int getNumberArguments() {
    return 1;
  }

  /**
   * Execute the function on automata.
   *
   * @param id       the id of the resulting automaton
   * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context  the Z3 context to execute expressions
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   * @throws CompilationException when the function fails
   */
  @Override
  public Automaton compose(String id, Set<String> flags, Context context, Automaton... automata)
      throws CompilationException {

    assert automata.length == 1;

    //Clone
    Automaton automaton = automata[0].copy();

    ColouringUtil colourer = new ColouringUtil();
    Map<Integer, List<ColouringUtil.ColourComponent>> colourMap = new HashMap<>();
    Map<AutomatonNode,Integer> initialColour = new HashMap<AutomatonNode,Integer>();
    for(AutomatonNode n: automaton.getNodes()){
      initialColour.put(n,BASE_COLOUR);
    }
    Multimap<Integer, AutomatonNode> nodeColours = colourer.performColouring(automaton, colourMap,initialColour);

    for (Collection<AutomatonNode> value : nodeColours.asMap().values()) {
      if (value.size() < 2) {
        continue;
      }
//System.out.println("Simp "+ automaton.toString());
      AutomatonNode mergedNode = Iterables.get(value, 0);

      for (AutomatonNode automatonNode : value) {
        if (automatonNode.equals(mergedNode)) {continue;};
        try {
          mergedNode = automaton.combineNodes(mergedNode, automatonNode, context);
        } catch (InterruptedException ignored) {
          throw new CompilationException(getClass(), "INTERRUPTED EXCEPTION");
        }
      }

      value.forEach(automaton::removeNode);
    }
    return automaton;
  }
}
