package mc.operations.functions;

import com.google.common.collect.Multiset;
import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;

import mc.Constant;
import mc.TraceType;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

public class GaloisAP2BC implements IProcessFunction {
  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)}).
   *
   * @return the name of the function
   */
  @Override
  public String getFunctionName() {
    return "fap2bc";
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
  @Override
  public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {
    System.out.println("GaloisAP2BC");
    Petrinet p = petrinets[0].reId("");
    MultiProcessModel model = GaloisBC2AP.buildmpmFromPetri(p);
    Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = model.getProcessNodesMapping().getMarkingToNode();

    return composeM(id,flags,context, markingToNode, ((Petrinet) model.getProcess(ProcessType.PETRINET)));
  }
  /**  Automata
   * Replace b^ with  R-b.t!->x, x-b?->E,
   * Replace b  with  R-b.t?->y, y-b!->E
   *
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
    //System.out.println("********ap2bc "+petrinet.myString());

    Set<PetriNetTransition> todo = petrinet.getTransitions().values().stream().collect(Collectors.toSet());
    System.out.println(todo.
      stream().map(x->x.myString()).reduce("",(x,y)->x+y+"\n"));

    for(PetriNetTransition tr: todo){
        //System.out.println("*2* "+tr.myString());
      String lab = tr.getLabel();
      if (lab.endsWith(Constant.ACTIVE)) {                //Active
        String prefix = lab.substring(0,lab.length()-1);
        PetriNetPlace x = petrinet.addPlace();
        Set<PetriNetPlace> xset = new HashSet<>();
        xset.add(x);
        petrinet.addTransition(tr.pre(), prefix + ".t"+ Constant.BROADCASTSoutput, xset );
        //petrinet.addTransition(xset, prefix + ".r?", tr.pre() );
        petrinet.addTransition(xset, prefix + Constant.BROADCASTSinput, tr.post() );
        petrinet.removeTransition(tr);
        //System.out.println("1."+petrinet.myString());
      } else if (!(lab.endsWith("!")||lab.endsWith("?"))) {  //Passive
        //String prefix = lab.substring(0,lab.length()-1);
        PetriNetPlace y = petrinet.addPlace();
        Set<PetriNetPlace> yset = new HashSet<>();
        yset.add(y);
        petrinet.addTransition(tr.pre(), lab +".t"+ Constant.BROADCASTSinput, yset );
        petrinet.addTransition(yset, lab + Constant.BROADCASTSoutput, tr.post() );
        petrinet.removeTransition(tr);
        //System.out.println("2."+petrinet.myString());
      }

    }

    //System.out.println("ap2bc end "+petrinet.myString());
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

