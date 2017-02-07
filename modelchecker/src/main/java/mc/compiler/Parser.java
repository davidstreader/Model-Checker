package mc.compiler;

import mc.Constant;
import mc.compiler.ast.*;
import mc.compiler.token.*;
import mc.exceptions.CompilationException;
import mc.util.Location;
import mc.util.expr.AdditionOperator;
import mc.util.expr.AndOperator;
import mc.util.expr.BitAndOperator;
import mc.util.expr.BitNotOperator;
import mc.util.expr.BitOrOperator;
import mc.util.expr.DivisionOperator;
import mc.util.expr.EqualityOperator;
import mc.util.expr.ExclOrOperator;
import mc.util.expr.Expression;
import mc.util.expr.ExpressionEvaluator;
import mc.util.expr.GreaterThanEqOperator;
import mc.util.expr.GreaterThanOperator;
import mc.util.expr.IntegerOperand;
import mc.util.expr.LeftShiftOperator;
import mc.util.expr.LessThanEqOperator;
import mc.util.expr.LessThanOperator;
import mc.util.expr.ModuloOperator;
import mc.util.expr.MultiplicationOperator;
import mc.util.expr.NegativeOperator;
import mc.util.expr.NotEqualOperator;
import mc.util.expr.NotOperator;
import mc.util.expr.OrOperator;
import mc.util.expr.PositiveOperator;
import mc.util.expr.RightShiftOperator;
import mc.util.expr.SubtractionOperator;
import mc.util.expr.VariableOperand;

import java.util.*;

/**
 * Created by sheriddavi on 30/01/17.
 */
public class Parser {

    private List<Token> tokens;
    private List<ProcessNode> processes;
    private List<OperationNode> operations;
    private Map<String, Expression> variableMap;
    private Map<String, ASTNode> constantMap;
    private List<IndexNode> actionRanges;
    private int index;
    private int variableId;

    private ExpressionParser expressionParser;
    private ExpressionEvaluator expressionEvaluator;

    public Parser(){
        processes = new ArrayList<ProcessNode>();
        operations = new ArrayList<OperationNode>();
        variableMap = new HashMap<String, Expression>();
        constantMap = new HashMap<String, ASTNode>();
        actionRanges = new ArrayList<IndexNode>();
        index = 0;
        variableId = 0;
        expressionParser = new ExpressionParser();
        expressionEvaluator = new ExpressionEvaluator();
    }

    public AbstractSyntaxTree parse(List<Token> tokens) throws CompilationException {
        reset();
        this.tokens = tokens;

        while(index < this.tokens.size()){
            Token token = peekToken();
            if(token instanceof ProcessTypeToken){
                parseProcessDefinition();
            }
            else if(token instanceof ConstToken){
                parseConstDefinition();
            }
            else if(token instanceof RangeToken){
                parseRangeDefinition();
            }
            else if(token instanceof SetToken){
                parseSetDefinition();
            }
        }

        return new AbstractSyntaxTree(processes, operations, variableMap);
    }

    /**
     * Attempts to parse and return an @code{IdentifierNode} the current position
     * in the @code{List} of {Tokens}.
     *
     * @return
     *      -- an @code{IdentifierNode}
     */
    private IdentifierNode parseIdentifier(){
        Token token = nextToken();

        if(token instanceof IdentifierToken){
            IdentifierToken identifier = (IdentifierToken)token;
            return new IdentifierNode(identifier.getIdentifier(), identifier.getLocation());
        }

        // TODO: throw error
        return null;
    }

    // ACTION LABELS

    /**
     * Attempts to parse an @code{ActionLabelNode} from the current position in the @code{List} of @code{Tokens}.
     * An @code{ActionLabelNode} is of the form:
     *
     * <pre>
     *     ActionLabel := (Label | '[' (Expression | ActionRange) ']') [['.'] ActionLabel]
     * </pre>
     *
     * @return
     *      -- the parsed @code{ActionLabelNode}
     */
    private ActionLabelNode parseActionLabel() throws CompilationException {
        int start = index;
        StringBuilder builder = new StringBuilder();

        while(true){
            Token token = nextToken();

            if(token instanceof ActionToken){
                builder.append(((ActionToken)token).getAction());
            }
            else if(token instanceof OpenBracketToken){
                String variable;
                if(hasLabel() || peekToken() instanceof OpenBraceToken){
                    variable = parseActionRange();
                }
                else if(peekToken() instanceof IdentifierToken){
                    String identifier = ((IdentifierToken)peekToken()).getIdentifier();
                    if(!constantMap.containsKey(identifier)){
                        // TODO: throw errror
                    }

                    ASTNode constant = constantMap.get(identifier);
                    if(constant instanceof ConstNode){
                        variable = parseActionRangeOrExpression();
                    }
                    else{
                        variable = parseActionRange();
                    }
                }
                else if(peekToken() instanceof ActionToken){
                    variable = "[" + ((ActionToken)token).getAction() + "]";

                    if(!(nextToken() instanceof CloseBracketToken)){
                        // TODO: throw error
                    }


                }
                else {
                    variable = parseActionRangeOrExpression();
                }

                if(!(nextToken() instanceof CloseBracketToken)){
                    // TODO: throw error
                }

                builder.append("[" + variable + "]");
            }
            else{
                // TODO: throw error
            }

            if(peekToken() instanceof DotToken){
                // gobble the dot token and add it to the action label
                nextToken();
                builder.append(".");
            }
            else if(!(peekToken() instanceof OpenBracketToken) && !(peekToken() instanceof ActionToken)){
                break;
            }
        }

        return new ActionLabelNode(builder.toString(), constructLocation(start));
    }

    /**
     * Attempts to parse an @code{ActionRange} from the current position in the @code{List} of @code{Tokens}.
     * Returns a @code{String} that represents the variable that can contain the values specified in the parsed @code{ActionRange}.
     * An @code{ActionLabelNode} is of the form:
     *
     * <pre>
     *     ActionRange := [Label ':'] (Identifier | Range | Set)
     * </pre>
     *
     * @return
     *      -- @code{String} variable that is used by the parsed @code{ActionRange}
     */
    private String parseActionRange() throws CompilationException {
        int start = index;
        String variable = null;
        if(hasLabel()){
            variable = "$" + parseLabel();
        }
        else{
            // a variable has not been defined, give the variable a unique internally defined name
            variable = nextVariableId();
        }

        ASTNode range = null;
        if(peekToken() instanceof IdentifierToken && !(tokens.get(index + 1) instanceof RangeSeparatorToken)){
            IdentifierNode identifier = parseIdentifier();

            if(!constantMap.containsKey(identifier.getIdentifier())){
                // TODO: throw error
            }

            ASTNode constant = constantMap.get(identifier.getIdentifier());

            if(constant instanceof ConstNode){
                // TODO: throw error
            }

            range = constant;
        }
        else if(peekToken() instanceof OpenBraceToken){
            range = parseSet();
        }
        else{
            range = parseRange();
        }

        IndexNode indexNode = new IndexNode(variable, range, null, constructLocation(start));
        actionRanges.add(indexNode);

        return variable;
    }

    /**
     * Attempts to parse a @code{RangeNode} from the current position in the @code{List} of @code{Tokens}.
     * A @code{RangeNode} is of the form:
     *
     * <pre>
     *     Range := Expression '..' Expression
     * </pre>
     *
     * @return
     *      -- the parsed @code{RangeNode}
     */
    private RangeNode parseRange() throws CompilationException {
        int start = index;

        // parse the next expression and evaluate it if necessary to get the start value of the range
        String expression = parseExpression();
        int startValue;
        if(variableMap.containsKey(expression)){
            // in this case, 'expression' is an internal variable reference to an expression
            startValue = expressionEvaluator.evaluateExpression(variableMap.get(expression), new HashMap<String, Integer>());
        }
        else{
            // otherwise the expression is an integer that we can parse
            startValue = Integer.parseInt(expression);
        }

        // ensure that the next token is the '..' token
        if(!(nextToken() instanceof RangeSeparatorToken)){
            // TODO: throw error
        }

        // parse the next expression and evaluate it if necessary to get the start value of the range
        expression = parseExpression();
        int endValue;
        if(variableMap.containsKey(expression)){
            // in this case, 'expression' is an internal variable reference to an expression
            endValue = expressionEvaluator.evaluateExpression(variableMap.get(expression), new HashMap<String, Integer>());
        }
        else{
            // otherwise the expression is an integer that we can parse
            endValue = Integer.parseInt(expression);
        }

        return new RangeNode(startValue, endValue, constructLocation(start));
    }

    /**
     * Attempts to parse a @code{SetNode} from the current position in the @code{List} of @code{Tokens}.
     * A @code{SetNode} is of the form:
     *
     * <pre>
     *     Set := '{' ActionLabel (',' ActionLabel)* '}'
     * </pre>
     *
     * @return
     *      -- the parsed @code{SetNode}
     */
    private SetNode parseSet() throws CompilationException {
        int start = index;
        // ensure the next token is the '{' token
        if(!(nextToken() instanceof OpenBraceToken)){
            // TODO: throw error
        }

        List<String> set = new ArrayList<String>();

        while(!(peekToken() instanceof CloseBracketToken)){
            // parse the current action and add it to the set
            set.add(parseActionLabel().getAction());

            // check if another action label can be parsed
            if(!(peekToken() instanceof CommaToken)){
                break;
            }

            // gobble the comma token
            nextToken();
        }

        // ensure the next token is the '}' token
        if(!(nextToken() instanceof CloseBraceToken)){
            // TODO: throw error
        }

        return new SetNode(set, constructLocation(start));
    }

    // CONSTANT DEFINITIONS

    private void parseConstDefinition() throws CompilationException {
        // ensure that the next token is the 'const' token
        if(!(nextToken() instanceof ConstToken)){
            // TODO: throw error
        }

        IdentifierNode identifier = parseIdentifier();

        // ensure that the next token is the '=' token
        if(!(nextToken() instanceof AssignToken)){
            // TODO: throw error
        }

        int start = index;
        int value = parseSimpleExpression();

        ConstNode node = new ConstNode(value, constructLocation(start));
        constantMap.put(identifier.getIdentifier(), node);
    }

    private void parseRangeDefinition() throws CompilationException {
        // ensure the next token is the 'range' token
        if(!(nextToken() instanceof RangeToken)){
            // TODO: throw error
        }

        IdentifierNode identifier = parseIdentifier();

        // ensure the next token is the '=' token
        if(!(nextToken() instanceof AssignToken)){
            // TODO: throw error
        }

        int start = index;
        int startValue = parseSimpleExpression();

        // ensure the next token is the '..' token
        if(!(nextToken() instanceof RangeSeparatorToken)){
            // TODO: throw error
        }

        int endValue = parseSimpleExpression();

        RangeNode range = new RangeNode(startValue, endValue, constructLocation(start));
        constantMap.put(identifier.getIdentifier(), range);
    }

    private void parseSetDefinition() throws CompilationException {
        // ensure that the next token is the 'set' token
        if(!(nextToken() instanceof SetToken)){
            // TODO: throw error
        }

        IdentifierNode identifier = parseIdentifier();

        // ensure that the next token is the '=' token
        if(!(nextToken() instanceof AssignToken)){
            // TODO: throw error
        }

        SetNode set = parseSet();
        constantMap.put(identifier.getIdentifier(), set);
    }

    // PROCESS DEFINITIONS

    private void parseProcessDefinition() throws CompilationException {
        String processType = parseProcessType();

        Token token = peekToken();
        if(token instanceof IdentifierToken){
            parseSingleProcessDefinition(processType);
        }
        else if(token instanceof OpenBraceToken){
            parseProcessDefinitionBlock(processType);
        }
        else{
            // TODO: throw error
        }
    }

    private void parseSingleProcessDefinition(String processType) throws CompilationException {
        int start = index;
        IdentifierNode identifier = parseIdentifier();

        // ensure that the next token is the '=' token
        if(!(nextToken() instanceof AssignToken)){
            // TODO: throw error
        }

        ASTNode process = parseComposite();

        // TODO: parse local processes
        List<LocalProcessNode> localProcesses = new ArrayList<LocalProcessNode>();

        while(peekToken() instanceof CommaToken){
            nextToken(); // gobble the comma
            localProcesses.add(parseLocalProcessDefinition());
        }

        ProcessNode processNode = new ProcessNode(processType, identifier.getIdentifier(), process, localProcesses, constructLocation(start));

        // ensure that the next token is the '.' token
        if(!(nextToken() instanceof DotToken)){
            // TODO: throw error
        }

        processes.add(processNode);
    }

    private void parseProcessDefinitionBlock(String processType) throws CompilationException {
        // ensure that the next token is the '{' token
        if(!(nextToken() instanceof OpenBraceToken)){
            // TODO: throw error
        }

        while(!(peekToken() instanceof CloseBraceToken)){
            parseSingleProcessDefinition(processType);
        }

        // ensure that the next token is the '}' token
        if(!(nextToken() instanceof CloseBraceToken)){
            // TODO: throw error
        }
    }

    private LocalProcessNode parseLocalProcessDefinition() throws CompilationException {
        int start = index;
        IdentifierNode identifier = parseIdentifier();

        // check if ranges have been defined for this process
        RangesNode ranges = null;
        if(peekToken() instanceof OpenBracketToken){
            ranges = parseRanges();
        }

        if(!(nextToken() instanceof AssignToken)){
            // TODO: throw error
        }

        ASTNode process = parseComposite();

        return new LocalProcessNode(identifier.getIdentifier(), ranges, process, constructLocation(start));
    }

    private String parseProcessType(){
        Token token = nextToken();
        if(token instanceof ProcessTypeToken){
            return ((ProcessTypeToken)token).getProcessType();
        }

        // TODO: throw error
        return null;
    }

    private ASTNode parseComposite() throws CompilationException {
        int start = index;

        String label = null;
        if(hasProcessLabel()){
            label = parseProcessLabel();
        }

        ASTNode process = parseChoice();

        RelabelNode relabel = null;
        if(peekToken() instanceof DivisionToken){
            relabel = parseRelabel();
        }

        HidingNode hiding = null;
        if(peekToken() instanceof HideToken || peekToken() instanceof AtToken){
            hiding = parseHiding();
        }

        if(peekToken() instanceof OrToken){
            nextToken(); // gobble the '||' token
            ASTNode process2 = parseComposite();
            process = new CompositeNode(process, process2, constructLocation(start));
        }

        if(label != null || relabel != null || hiding != null){
            return new ProcessRootNode(process, label, relabel, hiding, process.getLocation());
        }

        return process;
    }

    private ASTNode parseChoice() throws CompilationException {
        int start = index;

        ASTNode process = parseLocalProcess();

        if(peekToken() instanceof BitOrToken){
            nextToken(); // gobble the '|' token
            ASTNode process2 = parseComposite();
            return new ChoiceNode(process, process2, constructLocation(start));
        }

        return process;
    }

    private ASTNode parseLocalProcess() throws CompilationException {
        if(peekToken() instanceof OpenParenToken){
            nextToken();
            ASTNode process = parseComposite();
            if(!(nextToken() instanceof CloseParenToken)){
                // TODO: throw error
            }

            return process;
        }
        else if(peekToken() instanceof ActionToken || peekToken() instanceof OpenBracketToken){
            return parseSequence();
        }

        return parseBaseLocalProcess();
    }

    private ASTNode parseSequence() throws CompilationException {
        int start = index;

        int rangeStart = actionRanges.size();
        ActionLabelNode from = parseActionLabel();

        List<IndexNode> ranges = new ArrayList<IndexNode>();
        if(rangeStart < actionRanges.size()){
            ranges.addAll(actionRanges.subList(rangeStart, actionRanges.size()));
            actionRanges = new ArrayList<IndexNode>(actionRanges.subList(0, rangeStart));
            Collections.reverse(ranges);
        }

        // ensure that the next token is a '->' token
        if(!(nextToken() instanceof SequenceToken)){
            // TODO: throw error
        }

        ASTNode to = parseLocalProcess();

        ASTNode node = new SequenceNode(from, to, constructLocation(start));
        for(int i = 0; i < ranges.size(); i++){
            ranges.get(i).setProcess(node);
            node = ranges.get(i);
        }

        return node;
    }

    private ASTNode parseBaseLocalProcess() throws CompilationException {
        if(peekToken() instanceof TerminalToken){
            return parseTerminal();
        }
        else if(peekToken() instanceof IdentifierToken){
            IdentifierNode identifier = parseIdentifier();
            if(peekToken() instanceof OpenBracketToken){
                identifier.setIdentifer(identifier.getIdentifier() + parseIndices());
            }

            return identifier;
        }
        else if(peekToken() instanceof FunctionToken){
            return parseFunction();
        }
        else if(peekToken() instanceof ProcessTypeToken){
            return parseCasting();
        }
        else if(peekToken() instanceof IfToken){
            return parseIfStatement();
        }
        else if(peekToken() instanceof WhenToken){
            return parseWhenStatement();
        }
        else if(peekToken() instanceof ForAllToken){
            return parseForAllStatement();
        }
        else if(peekToken() instanceof OpenParenToken){
            nextToken();
            ASTNode process = parseComposite();

            // ensure that the next token is a ')' token
            if(!(nextToken() instanceof CloseParenToken)){
                // TODO: throw error
            }

            return process;
        }

        // TODO: throw error
        return null;
    }

    private FunctionNode parseFunction() throws CompilationException {
        int start = index;
        String type = parseFunctionType();

        // ensure that the next token is a '(' token
        if(!(nextToken() instanceof OpenParenToken)){
            // TODO: throw error;
        }

        ASTNode process = parseComposite();

        // ensure that the next token is a ')' token
        if(!(nextToken() instanceof CloseParenToken)){
            // TODO: throw error
        }

        return new FunctionNode(type, process, constructLocation(start));
    }

    private String parseFunctionType(){
        Token token = nextToken();
        if(token instanceof FunctionToken){
            return ((FunctionToken)token).getFunction();
        }

        // TODO: throw error
        return null;
    }

    private FunctionNode parseCasting() throws CompilationException {
        int start = index;
        String cast = parseProcessType();

        // ensure that the next token is a '(' token
        if(!(nextToken() instanceof OpenParenToken)){
            // TODO: throw error
        }

        ASTNode process = parseComposite();

        // ensure the next token is a ')' token
        if(!(nextToken() instanceof CloseParenToken)){
            // TODO: throw error;
        }

        return new FunctionNode(cast, process, constructLocation(start));
    }

    private IfStatementNode parseIfStatement() throws CompilationException {
        int start = index;
        // ensure that the next token is a 'if' token
        if(!(nextToken() instanceof IfToken)){
            // TODO: throw error
        }

        Expression expression = variableMap.get(parseExpression());

        // ensure that the next token is a 'then' token
        if(!(nextToken() instanceof ThenToken)){
            // TODO: throw error
        }

        ASTNode trueBranch = parseComposite();

        // check if an else branch was defined
        if(peekToken() instanceof ElseToken){
            nextToken(); // gobble the 'then' token
            ASTNode falseBranch = parseComposite();

            return new IfStatementNode(expression, trueBranch, falseBranch, constructLocation(start));
        }

        return new IfStatementNode(expression, trueBranch, constructLocation(start));
    }

    private IfStatementNode parseWhenStatement() throws CompilationException {
        int start = index;
        // ensure that the next token is a 'when' token
        if(!(nextToken() instanceof WhenToken)){
            // TODO: throw error
        }

        Expression expression = variableMap.get(parseExpression());
        ASTNode trueBranch = parseComposite();
        return new IfStatementNode(expression, trueBranch, constructLocation(start));
    }

    private ForAllStatementNode parseForAllStatement() throws CompilationException {
        int start = index;
        // ensure that the next token is a 'forall' token
        if(!(nextToken() instanceof ForAllToken)){
            // TODO: throw error
        }

        RangesNode ranges = parseRanges();

        if(!(nextToken() instanceof OpenParenToken)){
            // TODO: throw error
        }

        ASTNode process = parseComposite();

        if(!(nextToken() instanceof CloseParenToken)){
            // TODO: throw error
        }

        return new ForAllStatementNode(ranges, process, constructLocation(start));
    }

    private ASTNode parseTerminal(){
        Token token = nextToken();
        if(token instanceof TerminalToken){
            if(token instanceof StopToken){
                return new TerminalNode("STOP", token.getLocation());
            }

            // only other form of terminal is error so we can assume that the token is an error token
            ActionLabelNode deadlock = new ActionLabelNode(Constant.DEADLOCK, token.getLocation());
            TerminalNode terminal = new TerminalNode("ERROR", token.getLocation());
            return new SequenceNode(deadlock, terminal, token.getLocation());
        }

        // TODO: throw error
        return null;
    }

    private String parseIndices() throws CompilationException {
        StringBuilder builder = new StringBuilder();

        Token token = peekToken();
        if(!(token instanceof OpenBracketToken)){
            // TODO: throw error
        }

        while(token instanceof OpenBracketToken){
            // gobble the open bracket
            nextToken();

            String expression = parseExpression();

            token = nextToken();
            if(!(token instanceof CloseBracketToken)){
                // TODO: throw error
            }

            builder.append("[" + expression + "]");

            // setup the token for the loop condition
            token = peekToken();
        }

        return builder.toString();
    }

    private RangesNode parseRanges() throws CompilationException {
        int start = index;

        if(!(peekToken() instanceof OpenBracketToken)){
            // TODO: throw error
        }

        int rangeStart = actionRanges.size();

        while(peekToken() instanceof OpenBracketToken){
            // gobble the open bracket
            nextToken();
            parseActionRange();

            if(!(nextToken() instanceof CloseBracketToken)){
                // TODO: throw error
            }
        }

        List<IndexNode> ranges = actionRanges.subList(rangeStart, actionRanges.size());
        actionRanges = actionRanges.subList(0, rangeStart);

        return new RangesNode(ranges, constructLocation(start));
    }

    // RELABELLING AND HIDING

    private String parseProcessLabel(){
        StringBuilder builder = new StringBuilder();

        while(true){
            Token token = nextToken();
            if(token instanceof ActionToken){
                builder.append(((ActionToken)token).getAction());
            }
            else if(token instanceof OpenBracketToken){
                token = nextToken();
                if(token instanceof ActionToken){
                    builder.append("[$");
                    builder.append(((ActionToken)token).getAction());
                    builder.append("]");
                }
                else if(token instanceof IntegerToken){
                    builder.append(((IntegerToken)token).getInteger());
                }
                else{
                    // TODO: throw error
                }

                if(!(nextToken() instanceof CloseBracketToken)){
                    // TODO: throw error
                }
            }
            else{
                // TODO: throw error
            }

            if(peekToken() instanceof DotToken){
                nextToken();
                builder.append(".");
            }
            else if(!(peekToken() instanceof OpenBracketToken)){
                break;
            }

        }

        if(!(nextToken() instanceof ColonToken)){
            // TODO: throw error
        }

        return builder.toString();
    }

    private String parseLabel(){
        // ensure that the next token is an action token
        if(!(peekToken() instanceof ActionToken)){
            // TODO: throw error
        }

        String label = ((ActionToken)nextToken()).getAction();

        // ensure that the next token is a ':' token
        if(!(nextToken() instanceof ColonToken)){
            // TODO: throw error
        }

        return label;
    }

    private RelabelNode parseRelabel() throws CompilationException {
        int start = index;
        if(!(peekToken() instanceof DivisionToken)){
            // TODO: throw error
        }

        if(!(nextToken() instanceof OpenBraceToken)){
            // TODO: throw error
        }

        List<RelabelElementNode> relabels = new ArrayList<RelabelElementNode>();
        int rangeStart = actionRanges.size();

        while(!(peekToken() instanceof CloseBraceToken)){
            RelabelElementNode element = parseRelabelElement();

            if(rangeStart < actionRanges.size()){
                List<IndexNode> ranges = actionRanges.subList(rangeStart, actionRanges.size());
                actionRanges = actionRanges.subList(0, rangeStart);

                element.setRanges(new RangesNode(ranges, element.getLocation()));
            }

            relabels.add(element);

            if(peekToken() instanceof CommaToken){
                nextToken();
            }
        }

        // ensure that the next token is the '}' token
        if(!(nextToken() instanceof CloseBraceToken)){
            // TODO: throw error
        }

        return new RelabelNode(relabels, constructLocation(start));
    }

    private RelabelElementNode parseRelabelElement() throws CompilationException {
        int start = index;
        ActionLabelNode newLabel = parseActionLabel();

        if(!(nextToken() instanceof DivisionToken)){
            // TODO: throw error
        }

        ActionLabelNode oldLabel = parseActionLabel();

        return new RelabelElementNode(newLabel.getAction(), oldLabel.getAction(), constructLocation(start));
    }

    private HidingNode parseHiding() throws CompilationException {
        int start = index;
        if(!(peekToken() instanceof HideToken) || !(peekToken() instanceof AtToken)){
            // TODO: throw error
        }

        // assume that type is inclusive unless specified otherwise
        String type = "includes";
        if(nextToken() instanceof AtToken){
            type = "excludes";
        }

        SetNode set = parseSet();

        return new HidingNode(type, set.getSet(), constructLocation(start));
    }

    private VariableSetNode parseVariables(){
        int start = index;
        if(!(nextToken() instanceof DollarToken)){
            // TODO: throw error
        }

        if(!(nextToken() instanceof OpenBraceToken)){
            // TODO: throw error
        }

        Set<String> variables = new HashSet<String>();

        while(!(peekToken() instanceof CloseBraceToken)){
            if(!(peekToken() instanceof ActionToken)){
                // TODO: throw error
            }

            variables.add(((ActionToken)nextToken()).getAction());
        }

        if(!(nextToken() instanceof CloseBraceToken)){
            // TODO: throw error
        }

        return new VariableSetNode(variables, constructLocation(start));
    }

    private InterruptNode parseInterrupt() throws CompilationException {
        int start = index;
        if(!(nextToken() instanceof InterruptToken)){
            // TODO : throw error
        }

        ActionLabelNode action = parseActionLabel();

        if(!(nextToken() instanceof InterruptToken)){
            // TODO : throw error
        }

        ASTNode process = parseComposite();

        return new InterruptNode(action, process, constructLocation(start));
    }

    // EXPRESSION

    private String parseExpression() throws CompilationException {
        List<String> exprTokens = new ArrayList<String>();
        parseExpression(exprTokens);

        Expression expression = expressionParser.parseExpression(exprTokens);

        if(expressionEvaluator.isExecutable(expression)){
            int result = expressionEvaluator.evaluateExpression(expression, new HashMap<String, Integer>());
            return "" + result;
        }

        String variable = nextVariableId();
        variableMap.put(variable, expression);
        return variable;
    }

    private void parseExpression(List<String> expression){
        parseBaseExpression(expression);
        while(hasExpressionToken()){
            parseOperator(expression);
            parseBaseExpression(expression);
        }
    }

    private void parseBaseExpression(List<String> expression){
        Token token = nextToken();

        // check if a unary operation can be parsed
        if(token instanceof OperatorToken){
            if(token instanceof AdditionToken){
                expression.add("#+");
                token = nextToken();
            }
            else if(token instanceof SubtractionToken){
                expression.add("#-");
                token = nextToken();
            }
            else if(token instanceof NegateToken){
                expression.add("#!");
                token = nextToken();
            }
            else if(token instanceof BisimulationToken){
                expression.add("#~");
                token = nextToken();
            }

            // TODO: throw error: incorrect operator
        }

        // check if either a integer, variable, constant or parenthesised expression can be parsed
        if(token instanceof IntegerToken){
            int integer = ((IntegerToken)token).getInteger();
            expression.add("" + integer);
        }
        else if(token instanceof ActionToken){
            String variable = "$" + ((ActionToken)token).getAction();
            expression.add(variable);
        }
        else if(token instanceof IdentifierToken){
            String identifier = ((IdentifierToken)token).getIdentifier();

            // check that a constant has been defined with the specified identifier
            if(!constantMap.containsKey(identifier)){
                // TODO: throw error
            }

            // check that the constant referenced is a const (integer value)
            ASTNode constant = constantMap.get(identifier);
            if(!(constant instanceof ConstNode)){
                // TODO: throw error

            }

            int value = ((ConstNode)constant).getValue();
            expression.add("" + value);
        }
        else if(token instanceof OpenParenToken){
            expression.add("(");
            parseExpression(expression);

            token = nextToken();
            if(!(token instanceof CloseParenToken)){
                // TODO: throw error
            }

            expression.add(")");
        }
    }

    private void parseOperator(List<String> expression){
        Token token = nextToken();

        // check that the operation is an operator
        if(!(token instanceof OperatorToken)){
            // TODO: throw error
        }

        if(token instanceof OrToken){
            expression.add("||");
        }
        else if(token instanceof AndToken){
            expression.add("&&");
        }
        else if(token instanceof BitOrToken){
            expression.add("|");
        }
        else if(token instanceof ExclOrToken){
            expression.add("^");
        }
        else if(token instanceof BitAndToken){
            expression.add("&");
        }
        else if(token instanceof EqualityToken){
            expression.add("==");
        }
        else if(token instanceof NotEqualToken){
            expression.add("!=");
        }
        else if(token instanceof LessThanToken){
            expression.add("<");
        }
        else if(token instanceof LessThanEqToken){
            expression.add("<=");
        }
        else if(token instanceof GreaterThanToken){
            expression.add(">");
        }
        else if(token instanceof GreaterThanEqToken){
            expression.add(">=");
        }
        else if(token instanceof LeftShiftToken){
            expression.add("<<");
        }
        else if(token instanceof RightShiftToken){
            expression.add(">>");
        }
        else if(token instanceof AdditionToken){
            expression.add("+");
        }
        else if(token instanceof SubtractionToken){
            expression.add("-");
        }
        else if(token instanceof MultiplicationToken){
            expression.add("*");
        }
        else if(token instanceof DivisionToken){
            expression.add("/");
        }
        else if(token instanceof ModuloToken){
            expression.add("%");
        }
        else{
            // TODO: throw error
        }
    }

    private int parseSimpleExpression() throws CompilationException {
        List<String> exprTokens = new ArrayList<String>();
        parseSimpleExpression(exprTokens);

        Expression expression = expressionParser.parseExpression(exprTokens);
        return expressionEvaluator.evaluateExpression(expression, new HashMap<String, Integer>());
    }

    private void parseSimpleExpression(List<String> expression){
        parseBaseSimpleExpression(expression);
        while(hasExpressionToken()){
            parseSimpleOperator(expression);
            parseBaseSimpleExpression(expression);
        }
    }

    private void parseBaseSimpleExpression(List<String> expression){
        Token token = nextToken();

        // check if a unary operation can be parsed
        if(token instanceof OperatorToken){
            if(token instanceof AdditionToken){
                expression.add("#+");
                token = nextToken();
            }
            else if(token instanceof SubtractionToken){
                expression.add("#-");
                token = nextToken();
            }
            else if(token instanceof NegateToken){
                expression.add("#!");
                token = nextToken();
            }
            else if(token instanceof BisimulationToken){
                expression.add("#~");
                token = nextToken();
            }

            // TODO: throw error: incorrect operator
        }

        // check if either a integer, constant or parenthesised expression can be parsed
        if(token instanceof IntegerToken){
            int integer = ((IntegerToken)token).getInteger();
            expression.add("" + integer);
        }
        else if(token instanceof IdentifierToken){
            String identifier = ((IdentifierToken)token).getIdentifier();

            // check that a constant has been defined with the specified identifier
            if(!constantMap.containsKey(identifier)){
                // TODO: throw error
            }

            // check that the constant referenced is a const (integer value)
            ASTNode constant = constantMap.get(identifier);
            if(!(constant instanceof ConstNode)){
                // TODO: throw error

            }

            int value = ((ConstNode)constant).getValue();
        }
        else if(token instanceof OpenParenToken){
            expression.add("(");
            parseExpression(expression);

            token = nextToken();
            if(!(token instanceof CloseParenToken)){
                // TODO: throw error
            }

            expression.add(")");
        }
    }


    private void parseSimpleOperator(List<String> expression){
        Token token = nextToken();

        // check that the token is an operator
        if(!(token instanceof OperatorToken)){
            // TODO: throw error
        }

        if(token instanceof AdditionToken){
            expression.add("+");
        }
        else if(token instanceof SubtractionToken){
            expression.add("-");
        }
        else if(token instanceof MultiplicationToken){
            expression.add("*");
        }
        else if(token instanceof DivisionToken){
            expression.add("/");
        }
        else if(token instanceof ModuloToken){
            expression.add("%");
        }
        else{
            // TODO: throw error
        }
    }

    private boolean hasExpressionToken(){
        Token token = peekToken();
        if(token instanceof OperatorToken){
            return true;
        }
        else if(token instanceof IntegerToken){
            return true;
        }
        else if(token instanceof IdentifierToken){
            return true;
        }
        else if(token instanceof ActionToken){
            return true;
        }
        else if(token instanceof OpenParenToken){
            return true;
        }

        return false;
    }

    private String parseActionRangeOrExpression() throws CompilationException {
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

    private String nextVariableId(){
        return "$v" + variableId++;
    }

    /**
     * Helper method that retrieves the next @code{Token} and moves the index position
     * to point to the next @code{Token}.
     *
     * @return
     *      -- the next @Code{Token}
     */
    private Token nextToken(){
        checkNotEOF();
        return tokens.get(index++);
    }

    /**
     * Helper method that retrieves the next @code{Token} without incrementing the index position.
     *
     * @return
     *      -- the next @code{Token}
     */
    private Token peekToken(){
        checkNotEOF();
        return tokens.get(index);
    }

    private boolean hasProcessLabel(){
        int start = index;

        while(true){
            Token token = nextToken();
            if(token instanceof OpenBracketToken){
                token = nextToken();
                if(!(token instanceof ActionToken) && !(token instanceof IntegerToken)){
                    index = start;
                    return false;
                }

                if(!(nextToken() instanceof CloseBracketToken)){
                    index = start;
                    return false;
                }
            }
            else if(!(token instanceof ActionToken)){
                index = start;
                return false;
            }

            if(peekToken() instanceof DotToken){
                nextToken();
            }
            else if(!(peekToken() instanceof OpenBracketToken)){
                break;
            }

        }

        if(!(nextToken() instanceof ColonToken)){
            index = start;
            return false;
        }

        index = start;
        return true;
    }

    private boolean hasLabel(){
        if(!(peekToken() instanceof ActionToken)){
            return false;
        }
        if(index < tokens.size() - 1 && (tokens.get(index + 1) instanceof ColonToken)){
            return true;
        }

        return false;
    }

    private Location constructLocation(int start){
        Location locStart = tokens.get(start).getLocation();
        Location locEnd = tokens.get(index - 1).getLocation();
        return new Location(locStart, locEnd);
    }

    private void checkNotEOF(){
        if(index >= tokens.size()){
            // TODO: throw error
        }
    }

    private void reset(){
        processes.clear();
        variableMap.clear();
        constantMap.clear();
        actionRanges.clear();
        index = 0;
        variableId = 0;
    }

    private class ExpressionParser {

        private List<String> tokens;
        private Map<String, Integer> precedenceMap;
        private Stack<String> operatorStack;
        private Stack<Expression> output;

        public ExpressionParser(){
            tokens = new ArrayList<String>();
            precedenceMap = constructPrecedenceMap();
            operatorStack = new Stack<String>();
            output = new Stack<Expression>();
        }

        public Expression parseExpression(List<String> tokens){
            reset();
            this.tokens = tokens;

            for(String token : tokens){
                // check if the current token is an integer
                if(Character.isDigit(token.charAt(0))){
                    output.push(new IntegerOperand(Integer.parseInt(token)));
                }
                // check if the current token is a variable
                else if(token.charAt(0) == '$'){
                    output.push(new VariableOperand(token));
                }
                // check if token is an open parenthesis
                else if(token.equals("(")){
                    operatorStack.push(token);
                }
                // check if the token is a closed parenthesis
                else if(token.equals(")")){
                    while(!operatorStack.isEmpty()){
                        String operator = operatorStack.pop();
                        if(operator.equals("(")){
                            break;
                        }

                        output.push(constructOperator(operator));
                    }
                }
                // otherwise the token is an operator
                else{
                    int precedence = precedenceMap.get(token);
                    while(!operatorStack.isEmpty() && !operatorStack.peek().equals("(")){
                        if(precedenceMap.get(operatorStack.peek()) < precedence){
                            output.push(constructOperator(operatorStack.pop()));
                        }
                    }

                    operatorStack.push(token);
                }
            }

            while(!operatorStack.isEmpty()){
                output.push(constructOperator(operatorStack.pop()));
            }

            return output.pop();
        }

        private Expression constructOperator(String operator){
            if(operator.charAt(0) == '#'){
                return constructUnaryOperator(operator);
            }

            return constructBinaryOperator(operator);
        }

        private Expression constructUnaryOperator(String operator){
            Expression operand = output.pop();
            switch(operator){
                case "#+":
                    return new PositiveOperator(operand);
                case "#-":
                    return new NegativeOperator(operand);
                case "#!":
                    return new NotOperator(operand);
                case "#~":
                    return new BitNotOperator(operand);
            }

            return null;
        }

        private Expression constructBinaryOperator(String operator){
            Expression rhs = output.pop();
            Expression lhs = output.pop();
            switch(operator){
                case "||":
                    return new OrOperator(lhs, rhs);
                case "|":
                    return new BitOrOperator(lhs, rhs);
                case "^":
                    return new ExclOrOperator(lhs, rhs);
                case "&&":
                    return new AndOperator(lhs, rhs);
                case "&":
                    return new BitAndOperator(lhs, rhs);
                case "==":
                    return new EqualityOperator(lhs, rhs);
                case "!=":
                    return new NotEqualOperator(lhs, rhs);
                case "<":
                    return new LessThanOperator(lhs, rhs);
                case "<=":
                    return new LessThanEqOperator(lhs, rhs);
                case ">":
                    return new GreaterThanOperator(lhs, rhs);
                case ">=":
                    return new GreaterThanEqOperator(lhs, rhs);
                case "<<":
                    return new LeftShiftOperator(lhs, rhs);
                case ">>":
                    return new RightShiftOperator(lhs, rhs);
                case "+":
                    return new AdditionOperator(lhs, rhs);
                case "-":
                    return new SubtractionOperator(lhs, rhs);
                case "*":
                    return new MultiplicationOperator(lhs, rhs);
                case "/":
                    return new DivisionOperator(lhs, rhs);
                case "%":
                    return new ModuloOperator(lhs, rhs);
            }

            return null;
        }

        private Map<String, Integer> constructPrecedenceMap(){
            Map<String, Integer> precedenceMap = new HashMap<String, Integer>();
            precedenceMap.put("(", 0);
            precedenceMap.put(")", 0);

            precedenceMap.put("#+", 1);
            precedenceMap.put("#-", 1);
            precedenceMap.put("#!", 1);
            precedenceMap.put("#~", 1);

            precedenceMap.put("*", 2);
            precedenceMap.put("/", 2);
            precedenceMap.put("%", 2);

            precedenceMap.put("+", 3);
            precedenceMap.put("-", 3);

            precedenceMap.put("<<", 4);
            precedenceMap.put(">>", 4);

            precedenceMap.put("<", 5);
            precedenceMap.put("<=", 5);
            precedenceMap.put(">", 5);
            precedenceMap.put(">=", 5);

            precedenceMap.put("==", 6);
            precedenceMap.put("!=", 6);

            precedenceMap.put("&", 7);
            precedenceMap.put("^", 8);
            precedenceMap.put("|", 9);
            precedenceMap.put("&&", 10);
            precedenceMap.put("||", 11);

            return precedenceMap;
        }

        private void reset(){
            operatorStack.clear();
            output.clear();
        }

    }
}