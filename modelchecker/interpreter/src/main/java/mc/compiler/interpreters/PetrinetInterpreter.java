package mc.compiler.interpreters;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import mc.Constant;
import mc.compiler.Guard;
import mc.compiler.Interpreter;
import mc.compiler.ast.*;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.plugins.IProcessInfixFunction;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.operations.ChoiceFun;
import mc.processmodels.automata.operations.SequentialInfixFun;
import mc.processmodels.conversion.OwnersRule;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
import mc.processmodels.petrinet.operations.PetrinetReachability;
import mc.processmodels.petrinet.operations.RefineFun;
import mc.processmodels.petrinet.utils.PetrinetLabeller;
import mc.util.expr.ExpressionPrinter;
//import mc.operations.functions.AbstractionFunction;
import java.util.*;
import java.util.stream.Collectors;

import static mc.util.Utils.instantiateClass;

//import com.sun.tools.hat.internal.model.Root;

/**
 * input Atomic AST output Atomic PetriNets
 * Refactored to work on the return (processing after recursive call)
 * Recurse to the leaves of the AST at leaves use
 * Primitive one Place Zero transitions for STOP and ERROR
 * Action prefixing a->P   uses a  Primitive two Place one transition for an action 'a'
 * then converts a->P into Prim(a)=>P
 * <p>
 * All branches accept nets from below and return nets to above!
 * <p>
 * Local Refetences are added in referenceNode
 * References are transfered from astNode to Places and a TREE net built
 * later this may be converted into a cyclic automata
 * <p>
 * Symbolic PetriNets:  input symbolic AST output symbolic PetriNets
 *
 * @author Jacob Beal  David and others
 * @see AutomatonInterpreter
 * @see mc.processmodels.petrinet.Petrinet
 * @see PetriNetPlace
 * @see PetriNetTransition
 */


public class PetrinetInterpreter implements ProcessModelInterpreter {
  static int sid = 0;
  static String indent = "";
  static Map<String, Class<? extends IProcessInfixFunction>> infixFunctions = new HashMap<>();
  static Map<String, Class<? extends IProcessFunction>> functions = new HashMap<>();
  Context context;
  /* referenceSet = local references including root of current Process
     processMap = Global prcesses
  Y = b->STOP.
  Z =  a->X|c->Y,
       X = x->X.
  processMap Y->     referenceSet   Z->   X->
  */
  Set<String> referenceSet = new HashSet<>();
  //Set<String> fromReferenceSet = new HashSet<>();
  Map<String, ProcessModel> processMap = new HashMap<>();
  // private Stack<Petrinet> processStack = new Stack<>();  // SIDE effect methods push and pop processes
  //LocalCompiler compiler;
  Set<String> variableList;
  int subProcessCount = 0;
  VariableSetNode variables;
  Set<String> alpha;
  boolean symb;
  Map<String, Expr> globalVarMap;
  Map<String, Petrinet> localProcesses = new TreeMap<>(); //THIS might be only for symb?
  List<String> localProcessesToCome;
  List<String> vars = new ArrayList<>();

  boolean selfRef = true;

  public String myString(Map<String, Set<PetriNetPlace>> ref) {
    return ref.keySet().stream().
      map(x -> x + " " +
        ref.get(x).stream().map(y -> y.getId()).reduce((z, w) -> z + " " + w) + ": ").
      reduce((x, y) -> x + " " + y) + "";
  }

  public PetrinetInterpreter() {
  }

  public void setProcessMap(Map<String, ProcessModel> processMap) {
    this.processMap = processMap;
  }

  public PetrinetInterpreter(Map<String, ProcessModel> processMap) {
    this.processMap = processMap;
  }

  /**
   * Called from interpreter  for each processNode called from COMPILER
   * Executed once at the start of building each process
   * <p>
   * A = P[0]    P may be local Process   (interpret)
   * A ~ B      A and B must be defined global processes  (interpretEvalOp)
   *
   * @param processNode
   * @param processMap
   * @param context
   * @return
   * @throws CompilationException
   * @throws InterruptedException
   */
  @Override
  public ProcessModel interpret(ProcessNode processNode,  // new global process
                                Map<String, ProcessModel> processMap,
                                //will hold botth PN and Aut for past Processes
                                Context context,
                                Set<String> alpha,
                                Map<String, Expr> globalVarMap,  //needed for symbolic
                                boolean symb)
    throws CompilationException, InterruptedException {
      if (processNode.getIdentifier().equals(Constant.CRASHME)) {
          throw new
           CompilationException(processNode.getClass(), " debugging CRASHME",processNode.getLocation());
      }
    reset();
    this.symb = symb;
    this.globalVarMap = globalVarMap;
    //called by Interpreter and  Returns a ProcessModel that the Interpreter adds to the processMap
    this.alpha = alpha;
    //System.out.println(">>> PetriInterpret " + processNode.myString() + " \n   processmap  " + asString(processMap));

    this.context = context;
    variableList = new HashSet<>();

    this.processMap = processMap;
    String identifier = processNode.getIdentifier();
    this.variables = processNode.getSymbolicVariables();
    Map<String, String> eval = new TreeMap<>();
    //System.out.println("***123 processNode " + processNode.getProcess().toString());
    //NOT USED CURRENTLY  conversions implicit  for testing use a2p2a( )
    //ABOVE NOT USED
//below pushes a petri net onto the process stack
    String what = processNode.getProcess().myString();
   /* //System.out.println("**Petriinterpret**Calling  " +
      processNode.getIdentifier() + " " + what + " localP " + localProcessesToCome + "\n");*/
//Needed for Gluing
    //interpretProcess(processNode.getProcess(), identifier);
    //Petrinet petrinet = processStack.pop().reId("");

    /*  localProcesses only exist on symbolic Nets
           build and store the local PetriNets
     */

    if (symb) {
      List<String> bits = getBits(what);
      for (LocalProcessNode lpn : ((ProcessNode) processNode).getLocalProcesses()) {
        //System.out.println("  ***LP " + lpn.myString());
        //System.out.println("bits " + bits.toString()+" id "+lpn.getIdentifier());
        vars = lpn.getRanges().getRanges().stream().map(x -> x.getVariable()).collect(Collectors.toList());
        Petrinet pn = interpretASTNode(lpn.getProcess(), "fake");
        pn.getAllRoots().stream().forEach(x -> x.getReferences().add(lpn.getIdentifier()));
        //System.out.println("    LocalProcess " + pn.myString("edge"));
        int i = 1;
        for (String var : vars) {
          eval.put(var, bits.get(i++));
        }
        localProcesses.put(lpn.getIdentifier(), pn.reId(lpn.getIdentifier()));
      }
    } else {
      localProcesses = new TreeMap<>();
    }

    Petrinet pet = interpretProcess(processNode.getProcess(), identifier, symb, localProcesses);
    //System.out.println("\nPetriInterp  " + pet.myString());
    Petrinet petrinet =  pet.copy(); //must be copied as equati
    petrinet.setRootEvaluation(eval);

    //replaced by single line below
    //Petrinet petrinet = interpretASTNode(processNode.getProcess());
    //System.out.println("\nPetriInterp setRoot "+petrinet.myString());
    //System.out.println("Just Built "+petrinet.myString());
    //System.out.println("Ref "+ referenceSet.toString());

    ////System.out.println("Poped "+petrinet.myString());
    if (!petrinet.getId().equalsIgnoreCase(processNode.getIdentifier())) {
      petrinet.setId(processNode.getIdentifier());
    }
    //System.out.println("\nPoped1 "+petrinet.myString());

    if (processNode.hasRelabels()) {
      processRelabelling(petrinet, processNode.getRelabels());
    }
    //System.out.println("Relabeled "+petrinet.myString());

    if (processNode.hasHiding()) {
      processHiding(petrinet, processNode.getHiding());
    }
    //System.out.println("<<<   End of PetriNetInterpreting " + processNode.myString() + "\n returns  " + petrinet.myString());
    return petrinet;  //to be added to ProcessMap along with Aut (by Interpreter)
  }

  /**
   * called from interpreter that has been called from evalOp
   *
   * @param astNode
   * @param identifier
   * @param processMap // the pre built processes
   * @param context
   * @return
   * @throws CompilationException
   * @throws InterruptedException
   */

  public ProcessModel interpretEvalOp(ASTNode astNode,
                                      String identifier,
                                      Map<String, ProcessModel> processMap,
                                      Context context,
                                      Set<String> alpha)

    throws CompilationException, InterruptedException {
    reset();

    //System.out.println("interpret YY START "+identifier+" pMap "+ processMap.keySet());
    this.context = context;
    this.processMap = processMap;
    this.alpha = alpha;
    //interpretProcess(astNode, identifier);  //Side effect PetriNet is now on stack
    //Petrinet petrinet = ((Petrinet) processStack.pop()).copy();
    // replaced by single line below
    Petrinet petrinet = interpretASTNode(astNode); //RECURSIVE CALL the process is an ASTNode

    //processMap.put(petrinet.getId(),petrinet);
    //System.out.println("INTerpret YY END  "+ identifier);
    return petrinet;
  }

  //USED for debugging
  public static String asString(Map<String, ProcessModel> in) {
    String out = " Map cnt " + in.size() + "  ";
    for (String key : in.keySet()) {
      out += key + "->" + in.get(key).getId() + ", ";
    }
    return out;
  }

  /**
   * Main component called ONLY when building processes NOT in evalOp
   * Symbolic => local processes already built
   * <p>
   * <p>
   * Processes are  built and then returned
   *
   * @param astNode
   * @param identifier
   * @throws CompilationException
   * @throws InterruptedException
   */
  private Petrinet interpretProcess(ASTNode astNode,
                                    String identifier, //Root id
                                    boolean symb, Map<String, Petrinet> localProcesses)
    throws CompilationException, InterruptedException {
    //prity print AST
    //System.out.println("   interpretProcess (PN) astNode IS " + astNode.myString());
    //System.out.println("289 "+asString(processMap));
    //System.out.println("processMap keys " + processMap.keySet());
    PetrinetInterpreter.indent = PetrinetInterpreter.indent.concat("-");
    String className = astNode.getClass().getSimpleName();
    //System.out.println("interpret Pro " + PetrinetInterpreter.indent + " className "+className+ " identifier "+ identifier);
//  get the petri net from the processMap and push onto the stack
    if (astNode instanceof IdentifierNode) {  // ONLY for D = A.
// If Atomic THEN must have been defined
// If symbolic THEN could be local hence Add Reference to Root (for Tree2Net
//             may have literal index so add index to Root
//                     index not literal is ERROR
      String reference = (((IdentifierNode) astNode).getIdentifier());

      //System.out.println("*** interpretProcess IdentifierNode " + ((IdentifierNode) astNode).myString());
      if (!symb) {
        if (!reference.contains(":")) {
          reference = ((IdentifierNode) astNode).getIdentifier() + ":" +
            ((IdentifierNode) astNode).getDomain();
        }
        if (processMap.get(reference).getProcessType().equals(ProcessType.MULTI_PROCESS)) {
          //System.out.println("interpretProcess GETS *********** MULTI_PROCESS -> PN");
          //processStack.push(); //What a way to extact  a net
          Petrinet pn = processMap.get(reference).getProcessType().
            convertTo(ProcessType.PETRINET, processMap.get(reference));
          //System.out.println("X*X "+pn.myString());
          return pn;
        } else {
          //processStack.push((Petrinet) processMap.get(reference));
          Petrinet pn = ((Petrinet) processMap.get(reference));
          //System.out.println("Y*Y "+pn.myString());
          List<String> bits = getBits(reference);
          if (bits.get(0).equals(Constant.END)) {
            for (PetriNetPlace pl : pn.getPlaces().values()) {
              if (pl.isStart()) pl.setTerminal(Constant.END);
            }
          }
          //System.out.println("Look for END \n" + pn.myString());
          return pn;
        }
      } else { //symbolic //  a->P[1]   bits = [P, 1]
        //make the local process with the correct name the main process
        List<String> bits = getBits(reference);
        Petrinet pn = localProcesses.get(bits.get(0));
        if (bits.get(0).equals(Constant.END)) {
          for (PetriNetPlace pl : pn.getPlaces().values()) {
            if (pl.isStart()) pl.setTerminal(Constant.END);
          }
        }

        //now set the root evaluation
        int i = 1;
        Map<String, String> eval = new TreeMap<>();
        //System.out.println("vars " + vars);
        for (String key : vars) {
          eval.put(key, bits.get(i++));
        }
        //System.out.println("eval " + eval.toString()); // Just for root NOT for edges
        // now add, unlinked, any other local processes
        for (String key : localProcesses.keySet()) {
          if (!key.equals(bits.get(0))) {
            pn.joinNet(localProcesses.get(key));
          }
        }
        //System.out.println("SYMBOLIC prior to tree2net "+pn.myString("edge"));
        //tree2net should link them
        pn = tree2net(pn);  // SYMBOLIC
        //System.out.println("SYMBOLICafter to tree2net "+pn.myString("edge"));
        // processMap.put(pn.getId(),pn);  This is done in Interpreter NOT here?
        return pn;
      }
      //System.out.println("got Net "+processStack.peek().getId()+" from Map");
    } else if (astNode instanceof ProcessRootNode) {  //Optional Start of Net building
      //System.out.println("*** interpretProcess ProcessRootNode "+astNode.myString());
      ProcessRootNode root = (ProcessRootNode) astNode;
      // interpretProcess(root.getProcess(), identifier); //build new petri net and push on the stack
      //  Petrinet petrinet = processStack.pop().reId("");
      // replaced by single line below
      Petrinet petrinet = interpretASTNode(root.getProcess()); //RECURSIVE CALL the process is an ASTNode
      //System.out.println("\n*** ProcessRootNode poped petri "+petrinet.myString());

      petrinet = processLabellingAndRelabelling(petrinet, root); //888888
      //System.out.println("TO stack petri " + petrinet.getId());
      if (root.hasHiding()) {
        //should be COPY  see above
        processHiding(petrinet, root.getHiding());
      }
      //System.out.println("prior to tree2net "+petrinet.myString("edge"));
      petrinet = tree2net(petrinet);
      //System.out.println("after to tree2net "+petrinet.myString("edge"));
      //System.out.println("  *** ProcessRootNode petri "+petrinet.getId());
      //processStack.push(petrinet);
      return petrinet;
    } else {//neither IdentifierNode not RootNode
      //System.out.println("********* PETRIINTERPRETER ELSE " + identifier + " " + astNode.getClass().getSimpleName());
      Petrinet petrinet = new Petrinet(identifier, true);
      if (variables != null) {
        petrinet.setHiddenVariables(variables.getVariables());
        petrinet.setHiddenVariablesLocation(variables.getLocation());
      }
      if (variableList == null) {
        variableList = new HashSet<>();
      }
      //BUILD THE PETRI TREE
      //System.out.println(" interpretProcess " + identifier + " processmap  " + asString(processMap));
      petrinet = interpretASTNode(astNode, identifier); // PETRI TREE is built and returned
      //System.out.println("\n***PetriInterpret LocalProcessNode ");

      //System.out.println("  ***ELSE  petri "+petrinet.myString());

      //System.out.println("ELSE prior to tree2net "+petrinet.myString("edge"));
      petrinet = tree2net(petrinet);  //ELSE
      //System.out.println("ELSE after to tree2net "+petrinet.myString("edge"));
      //System.out.println("  ***ELSE ***tree2net petri "+petrinet.myString());
      return petrinet;
    }
    //DEAD LINE
  }

  /**
   * Interpreter first builds a "tree like" acyclic Petri Net with referances attched to (single Place) markings.
   * This method builds cyclic Petei Nets by glueing some leaves back to the refenanced Places
   *
   * @param petri
   * @return
   * @throws CompilationException
   */
  private Petrinet tree2net(Petrinet petri) throws CompilationException {
    // filter ref or fromRef places
    //System.out.println("\nTree2Net START" + petri.myString("edge"));
    Set<PetriNetPlace> plsRef =
      petri.getPlaces().values().stream().
        filter(x -> x.getReferences().size() > 0).collect(Collectors.toSet());
    Set<PetriNetPlace> placesOnLeaf =
      petri.getPlaces().values().stream().
        filter(x -> x.getLeafRef().size() > 0).collect(Collectors.toSet());
    //System.out.println("refR"+plsRef.stream().map(x->x.getId()).reduce("** ",(x,y)->x+" "+y));
    //System.out.println("refF"+placeOnLeaf.stream().map(x->x.getId()).reduce("** ",(x,y)->x+" "+y));
    // glue pairs of places where one has a ref and the other the same fromRef

    for (PetriNetPlace pl1 : plsRef) {
      for (PetriNetPlace placeOnLeaf : placesOnLeaf) {
        //System.out.println("\nRef  " + pl1.myString() + " " + pl1.getIncoming().size() + " " + pl1.getOutgoing().size());
        //System.out.println("From " + placeOnLeaf.myString() + " " + placeOnLeaf.getIncoming().size() + " " + placeOnLeaf.getOutgoing().size());
        if (placeOnLeaf.getId().equals(pl1.getId())) {
          continue;  // Do not try to glue a node to itself!
        }

        for (String leafRf : placeOnLeaf.getLeafRef()) {
          //System.out.println("leafRef " + leafRf);
          List<String> bits = getBits(leafRf, "\\.");
          //System.out.println("leafRef " + leafRf + "  frm " + bits.get(0) + " size " + bits.size());
          //System.out.println("Ref     " + pl1.getReferences());
          //System.out.println(pl1.getReferences() + " cont " + leafRf);
          if (pl1.getReferences().contains(leafRf)) {
            //System.out.println("Joining " + pl1.getId() + " " + placeOnLeaf.getId());
            placeOnLeaf.setTerminal(""); // Will be overwrite if END Reference
            //Petrinet pn = localProcesses.get(from);


            Multimap<String, String> prodNames =  // need to get new pl1 Place
              petri.gluePlaces(Collections.singleton(pl1), Collections.singleton(placeOnLeaf));
            //System.out.println("tree2net Petri "+petri.myString("edge"));
            String spl1 = prodNames.values().iterator().next();
            //System.out.println("new nodes " + prodNames.values());
            if (bits.size() > 1 && bits.get(1).equals(Constant.END)) {
              pl1.setTerminal(Constant.END);
              petri.getPlace(spl1).setTerminal(Constant.STOP);
              //System.out.println("Pingo");
            }
            //System.out.println(petri.myString());
            pl1 = petri.getPlace(spl1);
          }
        }
      }
      petri.buildAlpahbetFromTransiations();
      // break;// ONLY one FromRef
    }

    //System.out.println("Tree2Net 1"+petri.myString("edge"));
    petri.setRootFromStart();
    petri.setEndFromPlace();
    // remove referances else system retries
    petri.getPlaces().values().stream().
      filter(x -> x.getReferences().size() > 0).forEach(x -> x.setReferences(new LinkedHashSet<>()));
    petri.getPlaces().values().stream().
      filter(x -> x.getLeafRef().size() > 0).forEach(x -> x.setLeafRef(new LinkedHashSet<>()));
    //System.out.println("Tree2Net END" + petri.myString("edge"));
    return petri;
  }


  //only used to add a fake Id
  private Petrinet interpretASTNode(ASTNode currentNode)
    throws CompilationException, InterruptedException {
    Petrinet pn = interpretASTNode(currentNode, "fake");
    //System.out.println("Fake");
    return pn;
  }


  /*  Recursive WORK HORSE
      Build a petrinet defined from currentNode  Recurse down the AST
      NOTE only builds a PETRI TREE NOT Petri Net
      Functions Automaton -> Automaton  should  appear at head of AST and
      for computational reasons be processed seperatly

   */
  public Petrinet interpretASTNode(ASTNode currentNode, String petriId)
    throws CompilationException, InterruptedException {
    //System.out.println("Interpreter ASTNode " + petriId + " " + currentNode.myString());

    if (Thread.currentThread().isInterrupted()) {
      throw new InterruptedException();
    }

    Petrinet petri;
//prity print AST
    String info = "";
    PetrinetInterpreter.indent = PetrinetInterpreter.indent.concat("-");
    String className = currentNode.getClass().getSimpleName();
  /*  if (currentNode.getReferences() != null) {
      //System.out.println(">AST " + PetrinetInterpreter.indent + className +
        " refs " + currentNode.getReferences());
    } else {
      //System.out.println(">AST " + PetrinetInterpreter.indent + className);
    } */
    //System.out.println(currentNode.myString());
//
//UNprefixed use of Global Processes W in Z = W/{w}  NOT in Z = a->W;
    if (currentNode instanceof ProcessRootNode) { //currentPlace -> addpetriNet
      petri = interpretProcessRoot((ProcessRootNode) currentNode, petriId, context);
      info = ((ProcessRootNode) currentNode).getLabel();
    }
    // current node is in petri and is set to being terminal
    else if (currentNode instanceof TerminalNode) {
      petri = interpretTerminal((TerminalNode) currentNode);
      info = ((TerminalNode) currentNode).getTerminal();
      // called for b->a->STOP  a->ERROR  only
    } else if (currentNode instanceof SequenceNode) {
      petri = interpretSequence((SequenceNode) currentNode);
    }
    //this is for  |  processes held on ChoiceNode
    else if (currentNode instanceof ChoiceNode) {
      petri = interpretChoice((ChoiceNode) currentNode);
    }
    //this is for => and || and +
    else if (currentNode instanceof CompositeNode) {
      //System.out.println("478 "+asString(processMap));
      petri = interpretComposite((CompositeNode) currentNode, petriId);
      info = ((CompositeNode) currentNode).getOperation();
    }
    //Use ProcessMap to get petriNet for W  BOTH in Z = W/{w} and Z = a->W;
    else if (currentNode instanceof IdentifierNode) { //currentPlace -> addpetriNet
      petri = interpretIdentifier((IdentifierNode) currentNode);
      info = ((IdentifierNode) currentNode).getIdentifier();
    }
    // TokenRule to automata(if needed) then apply function and OwnersRule to Net
    //functions  nfa2dfa, prune, simp, abs,  .... are function on automata!
    else if (currentNode instanceof FunctionNode) {
      info = ((FunctionNode) currentNode).getFunction();
      //System.out.println("XXXXPetriInterp Fun "+((FunctionNode) currentNode).myString());
      petri = interpretFunction((FunctionNode) currentNode, petriId, alpha);
      //System.out.println("PetriInterpFun returns "+petri.myString());
    }
    // tokenRule and ownersRule
    else if (currentNode instanceof ConversionNode) { //currentPlace -> addpetriNet
      //System.out.println("PetriNetCoversion ConversionNode START "+petri.myString());
      petri = interpretConversion((ConversionNode) currentNode, petriId);
      info = ((ConversionNode) currentNode).from + " -> " + ((ConversionNode) currentNode).to;
      //Throwable t = new Throwable();
      //t.printStackTrace();
      //System.out.println("349 OUTSIDE of interpretConversion petri "+petri.myString());

    }
    // Build Stop node with FromRef  (Ref addded by RefReplacer)
    // At end of interpretation tree2net will glued FromRef to Ref
    else if (currentNode instanceof ReferenceNode) {
      String ref = ((ReferenceNode) currentNode).getReference();
      //System.out.println("petriInterp RefNode " + ref);
      if (selfRef)
        petri = Petrinet.stopNet(ref, "E");
      else {
        throw new CompilationException(this.getClass(),
          "No self reference allowed within function", currentNode.getLocation());
      }

    } else if (currentNode instanceof IndexExpNode) {
      petri = interpretIndexExp(((IndexExpNode) currentNode));

      // petri = interpretChoice((ProcessNode) currentNode);
    } else if (currentNode instanceof IfStatementExpNode) {
      petri = interpretIfStatementExp(((IfStatementExpNode) currentNode));

    } else {
      //System.out.println("\n\n Not Interpreted " + currentNode.getClass().getSimpleName() + " " + currentNode.myString() + "\n\n");
      throw new CompilationException(Guard.class, "Unable to interpret: " + currentNode.getClass().getSimpleName());
    }


    //prity print
    if (PetrinetInterpreter.indent.length() > 0)
      PetrinetInterpreter.indent = PetrinetInterpreter.indent.substring(1);
    if (currentNode.getReferences() != null) {
 /*     //System.out.println("AST<" + PetrinetInterpreter.indent + className + " ref " +
        currentNode.getReferences().toString() + " info " + info);
 */     //System.out.println(petri.myString());

      for (PetriNetPlace pl : petri.getAllRoots()) {
        if (currentNode.getReferences().size() > 0) {
          pl.addRefefances(currentNode.getReferences());
          //pl.setTerminal(""); //NOT HERE blows up self ref
          //
        }
      }
      //System.out.println(petri.myString());
    } //else

  /*System.out.println("AST<" + PetrinetInterpreter.indent + className + " info " + info
      + "\n" + petri.myString("edge"));
    if (PetrinetInterpreter.indent.length() > 1)
      PetrinetInterpreter.indent = PetrinetInterpreter.indent.substring(1); */
    return petri;
  }


  private Petrinet interpretIndexExp(IndexExpNode ien)
    throws CompilationException, InterruptedException {
    //System.out.println("With IndexExpNode  " + ien.myString());
    Petrinet pn = interpretASTNode(ien.getProcess());
    //System.out.println("IndexExpNode net " + pn.myString("edge"));
    return pn;
  }

  private Petrinet interpretIfStatementExp(IfStatementExpNode ifn)
    throws CompilationException, InterruptedException {
    //System.out.println("With interpretIfStatementExp  " + ifn.myString());


    Guard ifGuard = new Guard();
    ifGuard.setGuard(ifn.getCondition());
    Petrinet pn;
    Petrinet pnt = interpretASTNode(ifn.getTrueBranch());
    //System.out.println(pnt.myString("edge"));

    //pnt = addGuard(pnt, ifGuard);
    for (PetriNetEdge ed : pnt.getFirstEdges()) {
      ed.setGuard(ifGuard);
    }
    if (ifn.hasFalseBranch()) {
      Petrinet pnf = interpretASTNode(ifn.getFalseBranch());
      Guard notGuard = new Guard();
      notGuard.setGuard(context.mkNot(ifn.getCondition()));
      //pnf = addGuard(pnf, notGuard);
      for (PetriNetEdge ed : pnf.getFirstEdges()) {
        ed.setGuard(notGuard);
      }
      ChoiceFun cf = new ChoiceFun();
      pn = cf.compose("XX", pnt, pnf);
    } else {
      pn = pnt;
    }
    Iterator<String> iter = pnt.getOwners().iterator();
    if (iter.hasNext()) {
      String next = iter.next();
      List<String> vars = Arrays.stream(ifn.getCondition().getArgs()).
        map(x -> x.toString()).
        filter(x -> x.startsWith("$")).collect(Collectors.toList());
      for (int i = 0; i < vars.size(); i++) {
        if (!pn.getVariable2Owner().containsKey(vars.get(i)))
          pn.getVariable2Owner().put(vars.get(i), next);
      }
      //pn.getVariables().addAll(vars);
    } else {
      //System.out.println("           No Owner in If");
    }
    //System.out.println("IEN net CHECK VARIABLES in " + pn.myString("edge"));
    return pn;
  }


  /*
    for symbolic processes  this resids undet a ProcessNode
   */
  private Petrinet interpretLocalProcess(LocalProcessNode lpr, String petriId)
    throws CompilationException, InterruptedException {
    //System.out.println("what to do with local Process " + lpr.myString());
    Petrinet pn = interpretASTNode(lpr.getProcess(), petriId);
    //System.out.println("Local net " + pn.myString());
    return pn;
  }

  // current place is in petri Just set it as terminal
  private Petrinet interpretTerminal(TerminalNode term) throws CompilationException {
    //System.out.println("Terminal"+term.myString());
    if (term.getTerminal().equals(Constant.STOP))
      return Petrinet.stopNet();
    else if (term.getTerminal().equals(Constant.END))
      return Petrinet.endNet();
    return Petrinet.errorNet();
  }


  /**
   * @param choice
   * @throws CompilationException
   * @throws InterruptedException
   * @returns a new petrinet
   */
  private Petrinet interpretChoice(ChoiceNode choice)
    throws CompilationException, InterruptedException {
    //System.out.println("Choice first AST " + choice.getFirstProcess().myString());
    Petrinet op1 = interpretASTNode(choice.getFirstProcess());
    //System.out.println("Choice input petri net 1 " + op1.myString());

    Petrinet op2 = interpretASTNode(choice.getSecondProcess());
    //System.out.println("Choice input petri net 2 " + op2.myString());
    //temp now hold the first petri Net
    ChoiceFun sif = new ChoiceFun();
    Petrinet ret = sif.compose(op1.getId() + "+" + op2.getId() + ".", op1, op2);
    //System.out.println("CHOICE end "+ ret.myString());
    return ret;
  }


  private Petrinet interpretProcessRoot(ProcessRootNode processRoot,
                                        String petriId, Context context)
    throws CompilationException, InterruptedException {
    //System.out.println("\n               INTERPRETING Root 521 \n");
    //System.out.println("petri Root INput "+petri.myString());
    //  currentPlace = petri.getPlaces().get(currentPlace.getId());//only match on "id"
    //interpretProcess(processRoot.getProcess(), petriId + "." + ++subProcessCount);
    // Petrinet model = ((Petrinet) processStack.pop()).reId("");
    //replaced by single line below*/
    Petrinet model = interpretASTNode(processRoot.getProcess()); //RECURSIVE CALL the process is an ASTNode

    //System.out.println("model "+model.myString());
    model = processLabellingAndRelabelling(model, processRoot);

    if (processRoot.hasHiding()) {
      //should be copy seee ABOVE
      processHiding(model, processRoot.getHiding());
    }

    return model;
  }

  /**
   * Called for A = a->STOP    NOT => (see SequentialInfixFunction
   * currenPlace - label -> nextPlace(new currentPlace)
   *
   * @param seq
   * @throws CompilationException
   * @throws InterruptedException
   */
  private Petrinet interpretSequence(SequenceNode seq)
    throws CompilationException, InterruptedException {
    //System.out.println("Sequence start " + seq.myString());
    String lab = seq.getEventLabel().getAction(); // Now unique see Petrinet.netId
    Petrinet start = Petrinet.oneEventNet(lab);
    Petrinet petri = interpretASTNode(seq.getTo());  //initially the STOP net
    //System.out.println("SEQUENCE INPUT one   "+start.myString());
    //System.out.println("SEQUENCE INPUT petri "+petri.myString());
    //System.out.println("SEQUENCE INPUT ev "+ev.myString()+"\n");
    SequentialInfixFun sif = new SequentialInfixFun();
    Petrinet ret = sif.compose(lab, start, petri);
    //System.out.println("SEQUENCE end " + ret.myString() + "\n");

    return ret;
  }

  /*
     all Atomic Process in processMap added in Interpreter and are MultiProcesses
   */
  private Petrinet interpretIdentifier(IdentifierNode identifier)
    throws CompilationException, InterruptedException {

    //System.out.println("interp IdNode "+ identifier.myString());
    //System.out.println("processMap "+processMap.keySet());

    String id = identifier.getIdentifier();
    String pid = id;
    if (!id.contains(":")) pid = id + ":" + identifier.getDomain();

    //System.out.println("interpretIdentifier Start " + pid);
    Map<String, String> eval = new TreeMap<>();
    Petrinet pn;
    if (symb) {
      List<String> bits = identifier.getBits();
      if (bits.size() > 1) {
        //  a->B[x+1] the value "x+1" is an assignment on incoming edge OR Root
        id = bits.get(0);
        int i = 1;
        for (String var : vars) {
          String next = bits.get(i++);
          if (globalVarMap.containsKey(next))
            next = ExpressionPrinter.printExpression(globalVarMap.get(next));
          eval.put(var, next);
        }
        pn = Petrinet.stopNet(pid, "S");  // WHEN is this used


        pn.setRootEvaluation(eval);
      } else {

        //System.out.println("processMap "+processMap.keySet());
        if (processMap.containsKey(identifier.getIdentifier()))
          pn = (Petrinet) processMap.get(identifier.getIdentifier());
        else {
          //System.out.println(identifier.getIdentifier() + " not found in processMap");
          Throwable t = new Throwable();
          t.printStackTrace();
          pn = null;
        }
      }
      return pn;
    }  // not symbolic

    //System.out.println("pid "+pid+" processMap "+processMap.keySet());
    ProcessModel model = processMap.get(pid);
    Petrinet copy = null;
    if (model instanceof MultiProcessModel) {
      if (((MultiProcessModel) model).hasProcess(ProcessType.PETRINET)) {
        copy = ((Petrinet) ((MultiProcessModel) model).getProcess(ProcessType.PETRINET)).copy();
      }
    } else if (model instanceof Automaton) {
        System.out.println("PetrinetInterpreter Think this is deadcode");
      copy = OwnersRule.ownersRule((Automaton) model );
    } else {
        System.out.println("PetrinetInterpreter Think this is deadcode");
        //System.out.println("processMap "+processMap.keySet());
      throw new CompilationException(getClass(), "Unable to find petrinet for identifier: "
        + id, identifier.getLocation());
    }
    copy = copy.reId("");
    if (copy == null) {
      throw new CompilationException(getClass(), "Expecting a multiProcess in composite "
        , identifier.getLocation());
    }
    //System.out.println("IndetifierNode returns "+copy.myString());
    return copy;
  }

  private Automaton interpretAutIdentifier(IdentifierNode identifier)
    throws CompilationException, InterruptedException {

    //System.out.println("interp IdNode " + identifier.myString());
    //System.out.println("processMap " + processMap.keySet());

    String id = identifier.getIdentifier();
    String pid = id;
    if (!id.contains(":")) pid = id + ":" + identifier.getDomain();
    //System.out.println("interpretIdentifier Start " + pid);
    Map<String, String> eval = new TreeMap<>();
    if (symb) {
      System.out.println("FAILED  symb only Petri Nets");
      throw new CompilationException(getClass(), "symb only Petri Nets", identifier.getLocation());
      //return pn;
    }  // not symbolic

    //System.out.println("pid "+pid+" processMap "+processMap.keySet());
    ProcessModel model = processMap.get(pid);
    Automaton copy = null;
    if (model instanceof MultiProcessModel) {
      if (((MultiProcessModel) model).hasProcess(ProcessType.AUTOMATA)) {
        copy = ((Automaton) ((MultiProcessModel) model).getProcess(ProcessType.AUTOMATA)).copy();
      }
    } else if (model instanceof Automaton) {
      copy = (Automaton) model;
    } else if (model instanceof Petrinet) {
      copy = TokenRule.tokenRule((Petrinet) model);
    } else {
      Throwable t = new Throwable();
      t.printStackTrace();
      throw new CompilationException(getClass(), "Unable to find autoamat for identifier: "
        + id, identifier.getLocation());
    }
    copy = copy.reId("");
    if (copy == null) {
      throw new CompilationException(getClass(), "Expecting a multiProcess in composite " + id
        , identifier.getLocation());
    }
    return copy;
  }


  /**
   * @param composite Could be "||" or "=>" or
   * @param petriId
   * @return
   * @throws CompilationException
   * @throws InterruptedException
   */

  private Petrinet interpretComposite(CompositeNode composite, String petriId)
    throws CompilationException, InterruptedException {

    //System.out.println("interpret COMPOSITE "+composite.myString());
    //System.out.println(asString(processMap));
//NEEDed may be the renaming
    //interpretProcess(composite.getFirstProcess(), petriId + ".pc1");
    //interpretProcess(composite.getSecondProcess(), petriId + ".pc2");
    //ProcessModel model2 = processStack.pop();
    //ProcessModel model1 = processStack.pop();
    //replaced by two line below*/
    Petrinet model1 = interpretASTNode(composite.getFirstProcess()).reId(".pc1"); //RECURSIVE CALL the process is an ASTNode
    Petrinet model2 = interpretASTNode(composite.getSecondProcess()).reId(".pc2"); //RECURSIVE CALL the process is an ASTNode


    if (model1 == null || model2 == null) {
      throw new CompilationException(getClass(), "Expecting a petrinet in composite "
        + petriId, composite.getLocation());
    }
    //System.out.println("OPERATION " +composite.getOperation());
//comp is the correct Net
    Petrinet comp = instantiateClass(infixFunctions.get(composite.getOperation()))
      .compose(model1.getId() + composite.getOperation() + model2.getId(),
        (Petrinet) model1,
        (Petrinet) model2, composite.getFlags());
      //System.out.println("interpreter Operation outputs "+comp.myString());
    return comp;
    //addPetrinet( comp, petri);
  }

  /*
  The application of some dynamically loaded functions have access to Z3 contex
  most functions ar defined as automata to automata and not processed here
    Currently on expand  is a Net function (symbolicNet-> less symbolicNet)
  VOID VOID
   */
  private Petrinet interpretFunction(FunctionNode func, String petriId, Set<String> alpha)
    throws CompilationException, InterruptedException {
    Petrinet processed;
    //NO self references allowed as functions need to be applied to automata built from
    // Net not From TREE
    selfRef = false;
    if ( func.getFunction().equals("p2a2p")) {
        System.out.println("interpret P2A2P");
         Automaton a = interpretAutIdentifier((IdentifierNode) ((FunctionNode) func).getProcesses().get(0));
        System.out.println("\nOwners starting");
        processed = OwnersRule.ownersRule(a,processMap); // pass in the processMap for debugging

    }else if ( func.getFunction().equals("prune")) {
        System.out.println("interpret prune");
        processed = interpretIdentifier((IdentifierNode) ((FunctionNode) func).getProcesses().get(0));
        processed = instantiateClass(functions.get(func.getFunction()))
            .compose(processed.getId() + ".fn", new TreeSet<>(), context, processed);

    } else {
        Automaton a = getLocalAutomaton(context, alpha, func);
        //.out.println("interpretFunction "+a.myString());
    /* used to look at output of functions prior to OwnersRule-TokenRule

          Automaton debug = a.copy();
          debug.setId("debug-"+func.getFunction());
          processMap.put(debug.getId(), debug);

   */
        selfRef = true;
        System.out.println("\n   function "+func.myString()+ " -> "+a.getId());
        processed = OwnersRule.ownersRule(a);
    }

    return processed;
  }

  /*
  Works for OwnersRule
   */
  private Petrinet interpretConversion(ConversionNode conv, String petriId)
    throws CompilationException, InterruptedException {
    //  currentPlace = petri.getPlaces().get(currentPlace.getId());//only match on "id"

    //System.out.println("interpretConversion P->A start "+petri.myString());
    ProcessType to = ProcessType.valueOf(conv.to.toUpperCase());
    ProcessType from = ProcessType.valueOf(conv.from.toUpperCase());

    ProcessModel pm = new Interpreter().interpretForOperations(conv.from, conv.getProcess(),
      petriId + ".pc" + subProcessCount++, processMap, context, alpha);

    Petrinet temp = new Petrinet(petriId, false);

    Petrinet p = addPetrinet(        // Root needed,
      pm.getProcessType().convertTo(to, pm),
      temp, true).copy();
    //System.out.println("interpretConversion  ends "+p.myString());  //not
    return p;

  }


  /* adds roots may not be valid */
  private Petrinet addPetrinet(//PetriNetPlace currentPlace,
                               Petrinet petrinetToAdd, Petrinet master, boolean withRoot)
    throws CompilationException {
    //System.out.println("====masterPetrinet======== "+ withRoot+ " "+master.getId());
    //System.out.println("====petrinetToAdd ======== "+ petrinetToAdd.getId());
    //Throwable t = new Throwable(); t.printStackTrace();
    //System.out.println("currentPlace "+currentPlace.getId());
    List<String> references = new ArrayList<>();
    //TODO: References appear redundent when everything working remove all refs and check again
  /*if (currentPlace.getReferences() != null) {
   references.addAll(currentPlace.getReferences());
  }*/
    petrinetToAdd.validatePNet();
    if (withRoot) master.validatePNet();

    List<Set<String>> oldRoot = master.getRoots();
    master.addPetrinet(petrinetToAdd, withRoot, true);
    //System.out.println("====masterPetrinet======== "+ master.getId());
    if (withRoot) master.validatePNet();
    //System.out.println("master = "+master.myString());
    //add referances to Root Places ? adding to ArrayList I think redundent
    for (Set<String> oldrs : oldRoot) {
      oldrs.stream().map(x -> master.getPlaces().get(x)).map(PetriNetPlace::getReferences)
        .filter(Objects::nonNull)
        .forEach(references::addAll);
    }
    return master;
  }

  private Petrinet processLabellingAndRelabelling(Petrinet petri, ProcessRootNode processRoot)
    throws CompilationException, InterruptedException {


    if (processRoot.hasLabel()) {
      //System.out.println("processLabellingAndRelabelling "+processRoot.getLabel());
      petri = PetrinetLabeller.labelPetrinet(petri, processRoot.getLabel());
    }


    if (processRoot.hasRelabelSet()) {
//processes both renaming and refinement
      petri = processRelabelling(petri, processRoot.getRelabelSet());
    }
    //System.out.println("Relabel OUT "+petri.myString());
    return petri;
  }

  private Petrinet processRelabelling(Petrinet petri, RelabelNode relabelSet)
    throws CompilationException {
    //System.out.print("INTERPRET processRelabelling "+petri.myString());
  /*String s = "";
  for (String k : processMap.keySet()) {
   s = "processMap \n    " + k + "->" + processMap.get(k).getId() + "," + processMap.get(k).getProcessType();
  } //System.out.println(s); */
    for (RelabelElementNode r : relabelSet.getRelabels()) {
      //System.out.println("r = "+r.toString());
      if (r.getNewLabel() != null) { //event relabeling
        //System.out.println();
        if (!petri.getAlphabet().keySet().contains(r.getOldLabel())) {
          throw new CompilationException(getClass(), "Cannot find action " + r.getOldLabel()
            + "to relabel.", relabelSet.getLocation());
        }
        petri.relabelTransitions(r.getOldLabel(), r.getNewLabel());
      }
      if (r.getNewProcess() != null) { //event Refinement
        /*System.out.println("REFINEing from processRelabelling "+
            r.getNewProcess().getIdentifier()+"/"+r.getOldLabel() );
        //System.out.println(petri.myString()); */
        //First get the net to replace the events
        Petrinet newPet;
        IdentifierNode ridNode = r.getNewProcess();
        String rid = ridNode.getIdentifier() + ":" + ridNode.getDomain();
        if (processMap.get(rid).getProcessType().
          equals(ProcessType.MULTI_PROCESS)) {
          newPet = (processMap.get(rid).getProcessType().
            convertTo(ProcessType.PETRINET, processMap.get(rid))); //What a way to extact  a net
        } else {
          newPet = ((Petrinet) processMap.get(rid));
        }
        //Second  do the refinement
        RefineFun rf = new RefineFun();
        petri = rf.compose("Ref", r.getOldLabel(), petri, newPet); //
      }
    }
    return petri;
  }

  private void processHiding(Petrinet petri, HideNode hiding) throws CompilationException {
    //Includes syntax (\)
    //System.out.println("hidding BUG "+petri.myString()+ " -- "+hiding.myString());
    //Throwable t = new Throwable(); t.printStackTrace();
    if (hiding.getType().equalsIgnoreCase("includes")) {
      for (String hidden : hiding.getSet().getSet()) {
        //System.out.println("hiding "+hidden);
        if (petri.getAlphabet().keySet().contains(hidden)) {
          petri.relabelTransitions(hidden, Constant.HIDDEN);
        } else {
          System.out.println("LOST EVENT "+ hidden+"  "+ petri.myString());
          throw new CompilationException(getClass(), "Could not find " + hidden + " action to hide in "+petri.getId(),
            hiding.getLocation());
        }

      }
      //excludes syntax (@)
    } else {
      new ArrayList<>(petri.getAlphabet().keySet()).stream()
        .filter(k -> !hiding.getSet().getSet().contains(k))
        .forEach(a -> petri.relabelTransitions(a, Constant.HIDDEN));
    }

    if (hiding.getObs()) {
        Automaton a = TokenRule.tokenRule(petri);
       // AbstractionFunction abf = new AbstractionFunction();
    }
  }

  public void reset() {
    referenceSet.clear();
    // processStack.clear();
    sid = 0;
    indent = "";
    subProcessCount = 0;
  }

  /*  Called from the Interpreter
     Recursively unwind the functions then build from base up
     This avoids repeatedly applying the three steps:
          1 building a petriNet converting to an Automata
          2.  evaluateing the function
          3. convert the resulting automata to  a Petri Net
    if not a function then either
        A: if Identifier retreive then Automata
        B; else build the Petri Net and apply the TokenRule
   */
  public Automaton getAutomaton(Map<String, ProcessModel> processMap,
                                Interpreter interpreter,
                                Context context,
                                Set<String> alpha,
                                ASTNode ast) throws CompilationException, InterruptedException {
    //System.out.println("getAutomaton "+ ast.getName());
    Automaton NewAutomata;
    if (ast instanceof FunctionNode) {  //first time NOT allways true
      FunctionNode func = (FunctionNode) ast;
      //System.out.println("getA Fun "+func.myString());
        /* Recurive  application of Automata -> Automata functions */
      Automaton ain = getAutomaton(processMap, interpreter, context, alpha, ((FunctionNode) ast).getProcesses().get(0));
      // Automaton ain =  getAutomaton(processMap,interpreter,context,alpha,);
      Set<String> alphaFlags = new TreeSet<>();
      alphaFlags.addAll(alpha);  // add the listening events for revAP2BC
      alphaFlags.addAll(func.getFlags());
      NewAutomata = instantiateClass(functions.get(func.getFunction()))
        .compose(ain.getId() + ".fn", alphaFlags, context, ain);
       //System.out.println("getA Fun RETURNS \n"+a.myString());
    } else if (ast instanceof IdentifierNode) { //may be true first time
      String id = ((IdentifierNode) ast).getIdentifier();
      //System.out.println("getA Ident STARTS " + id);
      if (processMap.containsKey(id)) {
        NewAutomata = ((Automaton) ((MultiProcessModel) processMap.get(id)).getProcess(ProcessType.AUTOMATA)).copy();
      } else {
        NewAutomata = (Automaton) interpreter.interpretForOperations(Constant.AUTOMATA,
          ast, ast.myString(), processMap, context, alpha);
      }
      // a = interpretAutIdentifier(((IdentifierNode) ast), "ping");
      //System.out.println("getA Ident RETURNS \n"+a.myString());
    } else {
      //System.out.println("getA  else STARTS");
      Petrinet petri = (Petrinet) interpreter.interpretForOperations(Constant.PETRINET,
        ast, "temp", processMap, context, alpha);
      NewAutomata = TokenRule.tokenRule(petri);
      //System.out.println("getA Else RETURNS \n"+a.myString());
    }
    return NewAutomata;
  }

  /*  Called LOCALLY
     Recursively unwind the functions then build from base up
     This avoids repeatedly applying the three steps:
          1 building a petriNet converting to an Automata
          2.  evaluateing the function
          3. convert the resulting automata to  a Petri Net
    if not a function then either
        A: if Identifier retreive then Automata
        B; else build the Petri Net and apply the TokenRule
   */
  public Automaton getLocalAutomaton(Context context,
                                     Set<String> alpha,
                                     ASTNode ast) throws CompilationException, InterruptedException {
    //System.out.println("getLocalAutomaton "+ ast.getName());
    //System.out.println("getLocalAutomaton "+ processMap.keySet());
    Automaton a;
    if (ast instanceof FunctionNode) {  //first time allways true
      FunctionNode func = (FunctionNode) ast;
      //System.out.println("XXXXXgetLocalA Fun "+func.myString());
      Automaton ain = getLocalAutomaton(context, alpha, ((FunctionNode) ast).getProcesses().get(0));

      Set<String> alphaFlags = new TreeSet<>();
      if (alpha == null) alpha = new TreeSet<>();
      alphaFlags.addAll(alpha);  // add the listening events for revAP2BC
        if (func.getFlags() == null) func.setFlags(ImmutableSet.copyOf(new TreeSet<>()));
      alphaFlags.addAll(func.getFlags());
      a = instantiateClass(functions.get(func.getFunction()))
        .compose(ain.getId() + ".fn", alphaFlags, context, ain);
      //System.out.println("getA Fun RETURNS \n"+a.myString());
    } else if (ast instanceof IdentifierNode) {
      //System.out.println("XXXXXgetLocalA Ident STARTS "+((IdentifierNode)ast).getIdentifier());
      a = interpretAutIdentifier((IdentifierNode) ast);
    } else {
      //System.out.println("getLocalA  else STARTS");
      Petrinet petri = interpretProcess(ast, "", false, localProcesses);
      a = TokenRule.tokenRule(petri);
    }
    //System.out.println("getLocalA  RETURNS "+a.getId());
    return a;
  }


  public List<String> getBits(String id) {
    //System.out.println("rr1 "+id);
    String name = id.replaceAll("(\\[|\\])+", " ");
    //System.out.println("rr2 "+name);
    List<String> out = Arrays.asList(name.split(" "));
    //System.out.println("rr3 "+out.size()+"  "+out);

    return out;
  }

  public List<String> getBits(String name, String spliton) {
    //System.out.println("name " + name + " split " + spliton);

    List<String> out = Arrays.asList(name.split(spliton));
    //System.out.println("gives " + out.size() + "  " + out);

    return out;
  }
}

