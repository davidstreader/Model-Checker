package mc.processmodels.automata.operations;

import com.google.common.collect.Multimap;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.operations.PetrinetReachability;
import mc.util.expr.MyAssert;

import java.util.*;
import java.util.stream.Collectors;

public class ChoiceFun {

  public static final String MAPLET = "^";
  /**
   * Execute the function.
   *
   * @param id         the id of the resulting automaton
   * @param automaton1 the first  automaton in the function (e.g. {@code A} in {@code A||B})
   * @param automaton2 the second automaton in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting automaton of the operation
   */
  public Automaton compose(String id, Automaton automaton1, Automaton automaton2) throws CompilationException {

    return null;
  }

  /**
   * * Multi Roots changes the high level structure!
   * Let Roots = List of Set of Places Ra = Sa1,Sa2,...
   * Ra [] Rb = Sa1Sb1, Sa2Sb1, ...;  Sa2Sb1,Sa2Sb2, ...; ..... where
   *   SaiSbj  =  Sai TIMES Sbj
   * When A and B have only one root   RA [] RB =  Sa1 TIMES Sb1
   *  Sai and Saj are not always disjoint when i!=j see  a||(x+y)
      for each pair of roots Ri, Rj build a Ri[]Rj processes
   *  (x+y)[]c has two c events one offered with an x evnent the other with the y event
   *  (x+y)[](a+b) had four root nodes and 8 events 2 of each x,y,a,b
   *
   * @param id        the id of the resulting petrinet
   * @param n1 the first  petrinet in the function (e.g. {@code A} in {@code A||B})
   * @param n2 the second petrinet in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting petrinet of the operation
   */


  public Petrinet compose(String id, Petrinet n1, Petrinet n2)
    throws CompilationException {
    SequentialInfixFun sf = new SequentialInfixFun();
    //System.out.println("XXX   XXX   XXX   XXX   XXX\n\n  []   ********************** \n "+ n1.myString()+" \n []  "+n2.myString());
    Petrinet nt1 = n1.copy().reId("1");
    // Need to add end event simply to remove later  as computing the End proved difficult with a->STOP[]STOP
    Petrinet  E =  Petrinet.oneEventNet("*E","e1");
    Petrinet net1 = sf.compose("n1",nt1,E);
    net1.reown("1");
    Set<String> own1 = net1.getOwners();

    Petrinet nt2 = n2.copy().reId("2");
    Petrinet F = Petrinet.oneEventNet("*E","e2");
    Petrinet net2 = sf.compose("n2",nt2, F);
    net2.reown("2");
    Set<String> own2 = net2.getOwners();
    //System.out.println("YYY   YYY   YYY   YYY   YYY\n\n     *********[]********[]*****\n  "+ net1.myString()+" \n  "+net2.myString());

    //System.out.println("[]PETRI1 "+net1.myString("edge"));
    MyAssert.validate(net1,"Choice n1 []  Rule PRE condition ");
    MyAssert.validate(net2,"Choice [] n2  Rule PRE condition ");

    List<Set<String>> tRoots =  net2.copyRoots();
    List<Set<String>> oRoots =  net1.copyRoots();
    System.out.println("twoRoots "+tRoots+"  oneRoots "+oRoots);

    Petrinet choice = new Petrinet(id, false);
    choice.addPetrinet(net1, true, true); //adds net1 + root and end but changes Ids
    List<Set<String>>  oneRoots =  choice.reName(oRoots);

    choice.addPetrinet(net2, false, true);  //adds net2 + end but changes Ids
    List<Set<String>>  twoRoots =  choice.reName(tRoots);  // have to rename the roots

    System.out.println("twoRoots "+twoRoots+"  oneRoots "+oneRoots);


    //net2.setUpv2o(net1,net2);


    //System.out.println("Befor Glue O  \n"+choice.myString());
    choice.glueOwners(own1,own2);
    choice.reown("c");

    //for each pair of roots Ri, Rj build a Ri[]Rj root and copy the root post transition
    List<Set<String>> newRoots = new ArrayList<>();
    for (Set<String> r1: oneRoots) {
       for (Set<String> r2: twoRoots) {
        //Copy both roots and then Glue
        System.out.println("    GLUE ROOT  "+r1+ " with "+r2);
         //The Root2 must be copied prior to Gluing as they may be needed later (next iteration)
        Set<PetriNetPlace> newr2 = new HashSet<PetriNetPlace>();
        for(String rt2: r2) { newr2.add(choice.copyRootOrEnd(choice.getPlace(rt2),"None",false));
        }
        Set<PetriNetPlace> newr1 = new HashSet<PetriNetPlace>();
        for(String rt1: r1) { newr1.add(choice.copyRootOrEnd(choice.getPlace(rt1),"None",false));
        }

        Multimap<String, String> s2s =choice.gluePlaces(newr1, newr2, false);
        System.out.println("After Gluing  \n"+choice.myString());
      }
        //System.out.println("[]after Glueing start  " + net2.myString("edge") + "\n");
    }
    choice.removeStarE();
    System.out.println("FINAL After Gluing  \n"+choice.myString());

    choice = PetrinetReachability.removeUnreachableStates(choice);
    System.out.println("\n[] OUT "+ choice.myString(""));
    choice.buildAlphabetFromTrans();
    //System.out.println("[] OUT "+ choice.getId()+"\n");
    //choice.reId("");
   // MyAssert.setApply(true);
    MyAssert.validate(choice,"Choice []  Rule post condition ");
    return choice;
  }



  /**
   * Copies the edges from one automata to another.
   *
   * @param writeAutomaton the automata that will have the edges copied to it
   * @param readAutomaton  the automata that will have the edges copied from it
   * @param nodeMap        the mapping of the ids to AutomatonNodes
   */
  private void copyAutomataEdges(Automaton writeAutomaton, Automaton readAutomaton,
                                 Map<String, AutomatonNode> nodeMap,
                                 Multimap<String,String> edgeOwnersMap) throws CompilationException{


    for(AutomatonEdge readEdge : readAutomaton.getEdges()) {
      AutomatonNode fromNode = nodeMap.get(readEdge.getFrom().getId());
      AutomatonNode toNode = nodeMap.get(readEdge.getTo().getId());
        writeAutomaton.addOwnersToEdge(
                writeAutomaton.addEdge(readEdge.getLabel(), fromNode, toNode, readEdge.getGuard(), false,readEdge.getOptionalEdge()),
                getEdgeOwnersFromProduct(readEdge.getOwnerLocation(), edgeOwnersMap)
        );
    }
  }

  private Set<String> getEdgeOwnersFromProduct(Set<String> edgeOwners,
                                               Multimap<String,String> productSpace) {
    return edgeOwners.stream().map(productSpace::get)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

  /**
   *
   * @param net1
   * @param net2
   * @return the multiRoot for parallel composition of the nets
   */
  private static List<Set<String>> buildRoots(Petrinet net1,Petrinet net2) {
    //System.out.println("Building Roots");
    List<Set<String>> out = new ArrayList<>();
    for(Set<String> m1: net1.getRoots()) {
      for(Set<String> m2: net2.getRoots()) {
        out.add(buildMark(m1,m2));
      }
    }
    //System.out.println("New Roots "+out);
    return out;
  }

  private static Set<String> buildMark(Set<String> m1, Set<String> m2){
    //System.out.println("buildMark " +m1+" "+m2);
    Set<String> out = new HashSet<>();
    for(String s1:m1){
      for(String s2:m2){
        out.add(s1+MAPLET+s2);
      }
    }
    //System.out.println("Next root "+out);
    return out;
  }
}
