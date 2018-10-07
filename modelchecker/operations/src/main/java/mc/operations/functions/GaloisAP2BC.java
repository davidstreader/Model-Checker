package mc.operations.functions;

import com.google.common.collect.Multiset;
import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;

import mc.Constant;
import mc.TraceType;
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
import mc.processmodels.conversion.TokenRule;
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

  /**  Automata
   * Replace b with  R-b.t?->x,  x-b!->E,
   * Replace R-b^->E  with  R-b.t!->y,y-b.r?->R, y-b?->E
   * If N-/b^-> then    Add N-b.t?->x, x-b.r!->N
   *   so b.t? always enabled
   * * Execute the function on automata.
   *
   * @param id       the id of the resulting automaton
   * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   */
  @Override
  public Automaton compose(String id, Set<String> flags, Context context,  Automaton... automata)
    throws CompilationException {
    Automaton aut = automata[0];
    System.out.println("********ap2bc AUTOMATON "+aut.myString());
    for (AutomatonNode nd: aut.getNodes()) {
      for (String lab : aut.getAlphabet()) {
        if (lab.endsWith(Constant.ACTIVE))  continue;
        //String prefix = lab.substring(0,lab.length()-1);
        boolean found = false;
        for (AutomatonEdge ed : nd.getOutgoingEdges()) {
          if (ed.getLabel().equals(lab)) {
            found = true;
            break;
          }
        }
        if (!found) {
          AutomatonNode x = aut.addNode();
          aut.addEdge(lab + ".t"+ Constant.BROADCASTSinput, nd,  x,new Guard(),false,false );
          aut.addEdge(lab + ".r"+ Constant.BROADCASTSoutput, x, nd,new Guard(),false,false );
        }
      }
    }

    Set<AutomatonEdge> todo = aut.getEdges().stream().collect(Collectors.toSet());
    System.out.println(todo.
      stream().map(x->x.myString()).reduce("",(x,y)->x+y+"\n"));

    for(AutomatonEdge edge: todo){
      //System.out.println("*2* "+tr.myString());
      String lab = edge.getLabel();
      String prefix = lab;
      AutomatonNode x = aut.addNode();

      if (lab.endsWith(Constant.ACTIVE)) { //Active
        //x.copyProperties(tr.preOne());
        prefix = lab.substring(0,lab.length()-1);
        aut.addEdge(prefix + ".t"+ Constant.BROADCASTSinput, edge.getFrom(),  x,new Guard(),false,false );
        aut.addEdge(prefix + Constant.BROADCASTSoutput, x, edge.getTo(),new Guard(),false,false );
        aut.removeEdge(edge);
        //System.out.println("1."+petrinet.myString());
      } else  {  //Passive
        aut.addEdge(prefix + ".t"+ Constant.BROADCASTSoutput, edge.getFrom(),  x,new Guard(),false,false );
        aut.addEdge(prefix + Constant.BROADCASTSinput, x, edge.getTo(),new Guard(),false,false );
        aut.addEdge(prefix + ".r"+ Constant.BROADCASTSinput, x, edge.getFrom(),new Guard(),false,false );
        aut.removeEdge(edge);
        //System.out.println("2."+petrinet.myString());
      }

    }
    System.out.println("GaloisAP2BC Returns "+aut.myString());
    return aut;
  }
  @Override
  public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {
    System.out.println("GaloisAP2BC PETRINET");

    //Automaton a = TokenRule.tokenRule(petrinets[0]); // IN buildFromPetri
    Petrinet p = petrinets[0].reId("");
    MultiProcessModel model = buildmpmFromPetri(p);
    Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = model.getProcessNodesMapping().getMarkingToNode();

    return composeM(id,flags,context, markingToNode, ((Petrinet) model.getProcess(ProcessType.PETRINET)));
  }
  /**  Automata
   * Replace b with  R-b.t!->x,  x-b?->E,
   * Replace R-b^->E  with  R-b.t?->y,y-b.r!->R, y-b!->E
   * If N-/b^-> then    Add N-b.t?->x, x-b.r!->N
   *   so b.t? always enabled
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
    System.out.println("********ap2bc PETRI "+petrinet.myString());
   for (PetriNetPlace pl: petrinet.getPlaces().values()){
  // only for
   }

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
        //x.copyProperties(tr.preOne());
        petrinet.addTransition(tr.pre(), prefix + ".t"+ Constant.BROADCASTSinput, xset );
        petrinet.addTransition(xset, prefix + ".r"+ Constant.BROADCASTSoutput, tr.pre() );
        //petrinet.addTransition(xset, prefix + ".r?", tr.pre() );
        petrinet.addTransition(xset, prefix + Constant.BROADCASTSoutput, tr.post() );
        petrinet.removeTransition(tr);
        //System.out.println("1."+petrinet.myString());
      } else if (!(lab.endsWith("!")||lab.endsWith("?"))) {  //Passive
        //String prefix = lab.substring(0,lab.length()-1);
        PetriNetPlace y = petrinet.addPlace();
        Set<PetriNetPlace> yset = new HashSet<>();
        yset.add(y);
        //y.copyProperties(tr.preOne());
        petrinet.addTransition(tr.pre(), lab +".t"+ Constant.BROADCASTSoutput, yset );
        petrinet.addTransition(yset, lab + Constant.BROADCASTSinput, tr.post() );
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

  public static MultiProcessModel buildmpmFromPetri(Petrinet pet){

    MultiProcessModel model = new MultiProcessModel(pet.getId());
    //System.out.println("Interpreter Built Petri "+ modelPetri.getId());
    model.addProcess(pet);
    HashMap<AutomatonNode, Multiset<PetriNetPlace>> nodeToMarking = new HashMap<>();
    HashMap<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = new HashMap<>();

    ProcessModel modelAut = TokenRule.tokenRule(
      (Petrinet) model.getProcess(ProcessType.PETRINET), markingToNode, nodeToMarking);

    model.addProcess(modelAut);
    model.addProcessesMapping(new MappingNdMarking(nodeToMarking, markingToNode));
    return model;
  }

}

