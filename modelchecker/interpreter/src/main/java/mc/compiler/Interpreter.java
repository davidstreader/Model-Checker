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
import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessModel;

import javax.xml.soap.SOAPPart;

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
      ProcessModel model = null;
      if (process.getType().size() == 0) {
        continue;
      }
      if (process.getType().size() > 1) {
        model = new MultiProcessModel(process.getIdentifier());
        model.setLocation(process.getLocation());
      }

      if (process.getType().contains("automata")) {
        ProcessModel modelAut = automatonInterpreter.interpret(process, processMap, localCompiler, context);
        modelAut.setLocation(process.getLocation());
        if (model == null) { // If the model is not comprised of multiple types
          model = modelAut;
        } else {
          ((MultiProcessModel) model).addProcess(modelAut);
        }
      }
      if (process.getType().contains("petrinet")) {
        ProcessModel modelPetri = petrinetInterpreter.interpret(process, processMap, localCompiler, context);
        modelPetri.setLocation(process.getLocation());
        if (model == null) { // If the model is not comprised of multiple types
          model = modelPetri;
        } else {
          ((MultiProcessModel) model).addProcess(modelPetri);
        }
      }
      System.out.print("Done!");
      System.out.println("Built "+ model.toString());

      processMap.put(process.getIdentifier(), model);
    }


    return processMap;
  }

  public ProcessModel interpret(String processModelType, ASTNode astNode, String identifer, Map<String, ProcessModel> processMap, Context context) throws CompilationException, InterruptedException {
    ProcessModel model;
    switch (processModelType) {
      case "automata":
        model = automatonInterpreter.interpret(astNode, identifer, processMap, context);
        break;

      case "petrinet":
        model = petrinetInterpreter.interpret(astNode, identifer, processMap, context);
        break;

      default:
        throw new CompilationException(getClass(), "Unable to find the process type: " + processModelType);
    }

    return model;
  }
}
