{
    var keywords = ['const', 'range', 'set', 'if', 'then', 'else', 'when', 'forall', 'abs', 'simp'];
    
    var variableMap = {};
    var processes = {};
    var operations = [];
    
    var dependencies = {};
    
    function getConstant(ident){
        var variable = variableMap[ident];
        return (variable == undefined) ? ident : variable;
    }
    
    function isValidIdentifier(ident){
        if(variableMap[ident] != undefined || processes[ident] != undefined){
            error('The identifier \'' + ident + '\' has already been defined.');
        }
    }
    
    function isValidConstant(ident){
        if(variableMap[ident] == undefined){
            error('The identifier \'' + ident + '\' has not been defined.');
        }
    }
    
    function isValidActionLabel(label){
        for(var i = 0; i < keywords.length; i++){
            if(label == keywords[i]){
                error('Cannot use keyword \'' + keywords[i] + '\' as an action label.');
            }
        }
    }
}

//1. FSP Description

ParseTree = FiniteStateProcesses* {
    return {
        processes: processes,
        operations: operations
    };
}

FiniteStateProcesses
 = _ ConstantDefinition _
 / _ RangeDefinition _
 / _ SetDefinition _
 / _ Operation _
 / _ ProcessDefinition _
 / _ Comment _

//2. Identifiers

Identifier 'identifier'
 = UpperCaseIdentifier

Variable 'variable'
 = LowerCaseIdentifier

UpperCaseIdentifier
 = $([A-Z][a-zA-Z0-9_]*)
    
LowerCaseIdentifier
 = label:$([a-z][a-zA-Z0-9_]*) {
    //isValidActionLabel(label);
    return label;
 }

IntegerLiteral = [-]?[0-9]+ {
    return parseInt(text(), 10);
}

// Comments

Comment 
 = SingleLinedComment
 / MultiLinedComment

SingleLinedComment
 = '//' (!LineTerminator SourceCharacter)* {
    return text();
 }

MultiLinedComment 
 = '/*' (!'*/' SourceCharacter)* '*/' {
    return text();
 }

LineTerminator
 = [\n\r\u2028\u2029]

SourceCharacter
 = .

//3. Action Labels

ActionLabel
 = label:LowerCaseIdentifier _ label2:(_ActionLabel ?) {
    return (label2 != null) ? label + label2 : label;
 }
 / '[' _ exp:Expression _ ']' _ label:(_ActionLabel ?) {
    exp = '[' + exp + ']';
    return (label != null) ? exp + label : exp;
 }
 
_ActionLabel
 = '.' _ label:LowerCaseIdentifier _ label2:(_ActionLabel ?) {
    label = '.' + label;
    return (label2 != null) ? label + label2 : label;
 }
 / '[' _ exp:Expression _ ']' _ label:(_ActionLabel ?) {
    exp = '[' + exp + ']'
    return (label != null) ? exp + label : exp;
 }

ActionLabels 'action label'
 = label:ActionLabel _ label2:(_ActionLabels ?) {
    return (label2 != null) ? label + label2 : label;
 }
 / set:Set _ label:(_ActionLabels ?) {
    return (label != null) ? set + label : set;
 }
 / '[' _ range:ActionRange _ ']' _ label:(_ActionLabels ?) {
    range = '[' + range + ']';
    return (label != null) ? range + label : range;
 }
 
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

ActionRange
 = Range
 / Set
 / variable:Variable _ ':' _ index:(Range / Set) {
    return variable + ':' + index;
 }

Range
 = ident:Identifier {
    return getConstant(ident);
 }
 / start:Expression _ '..' _ end:Expression {
    return start + '..' + end;
 }

Set 'set'
 = ident:Identifier {
    return getConstant(ident);
 }
 / '{' _ set:SetElements _ '}' {
    return '{' + set + '}'
 }

SetElements
 = label:ActionLabels _ elements:(_SetElements ?) {
    return (elements != null) ? label + elements : label;
 }
 
_SetElements
 = ',' _ label:ActionLabels _ elements:(_SetElements ?) {
    label = ',' + label;
    return (elements != null) ? label + elements : label;
 }
 
//4. const, range, set

ConstantDefinition
 = 'const' _ ident:Identifier _ '=' _ value:SimpleExpression {
    isValidIdentifier(ident);
    variableMap[ident] = value;
 }

RangeDefinition
 = 'range' _ ident:Identifier _ '=' _ start:SimpleExpression _ '..' _ end:SimpleExpression {
    isValidIdentifier(ident);
    variableMap[ident] = start + '..' + end;
 }

SetDefinition
 = 'set' _ ident:Identifier _ '=' _ '{' _ set:SetElements _ '}' {
    isValidIdentifier(ident);
    variableMap[ident] = '{' + set + '}';
 }

//5. Process Definition

ProcessDefinition
 = ident:Identifier _ '=' _ body:ProcessBody _ relabel:(Relabel ?) _ hide:(Hiding ?) _ '.' {
    isValidIdentifier(ident);
    relabel = (relabel != null) ? relabel : '';
    hide = (hide != null) ? hide : '';
    
    var process = ident + '=' + body + relabel + hide + '.'
    var dependentOn = [];
    for(var i in dependencies){
        if(i != ident){
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
 }

ProcessBody
 = process:LocalProcess _ ',' _ definitions:LocalProcessDefinitions {
    return process + ',' + definitions;
 }
 / LocalProcess
 
LocalProcessDefinitions
 = definition:LocalProcessDefinition _ definitions:(_LocalProcessDefinitions ?) {
    return (definitions != null) ? definition + definitions : definition;
 }

_LocalProcessDefinitions
 = ',' _ definitions:LocalProcessDefinitions {
    return ',' + definitions;
 }

LocalProcessDefinition
 = ident:Identifier _  ranges:(IndexRanges ?) _ '=' _  process:LocalProcess {
    ranges = (ranges != null) ? ranges : '';
    return ident + ranges + '=' + process;
 }

LocalProcess
 = prefix:(PrefixLabel ?) _ process:_LocalProcess _ relabel:(Relabel ?) {
    if(prefix == null && relabel == null){
        return process;
    }
    
    prefix = (prefix != null) ? prefix : '';
    relabel = (relabel != null) ? relabel : '';
    
    return prefix + process + relabel;
 }

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

Function
 = type:FunctionType _ '(' _ process:LocalProcess _ relabel:(Relabel ?) _ hide:(Hiding ?) _ ')' {
    relabel = (relabel != null) ? relabel : '';
    hide = (hide != null) ? hide : '';
    return type + '(' + process + relabel + hide + ')';
 }

FunctionType
 = 'abs'
 / 'simp'

Composite
 = prefix:(PrefixLabel ?) _ composite:ParallelComposition _ relabel:(Relabel ?) {
    prefix = (prefix != null) ? prefix : '';
    relabel = (relabel != null) ? relabel : '';
    return prefix + '(' + composite + ')' + relabel;
 }
 / 'forall' _ ranges:Ranges _ composite:LocalProcess {
    return 'forall ' + ranges + ' ' + composite;
 }

PrefixLabel
 = label:ActionLabels _ ':' {
    return label + ':';
 }
    
ParallelComposition
 = '(' _ process:LocalProcess _ parallel:(_ParallelComposition ?) _ ')' {
    return (parallel != null) ? process + parallel : process;
 }

_ParallelComposition
 = '||' _ process:LocalProcess _ parallel:(_ParallelComposition ?) {
    process = '||' + process;
    return (parallel != null) ? process + parallel : process;
 }

Choice
 = prefix:ActionPrefix _  choice:(_Choice ?) {
    return (choice != null) ? prefix + choice : prefix;
 }

_Choice
 = '|' _ prefix:ActionPrefix _  choice:(_Choice ?) {
    prefix = '|' + prefix;
    return (choice != null) ? prefix + choice : prefix;
 }

ActionPrefix
 = guard:(Guard ?) _ prefix:PrefixActions {
    return (guard != null) ? guard + ' ' + prefix : prefix;
 }
        
PrefixActions
 = label:ActionLabels _ '->' _ process:LocalProcess {
    return label + '->' + process;
 }

Guard
 = 'when' _ exp:Expression {
    return 'when ' + exp;
 }

Indices
 = '[' _ exp:Expression _ ']' _  indices:(Indices ?) {
    exp = '[' + exp + ']';
    return (indices != null) ? exp + indices : exp;
 }
 
IndexRanges
 = '[' _ exp:Expression _ ']' _ ranges:(IndexRanges ?) {
    exp = '[' + exp + ']';
    return (ranges != null) ? exp + ranges : exp;
 }
 / '[' _ range:ActionRange _ ']' _ ranges:(IndexRanges ?) {
    range = '[' + range + ']';
    return (ranges != null) ? range + ranges : range;
 }

Ranges
 = '[' _ range:ActionRange _ ']' _ ranges:(Ranges ?) {
    range = '[' + range + ']';
    return (ranges != null) ? range + ranges : range;
 }

// Operations

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
 
OperationOperator
 = '~'

//6. Relabelling and Hiding

Relabel
 = '/' _ '{' _ relabel:RelabelDefinitions _ '}' {
    return '/{' + relabel + '}';
 }
    
RelabelDefinitions
 = definition:RelabelDefinition _ definitions:(_RelabelDefinitions ?) {
    return (definitions != null) ? definition + definitions : definition;   
 }

_RelabelDefinitions
 = ',' _ relabel:RelabelDefinitions {
    return ',' + relabel;   
 }
    
RelabelDefinition
 = newLabel:ActionLabels _ '/' _ oldLabel:ActionLabels {
    return newLabel + '/' + oldLabel;
 }
 / 'forall' _ ranges:IndexRanges _ '{' _  relabel:RelabelDefinitions _ '}' {
    return 'forall ' + ranges + '{' + relabel + '}';
 }

Hiding
 = '\\' set:Set { // hides everything in the set
    return '\\' + set;
 }
 / '@' set:Set { // hides everything that is not in the set
    return '@' + set;
 }

 //7. Expressions and Simple Expressions

Expression
 = base:BaseExpression _  op:Operator _ exp:Expression {
    return base + ' ' + op + ' ' + exp;
 }
 / BaseExpression

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

SimpleExpression
 = base:SimpleBaseExpression _ op:SimpleOperator _ exp:SimpleExpression {
    return base + ' ' + op + ' ' + exp; 
 }
 / SimpleBaseExpression

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
 / SimpleOperator

SimpleOperator
 = '+'
 / '-'
 / '*'
 / '/'
 / '%'
 
/* Parses whitespace */
_ 'whitespace' = [ \t\n\r]*