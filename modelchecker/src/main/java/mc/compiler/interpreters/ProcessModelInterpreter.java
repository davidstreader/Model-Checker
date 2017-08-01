package mc.compiler.interpreters;

import mc.compiler.Compiler;
import mc.compiler.ast.ASTNode;
import mc.compiler.ast.ProcessNode;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;

import java.util.Map;

/**
 * Created by sheriddavi on 27/01/17.
 */
public interface ProcessModelInterpreter {

    ProcessModel interpret(ProcessNode processNode, Map<String, ProcessModel> processMap, Compiler.LocalCompiler localCompiler) throws CompilationException, InterruptedException;

    ProcessModel interpret(ASTNode astNode, String identifier, Map<String, ProcessModel> processMap) throws CompilationException, InterruptedException;
}
