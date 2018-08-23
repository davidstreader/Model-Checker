package mc.operations.functions;

import com.google.common.collect.Iterables;
import com.microsoft.z3.Context;

import java.util.*;

import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.util.ColouringUtil;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.operations.PetrinetSimp;

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

   ArrayList<AutomatonEdge> edges = new ArrayList<>();
   ArrayList<AutomatonNode> nodes = new ArrayList<>();

    edges.addAll(automaton.getEdges());
    nodes.addAll(automaton.getNodes());

    ColouringUtil colourer = new ColouringUtil();
    Map<Integer, List<ColouringUtil.ColourComponent>> colourMap = new HashMap<>();

    colourer.performInitialColouring(nodes);
    colourer.doColouring(nodes);
    //System.out.println("SIMP colour "+ automaton.getId());

   Map<Integer,List<AutomatonNode>> colour2nodes = new HashMap<>();

//System.out.println("SIMP merge "+ automaton.getId());
     for(AutomatonNode nd: nodes) {
       if (colour2nodes.containsKey(nd.getColour()) ) {
        colour2nodes.get(nd.getColour()).add(nd);
       } else {
        colour2nodes.put(nd.getColour(), new ArrayList<AutomatonNode>(Arrays.asList(nd)));
       }
     }


   for (Collection<AutomatonNode> value : colour2nodes.values()) {
      if (value.size() < 2) {
        continue;
      }
//System.out.println("Simp "+ automaton.getId());
      AutomatonNode mergedNode = Iterables.get(value, 0);

      for (AutomatonNode automatonNode : value) {
        if (automatonNode.equals(mergedNode)) {continue;};
        try {
 //System.out.println("Merging "+mergedNode.getId()+" " + automatonNode.getId());
          mergedNode = automaton.combineNodes(mergedNode, automatonNode, context);
        } catch (InterruptedException ignored) {
          throw new CompilationException(getClass(), "INTERRUPTED EXCEPTION");
        }
      }

      value.forEach(automaton::removeNode);
    }
      //System.out.println("Simp out "+automaton.myString()+"\n");
    return automaton;
  }

  /**
   * TODO:
   * Execute the function on one or more petrinet.
   *
   * @param id        the id of the resulting petrinet
   * @param flags     the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context
   * @param petrinets the variable number of petrinets taken in by the function
   * @return the resulting petrinet of the operation
   * @throws CompilationException when the function fails
   */
  @Override
  public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {
    assert petrinets.length == 1;

    return PetrinetSimp.colSimp(petrinets[0].copy());
  }
    @Override
    public MultiProcessModel compose(String id, Set<String> flags, Context context, MultiProcessModel... multiProcess) throws CompilationException {
        return null;
    }
}
