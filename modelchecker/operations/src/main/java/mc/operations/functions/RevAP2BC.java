package mc.operations.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;
import com.microsoft.z3.Context;
import mc.Constant;
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
import mc.processmodels.conversion.TokenRulePureFunctions;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    return ImmutableSet.of("*");
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

   *
   * @param id       the id of the resulting automaton
   * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   */
  @Override
  public Automaton compose(String id, Set<String> flags, Context context,  Automaton... automata)
    throws CompilationException {
  Automaton aut = automata[0].reId(automata[0].getId()+"Rap2bc");

    System.out.println("RevAP2BC AUTOMATON start "+aut.myString()+ " flags "+flags);
  /*  Set<String> listeners = flags.stream().filter(x->x.endsWith("?")).collect(Collectors.toSet());
    //System.out.println("revAp2bc BEFORE "+aut.myString());
    QuiescentRefinement qr = new QuiescentRefinement();
    qr.addListeningLoops(aut,listeners); */

    replaceListeningSequence(aut);
    replaceSendingSequence(aut);
    // should be no events ending ".t!", ".t?" , ".r!", ".r?" ,".a!", ".a?" in aut
    // and "!" and "?" events must be preserved in mixed AP+BC process
    System.out.println("RevAP2BC after replaceing "+aut.myString());

    aut.cleanNodeLables();
    System.out.println("RevAP2BC AUTOMATON RETURNS "+aut.myString());
    return aut;
  }

  /*  Add lisening Loops - for events in flags   added when process built OR not?
      abstraction of  all *.t! and *.t? occurs in QuiescentRefinement
      but needs to occur before abstraction occurs see
      convert b? to b^ and b! to b
   */
   @Override
  public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {
    Automaton a =  TokenRule.tokenRule(petrinets[0]);
    Automaton[] auts = new Automaton[1];
    auts[0] = a;
    Automaton aout =  compose(id,flags,context, auts);

    return OwnersRule.ownersRule(aout );
  }
  public Petrinet OLDcompose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {

    Petrinet x = petrinets[0].reId("G");
    MultiProcessModel model = buildmpmFromPetri(x);
    Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = model.getProcessNodesMapping().getMarkingToNode();
    //Map<AutomatonNode, Multiset<PetriNetPlace> > nodeToMarking = model.getProcessNodesMapping().getNodeToMarking();

    return composeM(id,flags,context,markingToNode,x);
  }

  public static  void buildListeningLoops(Set<String> listeners, Automaton aut)
    throws CompilationException{
    //  For each marking

    for (AutomatonNode nd: aut.getNodes()){
        Set<AutomatonEdge> hearded =nd.getOutgoingEdges().
        stream().filter(x->x.getLabel().endsWith("?")).
        collect(Collectors.toSet());
        Set<String> heard =hearded.stream().map(x->x.getLabel()).collect(Collectors.toSet());
        //System.out.println("** trlist "+trlist);
      // set of listening labels with no transitions enabled
      Set<String> notHeard = listeners.
        stream().filter(x->!heard.contains(x)).collect(Collectors.toSet());
      //find the Places no owned by listening label
      //System.out.println("** trNotList "+trNotlist);
      for(String lab: notHeard){
        AutomatonEdge ed =  aut.addEdge(lab.substring(0,lab.length()-1),nd,nd,
          null,false,false);
        if (aut.getOwners().size() == 1)
           ed.setEdgeOwners(aut.getOwners());  // should be only one owner
        else
          throw new CompilationException(aut.getClass(),"Owners too large in RevAP2BC ",null);
        //System.out.println("**adding** "+ed.myString());
      }
    }

  }

  /*

   */
  private void replaceListeningSequence(Automaton aut) throws CompilationException {
    // add the n-a->m edges where n-a.t!->a?->m
    System.out.println("replaceListeningSequence START");
     for(AutomatonNode nd : aut.getNodes()){
       System.out.println("Node nd "+nd.getId());
       Set<AutomatonEdge> firsts =
        nd.getOutgoingEdges().stream().
           filter(ed->ed.getLabel().endsWith(".t"+Constant.BROADCASTSoutput)).
           collect(Collectors.toSet());
       for(AutomatonEdge first: firsts) {
         System.out.println("from "+nd.getId()+"-"+first.getLabel());
         String prefix = first.getLabel().substring(0,first.getLabel().length()-3);
         boolean matched = false;
         for(AutomatonEdge edx: first.getTo().getOutgoingEdges().stream().
           filter(ed->ed.getLabel().equals(prefix+".a"+Constant.BROADCASTSinput)).collect(Collectors.toSet())) {
           System.out.println("Edge edx "+edx.myString());
           AutomatonEdge ned =
           aut.addEdge(prefix,nd,edx.getTo(),null,false,false);
           aut.addOwnersToEdge(ned,edx.getEdgeOwners());
           System.out.println("adding "+ned.myString());
           aut.removeEdge(edx);
           matched = true;
         }
         if (matched) {
           for(AutomatonEdge ed: nd.getIncomingEdges().stream().
             filter(ed->(ed.getLabel().equals(prefix+".r"+Constant.BROADCASTSoutput)&& ed.getFrom().equalId(first.getTo()))).
             collect(Collectors.toSet())){
             aut.removeEdge(ed);
           }
           aut.removeEdge(first);
         }
       }
     }
     aut.setAlphabetFromEdges();
    System.out.println("replaceListeningSequence END");


  }

  /*
     replaces the sending sequence + removes the reject sequence
   */
  private void replaceSendingSequence(Automaton aut) throws CompilationException {
    // add the n-a^->m edges where n-a.t?->a!->m
    System.out.println("replaceSendingSequence START");
    for(AutomatonNode nd : aut.getNodes()){
      System.out.println("Node nd "+nd.getId());
      Set<AutomatonEdge> firsts =
        nd.getOutgoingEdges().stream().filter(ed->ed.getLabel().endsWith(".t?")).
          collect(Collectors.toSet());
      for(AutomatonEdge first: firsts) {
        System.out.println("from "+nd.getId()+"-"+first.getLabel());
        String prefix = first.getLabel().substring(0,first.getLabel().length()-3);
        boolean matched = false;
        for(AutomatonEdge edx: first.getTo().getOutgoingEdges().stream().
          filter(ed->ed.getLabel().equals(prefix+".a"+Constant.BROADCASTSoutput)).collect(Collectors.toSet()))
        {
          System.out.println("Edge edx "+edx.myString());
          AutomatonEdge ned =
            aut.addEdge(prefix+Constant.ACTIVE,nd,edx.getTo(),null,false,false);
          aut.addOwnersToEdge(ned,edx.getEdgeOwners());
          System.out.println("adding "+ned.myString());
          aut.removeEdge(edx);
          matched = true;
        }
        if (matched) {  // first nd start of active event
          aut.removeEdge(first);
        } else {       // first nd reject node should have only one exiting event
          for(AutomatonEdge edx: first.getTo().getOutgoingEdges().stream().
            filter(ed->ed.getLabel().equals(prefix+".r"+Constant.BROADCASTSoutput)).collect(Collectors.toSet()))
          {
            aut.removeEdge(edx);
            aut.removeEdge(first);
          }
        }
      }
    }
    aut.setAlphabetFromEdges();
    System.out.println("replaceSendingSequence END");


  }


  public Petrinet composeM(String id, Set<String> flags, Context context,
                           Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNode,
                           Petrinet petrinet) throws CompilationException {
   //Petrinet petrinet = petrinets[0].reId("Rev") ;
    //System.out.println("RevAP2BC start "+petrinet.getId()+ " flags "+flags);
    Set<String> listeners = flags.stream().filter(x->x.endsWith("?")).collect(Collectors.toSet());
    buildListeningLoops(markingToNode,listeners,petrinet); //MUST KEEP

    for(PetriNetTransition tr : petrinet.getTransitions().values()) {
     String prefix1 = tr.getLabel().substring(0,tr.getLabel().length()-1);
     if (tr.getLabel().endsWith(".t?") ||
         tr.getLabel().endsWith(".t!")  || tr.getLabel().endsWith(".r?") ||
       tr.getLabel().endsWith(".r!")  ) tr.setLabel(Constant.HIDDEN);
     else if (tr.getLabel().endsWith("?") ) tr.setLabel(prefix1);
     else if (tr.getLabel().endsWith("!") ) tr.setLabel(prefix1+Constant.ACTIVE);

   }
    // petrinet = ab.compose(id,flags,context,p);
     //System.out.println(petrinet.myString()+ "Rev end ");
    return petrinet;
  }
  public static  void buildListeningLoops(Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNode,
                                          Set<String> listeners, Petrinet petrinet)
    throws CompilationException{
    //  For each marking
    for (Multiset<PetriNetPlace> markM: markingToNode.keySet()){
      Set<PetriNetPlace> mark = markM.elementSet();
       //System.out.println("** Marking "+Petrinet.marking2String(mark));
//      find set of satisfied listening transitions
      Set<String> trlist = TokenRulePureFunctions.satisfiedTransitions(markM).
        stream().filter(x->!x.getLabel().endsWith("^")).
        map(x->x.getLabel()).collect(Collectors.toSet());
       //System.out.println("** trlist "+trlist);
      // set of listening labels with no transitions enabled
      Set<String> trNotlist = listeners.
        stream().filter(x->!trlist.contains(x)).collect(Collectors.toSet());
      //find the Places no owned by listening label
       //System.out.println("** trNotList "+trNotlist);
      for(String lab: trNotlist){
          PetriNetTransition t = petrinet.addTransition(mark,lab,mark);
           //System.out.println("**adding** "+t.myString());
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
    model.addProcessesMapping(new MappingNdMarking(nodeToMarking, markingToNode,pet.getId(),modelAut.getId()));
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

