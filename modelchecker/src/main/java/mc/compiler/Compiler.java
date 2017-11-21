package mc.compiler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ProcessNode;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.webserver.FakeContext;
import mc.webserver.webobjects.Context;
import mc.webserver.webobjects.LogMessage;
import mc.compiler.token.Token;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
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

    public Compiler() throws InterruptedException {
        this.lexer = new Lexer();
        parser = new Parser();
        this.expander = new Expander();
        this.replacer = new ReferenceReplacer();
        this.interpreter = new Interpreter();
        this.eqEvaluator = new EquationEvaluator();
        this.evaluator = new OperationEvaluator();
    }


    public CompilationObject compile(String code, Context context, com.microsoft.z3.Context z3Context, BlockingQueue<Object> messageQueue) throws CompilationException, InterruptedException {
        List<Token> codeInput = lexer.tokenise(code);
        AbstractSyntaxTree structedCode = parser.parse(codeInput, z3Context);
        return compile(structedCode, code, z3Context, context, messageQueue);
    }
    private CompilationObject compile(AbstractSyntaxTree ast, String code, com.microsoft.z3.Context z3Context, Context context, BlockingQueue<Object> messageQueue) throws CompilationException, InterruptedException {
        HashMap<String,ProcessNode> processNodeMap = new HashMap<>();
        for (ProcessNode node: ast.getProcesses()) {
            processNodeMap.put(node.getIdentifier(), (ProcessNode) node.copy());
        }
        ast = expander.expand(ast, messageQueue, z3Context);
        ast = replacer.replaceReferences(ast, messageQueue);
        Map<String, ProcessModel> processMap = interpreter.interpret(ast, new LocalCompiler(processNodeMap, expander, replacer,messageQueue),messageQueue,z3Context);
        List<OperationResult> results = evaluator.evaluateOperations(ast.getOperations(), processMap, interpreter, code,z3Context);
        EquationEvaluator.EquationReturn eqResults = eqEvaluator.evaluateEquations(ast.getEquations(), code, context,z3Context,messageQueue);
        processMap.putAll(eqResults.getToRender());
        if (!(context instanceof FakeContext)) {

            List<Automaton> toPosition = processMap.values().stream().filter(Automaton.class::isInstance).map(s -> (Automaton)s).filter(s -> s.getNodeCount() <= context.getAutoMaxNode()).collect(Collectors.toList());

            int counter = toPosition.size();
            for (Automaton automaton : toPosition) {
                messageQueue.add(new LogMessage("Performing layout for @|black "+automaton.getId()+"|@, remaining: "+counter--,true,false));
                automaton.position();
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

        public ProcessNode compile(ProcessNode node, com.microsoft.z3.Context context) throws CompilationException, InterruptedException {
            node = expander.expand(node,messageQueue,context);
            node = replacer.replaceReferences(node,messageQueue);
            return node;
        }
    }
}
