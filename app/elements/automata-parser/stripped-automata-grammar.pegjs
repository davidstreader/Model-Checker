{
    var keywords = ['const', 'range', 'set', 'if', 'then', 'else', 'when', 'forall', 'abs', 'simp'];
    
    var variableMap = {};
    var processes = {};
    var operations = [];
    
    var localDependencies = {};
    var dependencies = {};
    
    /**
     * Returns the constant associated with the specified identifier.
     */
    function getConstant(ident){
        var variable = variableMap[ident];
        return (variable == undefined) ? ident : variable;
    }
    
    /**
     * Throws an error if the specified identifier is invalid.
     */
    function isValidIdentifier(ident){
        if(variableMap[ident] != undefined || processes[ident] != undefined){
            error('The identifier \'' + ident + '\' has already been defined.');
        }
    }
    
    /**
     * Throws an error if the specified constant is invalid
     */
    function isValidConstant(ident){
        if(variableMap[ident] == undefined){
            error('The identifier \'' + ident + '\' has not been defined.');
        }
    }
    
    /**
     * Throws an error if the specified action label is invalid.
     */
    function isValidActionLabel(label){
        for(var i = 0; i < keywords.length; i++){
            if(label == keywords[i]){
                error('Cannot use keyword \'' + keywords[i] + '\' as an action label.');
            }
        }
    }
}

/* Constructs and returns a parse tree. */
ParseTree = FiniteStateProcesses* {
    return {
        processes: processes,
        operations: operations
    };
}

/* Attempts to parse a finite state process.*/
FiniteStateProcesses
 = _ ConstantDefinition _
 / _ RangeDefinition _
 / _ SetDefinition _
 / _ Operation _
 / _ ProcessDefinition _
 / _ Comment _

/**
 * IDENTIFIERS 
 */

/* Attempts to parse and return a name node */
Identifier 'identifier'
 = UpperCaseIdentifier

/* Attempts to parse and return a variable reference */
Variable 'variable'
 = LowerCaseIdentifier

/* Attempts to parse and return a string beginning with an uppercase letter. */
UpperCaseIdentifier
 = $([A-Z][a-zA-Z0-9_]*)

/* Attempts to parse and return a string beginning with a lowercase letter. */    
LowerCaseIdentifier
 = label:$([a-z][a-zA-Z0-9_]*) {
    //isValidActionLabel(label);
    return label;
 }

/* Attempts to parse and return an integer value. */
IntegerLiteral = [-]?[0-9]+ {
    return parseInt(text(), 10);
}

/**
 * COMMENTS
 */

/* Attempts to parse and return either a single lined or multi-lined comment. */
Comment 
 = SingleLinedComment
 / MultiLinedComment

/* Attempts to parse and return a single lined comment. */
SingleLinedComment
 = '//' (!LineTerminator SourceCharacter)* {
    return text();
 }

/* Attempts to parse and return a multi-lined comment. */
MultiLinedComment 
 = '/*' (!'*/' SourceCharacter)* '*/' {
    return text();
 }

/* Attempts to parse a line terminator. */
LineTerminator
 = [\n\r\u2028\u2029]

/* Attempts to parse a source character. */
SourceCharacter
 = .

/**
 * ACTION LABELS
 */

/* Attempts to parse and return an action label. These are simple labels which can either
   be lower case identifiers or expressions. */
ActionLabel
 = label:LowerCaseIdentifier _ label2:(_ActionLabel ?) {
    return (label2 != null) ? label + label2 : label;
 }
 / '[' _ exp:Expression _ ']' _ label:(_ActionLabel ?) {
    exp = '[' + exp + ']';
    return (label != null) ? exp + label : exp;
 }

/* Helper function for 'ActionLabel' which removes left hand recursion from parsing. */ 
_ActionLabel
 = '.' _ label:LowerCaseIdentifier _ label2:(_ActionLabel ?) {
    label = '.' + label;
    return (label2 != null) ? label + label2 : label;
 }
 / '[' _ exp:Expression _ ']' _ label:(_ActionLabel ?) {
    exp = '[' + exp + ']'
    return (label != null) ? exp + label : exp;
 }

/* Attempts to parse and return a concatenation of action labels. Action labels also allow
   for sets and ranges to be defined. */
ActionLabels 'action label'
 = type:(ActionLabelType ?) _ label:ActionLabel _ label2:(_ActionLabels ?) {
    label = (type != null) ? type + label : label;
    return (label2 != null) ? label + label2 : label;
 }
 / type:(ActionLabelType ?) _ set:Set _ label:(_ActionLabels ?) {
    set = (type != null) ? type + set : set;
    return (label != null) ? set + label : set;
 }
 / type:(ActionLabelType ?) _ '[' _ range:ActionRange _ ']' _ label:(_ActionLabels ?) {
    range = '[' + range + ']';
    range = (type != null) ? type + range : range;
    return (label != null) ? range + label : range;
 }

/* Helper function for 'ActionLabels' which removes left hand recursion from parsing. */ 
_ActionLabels
 = '.' _ label:ActionLabel _ label2:(_ActionLabels ?) {
    label = '.' + label;
    return (label2 != null) ? label + label2 : label;
 }
 / '.' _ set:Set _ label:(_ActionLabels ?) {
    set = '.' + set;
    return (label != null) ? set + label : set;
 }
 / '[' _ range:ActionRange _ ']' _ label:(_ActionLabels ?) {
    range = '[' + range + ']';
    return (label != null) ? range + label : range;
 }
 / '[' _ exp:Expression _ ']' _ label:(_ActionLabels ?) {
    exp = '[' + exp + ']';
    return (label != null) ? exp + label : exp;
 }

ActionLabelType
 = '!'
 / '?'

/* Attempts to parse and return an action range from within an ActionLabel. */
ActionRange
 = Range
 / Set
 / variable:Variable _ ':' _ index:(Range / Set) {
    return variable + ':' + index;
 }

/* Attempts to parse and return a range definition. */
Range
 = ident:Identifier {
    return getConstant(ident);
 }
 / start:Expression _ '..' _ end:Expression {
    return start + '..' + end;
 }

/* Attempts to parse and return a set definition. */
Set 'set'
 = ident:Identifier {
    return getConstant(ident);
 }
 / '{' _ set:SetElements _ '}' {
    return '{' + set + '}'
 }

/* Attempts to parse set elements for a set definition. */
SetElements
 = label:ActionLabels _ elements:(_SetElements ?) {
    return (elements != null) ? label + elements : label;
 }

/* Helper function for 'SetElements' which removes left hand recursion from parsing. */ 
_SetElements
 = ',' _ label:ActionLabels _ elements:(_SetElements ?) {
    label = ',' + label;
    return (elements != null) ? label + elements : label;
 }
 
/**
 * CONST, RANGE AND SET
 */

/* Attempts to parse a global constant definition. */
ConstantDefinition
 = 'const' _ ident:Identifier _ '=' _ value:SimpleExpression {
    isValidIdentifier(ident);
    variableMap[ident] = value;
 }

/* Attempts to parse a global range definition. */
RangeDefinition
 = 'range' _ ident:Identifier _ '=' _ start:SimpleExpression _ '..' _ end:SimpleExpression {
    isValidIdentifier(ident);
    variableMap[ident] = start + '..' + end;
 }

/* Attempts to parse a global set definition. */
SetDefinition
 = 'set' _ ident:Identifier _ '=' _ '{' _ set:SetElements _ '}' {
    isValidIdentifier(ident);
    variableMap[ident] = '{' + set + '}';
 }

/**
 * PROCESS DEFINITION
 */

/* Attempts to parse and return a process definition. */
ProcessDefinition
 = ident:Identifier _ '=' _ body:ProcessBody _ relabel:(Relabel ?) _ hide:(Hiding ?) _ '.' {
    isValidIdentifier(ident);
    relabel = (relabel != null) ? relabel : '';
    hide = (hide != null) ? hide : '';
    
    var process = ident + '=' + body + relabel + hide + '.'
    var dependentOn = [];
    
    for(var i in dependencies){
        var prefix = i.split('[')[0];
        if(i != ident && localDependencies[prefix] != prefix){
            dependentOn.push(i);
        }
    }
    if(dependentOn.length == 0){
        processes[ident] = { process: process };
    }
    else{
        processes[ident] = { process: process, dependencies: dependentOn };
    }
    
    dependencies = {};
    localDependencies = {};
 }

/* Attempts to parse and return the body of a process definition. */
ProcessBody
 = process:LocalProcess _ ',' _ definitions:LocalProcessDefinitions {
    return process + ',' + definitions;
 }
 / LocalProcess

/* Attempts to parse and return locally defined process definitions. */ 
LocalProcessDefinitions
 = definition:LocalProcessDefinition _ definitions:(_LocalProcessDefinitions ?) {
    return (definitions != null) ? definition + definitions : definition;
 }

/* Helper function for 'LocalProcessDefinitions' which removes left hand recursion from parsing. */
_LocalProcessDefinitions
 = ',' _ definitions:LocalProcessDefinitions {
    return ',' + definitions;
 }

/* Attempts to parse and return a locally defined process definition. */
LocalProcessDefinition
 = ident:Identifier _  ranges:(IndexRanges ?) _ '=' _  process:LocalProcess {
    ranges = (ranges != null) ? ranges : '';
    localDependencies[ident] = ident;
    return ident + ranges + '=' + process;
 }

/* Attempts to parse and return a local process. */
LocalProcess
 = prefix:(PrefixLabel ?) _ process:_LocalProcess _ relabel:(Relabel ?) {
    if(prefix == null && relabel == null){
        return process;
    }
    
    prefix = (prefix != null) ? prefix : '';
    relabel = (relabel != null) ? relabel : '';
    
    return prefix + process + relabel;
 }

/* Helper function for 'LocalProcess' which removes left hand recursion from parsing. */
_LocalProcess
 = BaseLocalProcess
 / 'if' _ exp:Expression _ 'then' _ thenProcess:LocalProcess _ 'else' _ elseProcess:LocalProcess {
    return 'if ' + exp + ' then ' + thenProcess + ' else ' + elseProcess;
 }
 / 'if' _ exp:Expression _ 'then' _ thenProcess:LocalProcess {
    return 'if ' + exp + ' then ' + thenProcess;
 }
 / process:(Function / Choice / Composite) {
    return process;
 }
 / '(' _ process:(Function / Choice / Composite) _ ')' {
    return  '(' + process + ')';
 }

/* Attempts to parse and return a base local process. A base local process
   symbolises the end point of a local process.*/
BaseLocalProcess
 = 'STOP'
 / 'ERROR'
 / ident:Identifier _ indices:(Indices ?) {
    indices = (indices != null) ? indices : '';
    ident += indices;
    if(variableMap[ident] == undefined){
        dependencies[ident] = true; 
    }
    return ident;
 }

/* Attempts to parse and return a function process. */
Function
 = type:FunctionType _ '(' _ process:LocalProcess _ relabel:(Relabel ?) _ hide:(Hiding ?) _ ')' {
    relabel = (relabel != null) ? relabel : '';
    hide = (hide != null) ? hide : '';
    return type + '(' + process + relabel + hide + ')';
 }

/* Attempts to parse and return a function type. */
FunctionType
 = 'abs'
 / 'simp'
 // add more function types here

/* Attempts to parse and return a composite process. */
Composite
 = prefix:(PrefixLabel ?) _ composite:ParallelComposition _ relabel:(Relabel ?) {
    prefix = (prefix != null) ? prefix : '';
    relabel = (relabel != null) ? relabel : '';
    return prefix + '(' + composite + ')' + relabel;
 }
 / 'forall' _ ranges:Ranges _ composite:LocalProcess {
    return 'forall ' + ranges + ' ' + composite;
 }

/* Attempts to parse and return a prefix label. */
PrefixLabel
 = label:ActionLabels _ ':' {
    return label + ':';
 }

/* Attempts to parse and return a parallel composition process. */    
ParallelComposition
 = '(' _ process:LocalProcess _ parallel:(_ParallelComposition ?) _ ')' {
    return (parallel != null) ? process + parallel : process;
 }

/* Helper function for 'ParallelComposition' which removes left hand recursion from parsing. */
_ParallelComposition
 = '||' _ process:LocalProcess _ parallel:(_ParallelComposition ?) {
    process = '||' + process;
    return (parallel != null) ? process + parallel : process;
 }

/* Attempts to parse and return a choice process. */
Choice
 = prefix:ActionPrefix _  choice:(_Choice ?) {
    return (choice != null) ? prefix + choice : prefix;
 }

/* Helper function for 'Choice' which removes left hand recursion from parsing. */
_Choice
 = '|' _ prefix:ActionPrefix _  choice:(_Choice ?) {
    prefix = '|' + prefix;
    return (choice != null) ? prefix + choice : prefix;
 }

/* Attempts to parse an return an action prefix. */
ActionPrefix
 = guard:(Guard ?) _ prefix:PrefixActions {
    return (guard != null) ? guard + ' ' + prefix : prefix;
 }

/* Attempts to parse and return a prefix action. This is a sequence of action events. */        
PrefixActions
 = label:ActionLabels _ '->' _ process:LocalProcess {
    return label + '->' + process;
 }

/* Attempts to parse and return a guard. */
Guard
 = 'when' _ exp:Expression {
    return 'when ' + exp;
 }

/* Attempts to parse and return indices until there are no more to parse. */
Indices
 = '[' _ exp:Expression _ ']' _  indices:(Indices ?) {
    exp = '[' + exp + ']';
    return (indices != null) ? exp + indices : exp;
 }

/* Attempts to parse and return index ranges until there are no more to parse. */ 
IndexRanges
 = '[' _ exp:Expression _ ']' _ ranges:(IndexRanges ?) {
    exp = '[' + exp + ']';
    return (ranges != null) ? exp + ranges : exp;
 }
 / '[' _ range:ActionRange _ ']' _ ranges:(IndexRanges ?) {
    range = '[' + range + ']';
    return (ranges != null) ? range + ranges : range;
 }

/* Attempts to parse and return ranges until there are no more to parse. */
Ranges
 = '[' _ range:ActionRange _ ']' _ ranges:(Ranges ?) {
    range = '[' + range + ']';
    return (ranges != null) ? range + ranges : range;
 }

/**
 * OPERATIONS
 */

/* Attempts to parse and return an operation. */
Operation
 = process1:LocalProcess _ negated:('!' ?) operator:OperationOperator _ process2:LocalProcess _ '.' {
    operator = (negated != null) ? negated + operator : operator;
    var operation = process1 + operator + process2 + '.';
    
    var dependentOn = [];
    for(var i in dependencies){
        dependentOn.push(i);
    }
    if(dependentOn.length == 0){
        operations.push({ operation: operation });
    }
    else{
        operations.push({ operation: operation, dependencies: dependentOn });
        dependencies = {};
    }
 }

/* Attempts to parse and return an operation operator. */ 
OperationOperator
 = '~'
 // add more operation operators here

/**
 * RELABELLING AND HIDING
 */

/* Attempts to parse and return a relabelling process. */
Relabel
 = '/' _ '{' _ relabel:RelabelDefinitions _ '}' {
    return '/{' + relabel + '}';
 }

/* Attempts to parse and return a series of relabelling definitions */    
RelabelDefinitions
 = definition:RelabelDefinition _ definitions:(_RelabelDefinitions ?) {
    return (definitions != null) ? definition + definitions : definition;   
 }

/* Helper function for 'RelabelDefinitions' which removes left hand recursion from parsing. */
_RelabelDefinitions
 = ',' _ relabel:RelabelDefinitions {
    return ',' + relabel;   
 }

/* Attempts to parse and return a relabelling definition. */    
RelabelDefinition
 = newLabel:ActionLabels _ '/' _ oldLabel:ActionLabels {
    return newLabel + '/' + oldLabel;
 }
 / 'forall' _ ranges:IndexRanges _ '{' _  relabel:RelabelDefinitions _ '}' {
    return 'forall ' + ranges + '{' + relabel + '}';
 }

/* Attempts to parse a hiding process. */
Hiding
 = '\\' set:Set { // hides everything in the set
    return '\\' + set;
 }
 / '@' set:Set { // hides everything that is not in the set
    return '@' + set;
 }

/**
 * EXPRESIONS AND SIMPLE EXPRESSIONS
 */

/* Attempts to parse and return an expression. This includes expressions with boolean operators
   as well as arithmetic operations. */
Expression
 = base:BaseExpression _  op:Operator _ exp:Expression {
    return base + ' ' + op + ' ' + exp;
 }
 / BaseExpression

/* Attempts to parse and return the base of an expresion. This can be either an integer,
   a variable reference or another expression. */
BaseExpression
 = IntegerLiteral
 / Variable
 / ident:Identifier {
    isValidConstant(ident);
    return variableMap[ident];
 }
 / '(' _ exp:Expression _ ')' {
    return '( ' + exp + ' )';
 }

/* Attempts to parse and return a simple expression. This includes only arithmetic operations. */
SimpleExpression
 = base:SimpleBaseExpression _ op:ArithmeticOperator _ exp:SimpleExpression {
    return base + ' ' + op + ' ' + exp; 
 }
 / SimpleBaseExpression

/* Attempts to parse and return the base of a simple expresion. This can be either an integer,
   a variable reference or another simple expression. */
SimpleBaseExpression
 = IntegerLiteral
 / Variable
 / ident:Identifier {
    isValidConstant(ident);
    return variableMap[ident];
 }
 / '(' _ exp:SimpleExpression _ ')' {
    return '( ' + exp + ' )';
 }

/* Attempts to parse and return an operator. */
Operator
 = '||'
 / '&&'
 / '|'
 / '^'
 / '&'
 / '=='
 / '!='
 / '<<'
 / '>>'
 / '<='
 / '<'
 / '>='
 / '>'
 / ArithmeticOperator

/* Attempts to parse and return an arithmetic operator. */
ArithmeticOperator
 = '+'
 / '-'
 / '*'
 / '/'
 / '%'
 
/* Parses whitespace */
_ 'whitespace' = [ \t\n\r]*