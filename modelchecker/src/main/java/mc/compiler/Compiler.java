package mc.compiler;

import mc.compiler.ast.AbstractSyntaxTree;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class Compiler {

    public static final int CODE = 0;
    public static final int JSON = 1;

    // fields
    private Lexer lexer;
    private Expander expander;
    private VariableHider hider;
    private ReferenceReplacer replacer;
    private Interpreter interpreter;
    private OperationEvaluator evaluator;
    private Parser parser;

    private JSONToASTConverter jsonToAst;

    public Compiler(){
        this.lexer = new Lexer();
        parser = new Parser();
        this.expander = new Expander();
        this.hider = new VariableHider();
        this.replacer = new ReferenceReplacer();
        this.interpreter = new Interpreter();
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
        return compile(parser.parse(lexer.tokenise(code)));
    }

    public CompilationObject compile(JSONObject json) throws CompilationException {
        AbstractSyntaxTree ast = jsonToAst.convert(json);
        return compile(ast);
    }

    public CompilationObject compile(AbstractSyntaxTree ast) throws CompilationException {
        ast = expander.expand(ast);
        ast = replacer.replaceReferences(ast);
        Map<String, ProcessModel> processMap = interpreter.interpret(ast);
        List<OperationResult> results = evaluator.evaluateOperations(ast.getOperations(), processMap, interpreter);
        return new CompilationObject(processMap, results);
    }


}
