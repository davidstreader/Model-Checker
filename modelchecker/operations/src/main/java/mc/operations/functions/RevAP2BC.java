package mc.operations.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;

import mc.Constant;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.Mapping;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

/*
Need to add this as a function on  MultiProcessModel if going to reuse the Markings
shortcut is to recompute reachability

 */

public class RevAP2BC implements IProcessFunction {
  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)}).
   *
   * @return the name of the function
   */
  @Override
  public String getFunctionName() {
    return "revap2bc";
  }

  /**
   * Get the available flags for the function described by this interface (e.g. {@code unfair} in
   * {@code abs{unfair}(A)}.
   *
   * @return a collection of available flags (note, no variables may be flags)
   */
  @Override
  public Collection<String> getValidFlags() {
    return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR, Constant.CONGURENT);
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
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   */
  @Override
  public Automaton compose(String id, Set<String> flags, Context context, Automaton... automata)
    throws CompilationException {
    return null;
  }

  /*  Add lisening Loops - for events in flags
      abstract all *.t! and *.t?
      convert b? to b^ and b! to b
   */
   @Override
  public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {

    Petrinet x = petrinets[0].reId("G");
    MultiProcessModel model = buildmpmFromPetri(x);
    Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = model.getProcessNodesMapping().getMarkingToNode();
    //Map<AutomatonNode, Multiset<PetriNetPlace> > nodeToMarking = model.getProcessNodesMapping().getNodeToMarking();

    return composeM(id,flags,context,markingToNode,x);
  }

  public Petrinet composeM(String id, Set<String> flags, Context context,
                           Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNode,
                           Petrinet petrinet) throws CompilationException {
   //Petrinet petrinet = petrinets[0].reId("Rev") ;
    System.out.println("RevAP2BC start "+petrinet.getId()+ " flags "+flags);
    Set<String> listeners = flags.stream().filter(x->x.endsWith("?")).collect(Collectors.toSet());
    buildListeningLoops(markingToNode,listeners,petrinet);

    for(PetriNetTransition tr : petrinet.getTransitions().values()) {
     String prefix1 = tr.getLabel().substring(0,tr.getLabel().length()-1);
     if (tr.getLabel().endsWith(".t?") ||
         tr.getLabel().endsWith(".t!")    ) tr.setLabel(Constant.HIDDEN);
     else if (tr.getLabel().endsWith("?") ) tr.setLabel(prefix1+Constant.ACTIVE);
     else if (tr.getLabel().endsWith("!") ) tr.setLabel(prefix1);

   }
    // petrinet = ab.compose(id,flags,context,p);
    System.out.println(petrinet.myString()+ "Rev end ");
    return petrinet;
  }
  public static  void buildListeningLoops(Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNode,
                                          Set<String> listeners, Petrinet petrinet)
    throws CompilationException{
    //  For each marking
    for (Multiset<PetriNetPlace> markM: markingToNode.keySet()){
      Set<PetriNetPlace> mark = markM.elementSet();
      System.out.println("** Marking "+Petrinet.marking2String(mark));
//      find set of satisfied listening transitions
      Set<String> trlist =TokenRule.satisfiedTransitions(markM).
        stream().filter(x->!x.getLabel().endsWith("^")).
        map(x->x.getLabel()).collect(Collectors.toSet());
      System.out.println("** trlist "+trlist);
      // set of listening labels with no transitions enabled
      Set<String> trNotlist = listeners.
        stream().filter(x->!trlist.contains(x)).collect(Collectors.toSet());
      //find the Places no owned by listening label
      System.out.println("** trNotList "+trNotlist);
      for(String lab: trNotlist){
          PetriNetTransition t = petrinet.addTransition(mark,lab,mark);
          System.out.println("**adding** "+t.myString());
      }
    }

  }

  /* CODE COPIED FROM Interpreter */
  public static MultiProcessModel buildmpmFromPetri(Petrinet pet){

    MultiProcessModel model = new MultiProcessModel(pet.getId());
    //System.out.println("Interpreter Built Petri "+ modelPetri.getId());
    model.addProcess(pet);
    HashMap<AutomatonNode, Multiset<PetriNetPlace>> nodeToMarking = new HashMap<>();
    HashMap<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = new HashMap<>();

    ProcessModel modelAut = TokenRule.tokenRule(
      (Petrinet) model.getProcess(ProcessType.PETRINET), markingToNode, nodeToMarking);

    model.addProcess(modelAut);
    model.addProcessesMapping(new Mapping(nodeToMarking, markingToNode));
    return model;
  }

/*
    This is needed to obtain the pre computed markingToNode
 */

  @Override
  public MultiProcessModel compose(String id, Set<String> flags, Context context, MultiProcessModel... multiProcess) throws CompilationException {

    return null;
  }
}

