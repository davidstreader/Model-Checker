{
    /**
     * Javascript objects which are used to form a parse tree in the JSON format.
     */
    var Node = {
        ModelNode: function(def, defs){
            this.type = 'model';
            this.definitions = defs ? [def].concat(defs) : [def];
        },
        DefinitionNode: function(type, name, process, relabel, hidden){
            this.type = type;
            this.name = name;
            this.process = process;
            this.relabel = relabel;
            this.hidden = hidden;
        },
        SequenceNode: function(from, to){
            this.type = 'sequence';
            this.from = from;
            this.to = to;
        },
        ChoiceNode: function(def1, def2){
            this.type = 'choice';
            this.option1 = def1;
            this.option2 = def2;
        },
        NameNode: function(name, label){
            this.type = 'name'
            this.name = name;
            this.label = label;
        },
        ActionNode: function(action){
            this.type = 'action';
            this.action = action;
        },
        IndexedActionNode: function(varible, range, action){
            this.type = 'indexed-action';
            this.variable = variable;
            this.range = range;
            this.action = action;
        },
        StopNode: function(){
            this.type = 'stop';
        },
        ErrorNode: function(){
            this.type = 'error';
        },
        ParallelNode: function(def1, def2){
            this.type = 'parallel';
            this.definition1 = def1;
            this.definition2 = def2;
        },
        FunctionNode: function(type, process){
            this.type = type;
            this.process = process;
        },
        ConstantNode: function(name, value){
            this.type = 'constant';
            this.name = name;
            this.value = value;
        },
        RangeNode: function(name, range){
            this.type = 'range';
            this.range = range;
        },
        OperationNode: function(input, process){
            this.type = 'operation';
            this.input = input;
            this.process = process;
        },
        CommentNode: function(comment){
            this.type = 'comment';
            this.comment = comment;
        }
    };
    
    /**
     * Functions used by parsing functions.
     */
    
    /* Constructs a javascript object which can be used to construct a name node. */
    function constructName(name, label){
        return {name: name, label: label};
    };
    
    /* Constructs a javascript object which can be used to constract a defintion node. */
    function constructDefinition(type, process, relabel, hidden){
        return {type: type, process: process, relabel: relabel, hidden: hidden};
    };
    
    function constructOperationProcess(type, isNegated, def1, def2){
        return {type: type, isNegated: isNegated, definition1: def1, definition2: def2};
    };
    
    function constructOperator(type, isNegated){
        return {type: type, isNegated: isNegated};
    };
    
    function constructRelabellingAndHiding(relabel, hidden){
        return {relabel: relabel, hidden: hidden};
    };
}

File = (Model / Operation / Declare_Constant / Declare_Range / Comment)*

Model = Single_Model / Multiple_Model

Single_Model = _ definition:Nested_Definition _ DEF_END _ {
    return new Node.ModelNode(definition);
}

Multiple_Model = _ definition:Definition _ DEF_SEPARATOR _ model:Model _ {
    var length = model.definitions.length;
    definition.relabel = model.definitions[length - 1].relabel;
    definition.hidden = model.definitions[length - 1].hidden;
    model.definitions[length - 1].relabel = undefined;
    model.definitions[length - 1].hidden = undefined;
    return new Node.ModelNode(definition, model.definitions);
}

Definition = _ name:Name _ DEF_ASSIGNMENT _ def:Parse_Definition _ {
    return new Node.DefinitionNode(def.type, name, def.process, def.relabel, def.hidden);
}

Nested_Definition = _ name:Name _ DEF_ASSIGNMENT _ def:Parse_Nested_Definition _ {
  return new Node.DefinitionNode(def.type, name, def.process, def.relabel, def.hidden);
}

/* Parses and returns either a standard, parallel, reference or function definition. */
Parse_Definition 'parse definition' = Standard_Definition / Parallel_Definition / Reference_Definition / Function_Definition

Parse_Nested_Definition = Nested_Standard_Definition / Nested_Parallel_Definition / Nested_Reference_Definition / Nested_Function_Definition

/**
 * Standard Definition
 */

/* Parses and returns a standard definition. */
Standard_Definition 'parse standard definition' = process:Parse_Standard_Definition {
    return constructDefinition('standard-definition', process, undefined, undefined);
}

Nested_Standard_Definition = process:Parse_Standard_Definition _ rah:(Parse_Relabelling_And_Hiding ?) {
    if(rah !== null){
      return constructDefinition('standard-definition', process, rah.relabel, rah.hidden);
    }
    return constructDefinition('standard-definition', process, undefined, undefined);
}

/* Attempts to parse and return a standard definition. */
Parse_Standard_Definition = Parse_Choice / Parse_Sequence / Parse_Bracketed_Standard_Definition

/* Attempts to parse and return a bracketed standard definition. */
Parse_Bracketed_Standard_Definition 'parse standard definition' = BRACKET_LEFT _ process:Parse_Standard_Definition _ BRACKET_RIGHT {
    return process;
}

/* Attempts to parse and return a choice for a standard definition. */
Parse_Choice 'parse choice' = a:(Parse_Sequence / Name) _ CHOICE _ b:(Terminal / Name / Parse_Standard_Definition) {
    return new Node.ChoiceNode(a, b);
}

/* Attempts to parse and return a sequence for a standard definition */
Parse_Sequence 'parse sequence' = from:Action _ SEQUENCE _ to:(Terminal / Name / Parse_Sequence) {
    return new Node.SequenceNode(from, to);
}

/**
 * Parallel Definition
 */

/* Parses and returns a parallel definition. */
Parallel_Definition 'parse parallel definition' = process:Parse_Parallel_Definition {
    return constructDefinition('parallel-definition', process, undefined, undefined);
}

Nested_Parallel_Definition = process:Parse_Parallel_Definition _ rah:(Parse_Relabelling_And_Hiding ?) {
  if(rah !== null){
      return constructDefinition('parallel-definition', process, rah.relabel, rah.hidden);
    }
    return constructDefinition('parallel-definition', process, undefined, undefined);
}

/* Attempts to parse and return a parallel definition. */
Parse_Parallel_Definition = Parse_Parallel_Composition / Parse_Bracketed_Parallel_Definition

/* Attempts to parse and return a bracketed parallel definition. */
Parse_Bracketed_Parallel_Definition = BRACKET_LEFT _ process:Parse_Parallel_Definition _ BRACKET_RIGHT {
    return process;
}

/* Attempts to parse and return a parallel composition. */
Parse_Parallel_Composition = a:(Name / Nested_Standard_Definition / Nested_Function_Definition / Parse_Bracketed_Parallel_Definition) _ PARALLEL _ b:(Name / Nested_Standard_Definition / Nested_Function_Definition / Parse_Parallel_Definition) {
    return new Node.ParallelNode(a, b);
}

/**
 * Reference Definition
 */
 
/* Parses and returns a reference definition. */
Reference_Definition 'parse reference definition' = process:Labelled_Name {
    return constructDefinition('reference-definition', process, undefined, undefined);
}

Nested_Reference_Definition = process:Labelled_Name _ rah:(Parse_Relabelling_And_Hiding ?) {
  if(rah !== null){
      return constructDefinition('reference-definition', process, rah.relabel, rah.hidden);
    }
    return constructDefinition('reference-definition', process, undefined, undefined);
}

/**
 * Function Definition
 */

/* Parses and returns a function definition. */
Function_Definition = process:Parse_Function {
    return constructDefinition('function-definition', process, undefined, undefined);
}

Nested_Function_Definition = process:Parse_Function _ rah:(Parse_Relabelling_And_Hiding ?) {
  if(rah !== null){
      return constructDefinition('function-definition', process, rah.relabel, rah.hidden);
    }
  return constructDefinition('function-definition', process, undefined, undefined); 
}

Parse_Function = Parse_Abstraction / Parse_Simplification

/* Attempts to parse and return an abstraction function. */
Parse_Abstraction = ABS _ BRACKET_LEFT _ process:Parse_Nested_Definition _ BRACKET_RIGHT {
    return new Node.FunctionNode('abstraction', process);
}

/* Attempts to parse and return a simplification function. */
Parse_Simplification = SIMP _ BRACKET_LEFT _ process:Parse_Nested_Definition _ BRACKET_RIGHT {
    return new Node.FunctionNode('simplification', process);
}

/**
 * Relabelling and Hiding
 */

/* Parses and returns the relabelling and hiding for a definition if there is any. */
Parse_Relabelling_And_Hiding = Relabelling_And_Hiding / Relabelling / Hiding

/* Attempts to parse and return a relabelling and hiding. */
Relabelling_And_Hiding = relabel:Parse_Relabelling _ hide:Parse_Hiding {
    return constructRelabellingAndHiding(relabel, hide);
}

/* Parses and returns a relabelling. */
Relabelling = relabel:Parse_Relabelling {
    return constructRelabellingAndHiding(relabel, undefined);
}

/* Attempts to parse and return a relabelling. */
Parse_Relabelling = RELABEL _ BRACE_LEFT _ relabel:Parse_Relabel _ BRACE_RIGHT {
    return relabel;
}

/* Parses the contents within relabelling braces. */
Parse_Relabel = a:Relabel _ DEF_SEPARATOR _ b:Parse_Relabel { return a.concat(b); } / Relabel

/* Parses and returns a single relabelling. */
Relabel = a:Action _ RELABEL _ b:Action {
    return [{'new-label': a.action, 'old-label': b.action}];
}

/* Parses and returns a hiding. */
Hiding = hide:Parse_Hiding {
    return constructRelabellingAndHiding(undefined, hide);
}

/* Attempts to parse and return a hiding. */
Parse_Hiding = HIDE _ BRACE_LEFT _ hidden:Parse_Hidden_Action _ BRACE_RIGHT {
    return hidden;
}

/* Parses and returns the contents within hiding braces. */
Parse_Hidden_Action = a:Hidden_Action _ DEF_SEPARATOR _ b:Parse_Hidden_Action { return a.concat(b); } / Hidden_Action

/* Parses and returns a single hidden action. */
Hidden_Action = action:Action {
    return [action.action];
}

/**
 * Operations
 */

/* Parses and returns an operation. */
Operation 'parse operation' = process:Parse_Operation {
    var input = text();
    return new Node.OperationNode(input, process);
}

/* Attempts to parse and return an operation. */
Parse_Operation = a:Parse_Definition _ type:Parse_Operator _ b:Parse_Definition _ DEF_END _ {
    return constructOperationProcess(type.type, type.isNegated, a, b); 
}

/* Parses an returns an operator for an operation and determines whether the
operator is to be negated or not. */
Parse_Operator = operator:(NOT Operator / Operator) {
    var isNegated = false;
    var op = '';
    var start = 0;
    
    // if operator is a string
    if(typeof(operator) === 'string'){
        op = operator;
    }
    // check if the operator is negated
    else if(typeof(operator) === 'object' && operator[0] === '!'){
        isNegated = true;
        start = 1;
    }
    
    // construct operator from array
    if(typeof(operator) === 'object'){
      for(var i = start; i < operator.length; i++){
          op += operator[i];
      }
    }
    
    // determine what type of operation this operator represents
    var type;
    switch(op){
        case '~':
            type = 'bisimulation';
            break;
        default:
            // throw an error as an unexpected operator was found
            expected(op + 'is not a valid operator.');
    }
    
    return constructOperator(type, isNegated);
}

/* Parses an returns an operator. */
Operator = BISIMULATION

Declare_Constant = _ CONST _ name:PascalCase _ DEF_ASSIGNMENT _ value:Integer _ {
    return new Node.ConstantNode(name, value);
}

Declare_Range = RANGE _ name:PascalCase _ DEF_ASSIGNMENT _ range:Parse_Range _ {
    return new Node.RangeNode(name, range);
}

Parse_Range = start:(CamelCase / PascalCase / Integer) _ RANGE_SEPARATOR _ end:(CamelCase / PascalCase / Integer) {
    return {start: start, end: end};
}

/**
 * Comments
 */

/* Parses and returns a comment */
Comment 'parse comment' = _ comment:(Single_Lined_Comment / Multi_Lined_Comment) _ {
    return new Node.CommentNode(comment);
}

/* Helper function for 'Comment' which parses and returns a single lined comment. */
Single_Lined_Comment = SINGLE_LINE_COMMENT (!LineTerminator SourceCharacter)* {
    return text();
}

/* Helper function for 'Comment' which parses and returns a multi lined comment. */
Multi_Lined_Comment = MULTI_LINE_COMMENT_START (!MULTI_LINE_COMMENT_END SourceCharacter)* MULTI_LINE_COMMENT_END {
    return text();
}

/**
 * Non Terminals
 */

/* Parses and returns a name node with no label. */
Name 'parse name' = name:(Parse_Indexed_Name / Parse_Name) {
    return new Node.NameNode(name.name, name.label);
}

/* Parses and returns a name node with a label. */
Labelled_Name 'name or labelled-name' = name:(Parse_Labelled_Name / Parse_Name) {
    return new Node.NameNode(name.name, name.label);
}

/* Helper function for 'Name' and 'Labelled_Name' which parses and returns a standard name. */
Parse_Name = name:PascalCase {
    return new constructName(name);
}

/* Helper function for 'Name' which parses and returns an indexed name. */
Parse_Indexed_Name = name:PascalCase SQUARE_BRACKET_LEFT _ index:Parse_Index _ SQUARE_BRACKET_RIGHT {
    return new constructName(name + '[' + index + ']');
}

/* Helper function for 'Name' and 'Labelled_Name' which parses and returns a labelled name. */
Parse_Labelled_Name = label:CamelCase LABEL name:PascalCase {
    return new constructName(name, label);
}

Action = Indexed_Action / Single_Action

/* Parses and returns an action node. */
Single_Action = action:(Parse_Indexed_Action / Parse_Labelled_Action / Parse_Action) {
    return new Node.ActionNode(action);
}

Indexed_Action = SQUARE_BRACKET_LEFT _ variable:Integer _ LABEL _ range:(Parse_Range / PascalCase) _ SQUARE_BRACKET_RIGHT _ DEF_END _ action:CamelCase {
    return new Node.IndexedActionNode(variable, range, action);
}

/* Helper function for 'Action' which parses and returns a standard action. */
Parse_Action = action:CamelCase {
    return action;
}

/* Helper function for 'Action' which parses and returns a labelled action. */
Parse_Labelled_Action = label:CamelCase DEF_END action:CamelCase {
    return label + '.' + action;
}

/* Helper function for 'Action' which parses and returns an indexed action. */
Parse_Indexed_Action = SQUARE_BRACKET_LEFT _ index:Parse_Index _ SQUARE_BRACKET_RIGHT action:CamelCase {
    return '[' + index + ']' + action;
}

/* Parses and returns an index which can be either an integer or a string of text in the camel case format. */
Parse_Index = Integer / CamelCase

/* Parses and returns a string of text that begins with a lower case letter. */
CamelCase 'camel case' = $([a-z][A-Za-z0-9_]*) { return text(); }

/* Parses and returns a string of text that begins with an upper case letter. */
PascalCase 'pascal case' = $([A-Z][A-Za-z0-9_]*) { return text(); }

/* Parses and returns an integer. */
Integer 'integer' = [-]?[0-9]+ { return parseInt(text(), 10); }

/* Parses the termination of a line. */
LineTerminator 'line terminator' = [\n\r\u2028\u2029]

/* Parses a source character for a comment. */
SourceCharacter 'source character' = .

/**
 * Terminals
 */

/* Parses and returns a terminal. */
Terminal = Stop / Error

/* Parses and returns a stop node. */
Stop = 'STOP' {
    return new Node.StopNode();
}

/* Parses and returns an error node. */
Error = 'ERROR' {
    return new Node.ErrorNode();
}

DEF_END 'end of definition' = '.'
DEF_SEPARATOR 'definition separator' = ','
DEF_ASSIGNMENT 'definition assignment' = '='

BRACKET_LEFT 'rounded left bracket' = '('
BRACKET_RIGHT 'rounded right bracket' = ')'
SQUARE_BRACKET_LEFT 'square left bracket' = '['
SQUARE_BRACKET_RIGHT 'square right bracket' = ']'
BRACE_LEFT 'left brace' = '{'
BRACE_RIGHT 'right brace' = '}'

SEQUENCE 'sequence' = '->'
CHOICE 'choice' = '|'
PARALLEL 'parallel' = '||'
LABEL 'label' = ':'
RELABEL 'relabel' = '/'
HIDE 'hide' = '\\'

ABS 'abstraction' = 'abs'
SIMP 'simplficiation' = 'simp'

BISIMULATION 'bisimulation' = '~'
NOT 'negation' = '!'

CONST 'constant declaration' = 'const'
RANGE 'range declaration' = 'range'
RANGE_SEPARATOR = '..'

SINGLE_LINE_COMMENT = '//'
MULTI_LINE_COMMENT_START = '/*'
MULTI_LINE_COMMENT_END = '*/'

/* Parses whitespace */
_ 'whitespace' = [ \t\n\r]*