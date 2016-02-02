{
    var processes = {};
    var operations = [];
    var comments = [];
    
    var variableMap = {};
    
    /* Determines if the specified identifier is valid */
    function isValidIdentifier(identifier){
        if(variableMap[identifier] != undefined){
            error('The identifier \'' + identifier + '\' has already been used.');
        }
    }
    
    function isTerminal(constant){
        if(constant == 'STOP' || constant == 'ERROR'){
            return true;
        }
        return false;
    }
    
    function isValidProcess(ident){
        if(processes[ident] == undefined){
            error('The process \'' + ident + '\' has not been defined.');
        }
        
        return processes[ident]
    }
    
    function isValidConstant(constant){
        if(variableMap[constant] == undefined){
                
        }
        
        return variableMap[constant];
    }
    
    function getConstant(constant){
        if(variableMap[constant] != undefined){
           return variableMap[constant];
        }
        
        return constant
    }
    
    function getFromVariableMap(identifier){
        var value = variableMap[identifier];
        if(value == undefined){
            error('The constant \'' + identifier + '\' has not been defined.');
        }
        return value;
    }
}

/* Constructs a parse tree of finite state processes */
ParseTree = FiniteStateProcess* {
    return {
        processes,
        operations,
    };
}

/* Attempts to parse and return a finite state process */
FiniteStateProcess
 =  _ ident:ConstantDefinition _
 / _ ident:RangeDefinition _
 / _ ident:SetDefinition _
 / _ process:ProcessDefinition _ {
    processes[process.identifier] = process.process;
 }
 / _ process:CompositeDefinition _ {
    processes[process.identifier] = process.process;
 }
 / _ process:FunctionDefinition _ {
    processes[process.identifier] = process.process;
 }
 / _ operation:OperationDefinition _ {
    operations.push(operation);
 }
 / _ comment:Comment _ {
    comments.push(text());
 }

/* Parses whitespace */
_ 'whitespace' = [ \t\n\r]*

/* Attempts to parse an indentifier for a finite state process */
Identifier
 = ('STOP' / '(' _ 'STOP' _ ')')
 / ('ERROR' / '(' _ 'ERROR' _')')
 / ident:UpperCaseIdentifier _ indices:Indices {
    return ident + indices;
 }
 / ident:UpperCaseIdentifier _ ranges:IndexRanges {
    return ident + ranges;
 }
 / ident:UpperCaseIdentifier {
    return ident;
 }

Variable
 = LowerCaseIdentifier

/* Parses and returns a string where the first character is upper case */
UpperCaseIdentifier
 = $([A-Z][A-Za-z0-9_]*) {
    return text();
 }

/* Parses and returns a string where the first character is lower case */
LowerCaseIdentifier
 = $([a-z][A-Za-z0-9_]*)

/* Parses and returns an integer */
IntegerLiteral
 = [-]?[0-9]+

/**
 * Parsing of Action Labels
 */

/* Attempts to parse and return an action label */
ActionLabel
 = action1:LowerCaseIdentifier _ action2:(_ActionLabel ?){
    return (action2 != null) ? action1 + action2 : action1;
 }
 / '[' _ exp:Expression _ ']' _ action:(_ActionLabel ?){
    exp = '[' + exp + ']';
    return (action != null) ? exp + action : exp;
 }

/* Used to avoid left hand recursion in ActionLabel */
_ActionLabel
 = '.' _ action:LowerCaseIdentifier { return '.' + action; }
 / '[' _ exp:Expression _ ']' { return '[' + exp + ']'; }

/* Attempts to parse and return multiple action labels */
ActionLabels
 = prefix:(('!' / '?') ?) _ action1:ActionLabel _ action2:(_ActionLabels ?) {
    action1 = (prefix != null) ? prefix + action1 : action1;
    return (action2 != null) ? action1 + action2 : action1;
 }
 / Set
 / prefix:(('!' / '?') ?) _ '[' _ range:ActionRange _ ']'  _ action:(_ActionLabels ?) {
    range = '[' + range + ']';
    range = (prefix != null) ? prefix + range : range;
    return (action != null) ? range + action : range;
 }
 / prefix:(('!' / '?') ?) _ '[' _ exp:Expression _ ']' _ action:(_ActionLabels ?) {
    exp = '[' + exp + ']';
    exp = (prefix != null) ? prefix + exp : exp;
    return (action != null) ? exp + action : exp;   
 }

/* Used to avoid left hand recursion in ActionLabels */
_ActionLabels
 = '.' _ action1:ActionLabel _ action2:(_ActionLabels ?) {
    action1 = '.' + action1;
    return (action2 != null) ? action1 + action2 : action1;
 }
 / '.' _ set:Set _ action:(_ActionLabels ?){
    set = '.' + set;
    return (action != null) ? set + action : set;  
 }
 / '[' _ range:ActionRange _ ']' _ action:(_ActionLabels ?) {
    range = '[' + range + ']';
    return (action != null) ? range + action : range;
 }
 / '[' _ exp:Expression _ ']' _ action:(_ActionLabels ?) {
    exp = '[' + exp + ']';
    return (action != null) ? exp + action : exp; 
 }

/* Attempts to parse and return an action range */
ActionRange
 = (Range / Set)
 / variable:Variable _ ':' _ range:(Range / Set) {
    return variable + ':' + range;
 }

/* Attempts to parse and return a range */
Range
 = start:Expression '..' end:Expression {
    return start + '..' + end;
 }
 / Identifier

/* Attempts to parse and return a set */
Set
 = '{' _ elements:SetElements _ '}' {
    return '{' + elements + '}';
 }
 / ident:Identifier {
    return getConstant(ident);
 }

/** Parse and return an array of set elements */
SetElements
 = action1:ActionLabels _ action2:(_SetElements ?){
    return (action2 != null) ? action1 + action2 : action1;
 }

/* Used to avoid left hand recursion in SetElements */
_SetElements
 =  ',' _ elements:SetElements {
    return ',' + elements;
 }

/**
 * Parsing of Constants, Ranges and Sets
 */

/* Attempts to parse and return a constant definition */
ConstantDefinition
 = 'const' _ ident:Identifier _ '=' _ exp:SimpleExpression {
    isValidIdentifier(ident);
    variableMap[ident] = exp.trim();
 }

/* Attempts to parse and return a range definition */
RangeDefinition
 = 'range' _ ident:Identifier _ '=' _ start:SimpleExpression _ '..' _ end:SimpleExpression {
    isValidIdentifier(ident);
    variableMap[ident] = start + '..' + end;
 }

/* Attempt to parse and return a set definition */
SetDefinition
 = 'set' _ ident:Identifier _ '=' _ set:Set {
    isValidIdentifier(ident);
    variableMap[ident] = set;
 }

/**
 * Parsing of Process Definitions
 */

/* Attempts to parse and return a process definition */
ProcessDefinition
 = ident:Identifier _ isVisible:('*' ?) _ '=' _ body:ProcessBody _ relabel:(Relabel ?) _ hide:(Hiding ?) _ '.' {
    isVisible = (isVisible != null) ? isVisible : '';
    relabel = (relabel != null) ? relabel : '';
    hide = (hide != null) ? hide : '';
    return { identifier: ident, process:ident + isVisible + '=' + body + relabel + hide + '.' };
 }
 
/* Attempts to parse and return the body of a process */
ProcessBody
 = process:LocalProcess _ ',' _ definitions:LocalProcessDefinitions {
    return process + ',' + definitions;
 }
 / LocalProcess

/* Attempts to parse and return a locally defined process definitions */
LocalProcessDefinitions
 = def1:LocalProcessDefinition _ def2:(_LocalProcessDefinitions ?) {
    return (def2 != null) ? def1 + def2 : def1;
 }

/* Used to remove left hand recursion from LocalProcessDefinitions */
_LocalProcessDefinitions
 = ',' _ definitions:LocalProcessDefinitions {
    return ',' + definitions;
 }

/* Attempts to parse and return a singular locally defined process definition */
LocalProcessDefinition
 = ident:Identifier _ '=' _ process:LocalProcess {
    return ident + '=' + process;
 }

/* Attempts to parse and return a local process */
LocalProcess
 = _LocalProcess
 
_LocalProcess
 = BaseLocalProcess
 / IfStatement
 / '(' _ choice:Choice _ ')' {
    return '(' + choice + ')';
 }
 / Choice

/* Attempts to parse and return a base local process */
BaseLocalProcess
 = ('STOP' / '(' _ 'STOP' _ ')') { return 'STOP'; }
 / ('ERROR' / '(' _ 'ERROR' _')') { return 'ERROR'; }
 / ident:Identifier { return ident; }

IfStatement
 = 'if' _ guard:Expression _ 'then' _ process1:_LocalProcess _ 'else' _ process2:_LocalProcess {
    return 'if ' + guard + ' then ' + process1 + ' else ' + process2;
 }
 / 'if' _ exp:Expression _ 'then' _ process:_LocalProcess {
    return 'if ' + guard + ' then ' + process;
 }

/* Attempts to parse and return a choice */
Choice
 = action:ActionPrefix _ choice:(_Choice ?) {
    return (choice != null) ? action + choice : action;
 }

/* Used to remove left hand recursion from 'Choice' */
_Choice
 = '|' _ choice:Choice {
    return '|' + choice;
 }

ActionPrefix
 = guard:(Guard ?) _ actions:PrefixActions {
    return (guard != null) ? guard + actions : actions;
 }
 
PrefixActions
 = action1:ActionLabels _ action2:(_PrefixActions ?) {
    return (action2 != null) ? action1 + action2 : action1;
 }
 
_PrefixActions
 = '->' _ process:(IfStatement / PrefixActions / LocalProcess) {
    return '->' + process;
 }

Guard
 = 'when' _ exp:Expression {
    return 'when ' + exp;
 }

Indices
 = '[' _ exp:Expression _ ']' _ indices:(Indices ?) {
    exp = '[' + exp + ']';
    return (indices != null) ? exp + indices : exp;
 }

IndexRanges
 = '[' _ range:ActionRange _ ']' _ index:(IndexRanges ?) {
    range = '[' + range + ']';
    return (index != null) ? range + index : range;
 }
 / '[' _ exp:Expression _ ']' _ index:(IndexRanges ?) {
    exp = '[' + exp + ']';
    return (index != null) ? exp + index : exp;
 }

/**
 * Parsing of Composite Processes
 */

CompositeDefinition
 = ('||' ?) _ ident:Identifier _ isVisible:('*' ?) _ '=' _ body:CompositeBody _ hide:(Hiding ?) _ '.' {
    isVisible = (isVisible != null) ? isVisible : '';
    hide = (hide != null) ? hide : '';
    return { identifier: ident, process: '||' + ident + '=' + body + hide + '.' };
 }
 
CompositeBody
 = 'forall' _ ranges:Ranges _ body:CompositeBody {
    return 'forall ' + ranges + ' ' + body;
 }
 / prefix:(PrefixLabel ?) _ ident:Identifier _ relabel:(Relabel ?) {
    prefix = (prefix != null) ? prefix : '';
    relabel = (relabel != null) ? relabel : '';
    ident = isValidProcess(ident);
    return prefix + ident + relabel;
 }
 / prefix:(PrefixLabel ?) _ '(' _ parallel:ParallelComposition _ ')' _ relabel:(Relabel ?) {
    prefix = (prefix != null) ? prefix : '';
    relabel = (relabel != null) ? relabel : '';
    return prefix + '(' + parallel + ')' + relabel;
 }

PrefixLabel
 = action:ActionLabels _ '::' {
    return action + '::';
 }
 / action:ActionLabels _ ':' {
    return action + ':';
 }
 
ParallelComposition
 = body:CompositeBody _ parallel:(_ParallelComposition ?) {
    return (parallel != null) ? body + parallel : body;
 }

/* Used to remove left hand recursion from 'ParallelComposition' */
_ParallelComposition
 = '||' _ parallel:ParallelComposition {
    return '||' + parallel;
 }

Ranges
 = '[' _ range:ActionRange _ ']' _ ranges:(Ranges ?) {
    range = '[' + range + ']';
    return (ranges != null) ? range + ranges : range;
 }

/**
 * Parsing of Function Processes
 */

FunctionDefinition
 = ident:Identifier _ isVisible:('*' ?) _ '=' _ type:FunctionType _ '(' _ body:FunctionBody _ ')' _ relabel:(Relabel ?) _ hide:(Hiding ?) _ '.' {
    isVisible = (isVisible != null) ? isVisible : '';
    relabel = (relabel != null) ? relabel : '';
    hide = (hide != null) ? hide : '';
    return { identifier:ident, process:ident + isVisible + '=' + type + '(' + body + ')' + relabel + hide + '.' };
 }

FunctionType
 = 'abs' { return 'abs' }
 / 'simp' { return 'simp' }

FunctionBody
 = type:FunctionType _ '(' _ body:FunctionBody _ ')' _ relabel:(Relabel ?) _ hide:(Hiding ?) {
    relabel = (relabel != null) ? relabel : '';
    hide = (hide != null) ? hide : '';
    return type + '(' + body + ')' + relabel + hide;
 }
 / ident:Identifier _ relabel:(Relabel ?) _ hide:(Hiding ?) {
    relabel = (relabel != null) ? relabel : '';
    hide = (hide != null) ? hide : '';
    ident = isValidProcess(ident);
    return ident + relabel + hide;
 }
 / process:LocalProcess _ relabel:(Relabel ?) _ hide:(Hiding ?) {
    relabel = (relabel != null) ? relabel : '';
    hide = (hide != null) ? hide : '';
    return process + relabel + hide;
 }
 / body:CompositeBody _ relabel:(Relabel ?) {
    relabel = (relabel != null) ? relabel : '';
    return body + relabel;
 }

/**
 * Parsing of Operations
 */

OperationDefinition
 = process1:OperationProcess _ isNegated:('!' ?) op:Operation _ process2:OperationProcess _ '.' {
    isNegated = (isNegated != null) ? isNegated : '';
    return process1 + isNegated + op + process2 + '.';
 }
 
OperationProcess
 = ident:BaseLocalProcess {
    if(ident == 'STOP' || ident == 'ERROR'){
        return ident;
    }
    return isValidProcess(ident);
 }
 / FunctionBody
 / ProcessBody
 / CompositeBody

Operation
 = '~' { return '~'; }

/**
 * Parsing of Relabelling and Hiding
 */
 
Relabel
 = '/' _ '{' _ definitions:RelabelDefinitions _ '}' {
    return '/{' + definitions + '}';
 }
 
RelabelDefinitions
 = definition:RelabelDefinition _ definitions:(_RelabelDefinitions ?) {
    return (definitions != null) ? definition + definitions : definition;
 }

/* Used to remove left hand recursion from 'RelabelDefinitions' */
_RelabelDefinitions
 = ',' _ definitions:RelabelDefinitions {
    return ',' + definitions;
 }

RelabelDefinition
 = newLabel:ActionLabels _ '/' _ oldLabel:ActionLabels {
    return newLabel + '/' + oldLabel;
 }

Hiding
 = '\\' _ set:Set {
    return '\\' + set;
 }
 / '@' _ set:Set {
    return '@' + set;
 }

/**
 * Parsing of Single Line and Multi Line Comments
 */

/* Parses and returns a comment */
Comment = _ (SingleLinedComment / MultiLinedComment) _ {
    return text().trim();
}

/* Helper function for 'Comment' which parses and returns a single lined comment. */
SingleLinedComment = '//' (!LineTerminator SourceCharacter)*

/* Helper function for 'Comment' which parses and returns a multi lined comment. */
MultiLinedComment = '/*' (!'*/' SourceCharacter)* '*/'

/* Parses the termination of a line. */
LineTerminator 'line terminator' = [\n\r\u2028\u2029]

/* Parses a source character for a comment. */
SourceCharacter 'source character' = .

/**
 * Parsing of Expressions and Simple Expressions
 */

Expression
 = _Expression

_Expression
 = operand1:BaseExpression _ remaining:((operator:ExpressionOperators _ operand2:_Expression {
    return operator + ' ' + operand2;
 }) ?) {
    if(remaining != null){
        operand1 = operand1 + ' ' + remaining;
    }
    return operand1;
 }

SimpleExpression
 = _SimpleExpression

_SimpleExpression
 = operand1:SimpleBaseExpression _ remaining:((operator:SimpleExpressionOperators _ operand2:_SimpleExpression ? {
    return operator + ' ' + operand2;
 }) ?) {
    if(remaining != null){
        operand1 = operand1 + ' ' + remaining;
    }
    return operand1;
 }

ExpressionOperators 'an expression operator'
 = '||'
 / '&&'
 / '|'
 / '^'
 / '&'
 / '=='
 / '!='
 / '<'
 / '<='
 / '>'
 / '>='
 / '>>'
 / '<<'
 / SimpleExpressionOperators
 
SimpleExpressionOperators
 = '+'
 / '-'
 / '*'
 / '/'
 / '%'
 
BaseExpression
 = IntegerLiteral { return text(); }
 / Variable { return text(); }
 / ident:UpperCaseIdentifier {
    isValidConstant(ident);
    return variableMap[ident];
 }
 / '(' _ exp:_Expression _ ')' { return '( ' + exp + ' )'; }
  
SimpleBaseExpression
 = IntegerLiteral { return text(); }
 / Variable { return text(); }
 / ident:UpperCaseIdentifier {
    isValidConstant(ident);
    return variableMap[ident];
 }
 / '(' _ exp:_SimpleExpression _ ')' { return '( ' + exp + ' )'; }