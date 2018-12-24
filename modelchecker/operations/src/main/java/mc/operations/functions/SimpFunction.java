package mc.operations.functions;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;

import mc.Constant;
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

    return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR,
      Constant.CONGURENT, Constant.OBSEVATIONAL);
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
   * Execute the simp function on automata.
   *
   * flag contains obs
   *     1. reduce state space from abstraction
   *     2. copy result to Copy
   *     3. saturate by abstraction
   *     4. colour
   *     5. apply color to Copy
   *     6. simplify copy
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
    Automaton automaton = automata[0].copy(); // Deep Clone  as automata changed
    //System.out.println("\nSIMP corrects data !");
    if (flags.contains(Constant.OBSEVATIONAL)) {
      automaton.validateAutomaton();
      AbstractionFunction af = new AbstractionFunction();
      automaton =  af.absMerge(flags,context, automaton); //1. reduce state space
      Automaton copySmall = automaton.copy();             //2. copy
      copySmall.validateAutomaton();
      af.observationalSemantics(flags, automaton, context); //3. saturate
      copySmall.validateAutomaton();
      System.out.println("\nOBSSem "+automaton.myString());
      List<List<String>> partition = buildPartition(flags, automaton); //4. color
      //Applipcation of coloring to unsaturated automata

      copySmall.validateAutomaton();
      mergeNodes(copySmall, partition, context);  //5. apply color and 6. simlify
      boolean isFair = flags.contains(Constant.FAIR) || !flags.contains(Constant.UNFAIR);
      af.divergence(copySmall,isFair);  //7. tidy up
      copySmall.validateAutomaton();
      System.out.println("\ncopySmall "+copySmall.myString());
      automaton = copySmall;
    } else {
      List<List<String>> partition = buildPartition(flags, automaton);
      //the automaton is changed
      mergeNodes(automaton, partition, context);
    }
 System.out.println("Simp out "+automaton.myString()+"\n");
    return automaton;
  }

  public List<List<String>> buildPartition(Set<String> flags, Automaton automaton){
    boolean cong = flags.contains(Constant.CONGURENT);
    //the Nodes are connected to the Edges  are connected to the Nodes
    // have the nodes you have the automaton
    ArrayList<AutomatonEdge> edges = new ArrayList<>();
    ArrayList<AutomatonNode> nodes = new ArrayList<>();
    edges.addAll(automaton.getEdges());
    nodes.addAll(automaton.getNodes());
    ColouringUtil colourer = new ColouringUtil();
    Map<Integer, List<ColouringUtil.ColourComponent>> colourMap = new HashMap<>();

    colourer.performInitialColouring(nodes, cong);
    colourer.doColouring(nodes, cong);
    //System.out.println("SIMP colour "+ automaton.getId());
    Map<Integer, List<AutomatonNode>> colour2nodes = new HashMap<>();

    System.out.println("SIMP colored "+ automaton.getId());
    for (AutomatonNode nd : nodes) {
      if (colour2nodes.containsKey(nd.getColour())) {
        colour2nodes.get(nd.getColour()).add(nd);
      } else {
        colour2nodes.put(nd.getColour(), new ArrayList<AutomatonNode>(Arrays.asList(nd)));
      }
    }
    List<List<String>> out = new ArrayList<>();
    for(List<AutomatonNode> nds: colour2nodes.values()){
      out.add(nds.stream().map(x->x.getId()).collect(Collectors.toList()));
    }

   return  out;
  }


  /**
   * This method glues together nodes in the same partition
   * Used here in simplification and in abstraction
   * @param ain   input output
   * @param partition  input
   * @param context
   * @throws CompilationException
   */
  public Automaton mergeNodes(Automaton ain,
                          Collection<List<String>> partition,
                          Context context)
    throws CompilationException {
    for (Collection<String> nodesWithSameColor : partition) {
      if (nodesWithSameColor.size() < 2) {
        continue;
      }
System.out.println("Merge "+ ain.getId());
      ain.validateAutomaton("simp 1");
      System.out.println("    partition "+partition);

      //AutomatonNode mergedNode = Iterables.get(nodesWithSameColor, 0);
      boolean first = true;
      AutomatonNode selectedNode = null;
      for (String nodeName : nodesWithSameColor) {
        AutomatonNode automatonNode = ain.getNode(nodeName);
        if (first) {
          selectedNode = automatonNode;
          first = false;
          continue;
        } else {
          try {
            System.out.println("Merging "+selectedNode.getId()+" " + automatonNode.getId());
            //combineNodes will remove mergedNode and return  new merged node
            ain = ain.mergeAutNodes(ain, selectedNode, automatonNode, context);
            System.out.println("   Merged result \n");
            ain.validateAutomaton("simp 2");
          } catch (InterruptedException ignored) {
            throw new CompilationException(getClass(), "INTERRUPTED EXCEPTION");
          }
        }
      }
      //nodesWithSameColor.forEach(automaton::removeNode);
    }
    ain.cleanNodeLables();
    return ain;
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
