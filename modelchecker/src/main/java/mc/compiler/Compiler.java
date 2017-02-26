package mc.compiler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ProcessNode;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.process_models.automata.generator.AutomatonGenerator;
import org.json.JSONObject;

import java.util.*;

public class Compiler {

    public static final int CODE = 0;
    public static final int JSON = 1;
    // fields
    private Lexer lexer;
    private Expander expander;
    private ReferenceReplacer replacer;
    private Interpreter interpreter;
    private OperationEvaluator evaluator;
    private EquationEvaluator eqEvaluator;
    private Parser parser;

    private JSONToASTConverter jsonToAst;

    public Compiler(){
        this.lexer = new Lexer();
        parser = new Parser();
        this.expander = new Expander();
        this.replacer = new ReferenceReplacer();
        this.interpreter = new Interpreter();
        this.eqEvaluator = new EquationEvaluator();
        this.evaluator = new OperationEvaluator();
        this.jsonToAst = new JSONToASTConverter();
    }

    public CompilationObject compile(String code, int type) throws CompilationException {
        if(type == CODE){
            return compile(code);
        }
        else if(type == JSON){
            JSONObject json = new JSONObject(code);
            return compile(json);
        }
        else{
            throw new CompilationException(getClass(),"Unable to find code type: "+type);
        }
    }

    public CompilationObject compile(String code) throws CompilationException{
        if (code.startsWith("random")) {
            int nodeCount = Integer.parseInt(code.split("random\\(")[1].replace(")",""));
            return new CompilationObject(new AutomatonGenerator().generateAutomaton(5,nodeCount,"A"),Collections.emptyList(),Collections.emptyList());
        }
        return compile(parser.parse(lexer.tokenise(code)), code);
    }

    public CompilationObject compile(JSONObject json) throws CompilationException {
        AbstractSyntaxTree ast = jsonToAst.convert(json);
        return compile(ast, json+"");
    }

    private CompilationObject compile(AbstractSyntaxTree ast, String code) throws CompilationException {
        HashMap<String,ProcessNode> processNodeMap = new HashMap<>();
        for (ProcessNode node: ast.getProcesses()) {
            processNodeMap.put(node.getIdentifier(), (ProcessNode) node.copy());
        }
        ast = expander.expand(ast);
        ast = replacer.replaceReferences(ast);
        Map<String, ProcessModel> processMap = interpreter.interpret(ast, new LocalCompiler(processNodeMap, expander, replacer));
        List<OperationResult> results = evaluator.evaluateOperations(ast.getOperations(), processMap, interpreter, code);
        List<OperationResult> eqResults = eqEvaluator.evaluateEquations(ast.getEquations(), interpreter, code);
        return new CompilationObject(processMap, results, eqResults);
    }
    @AllArgsConstructor
    public static class LocalCompiler {
        @Getter
        private HashMap<String,ProcessNode> processNodeMap;
        private Expander expander;
        private ReferenceReplacer replacer;

        public ProcessNode compile(ProcessNode node) throws CompilationException {
            node = expander.expand(node);
            node = replacer.replaceReferences(node);
            return node;
        }
    }
}
