package mc.compiler;

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import mc.compiler.ast.*;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.generator.AutomatonGenerator;
import mc.process_models.automata.operations.AutomataOperations;
import org.fusesource.jansi.Ansi;

import java.util.*;
import java.util.stream.Stream;

/**
 * Created by sheriddavi on 27/01/17.
 */
public class EquationEvaluator {

    private int equationId;

    private AutomataOperations automataOperations;

    public EquationEvaluator(){
        this.automataOperations = new AutomataOperations();
    }

    public List<OperationResult> evaluateEquations(List<OperationNode> operations, String code) throws CompilationException {
        reset();
        List<OperationResult> results = new ArrayList<OperationResult>();
        AutomatonGenerator generator = new AutomatonGenerator();
        for(OperationNode operation : operations){
            String firstId = OperationEvaluator.findIdent(operation.getFirstProcess(), code);
            String secondId = OperationEvaluator.findIdent(operation.getSecondProcess(), code);
            List<String> firstIds = OperationEvaluator.collectIdentifiers(operation.getFirstProcess());
            List<String> secondIds = OperationEvaluator.collectIdentifiers(operation.getSecondProcess());
            //Since both are tree based, they should have the same iteration order.
            List<Collection<ProcessModel>> generated = new ArrayList<>();
            Set<String> ids = new TreeSet<>(firstIds);
            ids.addAll(secondIds);
            int nodeCount = 2;
            for (String id: ids) {
                generated.add(generator.generateAutomaton(5,nodeCount,id,automataOperations,false));
            }
            Collection<List<ProcessModel>> modelCollection = permutations(generated);
            long passed = modelCollection.parallelStream().filter(processModels -> {
                AutomataOperations automataOperations = new AutomataOperations();
                Interpreter interpreter = new Interpreter();
                try {
                    List<Automaton> automata = new ArrayList<>();
                    Map<String, ProcessModel> currentMap = new HashMap<>();
                    for (ProcessModel m: processModels) {
                        currentMap.put(((Automaton)m).getId(),m);
                    }
                    automata.add((Automaton) interpreter.interpret("automata", operation.getFirstProcess(), getNextEquationId(), currentMap));
                    automata.add((Automaton) interpreter.interpret("automata", operation.getSecondProcess(), getNextEquationId(), currentMap));

                    if (Objects.equals(operation.getOperation(), "traceEquivalent")) {
                        List<Automaton> automata1 = new ArrayList<>();
                        for (Automaton a : automata) {
                            automata1.add(automataOperations.nfaToDFA(a));
                        }
                        automata = automata1;
                    }
                    boolean result = automataOperations.bisimulation(automata);
                    if (operation.isNegated()) {
                        result = !result;
                    }
                    return result;
                } catch (CompilationException ex) {
                    return false;
                }
            }).count();
            results.add(new OperationResult(operation.getFirstProcess(), operation.getSecondProcess(), firstId, secondId, operation.getOperation(), operation.isNegated(), passed == modelCollection.size()," @|black ("+passed+"/"+modelCollection.size()+") |@"));
        }
        return results;
    }

    private String getNextEquationId(){
        return "eq" + equationId++;
    }

    private void reset(){
        equationId = 0;
    }
    /**
     * Combines several collections of elements and create permutations of all of them, taking one element from each
     * collection, and keeping the same order in resultant lists as the one in original list of collections.
     *
     * <ul>Example
     * <li>Input  = { {a,b,c} , {1,2,3,4} }</li>
     * <li>Output = { {a,1} , {a,2} , {a,3} , {a,4} , {b,1} , {b,2} , {b,3} , {b,4} , {c,1} , {c,2} , {c,3} , {c,4} }</li>
     * </ul>
     *
     * @param collections Original list of collections which elements have to be combined.
     * @return Resultant collection of lists with all permutations of original list.
     */
    public static <T> Collection<List<T>> permutations(List<Collection<T>> collections) {
        if (collections == null || collections.isEmpty()) {
            return Collections.emptyList();
        } else {
            Collection<List<T>> res = Lists.newLinkedList();
            permutationsImpl(collections, res, 0, new LinkedList<T>());
            return res;
        }
    }

    /** Recursive implementation for {@link #permutations(List)} */
    private static <T> void permutationsImpl(List<Collection<T>> ori, Collection<List<T>> res, int d, List<T> current) {
        // if depth equals number of original collections, final reached, add and return
        if (d == ori.size()) {
            res.add(current);
            return;
        }

        // iterate from current collection and copy 'current' element N times, one for each element
        Collection<T> currentCollection = ori.get(d);
        for (T element : currentCollection) {
            List<T> copy = Lists.newLinkedList(current);
            copy.add(element);
            permutationsImpl(ori, res, d + 1, copy);
        }
    }
}
