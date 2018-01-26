package mc.compiler;

import com.microsoft.z3.Context;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import mc.compiler.ast.ASTNode;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ProcessNode;
import mc.compiler.interpreters.AutomatonInterpreter;
import mc.compiler.interpreters.PetrinetInterpreter;
import mc.exceptions.CompilationException;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.petrinet.Petrinet;
import mc.util.LogAST;

import static mc.processmodels.conversion.tokenRule.tokenRule;

/**
 * Created by sheriddavi on 24/01/17.
 */
public class Interpreter {

  // fields
  private AutomatonInterpreter automatonInterpreter = new AutomatonInterpreter();
  private PetrinetInterpreter petrinetInterpreter = new PetrinetInterpreter();

  public Map<String, ProcessModel> interpret(AbstractSyntaxTree ast, LocalCompiler localCompiler, BlockingQueue<Object> messageQueue, Context context) throws CompilationException, InterruptedException {
    Map<String, ProcessModel> processMap = new LinkedHashMap<>();

    List<ProcessNode> processes = ast.getProcesses();
    for (ProcessNode process : processes) {
      System.out.print("\nBuilding " + process.getType() + " " + process.getIdentifier() + "...");
      ProcessModel model;
      switch (process.getType()) {
        case "processes": // If it is not a automata or petrinet then dont construct it (Its a non-drawn process)
          continue;

        case "automata":
          model = automatonInterpreter.interpret(process, processMap, localCompiler, context);
          model.setLocation(process.getLocation());
          break;

        case "petrinet":
          model = petrinetInterpreter.interpret(process, processMap, localCompiler, context);
          model.setLocation(process.getLocation());


          break;

        default:
          throw new CompilationException(getClass(), "Unable to find the process type: " + process.getType());
      }

      System.out.print("Done!");

      processMap.put(process.getIdentifier(), model);
      if(model instanceof Petrinet) {
        Automaton createdAutomaton = tokenRule((Petrinet) model);
        processMap.put(createdAutomaton.getId(), createdAutomaton);
      }

    }


    return processMap;
  }

  public ProcessModel interpret(String processModelType, ASTNode astNode, String identifer, Map<String, ProcessModel> processMap, Context context) throws CompilationException, InterruptedException {
    ProcessModel model;
    switch (processModelType) {
      case "automata":
        model = automatonInterpreter.interpret(astNode, identifer, processMap, context);
        break;
      default:
        throw new CompilationException(getClass(), "Unable to find the process type: " + processModelType);
    }

    return model;
  }
}
