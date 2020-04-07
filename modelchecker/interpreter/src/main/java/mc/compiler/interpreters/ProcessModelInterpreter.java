package mc.compiler.interpreters;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import mc.compiler.ast.ASTNode;
import mc.compiler.ast.ProcessNode;
import mc.exceptions.CompilationException;
import mc.processmodels.ProcessModel;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by sheriddavi on 27/01/17.
 */
public interface ProcessModelInterpreter {

    ProcessModel interpret(ProcessNode processNode,
                           Map<String, ProcessModel> processMap,
                           // LocalCompiler localCompiler,
                           Context context,
                           Set<String> alpha,
                           Map<String,Expr> globalVarMap,
                           boolean symb)
            throws CompilationException, InterruptedException, ExecutionException;

    ProcessModel interpretEvalOp(ASTNode astNode,
                           String identifier,
                           Map<String, ProcessModel> processMap,
                           Context context,
                           Set<String> alpha)
        throws CompilationException, InterruptedException, ExecutionException;
}
