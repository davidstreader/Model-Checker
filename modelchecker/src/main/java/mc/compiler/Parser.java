package mc.compiler;

import mc.Constant;
import mc.compiler.ast.*;
import mc.compiler.token.*;
import mc.exceptions.CompilationException;
import mc.util.Location;
import mc.util.expr.*;

import java.util.*;

/**
 * Created by sheriddavi on 30/01/17.
 */
public class Parser {

    private List<Token> tokens;
    private List<ProcessNode> processes;
    private Set<String> processIdentifiers;
    private List<OperationNode> operations;
    private List<OperationNode> equations;
    private Map<String, Expression> variableMap;
    private Map<String, ASTNode> constantMap;
    private List<IndexNode> actionRanges;
    private Set<String> definedVariables;
    private int index;
    private int variableId;

    private ExpressionParser expressionParser;
    private ExpressionEvaluator expressionEvaluator;

    public Parser() {
        processes = new ArrayList<ProcessNode>();
        processIdentifiers = new HashSet<String>();
        operations = new ArrayList<OperationNode>();
        equations = new ArrayList<OperationNode>();
        variableMap = new HashMap<String, Expression>();
        constantMap = new HashMap<String, ASTNode>();
        actionRanges = new ArrayList<IndexNode>();
        definedVariables = new HashSet<String>();
        index = 0;
        variableId = 0;
        expressionParser = new ExpressionParser();
        expressionEvaluator = new ExpressionEvaluator();
    }

    public AbstractSyntaxTree parse(List<Token> tokens) throws CompilationException, InterruptedException {
        reset();
        this.tokens = tokens;

        while (index < this.tokens.size() && !Thread.currentThread().isInterrupted()) {
            Token token = peekToken();
            if (token instanceof ProcessTypeToken) {
                parseProcessDefinition();
            } else if (token instanceof ConstToken) {
                parseConstDefinition();
            } else if (token instanceof RangeToken) {
                parseRangeDefinition();
            } else if (token instanceof SetToken) {
                parseSetDefinition();
            } else if (token instanceof OperationToken) {
                parseOperation();
            } else if (token instanceof EquationToken) {
                parseEquation();
            } else {
                throw constructException("expecting to parse a process, operation or const definition but received \"" + token.toString() + "\"", token.getLocation());
            }
        }

        return new AbstractSyntaxTree(processes, operations, equations, variableMap);
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
            IdentifierToken identifier = (IdentifierToken) token;
            return new IdentifierNode(identifier.getIdentifier(), identifier.getLocation());
        }

        throw constructException("expecting to parse an identifier but received \"" + token.toString() + "\"", token.getLocation());
    }

    // ACTION LABELS

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
                    if (constant instanceof ConstNode) {
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

                builder.append("[" + variable + "]");
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
        if (peekToken() instanceof NegateToken || peekToken() instanceof QuestionMarkToken) {
            Token token = nextToken();
            if (token instanceof NegateToken) {
                builder.append("!");
            } else {
                builder.append("?");
            }
        }

        return new ActionLabelNode(builder.toString(), constructLocation(start));
    }

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
        String variable = null;
        if (hasLabel()) {
            String label = parseLabel();
            definedVariables.add(label);
            variable = "$" + label;
        } else {
            // a variable has not been defined, give the variable a unique internally defined name
            variable = nextVariableId();
        }

        ASTNode range = null;
        if (peekToken() instanceof IdentifierToken && !(tokens.get(index + 1) instanceof RangeSeparatorToken)) {
            IdentifierNode identifier = parseIdentifier();

            if (!constantMap.containsKey(identifier.getIdentifier())) {
                throw constructException("the identifier \"" + identifier.getIdentifier() + "\" has not been defined", identifier.getLocation());
            }

            ASTNode constant = constantMap.get(identifier.getIdentifier());

            if (constant instanceof ConstNode) {
                throw constructException("expecting a range or set constant but received a const", identifier.getLocation());
            }

            range = constant;
        } else if (peekToken() instanceof OpenBraceToken) {
            range = parseSet();
        } else {
            range = parseRange();
        }

        IndexNode indexNode = new IndexNode(variable, range, null, constructLocation(start));
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
            startValue = expressionEvaluator.evaluateExpression(variableMap.get(expression), new HashMap<String, Integer>());
        } else {
            // otherwise the expression is an integer that we can parse
            startValue = Integer.parseInt(expression);
        }

        // ensure that the next token is the '..' token
        if (!(nextToken() instanceof RangeSeparatorToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \"..\" but received \"" + error.toString() + "\"", error.getLocation());
        }

        // parse the next expression and evaluate it if necessary to get the start value of the range
        expression = parseExpression();
        int endValue;
        if (variableMap.containsKey(expression)) {
            // in this case, 'expression' is an internal variable reference to an expression
            endValue = expressionEvaluator.evaluateExpression(variableMap.get(expression), new HashMap<String, Integer>());
        } else {
            // otherwise the expression is an integer that we can parse
            endValue = Integer.parseInt(expression);
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

        List<String> set = new ArrayList<String>();
        Map<Integer, RangesNode> rangeMap = new HashMap<Integer, RangesNode>();

        while (!(peekToken() instanceof CloseBracketToken)) {
            // parse the current action and add it to the set

            int rangeStart = actionRanges.size();
            ActionLabelNode action = parseActionLabel();

            if (rangeStart < actionRanges.size()) {
                List<IndexNode> ranges = new ArrayList<IndexNode>(actionRanges.subList(rangeStart, actionRanges.size()));
                actionRanges = new ArrayList<IndexNode>(actionRanges.subList(0, rangeStart));
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

    // CONSTANT DEFINITIONS

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

        ConstNode node = new ConstNode(value, constructLocation(start));
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
        constantMap.put(identifier.getIdentifier(), set);
    }

    // PROCESS DEFINITIONS

    private void parseProcessDefinition() throws CompilationException, InterruptedException {
        String processType = parseProcessType();

        Token token = peekToken();
        if (token instanceof IdentifierToken) {
            parseSingleProcessDefinition(processType);
        } else if (token instanceof OpenBraceToken) {
            parseProcessDefinitionBlock(processType);
        } else {
            throw constructException("expecting to parse a process definition or a process definition block but received \"" + peekToken().toString() + "\"");
        }
    }

    private void parseSingleProcessDefinition(String processType) throws CompilationException, InterruptedException {
        int start = index;
        IdentifierNode identifier = parseIdentifier();

        // check if a proceess with this identifier has already been defined
        if (processIdentifiers.contains(identifier.getIdentifier())) {
            throw constructException("The identifier \"" + identifier.getIdentifier() + "\" has already been defined", identifier.getLocation());
        }
        processIdentifiers.add(identifier.getIdentifier());

        // check if this process has been marked as not to be rendered
        boolean toRender = true;
        if (peekToken() instanceof MultiplicationToken) {
            nextToken();
            toRender = false;
        }

        // ensure that the next token is the '=' token
        if (!(nextToken() instanceof AssignToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \"=\" but received \"" + error.toString() + "\"", error.getLocation());
        }

        ASTNode process = parseComposite();

        List<LocalProcessNode> localProcesses = new ArrayList<LocalProcessNode>();
        Set<String> localIdentifiers = new HashSet<String>();

        while (peekToken() instanceof CommaToken && !Thread.currentThread().isInterrupted()) {
            nextToken(); // gobble the comma
            localProcesses.add(parseLocalProcessDefinition(localIdentifiers));
        }

        ProcessNode processNode = new ProcessNode(processType, identifier.getIdentifier(), process, localProcesses, constructLocation(start));

        if (!toRender) {
            processNode.getMetaData().put("skipped", true);
        }

        // check if a relabel set has been defined
        if (peekToken() instanceof DivisionToken) {
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

    private void parseProcessDefinitionBlock(String processType) throws CompilationException, InterruptedException {
        // ensure that the next token is the '{' token
        if (!(nextToken() instanceof OpenBraceToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \"{\" but received \"" + error.toString() + "\"", error.getLocation());
        }

        while (!(peekToken() instanceof CloseBraceToken)) {
            parseSingleProcessDefinition(processType);
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

        ASTNode process = parseLocalProcess();

        return new LocalProcessNode(identifier.getIdentifier(), ranges, process, constructLocation(start));
    }

    private String parseProcessType() throws CompilationException {
        Token token = nextToken();
        if (token instanceof ProcessTypeToken) {
            return ((ProcessTypeToken) token).getProcessType();
        }

        throw constructException("expecting to parse a process type but received \"" + token.toString() + "\"", token.getLocation());
    }

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
        }

        HidingNode hiding = null;
        if (peekToken() instanceof HideToken || peekToken() instanceof AtToken) {
            hiding = parseHiding();
        }

        // wrap the parsed process as a process root if either a label, relabel or hiding has been defined
        if (label != null || relabel != null || hiding != null) {
            process = new ProcessRootNode(process, label, relabel, hiding, process.getLocation());
        }

        if (peekToken() instanceof OrToken) {
            nextToken(); // gobble the '||' token
            ASTNode process2 = parseComposite();
            process = new CompositeNode(process, process2, constructLocation(start));
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

        List<IndexNode> ranges = new ArrayList<IndexNode>();
        if (rangeStart < actionRanges.size()) {
            ranges.addAll(actionRanges.subList(rangeStart, actionRanges.size()));
            actionRanges = new ArrayList<IndexNode>(actionRanges.subList(0, rangeStart));
            Collections.reverse(ranges);
        }

        // ensure that the next token is a '->' token
        if (!(nextToken() instanceof SequenceToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \"->\" but received \"" + error.toString() + "\"", error.getLocation());
        }

        ASTNode to = parseLocalProcess();

        ASTNode node = new SequenceNode(from, to, constructLocation(start));
        for (int i = 0; i < ranges.size(); i++) {
            ranges.get(i).setProcess(node);
            node = ranges.get(i);
        }

        return node;
    }

    private ASTNode parseBaseLocalProcess() throws CompilationException, InterruptedException {
        if (peekToken() instanceof TerminalToken) {
            return parseTerminal();
        } else if (peekToken() instanceof IdentifierToken) {
            IdentifierNode identifier = parseIdentifier();
            if (peekToken() instanceof OpenBracketToken) {
                identifier.setIdentifer(identifier.getIdentifier() + parseIndices());
            }

            return identifier;
        } else if (peekToken() instanceof FunctionToken) {
            return parseFunction();
        } else if (peekToken() instanceof ProcessTypeToken) {
            return parseCasting();
        } else if (peekToken() instanceof IfToken) {
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

    private FunctionNode parseFunction() throws CompilationException, InterruptedException {
        int start = index;
        String type = parseFunctionType();

        // check if any flags have been set
        Set<String> flags = null;
        if (peekToken() instanceof OpenBraceToken) {
            flags = parseFlags(type);
        }

        // ensure that the next token is a '(' token
        if (!(nextToken() instanceof OpenParenToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \"(\" but received \"" + error.toString() + "\"", error.getLocation());
        }

        ASTNode process = parseComposite();

        // ensure that the next token is a ')' token
        if (!(nextToken() instanceof CloseParenToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \")\" but received \"" + error.toString() + "\"", error.getLocation());
        }

        FunctionNode function = new FunctionNode(type, process, constructLocation(start));

        if (type.equals("abs") && flags != null) {
            processAbstractionFlags(function, flags);
        }
        if (type.equals("simp") && flags != null) {
            function.getMetaData().put("replacements", flags);
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

    private Set<String> validAbsFlags = new HashSet<String>(Arrays.asList("fair", "unfair"));

    private Set<String> parseFlags(String functionType) throws CompilationException, InterruptedException {
        int start;

        if (!(nextToken() instanceof OpenBraceToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \"{\" but received \"" + error.toString() + "\"", error.getLocation());
        }

        Set<String> flags = new HashSet<String>();

        while (!(peekToken() instanceof CloseBraceToken)) {
            if (!(peekToken() instanceof ActionToken)) {
                throw constructException("Expecting to parse a flag but received \"" + peekToken().toString() + "\"");
            }

            ActionToken token = (ActionToken) nextToken();
            String flag = token.getAction();

            if (!validAbsFlags.contains(flag) && Objects.equals(functionType, "abs")) {
                throw constructException("\"" + flag + "\" is not a correct flag", token.getLocation());
            }
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

    private void processAbstractionFlags(FunctionNode function, Set<String> flags) {
        if (flags.contains("fair") && !flags.contains("unfair")) {
            function.getMetaData().put("isFair", true);
        } else if (flags.contains("unfair") && !flags.contains("fair")) {
            function.getMetaData().put("isFair", false);
        } else if (flags.contains("fair") && flags.contains("unfair")) {
            function.getMetaData().put("isFair", true);
        }

        if (flags.contains("prune")) {
            function.getMetaData().put("prune", true);
        }
    }

    private FunctionNode parseCasting() throws CompilationException, InterruptedException {
        int start = index;
        String cast = parseProcessType();

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

        return new FunctionNode(cast, process, constructLocation(start));
    }

    private IfStatementNode parseIfStatement() throws CompilationException, InterruptedException {
        int start = index;
        // ensure that the next token is a 'if' token
        if (!(nextToken() instanceof IfToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \"if\" but received \"" + error.toString() + "\"", error.getLocation());
        }
        String expr = parseExpression();
        Expression expression;
        if (variableMap.containsKey(expr)) {
            expression = variableMap.get(expr);
        } else {
            expression = Expression.constructExpression(expr);
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

            return new IfStatementNode(expression, trueBranch, falseBranch, constructLocation(start));
        }

        return new IfStatementNode(expression, trueBranch, constructLocation(start));
    }

    private IfStatementNode parseWhenStatement() throws CompilationException, InterruptedException {
        int start = index;
        // ensure that the next token is a 'when' token
        if (!(nextToken() instanceof WhenToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \"when\" but received \"" + error.toString() + "\"", error.getLocation());
        }
        String expr = parseExpression();
        Expression expression;
        if (variableMap.containsKey(expr)) {
            expression = variableMap.get(expr);
        } else {
            expression = Expression.constructExpression(expr);
        }
        ASTNode trueBranch = parseLocalProcess();
        return new IfStatementNode(expression, trueBranch, constructLocation(start));
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

    private ASTNode parseTerminal() throws CompilationException {
        Token token = nextToken();
        if (token instanceof TerminalToken) {
            if (token instanceof StopToken) {
                return new TerminalNode("STOP", token.getLocation());
            }

            // only other form of terminal is error so we can assume that the token is an error token
            ActionLabelNode deadlock = new ActionLabelNode(Constant.DEADLOCK, token.getLocation());
            TerminalNode terminal = new TerminalNode("ERROR", token.getLocation());
            return new SequenceNode(deadlock, terminal, token.getLocation());
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

            builder.append("[" + expression + "]");

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

        List<IndexNode> ranges = new ArrayList<IndexNode>(actionRanges.subList(rangeStart, actionRanges.size()));
        actionRanges = new ArrayList<IndexNode>(actionRanges.subList(0, rangeStart));

        return new RangesNode(ranges, constructLocation(start));
    }

    // RELABELLING AND HIDING

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

        List<RelabelElementNode> relabels = new ArrayList<RelabelElementNode>();
        int rangeStart = actionRanges.size();

        while (!(peekToken() instanceof CloseBraceToken)) {
            RelabelElementNode element = parseRelabelElement();

            if (rangeStart < actionRanges.size()) {
                List<IndexNode> ranges = new ArrayList<IndexNode>(actionRanges.subList(rangeStart, actionRanges.size()));
                actionRanges = new ArrayList<IndexNode>(actionRanges.subList(0, rangeStart));

                element.setRanges(new RangesNode(ranges, element.getLocation()));
            }

            relabels.add(element);

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
        int start = index;
        ActionLabelNode newLabel = parseActionLabel();

        if (!(nextToken() instanceof DivisionToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \"/\" but received \"" + error.toString() + "\"", error.getLocation());
        }

        ActionLabelNode oldLabel = parseActionLabel();

        return new RelabelElementNode(newLabel.getAction(), oldLabel.getAction(), constructLocation(start));
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

        SetNode set = parseSet();

        return new HidingNode(type, set, constructLocation(start));
    }

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

        Set<String> variables = new HashSet<String>();

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
        if (!(nextToken() instanceof OpenParenToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \"(\" but received \"" + error.toString() + "\"", error.getLocation());
        }
        boolean alphabet;
        int nodeCount, alphabetCount, maxTransitionCount;
        if (!(peekToken().toString().equals("true") || peekToken().toString().equals("false"))) {
            Token error = tokens.get(index);
            throw constructException("expecting to parse \"true or false\" but received \"" + error.toString() + "\"", error.getLocation());
        }
        alphabet = Boolean.parseBoolean(nextToken().toString());
        if (!(nextToken() instanceof CommaToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \",\" but received \"" + error.toString() + "\"", error.getLocation());
        }
        if (!(peekToken() instanceof IntegerToken)) {
            Token error = tokens.get(index);
            throw constructException("expecting to parse an integer but received \"" + error.toString() + "\"", error.getLocation());
        }
        nodeCount = ((IntegerToken) nextToken()).getInteger();
        if (!(nextToken() instanceof CommaToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \",\" but received \"" + error.toString() + "\"", error.getLocation());
        }
        if (!(peekToken() instanceof IntegerToken)) {
            Token error = tokens.get(index);
            throw constructException("expecting to parse an integer but received \"" + error.toString() + "\"", error.getLocation());
        }
        alphabetCount = ((IntegerToken) nextToken()).getInteger();
        if (!(nextToken() instanceof CommaToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \",\" but received \"" + error.toString() + "\"", error.getLocation());
        }
        if (!(peekToken() instanceof IntegerToken)) {
            Token error = tokens.get(index);
            throw constructException("expecting to parse an integer but received \"" + error.toString() + "\"", error.getLocation());
        }
        maxTransitionCount = ((IntegerToken) nextToken()).getInteger();

        if (!(nextToken() instanceof CloseParenToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \")\" but received \"" + error.toString() + "\"", error.getLocation());
        }
        if (!(peekToken() instanceof OpenBraceToken)) {
            parseSingleOperation(true, new EquationEvaluator.EquationSettings(alphabet, nodeCount, alphabetCount, maxTransitionCount));
        } else {
            parseOperationBlock(true, new EquationEvaluator.EquationSettings(alphabet, nodeCount, alphabetCount, maxTransitionCount));
        }
    }
    // OPERATIONS

    private void parseOperation() throws CompilationException, InterruptedException {
        if (!(nextToken() instanceof OperationToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \"operation\" but received \"" + error.toString() + "\"", error.getLocation());
        }

        if (!(peekToken() instanceof OpenBraceToken)) {
            parseSingleOperation(false, null);
        } else {
            parseOperationBlock(false, null);
        }
    }

    private void parseSingleOperation(boolean isEq, EquationEvaluator.EquationSettings equationSettings) throws CompilationException, InterruptedException {
        int start = index;
        ASTNode process1 = parseComposite();

        boolean isNegated = false;
        if (peekToken() instanceof NegateToken) {
            nextToken();
            isNegated = true;
        }

        String type = parseOperationType();

        ASTNode process2 = parseComposite();

        // ensure that the next token is a '.' token
        if (!(nextToken() instanceof DotToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \".\" but received \"" + error.toString() + "\"", error.getLocation());
        }
        OperationNode operation = new OperationNode(type, isNegated, process1, process2, constructLocation(start), equationSettings);
        if (isEq) {
            equations.add(operation);
        } else {
            operations.add(operation);
        }
    }

    private void parseOperationBlock(boolean isEq, EquationEvaluator.EquationSettings equationSettings) throws CompilationException, InterruptedException {
        if (!(nextToken() instanceof OpenBraceToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \"{\" but received \"" + error.toString() + "\"", error.getLocation());
        }

        while (!(peekToken() instanceof CloseBraceToken)) {
            parseSingleOperation(isEq, equationSettings);
        }

        if (!(nextToken() instanceof CloseBraceToken)) {
            Token error = tokens.get(index - 1);
            throw constructException("expecting to parse \"}\" but received \"" + error.toString() + "\"", error.getLocation());
        }
    }

    private String parseOperationType() throws CompilationException {
        if (peekToken() instanceof BisimulationTypeToken) {
            nextToken();
            return "bisimulation";
        }
        if (peekToken() instanceof TraceEquivalentTypeToken) {
            nextToken();
            return "traceEquivalent";
        }
        throw constructException("expecting to parse an operation type but received \"" + peekToken().toString() + "\"");
    }

    // EXPRESSIONS

    private String parseExpression() throws CompilationException, InterruptedException {
        List<String> exprTokens = new ArrayList<String>();
        parseExpression(exprTokens);

        Expression expression = expressionParser.parseExpression(exprTokens);
        if (expressionEvaluator.isExecutable(expression)) {
            Expression simp = ExpressionSimplifier.simplify(expression, Collections.emptyMap());
            if (simp instanceof BooleanOperand) {
                return ((BooleanOperand) simp).getValue() + "";
            }
            if (simp instanceof IntegerOperand) {
                return ((IntegerOperand) simp).getValue() + "";
            }
        } else if (expression instanceof VariableOperand) {
            return ((VariableOperand) expression).getValue();
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
                expression.add("#+");
                token = nextToken();
            } else if (token instanceof SubtractionToken) {
                expression.add("#-");
                token = nextToken();
            } else if (token instanceof NegateToken) {
                expression.add("#!");
                token = nextToken();
            } else if (token instanceof BisimulationTypeToken) {
                expression.add("#~");
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
            if (!(constant instanceof ConstNode)) {
                String type = (constant instanceof RangeNode) ? "range" : "set";
                Token error = tokens.get(index - 1);
                throw constructException("expecting a const but received a " + type, error.getLocation());

            }

            int value = ((ConstNode) constant).getValue();
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
        List<String> exprTokens = new ArrayList<String>();
        parseSimpleExpression(exprTokens);

        Expression expression = expressionParser.parseExpression(exprTokens);
        return expressionEvaluator.evaluateExpression(expression, new HashMap<String, Integer>());
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
                expression.add("#+");
                token = nextToken();
            } else if (token instanceof SubtractionToken) {
                expression.add("#-");
                token = nextToken();
            } else if (token instanceof NegateToken) {
                expression.add("#!");
                token = nextToken();
            } else if (token instanceof BisimulationTypeToken) {
                expression.add("#~");
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
            if (!(constant instanceof ConstNode)) {
                String type = (constant instanceof RangeNode) ? "range" : "set";
                Token error = tokens.get(index - 1);
                throw constructException("expecting a const but received a " + type, error.getLocation());
            }

            int value = ((ConstNode) constant).getValue();
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
        if (!(peekToken() instanceof ActionToken)) {
            return false;
        }
        if (index < tokens.size() - 1 && (tokens.get(index + 1) instanceof ColonToken)) {
            return true;
        }

        return false;
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

    private CompilationException constructException(String message, Location location) {
        return new CompilationException(Parser.class, message, location);
    }

    private class ExpressionParser {

        private List<String> tokens;
        private Map<String, Integer> precedenceMap;
        private Stack<String> operatorStack;
        private Stack<Expression> output;

        public ExpressionParser() {
            tokens = new ArrayList<String>();
            precedenceMap = constructPrecedenceMap();
            operatorStack = new Stack<String>();
            output = new Stack<Expression>();
        }

        public Expression parseExpression(List<String> tokens) {
            reset();
            this.tokens = tokens;

            for (String token : tokens) {
                // check if the current token is an integer
                if (Character.isDigit(token.charAt(0))) {
                    output.push(new IntegerOperand(Integer.parseInt(token)));
                }
                // check if the current token is a variable
                else if (token.charAt(0) == '$') {
                    output.push(new VariableOperand(token));
                }
                // check if token is an open parenthesis
                else if (token.equals("(")) {
                    operatorStack.push(token);
                }
                // check if the token is a closed parenthesis
                else if (token.equals(")")) {
                    while (!operatorStack.isEmpty()) {
                        String operator = operatorStack.pop();
                        if (operator.equals("(")) {
                            break;
                        }

                        output.push(constructOperator(operator));
                    }
                }
                // otherwise the token is an operator
                else {
                    int precedence = precedenceMap.get(token);
                    while (!operatorStack.isEmpty() && !operatorStack.peek().equals("(")) {
                        if (precedenceMap.get(operatorStack.peek()) < precedence) {
                            output.push(constructOperator(operatorStack.pop()));
                        } else {
                            break;
                        }
                    }

                    operatorStack.push(token);
                }
            }

            while (!operatorStack.isEmpty()) {
                output.push(constructOperator(operatorStack.pop()));
            }

            return output.pop();
        }

        private Expression constructOperator(String operator) {
            if (operator.charAt(0) == '#') {
                return constructUnaryOperator(operator);
            }

            return constructBinaryOperator(operator);
        }

        private Expression constructUnaryOperator(String operator) {
            Expression operand = output.pop();
            switch (operator) {
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

        private Expression constructBinaryOperator(String operator) {
            Expression rhs = output.pop();
            Expression lhs = output.pop();
            switch (operator) {
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

        private Map<String, Integer> constructPrecedenceMap() {
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

        private void reset() {
            operatorStack.clear();
            output.clear();
        }

    }
}
