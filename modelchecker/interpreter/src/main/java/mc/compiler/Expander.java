package mc.compiler;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import mc.compiler.ast.*;
import mc.compiler.iterator.IndexIterator;
import mc.compiler.iterator.RangeIterator;
import mc.exceptions.CompilationException;
import mc.util.Location;
import mc.util.expr.Expression;
import mc.util.expr.ExpressionEvaluator;
import mc.util.expr.ExpressionPrinter;
import mc.util.expr.VariableCollector;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Expander {

  private final Pattern VAR_PATTERN = Pattern.compile("\\$[a-z][a-zA-Z0-9_]*");
/*
  Expanding replaces varaibles over  ranges with atomic processes
  Also performs validation.
  Varibles occur:
    1. in a LocalProcessNode->RangesNode->IEN scope local process = State indexing
    2. in an IEN stored as a process, IEN->RangesNode  scope the IEN = event indexing
 */

  /*  $v1 ->  $i = 1 , $v2 -> $i=2 , $v3 -> $j < 0 ,
   Only seen booleans. And are used temporarliy in IndentifyerNode.identifyer  C[$v2][$v3]
   until expanded to C[2][$k]  where k is a hidden variable
    */
  private Map<String, Expr> globalVariableMap;  // symbolic variables

  private ExpressionEvaluator evaluator = new ExpressionEvaluator();
  private Map<String, List<String>> identMap = new HashMap<>();  // LocalId -> $i,$s

  //set at top level may remove shortly
  // These are for use in symbolic processes only ?
  private Set<String> symbolicVariables = new HashSet<>();  // C${i,k} symbolic variables i and k
  private String forAllId = null;
  private List<LocalProcessNode> forAllLocalProcesses = new ArrayList<>();
  //private Map<String,ProcessNode> symbolicStore = new TreeMap<>();
/*
   ONLY starting point
 */
  public AbstractSyntaxTree expand(AbstractSyntaxTree ast, BlockingQueue<Object> messageQueue, Context context)
    throws CompilationException, InterruptedException {
    globalVariableMap = ast.getVariableMap();
//expand each process
    List<ProcessNode> processes = ast.getProcesses();
    //System.out.println("processes cnt "+processes.size());
    for (ProcessNode process : processes) {
      expand(process, messageQueue, context);
    }

//expand each operation  ?? may be redundent as inedx definitions should not be used here
    /*
       THINK this is redundent but symbolic processing makes the whole of Expansion disapear
     */
    List<OperationNode> operations = ast.getOperations();
    for (OperationNode operation : operations) {
      if (operation instanceof ImpliesNode) {
        System.out.println("expander ==>");
      } else {
        /*
           maps indexes to Integer and ??
         */
        Map<String, Object> variableMap = new HashMap<>();
        ASTNode process1 = expand(operation.getFirstProcess(), variableMap, context);
        ASTNode process2 = expand(operation.getSecondProcess(), variableMap, context);

        operation.setFirstProcess(process1);
        operation.setSecondProcess(process2);
      }
    }
    //System.out.println("\n Expander ends with ");
    //System.out.println("   " + ast.myString());
    //System.out.println("   " + ast.toString());
    return ast;
  }

  /**
   * Expand ProcessNode (AST for one process)
   *            EVERY THING goes through here
   * index-> Int variableMap reInitialised
   * processNode is being overwriten hence not available for subsequent symbolic expansion!
   *
   * @param process
   * @param messageQueue
   * @param context
   * @return
   * @throws CompilationException
   * @throws InterruptedException
   */
  private ProcessNode expand(ProcessNode process, BlockingQueue<Object> messageQueue, Context context)
    throws CompilationException, InterruptedException {
    // symbolicStore.put(process.getIdentifier(), (ProcessNode) process.copy()); // deep clone!
    //System.out.println("symbolic "+ process.getIdentifier()+"->"+  symbolicStore.get(process.getIdentifier()).myString());
    boolean forAll = false;
    // messageQueue.add(new LogAST("Expanding:", process));
    //System.out.println("\nexpanding process " + process.myString());
    if (process.getProcess() instanceof ForAllStatementNode) {
      //System.out.println("ping " + process.getIdentifier());
      forAll = true;
      forAllId = process.getIdentifier();
    } else {
      forAllId = "";
    }
    identMap.clear();
    if (process.hasSymbolicVariableSet()) {
      symbolicVariables = process.getSymbolicVariables().getVariables();
    } else {
      symbolicVariables = new HashSet<>();
    }
    //No Scope of variables considered!
    Map<String, Object> variableMap = new HashMap<>();   //Store from variables->values

    for (LocalProcessNode node : process.getLocalProcesses()) {
      identMap.put(node.getIdentifier(), new ArrayList<>());
      if (node.getRanges() != null) {
        for (IndexExpNode in : node.getRanges().getRanges()) {

          identMap.get(node.getIdentifier()).add(in.getVariable());
        }
      }
    }
    //System.out.println("\nSTARTING "+process.getProcess().myString());
    //System.out.println("idMap  "+ identMap.keySet().stream().map(x->x+"->"+identMap.get(x)).collect(Collectors.joining()));
    ASTNode root;
    //  if (symbolicVariables.size()==0)
    root = expand(process.getProcess(), variableMap, context);
 /*   else {
      //System.out.println(process.getIdentifier()+" -holds- "+ ((IdentifierNode) process.getProcess()).getIdentifier());
      ProcessNode symb = symbolicStore.get(((IdentifierNode) process.getProcess()).getIdentifier());
//symb.getProcess().getName()
      //System.out.println("symbolic "+((ProcessNode) symb).myString());
      root = symb;  //expand(symb, variableMap, context);
    } */
    //System.out.println("root "+root.myString());
    process.setProcess(root);
    List<LocalProcessNode> localProcesses = expandLocalProcesses(process.getLocalProcesses(), variableMap, context);
    process.setLocalProcesses(localProcesses);
    if (forAll) {
      //System.out.println("pong " + process.getIdentifier());
      List<LocalProcessNode> lpns = new ArrayList<>();
      for(LocalProcessNode x: forAllLocalProcesses ){ //Object reference
        lpns.add(x);
      }
      process.setLocalProcesses(lpns);
    }
    forAllLocalProcesses.clear();


    //System.out.println("Ending Expand " + process.myString());
    return process;
  }

  /*
  expand local process  - needs ranges on the Local Process
      indexes to be expanded only on LocalProcesses
      a one -> many expansion of local processes
   */
  private List<LocalProcessNode> expandLocalProcesses(List<LocalProcessNode> localProcesses,
                                                      Map<String, Object> variableMap,
                                                      Context context)
    throws CompilationException, InterruptedException {
    //System.out.println("expand Locals ");
    List<LocalProcessNode> newLocalProcesses = new ArrayList<>();
    for (LocalProcessNode localProcess : localProcesses) {
      //System.out.println(" expandLocalProcesses start "+ localProcess.myString());
      if (localProcess.getRanges() == null) {
        ASTNode root = expand(localProcess.getProcess(), variableMap, context);
        localProcess.setProcess(root);
        newLocalProcesses.add(localProcess);
      } else {
        newLocalProcesses.addAll(expandLocalProcesse_5(localProcess, variableMap, localProcess.getRanges().getRanges(), 0, context));
      }
    }
    //System.out.println(" expandLocalProcesses ends ");
     // newLocalProcesses.stream().forEach(x->System.out.println(" expLP  "+x.myString()+"\n"));

    return newLocalProcesses;
  }

  /* const M = 2.
    This expands C[i:0..M][j:0..M]  using ranges
    List<IndexNode> = [ ($i, start=0, end=2),($j, start=0, end=2)]
    Atomic processes have no hidden variables and
    LocalProcess identifyer all leteral C[0][3]
    Symbolic processes
   */
  private List<LocalProcessNode> expandLocalProcesse_5(LocalProcessNode localProcess,
                                                     Map<String, Object> variableMap,
                                                     List<IndexExpNode> ranges, int index,
                                                     Context context)
    throws CompilationException, InterruptedException {
    //System.out.println("expand Local "+localProcess.myString()+ " index " + index+"\n ranges "+ranges.stream().map(x->x.myString()).collect(Collectors.joining(",   ")));

    List<LocalProcessNode> newLocalProcesses = new ArrayList<>();
    if (index < ranges.size()) {
      IndexExpNode range = ranges.get(index);
      IndexIterator iterator = IndexIterator.construct(expand(range, context));
      String variable = range.getVariable();
      if (!symbolicVariables.contains(variable.substring(1))) { // &&
    //      !localProcess.getRanges().getRanges().stream().map(x->x.getVariable()).collect(Collectors.toSet()).contains(variable.substring(1))) {
        //System.out.println("Symbolic_5 "+ variable);
        //Works for indexing outside of forall
        localProcess.setIdentifier(localProcess.getIdentifier() + "[" + variable + "]");
        //this will add the identifier for expansion and remove the Range?
       // localProcess.setIdentifierOnce(variable);
        while (iterator.hasNext()) {
          variableMap.put(variable, iterator.next()); // variable->Value
          newLocalProcesses.addAll(expandLocalProcesse_5((LocalProcessNode) localProcess.copy(), variableMap, ranges, index + 1, context));
        }
      } else { // $i is a hidden variable in  C${i}.
        //System.out.println("Not Symbolic_5");
        localProcess.setIdentifierOnce(variable);
        //localProcess.setIdentifier(localProcess.getIdentifier() + "[" + variable + "]");
        newLocalProcesses.addAll(expandLocalProcesse_5((LocalProcessNode) localProcess.copy(), variableMap, ranges, index + 1, context));
      }
    } else {
      LocalProcessNode clone = (LocalProcessNode) localProcess.copy();
      ASTNode root = expand(clone.getProcess(), variableMap, context);
  //MAKE the CHANGE
      clone.setIdentifier(processVariables(clone.getIdentifier(), variableMap, clone.getLocation(), context));
      clone.setProcess(root);
      newLocalProcesses.add(clone);
    }
    //System.out.println("expandLocalProcesse_5 ends with");
    //System.out.println("newLocal "+newLocalProcesses.stream().map(x->x.myString()+" ").collect(Collectors.joining()));
    return newLocalProcesses;
  }


  /*
    Recursive WORK HORSE
   */
  private ASTNode expand(ASTNode astNode, Map<String, Object> variableMap, Context context)
    throws CompilationException, InterruptedException {
    //System.out.println("**** epanding ASTNode "+astNode.myString()+" "+astNode.getClass().getSimpleName());
    if (Thread.currentThread().isInterrupted()) {
      throw new InterruptedException();
    }
    if (astNode instanceof ProcessRootNode) {
      astNode = expand((ProcessRootNode) astNode, variableMap, context);
    } else if (astNode instanceof ActionLabelNode) {
      astNode = expand((ActionLabelNode) astNode, variableMap, context);
    } else if (astNode instanceof IndexExpNode) {
      astNode = expand((IndexExpNode) astNode, variableMap, context);
    } else if (astNode instanceof SequenceNode) {
      astNode = expand((SequenceNode) astNode, variableMap, context);
      astNode = expand((SequenceNode) astNode, variableMap, context);
    } else if (astNode instanceof ChoiceNode) {
      astNode = expand((ChoiceNode) astNode, variableMap, context);
    } else if (astNode instanceof CompositeNode) {
      astNode = expand((CompositeNode) astNode, variableMap, context);
    } else if (astNode instanceof IfStatementExpNode) {
      astNode = expand((IfStatementExpNode) astNode, variableMap, context);
    } else if (astNode instanceof FunctionNode) {
      astNode = expand((FunctionNode) astNode, variableMap, context);
    } else if (astNode instanceof IdentifierNode) {
      astNode = expand((IdentifierNode) astNode, variableMap, context);
    } else if (astNode instanceof ForAllStatementNode) {
      astNode = expand((ForAllStatementNode) astNode, variableMap, context);
    }
    //Create a temporary variable map that does not contain hidden variables and store it.
    HashMap<String, Object> tmpVarMap = new HashMap<>(variableMap);
    tmpVarMap.keySet().removeIf(s -> symbolicVariables.contains(s.substring(1)));
    astNode.setModelVariables(tmpVarMap);
    //System.out.println("   XXX "+ astNode.getName()+" ending expand next "+astNode.myString());
    //System.out.println("var map "+variableMap.keySet());
    //System.out.println("global Var map ");
    return astNode;
  }

  private ASTNode expand(ProcessRootNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
    if (astNode.hasLabel()) {
      astNode.setLabel(processVariables(astNode.getLabel(), variableMap, astNode.getLocation(), context));
    }
    ASTNode process = expand(astNode.getProcess(), variableMap, context);
    astNode.setProcess(process);

    if (astNode.hasRelabelSet()) {
      astNode.setRelabelSet(expand(astNode.getRelabelSet(), context));
    }
/*
  The HidingNode.getSet may contain ranges that are now expanded
 */
    if (astNode.hasHiding()) {
      HidingNode hiding = astNode.getHiding();
      hiding.setSet(expand(hiding.getSet(), context));
      astNode.setHiding(hiding);
    }

    return astNode;
  }

  private ActionLabelNode expand(ActionLabelNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
    String action = processVariables(astNode.getAction(), variableMap, astNode.getLocation(), context);
    astNode.setAction(action);
    return astNode;
  }

  /*
    Building many copies of process in IEN
    change to variableMap  alters error detection! (local scope)
   */
  private ASTNode expand(IndexExpNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
    IndexIterator iterator = IndexIterator.construct(expand(astNode, context));
    Stack<ASTNode> iterations = new Stack<>();
    while (iterator.hasNext()) {
      Object element = iterator.next();
      variableMap.put(astNode.getVariable(), element);
      iterations.push(expand(astNode.getProcess().copy(), variableMap, context));
    }

    ASTNode node = iterations.pop();
    while (!iterations.isEmpty()) {
      ASTNode nextNode = iterations.pop();
      node = new ChoiceNode(nextNode, node, astNode.getLocation());
    }

    return node;
  }

  private ASTNode expand(IndexExpNode astNode, Context context) throws CompilationException, InterruptedException {
    // expand out nested indices
    ASTNode range = astNode;
    while (range instanceof IndexExpNode) {
      range = ((IndexExpNode) range).getRange();
    }

    //if the range is a set then it may need expanding
    if (range instanceof SetNode) {
      range = expand((SetNode) range, context);
    }

    return range;
  }

  private SequenceNode expand(SequenceNode astNode,
                              Map<String, Object> variableMap,
                              Context context)
    throws CompilationException, InterruptedException {
    //add a guard to every sequenceNode. This will only contain next data.
    //System.out.println("Sequence getTo "+astNode.getTo().myString());


    Guard guard = new Guard();
    if (astNode.getTo() instanceof IdentifierNode) {
      if (symbolicVariables.isEmpty()) {
        //System.out.println("astNode "+astNode.myString()+ " globVar "+ globalVariableMap.keySet().toString());
      } else { // process symbolic ID
        //Parse the next values from this IdentifierNode Set up guard on astNode

        guard.parseNext(((IdentifierNode) astNode.getTo()).getIdentifier(), globalVariableMap, identMap, astNode.getTo().getLocation());
        //There were next values, so assign to the metadata
        if (guard.hasData()) {
          astNode.setGuard(guard);
        }
      }
    }
    //nextMap and next is set on astNode Guard!
    ActionLabelNode from = expand(astNode.getFrom(), variableMap, context);

    ASTNode to = expand(astNode.getTo(), variableMap, context);
    astNode.setFrom(from);
    astNode.setTo(to);
    return astNode;
  }

  private ASTNode expand(ChoiceNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
    ASTNode process1 = expand(astNode.getFirstProcess(), variableMap, context);
    ASTNode process2 = expand(astNode.getSecondProcess(), variableMap, context);

    // check if either one of the branches is empty  Terminal is not empty!
  /*  if (process1 instanceof EmptyNode || process1 instanceof TerminalNode) {
      return process2;
    } else if (process2 instanceof EmptyNode || process2 instanceof TerminalNode) {
      return process1;
    }*/
    if (process1 instanceof EmptyTestOnlyNode) {
      return process2;
    } else if (process2 instanceof EmptyTestOnlyNode) {
      return process1;
    }

    astNode.setFirstProcess(process1);
    astNode.setSecondProcess(process2);
    return astNode;
  }

  private ASTNode expand(CompositeNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
    ASTNode process1 = expand(astNode.getFirstProcess(), variableMap, context);
    ASTNode process2 = expand(astNode.getSecondProcess(), variableMap, context);

    // check if either one of the branches is empty
    if (process1 instanceof EmptyTestOnlyNode) {
      return process2;
    } else if (process2 instanceof EmptyTestOnlyNode) {
      return process1;
    }

    astNode.setFirstProcess(process1);
    astNode.setSecondProcess(process2);
    return astNode;
  }

  private ASTNode expand(IfStatementExpNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
    VariableCollector collector = new VariableCollector();
    Map<String, Integer> vars = collector.getVariables(astNode.getCondition(), variableMap);
    Guard trueGuard = new Guard(astNode.getCondition(), vars, symbolicVariables);
    Guard falseGuard = new Guard(astNode.getCondition(), vars, symbolicVariables);
    ASTNode trueBranch = expand(astNode.getTrueBranch(), variableMap, context);
    if (trueBranch.getGuard() != null) {
      trueGuard.mergeWith((Guard) trueBranch.getGuard());
    }

    trueBranch.setGuard(trueGuard);
    ASTNode falseBranch = null;

    if (astNode.hasFalseBranch()) {
      falseBranch = expand(astNode.getFalseBranch(), variableMap, context);
      if (falseBranch.getGuard() != null) {
        falseGuard.mergeWith((Guard) falseBranch.getGuard());
      }
      falseBranch.setGuard(falseGuard);
    }
    //Check if there are any hidden variables inside both the variableMap and the expression
    if (vars.keySet().stream().map(s -> s.substring(1)).anyMatch(s -> symbolicVariables.contains(s))) {
      if (astNode.hasFalseBranch()) {
        return new ChoiceNode(trueBranch, falseBranch, astNode.getLocation());
      } else {
        return trueBranch;
      }
    }
    //Collect all hidden variables, including variables that aren't in variableMap.
    vars = collector.getVariables(astNode.getCondition(), symbolicVariables.stream().collect(Collectors.toMap(s -> "$" + s, s -> 0)));
    boolean hiddenVariableFound = vars.keySet().stream().map(s -> s.substring(1)).anyMatch(s -> symbolicVariables.contains(s));
    if (evaluateCondition(astNode.getCondition(), variableMap, context)) {
      //If a hidden variable is found in the current expression
      if (astNode.hasFalseBranch() && hiddenVariableFound) {
        ASTNode falseBranch2 = astNode.getFalseBranch();
        //See if we can find an else with no if tied to it
        while (falseBranch2 instanceof IfStatementExpNode) {
          vars = collector.getVariables(((IfStatementExpNode) falseBranch2).getCondition(), symbolicVariables.stream().collect(Collectors.toMap(s -> "$" + s, s -> 0)));
          if (vars.keySet().stream().map(s -> s.substring(1)).anyMatch(s -> symbolicVariables.contains(s))) {
            break;
          }
          falseBranch2 = ((IfStatementExpNode) falseBranch2).getFalseBranch();
        }
        //One was found, we must include it as it is possible for there to be hidden variables that can go through that branch.
        if (falseBranch2 != null) {
          return new ChoiceNode(trueBranch, falseBranch, astNode.getLocation());
        }
      }
      return trueBranch;
    } else if (astNode.hasFalseBranch()) {
      return falseBranch;
    }
    return new EmptyTestOnlyNode();
  }

  private FunctionNode expand(FunctionNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
    List<ASTNode> processes = astNode.getProcesses();
    //System.out.println("expand function "+ astNode.myString());
    for (int i = 0; i < processes.size(); i++) {
      ASTNode process = processes.get(i);
      process = expand(process, variableMap, context);
      if (astNode.getReferences() != null) {
        Set<String> unReplacements = (Set<String>) astNode.getReplacements();
        //System.out.println("unR "+unReplacements);
        HashMap<String, Expr> replacements = new HashMap<>();
        for (String str : unReplacements) {
          String var = str.substring(0, str.indexOf('='));
          String exp = str.substring(str.indexOf('=') + 1);
          Expr expression;
          //System.out.println("global "+ globalVariableMap.keySet().stream().map(x->x+"->"+globalVariableMap.get(x).toString()).collect(Collectors.joining()));
          if (globalVariableMap.containsKey(exp)) {
            expression = globalVariableMap.get(exp);
          } else {
            expression = Expression.constructExpression(exp, astNode.getLocation(), context);
            //System.out.println("z3 "+expression.toString());
          }
          replacements.put("$" + var, expression);
        }
        astNode.setReplacements(replacements);
      }
      astNode.getProcesses().set(i, process);
    }
    return astNode;
  }


  private IdentifierNode expand(IdentifierNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
    String identifier = processVariables(astNode.getIdentifier(), variableMap, astNode.getLocation(), context);
    astNode.setIdentifier(identifier);
    return astNode;
  }

  private String l2s(List<ASTNode> lt) {
    return lt.stream().map(n -> n.myString()).collect(Collectors.joining(", "));
  }

  private ASTNode expand(ForAllStatementNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
    //System.out.println("EXPANDER Z0 ForAllStatementNode " + forAllId + "\n   " + astNode.myString());
    CompositeNode out = null;
    IdentifierNode idn = null;
    boolean once = true;
    List<IndexExpNode> iexs =  astNode.getRanges().getRanges();

    //System.out.println("iexs "+iexs.stream().map(x->x.myString()).collect(Collectors.joining(" ,")));
    List<LocalProcessNode> localProcesses = new ArrayList<>();
/// the Integer is the value  the variable has been instntiated to
    //String vm = variableMap.keySet().stream().map(n -> n+"->"+variableMap.get(n).toString()).collect(Collectors.joining(", "));
    //System.out.println("ForAll 1  vm = "+vm);
    //System.out.println(" STARTing inner expantion of  "+astNode.getProcess().myString());

    //Nested forall recursive call tweak the names!
    Map<Integer, ASTNode> nodes = expand(astNode.getProcess(), variableMap, astNode.getRanges().getRanges(), context);

    String vm = variableMap.keySet().stream().map(n -> n+"->"+variableMap.get(n).toString()).collect(Collectors.joining(", "));
    String o = nodes.values().stream().map(n -> n.myString()).collect(Collectors.joining(", "));
    //System.out.println("ForAll expanded nodes = "+o+ "  \n vm = "+vm);

    // Build local processes and Process that is their Parallel conp
    RangesNode rEmpty = new RangesNode(new ArrayList<>(),astNode.getLocation());
    int i = 1;
    String nextPr = ""; // This is the name of the local process
      for (int imap : nodes.keySet()) {
        LocalProcessNode lpn = null;
        IdentifierNode idn2 ;
        nextPr = forAllId + "[" + imap + "]";  // No nested forall backed in
        if (i == 1) {
          ASTNode node = nodes.get(imap);
          idn = new IdentifierNode(nextPr, node.getLocation());
          lpn = new LocalProcessNode(nextPr, rEmpty, node, astNode.getLocation());
          localProcesses.add(lpn);
        } else if (i == 2) {
          ASTNode n2 = nodes.get(imap);
          idn2 = new IdentifierNode(nextPr, n2.getLocation());
          LocalProcessNode lpn2 =
            new LocalProcessNode(nextPr, rEmpty, n2, astNode.getLocation());
          localProcesses.add(lpn2);
          out = new CompositeNode("||", idn, idn2, astNode.getLocation(), new HashSet<String>());
        } else {
          ASTNode nextNode = nodes.get(imap);
          lpn = new LocalProcessNode(nextPr, rEmpty, nextNode, astNode.getLocation());
          localProcesses.add(lpn);
          IdentifierNode idni = new IdentifierNode(nextPr, astNode.getLocation());
          out = new CompositeNode("||", idni, out, astNode.getLocation(), new HashSet<String>());
          //System.out.println("For "+i+" out "+ out.myString());
        }
        i++;
        // we need to instantiate the variable in the Local processes

          for (IndexExpNode iex : iexs) {
            String variable = iex.getVariable();
            //System.out.println("variable " + variable + " Lcnt " + astNode.getLocalProcesses().size());
            variableMap.put(variable, imap);
            break;
          }
        //if (once) { // Once only expand and process local to ForAllS
          //System.out.println("ONCE");
          //once = false;
          List<LocalProcessNode> localExpanded =
            expandLocalProcesses(astNode.getLocalProcesses(), variableMap, context);
          forAllLocalProcesses.addAll(localExpanded);
          if (forAllLocalProcesses.size() > 0) {
            //System.out.println("Expanded ForAll LOCAL local processes");
            //System.out.println(forAllLocalProcesses.stream().map(x -> "  " + x.myString()).collect(Collectors.joining(" ,")));
          }
       // }
      }
    forAllLocalProcesses.addAll(localProcesses);

      if (out==null) {
        if (idn==null) {
          throw new CompilationException(Expander.class, "forall to small: ", astNode.getLocation());
        } else {
          return idn;
        }
      }
      /*
      If this is a nested forall both
           the local process names need to be indexed by outside index
           the global process
       */
     String ls = forAllLocalProcesses.stream().map(x->"  "+x.myString()+"\n").collect(Collectors.joining());
    //System.out.println("forAll EXPANDER ENDs with "+ " " + out.myString()+"\n" + ls );
    return out;
  }

  /*
      ONLY call from forAllStatementNode
       builds  a map of indexed processes  one for each index value
           later to be expanded
   */
  private Map<Integer, ASTNode> expand(ASTNode process, Map<String, Object> variableMap,
                                       List<IndexExpNode> ranges, Context context) throws CompilationException, InterruptedException {
    Map<Integer, ASTNode> nodes = new TreeMap<>();
    ASTNode ps = process.copy();
    //System.out.println("EXPAND 4all  process " + ps.myString());
    int ir = 0;
    for (int ix = 0; ix < ranges.size(); ix++) {// more than one range

      IndexExpNode node = ranges.get(ix);
      IndexIterator iterator = IndexIterator.construct(expand(node, context));
      String variable = node.getVariable();
      //System.out.println("Expander var " + variable + " ix = " + ix + " ir = " + ir);
      while (iterator.hasNext()) {

        if (iterator instanceof RangeIterator) {
         ir =  ((RangeIterator) iterator).next() ;
        } else {
          iterator.next();
        }
        variableMap.put(variable, ir);
        //System.out.println(variableMap.keySet().stream().map(x->x+"->"+variableMap.get(x).toString()).collect(Collectors.joining(", ")));
          //System.out.println("    iterating input " + ps.myString());
        ASTNode psNew = expand(ps.copy(), variableMap, context);
        //System.out.println( "   iterating output ps " + psNew.myString() +" while ir = " + ir );
        nodes.put(ir, psNew);
        ir++;
      }
    }
    String o = nodes.values().stream().map(n -> n.myString()).collect(Collectors.joining(", "));
    //System.out.println(" EXPAND 4all RETURNS map \n    " + o);
    return nodes;

  }
/*  HOLD
  private Map<Integer, ASTNode> expand(ASTNode process, Map<String, Object> variableMap,
                                       List<IndexExpNode> ranges, Context context) throws CompilationException, InterruptedException {
    Map<Integer, ASTNode> nodes = new TreeMap<>();
    ASTNode ps = process.copy();
    //System.out.println("EXPAND 4all  process " + process.myString());
    int ir = 0;
    for (int ix = 0; ix < ranges.size(); ix++) {// more than one range

      IndexExpNode node = ranges.get(ix);
      IndexIterator iterator = IndexIterator.construct(expand(node, context));
      String variable = node.getVariable();
      //System.out.println("Expander var " + variable + " ix = " + ix + " ir = " + ir);
      while (iterator.hasNext()) {
        iterator.next();
        variableMap.put(variable, ir);
        //System.out.println(variableMap.keySet().stream().map(x->x+"->"+variableMap.get(x).toString()).collect(Collectors.joining(", ")));

        ASTNode psNew = expand(ps.copy(), variableMap, context);
        //System.out.println(" while ir = " + ir + " expanded into ps " + psNew.myString());
        nodes.put(ir, psNew);
        ir++;
      }
    }
    //String o = nodes.values().stream().map(n -> n.myString()).collect(Collectors.joining(", "));
    //System.out.println(" EXPAND 4all RETURNS map \n    " + o);
    return nodes;

  }
*/

  private RelabelNode expand(RelabelNode relabel, Context context) throws CompilationException, InterruptedException {
    List<RelabelElementNode> relabels = new ArrayList<>();

    for (RelabelElementNode element : relabel.getRelabels()) {
      if (!element.hasRanges()) {
        relabels.add(element);
      } else {
        Map<String, Object> variableMap = new HashMap<>();
        relabels.addAll(expand(element, variableMap, element.getRanges().getRanges(), 0, context));
      }
    }

    return new RelabelNode(relabels, relabel.getLocation());
  }

  private List<RelabelElementNode> expand(RelabelElementNode element,
                                          Map<String, Object> variableMap, List<IndexExpNode> ranges,
                                          int index, Context context)
    throws CompilationException, InterruptedException {
    List<RelabelElementNode> elements = new ArrayList<>();

    if (index < ranges.size()) {
      IndexExpNode node = ranges.get(index);
      IndexIterator iterator = IndexIterator.construct(expand(node, context));
      String variable = node.getVariable();

      while (iterator.hasNext()) {
        variableMap.put(variable, iterator.next());
        elements.addAll(expand(element, variableMap, ranges, index + 1, context));
      }
    } else {
      String newLabel = processVariables(element.getNewLabel(), variableMap, element.getLocation(), context);
      String oldLabel = processVariables(element.getOldLabel(), variableMap, element.getLocation(), context);
      elements.add(new RelabelElementNode(newLabel, oldLabel, element.getLocation()));
    }

    return elements;
  }

  private SetNode expand(SetNode set, Context context) throws CompilationException, InterruptedException {
    // check if any ranges were defined for this set
    Map<Integer, RangesNode> rangeMap = set.getRangeMap();
    if (rangeMap.isEmpty()) {
      return set;
    }

    List<String> actions = set.getSet();
    List<String> newActions = new ArrayList<>();
    for (int i = 0; i < actions.size(); i++) {
      if (rangeMap.containsKey(i)) {
        Map<String, Object> variableMap = new HashMap<>();
        newActions.addAll(expand(actions.get(i), variableMap, rangeMap.get(i).getRanges(), 0, context));
      } else {
        newActions.add(actions.get(i));
      }
    }

    return new SetNode(newActions, set.getLocation());
  }

  /*
     Processes multiple ranges!
   */
  private List<String> expand(String action, Map<String, Object> variableMap, List<IndexExpNode> ranges, int index, Context context)
    throws CompilationException, InterruptedException {
    List<String> actions = new ArrayList<>();
    if (index < ranges.size()) {
      IndexExpNode node = ranges.get(index);
      IndexIterator iterator = IndexIterator.construct(expand(node, context));
      String variable = node.getVariable();

      while (iterator.hasNext()) {
        variableMap.put(variable, iterator.next());
        actions.addAll(expand(action, variableMap, ranges, index + 1, context));
      }
    } else {
      actions.add(processVariables(action, variableMap, getFullRangeLocation(ranges), context));
    }
    //System.out.println("expanding "+action+" into "+actions);
    return actions;
  }

  //Get the location from a ranges node.
  private Location getFullRangeLocation(List<IndexExpNode> ranges) {
    Location start = ranges.get(0).getLocation();
    Location end = ranges.get(ranges.size() - 1).getLocation();
    return new Location(start.getLineStart(), start.getColStart(), end.getLineEnd(), end.getColEnd(), start.getStartIndex(), end.getEndIndex());
  }

  private boolean evaluateCondition(BoolExpr condition, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
    Map<String, Integer> variables = new HashMap<>();
    for (Map.Entry<String, Object> entry : variableMap.entrySet()) {
      if (entry.getValue() instanceof Integer) {
        variables.put(entry.getKey(), (Integer) entry.getValue());
        //Hard codeing restriction to Integers
      }
    }
    boolean b = Expression.isSolvable(condition, variables, context);
    //System.out.println("z3 "+condition.toString()+" "+ variableMap.entrySet()+" returns "+b);
    return b;
  }

  /*
    called when expanding Identifier  and an event name v$ -> i$ < 2
                   DOES ALL THE CHANGEING
   */
  private String processVariables(String string, Map<String, Object> variableMap, Location location, Context context) throws CompilationException, InterruptedException {
    Map<String, Integer> integerMap = constructIntegerMap(variableMap);
    //System.out.println("integer Map "+integerMap.entrySet().stream().map(x->x.getKey()+"->"+x.getValue()+", ").collect(Collectors.joining()));
    //Construct a pattern with all hidden variables removed.
    String d = string;
    Pattern pattern = Pattern.compile(VAR_PATTERN + symbolicVariables.stream().map(s -> "(?<!\\$" + s + ")").collect(Collectors.joining()) + "\\b");
    //System.out.println("string "+ string+ " pattern "+pattern.pattern());
    while (true) {
      Matcher matcher = pattern.matcher(string);
      if (matcher.find()) {
        String variable = matcher.group();
        //System.out.println("FOUND "+variable);
        // check if the variable is a global variable
        if (globalVariableMap.containsKey(variable)) {
          //System.out.println(" in globalVariableMap");
          Expr expression = globalVariableMap.get(variable);
          if (containsHidden(expression)) {
            string = string.replaceAll(Pattern.quote(variable) + "\\b", "" + ExpressionPrinter.printExpression(expression).replace("$", ""));
          } else {
            int result = evaluator.evaluateIntExpression(expression, integerMap, context);
            string = string.replaceAll(Pattern.quote(variable) + "\\b", "" + result);
          }
        } else if (integerMap.containsKey(variable)) {
          //System.out.println(" in variableMap");
          string = string.replaceAll(Pattern.quote(variable) + "\\b", "" + integerMap.get(variable));
        } else if (variableMap.containsKey(variable)) {
          //System.out.println(" Not integer in variableMap");
          string = string.replaceAll(Pattern.quote("[" + variable + "]"), "" + variableMap.get(variable));
        } else {
          throw new CompilationException(Expander.class, "Unable to find a variable replacement for: " + variable, location);
        }
      } else {
        //System.out.println("NOT FOUND ");
        break;
      }
    }
    //System.out.println("processVariables "+ d+" -> "+string);
    return string;
  }

  private boolean containsHidden(Expr ex) {
    //If there is an "and" inside this expression, then don't check its variables as it is added on its own.
    if (ex.isAnd()) {
      return false;
    }
    if (ex.isConst()) {
      return symbolicVariables.contains(ex.toString().substring(1));
    }
    for (Expr expr : ex.getArgs()) {
      if (containsHidden(expr)) {
        return true;
      }
    }
    return false;
  }

  private Map<String, Integer> constructIntegerMap(Map<String, Object> variableMap) {
    Map<String, Integer> integerMap = new HashMap<>();
    for (Map.Entry<String, Object> entry : variableMap.entrySet()) {
      if (entry.getValue() instanceof Integer) {
        integerMap.put(entry.getKey(), (Integer) entry.getValue());
      }
    }

    return integerMap;
  }
}
