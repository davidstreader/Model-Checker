package mc.compiler;

import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ProcessNode;
import mc.compiler.interpreters.AutomatonInterpreter;
import mc.process_models.ProcessModel;

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

    public Map<String, ProcessModel> interpret(AbstractSyntaxTree ast){
        Map<String, ProcessModel> processMap = new HashMap<String, ProcessModel>();

        List<ProcessNode> processes = ast.getProcesses();
        for(ProcessNode process : processes){
            ProcessModel model = null;
            switch(process.getType()){
                case "automata":
                    model = automaton.interpret(process, processMap);
                    break;
                default:
                    System.out.println("ERROR: " + process.getType());
                    // TODO: throw error
            }

            processMap.put(process.getIdentifier(), model);
        }

        return processMap;
    }
}
