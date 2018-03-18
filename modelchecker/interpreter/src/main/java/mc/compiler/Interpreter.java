package mc.compiler;

import com.microsoft.z3.Context;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.conversion.OwnersRule;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.util.LogAST;

/**
 * BUILDS Processes and stores them in Map<String, ProcessModel>.
 */
public class Interpreter {

  // fields
  private AutomatonInterpreter automatonInterpreter = new AutomatonInterpreter();
  private PetrinetInterpreter petrinetInterpreter = new PetrinetInterpreter();

//TODO  Document or remove this Design  HACK1
  // This is where what is build is decided
  //ONLY called from compiler  HACK other palces call the method below
  public Map<String, ProcessModel> interpret(AbstractSyntaxTree ast,
                                            // LocalCompiler localCompiler,
                                             BlockingQueue<Object> messageQueue,
                                             Context context)
      throws CompilationException, InterruptedException {

    Map<String, ProcessModel> processMap = new LinkedHashMap<>();
//build the processes
    List<ProcessNode> processes = ast.getProcesses();
    System.out.println("AST processes "+ processes.stream().map(x->x.getIdentifier()).
      reduce("{",(x,y)->x+" "+y)+"}");
    for (ProcessNode process : processes) {

 System.out.print("Building " + process.getType() + " " + process.getIdentifier() + "...");
      ProcessModel model = null;
      if (process.getType().size() == 0) {
 System.out.println("skip");
        continue;
      }
      if (process.getType().size() > 1) {
        model = new MultiProcessModel(process.getIdentifier());
        model.setLocation(process.getLocation());
      }
//***** TWO ways to interpret  WHY?
      if (process.getType().contains("petrinet")) {
 System.out.println("Build PetriNet "+ process.getIdentifier());
        ProcessModel modelPetri = petrinetInterpreter.interpret(process,
                    processMap, context);
        //System.out.println("XXX "+((Petrinet) modelPetri).myString());
        modelPetri.setLocation(process.getLocation());
        if (model == null) { // If the model is not comprised of multiple types
          model = modelPetri;
        } else {
          ((MultiProcessModel) model).addProcess(modelPetri);
        }
      }


      if (process.getType().contains("forcedautomata")) {

        Automaton modelAut = (Automaton) automatonInterpreter.interpret(process,
             processMap,
         //   localCompiler,
              context);

        modelAut.setLocation(process.getLocation());
        if (model == null) { // If the model is not comprised of multiple types
          model = modelAut;
        } else {
          ((MultiProcessModel) model).addProcess(modelAut);
        //  System.out.println("WHAT THE HELL\n");
          ((MultiProcessModel) model).addProcess(OwnersRule.ownersRule(modelAut));
        }

      } else if (process.getType().contains("automata")) {
 System.out.print("Build automata "+ process.getIdentifier());
        ProcessModel modelAut;
        HashMap<AutomatonNode, Set<PetriNetPlace>> nodeToMarking = new HashMap<>();
        HashMap<Set<PetriNetPlace>, AutomatonNode> markingToNode = new HashMap<>();
        if (process.getType().contains("petrinet")) {
 System.out.println(" FROM petriNet");
          modelAut = TokenRule.tokenRule(
              (Petrinet) ((MultiProcessModel) model)
                  .getProcess(ProcessType.PETRINET), markingToNode, nodeToMarking);
        } else {
 System.out.println(" Directly");
          modelAut = automatonInterpreter.interpret(process,
                 processMap,
             //    localCompiler,
                 context);
        }

        modelAut.setLocation(process.getLocation());
        if (model == null) { // If the model is not comprised of multiple types
          model = modelAut;
        } else {
          ((MultiProcessModel) model).addProcess(modelAut);
          ((MultiProcessModel) model)
              .addProcessesMapping(new Mapping(nodeToMarking, markingToNode));
        }
      }


      messageQueue.add(new LogAST("Built:", process));

      //System.out.println("XXX "+((Petrinet) model).myString());

      processMap.put(process.getIdentifier(), model);
      System.out.println("Compiler Interpreter Done! "+ processMap.keySet());

    }
    return processMap;

  }
//called from outside compiler - including PetriNetInterpretor  conversion!
  public ProcessModel interpret(String processModelType,
                                ASTNode astNode,
                                String identifer,
                                Map<String, ProcessModel> processMap,
                                Context context)
      throws CompilationException, InterruptedException {
    ProcessModel model;
    switch (processModelType) {
      case "forcedautomata":
      case "automata":
    System.out.println("***** interpret automata" );
        model = automatonInterpreter.interpret(astNode, identifer, processMap, context);
      //  System.out.println(model.toString());
        break;

      case "petrinet":  //****
   System.out.println("***** interpret petrinet" );
        model = petrinetInterpreter.interpret(astNode, identifer, processMap, context);

        break;

      default:
        throw new CompilationException(getClass(), "Unable to find the process type: "
            + processModelType);
    }

    return model;
  }
}
