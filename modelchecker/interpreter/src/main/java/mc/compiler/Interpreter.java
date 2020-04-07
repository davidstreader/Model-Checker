package mc.compiler;

import com.google.common.collect.Multiset;
import com.microsoft.z3.Context;
//import com.sun.tools.javac.resources.compiler;
import mc.Constant;
import mc.compiler.ast.ASTNode;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ProcessNode;
import mc.compiler.interpreters.AutomatonInterpreter;
import mc.compiler.interpreters.PetrinetInterpreter;
import mc.exceptions.CompilationException;
import mc.processmodels.MappingNdMarking;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.util.LogMessage;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;

/**
 * Compiler applies Interpreter  to processes {...} and seperatly
 * applies OperationEvaluator to operation {...} that calls Interpreter
 * Incrementally BUILDS Processes and stores them in Map<String, ProcessModel>.
 * Both petriNets and Automata are built and a MultiProcessModel is stored in processMap
 *
 * MultiProcessModel = (Pp,Pa,mapping) with a mapping between nodes and places
 * <p>
 * MuitiProcess(P) = (Pp,Pa)
 * 1. All specs P = term  apply PertiInterpret to build Pp then Token Rule to Pa  - save (Pp,Pa)
 * 1a. P =A term  apply automataInterpret to build Pa the OwnersRule to Pp - save (Pp,Pa)
 * 2. Define all operators with params Pp,Pa or (Pp,Pa)  and return Pp or Pa
 * 3. The three element of an operation P = Q  can each be either Petri or Automata
 * 3 (1,3).   Term T  use interpretPetri(T) for Aut(T) use interpretAutomata(T)
 * 3 (2).   T ~ R  Use  T ~p R  for T ~A B use ~a
 * <p>
 * The parser could set the type of the AST ProcessNode acording to "Aut" and "=A".
 * Both "~" and "~A" ...  can be added infixfunctions!
 * <p>
 * A = term  adds (Ap,Aa) to processMap  this is then displaied - less hierarchy
 */
public class Interpreter {

  // fields
  private AutomatonInterpreter automatonInterpreter = new AutomatonInterpreter();

  private PetrinetInterpreter petrinetInterpreter = new PetrinetInterpreter();
  public PetrinetInterpreter getpetrinetInterpreter(){ return petrinetInterpreter;}
  private Set<String> alpha;


  /* This is called once from the compiler and builds all proesses
   RETURNS Map to  models with both PetriNets, Automata + mapping between them!
   catch exception and return completed automata
  */
  public ProcessMapFull interpret(AbstractSyntaxTree ast,
                                             // LocalCompiler localCompiler,
                                             BlockingQueue<Object> messageQueue,
                                             Context context,
                                             Set<String> alpha,
                                             boolean symb)
    throws CompilationException, InterruptedException {
    this.alpha = alpha;
    //System.out.println("*** Interp START ");
    StringBuilder sb = new StringBuilder();
    //System.out.print("Who calls interpret Y? ");//Throwable t = new Throwable();t.printStackTrace();
    Map<String, ProcessModel> processMap = new LinkedHashMap<>();  //already built proceesses
// build all  processes including global sub processes
//    .getType tells us which to build ** .identifier its name  ** .process its definition
    List<ProcessNode> processes = ast.getProcesses();
      //System.out.println("Interpretor building "+processes.size());
    //Collections.sort(processes,Comparator.comparing(ProcessNode::getDomain));
    //System.out.println("AST processes "+ processes.stream().map(x->x.getIdentifier()).
    //  reduce("{",(x,y)->x+" "+y)+"}");
      ProcessMapFull pmf = new ProcessMapFull();
      pmf.setIsFull(true);
    int i = 0;
      for (ProcessNode process : processes) { //BUILD ALL PROCESSES
        try {
            System.out.println("  Interpreter Building-"+i+" " + process.myString()); // + " ... "+ process.getType().toString());
            ProcessModel model = null; i++;
            model = new MultiProcessModel(process.getIdentifier());
            model.setLocation(process.getLocation());  //location on screen

            //Either build petri first OR build automata FIRST
            // Build petrinets (then build automata)
            String className = process.getProcess().getClass().getSimpleName();
            //System.out.println("className "+className);
            ProcessModel modelPetri = null;
            if (process.getType().contains("petrinet")) { //interpretASTAutNode
                modelPetri = petrinetInterpreter.interpret(process, processMap,
                    context, alpha, ast.getVariableMap(), symb);
                //System.out.println("++++++Interpreter Built Petri "+ modelPetri.getId());
                ((Petrinet) modelPetri).reown("");
                model = buildmpmFromPetri((Petrinet) modelPetri);
                //System.out.println(((MultiProcessModel) model).getProcessNodesMapping());
            } else if (process.getType().contains("automata")) { //interpretASTAutNode
                System.out.println("\n\nWARNING INTERPRETING AUTOMATA (should not occur)\n");
            }
            //messageQueue.add(new LogAST("Built:", process));

            int autSize = ((Automaton) ((MultiProcessModel) model).getProcess(ProcessType.AUTOMATA)).getNodeCount();
            sb.append(i+"- "+process.getIdentifier() + "("+autSize+"), ");
            processMap.put(process.getIdentifier() + ":" + process.getDomain(), model); //SAVE MultiProcess in processMap
            if (!process.getDomain().equals("*")) {
                processMap.put(process.getIdentifier() + ":*", model);  //used in display and unique name
            }
        } catch (CompilationException | ExecutionException e){
            pmf.setIsFull(false);
            sb.append("failed on "+ process.getIdentifier()+"  "+ e.getMessage() );
            e.printStackTrace();
             break;  // stop building

        }
    }
    //System.out.println("*** Interp " + this.alpha);
    //System.out.println("End of inteterpret  "+ processMap.keySet());
    messageQueue.add(new LogMessage("Built: "+ sb.toString(),  true,
      false,
      null,
      -1,
      Thread.currentThread()));
    System.out.println("Interpreter returning "+ processMap.size());

      pmf.setProcessMap(processMap);
      //Optional<Map<String, ProcessModel>> pM = Optional.of(processMap);
    return pmf;

  }

  /* Copy of code in Galois.... */
  public MultiProcessModel buildmpmFromPetri(Petrinet pet) {

    MultiProcessModel model = new MultiProcessModel(pet.getId());
  //System.out.println("Interpreter buildmpmFromPetri "+ pet.myString());
    model.addProcess(pet);
    HashMap<AutomatonNode, Multiset<PetriNetPlace>> nodeToMarking = new HashMap<>();
    HashMap<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = new HashMap<>();

    ProcessModel modelAut = TokenRule.tokenRule(
      (Petrinet) model.getProcess(ProcessType.PETRINET), markingToNode, nodeToMarking);
    //System.out.println("Interpreter buildmpmFromPetri "+ ((Automaton) modelAut).myString());
    model.addProcess(modelAut);
      //System.out.println("Interpreter buildmpmFromPetri "+((Automaton) modelAut).myString());
    model.addProcessesMapping(new MappingNdMarking(nodeToMarking, markingToNode, pet.getId(), modelAut.getId()));
    return model;
  }

  /**
   * called from  operation / equation evaluation and conversions
   * <p>
   * (A=>B) ~ C.
   * need Ap, Bp, => to be Petri Net  then TokenRule to Automata
   * C look up Automata Ca or build Ca from Cp?
   * S = ownersRule(P)   Pa->Sp, Sp-token->Sa
   */


  public ProcessModel interpretForOperations(String processModelType,
                                             ASTNode astNode,
                                             String identifer,
                                             Map<String, ProcessModel> processMap,
                                             Context context, Set<String> alpha)
      throws CompilationException, InterruptedException, ExecutionException {

    //System.out.println(" ****CALLing interpret X  " + processModelType + " " + astNode.toString() + " " + alpha);
    //TODO Multy Keep to Petri Interpret!
    ProcessModel model;
    switch (processModelType) {
      //case "forcedautomata":
      case Constant.AUTOMATA:
        //System.out.println("TO REMOVE interpret type SWITCH to "+processModelType);

        //System.out.println("***** interpret automata" );
        model = automatonInterpreter.interpretEvalOp(astNode, identifer, processMap, context, alpha);
        //System.out.println("***Interpreter.interpret A alpha " + this.alpha + " id " + identifer + " " + astNode.myString());
        //System.out.println(model.toString());
        break;

      case Constant.PETRINET:  //****
        //System.out.println("***Interpreter.interpret still P alpha " + this.alpha + " id " + identifer + " " + astNode.myString());
        model = petrinetInterpreter.interpretEvalOp(astNode, identifer, processMap, context, alpha);

        break;

      default:
        throw new CompilationException(getClass(), "Unable to find the process type: "
          + processModelType);
    }
    //System.out.println("End of Interpret  "+ model.getId());
    return model;
  }

  /*public Automaton getAut (Map<String, ProcessModel> processMap,
                                 Interpreter interpreter,
                                 Context context,
                                 Set<String> alpha,
                                 ASTNode ast) throws CompilationException, InterruptedException {
    //System.out.println("getAut");
    //Automaton  a = petrinetInterpreter.getAutomaton (processMap,interpreter,context,alpha, ast);
    Automaton a = petrinetInterpreter.getLocalAutomaton(context,alpha, ast);
    //System.out.println("getAut RETURNS "+a.getId());
  return a;
  } */


  public  class ProcessMapFull {
      private  Map<String, ProcessModel> pMap = new TreeMap<>();
      private  boolean isFull;
      public ProcessMapFull(){};
      public  void setProcessMap(Map<String, ProcessModel> p) {
          pMap = p;
      }
      public  Map<String, ProcessModel> getProcessMap(){return pMap;}
      public  void setIsFull(boolean b) { isFull = b;}
      public  boolean getIsFull() {return isFull;}
    }
}
