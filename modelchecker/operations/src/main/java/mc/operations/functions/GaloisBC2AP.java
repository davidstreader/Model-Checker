package mc.operations.functions;

import com.google.common.collect.Multiset;
import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;

import mc.TraceType;
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

public class GaloisBC2AP implements IProcessFunction {
  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)}).
   *
   * @return the name of the function
   */
  @Override
  public String getFunctionName() {
    return "fbc2ap";
  }

  /**
   * Get the available flags for the function described by this interface (e.g. {@code unfair} in
   * {@code abs{unfair}(A)}.
   *
   * @return a collection of available flags (note, no variables may be flags)
   */
  @Override
  public Collection<String> getValidFlags() {
    return Collections.singleton("*");
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
   * @param tt
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   */
  @Override
  public Automaton compose(String id, Set<String> flags, Context context,  Automaton... automata)
    throws CompilationException {
 return null;
  }

 /*
     If called only with a petri Net  then use token rule to build reachable markings stored in MultiModel
     then call MultiModel
  */
 @Override
  public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {

   Petrinet x = petrinets[0].reId("G");
   MultiProcessModel model = buildmpmFromPetri(x);
   Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = model.getProcessNodesMapping().getMarkingToNode();
   //Map<AutomatonNode, Multiset<PetriNetPlace> > nodeToMarking = model.getProcessNodesMapping().getNodeToMarking();

   return composeM(id,flags,context,markingToNode,x);
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
  /**
   * Petri Nets  relabeltransitions b! to b^  and b? to b
   * Add listening loops to a Petri Net
   *    for each listening transition
   *       for all reachable Markings subset to location of transition
   *           add listening loop on resulting subMarkings
   *
   * @param id        the id of the resulting petrinet
   * @param flags     the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context
   * @param petrinet the variable number of petrinets taken in by the function
   * @return the resulting petrinet of the operation
   * @throws CompilationException when the function fails
   */

    public Petrinet composeM(String id, Set<String> flags, Context context, Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNode, Petrinet petrinet) throws CompilationException {
      System.out.println("GaloisBC2AP "+ petrinet.getId());


    for(PetriNetTransition tr: petrinet.getTransitions().values()){
        tr.setLabel(reLabel(tr.getLabel()));
      //System.out.println("Relabeled "+tr.myString());
    }
/*       Add listening loops to a Petri Net?
   build a mapping from  listening label 2 owners
   For each marking
         find set of satisfied listening transitions
         foreach listening label with no transition enabled
            add transition loop from places owned by lablel

 */
//build a mapping from  listening label 2 owners
      Map<String, Set<String>> listener2Owners = buildListener2Owners(petrinet);

//  For each marking
   for (Multiset<PetriNetPlace> mark: markingToNode.keySet()){
     //System.out.println("** Marking "+Petrinet.marking2String(mark));
//      find set of satisfied listening transitions
     Set<String> trlist =TokenRule.satisfiedTransitions(mark).
                                 stream().filter(x->!x.getLabel().endsWith("^")).
                                 map(x->x.getLabel()).collect(Collectors.toSet());
     //System.out.println("** trlist "+trlist);
     // set of listening labels with no transitions enabled
     Set<String> trNotlist = listener2Owners.keySet().
       stream().filter(x->!trlist.contains(x)).collect(Collectors.toSet());
     //find the Places no owned by listening label
     //System.out.println("** trNotList "+trNotlist);
     for(String lab: trNotlist){
       Set<PetriNetPlace> trprenew = mark.stream().
         filter(x->listener2Owners.get(lab).containsAll(x.getOwners())).
         collect(Collectors.toSet());
       if(trprenew.size()>0) {
         PetriNetTransition t = petrinet.addTransition(trprenew,lab,trprenew);
         //System.out.println("**** "+t.myString());
       } else {
         System.out.println("ownership ERROR add "+lab+ " transition");
       }
    }

   }
    return   petrinet;
  }


  public static Map<String, Set<String>> buildListener2Owners(Petrinet petrinet) {
    //Set<String>  listeners = new HashSet<>();
    Map<String, Set<String>> listener2Owners = new HashMap<>();
    for(PetriNetTransition tr: petrinet.getTransitions().values().
      stream().filter(x->!x.getLabel().endsWith("^")).
      collect(Collectors.toSet())) {
      //System.out.println("*** listener "+tr.myString());
      if (listener2Owners.keySet().contains(tr.getLabel())) {
        if (!listener2Owners.get(tr.getLabel()).equals(tr.getOwners())) {
          System.out.println("ERROR in owners of transitions labeled "+tr.getLabel()+
            listener2Owners.get(tr.getLabel())+ " "+ tr.getOwners()
          );
        }
      } else {
        listener2Owners.put(tr.getLabel(),tr.getOwners());
        //listeners.add(tr.getLabel());
        //System.out.println("*** put ("+tr.getLabel()+","+tr.getOwners()+")");
      }
    }
    return listener2Owners;
  }

  private Set<PetriNetTransition> noListener(Petrinet petrinet, Multiset<PetriNetPlace> mark){

    return petrinet.getTransitions().values().stream()
      .filter(x->( x.getLabel().endsWith("?") && !mark.containsAll(x.pre())))
      .collect(Collectors.toSet());
  }
  private String reLabel(String ac){

    String back;
    if (ac.endsWith("!"))
      back = ac.substring(0,ac.length()-1)+"^";
    else if (ac.endsWith("?")) {
      back = ac.substring(0, ac.length() - 1);
    } else {
      back = ac;
    }
    //System.out.println("reLabel "+ ac+" to "+ back);
    return back;
  }
/*
    This is needed to obtain the pre computed markingToNode
 */

  @Override
  public MultiProcessModel compose(String id, Set<String> flags, Context context, MultiProcessModel... multiProcess) throws CompilationException {
    MultiProcessModel model = multiProcess[0].reId("Gal");
   Petrinet petrinet = (Petrinet) multiProcess[0].getProcess(ProcessType.PETRINET);
    Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNode =
          multiProcess[0].getProcessNodesMapping().getMarkingToNode();
    this.composeM(id,flags,context,markingToNode,petrinet);
    return null;
  }
}

