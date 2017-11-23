package mc.compiler;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import mc.compiler.ast.OperationNode;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.generator.AutomatonGenerator;
import mc.process_models.automata.operations.AutomataOperations;
import mc.webserver.webobjects.Context;
import mc.webserver.webobjects.LogMessage;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *  Generates different automata to test if they work within the user created model.
 *  For example, if we're looking at the property of communitivity.
 *  Where A || (B || C) = (A || B) || C
 *  If we generate different models for A, B and C we can test that it follows that law. If the user generated an automata that followed that process as such.
 */
public class EquationEvaluator {

    private int equationId;

    private AutomataOperations automataOperations;

    public EquationEvaluator(){
        this.automataOperations = new AutomataOperations();
    }

    public EquationReturn evaluateEquations(List<OperationNode> operations, String code, Context context, com.microsoft.z3.Context z3Context, BlockingQueue<Object> messageQueue) throws CompilationException {
        reset();
        List<OperationResult> results = new ArrayList<>();
        Map<String,ProcessModel> toRender = new ConcurrentSkipListMap<>();
        AutomatonGenerator generator = new AutomatonGenerator();
        for(OperationNode operation : operations){
            String firstId = OperationEvaluator.findIdent(operation.getFirstProcess(), code);
            String secondId = OperationEvaluator.findIdent(operation.getSecondProcess(), code);
            List<String> firstIds = OperationEvaluator.collectIdentifiers(operation.getFirstProcess());
            List<String> secondIds = OperationEvaluator.collectIdentifiers(operation.getSecondProcess());
            String opIdent = firstId+" "+OperationResult.getOpSymbol(operation.getOperation())+" "+secondId;
            messageQueue.add(new LogMessage("Checking equation: "+opIdent,true,false));
            //Since both are tree based, they should have the same iteration order.
            List<Collection<ProcessModel>> generated = new ArrayList<>();
            Set<String> ids = new TreeSet<>(firstIds);
            ids.addAll(secondIds);
            messageQueue.add(new LogMessage("Generating models"));
            for (String id: ids) {
                generated.add(generator.generateAutomaton(id,automataOperations,operation.getEquationSettings()));
            }
            messageQueue.add(new LogMessage("Generating permutations"));
            List<List<ProcessModel>> perms = permutations(generated).collect(Collectors.toList());
            messageQueue.add(new LogMessage("Evaluating equations (0/"+perms.size()+")"));
            ModelStatus status = new ModelStatus();
            ExecutorService service = Executors.newFixedThreadPool(4);
            for (List<ProcessModel> models : perms) {
                service.submit(()->testModel(models,messageQueue,status, operation, context, z3Context, toRender, firstId, secondId, perms.size()));
            }
            service.shutdown();
            try {
                service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                service.shutdownNow();
            }
            results.add(new OperationResult(operation.getFirstProcess(), operation.getSecondProcess(), firstId, secondId, operation.getOperation(), operation.isNegated(), status.passCount == perms.size()," @|black ("+status.passCount+"/"+perms.size()+") |@"));
        }
        return new EquationReturn(results,toRender);
    }

    private boolean testModel(List<ProcessModel> processModels, BlockingQueue<Object> messageQueue, ModelStatus status, OperationNode operation, Context context, com.microsoft.z3.Context z3Context, Map<String, ProcessModel> toRender, String firstId, String secondId, int size) {
        AutomataOperations automataOperations = new AutomataOperations();
        Interpreter interpreter = new Interpreter();
        try {
            List<Automaton> automata = new ArrayList<>();
            Map<String, ProcessModel> currentMap = new HashMap<>();
            for (ProcessModel m: processModels) {
                currentMap.put(m.getId(),m);
            }
            automata.add((Automaton) interpreter.interpret("automata", operation.getFirstProcess(), getNextEquationId(), currentMap,z3Context));
            automata.add((Automaton) interpreter.interpret("automata", operation.getSecondProcess(), getNextEquationId(), currentMap,z3Context));

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
            if (!result && status.failCount < context.getFailCount()) {
                String id2 = "op"+(status.id++)+": (";
                toRender.put(id2+firstId+")",automata.get(0));
                toRender.put(id2+secondId+")",automata.get(1));

                for (String id: currentMap.keySet()) {
                    toRender.put(id2+id+")",currentMap.get(id));
                }
                status.failCount++;
            }
            int done = ++status.doneCount;
            messageQueue.add(new LogMessage("Evaluating equations (" + done + "/" + size +") ("+((int)(done/(double)size*100.0))+ "%)", 1));
            status.timeStamp = System.currentTimeMillis();


            return result;
        } catch (CompilationException ex) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private String getNextEquationId(){
        return "eq" + equationId++;
    }

    private void reset(){
        equationId = 0;
    }
    private <T> Stream<List<T>> permutations(List<Collection<T>> collections) {
        if (collections == null || collections.isEmpty()) {
            return Stream.empty();
        } else {
            return permutationsImpl(collections, 0, new LinkedList<>());
        }
    }
    private static <T> Stream<List<T>> permutationsImpl(List<Collection<T>> ori, int d, List<T> current) {
        if (d == ori.size()) {
            return Stream.of(current);
        }
        Collection<T> currentCollection = ori.get(d);
        return currentCollection.parallelStream().flatMap(map -> {
            List<T> copy = Lists.newLinkedList(current);
            copy.add(map);
            return permutationsImpl(ori,d+1,copy);
        });
    }
    @Getter
    @AllArgsConstructor
    class EquationReturn {
        List<OperationResult> results;
        Map<String,ProcessModel> toRender;
    }
    @Getter
    @AllArgsConstructor
    @ToString
    public static class EquationSettings {
        boolean alphabet;
        int nodeCount;
        int alphabetCount;
        int maxTransitionCount;
    }

    private class ModelStatus {
        int passCount;
        int failCount;
        int id;
        int doneCount;
        long timeStamp;
    }
}
