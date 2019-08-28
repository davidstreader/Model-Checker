package mc.operations.functions;

import com.google.common.collect.Multiset;
import com.microsoft.z3.Context;
import mc.Constant;
import mc.compiler.Guard;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.MappingNdMarking;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.conversion.OwnersRule;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;

import java.util.*;
import java.util.stream.Collectors;

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
   *
   * flags should be the set of pasive events that need to be considered {a,d}
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
   * Automata
   * Replace b with  R-b.t?->x,  x-b!->E,
   * Replace R-b^->E  with  R-b.t!->y,y-b.r?->R, y-b?->E
   * If N-/b^-> then    Add N-b.t?->x, x-b.r!->N
   * so b.t? always enabled
   * * Execute the function on automata.
   *
   * @param id       the id of the resulting automaton
   * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   */
  @Override
  public Automaton compose(String id, Set<String> flags, Context context, Automaton... automata)
    throws CompilationException {
    Automaton aut = automata[0].copy();
    int newLabelNode = aut.getNodeCount()+1;
    Set<String> owns = aut.getOwners();
    if (owns.size() != 1) {
      System.out.println("Warning GALOIS on automata with owners " + owns+" newNode "+newLabelNode);
    }
    //aut.reId("fA2B");
    //aut = aut.copy();
    //System.out.println("\n ******** AP2BC input " + aut.myString());


    Set<String> alph = aut.getAlphabet().stream().filter(x -> x.endsWith(Constant.ACTIVE)).collect(Collectors.toSet());

    for (AutomatonNode nd : aut.getNodes()) {
      for (String lb : flags) {
        String lab = lb+Constant.ACTIVE;
        boolean found = false;
        for (AutomatonEdge ed : nd.getOutgoingEdges()) {
          //System.out.println("  edge "+ed.myString());
          if (ed.getLabel().equals(lab)) {
            found = true;
            break;
          }
        }
        //System.out.println("found = "+found);
        if (!found) {
          String prefix = lab.substring(0, lab.length() - 1);
          //System.out.println("Pingo");
          AutomatonNode x = aut.addNode();
          x.setLabelNumber(newLabelNode++);
          AutomatonEdge edt =
            aut.addEdge(prefix + ".t" + Constant.BROADCASTSinput, nd, x, new Guard(), false, false);
          edt.setEdgeOwners(owns);
          AutomatonEdge edr =
            aut.addEdge(prefix + ".r" + Constant.BROADCASTSoutput, x, nd, new Guard(), false, false);
          edr.setEdgeOwners(owns);
          //System.out.println("Built " +edt.myString()+"   "+edr.myString());
        }
      }
    }
    //System.out.println("HALF WAY " + aut.myString() + "\n");

    Set<AutomatonEdge> todo = aut.getEdges().stream().collect(Collectors.toSet());
    /*System.out.println(todo.
      stream().map(x -> x.myString()).reduce("", (x, y) -> x + y + "\n")); */

    for (AutomatonEdge edge : todo) {
      //System.out.println("*1* " + edge.myString());
      String lab = edge.getLabel();
      if (lab.endsWith(Constant.BROADCASTSinput) ||
        lab.endsWith(Constant.BROADCASTSoutput) ||
        lab.equals(Constant.HIDDEN)) continue;  // preserve any broadcast events
      //This allows mixed AP and BC events
      //System.out.println("*2* " + edge.myString());
      String prefix;
      AutomatonNode x = aut.addNode();
      x.setLabelNumber(newLabelNode++);
      if (lab.endsWith(Constant.ACTIVE)) { //Active
        //x.copyProperties(tr.preOne());
        prefix = lab.substring(0, lab.length() - 1);
        AutomatonEdge edtx =
          aut.addEdge(prefix + ".t" + Constant.BROADCASTSinput, edge.getFrom(), x, new Guard(), false, false);
        edtx.setEdgeOwners(owns);
        AutomatonEdge edx =
          aut.addEdge(prefix +".a"+ Constant.BROADCASTSoutput, x, edge.getTo(), new Guard(), false, false);
        edx.setEdgeOwners(owns);
        aut.removeEdge(edge);
        //System.out.println("1."+petrinet.myString());
      } else {  //Passive
        prefix = lab;
        AutomatonEdge edt =
          aut.addEdge(prefix + ".t" + Constant.BROADCASTSoutput, edge.getFrom(), x, new Guard(), false, false);
        edt.setEdgeOwners(owns);
        AutomatonEdge edx =
          aut.addEdge(prefix +".a"+ Constant.BROADCASTSinput, x, edge.getTo(), new Guard(), false, false);
        edx.setEdgeOwners(owns);
        AutomatonEdge edr =
          aut.addEdge(prefix + ".r" + Constant.BROADCASTSinput, x, edge.getFrom(), new Guard(), false, false);
        edr.setEdgeOwners(owns);
        aut.removeEdge(edge);
        //System.out.println("2."+petrinet.myString());
      }

    }
    aut.cleanNodeLables();
    System.out.println("GaloisAP2BC Returns " + aut.myString());
    return aut;
  }

  @Override
  public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {
    //System.out.println("GaloisAP2BC Petriten2Automata");
    Automaton a = TokenRule.tokenRule(petrinets[0]);
    Automaton[] auts = new Automaton[1];
    auts[0] = a;
    Automaton aout = compose(id, flags, context, auts);

    return OwnersRule.ownersRule(aout);
  }


  @Override
  public MultiProcessModel compose(String id, Set<String> flags, Context context, MultiProcessModel... multiProcess) throws CompilationException {
    MultiProcessModel model = multiProcess[0].reId("Gal");
    Petrinet petrinet = (Petrinet) multiProcess[0].getProcess(ProcessType.PETRINET);
    Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNode =
      multiProcess[0].getProcessNodesMapping().getMarkingToNode();
  //  this.composeM(id, flags, context, markingToNode, petrinet);
    return model;
  }

  public static MultiProcessModel buildmpmFromPetri(Petrinet pet) {

    MultiProcessModel model = new MultiProcessModel(pet.getId());
    //System.out.println("Interpreter Built Petri "+ modelPetri.getId());
    model.addProcess(pet);
    HashMap<AutomatonNode, Multiset<PetriNetPlace>> nodeToMarking = new HashMap<>();
    HashMap<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = new HashMap<>();

    ProcessModel modelAut = TokenRule.tokenRule(
      (Petrinet) model.getProcess(ProcessType.PETRINET), markingToNode, nodeToMarking);

    model.addProcess(modelAut);
    model.addProcessesMapping(new MappingNdMarking(nodeToMarking, markingToNode, pet.getId(),modelAut.getId()));
    return model;
  }

}

