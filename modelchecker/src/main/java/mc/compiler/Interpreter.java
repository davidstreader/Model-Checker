package mc.compiler;

import mc.compiler.ast.ASTNode;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.OperationNode;
import mc.compiler.ast.ProcessNode;
import mc.compiler.interpreters.AutomatonInterpreter;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.webserver.LogMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sheriddavi on 24/01/17.
 */
public class Interpreter {

    // fields
    private AutomatonInterpreter automaton;

    public Interpreter(){
        this.automaton = new AutomatonInterpreter();
    }

    public Map<String, ProcessModel> interpret(AbstractSyntaxTree ast) throws CompilationException {
        Map<String, ProcessModel> processMap = new HashMap<String, ProcessModel>();

        List<ProcessNode> processes = ast.getProcesses();
        for(ProcessNode process : processes){
          new LogMessage("Interpreting:",process).send();
            ProcessModel model = null;
            switch(process.getType()){
                case "automata":
                    model = automaton.interpret(process, processMap);
                    break;
                default:
                    throw new CompilationException(getClass(),"Unable to find the process type: "+process.getType());
            }
            String ident = process.getIdentifier();
            if (ident.endsWith("*")) ident = ident.substring(0,ident.length()-1);
            processMap.put(ident, model);
        }

        return processMap;
    }

    public ProcessModel interpret(String processModelType, ASTNode astNode, String identifer, Map<String, ProcessModel> processMap) throws CompilationException {
        ProcessModel model = null;
        switch(processModelType){
            case "automata":
                model = automaton.interpret(astNode, identifer, processMap);
                break;
            default:
                throw new CompilationException(getClass(),"Unable to find the process type: "+processModelType);
        }

        return model;
    }
}
