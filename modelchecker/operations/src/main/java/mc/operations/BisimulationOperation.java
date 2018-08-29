package mc.operations;

import static mc.processmodels.automata.util.ColouringUtil.ColourComponent;

import java.util.*;
import java.util.stream.Collectors;

import com.microsoft.z3.Context;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
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
        return "~p";
    }
    @Override
    public String getOperationType(){return "petrinet";}

    /**
     * Evaluate the function.  we can pass the function auto OR petri
     *
     * @param processModels the list of automata / PetriNets being compared
     * @return the resulting automaton of the operation
     */
    @Override
    public boolean evaluate(Set<String> flags,
                            Context context, Collection<ProcessModel> processModels) throws CompilationException {

        if (processModels.iterator().next() instanceof Petrinet) {
            System.out.println("Bisimulation on Petri Nets");
            processModels.stream().forEach(x->{  System.out.println(x.getId());});
            String tag1 = "*P1";
            String tag2 = "*P2";
            Iterator<ProcessModel> ip = processModels.iterator();
            Petrinet p1 = (Petrinet) ip.next();
            Petrinet p2 = (Petrinet) ip.next();
            //System.out.println("p1 root " + p1.getRoots());
            //System.out.println("p2 root " + p2.getRoots());
            Petrinet composition = new Petrinet(p1.getId() + "COL" + p2.getId(), false);
            composition.getOwners().clear();
            composition.getOwners().addAll(p1.getOwners());
            composition.getOwners().addAll(p2.getOwners());

            composition.addPetrinetNoOwner(p1, "");
            composition.addPetrinetNoOwner(p2, "");
            //System.out.println("composition root " + composition.getRoots());
            PetriColouring pc = new PetriColouring();
            Map<String, Integer> ic = pc.initColour(composition);
            pc.doColour(composition, ic);

            List<Set<Integer>> r1cs = new ArrayList<>();
            for(Set<String> m: p1.getRoots()) {
                r1cs.add(m.stream().map(x->composition.getPlace(x).getColour()).collect(Collectors.toSet()));
            }
            List<Set<Integer>> r2cs = new ArrayList<>();
            for(Set<String> m: p2.getRoots()) {
                r2cs.add(m.stream().map(x->composition.getPlace(x).getColour()).collect(Collectors.toSet()));
            }
            System.out.println("Root cols "+r1cs+" ** "+r2cs);
            boolean found = true;
            for(Set<Integer> r1c :r1cs){
                found = false;
                for(Set<Integer> r2c :r2cs){
                    if (r1c.equals(r2c)) {
                        found = true;
                        break;
                    }
                }
                if (!found) break;
            }
            return found;
        }

        System.out.printf("\nBisimulation semantics expecting PetriNets but found " + processModels.iterator().next().getClass()+"\n");
        System.out.println(processModels.iterator().next().getId());
        Throwable t = new Throwable();
        t.printStackTrace();
        return false;
    }
}

