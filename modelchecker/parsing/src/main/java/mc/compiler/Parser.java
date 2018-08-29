package mc.compiler;

import static mc.util.Utils.instantiateClass;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.BitVecNum;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import mc.Constant;
import mc.compiler.ast.*;
import mc.compiler.token.*;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.plugins.IProcessInfixFunction;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.AutomatonNode;
import mc.util.Location;
import mc.util.expr.Expression;
import mc.util.expr.ExpressionEvaluator;
import mc.util.expr.ExpressionPrinter;
/*
 * Created by sheriddavi on 30/01/17.
 */
public class Parser {

  //Plugin
  static Map<String, Class<? extends IProcessFunction>> functions = new HashMap<>();
  static Map<String, Class<? extends IProcessInfixFunction>> infixFunctions = new HashMap<>();


  private List<Token> tokens;
  private List<ProcessNode> processes;
  private Set<String> processIdentifiers;
  private List<OperationNode> operations;
  private List<OperationNode> equations;
  private List<ActionLabelNode> alphabet;
  private Map<String, Expr> variableMap;
  private Map<String, ASTNode> constantMap;
  private List<IndexExpNode> actionRanges;
  private Set<String> definedVariables;
  private int index;
  private int variableId;
  private Context context;

  private ExpressionEvaluator expressionEvaluator;

  public Parser() {
    processes = new ArrayList<>();
    processIdentifiers = new HashSet<>();
    operations = new ArrayList<>();
    equations = new ArrayList<>();
    alphabet = new ArrayList<>();
    variableMap = new HashMap<>();
    constantMap = new HashMap<>();
    actionRanges = new ArrayList<>();
    definedVariables = new HashSet<>();
    index = 0;
    variableId = 0;
    expressionEvaluator = new ExpressionEvaluator();
  }

  private static CompilationException constructException(String message, Location location) {
    return new CompilationException(Parser.class, message, location);
  }

  public AbstractSyntaxTree parse(List<Token> tokens, Context context) throws CompilationException, InterruptedException {
    reset();
    System.out.println("Parse "+ tokens);
    this.tokens = tokens;
    this.context = context;

    while (index < this.tokens.size() && !Thread.currentThread().isInterrupted()) {
      Token token = peekToken();
      if (token instanceof ProcessesDefintionToken) {
        parseProcessesDefinition();
      } else if (token instanceof ConstToken) {
        parseConstDefinition();
      } else if (token instanceof DisplayTypeToken) {  //might be dead code
        parseDisplayType();
      } else if (token instanceof RangeToken) {
        parseRangeDefinition();
      } else if (token instanceof SetToken) {
        parseSetDefinition();
      } else if (token instanceof OperationToken) {
        parseOperation();
      }else if (token instanceof AlphabetToken) {
        parseAlphabet();
      } else if (token instanceof EquationToken) {
        parseEquation();
      } else {
        throw constructException("expecting to parse a process, operation or const definition but received \"" + token.toString() + "\"", token.getLocation());
      }
    }

    return new AbstractSyntaxTree(processes, alphabet, operations, equations, variableMap);
  }

  /**
   * Attempts to parse and return an @code{IdentifierNode} the current position
   * in the @code{List} of {Tokens}.
   *
   * @return -- an @code{IdentifierNode}
   */
  private IdentifierNode parseIdentifier() throws CompilationException {
    Token token = nextToken();
    if (token instanceof IdentifierToken) {
     //System.out.println("parse Identifyer "+((IdentifierToken) token).getIdentifier());
      IdentifierToken identifier = (IdentifierToken) token;
      return new IdentifierNode(identifier.getIdentifier(), identifier.getLocation());
    }

    throw constructException("expecting to parse an identifier but received \"" + token.toString() + "\"", token.getLocation());
  }

  /**
   * Attempts to parse an @code{ActionLabelNode} from the current position in the @code{List} of @code{Tokens}.
   * An @code{ActionLabelNode} is of the form:
   * <p>
   * <pre>
   *     ActionLabel := (Label | '[' (Expression | ActionRange) ']') [['.'] ActionLabel]
   * </pre>
   *
   * @return -- the parsed @code{ActionLabelNode}
   */
  private ActionLabelNode parseActionLabel() throws CompilationException, InterruptedException {
    int start = index;
    StringBuilder builder = new StringBuilder();

    while (true) {
      Token token = nextToken();

      if (token instanceof ActionToken) {
        builder.append(((ActionToken) token).getAction());
      } else if (token instanceof OpenBracketToken) {
        String variable;
        if (hasLabel() || peekToken() instanceof OpenBraceToken) {
          variable = parseActionRange();
        } else if (hasIdentifierReference()) {
          String identifier = ((IdentifierToken) peekToken()).getIdentifier();
          if (!constantMap.containsKey(identifier)) {
            throw constructException("The identifier \"" + identifier + "\" has not been defined");
          }

          ASTNode constant = constantMap.get(identifier);
          if (constant instanceof ConstParseOnlyNode) {
            variable = parseActionRangeOrExpression();
          } else {
            variable = parseActionRange();
          }
        } else if (hasVariableReference()) {
          variable = "$" + ((ActionToken) nextToken()).getAction();
        } else {
          variable = parseActionRangeOrExpression();
        }

        if (!(nextToken() instanceof CloseBracketToken)) {
          Token error = tokens.get(index - 1);
          throw constructException("Expecting to parse a \"]\" but received \"" + error.toString() + "\"", error.getLocation());
        }

        builder.append("[").append(variable).append("]");
      } else {
        throw constructException("expecting to parse an action label or action range but received \"" + token.toString() + "\"", token.getLocation());
      }

      if (peekToken() instanceof DotToken) {
        // gobble the dot token and add it to the action label
        nextToken();
        builder.append(".");
      } else if (!(peekToken() instanceof OpenBracketToken) && !(peekToken() instanceof ActionToken)) {
        break;
      }
    }

    // check if this action label has been specified as either a broadcaster or a receiver
    if (peekToken() instanceof NegateToken || peekToken() instanceof QuestionMarkToken
      || peekToken() instanceof ExclOrToken) {
      Token token = nextToken();
      if (token instanceof NegateToken) {
        builder.append(Constant.BROADCASTSoutput);
      } else if (token instanceof QuestionMarkToken) {
        builder.append(Constant.BROADCASTSinput);
      } else {
        builder.append(Constant.ACTIVE);
      }
    }

    return new ActionLabelNode(builder.toString(), constructLocation(start));
  }

  // CONSTANT DEFINITIONS

  /**
   * Attempts to parse an @code{ActionRange} from the current position in the @code{List} of @code{Tokens}.
   * Returns a @code{String} that represents the variable that can contain the values specified in the parsed @code{ActionRange}.
   * An @code{ActionLabelNode} is of the form:
   * <p>
   * <pre>
   *     ActionRange := [Label ':'] (Identifier | Range | Set)
   * </pre>
   *
   * @return -- @code{String} variable that is used by the parsed @code{ActionRange}
   */
  private String parseActionRange() throws CompilationException, InterruptedException {
    int start = index;
    String variable;
    if (hasLabel()) {
      String label = parseLabel();
      definedVariables.add(label);
      variable = "$" + label;
    } else {
      // a variable has not been defined, give the variable a unique internally defined name
      variable = nextVariableId();
    }

    ASTNode range;
    if (peekToken() instanceof IdentifierToken && !(tokens.get(index + 1) instanceof RangeSeparatorToken)) {
      IdentifierNode identifier = parseIdentifier();

      if (!constantMap.containsKey(identifier.getIdentifier())) {
        throw constructException("the identifier \"" + identifier.getIdentifier() + "\" has not been defined", identifier.getLocation());
      }

      ASTNode constant = constantMap.get(identifier.getIdentifier());

      if (constant instanceof ConstParseOnlyNode) {
        throw constructException("expecting a range or set constant but received a const", identifier.getLocation());
      }

      range = constant;
    } else if (peekToken() instanceof OpenBraceToken) {
      range = parseSet();
    } else {
      range = parseRange();
    }

    IndexExpNode indexNode = new IndexExpNode(variable, range, null, constructLocation(start));
    actionRanges.add(indexNode);

    return variable;
  }

  /**
   * Attempts to parse a @code{RangeNode} from the current position in the @code{List} of @code{Tokens}.
   * A @code{RangeNode} is of the form:
   * <p>
   * <pre>
   *     Range := Expression '..' Expression
   * </pre>
   *
   * @return -- the parsed @code{RangeNode}
   */
  private RangeNode parseRange() throws CompilationException, InterruptedException {
    int start = index;

    // parse the next expression and evaluate it if necessary to get the start value of the range
    String expression = parseExpression();
    int startValue;
    if (variableMap.containsKey(expression)) {
      // in this case, 'expression' is an internal variable reference to an expression
      startValue = expressionEvaluator.evaluateExpression(variableMap.get(expression), new HashMap<>(), context);
    } else {
      try {
        // otherwise the expression is an integer that we can parse
        startValue = Integer.parseInt(expression);
      } catch (NumberFormatException ex) {
        throw constructException("expecting to parse a number but received \"" + expression + "\"", constructLocation(start));
      }
    }

    // ensure that the next token is the '..' token
    if (!(nextToken() instanceof RangeSeparatorToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"..\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    start = index;
    // parse the next expression and evaluate it if necessary to get the start value of the range
    expression = parseExpression();
    int endValue;
    if (variableMap.containsKey(expression)) {
      // in this case, 'expression' is an internal variable reference to an expression
      endValue = expressionEvaluator.evaluateExpression(variableMap.get(expression), new HashMap<>(), context);
    } else {
      // otherwise the expression is an integer that we can parse
      try {
        // otherwise the expression is an integer that we can parse
        endValue = Integer.parseInt(expression);
      } catch (NumberFormatException ex) {
        throw constructException("expecting to parse a number but received \"" + expression + "\"", constructLocation(start));
      }
    }

    return new RangeNode(startValue, endValue, constructLocation(start));
  }

  /**
   * Attempts to parse a @code{SetNode} from the current position in the @code{List} of @code{Tokens}.
   * A @code{SetNode} is of the form:
   * <p>
   * <pre>
   *     Set := '{' ActionLabel (',' ActionLabel)* '}'
   * </pre>
   *
   * @return -- the parsed @code{SetNode}
   */
  private SetNode parseSet() throws CompilationException, InterruptedException {
    int start = index;
    // ensure the next token is the '{' token
    if (!(nextToken() instanceof OpenBraceToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"{\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    List<String> set = new ArrayList<>();
    Map<Integer, RangesNode> rangeMap = new HashMap<>();

    while (!(peekToken() instanceof CloseBracketToken)) {
      // parse the current action and add it to the set

      int rangeStart = actionRanges.size();
      ActionLabelNode action = parseActionLabel();

      if (rangeStart < actionRanges.size()) {
        List<IndexExpNode> ranges = new ArrayList<>(actionRanges.subList(rangeStart, actionRanges.size()));
        actionRanges = new ArrayList<>(actionRanges.subList(0, rangeStart));
        RangesNode range = new RangesNode(ranges, action.getLocation());
        rangeMap.put(set.size(), range);
      }

      set.add(action.getAction());

      // check if another action label can be parsed
      if (!(peekToken() instanceof CommaToken)) {
        break;
      }

      // gobble the comma token
      nextToken();
    }

    // ensure the next token is the '}' token
    if (!(nextToken() instanceof CloseBraceToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"}\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    return new SetNode(set, rangeMap, constructLocation(start));
  }

  // PROCESS DEFINITIONS

  private void parseConstDefinition() throws CompilationException, InterruptedException {
    // ensure that the next token is the 'const' token
    if (!(nextToken() instanceof ConstToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"const\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    IdentifierNode identifier = parseIdentifier();

    // check if a process with this identifier has already been defined
    if (processIdentifiers.contains(identifier.getIdentifier())) {
      throw constructException("The identifier \"" + identifier.getIdentifier() + "\" has already been defined", identifier.getLocation());
    }
    processIdentifiers.add(identifier.getIdentifier());

    // ensure that the next token is the '=' token
    if (!(nextToken() instanceof AssignToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"=\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    int start = index;
    int value = parseSimpleExpression();

    if (!(nextToken() instanceof DotToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \".\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    ConstParseOnlyNode node = new ConstParseOnlyNode(value, constructLocation(start));
    constantMap.put(identifier.getIdentifier(), node);
  }

  private void parseRangeDefinition() throws CompilationException, InterruptedException {
    // ensure the next token is the 'range' token
    if (!(nextToken() instanceof RangeToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"range\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    IdentifierNode identifier = parseIdentifier();

    // check if a process with this identifier has already been defined
    if (processIdentifiers.contains(identifier.getIdentifier())) {
      throw constructException("The identifier \"" + identifier.getIdentifier() + "\" has already been defined", identifier.getLocation());
    }
    processIdentifiers.add(identifier.getIdentifier());

    // ensure the next token is the '=' token
    if (!(nextToken() instanceof AssignToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"=\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    int start = index;
    int startValue = parseSimpleExpression();

    // ensure the next token is the '..' token
    if (!(nextToken() instanceof RangeSeparatorToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"..\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    int endValue = parseSimpleExpression();

    if (!(nextToken() instanceof DotToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \".\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    RangeNode range = new RangeNode(startValue, endValue, constructLocation(start));
    constantMap.put(identifier.getIdentifier(), range);
  }

  private void parseSetDefinition() throws CompilationException, InterruptedException {
    // ensure that the next token is the 'set' token
    if (!(nextToken() instanceof SetToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"set\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    IdentifierNode identifier = parseIdentifier();

    // check if a process with this identifier has already been defined
    if (processIdentifiers.contains(identifier.getIdentifier())) {
      throw constructException("The identifier \"" + identifier.getIdentifier() + "\" has already been defined", identifier.getLocation());
    }
    processIdentifiers.add(identifier.getIdentifier());

    // ensure that the next token is the '=' token
    if (!(nextToken() instanceof AssignToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"=\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    SetNode set = parseSet();

    if (!(nextToken() instanceof DotToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \".\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    constantMap.put(identifier.getIdentifier(), set);
  }

  private void parseProcessesDefinition() throws CompilationException, InterruptedException {
    nextToken();// Eat the ProcessDefintionToken

    Token token = peekToken();
    if (token instanceof IdentifierToken) {
      parseSingleProcessDefinition();
    } else if (token instanceof OpenBraceToken) {
      parseProcessDefinitionBlock();
    } else {
      throw constructException("expecting to parse a process definition or a process definition block but received \"" + peekToken().toString() + "\"");
    }
  }

  /**
   * parsing a single process def " A = a->STOP"
   * @throws CompilationException
   * @throws InterruptedException
   */
  private void parseSingleProcessDefinition() throws CompilationException, InterruptedException {
    int start = index;
    IdentifierNode identifier = parseIdentifier();
    // check if a process with this identifier has already been defined
    if (processIdentifiers.contains(identifier.getIdentifier())) {
      throw constructException("The identifier \"" + identifier.getIdentifier() + "\" has already been defined", identifier.getLocation());
    }

    processIdentifiers.add(identifier.getIdentifier());

    // check if this process has been marked as not to be rendered *Depericated*.
    if (peekToken() instanceof MultiplicationToken) {
      nextToken();
    }

    // ensure that the next token is the '=' token
    if (!(peekToken() instanceof AssignToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"=\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    AssignToken  at = (AssignToken) nextToken();
    if (peekToken()  instanceof AutomatonToken) {
      at.setPType(ProcessType.AUTOMATA);
      nextToken();
    }
    ASTNode process = parseComposite();

    List<LocalProcessNode> localProcesses = new ArrayList<>();
    Set<String> localIdentifiers = new HashSet<>();

    while (peekToken() instanceof CommaToken && !Thread.currentThread().isInterrupted()) {
      nextToken(); // gobble the comma
      localProcesses.add(parseLocalProcessDefinition(localIdentifiers));
    }

    //Dont set ProcessNode type just yet as that will be done when,
    //or if we encounter a display node that sets it.
    ProcessNode processNode = new ProcessNode(identifier.getIdentifier(), process, localProcesses,
        constructLocation(start));
    // "P = term"  build petrinet  and "P =A term" build  automata
    if (at.getPType().equals(ProcessType.AUTOMATA))  // lexer sets type of AssignToken
         processNode.getType().add("automata");
    else if (at.getPType().equals(ProcessType.PETRINET))
      processNode.getType().add("petrinet");

    // check if a relabel set has been defined
    if (peekToken() instanceof DivisionToken) {
     //System.out.println("Division Token");
      processNode.setRelabels(parseRelabel());
    }

    // check if a hiding set has been defined
    if (peekToken() instanceof HideToken || peekToken() instanceof AtToken) {
      processNode.setHiding(parseHiding());
    }

    // check if an interrupt process has been defined
    if (peekToken() instanceof InterruptToken) {
      processNode.setInterrupt(parseInterrupt());
    }

    // check if a variable set has been defined
    if (peekToken() instanceof DollarToken) {
      processNode.setVariables(parseVariables());
    }

    // ensure that the next token is the '.' token
    if (!(nextToken() instanceof DotToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \".\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    processes.add(processNode);
  }

  private void parseProcessDefinitionBlock() throws CompilationException, InterruptedException {
    // ensure that the next token is the '{' token
    if (!(nextToken() instanceof OpenBraceToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"{\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    while (!(peekToken() instanceof CloseBraceToken)) {
      parseSingleProcessDefinition();
    }

    // ensure that the next token is the '}' token
    if (!(nextToken() instanceof CloseBraceToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"}\" but received \"" + error.toString() + "\"", error.getLocation());
    }
  }

  private LocalProcessNode parseLocalProcessDefinition(Set<String> localIdentifiers) throws CompilationException, InterruptedException {
    int start = index;
    IdentifierNode identifier = parseIdentifier();

    // check if ranges have been defined for this process
    RangesNode ranges = null;
    if (peekToken() instanceof OpenBracketToken) {
      ranges = parseRanges();
    }

    // check if a local process with this identifier has already been defined
    if (localIdentifiers.contains(identifier.getIdentifier())) {
      throw constructException("The identifier \"" + identifier.getIdentifier() + "\" has already been defined", identifier.getLocation());
    }
    localIdentifiers.add(identifier.getIdentifier());

    if (!(nextToken() instanceof AssignToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"=\" but received \"" + error.toString() + "\"", error.getLocation());
    }
    ASTNode process;
    if (peekToken() instanceof OpenParenToken) {
      process = parseLocalProcess();
    } else {
      process = parseComposite();
    }

    return new LocalProcessNode(identifier.getIdentifier(), ranges, process, constructLocation(start));
  }

  private void parseDisplayType() throws CompilationException {
    Token token = nextToken();

    if (!(token instanceof DisplayTypeToken)) {
      throw constructException("expecting to parse a display type but received \"" + token.toString() + "\"", token.getLocation());
    }
System.out.println("parseDisplayType "+ token.toString());
    DisplayTypeToken currentDisplayType = (DisplayTypeToken) token;

    token = nextToken();
    if (!(token instanceof IdentifierToken)) {
      throw constructException("expecting to parse a identifier but received \"" + token.toString() + "\"", token.getLocation());
    }

    boolean contained = false;

    for (ProcessNode node : processes) {
      if (node.getIdentifier().equals(((IdentifierToken) token).getIdentifier())) {
        node.addType(currentDisplayType.getProcessType());
        contained = true;
        break;
      }
    }

    if (!contained) {
      throw constructException("identifier not defined \"" + ((IdentifierToken) token).getIdentifier() + "\"", token.getLocation());
    }


    for (token = nextToken(); token instanceof CommaToken; token = nextToken()) {
      contained = false;
      token = nextToken();

      if (!(token instanceof IdentifierToken)) {
        throw constructException("expecting to parse a identifier but received \"" + token.toString() + "\"", token.getLocation());
      }

      for (ProcessNode node : processes) {
        if (node.getIdentifier().equals(((IdentifierToken) token).getIdentifier())) {
          node.addType(currentDisplayType.getProcessType());
          contained = true;
          break;
        }
      }

      if (!contained) {
        throw constructException("identifier not defined\"" + ((IdentifierToken) token).getIdentifier() + "\"", token.getLocation());
      }

    }

  }

  private String parseCastType() throws CompilationException {
    Token token = nextToken();
    if (!(token instanceof CastToken)) {
      throw constructException("expected cast token got\"" + token.toString() + "\"", token.getLocation());
    }

    return ((CastToken) token).getCastType();
  }
/*

 */
  private ASTNode parseComposite() throws CompilationException, InterruptedException {
    int start = index;

    String label = null;
    if (hasProcessLabel()) {
      label = parseProcessLabel();
    }

    ASTNode process = parseChoice();

    RelabelNode relabel = null;
    if (peekToken() instanceof DivisionToken) {
      relabel = parseRelabel();
     //System.out.println("GOT YOU relabel = "+ relabel.getRelabels());
    }

    HidingNode hiding = null;
    if (peekToken() instanceof HideToken || peekToken() instanceof AtToken) {
      hiding = parseHiding();
    }

    // wrap the parsed process as a process root if either a label, relabel or hiding has been defined
    if (label != null || relabel != null || hiding != null) {
      process = new ProcessRootNode(process, label, relabel, hiding, process.getLocation());
    }

    for (String key : infixFunctions.keySet()) {
      if (peekToken().toString().equals(key)) {
        nextToken(); // gobble the '||' token
        ASTNode process2 = parseComposite();
        process = new CompositeNode(key, process, process2, constructLocation(start));
        break;
      }
    }

    return process;
  }

  private ASTNode parseChoice() throws CompilationException, InterruptedException {
    int start = index;

    ASTNode process = parseLocalProcess();

    if (peekToken() instanceof BitOrToken) {
      nextToken(); // gobble the '|' token
      ASTNode process2 = parseComposite();
      return new ChoiceNode(process, process2, constructLocation(start));
    }

    return process;
  }

  //could be  pingo -> C[i+1][0]..

  private ASTNode parseLocalProcess() throws CompilationException, InterruptedException {
    if (peekToken() instanceof OpenParenToken) {

      nextToken();
      ASTNode process = parseComposite();
      if (!(nextToken() instanceof CloseParenToken)) {
        Token error = tokens.get(index - 1);
        throw constructException("expecting to parse \")\" but received \"" + error.toString() + "\"", error.getLocation());
      }

      return process;
    } else if (peekToken() instanceof ActionToken || peekToken() instanceof OpenBracketToken) {
      return parseSequence();
    }

    return parseBaseLocalProcess();
  }

  private ASTNode parseSequence() throws CompilationException, InterruptedException {
    int start = index;

    int rangeStart = actionRanges.size();
    ActionLabelNode from = parseActionLabel();

    List<IndexExpNode> ranges = new ArrayList<>();
    if (rangeStart < actionRanges.size()) {
      ranges.addAll(actionRanges.subList(rangeStart, actionRanges.size()));
      actionRanges = new ArrayList<>(actionRanges.subList(0, rangeStart));
      Collections.reverse(ranges);
    }


// ensure that the next token is a ':' token
    if (peekToken() instanceof ColonToken) {
      index = start;
      return parseComposite();
    }
// ensure that the next token is a '->' token
    if (!(nextToken() instanceof SequenceToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"->\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    ASTNode to = parseLocalProcess();

    ASTNode node = new SequenceNode(from, to, constructLocation(start));
    for (IndexExpNode range : ranges) {
      range.setProcess(node);
      node = range;
    }

    return node;
  }

  /**
   *
   * @return
   * @throws CompilationException
   * @throws InterruptedException
   */
  private ASTNode parseBaseLocalProcess() throws CompilationException, InterruptedException {
    if (peekToken() instanceof TerminalToken) {
      return parseTerminal();
    } else if (peekToken() instanceof IdentifierToken) {
      IdentifierNode identifier = parseIdentifier();

      if (peekToken() instanceof OpenBracketToken) {
        identifier.setIdentifier(identifier.getIdentifier() + parseIndices());
      }

      return identifier;
    } else if (peekToken() instanceof OwnersRuleToken || peekToken() instanceof TokenRuleToken) {
      return parseConversion();
    } else if (peekToken() instanceof FunctionToken) {
      return parseFunction();
    } else if (peekToken() instanceof CastToken) {
      return parseCasting();
    } else if (peekToken() instanceof IfToken) { // Else token is parsed within the IfStatement block
      return parseIfStatement();
    } else if (peekToken() instanceof WhenToken) {
      return parseWhenStatement();
    } else if (peekToken() instanceof ForAllToken) {
      return parseForAllStatement();
    } else if (peekToken() instanceof OpenParenToken) {
      nextToken();
      ASTNode process = parseComposite();

      // ensure that the next token is a ')' token
      if (!(nextToken() instanceof CloseParenToken)) {
        Token error = tokens.get(index - 1);
        throw constructException("expecting to parse \")\" but received \"" + error.toString() + "\"", error.getLocation());
      }

      return process;
    }

    throw constructException("expecting to parse a base local process but received \"" + peekToken().toString() + "\"");
  }

  private ConversionNode parseConversion() throws CompilationException, InterruptedException {
    int start = index;
    String type = nextToken().toString();
    String from = ConversionNode.nameMap.get(type).get(0);
    String to = ConversionNode.nameMap.get(type).get(1);
    ASTNode process = parseComposite();
    return new ConversionNode(from, to, process,constructLocation(start));
  }

  private FunctionNode parseFunction() throws CompilationException, InterruptedException {
    int start = index;

    String type = parseFunctionType();

    IProcessFunction functionDefinition = instantiateClass(functions.get(type));

    // check if any flags have been set
    Set<String> flags = new HashSet<>();
    if (peekToken() instanceof OpenBraceToken) {
      flags = parseFlags(type);
    }

    // ensure that the next token is a '(' token
    if (!(nextToken() instanceof OpenParenToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"(\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    //get all the processes to be used in the function
    List<ASTNode> processes = new ArrayList<>();
    for (int i = 0; i < functionDefinition.getNumberArguments(); i++) {
      processes.add(parseComposite());
      if (i != functionDefinition.getNumberArguments() - 1) {
        if (!(nextToken() instanceof CommaToken)) {
          Token error = tokens.get(index - 1);
          throw constructException("expecting to parse \",\" but received \"" + error.toString() + "\"", error.getLocation());
        }
      }

    }

    // ensure that the next token is a ')' token
    if (!(nextToken() instanceof CloseParenToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \")\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    FunctionNode function = new FunctionNode(type, processes, constructLocation(start));
    function.setFlags(ImmutableSet.copyOf(flags));

    //TODO: replace
    if (type.equals("simp") && flags != null) {
      if (function.getReplacements() == null) {
        function.setReplacements(new HashMap<>());
      }

      for (String simpReplacements : flags) // UGH, *TODO* We need to change this. Im not sure why this is a thing to begin with.
      {
        function.getReplacements().put(simpReplacements, null);
      }
    }
    return function;
  }

  private String parseFunctionType() throws CompilationException {
    Token token = nextToken();
    if (token instanceof FunctionToken) {
      return ((FunctionToken) token).getFunction();
    }

    throw constructException("expecting to parse a function type but received \"" + token.toString() + "\"", token.getLocation());
  }

  private Set<String> parseFlags(String functionType) throws CompilationException, InterruptedException {

    if (!(nextToken() instanceof OpenBraceToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"{\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    ImmutableSet<String> acceptedFlags = ImmutableSet.copyOf(instantiateClass(functions.get(functionType)).getValidFlags());
    Set<String> flags = new HashSet<>();

    boolean wildcard = acceptedFlags.contains("*");

    while (!(peekToken() instanceof CloseBraceToken)) {

      if (!(peekToken() instanceof ActionToken)) {
        throw constructException("Expecting to parse a flag but received \"" + peekToken().toString() + "\"");
      }

      ActionToken token = (ActionToken) nextToken();
      String flag = token.getAction();

      if (!acceptedFlags.contains(flag) && !wildcard) {
        throw constructException("\"" + flag + "\" is not a correct flag for " + functionType, token.getLocation());
      }

      //TODO: remove this somehow
      if (Objects.equals(functionType, "simp")) {
        if (!(peekToken() instanceof AssignToken)) {
          throw constructException("Expecting to parse '=' but received \"" + peekToken().toString() + "\"");
        }
        nextToken();
        flag += "=" + parseExpression();
      }
      flags.add(flag);

      if (peekToken() instanceof CommaToken) {
        nextToken();
      }
    }

    if (!(nextToken() instanceof CloseBraceToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"}\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    return flags;
  }

  private FunctionNode parseCasting() throws CompilationException, InterruptedException {
    int start = index;
    String cast = parseCastType();

    // ensure that the next token is a '(' token
    if (!(nextToken() instanceof OpenParenToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"(\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    ASTNode process = parseComposite();

    // ensure the next token is a ')' token
    if (!(nextToken() instanceof CloseParenToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \")\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    return new FunctionNode(cast, Collections.singletonList(process), constructLocation(start));
  }

  private IfStatementExpNode parseIfStatement() throws CompilationException, InterruptedException {
    // ensure that the next token is a 'if' token
    if (!(nextToken() instanceof IfToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"if\" but received \"" + error.toString() + "\"", error.getLocation());
    }
    int start = index;
    String expr = parseExpression();
    Expr expression;
    if (variableMap.containsKey(expr)) {
      expression = variableMap.get(expr);
    } else {
      expression = Expression.constructExpression(expr, constructLocation(start), context);
    }
    if (!(expression instanceof BoolExpr)) {
      throw constructException("expecting to parse a boolean statement but received \"" + ExpressionPrinter.printExpression(expression) + "\"", constructLocation(start));
    }
    // ensure that the next token is a 'then' token
    if (!(nextToken() instanceof ThenToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"then\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    ASTNode trueBranch = parseLocalProcess();

    // check if an else branch was defined
    if (peekToken() instanceof ElseToken) {
      nextToken(); // gobble the 'then' token
      ASTNode falseBranch = parseLocalProcess();
      return new IfStatementExpNode((BoolExpr) expression, trueBranch, falseBranch, constructLocation(start), context);
    }

    return new IfStatementExpNode((BoolExpr) expression, trueBranch, constructLocation(start), context);
  }

  private IfStatementExpNode parseWhenStatement() throws CompilationException, InterruptedException {
    // ensure that the next token is a 'when' token

    if (!(nextToken() instanceof WhenToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"when\" but received \"" + error.toString() + "\"", error.getLocation());
    }

//parse boolean
    int start = index;
    String expr = parseExpression();

    Expr expression;
    if (variableMap.containsKey(expr)) {
      expression = variableMap.get(expr);
    } else {
      expression = Expression.constructExpression(expr, constructLocation(start), context);
    }
    if (!(expression instanceof BoolExpr)) {
      throw constructException("expecting to parse a boolean statement but received \"" + ExpressionPrinter.printExpression(expression) + "\"", constructLocation(start));
    }

//parse local process -- could be ping->P[i+1][0]...
    ASTNode trueBranch = parseLocalProcess();
    return new IfStatementExpNode((BoolExpr) expression, trueBranch, constructLocation(start), context);
  }

  private ForAllStatementNode parseForAllStatement() throws CompilationException, InterruptedException {
    int start = index;
    // ensure that the next token is a 'forall' token
    if (!(nextToken() instanceof ForAllToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"forall\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    RangesNode ranges = parseRanges();

    if (!(nextToken() instanceof OpenParenToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"(\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    ASTNode process = parseComposite();

    if (!(nextToken() instanceof CloseParenToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \")\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    return new ForAllStatementNode(ranges, process, constructLocation(start));
  }

  // RELABELLING AND HIDING

  private ASTNode parseTerminal() throws CompilationException {
    Token token = nextToken();
    if (token instanceof TerminalToken) {
      if (token instanceof StopToken) {
        return new TerminalNode(Constant.STOP, token.getLocation());
      }

      return new TerminalNode(Constant.ERROR, token.getLocation());
    }

    throw constructException("expecting to parse a terminal but received \"" + token.toString() + "\"", token.getLocation());
  }

  private String parseIndices() throws CompilationException, InterruptedException {
    StringBuilder builder = new StringBuilder();

    Token token = peekToken();
    if (!(token instanceof OpenBracketToken)) {
      throw constructException("expecting to parse \"[\" but received \"" + token.toString() + "\"");
    }

    while (token instanceof OpenBracketToken) {
      // gobble the open bracket
      nextToken();

      String expression = parseExpression();

      token = nextToken();
      if (!(token instanceof CloseBracketToken)) {
        throw constructException("expecting to parse \"]\" but received \"" + token.toString() + "\"", token.getLocation());
      }

      builder.append("[").append(expression).append("]");

      // setup the token for the loop condition
      token = peekToken();
    }

    return builder.toString();
  }

  private RangesNode parseRanges() throws CompilationException, InterruptedException {
    int start = index;

    if (!(peekToken() instanceof OpenBracketToken)) {
      throw constructException("expecting to parse \"[\" but received \"" + peekToken().toString() + "\"");
    }

    int rangeStart = actionRanges.size();

    while (peekToken() instanceof OpenBracketToken) {
      // gobble the open bracket
      nextToken();
      parseActionRange();

      if (!(nextToken() instanceof CloseBracketToken)) {
        Token error = tokens.get(index - 1);
        throw constructException("expecting to parse \"]\" but received \"" + error.toString() + "\"", error.getLocation());
      }
    }

    List<IndexExpNode> ranges = new ArrayList<>(actionRanges.subList(rangeStart, actionRanges.size()));
    actionRanges = new ArrayList<>(actionRanges.subList(0, rangeStart));

    return new RangesNode(ranges, constructLocation(start));
  }
/*

 */
  private String parseProcessLabel() throws CompilationException {
    StringBuilder builder = new StringBuilder();

    while (true) {
      Token token = nextToken();
      if (token instanceof ActionToken) {
        builder.append(((ActionToken) token).getAction());
      } else if (token instanceof OpenBracketToken) {
        token = nextToken();
        if (token instanceof ActionToken) {
          builder.append("[$");
          builder.append(((ActionToken) token).getAction());
          builder.append("]");
        } else if (token instanceof IntegerToken) {
          builder.append(((IntegerToken) token).getInteger());
        } else {
          throw constructException("expecting to parse an integer or variable but received \"" + token.toString() + "\"", token.getLocation());
        }

        if (!(nextToken() instanceof CloseBracketToken)) {
          Token error = tokens.get(index - 1);
          throw constructException("expecting to parse \"]\" but received \"" + error.toString() + "\"", error.getLocation());
        }
      } else {
        throw constructException("expecting to parse a process label but received \"" + token.toString() + "\"", token.getLocation());
      }

      if (peekToken() instanceof DotToken) {
        nextToken();
        builder.append(".");
      } else if (!(peekToken() instanceof OpenBracketToken)) {
        break;
      }

    }

    if (!(nextToken() instanceof ColonToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \":\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    return builder.toString();
  }

  private String parseLabel() throws CompilationException {
    // ensure that the next token is an action token
    if (!(peekToken() instanceof ActionToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse a variable but received \"" + error.toString() + "\"", error.getLocation());
    }

    String label = ((ActionToken) nextToken()).getAction();

    // ensure that the next token is a ':' token
    if (!(nextToken() instanceof ColonToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \":\" but received \"" + error.toString() + "\"", error.getLocation());
    }
    return label;
  }

  private RelabelNode parseRelabel() throws CompilationException, InterruptedException {
    int start = index;
    if (!(nextToken() instanceof DivisionToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"/\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    if (!(nextToken() instanceof OpenBraceToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"{\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    List<RelabelElementNode> relabels = new ArrayList<>();
    int rangeStart = actionRanges.size();

    while (!(peekToken() instanceof CloseBraceToken)) {
      RelabelElementNode element = parseRelabelElement();
     //System.out.println("element = "+element);
      if (rangeStart < actionRanges.size()) {
        List<IndexExpNode> ranges = new ArrayList<>(actionRanges.subList(rangeStart, actionRanges.size()));
        actionRanges = new ArrayList<>(actionRanges.subList(0, rangeStart));

        element.setRanges(new RangesNode(ranges, element.getLocation()));
      }

      relabels.add(element);
     //System.out.println("element = "+element);
      if (peekToken() instanceof CommaToken) {
        nextToken();
      }
    }

    // ensure that the next token is the '}' token
    if (!(nextToken() instanceof CloseBraceToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"}\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    return new RelabelNode(relabels, constructLocation(start));
  }

  private RelabelElementNode parseRelabelElement() throws CompilationException, InterruptedException {

   //System.out.println("Parsing relabeling ");
    int start = index;
    RelabelElementNode rel;
    if (peekToken() instanceof ActionToken) {

     //System.out.println("Action");
      ActionLabelNode newLabel = parseActionLabel();
      if (!(nextToken() instanceof DivisionToken)) {
        Token error = tokens.get(index - 1);
        throw constructException("expecting to parse \"/\" but received \"" +error.toString()+"\"", error.getLocation());
      }
      ActionLabelNode oldLabel = parseActionLabel();
      rel = new RelabelElementNode(newLabel.getAction(),
        oldLabel.getAction(), constructLocation(start));
    } else if (peekToken() instanceof IdentifierToken) {

     //System.out.println("Identifier");
      IdentifierNode identifier = parseIdentifier();
      if (!(nextToken() instanceof DivisionToken)) {
        Token error = tokens.get(index - 1);
        throw constructException("expecting to parse \"/\" but received \"" +
          error.toString() + "\"", error.getLocation());
      }

      ActionLabelNode oldLabel = parseActionLabel();
      rel = new RelabelElementNode(identifier,
        oldLabel.getAction(), constructLocation(start));
     //System.out.println("parseRelabel "+identifier.getIdentifier()+" and "+ oldLabel.getAction());
    } else {
      throw constructException("expecting to event or Process not " +nextToken().toString());
    }

   //System.out.println("parseRelabelElement() end "+rel.getOldLabel()+"->"+rel.getNewProcess());
    return rel;
  }

  private HidingNode parseHiding() throws CompilationException, InterruptedException {
    int start = index;
    if (!(peekToken() instanceof HideToken) && !(peekToken() instanceof AtToken)) {
      throw constructException("expecting to parse \"\\\" or \"@\" but received \"" + peekToken().toString() + "\"");
    }

    // assume that type is inclusive unless specified otherwise
    String type = "includes";
    if (nextToken() instanceof AtToken) {
      type = "excludes";
    }
    SetNode set;
    //TODO: make already given sets work
//    if(peekToken() instanceof IdentifierToken) {
//      set = nextToken();
//    } else {
    set = parseSet();
//    }

    return new HidingNode(type, set, constructLocation(start));
  }
  // OPERATIONS

  private VariableSetNode parseVariables() throws CompilationException {
    int start = index;
    if (!(nextToken() instanceof DollarToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"$\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    if (!(nextToken() instanceof OpenBraceToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"{\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    Set<String> variables = new HashSet<>();

    while (!(peekToken() instanceof CloseBraceToken)) {
      if (!(peekToken() instanceof ActionToken)) {
        throw constructException("expecting to parse a variable but received \"" + peekToken().toString() + "\"");
      }

      variables.add(((ActionToken) nextToken()).getAction());

      if (peekToken() instanceof CommaToken) {
        nextToken();
      }
    }

    if (!(nextToken() instanceof CloseBraceToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"}\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    return new VariableSetNode(variables, constructLocation(start));
  }

  private InterruptNode parseInterrupt() throws CompilationException, InterruptedException {
    int start = index;
    if (!(nextToken() instanceof InterruptToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"~>\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    ActionLabelNode action = parseActionLabel();

    if (!(nextToken() instanceof InterruptToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"~>\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    ASTNode process = parseComposite();

    return new InterruptNode(action, process, constructLocation(start));
  }

  private void parseEquation() throws CompilationException, InterruptedException {
    if (!(nextToken() instanceof EquationToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"equation\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    if (!(peekToken() instanceof OpenBraceToken)) {
      parseSingleOperation(true);
    } else {
      parseOperationBlock(true);
    }
  }

  private void parseAlphabet() throws CompilationException, InterruptedException {
    if (!(nextToken() instanceof AlphabetToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"alphabet\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    if (!(peekToken() instanceof OpenBraceToken)) {
      alphabet.add( parseActionLabel()) ;
    } else {
      parseAlphabetBlock(false);  // "{ ...  }
    }
  }
  private void parseAlphabetBlock(boolean isEq) throws CompilationException, InterruptedException {
    System.out.println("parseAlphabetBlock "+ peekToken().toString());

    if (!(nextToken() instanceof OpenBraceToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"{\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    while (!(peekToken() instanceof CloseBraceToken)) {
      alphabet.add(parseActionLabel());
      if ((peekToken() instanceof CommaToken)) nextToken();
      else   break;
    }

    if (!(nextToken() instanceof CloseBraceToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"}\" but received \"" + error.toString() + "\"", error.getLocation());
    }
  }
  /**
   * Parsing  "operations" { A ~ B;}
   * @throws CompilationException
   * @throws InterruptedException
   */
  private void parseOperation() throws CompilationException, InterruptedException {
    if (!(nextToken() instanceof OperationToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"operation\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    if (!(peekToken() instanceof OpenBraceToken)) {
      parseSingleOperation(false);
    } else {
      parseOperationBlock(false);  // "{ ...  }
    }
  }


  // parse "A ~ B"  AST add OperationNode to "operations" as side effect
  // "Aut(A)" stores the type automaton on Operation node
  // isEq diferentiates operations from equations

  private void parseSingleOperation(boolean isEq) throws CompilationException, InterruptedException {
    System.out.println("parseSingleOperation "+ peekToken().toString());

    int start = index;
    OperationNode firstOperation = parseSOperation(isEq);

    OperationNode operation;
    if ((peekToken() instanceof ImpliesToken)) {
      System.out.println("implies"+nextToken().toString());
      OperationNode secondOperation = parseSOperation(isEq);
      operation = new ImpliesNode(firstOperation, secondOperation,this.constructLocation(start));
    } else {
      operation = firstOperation;
    }
// ensure that the next token is a '.' token
    if (!(nextToken() instanceof DotToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \".\" but received \"" + error.toString() + "\"", error.getLocation());
    }
    if (isEq) {
      equations.add(operation);
    } else {
      operations.add(operation);
    }
  }

  // parse "A ~ B"  AST returns an  OperationNode
  // isEq diferentiates operations from equations

  private OperationNode parseSOperation(boolean isEq) throws CompilationException, InterruptedException {
    System.out.println("parseSOperation "+ peekToken().toString());

    int start = index;
    boolean firstAut = false; boolean secondAut = false;
    if (peekToken() instanceof AutomatonToken) {
      firstAut = true;
      nextToken();
    }
    ASTNode process1 = parseComposite();

    boolean isNegated = false;
    if (peekToken() instanceof NegateToken) {
      nextToken();
      isNegated = true;
    }
    String type = parseOperationType();  // nextToken.toString()
    if (peekToken() instanceof AutomatonToken) {
      secondAut = true;
      nextToken();
    }
    ASTNode process2 = parseComposite();

    OperationNode firstOperation = null;



    OperationNode operation = new OperationNode(type, isNegated, process1, process2, this.constructLocation(start));
    if(firstAut)  operation.setFirstProcessType("automata");
    if(secondAut) operation.setSecondProcessType("automata");

    return operation;
  }

  private void parseOperationBlock(boolean isEq) throws CompilationException, InterruptedException {
    System.out.println("parseOperationBlock "+ peekToken().toString());

    if (!(nextToken() instanceof OpenBraceToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"{\" but received \"" + error.toString() + "\"", error.getLocation());
    }

    while (!(peekToken() instanceof CloseBraceToken)) {
      parseSingleOperation(isEq);
    }

    if (!(nextToken() instanceof CloseBraceToken)) {
      Token error = tokens.get(index - 1);
      throw constructException("expecting to parse \"}\" but received \"" + error.toString() + "\"", error.getLocation());
    }
  }

  private String parseOperationType() throws CompilationException {
    return nextToken().toString();
    //    throw constructException("expecting to parse an operation type but received \"" + peekToken().toString() + "\"");
  }

  private String parseExpression() throws CompilationException, InterruptedException {
    List<String> exprTokens = new ArrayList<>();
    int start = index;
    parseExpression(exprTokens);

    Expr expression = Expression.constructExpression(String.join(" ", exprTokens), constructLocation(start), context);

    if (expressionEvaluator.isExecutable(expression)) {
      Expr simp = expression.simplify();
      if (simp.isTrue()) {
        return "true";
      }
      if (simp.isFalse()) {
        return "false";
      }
      if (simp instanceof BitVecNum) {
        return ExpressionEvaluator.evaluate((BitVecNum) simp, context) + "";
      }
    } else if (expression.isConst()) {

      return expression.toString();
    }

    String variable = nextVariableId();

    variableMap.put(variable, expression);
    return variable;
  }

  private void parseExpression(List<String> expression) throws CompilationException {
    parseBaseExpression(expression);
    while (hasExpressionToken()) {
      parseOperator(expression);
      parseBaseExpression(expression);
    }
  }

  private void parseBaseExpression(List<String> expression) throws CompilationException {
    Token token = nextToken();

    // check if a unary operation can be parsed
    if (token instanceof OperatorToken) {
      if (token instanceof AdditionToken) {
        expression.add("+");
        token = nextToken();
      } else if (token instanceof SubtractionToken) {
        expression.add("-");
        token = nextToken();
      } else if (token instanceof NegateToken) {
        expression.add("!");
        token = nextToken();
      } else {
        throw constructException("expecting to parse an unary operator but received the operator \"" + token.toString() + "\"", token.getLocation());
      }
    }

    // check if either a integer, variable, constant or parenthesised expression can be parsed
    if (token instanceof IntegerToken) {
      int integer = ((IntegerToken) token).getInteger();
      expression.add("" + integer);
    } else if (token instanceof ActionToken) {
      String variable = "$" + ((ActionToken) token).getAction();
      expression.add(variable);
    } else if (token instanceof IdentifierToken) {
      String identifier = ((IdentifierToken) token).getIdentifier();

      // check that a constant has been defined with the specified identifier
      if (!constantMap.containsKey(identifier)) {
        Token error = tokens.get(index - 1);
        throw constructException("the identifier \"" + identifier + "\" has not been defined", error.getLocation());
      }

      // check that the constant referenced is a const (integer value)
      ASTNode constant = constantMap.get(identifier);
      if (!(constant instanceof ConstParseOnlyNode)) {
        String type = (constant instanceof RangeNode) ? "range" : "set";
        Token error = tokens.get(index - 1);
        throw constructException("expecting a const but received a " + type, error.getLocation());

      }

      int value = ((ConstParseOnlyNode) constant).getValue();
      expression.add("" + value);
    } else if (token instanceof OpenParenToken) {
      expression.add("(");
      parseExpression(expression);

      token = nextToken();
      if (!(token instanceof CloseParenToken)) {
        throw constructException("expecting to parse \")\" but received \"" + token.toString() + "\"", token.getLocation());
      }

      expression.add(")");
    }
  }

  private void parseOperator(List<String> expression) throws CompilationException {
    Token token = nextToken();

    // check that the operation is an operator
    if (!(token instanceof OperatorToken)) {
      throw constructException("expecting to parse an operator but received \"" + token.toString() + "\"", token.getLocation());
    }

    if (token instanceof OrToken) {
      expression.add("||");
    } else if (token instanceof AndToken) {
      expression.add("&&");
    } else if (token instanceof BitOrToken) {
      expression.add("|");
    } else if (token instanceof ExclOrToken) {
      expression.add("^");
    } else if (token instanceof BitAndToken) {
      expression.add("&");
    } else if (token instanceof EqualityToken) {
      expression.add("==");
    } else if (token instanceof NotEqualToken) {
      expression.add("!=");
    } else if (token instanceof LessThanToken) {
      expression.add("<");
    } else if (token instanceof LessThanEqToken) {
      expression.add("<=");
    } else if (token instanceof GreaterThanToken) {
      expression.add(">");
    } else if (token instanceof GreaterThanEqToken) {
      expression.add(">=");
    } else if (token instanceof LeftShiftToken) {
      expression.add("<<");
    } else if (token instanceof RightShiftToken) {
      expression.add(">>");
    } else if (token instanceof AdditionToken) {
      expression.add("+");
    } else if (token instanceof SubtractionToken) {
      expression.add("-");
    } else if (token instanceof MultiplicationToken) {
      expression.add("*");
    } else if (token instanceof DivisionToken) {
      expression.add("/");
    } else if (token instanceof ModuloToken) {
      expression.add("%");
    } else {
      throw constructException("received an incorrect operator \"" + token.toString() + "\"", token.getLocation());
    }
  }

  private int parseSimpleExpression() throws CompilationException, InterruptedException {
    List<String> exprTokens = new ArrayList<>();
    int start = index;
    parseSimpleExpression(exprTokens);

    Expr expression = Expression.constructExpression(String.join(" ", exprTokens), constructLocation(start), context);
    return expressionEvaluator.evaluateExpression(expression, new HashMap<>(), context);
  }

  private void parseSimpleExpression(List<String> expression) throws CompilationException {
    parseBaseSimpleExpression(expression);
    while (hasExpressionToken()) {
      parseSimpleOperator(expression);
      parseBaseSimpleExpression(expression);
    }
  }

  private void parseBaseSimpleExpression(List<String> expression) throws CompilationException {
    Token token = nextToken();

    // check if a unary operation can be parsed
    if (token instanceof OperatorToken) {
      if (token instanceof AdditionToken) {
        expression.add("+");
        token = nextToken();
      } else if (token instanceof SubtractionToken) {
        expression.add("-");
        token = nextToken();
      } else if (token instanceof NegateToken) {
        expression.add("!");
        token = nextToken();
      } else {
        throw constructException("expecting to parse an unary operator but received the operator \"" + token.toString() + "\"", token.getLocation());
      }
    }

    // check if either a integer, constant or parenthesised expression can be parsed
    if (token instanceof IntegerToken) {
      int integer = ((IntegerToken) token).getInteger();
      expression.add("" + integer);
    } else if (token instanceof IdentifierToken) {
      String identifier = ((IdentifierToken) token).getIdentifier();

      // check that a constant has been defined with the specified identifier
      if (!constantMap.containsKey(identifier)) {
        Token error = tokens.get(index - 1);
        throw constructException("the identifier \"" + identifier + "\" has not been defined", error.getLocation());
      }

      // check that the constant referenced is a const (integer value)
      ASTNode constant = constantMap.get(identifier);
      if (!(constant instanceof ConstParseOnlyNode)) {
        String type = (constant instanceof RangeNode) ? "range" : "set";
        Token error = tokens.get(index - 1);
        throw constructException("expecting a const but received a " + type, error.getLocation());
      }

      int value = ((ConstParseOnlyNode) constant).getValue();
      expression.add("" + value);
    } else if (token instanceof OpenParenToken) {
      expression.add("(");
      parseExpression(expression);

      token = nextToken();
      if (!(token instanceof CloseParenToken)) {
        throw constructException("expecting to parse \")\" but received \"" + token.toString() + "\"", token.getLocation());
      }

      expression.add(")");
    }
  }

  private void parseSimpleOperator(List<String> expression) throws CompilationException {
    Token token = nextToken();

    // check that the token is an operator
    if (!(token instanceof OperatorToken)) {
      throw constructException("expecting to parse an operator but received \"" + token.toString() + "\"", token.getLocation());
    }

    if (token instanceof AdditionToken) {
      expression.add("+");
    } else if (token instanceof SubtractionToken) {
      expression.add("-");
    } else if (token instanceof MultiplicationToken) {
      expression.add("*");
    } else if (token instanceof DivisionToken) {
      expression.add("/");
    } else if (token instanceof ModuloToken) {
      expression.add("%");
    } else {
      throw constructException("received an incorrect operator \"" + token.toString() + "\"", token.getLocation());
    }
  }

  private boolean hasExpressionToken() throws CompilationException {
    Token token = peekToken();
    if (token instanceof OperatorToken) {
      return true;
    } else if (token instanceof IntegerToken) {
      return true;
    } else if (token instanceof IdentifierToken) {
      return true;
    } else if (token instanceof ActionToken) {
      String action = ((ActionToken) token).getAction();
      return definedVariables.contains(action);
    } else if (token instanceof OpenParenToken) {
      return true;
    }

    return false;
  }

  private String parseActionRangeOrExpression() throws CompilationException, InterruptedException {
    int currentIndex = index;
    int currentVarId = variableId;

    // parse an expression
    String variable = parseExpression();

    // check if the expression is part of an action range
    if (peekToken() instanceof RangeSeparatorToken) {
      // reset index and parse the action range
      index = currentIndex;
      variableId = currentVarId;
      variable = parseActionRange();
    }

    return variable;
  }

  private String nextVariableId() {
    return "$v" + variableId++;
  }

  /**
   * Helper method that retrieves the next @code{Token} and moves the index position
   * to point to the next @code{Token}.
   *
   * @return -- the next @Code{Token}
   */
  private Token nextToken() throws CompilationException {
    checkNotEOF();
    return tokens.get(index++);
  }

  /**
   * Helper method that retrieves the next @code{Token} without incrementing the index position.
   *
   * @return -- the next @code{Token}
   */
  private Token peekToken() throws CompilationException {
    checkNotEOF();
    return tokens.get(index);
  }

  private boolean hasProcessLabel() throws CompilationException {
    int start = index;

    while (true) {
      Token token = nextToken();
      if (token instanceof OpenBracketToken) {
        token = nextToken();
        if (!(token instanceof ActionToken) && !(token instanceof IntegerToken)) {
          index = start;
          return false;
        }

        if (!(nextToken() instanceof CloseBracketToken)) {
          index = start;
          return false;
        }
      } else if (!(token instanceof ActionToken)) {
        index = start;
        return false;
      }

      if (peekToken() instanceof DotToken) {
        nextToken();
      } else if (!(peekToken() instanceof OpenBracketToken)) {
        break;
      }

    }

    if (!(nextToken() instanceof ColonToken)) {
      index = start;
      return false;
    }

    index = start;
    return true;
  }

  private boolean hasLabel() throws CompilationException {
    return peekToken() instanceof ActionToken && index < tokens.size() - 1 && (tokens.get(index + 1) instanceof ColonToken);
  }

  private boolean hasVariableReference() throws CompilationException {
    int start = index;
    if (nextToken() instanceof ActionToken) {
      if (nextToken() instanceof CloseBracketToken) {
        index = start;
        return true;
      }
    }

    index = start;
    return false;
  }

  private boolean hasIdentifierReference() throws CompilationException {
    int start = index;
    if (nextToken() instanceof IdentifierToken) {
      if (nextToken() instanceof CloseBracketToken) {
        index = start;
        return true;
      }
    }

    index = start;
    return false;
  }

  private Location constructLocation(int start) {
    Location locStart = tokens.get(start).getLocation();
    Location locEnd = tokens.get(index - 1).getLocation();
    return new Location(locStart, locEnd);
  }

  private void checkNotEOF() throws CompilationException {
    if (index >= tokens.size()) {
      Location last = tokens.get(tokens.size() - 1).getLocation();
      Location eof = new Location(last.getLineStart(), last.getColStart() + 1, last.getLineEnd(), last.getColEnd() + 2, last.getStartIndex() + 1, last.getEndIndex() + 2);
      throw constructException("end of file reached", eof);

    }
  }

  private void reset() {
    processes.clear();
    processIdentifiers.clear();
    operations.clear();
    variableMap.clear();
    constantMap.clear();
    actionRanges.clear();
    definedVariables.clear();
    index = 0;
    variableId = 0;
  }

  private CompilationException constructException(String message) {
    Location location = tokens.get(index).getLocation();
    return constructException(message, location);
  }
}
