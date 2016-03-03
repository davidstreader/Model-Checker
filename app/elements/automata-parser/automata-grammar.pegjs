{
    /* JavaScript Objects that are constructed through building the parse tree. */
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
    
    // variables used while parsing
    var localDefinitions = [];
    var variableMap = {};
    var variableCount = 0;
    var actionIndices = [];
    var identifierIndices = [];
    var forallIndices = [];
    
    /* Processes the specified index accordingly based on the type of index it is. */
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
    
    /**
     * Constructs and returns an index node.
     */
    function constructIndexNode(indices, process){
        if(indices != undefined){
            for(var i = 0; i < indices.length; i++){
                var index = indices[i];
                process = new Node.IndexNode(index.variable, index.index, process);
            }
        }

        return process;    
    }
    
    /**
     * Constructs and returns a composite node. If indices have been defined then wraps
     * the composite node within an index node.
     */
    function constructCompositeNode(prefix, composite, relabel){
        if(prefix == null){
            return new Node.CompositeNode(prefix, composite, relabel);
        }
      
        var indices = prefix.indices;
        delete prefix.indices;
        var node = new Node.CompositeNode(prefix, composite, relabel);
        return constructIndexNode(indices, node);
    }
    
    /** 
     * Constructs and returns an expression. Stores expressions in the
     * variable map and constructs and returns a variable name if necessary.
     */
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
    
    /**
     * Returns the next variable name in the sequence.
     */
    function getNextVariable(){
        return '$v<' + variableCount++ + '>';
    }
    
    /**
     * Process the specifiec set elements to determine whether any of the
     * elements require indexing.
     */
    function processSetElements(label, elements){
      var indices = label.indices;

      // if no index is defined just return the action label
      if(indices == undefined){
          return (elements != null) ? [label.action].concat(elements) : [label.action];
      }

      // otherwise construct and return an index node
      delete label.indices;
      var node = constructIndexNode(indices, label.action);
      return (elements != null) ? [node].concat(elements) : [node];
    }
}

/* Constructs and returns a parse try to be passed to the interpreter. */
ParseTree = processes:ParseTreeProcesses* {
    return {
        processes: processes,
        variableMap: variableMap
    };
}

/* Attempts to parse and return a either a process definition or an operation
   for the parse tree. */
ParseTreeProcesses
 = _ process:ProcessDefinition _ {
    return process;
 }
 / _ operation:Operation _ {
    return operation;
 }

/**
 * IDENTIFIERS 
 */

/* Attempts to parse and return a name node */
Identifier
 = ident:UpperCaseIdentifier {
    return new Node.NameNode(ident);
 }

/* Attempts to parse and return a variable reference */
Variable
 = variable:LowerCaseIdentifier {
    return '$' + variable;
 }

/* Attempts to parse and return a string beginning with an uppercase letter. */
UpperCaseIdentifier
 = $([A-Z][a-zA-Z0-9_]*)

/* Attempts to parse and return a string beginning with a lowercase letter. */
LowerCaseIdentifier
 = $([a-z][a-zA-Z0-9_]*)

/* Attempts to parse and return an integer value. */
IntegerLiteral = [-]?[0-9]+ {
    return parseInt(text(), 10);
}

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

/* Helper function for 'ActionLabels' which removes left hand recursion from parsing. */
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

/* Attempts to parse and return a set defined within an ActionLabel. */
ActionSet
 = index:Set {
    return { index: index };
 }

/* Attempts to parse and return an action range from within an ActionLabel. */
ActionRange
 = index:(Range / Set) {
    return { index: index };
 }
 / variable:Variable _ ':' _ index:(Range / Set) {
    return { variable: variable, index: index };
 }

/* Attempts to parse and return a range definition. */
Range
 = start:Expression _ '..' _ end:Expression {
    return new Node.RangeNode(start, end);
 }

/* Attempts to parse and return a set definition. */
Set
 = '{' _ set:SetElements _ '}' {
    return new Node.SetNode(set);
 }

/* Attempts to parse set elements for a set definition. */
SetElements
 = label:ActionLabels _ elements:(_SetElements ?) {
    return processSetElements(label, elements);
 }

/* Helper function for 'SetElements' which removes left hand recursion from parsing. */
_SetElements
 = ',' _ label:ActionLabels _ elements:(_SetElements ?) {
    return processSetElements(label, elements);
 }

/**
 * PROCESS DEFINITION
 */

/* Attempts to parse and return a process definition. */
ProcessDefinition
 = ident:Identifier _ '=' _ body:ProcessBody _ relabel:(Relabel ?) _ hide:(Hiding ?) _ '.' {
    var node = new Node.DefinitionNode(ident, body, relabel, hide, true);
    var definitions = localDefinitions;
    definitions.unshift(node);
    localDefinitions = [];
    return new Node.ModelNode(definitions);
 }

/* Attempts to parse and return the body of a process definition. */
ProcessBody
 = process:LocalProcess _ ((',' _ definitions:LocalProcessDefinitions { localDefinitions = definitions }) ?) {
    return process;
 }

/* Attempts to parse and return locally defined process definitions. */
LocalProcessDefinitions
 = definition:LocalProcessDefinition _ definitions:(_LocalProcessDefinitions ?) {
    return (definitions != null) ? [definition].concat(definitions) : [definition];
 }

/* Helper function for 'LocalProcessDefinitions' which removes left hand recursion from parsing. */
_LocalProcessDefinitions
 = ',' _ definitions:LocalProcessDefinitions {
   return definitions;
 }

/* Attempts to parse and return a locally defined process definition. */
LocalProcessDefinition
 = ident:Identifier _  ranges:(IndexRanges ?) _  '=' _  process:LocalProcess {
    if(ranges != null){
        ident.name += ranges;
        ident = constructIndexNode(identifierIndices, ident);
        identifierIndices = [];
    }
    return new Node.DefinitionNode(ident, process);
 }

/* Attempts to parse and return a local process. */
LocalProcess
 = prefix:(PrefixLabel ?) _ process:_LocalProcess _ relabel:(Relabel ?) {
    if(prefix == null && relabel == null){
        return process;
    }
    
    return new Node.CompositeNode(prefix, process, relabel);
 }

/* Helper function for 'LocalProcess' which removes left hand recursion from parsing. */
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

/* Attempts to parse and return a base local process. A base local process
   symbolises the end point of a local process.*/
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

/* Attempts to parse and return a function process. */
Function
 = type:FunctionType _ '(' _ process:LocalProcess _ relabel:(Relabel ?) _ hide:(Hiding ?)  _ ')' {
    return new Node.FunctionNode(type, process, relabel, hide);
 }

/* Attempts to parse and return a function type. */
FunctionType
 = 'abs' {
    return 'abstraction';
 }
 / 'simp' {
    return 'simplification';
 }
 // add more function types here

/* Attempts to parse and return a composite process. */
Composite
 = prefix:(PrefixLabel ?) _ parallel:ParallelComposition _ relabel:(Relabel ?) {
    if(prefix == null && relabel == null){
        return parallel;
    }
    return constructCompositeNode(prefix, parallel, relabel);
 }

/* Attempts to parse and return a prefix label. */
PrefixLabel
 = label:ActionLabels _ ':' {
    return label;
 }

/* Attempts to parse and return a parallel composition process. */
ParallelComposition
 = '(' _ process:LocalProcess _ parallel:(_ParallelComposition ?) _ ')' {
    return (parallel != null) ? new Node.ParallelNode(process, parallel) : process;
 }

/* Helper function for 'ParallelComposition' which removes left hand recursion from parsing. */
_ParallelComposition
 = '||' _ process:LocalProcess _ parallel:(_ParallelComposition ?) {
    return (parallel != null) ? new Node.ParallelNode(process, parallel) : process;
 }

/* Attempts to parse and return a choice process. */
Choice
 = prefix:ActionPrefix _  choice:(_Choice ?) {
    return (choice != null) ? new Node.ChoiceNode(prefix, choice) : prefix;
 }

/* Helper function for 'Choice' which removes left hand recursion from parsing. */
_Choice
 = '|' _ prefix:ActionPrefix _  choice:(_Choice ?) {
    return (choice != null) ? new Node.ChoiceNode(prefix, choice) : prefix;
 }

/* Attempts to parse an return an action prefix. */
ActionPrefix
 = guard:(Guard ?) _ prefix:PrefixActions {
    return (guard != null) ? new Node.IfNode(guard, prefix) : prefix;
 }

/* Attempts to parse and return a prefix action. This is a sequence of action events. */
PrefixActions
 = label:ActionLabels _ '->' _ process:(PrefixActions / LocalProcess) {
    var indices = label.indices;
    delete label.indices;
    var node = new Node.SequenceNode(label, process);
    return constructIndexNode(indices, node);
 }

/* Attempts to parse and return a guard. */
Guard
 = 'when' _ exp:Expression {
    return exp;
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
    range = processIndex(range, 'identifier');
    range = '[' + range + ']';
    return (ranges != null) ? range + ranges : range;
 }

/* Attempts to parse and return ranges until there are no more to parse. */
Ranges
 = '[' _ range:ActionRange _ ']' _ ranges:(Ranges ?) {
    range = processIndex(range, 'forall');
    range = '[' + range + ']';
    return (ranges != null) ? range + ranges : range;
 }

/**
 * OPERATIONS
 */

/* Attempts to parse and return an operation. */
Operation
 = process1:LocalProcess _ negated:('!' ?) operator:OperationOperator _ process2:LocalProcess _ '.' {
    negated = (negated != null) ? true : false;
    return new Node.OperationNode(operator, process1, process2, negated);
 }

/* Attempts to parse and return an operation operator. */
OperationOperator
 = '~' {
    return 'bisimulation';
 }
 // add more operation operators here

/**
 * RELABELLING AND HIDING
 */

/* Attempts to parse and return a relabelling process. */
Relabel
 = '/' _ '{' _ relabel:RelabelDefinitions _ '}' {
    return relabel;
 }

/* Attempts to parse and return a series of relabelling definitions */
RelabelDefinitions
 = definition:RelabelDefinition _ definitions:(_RelabelDefinitions ?) {
    return (definitions != null) ? [definition].concat(definitions) : [definition];   
 }

/* Helper function for 'RelabelDefinitions' which removes left hand recursion from parsing. */
_RelabelDefinitions
 = ',' _ relabel:RelabelDefinitions {
    return relabel   
 }
 
/* Attempts to parse and return a relabelling definition. */
RelabelDefinition
 = newLabel:ActionLabels _ '/' _ oldLabel:ActionLabels {
    return { newLabel: newLabel.action, oldLabel: oldLabel.action };
 }
 / 'forall' _ ranges:IndexRanges _ '{' _  relabel:RelabelDefinitions _ '}' {
    return 'forall ' + ranges + '{' + relabel + '}';
 }

/* Attempts to parse a hiding process. */
Hiding
 = '\\' set:Set { // inclusive hiding: hides every action label within the set
    return { type: 'includes', set: set.set };
 }
 / '@' set:Set { // exclusive hiding: hides every action label not within the set
    return { type: 'excludes', set: set.set };
 }

/**
 * EXPRESIONS AND SIMPLE EXPRESSIONS
 */

/* Attempts to parse and return an expression. This includes expressions with boolean operators
   as well as arithmetic operations. */
Expression
 = exp:_Expression {
    return constructExpression(exp);
 }

/* Helper function for 'Expression' which removes left hand recursion from parsing. */
_Expression
 = base:BaseExpression _  op:Operator _ exp:Expression {
    return base + ' ' + op + ' ' + exp;
 }
 / BaseExpression

/* Attempts to parse and return the base of an expresion. This can be either an integer,
   a variable reference or another expression. */
BaseExpression
 = IntegerLiteral
 / Variable
 / '(' _ exp:_Expression _ ')' {
    return '( ' + exp + ' )';
 }

/* Attempts to parse and return a simple expression. This includes only arithmetic operations. */
SimpleExpression
 = exp:_SimpleExpression {
    return contructExpression(exp);
 }

/* Helper function for 'SimpleExpression' which removes left hand recursion from parsing. */
_SimpleExpression
 = base:SimpleBaseExpression op:ArithmeticOperator exp:SimpleExpression {
    return base + ' ' + op + ' ' + exp; 
 }
 / SimpleBaseExpression

/* Attempts to parse and return the base of an expresion. This can be either an integer,
   a variable reference or another simple expression. */
SimpleBaseExpression
 = IntegerLiteral
 / Variable
 / '(' _ exp:_SimpleExpression _ ')' {
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