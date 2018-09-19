package mc.compiler;

import com.google.common.collect.Multiset;
import com.microsoft.z3.Context;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import mc.compiler.ast.ASTNode;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ConversionNode;
import mc.compiler.ast.ProcessNode;
import mc.compiler.ast.FunctionNode;

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
 * Compiler applies Interpreter  to processes {...} and seperatly
 *     applies OperationEvaluator to operation {...} that calls Interpreter
 * BUILDS Processes and stores them in Map<String, ProcessModel>.
 * Both petriNets and Automata are built and a MultiProcessModel is stored in processMap
 * MultiProcessModel = (Pp,Pa,mapping) with a mapping between nodes and places
 *
 *    MuitiProcess(P) = (Pp,Pa)
 * 1. All specs P = term  apply PertiInterpret to build Pp then Token Rule to Pa  - save (Pp,Pa)
 * 1a. P =A term  apply automataInterpret to build Pa the OwnersRule to Pp - save (Pp,Pa)
 * 2. Define all operators with params Pp,Pa or (Pp,Pa)  and return Pp or Pa
 * 3. The three element of an operation P = Q  can each be either Petri or Automata
 * 3 (1,3).   Term T  use interpretPetri(T) for Aut(T) use interpretAutomata(T)
 * 3 (2).   T ~ R  Use  T ~p R  for T ~A B use ~a
 *
 * The parser could set the type of the AST ProcessNode acording to "Aut" and "=A".
 * Both "~" and "~A" ...  can be added infixfunctions!
 *
 * A = term  adds (Ap,Aa) to processMap  this is then displaied - less hierarchy
 */
public class Interpreter {

  // fields
  private AutomatonInterpreter automatonInterpreter = new AutomatonInterpreter();
  private PetrinetInterpreter petrinetInterpreter = new PetrinetInterpreter();

//TODO  Document
  // This is called once from the compiler and builds all proesses
  // ONLY called from compiler
  public Map<String, ProcessModel> interpret(AbstractSyntaxTree ast,
                                            // LocalCompiler localCompiler,
                                             BlockingQueue<Object> messageQueue,
                                             Context context,
                                             Set<String> alpha)
      throws CompilationException, InterruptedException {

    //System.out.print("Who calls interpret Y? ");//Throwable t = new Throwable();t.printStackTrace();
    Map<String, ProcessModel> processMap = new LinkedHashMap<>();
// build all  processes including global sub processes
//    .getType tells us which to build ** .identifier its name  ** .process its definition
    List<ProcessNode> processes = ast.getProcesses();
    //System.out.println("AST processes "+ processes.stream().map(x->x.getIdentifier()).
    //  reduce("{",(x,y)->x+" "+y)+"}");
    for (ProcessNode process : processes) { //BUILDING ALL PROCESSES

        //System.out.println("Interpreter Building " + process.getIdentifier() + " ... "+ process.getType().toString());
        ProcessModel model = null;
        model = new MultiProcessModel(process.getIdentifier());
        model.setLocation(process.getLocation());

        //Either build petri first OR build automata FIRST
// Build petrinets (then build automata)
        String className = process.getProcess().getClass().getSimpleName();
        //System.out.println("className "+className);
        ProcessModel modelPetri = null;
        if (process.getType().contains("petrinet")) { //interpretASTAutNode

            modelPetri = petrinetInterpreter.interpret(process, processMap, context,alpha);

            //System.out.println("Interpreter Built Petri "+ modelPetri.getId());

      model = buildmpmFromPetri((Petrinet) modelPetri);
        } else if (process.getType().contains("automata")) { //interpretASTAutNode
          System.out.println("\n\nWARNING INTERPRETING AUTOMATA\n");
 /*           Automaton  aut = (Automaton) automatonInterpreter.interpret(process, processMap, context);
            modelPetri =  OwnersRule.ownersRule(aut);

            //System.out.println("Interpreter Built Automata "+ aut.getId());

            modelPetri.setLocation(process.getLocation());

            ((MultiProcessModel) model).addProcess(modelPetri);
/*

            HashMap<AutomatonNode, Multiset<PetriNetPlace>> nodeToMarking = new HashMap<>();
            HashMap<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = new HashMap<>();

            ProcessModel modelAut = TokenRule.tokenRule(
                    (Petrinet) ((MultiProcessModel) model)
                            .getProcess(ProcessType.PETRINET), markingToNode, nodeToMarking);
            //System.out.println("Built automata with tokenRule "+ ((Automaton) modelAut).myString());

            ((MultiProcessModel) model).addProcess(modelAut);
            ((MultiProcessModel) model)
                    .addProcessesMapping(new Mapping(nodeToMarking, markingToNode));
  */
   //         ((MultiProcessModel) model).addProcess(aut);

        }

        messageQueue.add(new LogAST("Built:", process));

      //SAVE MultiProcess in processMap
      processMap.put(process.getIdentifier(), model);


    }
      //System.out.println("End of inteterpret Y ");

    return processMap;

  }

/* Copy of code in Galois.... */
  public MultiProcessModel buildmpmFromPetri(Petrinet pet){

    MultiProcessModel model = new MultiProcessModel(pet.getId());
      //System.out.println("Interpreter Built Petri "+ modelPetri.getId());
      model.addProcess(pet);
     HashMap<AutomatonNode, Multiset<PetriNetPlace>> nodeToMarking = new HashMap<>();
     HashMap<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = new HashMap<>();

      ProcessModel modelAut = TokenRule.tokenRule(
        (Petrinet) model.getProcess(ProcessType.PETRINET), markingToNode, nodeToMarking);

      model.addProcess(modelAut);
      model.addProcessesMapping(new Mapping(nodeToMarking, markingToNode));
      return model;
  }
  /**
    called from  operation / equation evaluation and conversions

   (A=>B) ~ C.
   need Ap, Bp, => to be Petri Net  then TokenRule to Automata
   C look up Automata Ca or build Ca from Cp?
   S = ownersRule(P)   Pa->Sp, Sp-token->Sa
   */


  public ProcessModel interpret(String processModelType,
                                ASTNode astNode,
                                String identifer,
                                Map<String, ProcessModel> processMap,
                                Context context)
      throws CompilationException, InterruptedException {

    //System.out.println("\n CALLing interpret X  "+ processModelType+" "+astNode.toString()+" \n");
    //TODO Multy Keep to Petri Interpret!
    ProcessModel model;
    switch (processModelType) {
      case "forcedautomata":
      case "automata":
    //System.out.println("TO REMOVE interpret type SWITCH to "+processModelType);

        //System.out.println("***** interpret automata" );
        model = automatonInterpreter.interpret(astNode, identifer, processMap, context);
      //System.out.println(model.toString());
        break;

      case "petrinet":  //****
   //System.out.println("Interpreter.interpret still P net" +identifer+" "+astNode.toString());
        model = petrinetInterpreter.interpret(astNode, identifer, processMap, context);

        break;

      default:
        throw new CompilationException(getClass(), "Unable to find the process type: "
            + processModelType);
    }
      //System.out.println("End of Interpret  "+ model.getId());
    return model;
  }
}
