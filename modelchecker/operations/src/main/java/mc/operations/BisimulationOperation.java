package mc.operations;

import static mc.processmodels.automata.util.ColouringUtil.ColourComponent;

import java.util.*;
import java.util.stream.Collectors;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.util.ColouringUtil;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.utils.PetriColouring;

public class BisimulationOperation implements IOperationInfixFunction {


  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "BiSimulation";
  }

  /**
   * The form which the function will appear when composed in the text.
   *
   * @return the textual notation of the infix function
   */
  @Override
  public String getNotation() {
    return "~";
  }

  /**
   * Evaluate the function.
   *
   * @param automata the list of automata being compared
   * @return the resulting automaton of the operation
   */
  @Override
  public boolean evaluate(Collection<Automaton> automata) throws CompilationException {

   ArrayList<AutomatonEdge> edges = new ArrayList<>();
   ArrayList<AutomatonNode> nodes = new ArrayList<>();

   final int BASE_COLOUR = 1;
 //  System.out.println("Bisim evaluate");
    int i =0;
    for(Automaton a: automata){
  //   System.out.println(i++ +" "+ a.toString());
     edges.addAll(a.getEdges());
     nodes.addAll(a.getNodes());
    }
   Set<Integer> root_colors = new TreeSet<Integer>();
   Set<Integer> first_colors = new TreeSet<Integer>();
    Map<Integer, List<ColourComponent>> colourMap = new HashMap<>();
    //int rootColour = Integer.MIN_VALUE;

    // set up the initial colouring ( on the nodes)
   ColouringUtil colourer = new ColouringUtil();
   colourer.performInitialColouring(nodes);
   colourer.doColouring(edges, nodes); // uses initial colouring on nodes


   i = 0;
   for(Automaton automaton: automata){
    //System.out.println("bisim ~ "+ automaton.myString());
      Set<AutomatonNode> root = automaton.getRoot();

     if (i ==0){
        for(AutomatonNode n : root) {
          first_colors.add(n.getColour());
        }
 // System.out.println("Aut "+ automaton.getId()+ " first col "+ first_colors);
        i++;
      } else {
        for (AutomatonNode n : root) {
         root_colors.add(n.getColour());
        }
 // System.out.println("Aut "+ automaton.getId()+ " root col "+ root_colors);
        if (root_colors.equals(first_colors)) {   //comparison between this current automaton and the first
           return true;
        } else {
           return false;
        }
     }
    }

    return true;
  }

    public boolean evaluateP(Collection<Petrinet> petrinets) throws CompilationException {
      assert petrinets.size() == 2;
        String tag1 = "*P1";
        String tag2 = "*P2";
      Iterator<Petrinet> ip = petrinets.iterator();
      Petrinet p1 = ip.next();
      Petrinet p2 = ip.next();
        System.out.println("p1 root "+p1.getRoots());
        System.out.println("p2 root "+p2.getRoots());
        Petrinet composition = new Petrinet(p1.getId() + "COL" + p2.getId(), false);
        composition.getOwners().clear();
        composition.getOwners().addAll(p1.getOwners());
        composition.getOwners().addAll(p2.getOwners());

        composition.addPetrinetNoOwner(p1,"");
        composition.addPetrinetNoOwner(p2,"");
        System.out.println("composition root "+composition.getRoots());
        PetriColouring pc = new PetriColouring();
        Map<String, Integer> ic = pc.initColour(composition);
        pc.doColour(composition, ic);

    return true;
    }
}
