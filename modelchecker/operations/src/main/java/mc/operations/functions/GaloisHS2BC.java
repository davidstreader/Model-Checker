package mc.operations.functions;

import com.google.common.collect.Multiset;
import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;

import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

public class GaloisHS2BC implements IProcessFunction {
  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)}).
   *
   * @return the name of the function
   */
  @Override
  public String getFunctionName() {
    return "fhs2bc";
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
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   */
  @Override
  public Automaton compose(String id, Set<String> flags, Context context, Automaton... automata)
    throws CompilationException {


    return null;
  }
  @Override
  public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {
    System.out.println("GaloisAP2BC");
    Petrinet p = petrinets[0].reId("");
    MultiProcessModel model = GaloisBC2AP.buildmpmFromPetri(p);
    Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = model.getProcessNodesMapping().getMarkingToNode();

    return composeM(id,flags,context, markingToNode, ((Petrinet) model.getProcess(ProcessType.PETRINET)));
  }
  /**  Automata
   * Replace b^ with  R-bt!->x, x-ba?->E, x-br?->R
   * Replace b  with  R-bt?->y, y-ba!->E
   *  if b notin pi(n)  add  n-bt?->z, z-br!->n
   *      Last requirement on PetriNets is a refinment of ading "listening loops"
   *
   * @param id        the id of the resulting petrinet
   * @param flags     the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context
   * @param petrinets the variable number of petrinets taken in by the function
   * @return the resulting petrinet of the operation
   * @throws CompilationException when the function fails
   */

  public Petrinet composeM(String id, Set<String> flags, Context context, Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNode, Petrinet... petrinets) throws CompilationException {
    Petrinet petrinet = petrinets[0];
    System.out.println("********ap2bc "+petrinet.myString());
    Set<String> alphabetListen = petrinet.getAlphabet().keySet().stream().filter(x->x.endsWith("?")).collect(Collectors.toSet());

    Map<String, Set<String>> listener2Owners = GaloisBC2AP.buildListener2Owners(petrinet);

    //  For each marking
    Set<Multiset<PetriNetPlace>> marks = markingToNode.keySet();
    for (Multiset<PetriNetPlace> mark: marks) {
      System.out.println("** Marking " + Petrinet.marking2String(mark));
//      find set of satisfied listening transitions
      Set<String> trlist = TokenRule.satisfiedTransitions(mark).
        stream().filter(x -> !(x.getLabel().endsWith("^") )).
        map(x -> x.getLabel()).collect(Collectors.toSet());
      System.out.println("** trlist " + trlist);
      // set of listening labels with no transitions enabled
      Set<String> trNotlist = listener2Owners.keySet().
        stream().filter(x -> !trlist.contains(x)).collect(Collectors.toSet());
      //find the Places no owned by listening label
      System.out.println("** trNotList " + trNotlist);
      for (String lab : trNotlist) {
        System.out.println("**** lab " + lab);
        Set<PetriNetPlace> trprenew = mark.stream().
          filter(x -> listener2Owners.get(lab).containsAll(x.getOwners())).
          collect(Collectors.toSet());
        System.out.println("**** " + trprenew.stream().map(x -> x.getId() + " " + x.getOwners() + " ")
          .reduce("", (x, y) -> x + y));
        if (trprenew.size() > 0) {
          PetriNetPlace z = petrinet.addPlace();
          Set<PetriNetPlace> zset = new HashSet<>();
          zset.add(z);
          PetriNetTransition t1 = petrinet.addTransition(trprenew, lab+".t?", zset);
          PetriNetTransition t2 = petrinet.addTransition(zset, lab+".r!", trprenew);
          System.out.println("**** " + t1.myString());
        } else {
          System.out.println("ownership ERROR add " + lab + " transition");
        }
      }
    }

 /*   //  For each marking
    for (Multiset<PetriNetPlace> mark: markingToNode.keySet()){
//      find set of satisfied listening transitions
      Set<String> trlist =TokenRule.satisfiedTransitions(mark).
        stream().filter(x->x.getLabel().endsWith("?")).
        map(x->x.getLabel()).collect(Collectors.toSet());
      // set of listening labeles with no transitions enabled
      Set<String> trNotlist = listeners.stream().filter(x->!trlist.contains(x)).collect(Collectors.toSet());
      //find the Places no owned by listening label

      for(String lab: trNotlist){
        Set<PetriNetPlace> trprenew = mark.stream().
          filter(x->listener2Owners.get(lab).containsAll(x.getOwners())).
          collect(Collectors.toSet());
        if(trprenew.size()>0) {
          PetriNetPlace z =  petrinet.addPlace();
          Set<PetriNetPlace> zset = new HashSet<>();
          zset.add(z);
          petrinet.addTransition(trprenew,lab,zset);
        }
      }

*/

    Set<PetriNetTransition> todo = petrinet.getTransitions().values().stream().collect(Collectors.toSet());
    System.out.println(todo.
      stream().map(x->x.myString()).reduce("",(x,y)->x+y+"\n"));

    for(PetriNetTransition tr: todo){
      System.out.println("*2* "+tr.myString());
      String lab = tr.getLabel();
      if (lab.endsWith("^")) {
        String prefix = lab.substring(0,lab.length()-1);
        PetriNetPlace x = petrinet.addPlace();
        Set<PetriNetPlace> xset = new HashSet<>();
        xset.add(x);
        petrinet.addTransition(tr.pre(), prefix + ".t!", xset );
        petrinet.addTransition(xset, prefix + ".r?", tr.pre() );
        petrinet.addTransition(xset, prefix + "?", tr.post() );
        petrinet.removeTransition(tr);
        System.out.println("1."+petrinet.myString());
      } else if (!(lab.endsWith("!")||lab.endsWith("?"))) {
        //String prefix = lab.substring(0,lab.length()-1);
        PetriNetPlace y = petrinet.addPlace();
        Set<PetriNetPlace> yset = new HashSet<>();
        yset.add(y);
        petrinet.addTransition(tr.pre(), lab +".t?", yset );
        petrinet.addTransition(yset, lab +"!", tr.post() );
        petrinet.removeTransition(tr);
        System.out.println("2."+petrinet.myString());
      }

    }

    System.out.println(petrinet.getId()+" is valid "+petrinet.validatePNet());
    System.out.println("ap2bc end "+petrinet.myString());
    return   petrinet;

  }

  @Override
  public MultiProcessModel compose(String id, Set<String> flags, Context context, MultiProcessModel... multiProcess) throws CompilationException {
    MultiProcessModel model = multiProcess[0].reId("Gal");
    Petrinet petrinet = (Petrinet) multiProcess[0].getProcess(ProcessType.PETRINET);
    Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNode =
      multiProcess[0].getProcessNodesMapping().getMarkingToNode();
    this.composeM(id,flags,context,markingToNode,petrinet);
    return model;
  }
}


