package mc.compiler;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.compiler.ast.OperationNode;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.generator.AutomatonGenerator;
import mc.process_models.automata.operations.AutomataOperations;
import mc.webserver.Context;
import mc.webserver.LogMessage;
import mc.webserver.WebSocketServer;
import org.eclipse.jetty.websocket.api.Session;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class EquationEvaluator {

    private int equationId;

    private AutomataOperations automataOperations;

    public EquationEvaluator(){
        this.automataOperations = new AutomataOperations();
    }

    public EquationReturn evaluateEquations(List<OperationNode> operations, String code, Context context) throws CompilationException {
        reset();
        final AtomicInteger passedCount = new AtomicInteger(0),
            failedCount = new AtomicInteger(0),
            opId = new AtomicInteger(0),
            doneCount = new AtomicInteger(0);
        final AtomicLong timeStamp = new AtomicLong(System.currentTimeMillis());
        List<OperationResult> results = new ArrayList<OperationResult>();
        Map<String,ProcessModel> toRender = new ConcurrentSkipListMap<>();
        AutomatonGenerator generator = new AutomatonGenerator();
        for(OperationNode operation : operations){
            String firstId = OperationEvaluator.findIdent(operation.getFirstProcess(), code);
            String secondId = OperationEvaluator.findIdent(operation.getSecondProcess(), code);
            List<String> firstIds = OperationEvaluator.collectIdentifiers(operation.getFirstProcess());
            List<String> secondIds = OperationEvaluator.collectIdentifiers(operation.getSecondProcess());
            String opIdent = firstId+" "+OperationResult.getOpSymbol(operation.getOperation())+" "+secondId;
            new LogMessage("Checking equation: "+opIdent,true,false).send();
            //Since both are tree based, they should have the same iteration order.
            List<Collection<ProcessModel>> generated = new ArrayList<>();
            Set<String> ids = new TreeSet<>(firstIds);
            ids.addAll(secondIds);
            new LogMessage("Generating models").send();
            for (String id: ids) {
                generated.add(generator.generateAutomaton(id,automataOperations,operation.getEquationSettings()));
            }
            new LogMessage("Generating permutations").send();
            List<List<ProcessModel>> perms = permutations(generated).collect(Collectors.toList());
            new LogMessage("Evaluating equations (0/"+perms.size()+")").send();
            BlockingQueue<LogMessage> messageQueue = WebSocketServer.getMessageQueue().get();
            Supplier<Boolean> isStopped = WebSocketServer::isStopped;
            long passed = perms.parallelStream().filter(processModels -> {
                AutomataOperations automataOperations = new AutomataOperations();
                Interpreter interpreter = new Interpreter();
                try {
                    List<Automaton> automata = new ArrayList<>();
                    Map<String, ProcessModel> currentMap = new HashMap<>();
                    for (ProcessModel m: processModels) {
                        currentMap.put(m.getId(),m);
                    }
                    if (isStopped.get()) return false;
                    automata.add((Automaton) interpreter.interpret("automata", operation.getFirstProcess(), getNextEquationId(), currentMap));
                    automata.add((Automaton) interpreter.interpret("automata", operation.getSecondProcess(), getNextEquationId(), currentMap));

                    if (Objects.equals(operation.getOperation(), "traceEquivalent")) {
                        List<Automaton> automata1 = new ArrayList<>();
                        for (Automaton a : automata) {
                            automata1.add(automataOperations.nfaToDFA(a));
                        }
                        automata = automata1;
                    }
                    if (isStopped.get()) return false;
                    boolean result = automataOperations.bisimulation(automata, isStopped);
                    if (operation.isNegated()) {
                        result = !result;
                    }
                    if (!result && failedCount.get() < context.getGraphSettings().getFailCount()) {
                        String id2 = "op"+opId.getAndIncrement()+": (";
                        toRender.put(id2+firstId+")",automata.get(0));
                        toRender.put(id2+secondId+")",automata.get(1));

                        for (String id: currentMap.keySet()) {
                            toRender.put(id2+id+")",currentMap.get(id));
                        }
                        failedCount.incrementAndGet();
                    }
                    int done = doneCount.incrementAndGet();
                    if (System.currentTimeMillis()-timeStamp.get() > 100) {
                        messageQueue.add(new LogMessage("Evaluating equations (" + done + "/" + perms.size() +") ("+((int)(done/(double)perms.size()*100.0))+ "%)", 1));
                        timeStamp.set(System.currentTimeMillis());
                    }

                    return result;
                } catch (CompilationException ex) {
                    return false;
                }
            }).count();
            results.add(new OperationResult(operation.getFirstProcess(), operation.getSecondProcess(), firstId, secondId, operation.getOperation(), operation.isNegated(), passed == perms.size()," @|black ("+passed+"/"+perms.size()+") |@"));
        }
        return new EquationReturn(results,toRender);
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
            return permutationsImpl(collections, 0, new LinkedList<T>());
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
}
