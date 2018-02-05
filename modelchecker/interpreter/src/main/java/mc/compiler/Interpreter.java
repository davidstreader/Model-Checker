package mc.compiler;

import com.microsoft.z3.Context;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import mc.compiler.ast.ASTNode;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ProcessNode;
import mc.compiler.interpreters.AutomatonInterpreter;
import mc.compiler.interpreters.PetrinetInterpreter;
import mc.exceptions.CompilationException;
import mc.processmodels.Mapping;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.util.LogAST;

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

      if (process.getType().contains("petrinet")) {
        ProcessModel modelPetri = petrinetInterpreter.interpret(process, processMap, localCompiler, context);
        modelPetri.setLocation(process.getLocation());
        if (model == null) { // If the model is not comprised of multiple types
          model = modelPetri;
        } else {
          ((MultiProcessModel) model).addProcess(modelPetri);
        }
      }


      if(process.getType().contains("forcedautomata")) {
        ProcessModel modelAut = automatonInterpreter.interpret(process, processMap, localCompiler, context);
        modelAut.setLocation(process.getLocation());
        if (model == null) { // If the model is not comprised of multiple types
          model = modelAut;
        } else {
          ((MultiProcessModel) model).addProcess(modelAut);
        }

      } else if (process.getType().contains("automata")) {
        ProcessModel modelAut;
        HashMap<AutomatonNode, Set<PetriNetPlace>> nodeToMarking = new HashMap<>();
        HashMap<Set<PetriNetPlace>, AutomatonNode> markingToNode = new HashMap<>();
        if(process.getType().contains("petrinet")) {
          modelAut = TokenRule.tokenRule((Petrinet) ((MultiProcessModel)model).getProcess(ProcessType.PETRINET), markingToNode, nodeToMarking);
        } else {
          modelAut = automatonInterpreter.interpret(process, processMap, localCompiler, context);
        }

        modelAut.setLocation(process.getLocation());
        if (model == null) { // If the model is not comprised of multiple types
          model = modelAut;
        } else {
          ((MultiProcessModel) model).addProcess(modelAut);
          ((MultiProcessModel) model).addProcessesMapping(new Mapping(nodeToMarking, markingToNode));
        }
      }

      System.out.print("Done!");

      messageQueue.add(new LogAST("Built:", process));

      System.out.println("Built "+ model.toString());

      processMap.put(process.getIdentifier(), model);
    }


    return processMap;
  }

  public ProcessModel interpret(String processModelType, ASTNode astNode, String identifer, Map<String, ProcessModel> processMap, Context context) throws CompilationException, InterruptedException {
    ProcessModel model;
    switch (processModelType) {
      case "forcedautomata":
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
