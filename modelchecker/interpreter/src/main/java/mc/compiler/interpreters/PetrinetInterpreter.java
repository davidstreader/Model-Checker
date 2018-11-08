package mc.compiler.interpreters;

import static mc.util.Utils.instantiateClass;

import com.microsoft.z3.Context;
import com.google.common.collect.Multiset;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import mc.Constant;
import mc.compiler.Interpreter;
import mc.compiler.ast.*;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.plugins.IProcessInfixFunction;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.operations.ChoiceFun;
import mc.processmodels.automata.operations.SequentialInfixFun;
import mc.processmodels.conversion.OwnersRule;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
import mc.processmodels.petrinet.operations.RefineFun;
import mc.processmodels.petrinet.utils.PetrinetLabeller;
import mc.processmodels.MappingNdMarking;
import mc.compiler.Guard;
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
  Stack<Petrinet> processStack = new Stack<>();  // SIDE effect methods push and pop processes
  //LocalCompiler compiler;
  Set<String> variableList;
  int subProcessCount = 0;
  VariableSetNode variables;
  Set<String> alpha;
  boolean symb;
  public String myString(Map<String, Set<PetriNetPlace>> ref) {
    return ref.keySet().stream().
      map(x -> x + " " +
        ref.get(x).stream().map(y -> y.getId()).reduce((z, w) -> z + " " + w) + ": ").
      reduce((x, y) -> x + " " + y) + "";
  }


  /**
   * Called from interpreter called from COMPILER
   * Executed once at the start of building each process
   *
   * @param processNode
   * @param processMap
   * @param context
   * @return
   * @throws CompilationException
   * @throws InterruptedException
   */
  @Override
  public ProcessModel interpret(ProcessNode processNode,
                                Map<String, ProcessModel> processMap,
                                Context context,
                                Set<String> alpha,
                                boolean symb)
    throws CompilationException, InterruptedException {
    reset();
    this.symb = symb;
    //called by Interpreter and  Returns a ProcessModel that the Interpreter adds to the processMap
    this.alpha = alpha;
    System.out.println("Petri interpret " + processNode.getIdentifier() + " processmap  " + asString(processMap));

    this.context = context;
    variableList = new HashSet<>();
    this.processMap = processMap;
    String identifier = processNode.getIdentifier();
    this.variables = processNode.getSymbolicVariables();
    //System.out.println("***123 processNode " + processNode.getProcess().toString());

    //NOT USED CURRENTLY  conversions implicit  for testing use a2p2a( )
    //ABOVE NOT USED
//below pushes a petri net onto the process stack

    System.out.println("\n**Petriinterpret**Calling  " +
      processNode.getIdentifier() + " " + processNode.getType() + "\n");
    interpretProcess(processNode.getProcess(), identifier);
    Petrinet petrinet = processStack.pop().reId("");
    //System.out.println("\nPetriInterp Poped "+petrinet.myString());
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
    //System.out.println("interpret ENDS "+ processNode.getIdentifier() + " "+processMap.keySet().toString());

    //System.out.println("\nPetriInterpretor XX END  "+petrinet.getId());
    return petrinet;
  }

  /**
   * called from interpreter that has been called from OUTide of complier
   *
   * @param astNode
   * @param identifier
   * @param processMap  // the pre built processes
   * @param context
   * @return
   * @throws CompilationException
   * @throws InterruptedException
   */
  @Override
  public ProcessModel interpret(ASTNode astNode, String identifier,
                                Map<String, ProcessModel> processMap,
                                Context context,
                                Set<String> alpha)
    throws CompilationException, InterruptedException {
    reset();

    //System.out.println("interpret YY START "+identifier+" pMap "+ processMap.keySet());
    this.context = context;
    this.processMap = processMap;
    this.alpha = alpha;
    interpretProcess(astNode, identifier);  //Side effect PetriNet is now on stack
    Petrinet petrinet = ((Petrinet) processStack.pop()).copy();


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
   * Main component called from ALL places
   * RECURSE down the AST
   * Processes are  built and then PUSHED onto the stack
   * Binary operations pop two process off the stack and then push the result
   * Must be private as strictly SIDE EFFECT method
   *
   * @param astNode
   * @param identifier
   * @throws CompilationException
   * @throws InterruptedException
   */
  private void interpretProcess(ASTNode astNode, String identifier)
    throws CompilationException, InterruptedException {
    //prity print AST
    System.out.println("interpretProcess (PN) astNode IS " + astNode.myString());
    //System.out.println("289 "+asString(processMap));
    System.out.println("processMap keys " + processMap.keySet());
    PetrinetInterpreter.indent = PetrinetInterpreter.indent.concat("-");
    String className = astNode.getClass().getSimpleName();
    //System.out.println("iPro " + PetrinetInterpreter.indent + className);
//  get the petri net from the processMap and push onto the stack
    if (astNode instanceof IdentifierNode) {
      String reference = (((IdentifierNode) astNode).getIdentifier());
      //if
      if (!reference.contains(":")) {
        reference = ((IdentifierNode) astNode).getIdentifier() + ":" +
          ((IdentifierNode) astNode).getDomain();
      }
      System.out.println("*** interpretProcess IdentifierNode " + reference);
      //System.out.println(asString(processMap));

      if (processMap.get(reference).getProcessType().equals(ProcessType.MULTI_PROCESS)) {
        //System.out.println("interpretProcess GETS *********** MULTI_PROCESS -> PN");
        processStack.push(processMap.get(reference).getProcessType().
          convertTo(ProcessType.PETRINET, processMap.get(reference))); //What a way to extact  a net
      } else {
        processStack.push((Petrinet) processMap.get(reference));
      }
      //System.out.println("got Net "+processStack.peek().getId()+" from Map");
    } else if (astNode instanceof ProcessRootNode) {  //Optional Start of Net building
      System.out.println("*** interpretProcess ProcessRootNode");
      ProcessRootNode root = (ProcessRootNode) astNode;
      interpretProcess(root.getProcess(), identifier); //RECURSIVE CALL the process is an ASTNode
      // build new petri net and push on the stack
      Petrinet petrinet = processStack.pop().reId("");
      //System.out.println("\n*** ProcessRootNode poped petri "+petrinet.myString());

      petrinet = processLabellingAndRelabelling(petrinet, root); //888888
      //System.out.println("TO stack petri " + petrinet.getId());
      if (root.hasHiding()) {
        processHiding(petrinet, root.getHiding());
      }
      //System.out.println("  *** ProcessRootNode petri "+petrinet.getId());
      processStack.push(petrinet);

    } else {
      System.out.println("*** interpretProcess ELSE " + identifier);
      Petrinet petrinet = new Petrinet(identifier, true);
      PetriNetPlace currentPlace = petrinet.getPlace(petrinet.getRoots().get(0).iterator().next());
      //PetriNetPlace currentPlace = new ArrayList<>(petrinet.getRoot()).get(0);
      //  //System.out.println("WARNING  ADDING root node "+ currentPlace.getId());

      if (variables != null) {
        petrinet.setHiddenVariables(variables.getVariables());
        petrinet.setHiddenVariablesLocation(variables.getLocation());
      }
      if (variableList == null) {
        variableList = new HashSet<>();
      }
      petrinet.getVariables().addAll(variableList);

      petrinet.setVariablesLocation(astNode.getLocation());

      //Interpret Node
      //System.out.println("\n before 236*****Interpret " +petrinet.myString()+"\n");
      System.out.println(" interpretProcess " + identifier + " processmap  " + asString(processMap));
      System.out.println("349 " + astNode.myString());
      petrinet = interpretASTNode(astNode, petrinet); // PETRI TREE is built and returned
      //System.out.println("236 *****Interpret "+astNode.toString());
      //System.out.println("\n***PetriInterpret " +petrinet.myString()+"\n");
      if (astNode.getReferences() != null) {
        for (PetriNetPlace pl : petrinet.getAllRoots()) {
          pl.addRefefances(astNode.getReferences());
          pl.setTerminal("");
        }
      }
      System.out.println("\n*X* " + petrinet.myString());
      petrinet = tree2net(petrinet);
      //System.out.println("  ***ELSE ***tree2net petri "+petrinet.getId());
      processStack.push(petrinet);  // newly built petri net pushed onto stack
    }
    if (astNode.getReferences() == null) {
      //System.out.println("astNode.getReferences() = null");
      //Throwable t = new Throwable(); t.printStackTrace();
    } else //System.out.println("iPro<" + PetrinetInterpreter.indent + className + " " + " ref " +
      //astNode.getReferences().toString());
      if (PetrinetInterpreter.indent.length() > 0)
        PetrinetInterpreter.indent = PetrinetInterpreter.indent.substring(1);

    return;
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
    //Map<Set<PetriNetPlace>,Set<PetriNetPlace>> marking2Marking = new HashMap<>();
    //Map<PetriNetPlace,PetriNetPlace> marking2Marking = new HashMap<>();
    // filter ref or fromRef places
    //System.out.println("\nTree2Net START"+petri.getId());
    Set<PetriNetPlace> plsRef =
      petri.getPlaces().values().stream().
        filter(x -> x.getReferences().size() > 0).collect(Collectors.toSet());
    Set<PetriNetPlace> plsFrom =
      petri.getPlaces().values().stream().
        filter(x -> x.getFromReferences().size() > 0).collect(Collectors.toSet());
    //System.out.println("refR"+plsRef.stream().map(x->x.getId()).reduce("** ",(x,y)->x+" "+y));
    //System.out.println("refF"+plsFrom.stream().map(x->x.getId()).reduce("** ",(x,y)->x+" "+y));
    // glue pairs of places where one has a ref and the other the same fromRef
    for (PetriNetPlace pl1 : plsRef) {
      //System.out.println("Ref  "+pl1.myString());
      for (PetriNetPlace plFrom : plsFrom) {
        //System.out.println("From "+plFrom.myString());
        if (pl1.getReferences().containsAll(plFrom.getFromReferences())) {
          //System.out.println("Joining "+pl1.getId()+" "+plFrom.getId());
          plFrom.setTerminal("");
          Map<String, String> prodNames =  // need to get new pl1 Place
            petri.gluePlaces(Collections.singleton(pl1), Collections.singleton(plFrom));
          String spl1 = prodNames.values().iterator().next();
          pl1 = petri.getPlace(spl1);
        }
      }
    }
    petri.setRootFromStart();
    // remove referances else system retries
    petri.getPlaces().values().stream().
      filter(x -> x.getReferences().size() > 0).forEach(x -> x.setReferences(new LinkedHashSet<>()));
    petri.getPlaces().values().stream().
      filter(x -> x.getFromReferences().size() > 0).forEach(x -> x.setFromReferences(new LinkedHashSet<>()));
    //System.out.println("Tree2Net "+petri.myString()+"\n");
    //
    return petri;
  }


  //only used to build atomic petriNets
  private Petrinet interpretASTNode(ASTNode currentNode)
    throws CompilationException, InterruptedException {
    Petrinet fake = new Petrinet("Atom");

    //System.out.println("Atom builder "+fake.myString());
    return interpretASTNode(currentNode, fake);
  }


  /*  Recursive WORK HORSE
      Build a petrinet defined from currentNode  Recurse down the AST
      Functions Automaton -> Automaton  should  appear at head of AST and
      for computational reasons be processed seperatly

   */
  private Petrinet interpretASTNode(ASTNode currentNode, Petrinet petri)
    throws CompilationException, InterruptedException {
    System.out.println("ASTNode " + petri.myString());
    System.out.println("ASTNode " + currentNode.myString());

    if (Thread.currentThread().isInterrupted()) {
      throw new InterruptedException();
    }


//prity print AST
    String info = "";
    PetrinetInterpreter.indent = PetrinetInterpreter.indent.concat("-");
    String className = currentNode.getClass().getSimpleName();
    if (currentNode.getReferences() != null) {
      System.out.println("AST " + PetrinetInterpreter.indent + className +
        " refs " + currentNode.getReferences());
    } else {
      System.out.println("AST " + PetrinetInterpreter.indent + className);
    }
//
//UNprefixed use of Global Processes W in Z = W/{w}  NOT in Z = a->W;
    if (currentNode instanceof ProcessRootNode) { //currentPlace -> addpetriNet
      interpretProcessRoot((ProcessRootNode) currentNode, petri,context);
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
      petri = interpretComposite((CompositeNode) currentNode, petri);
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
      //System.out.println("PetriInterp alpha "+alpha);
      petri = interpretFunction((FunctionNode) currentNode, petri, alpha);
    }
    // tokenRule and ownersRule
    else if (currentNode instanceof ConversionNode) { //currentPlace -> addpetriNet
      //System.out.println("PetriNetCoversion ConversionNode START "+petri.myString());
      petri = interpretConversion((ConversionNode) currentNode, petri);
      info = ((ConversionNode) currentNode).from + " -> " + ((ConversionNode) currentNode).to;
      //Throwable t = new Throwable();
      //t.printStackTrace();
      //System.out.println("349 OUTSIDE of interpretConversion petri "+petri.myString());

    }
    // Build Stop node with FromRef  (Ref addded by RefReplacer)
    // At end of interpretation tree2net will glued FromRef to Ref
    else if (currentNode instanceof ReferenceNode) {
      String ref = ((ReferenceNode) currentNode).getReference();
      System.out.println("petriInterp RefNode "+ref);
      if (ref.length() > 0) {
        petri = Petrinet.stopNet(ref);
      }
    } else if (currentNode instanceof ProcessNode) {
      if (((ProcessNode) currentNode).hasHiding()) {
        interpretHidden(((ProcessNode) currentNode).getHiding());
        for (LocalProcessNode lpn : ((ProcessNode) currentNode).getLocalProcesses()) {
          interpretLocalProcess(lpn, petri);
        }
      } else {
        System.out.println("Problem Child " + currentNode.myString());
      }
      System.out.println("How do we glue the components together?");
    } else if (currentNode instanceof IndexExpNode) {
       petri = interpretIndexExp(((IndexExpNode) currentNode));

   // petri = interpretChoice((ProcessNode) currentNode);
    } else if (currentNode instanceof IfStatementExpNode) {
      petri = interpretIfStatementExp(((IfStatementExpNode) currentNode));

    } else {
      System.out.println("\n\n Not Interpreted " + currentNode.getClass().getSimpleName() + " " + currentNode.myString() + "\n\n");
    }


    //prity print
    if (PetrinetInterpreter.indent.length() > 0)
      PetrinetInterpreter.indent = PetrinetInterpreter.indent.substring(1);
    if (currentNode.getReferences() != null) {
      System.out.println("AST<" + PetrinetInterpreter.indent + className + " ref " +
        currentNode.getReferences().toString() + " info " + info);
      // System.out.println(petri.myString());

      for (PetriNetPlace pl : petri.getAllRoots()) {
        if (currentNode.getReferences().size() > 0) {
          pl.addRefefances(currentNode.getReferences());
          pl.setTerminal("");
        }
      }
      //System.out.println(petri.myString());
    } //else
    //System.out.println("AST<" + PetrinetInterpreter.indent + className + " info " + info);
    // + petri.myString());

    return petri;
  }

  private void interpretHidden(HidingNode hnd) {
    System.out.println("what to do with hidden variables " + hnd.myString());
  }

  private Petrinet interpretIndexExp(IndexExpNode ien)
    throws CompilationException, InterruptedException {
    System.out.println("With IEX  " + ien.myString());
    Petrinet pn = interpretASTNode(ien.getProcess());
    System.out.println("IEN net " + pn.myString());
    return pn;
  }
  private Petrinet interpretIfStatementExp(IfStatementExpNode ifn)
    throws CompilationException, InterruptedException {
    System.out.println("With interpretIfStatementExp  " + ifn.myString());
    Guard ifGuard = new Guard();
    ifGuard.setGuard(ifn.getCondition());
    Petrinet pn;
    Petrinet pnt = interpretASTNode(ifn.getTrueBranch());
    pnt = addGuard(pnt,ifGuard);
    if (ifn.hasFalseBranch()){
      Petrinet pnf = interpretASTNode(ifn.getFalseBranch());
      Guard notGuard = new Guard();
      notGuard.setGuard(context.mkNot(ifn.getCondition()));
      pnf = addGuard(pnf,notGuard);
      ChoiceFun cf = new ChoiceFun();
      pn = cf.compose("XX",pnt,pnf);
    } else {
      pn = pnt;
    }
    System.out.println("IEN net " + pn.myString());
    return pn;
  }

  private Petrinet addGuard(Petrinet petri, Guard g){
    System.out.println("addinGuard to do!");
    return petri;
  }
  private Petrinet interpretLocalProcess(LocalProcessNode lpr, Petrinet petri)
    throws CompilationException, InterruptedException {
    System.out.println("what to do with local Process " + lpr.myString());
    Petrinet pn = interpretASTNode(lpr.getProcess(), petri);
    System.out.println("Local net " + pn.myString());
    return pn;
  }

  // current place is in petri Just set it as terminal
  private Petrinet interpretTerminal(TerminalNode term) throws CompilationException {
    if (term.getTerminal().equals("STOP"))
      return Petrinet.stopNet();
    else
      return Petrinet.errorNet();
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

    String lab = seq.getFrom().getAction(); // Now unique see Petrinet.netId
    Petrinet ev = Petrinet.oneEventNet(lab);
    Petrinet petri = interpretASTNode(seq.getTo());  //initially the STOP net
    //System.out.println("SEQUENCE INPUT petri "+petri.myString()+"\n");
    //System.out.println("SEQUENCE INPUT ev "+ev.myString()+"\n");
    SequentialInfixFun sif = new SequentialInfixFun();
    Petrinet ret = sif.compose(lab, ev, petri);
//System.out.println("SEQUENCE end "+ ret.myString()+"\n");
    return ret;
  }


  /**
   * @param choice
   * @throws CompilationException
   * @throws InterruptedException
   * @returns a new petrinet
   */
  private Petrinet interpretChoice(ChoiceNode choice)
    throws CompilationException, InterruptedException {


    Petrinet op2 = interpretASTNode(choice.getSecondProcess());
    //temp now hold the first petri Net
    Petrinet op1 = interpretASTNode(choice.getFirstProcess());
    ChoiceFun sif = new ChoiceFun();
    Petrinet ret = sif.compose(op1.getId() + "+" + op2.getId() + ".", op1, op2);
    //System.out.println("CHOICE end "+ ret.myString());
    return ret;
  }


  private void interpretProcessRoot(ProcessRootNode processRoot,
                                    Petrinet petri,Context context)
    throws CompilationException, InterruptedException {
    //System.out.println("\n               INTERPRETING Root 521 \n");
    //System.out.println("petri Root INput "+petri.myString());
    //  currentPlace = petri.getPlaces().get(currentPlace.getId());//only match on "id"
    interpretProcess(processRoot.getProcess(), petri.getId() + "." + ++subProcessCount);
    Petrinet model = ((Petrinet) processStack.pop()).reId("");
    //System.out.println("model "+model.myString());
    model = processLabellingAndRelabelling(model, processRoot);

    if (processRoot.hasHiding()) {
      processHiding(model, processRoot.getHiding());
    }

    addPetrinet(model, petri, true);//NOT sure but currently works with root
  }

  private Petrinet interpretIdentifier(IdentifierNode identifier)
    throws CompilationException, InterruptedException {
    String id = identifier.getIdentifier();
    System.out.println("interpet Start " + id);
    if (symb) {
      List<String> bits = getBits(id);
      if (bits.size()> 0) id = bits.get(0);
      System.out.println("interpet Start " + id);
    }
    System.out.println("processMap keys "+ processMap.keySet());
    ProcessModel model = processMap.get(id + ":" + identifier.getDomain());
    Petrinet copy = null;
    if (model instanceof MultiProcessModel) {
      if (((MultiProcessModel) model).hasProcess(ProcessType.PETRINET)) {
        copy = ((Petrinet) ((MultiProcessModel) model).getProcess(ProcessType.PETRINET)).copy();
      }
    } else if (!(model instanceof Petrinet)) {
      throw new CompilationException(getClass(), "Unable to find petrinet for identifier: "
        + id, identifier.getLocation());
    } else {
      copy = ((Petrinet) model).copy();
    }
    copy = copy.reId("");
    if (copy == null) {
      throw new CompilationException(getClass(), "Expecting a multiProcess in composite "
        , identifier.getLocation());
    }

    //petri.addPetrinet(copy, false);      //Root not needed
    //petri = copy;
    //System.out.println("interpretId End "+copy.myString());
    //addPetrinet( copy, petri, false); //Root not needed
    return copy;
  }

  private Automaton interpretAutIdentifier(IdentifierNode identifier, String id)
    throws CompilationException {
    //System.out.println("AutIdentifier "+ identifier.getIdentifier()+ " id " +id);
    ProcessModel model = processMap.get(identifier.getIdentifier() + ":" + identifier.getDomain());
    //System.out.println("model "+model.toString());
    Automaton copy = null;
    if (model instanceof MultiProcessModel) {
      if (((MultiProcessModel) model).hasProcess(ProcessType.AUTOMATA)) {
        copy = ((Automaton) ((MultiProcessModel) model).getProcess(ProcessType.AUTOMATA)).copy();
      }
    } else if (!(model instanceof Automaton)) {
      throw new CompilationException(getClass(), "Unable to find automaton for identifier: "
        + identifier.getIdentifier(), identifier.getLocation());
    } else {
      copy = ((Automaton) model).copy();
    }
    if (copy == null) {
      throw new CompilationException(getClass(), "Expecting a multiProcess in composite "
        + id, identifier.getLocation());
    }
    //System.out.println("returning "+copy.myString());
    return copy;
  }

  /**
   * @param composite Could be "||" or "=>" or
   * @param petri
   * @return
   * @throws CompilationException
   * @throws InterruptedException
   */

  private Petrinet interpretComposite(CompositeNode composite, Petrinet petri)
    throws CompilationException, InterruptedException {

    //System.out.println("interpretCOMPOSITE "+composite.getOperation());
    //System.out.println(asString(processMap));

    interpretProcess(composite.getFirstProcess(), petri.getId() + ".pc1");
    interpretProcess(composite.getSecondProcess(), petri.getId() + ".pc2");
    Set<String> flags = composite.getFlags();
    ProcessModel model2 = processStack.pop();
    ProcessModel model1 = processStack.pop();

    if (model1 == null || model2 == null) {
      throw new CompilationException(getClass(), "Expecting a petrinet in composite "
        + petri.getId(), composite.getLocation());
    }
    //System.out.println("OPERATION " +composite.getOperation());
//comp is the correct Net
    Petrinet comp = instantiateClass(infixFunctions.get(composite.getOperation()))
      .compose(model1.getId() + composite.getOperation() + model2.getId(),
        (Petrinet) model1,
        (Petrinet) model2, composite.getFlags());
    return comp;
    //addPetrinet( comp, petri);
  }

  /*
  The application of some dynamically loaded functions have access to Z3 contex
  May be automata to automata but with PetriNet input --
       when stacked   simp(abs(-))  ap2bc(bc2ap(-)) will best have automata input

   */
  private Petrinet interpretFunction(FunctionNode func, Petrinet petri, Set<String> alpha)
    throws CompilationException, InterruptedException {
    //System.out.println("PetriInterp function "+func.getFunction()+ alpha+ " " + petri.getId());

    List<Petrinet> models = new ArrayList<>();
    for (ASTNode p : func.getProcesses()) {
      interpretProcess(p, petri.getId() + ".fn");
      models.add(processStack.pop());
    }

    if (models.isEmpty()) {
      throw new CompilationException(getClass(),
        "Expecting a petrinet, received an undefined process.", func.getLocation());
    }

    Petrinet[] petris = models.stream().map(Petrinet.class::cast).toArray(Petrinet[]::new);
    Set<String> alphaFlags = new TreeSet<>();
    alphaFlags.addAll(alpha);  // add the listening events for revAP2BC
    alphaFlags.addAll(func.getFlags());
    Petrinet processed = instantiateClass(functions.get(func.getFunction()))
      .compose(petri.getId() + ".fn", alphaFlags, context, petris);
    if (processed == null) {
      throw new CompilationException(getClass(),
        "Returned null. Check if this function should only be used in operations and equations!", func.getLocation());
    }
    //   //System.out.println("processed FUNCTION "+processed.myString());
    //addPetrinet( processed, petri);
    //System.out.println("interpret function END "+func.getFunction()+ alpha+ " " + processed.getId());
    return processed;
  }

  private Automaton interpretAbsFunction(FunctionNode func, String id)
    throws CompilationException, InterruptedException {
    Automaton models = null;
    //System.out.println("interpretAbsFunction " + func.getFunction()); //+ " process "+func.getProcesses().get(0).);

    ASTNode p = func.getProcesses().get(0);
    //interpretProcess(p, id + ".fn");  //Recursive Call
    //models.add(processStack.pop());
    models = interpretASTAutNode(p, id + ".fn");  //Recursive Call


    if (models == null) {
      Throwable t = new Throwable();
      t.printStackTrace();
      throw new CompilationException(getClass(),
        "Expecting an Automaton, received an undefined process.", func.getLocation());
    }

    Automaton[] auts = {models};


    Automaton processed = instantiateClass(functions.get(func.getFunction()))
      .compose(id + ".fn", func.getFlags(), context, auts);
    //System.out.println("processed FUNCTION "+processed.myString());
    //addPetrinet(currentPlace, processed, petri);
    //System.out.println("interpret FUNCTION "+petri.myString());
    return processed;
  }

  /*
  Works for OwnersRule
   */
  private Petrinet interpretConversion(ConversionNode conv, Petrinet petri)
    throws CompilationException, InterruptedException {
    //  currentPlace = petri.getPlaces().get(currentPlace.getId());//only match on "id"

    //System.out.println("interpretConversion P->A start "+petri.myString());
    ProcessType to = ProcessType.valueOf(conv.to.toUpperCase());
    ProcessType from = ProcessType.valueOf(conv.from.toUpperCase());

    ProcessModel pm = new Interpreter().interpret(conv.from, conv.getProcess(),
      petri.getId() + ".pc" + subProcessCount++, processMap, context, alpha);

    Petrinet temp = new Petrinet(petri.getId(), false);

    Petrinet p = addPetrinet(        // Root needed,
      pm.getProcessType().convertTo(to, pm),
      temp, true).copy();
    //System.out.println("interpretConversion  ends "+p.myString());  //not
    return p;

  }

  private Petrinet interpretAutConversion(ConversionNode conv, String id,
                                          PetriNetPlace currentPlace)
    throws CompilationException, InterruptedException {
    //  currentPlace = petri.getPlaces().get(currentPlace.getId());//only match on "id"

    //System.out.println("interpretConversion P->A start "+petri.myString());
    ProcessType to = ProcessType.valueOf(conv.to.toUpperCase());
    ProcessType from = ProcessType.valueOf(conv.from.toUpperCase());

    ProcessModel pm = new Interpreter().interpret(conv.from, conv.getProcess(),
      id + ".pc" + subProcessCount++, processMap, context, alpha);

    Petrinet temp = new Petrinet(id, false);

    Petrinet p = addPetrinet(//Never Used
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
    master.addPetrinet(petrinetToAdd, withRoot);
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

    //System.out.println("HAS L " + processRoot.hasLabel() + " has P "+processRoot.hasNewProcess());
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
          throw new CompilationException(getClass(), "Cannot find action" + r.getOldLabel()
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

  private void processHiding(Petrinet petri, HidingNode hiding) throws CompilationException {
    //Includes syntax (\)
    if (hiding.getType().equalsIgnoreCase("includes")) {
      for (String hidden : hiding.getSet().getSet()) {
        if (petri.getAlphabet().keySet().contains(hidden)) {
          petri.relabelTransitions(hidden, Constant.HIDDEN);
        } else {
          //System.out.println(petri.myString());
          throw new CompilationException(getClass(), "Could not find " + hidden + " action to hide",
            hiding.getLocation());
        }

      }
      //excludes syntax (@)
    } else {
      new ArrayList<>(petri.getAlphabet().keySet()).stream()
        .filter(k -> !hiding.getSet().getSet().contains(k))
        .forEach(a -> petri.relabelTransitions(a, Constant.HIDDEN));
    }
  }

  public void reset() {
    referenceSet.clear();
    processStack.clear();
    sid = 0;
    indent = "";
    subProcessCount = 0;
  }

  public Automaton getAutomaton(Map<String, ProcessModel> processMap,
                                Interpreter interpreter,
                                Context context,
                                Set<String> alpha,
                                ASTNode ast) throws CompilationException, InterruptedException {
    //System.out.println("getAutomaton "+ ast.getName());
    Automaton a;
    if (ast instanceof FunctionNode) {
      FunctionNode func = (FunctionNode) ast;
      //System.out.println("getA Fun "+func.myString());
      Automaton ain = getAutomaton(processMap, interpreter, context, alpha, ((FunctionNode) ast).getProcesses().get(0));

      Set<String> alphaFlags = new TreeSet<>();
      alphaFlags.addAll(alpha);  // add the listening events for revAP2BC
      alphaFlags.addAll(func.getFlags());
      a = instantiateClass(functions.get(func.getFunction()))
        .compose(ain.getId() + ".fn", alphaFlags, context, ain);
      //System.out.println("getA Fun RETURNS \n"+a.myString());
    } else if (ast instanceof IdentifierNode) {
      //System.out.println("getA Ident STARTS "+((IdentifierNode)ast).getIdentifier());
      a = (Automaton) interpreter.interpret(Constant.AUTOMATA,
        ast, ast.myString(), processMap, context, alpha);
      // a = interpretAutIdentifier(((IdentifierNode) ast), "ping");
      //System.out.println("getA Ident RETURNS \n"+a.myString());
    } else {
      //System.out.println("getA  else STARTS");
      Petrinet petri = (Petrinet) interpreter.interpret(Constant.PETRINET,
        ast, "temp", processMap, context, alpha);
      a = TokenRule.tokenRule(petri);
      //System.out.println("getA Else RETURNS \n"+a.myString());
    }
    return a;
  }


  /*
  Builds an Automata
   */
  public Automaton interpretASTAutNode(ASTNode currentNode, String id)
    throws CompilationException, InterruptedException {
    //System.out.println("ASTAutNode " + currentNode.getClass().getSimpleName());

    if (Thread.currentThread().isInterrupted()) {
      throw new InterruptedException();
    }

//prity print AST
    PetrinetInterpreter.indent = PetrinetInterpreter.indent.concat("-");
    String className = currentNode.getClass().getSimpleName();
    if (currentNode.getReferences() != null) {
      //System.out.println("ASTAut " + PetrinetInterpreter.indent + className +
      //    " refs " + currentNode.getReferences());
    } else {
      //System.out.println("ASTAut " + PetrinetInterpreter.indent + className);
    }
/* UNprefixed use of Global Processes W in Z = W/{w}  NOT in Z = a->W;
  if (currentNode instanceof ProcessRootNode) {
   interpretProcessRoot((ProcessRootNode) currentNode, auto, currentPlace);
  } */
    Automaton auto = null;
    //Use ProcessMap to get petriNet for W  BOTH in Z = W/{w} and Z = a->W;
    if (currentNode instanceof IdentifierNode) {
      auto = interpretAutIdentifier((IdentifierNode) currentNode, id);
      //System.out.println("identifier "+((IdentifierNode) currentNode).getIdentifier()+" "+auto.getId());
    }
    // TokenRule to automata(if needed) then apply function and OwnersRule to Net
    //functions  nfa2dfa, prune, simp, abs,  .... are function on automata!
    else if (currentNode instanceof FunctionNode) {
      auto = interpretAbsFunction((FunctionNode) currentNode, id);
      //System.out.println("ASTAut "+auto.myString());
    }

    /*prity print
    if (PetrinetInterpreter.indent.length() > 0)
      PetrinetInterpreter.indent = PetrinetInterpreter.indent.substring(1);
    if (currentNode.getReferences() != null) {
      System.out.println("ASTAut<" + PetrinetInterpreter.indent + className + " ref " +
        currentNode.getReferences().toString());
      //System.out.println(petri.myString());
    } else
      System.out.println("ASTAut<" + PetrinetInterpreter.indent + className);
    // + petri.myString()); */

    return auto;
  }

  public List<String> getBits(String n) {
    System.out.println("1 "+n);
     String name  = n.replaceAll("\\[|\\]*", "  ");
    System.out.println("2 "+name);
    Pattern whitespace = Pattern.compile("\\s*");
    List<String> out = Arrays.asList(name.split("\\s*"));
    System.out.println("3 "+out.size()+"  "+out);

    return out;
  }

}

/*
    if (processNode.getProcess() instanceof ConversionNode) {
      if (((ConversionNode) processNode.getProcess()).to.equals("petrinet")) {
        //  ownersRule(P);
        //System.out.println("\n**Petriinterpret**Calling Owners " + "\n");
        ProcessModel model = new MultiProcessModel(processNode.getIdentifier());
        model.setLocation(processNode.getLocation());
//1. build petrinet P push to stack
        interpretProcess((((ConversionNode) processNode.getProcess()).getProcess()), identifier);
        //System.out.println("Ping");
        Petrinet modelPetri = processStack.pop().copy();
        ((MultiProcessModel) model).addProcess(modelPetri);
//2. build automata via TokenRule (may be redundent as may have been pre built)
        ProcessModel modelAut;
        HashMap<AutomatonNode, Multiset<PetriNetPlace>> nodeToMarking =
          new HashMap<>();
        HashMap<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = new HashMap<>();
        //System.out.println("interpret Token1 ");
        modelAut = TokenRule.tokenRule(
          (Petrinet) ((MultiProcessModel) model)
            .getProcess(ProcessType.PETRINET), markingToNode, nodeToMarking);
        //System.out.println("First tokenRule ");

//3. NOW at start apply Owners Rule to build new PetriNet

        modelPetri = OwnersRule.ownersRule((Automaton) modelAut);
        ((MultiProcessModel) model).addProcess(modelPetri);
//4. finally build new Automata vis Token Rule
        //System.out.println("interpretPetri Token 1" + ((Petrinet) modelPetri).getId());
        modelAut = TokenRule.tokenRule(
          (Petrinet) ((MultiProcessModel) model)
            .getProcess(ProcessType.PETRINET), markingToNode, nodeToMarking);
        //System.out.println("Built automata with tokenRule "+ modelAut.getId());
        ((MultiProcessModel) model).addProcess(modelAut);
        ((MultiProcessModel) model)
          .addProcessesMapping(new MappingNdMarking(nodeToMarking, markingToNode));
        //System.out.println("END of Owners");

        return model;
      } else if (((ConversionNode) processNode.getProcess()).to.equals("automata")) {
        System.out.println("\n**Petriinterpret**Call Token " +processNode.toString()+"\n");
        ProcessModel model = new MultiProcessModel(processNode.getIdentifier());
        model.setLocation(processNode.getLocation());

        interpretProcess((((ConversionNode) processNode.getProcess()).getProcess()), identifier);

        Petrinet modelPetri = processStack.pop().copy();
        //System.out.println("\nPing"+modelPetri.myString());
        ((MultiProcessModel) model).addProcess(modelPetri);
        ProcessModel modelAut;
        HashMap<AutomatonNode, Multiset<PetriNetPlace>> nodeToMarking = new HashMap<>();
        HashMap<Multiset<PetriNetPlace>, AutomatonNode> markingToNode = new HashMap<>();

       // Token rule works here but can not be called from petrinetInterpreter

//System.out.println("2 petriIntepret");
        modelAut = TokenRule.tokenRule(
          (Petrinet) ((MultiProcessModel) model)
          .getProcess(ProcessType.PETRINET), markingToNode, nodeToMarking);
          //System.out.println("Built automata with tokenRule "+ modelAut.getId());
          ((MultiProcessModel) model).addProcess(modelAut);
          ((MultiProcessModel) model)
          .addProcessesMapping(new MappingNdMarking(nodeToMarking, markingToNode));
          //System.out.println("END of Token");
          return model;
          } else {
          //System.out.println("\nOops PetrinetInterpreter! "+ processNode.getIdentifier() + " " + processNode.getType()+"\n");
          }
          //System.out.println("CONVERSION end in petrinet interpreter");
          }

 */
