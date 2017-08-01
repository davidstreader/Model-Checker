package mc.compiler;

import com.eclipsesource.v8.V8;
import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ProcessNode;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.generator.AutomatonGenerator;
import mc.process_models.automata.operations.AutomataOperations;
import mc.util.GraphvizV8ThreadedEngine;
import mc.webserver.FakeContext;
import mc.webserver.WebSocketServer;
import mc.webserver.webobjects.Context;
import mc.webserver.webobjects.LogMessage;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Compiler {
    // fields
    private Lexer lexer;
    private Expander expander;
    private ReferenceReplacer replacer;
    private Interpreter interpreter;
    private OperationEvaluator evaluator;
    private EquationEvaluator eqEvaluator;
    private Parser parser;

    public Compiler(){
        this.lexer = new Lexer();
        parser = new Parser();
        this.expander = new Expander();
        this.replacer = new ReferenceReplacer();
        this.interpreter = new Interpreter();
        this.eqEvaluator = new EquationEvaluator();
        this.evaluator = new OperationEvaluator();
    }

    public CompilationObject compile(String code, Context context, BlockingQueue<Object> messageQueue) throws CompilationException, InterruptedException {
        if (code.startsWith("random")) {
            messageQueue.add(new LogMessage("Generating random models"));
            boolean alphabet = Boolean.parseBoolean(code.split(",")[1]);
            int nodeCount = Integer.parseInt(code.split(",")[2]);
            int alphabetCount = Integer.parseInt(code.split(",")[3]);
            int maxTransitionCount = Integer.parseInt(code.split(",")[4]);
            Map<String,ProcessModel> models = new HashMap<>();
            int i = 0;
            List<ProcessModel> nodes = new AutomatonGenerator().generateAutomaton("A",new AutomataOperations(),new EquationEvaluator.EquationSettings(alphabet,alphabetCount,nodeCount,maxTransitionCount));
            for (ProcessModel model:nodes) {
                Automaton a = (Automaton) model;
                models.put(a.getId()+(i++),a);
            }
            return new CompilationObject(models,Collections.emptyList(),Collections.emptyList());
        }
        return compile(parser.parse(lexer.tokenise(code)), code, context, messageQueue);
    }
    private CompilationObject compile(AbstractSyntaxTree ast, String code, Context context, BlockingQueue<Object> messageQueue) throws CompilationException, InterruptedException {
        HashMap<String,ProcessNode> processNodeMap = new HashMap<>();
        for (ProcessNode node: ast.getProcesses()) {
            processNodeMap.put(node.getIdentifier(), (ProcessNode) node.copy());
        }
        ast = expander.expand(ast, messageQueue);
        ast = replacer.replaceReferences(ast, messageQueue);
        Map<String, ProcessModel> processMap = interpreter.interpret(ast, new LocalCompiler(processNodeMap, expander, replacer,messageQueue),messageQueue);
        List<OperationResult> results = evaluator.evaluateOperations(ast.getOperations(), processMap, interpreter, code);
        EquationEvaluator.EquationReturn eqResults = eqEvaluator.evaluateEquations(ast.getEquations(), code, context,messageQueue);
        processMap.putAll(eqResults.getToRender());
        if (!(context instanceof FakeContext)) {
            List<Automaton> toPosition = processMap.values().stream().filter(Automaton.class::isInstance).map(s -> (Automaton)s).filter(s -> s.getNodeCount() <= context.getAutoMaxNode()).collect(Collectors.toList());
            ExecutorService service = Executors.newCachedThreadPool();
            final AtomicInteger counter=new AtomicInteger(toPosition.size());
            messageQueue.add(new LogMessage("Laying out processes",true,false));
            List<V8> engines = new ArrayList<>();
            GraphvizV8ThreadedEngine.getV8Engines().put(Thread.currentThread(),engines);
            toPosition.forEach(s ->
                service.submit(() -> {
                    GraphvizV8ThreadedEngine.getV8Engines().put(Thread.currentThread(),engines);
                    s.position();
                    messageQueue.add(new LogMessage("Layout done for @|black "+s.getId()+"|@, remaining: "+counter.decrementAndGet(),true,false));
                }));
            try {
                service.shutdown();
                service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                service.shutdownNow();
                throw e;
            }
        }
        return new CompilationObject(processMap, results, eqResults.getResults());
    }
    @AllArgsConstructor
    public static class LocalCompiler {
        @Getter
        private HashMap<String,ProcessNode> processNodeMap;
        private Expander expander;
        private ReferenceReplacer replacer;
        private BlockingQueue<Object> messageQueue;

        public ProcessNode compile(ProcessNode node) throws CompilationException, InterruptedException {
            node = expander.expand(node,messageQueue);
            node = replacer.replaceReferences(node,messageQueue);
            return node;
        }
    }
}
