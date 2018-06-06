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
 * BUILDS Processes and stores them in Map<String, ProcessModel>.
 * When both petriNets and Automata are built then ONE entry in process Model
 * a MultiProcessModel object is stored with mapping between nodes and places
 * Ditch the Automata Interpreter!
 *    Every thing is --- MuitiProcess(P) = (Pp,Pa)
 * 1. All specs P=.. Perti Interpret to Pp then Token Rule to Pa  Tick
 * 2.   P = ownersRule(Q).  from Qa build Pp then Token to Pa     T
 * 3.   P = TokenRule(Q).   from Qp build Pa then Owners to Pp
 * 4.   P = a2p2a(Q).  from Qa Owners to build Pp then Token to Pa
 * 5.   A ~ B  Use Aa ~ Ba                                        Tick
 * 6.   P = simp/abs(Q).    first Pa= abs(Qa) then Owners to Pp
 * 7.   S = P{Q/a}    first Sp =  Pp{Qp/A}  then Token to Sa
 *
 * Control of what is to be displaid ?
 */
public class Interpreter {

  // fields
  private AutomatonInterpreter automatonInterpreter = new AutomatonInterpreter();
  private PetrinetInterpreter petrinetInterpreter = new PetrinetInterpreter();

//TODO  Document
  // This is called once and builds all proesses
  // ONLY called from compiler
  public Map<String, ProcessModel> interpret(AbstractSyntaxTree ast,
                                            // LocalCompiler localCompiler,
                                             BlockingQueue<Object> messageQueue,
                                             Context context)
      throws CompilationException, InterruptedException {

    //System.out.print("Who calls interpret? ");Throwable t = new Throwable();t.printStackTrace();
    Map<String, ProcessModel> processMap = new LinkedHashMap<>();
// build all  processes including global sub processes
//      BUT .getType tells us which to build
    List<ProcessNode> processes = ast.getProcesses();
    //System.out.println("AST processes "+ processes.stream().map(x->x.getIdentifier()).
    //  reduce("{",(x,y)->x+" "+y)+"}");
    for (ProcessNode process : processes) { //BUILDING ALL PROCESSES

 //System.out.println("  Interpreter Building " + process.getIdentifier() + " ... "+ process.getType().toString());
        ProcessModel model = null;
        model = new MultiProcessModel(process.getIdentifier());
        model.setLocation(process.getLocation());

        //Either build petri first OR build automata FIRST
// Build petrinets (then build automata)
        String className = process.getProcess().getClass().getSimpleName();
        //System.out.println("className "+className);
        ProcessModel modelPetri = null;
        if (process.getProcess() instanceof FunctionNode &&
                !(((FunctionNode) process.getProcess()).getFunction().equals("abs") ||
                   ((FunctionNode) process.getProcess()).getFunction().equals("simp"))) { //interpretASTAutNode
            Automaton modelAut = null;
            modelAut = petrinetInterpreter.interpretASTAutNode(process.getProcess(),process.getIdentifier());

            modelPetri =  OwnersRule.ownersRule( modelAut);
            ((MultiProcessModel) model).addProcess(modelAut); //debugging
            ((MultiProcessModel) model).addProcess(modelPetri);
        } else {
            modelPetri = petrinetInterpreter.interpret(process, processMap, context);

  //System.out.println("Built PetriNet "+ ((Petrinet)  modelPetri).myString());// process.getIdentifier());

        modelPetri.setLocation(process.getLocation());

        ((MultiProcessModel) model).addProcess(modelPetri);


        HashMap<AutomatonNode, Multiset<PetriNetPlace>> nodeToMarking = new HashMap<>();
        HashMap<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = new HashMap<>();
      /*
        Token rule works here but can not be called from petrinetInterpreter
       */
            //System.out.println("interp 3 "+ ((Petrinet) modelPetri).myString()); //FAIL
         ProcessModel modelAut = TokenRule.tokenRule(
              (Petrinet) ((MultiProcessModel) model)
                  .getProcess(ProcessType.PETRINET), markingToNode, nodeToMarking);
   //System.out.println("Built automata with tokenRule "+ ((Automaton) modelAut).myString());

          ((MultiProcessModel) model).addProcess(modelAut);
          ((MultiProcessModel) model)
              .addProcessesMapping(new Mapping(nodeToMarking, markingToNode));
        }

      messageQueue.add(new LogAST("Built:", process));

      //SAVE MultiProcess in processMap
      processMap.put(process.getIdentifier(), model);
     /*System.out.println("Process Map "+processMap.keySet().stream().
       map(x->x+ " " + processMap.get(x).getProcessType()+ " "+ processMap.get(x).getId()).
             reduce("",(x,y)->x+"->"+y)); */
      //System.out.println("Compiler Interpreter DONE! "+ processMap.keySet());

    }
    return processMap;

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

    System.out.println("\n CALLing interpret "+ processModelType+" "+astNode.toString()+" \n");
    //TODO Multy Keep to Petri Interpret!
    ProcessModel model;
    switch (processModelType) {
      case "forcedautomata":
      case "automata":
    //System.out.println("TO REMOVE interpret type SWITCH to "+processModelType);

        //System.out.println("***** interpret automata" );
        model = automatonInterpreter.interpret(astNode, identifer, processMap, context);
      //  System.out.println(model.toString());
        break;

      case "petrinet":  //****
   //System.out.println("Interpreter.interpret still P net" );
        model = petrinetInterpreter.interpret(astNode, identifer, processMap, context);

        break;

      default:
        throw new CompilationException(getClass(), "Unable to find the process type: "
            + processModelType);
    }

    return model;
  }
}
