package mc.compiler;

import mc.compiler.ast.AbstractSyntaxTree;
import mc.process_models.ProcessModel;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by sheriddavi on 24/01/17.
 */
public class Compiler {

  public static final int CODE = 0;
  public static final int JSON = 1;

  // fields
  private Lexer lexer;
  private Expander expander;
  private ReferenceReplacer replacer;
  private Interpreter interpreter;
  private OperationEvaluator evaluator;

  private JSONToASTConverter jsonToAst;

  public Compiler(){
    this.lexer = new Lexer();
    this.expander = new Expander();
    this.replacer = new ReferenceReplacer();
    this.interpreter = new Interpreter();
    this.evaluator = new OperationEvaluator();
    this.jsonToAst = new JSONToASTConverter();
  }

  public CompilationObject compile(String code, int type){
    if(type == CODE){
      return compile(code);
    }
    else if(type == JSON){
      JSONObject json = new JSONObject(code);
      return compile(json);
    }
    else{
      // TODO: error
      return null;
    }
  }

  public CompilationObject compile(String code){
    // TODO
    return null;
  }

  public CompilationObject compile(JSONObject json){
    AbstractSyntaxTree ast = jsonToAst.convert(json);
    return compile(ast);
  }

  private CompilationObject compile(AbstractSyntaxTree ast){
    ast = expander.expand(ast);
    ast = replacer.replaceReferences(ast);
    Map<String, ProcessModel> processMap = interpreter.interpret(ast);
    List<OperationResult> results = evaluator.evaluateOperations(ast.getOperations(), processMap, interpreter);
    return new CompilationObject(processMap, results);
  }


}
