package mc.compiler;

import com.microsoft.z3.Context;
import mc.compiler.ast.ASTNode;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ProcessNode;
import mc.compiler.interpreters.AutomatonInterpreter;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.util.LogAST;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

/**
 * Created by sheriddavi on 24/01/17.
 */
public class Interpreter {

    // fields
    private AutomatonInterpreter automaton;

    public Interpreter(){
        this.automaton = new AutomatonInterpreter();
    }

    public Map<String, ProcessModel> interpret(AbstractSyntaxTree ast, LocalCompiler localCompiler, BlockingQueue<Object> messageQueue, Context context) throws CompilationException, InterruptedException {
        Map<String, ProcessModel> processMap = new LinkedHashMap<>();

        List<ProcessNode> processes = ast.getProcesses();
        for(ProcessNode process : processes){
            messageQueue.add(new LogAST("Interpreting:",process));
            ProcessModel model;
            switch(process.getType()){
                case "processes": // If it is not a automata or petrinet then construct it (Its a non-drawn process)
                    continue;

                case "automata":
                    model = automaton.interpret(process, processMap, localCompiler, context);
                    model.setLocation(process.getLocation());
                    break;

                case "petrinet": // Not implemented yet.
                    continue;

                default:
                    throw new CompilationException(getClass(),"Unable to find the process type: "+process.getType());
            }

            processMap.put(process.getIdentifier(), model);
        }



        return processMap;
    }

    public ProcessModel interpret(String processModelType, ASTNode astNode, String identifer, Map<String, ProcessModel> processMap, Context context) throws CompilationException, InterruptedException {
        ProcessModel model;
        switch(processModelType){
            case "automata":
                model = automaton.interpret(astNode, identifer, processMap, context);
                break;
            default:
                throw new CompilationException(getClass(),"Unable to find the process type: "+processModelType);
        }

        return model;
    }
}
