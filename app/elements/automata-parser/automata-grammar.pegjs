{
    var STOP = 'stop';
    var ERROR = 'error';
    var SIMPLE_EXPRESSION = 'simple-expression';
    var NO_IDENTIFIER = null;
    var NO_RELABEL = null;
    var NO_HIDE = null;
    var NOT_VISIBLE = '*';
    
    var expressionCount = 0;
    var variableMap = {};
    
    var indexArray = [];
    
    var Node = {
        ModelNode: function(definitions){
            this.type = 'model';
            this.definitions = definitions;
        },
        DefinitionNode: function(subtype, name){
            this.type = 'definition';
            this.subtype = subtype;
            if(name != null){ this.name = name };
        },
        OperationNode: function(operation, isNegated, input, process1, process2){
            this.type = 'operation';
            this.operation = operation;
            this.isNegated = isNegated;
            this.input = input;
            this.process1 = process1;
            this.process2 = process2;
            this.position = location();
        },
        CommentNode: function(comment){
            this.type = 'comment';
            this.comment = comment;
        },
        RangeNode: function(start, end){
            this.type = 'range';
            this.start = start;
            this.end = end;
        },
        NameNode: function(name){
            this.type = 'name';
            this.name = name;
        },
        ActionNode: function(action){
            this.type = 'action';
            this.action = action;
        },
        IndexNode: function(variable, index, process){
            this.type = 'index';
            this.variable = variable;
            this.index = index;
            this.process = process;
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
        SequenceNode: function(from, to){
            this.type = 'sequence';
            this.from = from;
            this.to = to;
        },
        ChoiceNode: function(option1, option2){
            this.type = 'choice';
            this.option1 = option1;
            this.option2 = option2;
        },
        IfNode: function(guard, thenProcess, elseProcess){
            this.type = 'if-statement';
            this.guard = guard,
            this.thenProcess = thenProcess;
            if(elseProcess != undefined){ this.elseProcess = elseProcess; }
        },
        ForAllNode: function(range, process){
            this.type = 'forall';
            this.range = range;
            this.process = process;
        },
        CompositeNode: function(label, composite, relabel){
            this.type = 'composite';
            if(label != null){ this.label = label; };
            this.composite = composite;
            if(relabel != null){ this.relabel = relabel; }
        },
        ParallelNode: function(process1, process2){
            this.type = 'parallel';
            this.process1 = process1;
            this.process2 = process2;
        },
        FunctionNode: function(type, process){
            this.type = type;
            this.process = process;
        },
        TerminalNode: function(type){
            this.type = type;
        },
        ExpressionNode: function(expression){
            this.type = 'expression';
            this.expression = expression;
        }
    };
    
    function constructModelNode(definition){
        var definitions = [];
        var local = undefined;
        do{
            local = definition.process.local;
            delete definition.process['local'];
            definitions.push(definition);
            definition = local;
        }while(local != undefined);       
    
        return new Node.ModelNode(definitions);
    }
    
    /* constructs a process definition node for the parse tree */
    function constructModelDefinitionNode(subtype, name, process, relabel, hidden, isVisible){
        var node = new Node.DefinitionNode(subtype, name);
        node.isVisible = (isVisible == null) ? true : false;
        node.process = process;
        if(relabel != null){ node.relabel = relabel; }
        if(hidden != null){ node.hidden = hidden; }       
        return node;
    }

    /* constructs a constant definition node for the parse tree */
    function constructConstantDefinitionNode(name, value){
        var node = new Node.DefinitionNode('constant', name);
        node.value = value;
        return node;
    }

    /* constructs a range definition node for the parse tree */
    function constructRangeDefinitionNode(name, start, end){
        var node = new Node.DefinitionNode('range', name);
        node.range = new Node.RangeNode(start, end);
        return node;
    }

    /* constructs a set definition node for the parse tree */
    function constructSetDefinitionNode(name, set){
        var node = new Node.DefinitionNode('set', name);
        node.set = set.set;
        return node;
    }
    
    /* constructs and returns a name node */
    function constructNameNode(name, indices){
        var node = new Node.NameNode(name);
        if(indices != null){ node.index = indices; }
        return node;
    }
    
    /* constructs and returns a sequence node */
    function constructSequenceNode(sequence){
        // pop first two actions from the sequence
        var to = sequence.pop();
        var from = sequence.pop();
        // if only one action was retrieved then return only that one
        if(from == undefined){
            return to;
        }
        // otherwise construct sequence
        var node = new Node.SequenceNode(from, to);
        while(sequence.length != 0){
            from = sequence.pop();
            node = new Node.SequenceNode(from, node);
        }
        
        return node;
    }
    
    function processActionRange(range){
      var variable = range;
      if(range.variable != undefined){
          variable = range.variable;
      }
      else if(range[0] != '$'){
          variable = '$v<' + expressionCount++ + '>';
          range.variable = variable;
      }

      indexArray.push(range);
      return '[' + variable + ']';    
    }
    
    function constructLocalProcess(process){
        for(var i = 0; i < indexArray.length; i++){
            var index = indexArray[i];
            process = new Node.IndexNode(index.variable, index.index, process);
        }
        
        indexArray = [];
        return process;
    }
    
    function processOperationText(text){
        // remove any unnecessary line breaks and whitespace from input
        text = text.replace(/ /g, '\n');
        text = text.split('\n');
        var result = '';
        for(var i = 0; i < text.length; i++){
            if(text[i] != ''){
                result += text[i];
            }
        }
        // remove the dot from the end of the operation
        result = result.slice(0, result.length - 1);
        return result;
    }
}

/* Constructs a parse tree of finite state processes */
ParseTree = processes:FiniteStateProcess* {
    return { processes: processes, variableMap: variableMap };
}

/* Attempts to parse and return a finite state process */
FiniteStateProcess
 =  _ definition:ConstantDefinition _ {
    return definition;
 }
 / _ definition:RangeDefinition _ {
    return definition;
 }
 / _ definition:SetDefinition _ {
    return definition;
 }
 / _ definitions:ProcessDefinition _ {
    return constructModelNode(definitions);
 }
 / _ definition:CompositeDefinition _ {
    return constructModelNode(definition);
 }
 / _ definition:FunctionDefinition _ {
    return constructModelNode(definition);
 }
 / _ operation:OperationDefinition _ {
    return operation;
 }
 / _ comment:Comment _ {
    return comment;
 }

/* Parses whitespace */
_ 'whitespace' = [ \t\n\r]*

/* Attempts to parse an indentifier for a finite state process */
Identifier
 = stop:('STOP' / '(' _ 'STOP' _ ')') {
    return new Node.TerminalNode(STOP);
 }
 / error:('ERROR' / '(' _ 'ERROR' _')') {
    return new Node.TerminalNode(ERROR);
 }
 / ident:UpperCaseIdentifier _ index:Indices {
    return constructNameNode(ident, index);
 }
 / ident:UpperCaseIdentifier _ index:IndexRanges {
    return constructNameNode(ident, index);
 }
 / ident:UpperCaseIdentifier {
    return constructNameNode(ident);
 }

Variable
 = ident:LowerCaseIdentifier {
    return '$' + ident;
 }

/* Parses and returns a string where the first character is upper case */
UpperCaseIdentifier
 = $([A-Z][A-Za-z0-9_]*) {
    return text();
 }

/* Parses and returns a string where the first character is lower case */
LowerCaseIdentifier
 = $([a-z][A-Za-z0-9_]*) {
    return text();
 }

/* Parses and returns an integer */
IntegerLiteral
 = [-]?[0-9]+ {
    return parseInt(text(), 10);
 }

/**
 * Parsing of Action Labels
 */

/* Attempts to parse and return an action label */
ActionLabel
 = action1:LowerCaseIdentifier _ action2:(_ActionLabel ?) {
    // if unable to parse a more complex action node, just return a stardard action node
    if(action2 == null){
        return action1;
    }
    // otherwise construct a more complex action node
    return action1 + action2;
 }
 / '[' _ exp:Expression _ ']' _ action:(_ActionLabel ?) {
    if(action == null){
        return '[' + exp + ']';
    }
    return '[' + exp + ']' + action;
 }

/* Used to avoid left hand recursion in ActionLabel */
_ActionLabel
 = '.' _ action:LowerCaseIdentifier {
    return '.' + action;
 }
 / '[' _ exp:Expression _ ']' {
    return '[' + exp + ']';
 } 

/* Attempts to parse and return multiple action labels */
ActionLabels
 = action1:ActionLabel _ action2:(_ActionLabels ?) {
    // if unable to parse further action labels then return current action label
    if(action2 == null){
        return new Node.ActionNode(action1);
    }
    
    // otherwise construct and return new action node
    return new Node.ActionNode(action1 + action2);
 }
 / set:Set {
    return set;
 }
 / '[' _ range:ActionRange _ ']' {
    var action = processActionRange(range);
    return new Node.ActionNode(action);
 }
 / '[' _ exp:Expression _ ']' {
    return new Node.ActionNode('[' + exp + ']');
 }

/* Used to avoid left hand recursion in ActionLabels */
_ActionLabels
 = '.' _ action1:ActionLabel _ action2:(_ActionLabels ?) {
    if(action2 == null){
        return '.' + action1;
    }
    
    return ',' + action1 + action2;
 }
 / '.' _ set:Set _ action:(_ActionLabels ?) {
    var variable = '$v<' + expressionCount++ + '>';
    variableMap[variable] = set;
    if(action == null){
        return '.' + variable;
    }
    
    return '.' + variable + action;
 }
 / '[' _ range:ActionRange _ ']' _ action:(_ActionLabels ?) {
    var label = processActionRange(range);
    if(action != null){
        label = label + action;
    }
    
    return label;
 }
  / '[' _ exp:Expression _ ']' _ action:(_ActionLabels ?) {
    if(action == null){
        return '[' + exp + ']';
    }
    
    return '[' + exp + ']' + action;
 }

/* Attempts to parse and return an action range */
ActionRange
 = range:(Range / Set) {
    return { index: range };
 }
 / variable:Variable _ ':' _ range:(Range / Set) {
    return { variable: variable, index: range };
 }

/* Attempts to parse and return a range */
Range
 = start:Expression  '..' end:Expression {
    return new Node.RangeNode(start, end);
 }
 / Identifier
/* Attempts to parse and return a set */
Set
 = Identifier
 / '{' _ elements:SetElements _ '}' {
    return new Node.SetNode(elements);
 }

/** Parse and return an array of set elements */
SetElements
 = a:ActionLabels _ b:(_SetElements ?) {
    // if unable to parse more set elements then return current ones
    if(b == null){
        return a;
    }
    
    // otherwise join set elements together
    return [a].concat(b);
 }

/* Used to avoid left hand recursion in SetElements */
_SetElements
 =  ',' _ elements:SetElements {
    return elements;
 }

/**
 * Parsing of Constants, Ranges and Sets
 */

/* Attempts to parse and return a constant definition */
ConstantDefinition
 = 'const' _ ident:Identifier _ '=' _ value:SimpleExpression {
    return constructConstantDefinitionNode(ident, value);
 }

/* Attempts to parse and return a range definition */
RangeDefinition
 = 'range' _ ident:Identifier _ '=' _ start:SimpleExpression _ '..' _ end:SimpleExpression {
    return constructRangeDefinitionNode(ident, start, end);
 }

/* Attempt to parse and return a set definition */
SetDefinition
 = 'set' _ ident:Identifier _ '=' _ set:Set {
    return constructSetDefinitionNode(ident, set);
 }

/**
 * Parsing of Process Definitions
 */

/* Attempts to parse and return a process definition */
ProcessDefinition
 = ident:Identifier _ visible:('*' ?) _ '=' _ body:ProcessBody _ relabel:(Relabel ?) _ hide:(Hiding ?) _ '.' {
    return constructModelDefinitionNode('process', ident, body, relabel, hide, visible);
 }
 
/* Attempts to parse and return the body of a process */
ProcessBody
 = process:LocalProcess _ ',' _ definitions:LocalProcessDefinitions {
    process.local = definitions;
    return process
 }
 / LocalProcess

/* Attempts to parse and return a locally defined process definitions */
LocalProcessDefinitions
 = def1:LocalProcessDefinition _ def2:(_LocalProcessDefinitions ?) {
    if(def2 == null){
        return def1;
    }
    def1.process.local = def2;
    return def1;
 }

/* Used to remove left hand recursion from LocalProcessDefinitions */
_LocalProcessDefinitions
 = ',' _ definition:LocalProcessDefinitions {
    return definition;
 }

/* Attempts to parse and return a singular locally defined process definition */
LocalProcessDefinition
 = ident:Identifier _ index:(IndexRanges ?) _ '=' _ process:LocalProcess {
    var name = constructNameNode(ident, index);
    return constructModelDefinitionNode('process', name, process, NO_RELABEL, NO_HIDE, NOT_VISIBLE);
 }

/* Attempts to parse and return a local process */
LocalProcess
 = process:_LocalProcess {
    return constructLocalProcess(process);
 }
_LocalProcess
 = BaseLocalProcess
 / process:IfStatement
 / '(' _ choice:Choice _ ')' {
    return choice;  
 }
 / process:Choice

/* Attempts to parse and return a base local process */
BaseLocalProcess
 = stop:('STOP' / '(' _ 'STOP' _ ')') {
    return new Node.TerminalNode(STOP);
 }
 / error:('ERROR' / '(' _ 'ERROR' _')') {
    return new Node.TerminalNode(ERROR);
 }
 / ident:Identifier _ indices:(Indices ?){
    return constructNameNode(ident, indices);
 }

IfStatement
 = 'if' _ exp:Expression _ 'then' _ process1:_LocalProcess _ 'else' _ process2:_LocalProcess {
    return new Node.IfNode(exp, process1, process2);
 }
 / 'if' _ exp:Expression _ 'then' _ process:_LocalProcess {
    return new Node.IfNode(exp.expression, process);
 }

/* Attempts to parse and return a choice */
Choice
 = prefix:ActionPrefix _ choice:(_Choice ?) {
    if(choice != null){
        return new Node.ChoiceNode(prefix, choice);
    }
    
    return prefix;
 }

/* Used to remove left hand recursion from 'Choice' */
_Choice
 = '|' _ choice:Choice {
    return choice;
 }

ActionPrefix
 = guard:(Guard ?) _ action:PrefixActions {
    var node = new constructSequenceNode(action);
    if(guard != null){
        node = new Node.IfNode(guard.expression, node);
    }
    return node;
 }
 
PrefixActions
 = action1:ActionLabels _ action2:(_PrefixActions ?) {
    if(action2 == null){
        return [action1];   
    }
    
    return [action1].concat(action2);
 }
 
_PrefixActions
 = '->' _ action:(IfStatement / PrefixActions / LocalProcess) {
    return action;
 }

Guard
 = 'when' _ exp:Expression {
    return exp;
 }

Indices
 = '[' _ exp:Expression _ ']' _ indices:(Indices ?) {
    return exp;
 }

IndexRanges
 = '[' _ index:(ActionRange / Expression) _ ']' _ indices:(IndexRanges ?) {
    return index;
 }

/**
 * Parsing of Composite Processes
 */

CompositeDefinition
 = ('||' ?) _ ident:Identifier _ visible:('*' ?) _ '=' _ body:CompositeBody _ hide:(Hiding ?) _ '.' {
    return constructModelDefinitionNode('composite', ident, body, NO_RELABEL, hide, visible);
 }
 
CompositeBody
 = label:(PrefixLabel ?) _ ident:Identifier _ relabel:(Relabel ?) {
    return new Node.CompositeNode(label, ident, relabel);
 }
 / label:(PrefixLabel ?) _ '(' _ comp:ParallelComposition _ ')' _ relabel:(Relabel ?) {
    return new Node.CompositeNode(label, comp, relabel);
 }
 / 'forall' _ range:Ranges _ body:CompositeBody {
    return new Node.ForAllNode(range, body);
 }

PrefixLabel
 = action:ActionLabels _ '::' {
    return action;
 }
 / action:ActionLabels _ ':' {
    return action;
 }
 
ParallelComposition
 = body:CompositeBody _ parallel:(_ParallelComposition ?) {
    // if cannot construct parallel node then just return composite body
    if(parallel == null){
        return body;
    }
    // otherwise construct and return a parallel node
    return new Node.ParallelNode(body, parallel);
 }

/* Used to remove left hand recursion from 'ParallelComposition' */
_ParallelComposition
 = '||' _ parallel:ParallelComposition {
    return parallel;
 }

Ranges
 = '[' _ range:ActionRange _ ']' _ ranges:(Ranges ?) {
    return range;
 }

/**
 * Parsing of Function Processes
 */

FunctionDefinition
 = ident:Identifier _ visible:('*' ?) _ '=' _ type:FunctionType _ '(' _ body:FunctionBody _ ')' _ relabel:(Relabel ?) _ hide:(Hiding ?) _ '.' {
    var node = new Node.FunctionNode(type, body);
    return constructModelDefinitionNode('function', ident, node, relabel, hide, visible);
 }

FunctionType
 = 'abs' {
    return 'abstraction';
 }
 / 'simp' {
    return 'simplification';
 }

FunctionBody
 = type:FunctionType _ '(' _ body:FunctionBody _ ')' _ relabel:(Relabel ?) _ hide:(Hiding ?) {
    var node = new Node.FunctionNode(type, body);
    return constructModelDefinitionNode('function', NO_IDENTIFIER, node, relabel, hide, NOT_VISIBLE);
 }
 / body:ProcessBody _ relabel:(Relabel ?) _ hide:(Hiding ?) {
    return constructModelDefinitionNode('process', NO_IDENTIFIER, body, relabel, hide, NOT_VISIBLE);
 }
 / body:CompositeBody _ relabel:(Relabel ?) {
    return constructModelDefinitionNode('composite', NO_IDENTIFIER, body, relabel, NO_HIDE, NOT_VISIBLE);
 }

/**
 * Parsing of Operations
 */

OperationDefinition
 = process1:OperationProcess _ negate:('!' ?) op:Operation _ process2:OperationProcess _ '.' {
    var isNegated = (negate != null) ? true : false;
    var input = processOperationText(text());
    return new Node.OperationNode(op, isNegated, input, process1, process2);
 }
 
OperationProcess
 = BaseLocalProcess
 / FunctionBody
 / ProcessBody
 / CompositeBody

Operation
 = '~' {
    return 'bisimulation';
 }

/**
 * Parsing of Relabelling and Hiding
 */
 
Relabel
 = '/' _ '{' _ relabel:RelabelDefinitions _ '}' {
    return relabel;
 }
 
RelabelDefinitions
 = relabel1:RelabelDefinition _ relabel2:(_RelabelDefinitions ?) {
    if(relabel2 == null){
        return [relabel1];
    }
    
    return [relabel1].concat(relabel2);
 }

/* Used to remove left hand recursion from 'RelabelDefinitions' */
_RelabelDefinitions
 = ',' _ relabel: RelabelDefinitions {
    return relabel;
 }

RelabelDefinition
 = label1:ActionLabels _ '/' _ label2:ActionLabels {
    return { new: label1, old: label2 };
 }

Hiding
 = '\\' _ set:Set {
    return { type: 'includes', set: set.set };
 }
 / '@' _ set:Set {
    return { type: 'excludes', set: set.set };
 }

/**
 * Parsing of Single Line and Multi Line Comments
 */

/* Parses and returns a comment */
Comment = _ comment:(SingleLinedComment / MultiLinedComment) _ {
    return new Node.CommentNode(comment);
}

/* Helper function for 'Comment' which parses and returns a single lined comment. */
SingleLinedComment = '//' (!LineTerminator SourceCharacter)* {
    return text();
}

/* Helper function for 'Comment' which parses and returns a multi lined comment. */
MultiLinedComment = '/*' (!'*/' SourceCharacter)* '*/' {
    return text();
}

/* Parses the termination of a line. */
LineTerminator 'line terminator' = [\n\r\u2028\u2029]

/* Parses a source character for a comment. */
SourceCharacter 'source character' = .

/**
 * Parsing of Expressions and Simple Expressions
 */

Expression
 = exp:RPNExpression {
    // if expression is a single number then return value
    if(typeof(exp) == 'number'){
        return exp;
    }
    
    // check if expression is a reference to a constant
    if(exp[0] == '$'){
        return exp;
    }
    
    // otherwise add expression to the variable map
    var variable = '$v<' + expressionCount++ +'>'
    variableMap[variable] = exp;
    return variable;
 }

RPNExpression
 = operand1:BaseExpression _ remaining:((operator:ExpressionOperators _ operand2:RPNExpression {
    return { operator: operator, operand2: operand2 };
 }) ?) {
    if(remaining == null){
        return operand1;
    }
    else{
        return (operand1 + ' ' + remaining.operand2 + ' ' + remaining.operator);
    }
 }

SimpleExpression
 = exp:RPNExpression {
    // if expression is a single number then return value
    if(typeof(exp) == 'number'){
        return exp;
    }
    
    // check if expression is a reference to a constant
    if(exp[0] == '$'){
        return exp;
    }
    
    // otherwise add expression to the variable map
    var variable = '$v<' + expressionCount++ +'>'
    variableMap[variable] = exp;
    return variable;
 }

RPNSimpleExpression
 = operand1:SimpleBaseExpression _ remaining:((operator:SimpleExpressionOperators _ operand2:RPNSimpleExpression ? {
    return { operator: operator, operand2: operand2 };
 }) ?) {
    if(remaining != null){
        return operand1;
    }
    else return (operand1 + ' ' + remaining.operand2 + ' ' + remaining.operator);
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
 = IntegerLiteral
 / Variable
 / variable:UpperCaseIdentifier {
    return '$' + variable;
 }
 / '(' _ exp:Expression _ ')'{
    return exp;
 }
  
SimpleBaseExpression
 = IntegerLiteral
 / Variable
 / UpperCaseIdentifier
 / '(' _ exp:SimpleExpression _ ')'{
    return exp;
 }