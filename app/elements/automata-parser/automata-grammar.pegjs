{
    var Node = {
        NameNode: function(name){
            this.type = 'name';
            this.name = name;
        },
        ActionNode: function(action, indices){
            this.type = 'action';
            this.action = action;
            if(indices.length > 0){ this.indices = indices; }
        },
        RangeNode: function(start, end){
            this.type = 'range';
            this.start = start;
            this.end = end;
        },
        SetNode: function(set){
            this.type = 'set';
            this.set = set;
        },
        ModelNode: function(definitions){
            this.type = 'model';
            this.definitions = definitions;
        },
        DefinitionNode: function(name, process, relabel, hidden, isVisible){
            this.type = "definition";
            this.name = name;
            this.process = process;
            if(relabel != null){ this.relabel = relabel; }
            if(hidden != null){ this.hidden = hidden; }
            if(isVisible != null){ this.isVisible = isVisible; }
        },
        SequenceNode: function(from, to){
            this.type = 'sequence';
            this.from = from;
            this.to = to;
        },
        IfNode:function(guard, thenProcess, elseProcess){
            this.type = 'if-statement';
            this.guard = guard;
            this.thenProcess = thenProcess;
            if(elseProcess != null){ this.elseProcess = elseProcess; }
        },
        CompositeNode: function(label, composite, relabel){
            this.type = 'composite';
            if(label != null){ this.label = label; }
            this.composite = composite;
            if(relabel != null){ this.relabel = relabel; }
        },
        ParallelNode: function(process1, process2){
            this.type = 'parallel';
            this.process1 = process1;
            this.process2 = process2;
        },
        ChoiceNode: function(option1, option2){
            this.type = 'choice';
            this.option1 = option1;
            this.option2 = option2;
        },
        FunctionNode: function(func, process, relabel, hidden){
            this.type = 'function';
            this.func = func;
            this.process = process;
            if(relabel != null){ this.relabel = relabel; }
            if(hidden != null){ this.hidden = hidden; }
        },
        IndexNode: function(variable, index, process){
            this.type = 'index';
            this.variable = variable;
            this.index = index;
            this.process = process;
        },
        TerminalNode: function(type){
            this.type = type;
        },
        OperationNode: function(operator, process1, process2, isNegated, input){
            this.type = 'operation';
            this.operator = operator;
            this.process1 = process1;
            this.process2 = process2;
            this.isNegated = isNegated;
            this.input = text().trim();
        }
    }
    
    var localDefinitions = [];
    var variableMap = {};
    var variableCount = 0;
    var actionIndices = [];
    var identifierIndices = [];
    var forallIndices = [];
    
    function processIndex(index, type){
        if(index.variable == undefined){
            index.variable = getNextVariable();
        }
        
        if(type == 'action'){
            actionIndices.push(index);
        }
        else if(type == 'identifier'){
            identifierIndices.push(index);
        }
        else if(type == 'forall'){
            forallIndices.push(index);
        }
   
        return index.variable;
    }
    
    function constructIndexNode(indices, process){
        if(indices != undefined){
            for(var i = 0; i < indices.length; i++){
                var index = indices[i];
                process = new Node.IndexNode(index.variable, index.index, process);
            }
        }

        return process;    
    }
    
    function constructCompositeNode(prefix, composite, relabel){
        if(prefix == null){
            return new Node.CompositeNode(prefix, composite, relabel);
        }
      
        var indices = prefix.indices;
        delete prefix.indices;
        var node = new Node.CompositeNode(prefix, composite, relabel);
        return constructIndexNode(indices, node);
    }
    
    function constructExpression(exp){
        if(typeof(exp) == 'number'){
            return exp;
        }

        if(!exp.includes(' ')){
            return exp;
        }
        var variable = getNextVariable();
        variableMap[variable] = exp;
        return variable;
    }
    
    function getNextVariable(){
        return '$v<' + variableCount++ + '>';
    }
}

ParseTree = processes:ParseTreeProcesses* {
    return {
        processes: processes,
        variableMap: variableMap
    };
}

ParseTreeProcesses
 = _ process:ProcessDefinition _ {
    return process;
 }
 / _ operation:Operation _ {
    return operation;
 }

/* 1. Identifiers */

Identifier
 = ident:UpperCaseIdentifier {
    return new Node.NameNode(ident);
 }

Variable
 = variable:LowerCaseIdentifier {
    return '$' + variable;
 }

UpperCaseIdentifier
 = $([A-Z][a-zA-Z0-9_]*)
    
LowerCaseIdentifier
 = $([a-z][a-zA-Z0-9_]*)

IntegerLiteral = [-]?[0-9]+ {
    return parseInt(text(), 10);
}

/* 2. Action Labels */

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

ActionLabels
 = label:ActionLabel _ label2:(_ActionLabels ?) {
    label = (label2 != null) ? label + label2 : label;
    var indices = actionIndices;
    actionIndices = [];
    return new Node.ActionNode(label, indices);
 }
 / set:ActionSet _ label:(_ActionLabels ?) {
    set = processIndex(set, 'action');
    set = (label != null) ? set + label : set;
    var indices = actionIndices;
    actionIndices = [];
    return new Node.ActionNode(set, indices);
 }
 / '[' _ range:ActionRange _ ']' _ label:(_ActionLabels ?) {
    range = '[' + processIndex(range, 'action') + ']';
    range = (label != null) ? range + label : range;
    var indices = actionIndices;
    actionIndices = [];
    return new Node.ActionNode(range, indices);
 }
 
_ActionLabels
 = '.' _ label:ActionLabel _ label2:(_ActionLabels ?) {
    label = '.' + label;
    return (label2 != null) ? label + label2 : label;
 }
 / '.' _ set:ActionSet _ label:(_ActionLabels ?) {
    set = '.' + processIndex(set, 'action');
    return (label != null) ? set + label : set;
 }
 / '[' _ range:ActionRange _ ']' _ label:(_ActionLabels ?) {
    range = '[' + processIndex(range, 'action') + ']';
    return (label != null) ? range + label : range;
 }
 / '[' _ exp:Expression _ ']' _ label:(_ActionLabels ?) {
    exp = '[' + exp + ']';
    return (label != null) ? exp + label : exp;
 }

ActionSet
 = index:Set {
    return { index: index };
 }

ActionRange
 = index:(Range / Set) {
    return { index: index };
 }
 / variable:Variable _ ':' _ index:(Range / Set) {
    return { variable: variable, index: index };
 }

Range
 = start:Expression _ '..' _ end:Expression {
    return new Node.RangeNode(start, end);
 }

Set
 = '{' _ set:SetElements _ '}' {
    return new Node.SetNode(set);
 }

SetElements
 = label:ActionLabels _ elements:(_SetElements ?) {
    return (elements != null) ? [label.action].concat(elements) : [label.action];
 }
 
_SetElements
 = ',' _ label:ActionLabels _ elements:(_SetElements ?) {
    return (elements != null) ? [label.action].concat(elements) : label.action;
 }

/* 3. Process Definition */

ProcessDefinition
 = ident:Identifier _ '=' _ body:ProcessBody _ relabel:(Relabel ?) _ hide:(Hiding ?) _ '.' {
    var node = new Node.DefinitionNode(ident, body, relabel, hide, true);
    var definitions = localDefinitions;
    definitions.unshift(node);
    localDefinitions = [];
    return new Node.ModelNode(definitions);
 }

ProcessBody
 = process:LocalProcess _ ((',' _ definitions:LocalProcessDefinitions { localDefinitions = definitions }) ?) {
    return process;
 }
 
LocalProcessDefinitions
 = definition:LocalProcessDefinition _ definitions:(_LocalProcessDefinitions ?) {
    return (definitions != null) ? [definition].concat(definitions) : [definition];
 }

_LocalProcessDefinitions
 = ',' _ definitions:LocalProcessDefinitions {
   return definitions;
 }

LocalProcessDefinition
 = ident:Identifier _  ranges:(IndexRanges ?) _  '=' _  process:LocalProcess {
    if(ranges != null){
        ident.name += ranges;
        ident = constructIndexNode(identifierIndices, ident);
        identifierIndices = [];
    }
    return new Node.DefinitionNode(ident, process);
 }

LocalProcess
 = prefix:(PrefixLabel ?) _ process:_LocalProcess _ relabel:(Relabel ?) {
    if(prefix == null && relabel == null){
        return process;
    }
    
    return new Node.CompositeNode(prefix, process, relabel);
 }

_LocalProcess
 = BaseLocalProcess
 / 'if' _ exp:Expression _ 'then' _ thenProcess:LocalProcess _ 'else' _ elseProcess:LocalProcess {
    return new Node.IfNode(exp, thenProcess, elseProcess);
 }
 / 'if' _ exp:Expression _ 'then' _ thenProcess:LocalProcess {
    return new Node.IfNode(exp, thenProcess);
 }
 / 'forall' _ ranges:Ranges _ composite:LocalProcess {
    var indices = forallIndices;
    forallIndices = [];
    return constructIndexNode(indices, composite);
 }
 / process:(Function / Composite / Choice) {
    return process;
 }
 / '(' _ process:(Function / Composite / Choice) _ ')' {
    return process;
 }

BaseLocalProcess
 = 'STOP' {
    return new Node.TerminalNode('stop');
 }
 / 'ERROR' {
    return new Node.TerminalNode('error');
 }
 / ident:Identifier _ indices:(Indices ?) {
    if(indices != null){
        ident.name += indices;
    }
    return ident;
 }

Function
 = type:FunctionType _ '(' _ process:LocalProcess _ relabel:(Relabel ?) _ hide:(Hiding ?)  _ ')' {
    return new Node.FunctionNode(type, process, relabel, hide);
 }

FunctionType
 = 'abs' {
    return 'abstraction';
 }
 / 'simp' {
    return 'simplification';
 }

Composite
 = prefix:(PrefixLabel ?) _ parallel:ParallelComposition _ relabel:(Relabel ?) {
    if(prefix == null && relabel == null){
        return parallel;
    }
    return constructCompositeNode(prefix, parallel, relabel);
 }

PrefixLabel
 = label:ActionLabels _ ':' {
    return label;
 }
    
ParallelComposition
 = '(' _ process:LocalProcess _ parallel:(_ParallelComposition ?) _ ')' {
    return (parallel != null) ? new Node.ParallelNode(process, parallel) : process;
 }

_ParallelComposition
 = '||' _ process:LocalProcess _ parallel:(_ParallelComposition ?) {
    return (parallel != null) ? new Node.ParallelNode(process, parallel) : process;
 }

Choice
 = prefix:ActionPrefix _  choice:(_Choice ?) {
    return (choice != null) ? new Node.ChoiceNode(prefix, choice) : prefix;
 }

_Choice
 = '|' _ prefix:ActionPrefix _  choice:(_Choice ?) {
    return (choice != null) ? new Node.ChoiceNode(prefix, choice) : prefix;
 }

ActionPrefix
 = guard:(Guard ?) _ prefix:PrefixActions {
    return (guard != null) ? new Node.IfNode(guard, prefix) : prefix;
 }
        
PrefixActions
 = label:ActionLabels _ '->' _ process:(PrefixActions / LocalProcess) {
    var indices = label.indices;
    delete label.indices;
    var node = new Node.SequenceNode(label, process);
    return constructIndexNode(indices, node);
 }

Guard
 = 'when' _ exp:Expression {
    return exp;
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
    range = processIndex(range, 'identifier');
    range = '[' + range + ']';
    return (ranges != null) ? range + ranges : range;
 }

Ranges
 = '[' _ range:ActionRange _ ']' _ ranges:(Ranges ?) {
    range = processIndex(range, 'forall');
    range = '[' + range + ']';
    return (ranges != null) ? range + ranges : range;
 }

// Operations

Operation
 = process1:LocalProcess _ negated:('!' ?) operator:OperationOperator _ process2:LocalProcess _ '.' {
    negated = (negated != null) ? true : false;
    return new Node.OperationNode(operator, process1, process2, negated);
 }
 
OperationOperator
 = '~' {
    return 'bisimulation';
 }

/* 4. Relabelling and Hiding */

Relabel
 = '/' _ '{' _ relabel:RelabelDefinitions _ '}' {
    return relabel;
 }
    
RelabelDefinitions
 = definition:RelabelDefinition _ definitions:(_RelabelDefinitions ?) {
    return (definitions != null) ? [definition].concat(definitions) : [definition];   
 }

_RelabelDefinitions
 = ',' _ relabel:RelabelDefinitions {
    return relabel   
 }
    
RelabelDefinition
 = newLabel:ActionLabels _ '/' _ oldLabel:ActionLabels {
    return { newLabel: newLabel.action, oldLabel: oldLabel.action };
 }
 / 'forall' _ ranges:IndexRanges _ '{' _  relabel:RelabelDefinitions _ '}' {
    return 'forall ' + ranges + '{' + relabel + '}';
 }

Hiding
 = '\\' set:Set {
    return { type: 'includes', set: set.set };
 }
 / '@' set:Set {
    return { type: 'excludes', set: set.set };
 }

/* 5. Expressions and Simple Expressions */

Expression
 = exp:_Expression {
    return constructExpression(exp);
 }

_Expression
 = base:BaseExpression _  op:Operator _ exp:Expression {
    return base + ' ' + op + ' ' + exp;
 }
 / BaseExpression

BaseExpression
 = IntegerLiteral
 / Variable
 / '(' _ exp:_Expression _ ')' {
    return '( ' + exp + ' )';
 }

SimpleExpression
 = exp:_SimpleExpression {
    return contructExpression(exp);
 }

_SimpleExpression
 = base:SimpleBaseExpression op:SimpleOperator exp:SimpleExpression {
    return base + ' ' + op + ' ' + exp; 
 }
 / SimpleBaseExpression

SimpleBaseExpression
 = IntegerLiteral
 / Variable
 / '(' _ exp:_SimpleExpression _ ')' {
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