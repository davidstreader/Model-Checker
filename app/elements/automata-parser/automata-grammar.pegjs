{
  var Node = {
    ModelNode: function(def, defs, relabel, hidden){
          this.type = 'model';
          this.definitions = defs ? [def].concat(defs) : [def];
          this.relabel = relabel;
          this.hidden = hidden;
      },
      DefinitionNode: function(type, name, process){
          this.type = type;
          this.name = name;
          this.process = process;
      },
        OperationNode: function(operation){
          this.type = 'operation'
            this.process = operation;
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
      ParallelNode: function(def1, def2){
          this.type = 'parallel';
          this.definition1 = def1;
          this.definition2 = def2;
      },
      NameNode: function(name){
          this.type = 'name';
          this.name = name;
      },
        LabelledNameNode: function(name, label){
          this.type = 'labelled-name';
            this.name = name;
            this.label = label;
        },
        ActionNode: function(action){
          this.type = 'action';
          this.action = action;
      },
      AbstractionNode: function(name){
          this.type = 'abstraction';
          this.name = name;
      },
      SimplificationNode: function(name){
          this.type = 'simplification';
          this.name = name;
      },
        BisimulationNode: function(def1, def2){
          this.type = 'bisimulation';
            this.definition1 = def1;
            this.definition2 = def2;
        },
      StopNode: function(){
          this.type = 'stop';
      },
      ErrorNode: function(){
          this.type = 'error';
      },
      CommentNode: function(comment){
          this.type = 'comment';
          this.comment = comment;
      }
    };
}

File = Model*

Model = _ definition:Definition _ process:Relabelling_AND_Hiding _ {
  return new Node.ModelNode(definition, undefined, process.relabel, process.hide);
}
/ _ definition:Definition _ symbol_DefinitionListSeparator _ model:Model _ {
  return new Node.ModelNode(definition, model.definitions, model.relabel, model.hidden);
}
/ _ operation:Operation _ symbol_DefinitionListEnd {
  return new Node.OperationNode(operation);
}
/ _ Process_Comment

Definition = name:Name _ symbol_DefinitionAssignment _ process:Process_Definition {
  return new Node.DefinitionNode(process.type, name, process.process);
}

Process_Definition = Standard_Definition
           / Parallel_Definition
                   / Reference_Definition
                   / Function_Definition

/**
 * STANDARD DEFINITION
 */
  
Standard_Definition = process:Process_Standard {
  return {type: 'standard-definition', process: process};
}

Process_Standard = a:Name_OR_Choice _ b:Process_Standard {
  return new Node.ParallelNode(a, b);
}
/ Process_Choice

Process_Choice = a:Process_Sequence _ symbol_Choice _ b:Process_Choice {
  return new Node.ChoiceNode(a, b);
}
/ Process_Sequence

Process_Sequence = from:Action _ symbol_Sequence _ to:Name_OR_Sequence {
  return new Node.SequenceNode(from, to);
}
/ Terminal_OR_Brackets

/**
 * PARALLEL DEFINITION
 */
 
Parallel_Definition = process:Process_Parallel {
  return {type: 'parallel-definition', process: process};
}

Process_Parallel = a:Name_OR_Label _ symbol_Parallel _ b:Process_Parallel{
  return new Node.ParallelNode(a, b);
}
/ Process_Parallel_Composition
     

Process_Parallel_Composition = a:Name_OR_Label _ symbol_Parallel _ b:Name_OR_Parallel_Composition {
  return new Node.ParallelNode(a, b);
}
/ Parallel_Brackets

/**
 * REFERENCE DEFINITION
 */
 
Reference_Definition = process:Name_OR_Label {
  return {type: 'reference-definition', process: process};
}

/**
 * FUNCTION DEFINITION
 */

Function_Definition = process:Process_Name {
  return {type: 'function-definition', process: process};
}

Process_Abstraction = symbol_Abstraction _ symbol_BracketLeft _ name:Process_Name _ symbol_BracketRight {
  return new Node.AbstractionNode(name);
}

Process_Simplification = symbol_Simplification _ symbol_BracketLeft _ name:Process_Name _ symbol_BracketRight {
  return new Node.SimplificationNode(name);
}

/**
 * RELABELLING AND HIDING
 */

Relabelling_AND_Hiding = relabel:Process_Relabel _ hide:Process_Hide _ symbol_DefinitionListEnd _ {
  return {relabel:relabel, hide:hide};
}
/ relabel:Process_Relabel _ symbol_DefinitionListEnd _ {
  return {relabel:relabel, hide:undefined};
}
/ hide:Process_Hide _ symbol_DefinitionListEnd _ {
  return {relabel:undefined, hide:hide};
}
/ symbol_DefinitionListEnd _ {
  return {relabel:undefined, hide:undefined};
}

Process_Relabel = symbol_Relabel _ symbol_BraceLeft _ relabel:Relabel_OR_Brace {
  return relabel;
}

Process_Hide = symbol_Hide _ symbol_BraceLeft _ hide:Action_OR_Brace {
  return hide;
}

/**
 * OPERATIONS
 */

Operation = a:Process_Definition _ symbol_Bisimulation _ b:Process_Definition {
  return new Node.BisimulationNode(a, b);
}

/**
 * COMMENTs
 */
 
Process_Comment = SingleLineComment

SingleLineComment = symbol_SingleLineComment c:Comment [\n] {
  var comment = '';
  for(var i = 0; i < c.length; i++){
    comment += c[i];
  }
  return new Node.CommentNode(comment);
}
/ symbol_SingleLineComment c:Comment {
  var comment = '';
  for(var i = 0; i < c.length; i++){
    comment += c[i]
  }
  return new Node.CommentNode(comment);
}

/**
 * HELPER FUNCTIONS
 */

Label_OR_Process_Parallel = Label
/ Process_Parallel

Name_OR_Choice = Process_Name
/ Process_Choice

Name_OR_Sequence = Process_Sequence
/ Process_Name

Name_OR_Parallel_Composition = Process_Parallel_Composition
/ Name_OR_Label

Name_OR_Label = Name
/ Label

Relabel_OR_Brace = relabel:Relabel _ symbol_BraceRight {
  return [relabel];
}
/ a:Relabel _ symbol_DefinitionListSeparator _ b:Relabel_OR_Brace {
  return b.concat(a);
}

Action_OR_Brace = a:Action _ symbol_BraceRight {
  return [a.action];
}
/ a:Action _ symbol_DefinitionListSeparator _ b:Action_OR_Brace {
  return b.concat(a.action);
}

Terminal_OR_Brackets = Terminal
/ symbol_BracketLeft _ process:Process_Standard _ symbol_BracketRight {
  return process;
}

Parallel_Brackets = symbol_BracketLeft _ process:Process_Parallel _ symbol_BracketRight {
  return process;
}

Terminal = Stop
/  Error

/**
 * Signifies the end of a sequence.
 */
Stop = 'STOP' {
  return new Node.StopNode();
}

/**
 * Signifies an error in the parsing of a definition.
 */
Error = 'ERROR' {
  return new Node.ErrorNode();
}

/**
 * The name given to a definition of an automaton.
 */
Name = name:$([A-Z][A-Za-z0-9_]*) {
  return new Node.NameNode(name);
}

Process_Name
  =  Name
  /  Process_Abstraction
  /  Process_Simplification

/**
 * A process which takes an automaton from one state to another.
 */
Action = action:$([a-z][A-Za-z0-9_]*) {
  return new Node.ActionNode(action);
}

/**
 * A new label to be given to the definition of an automaton.
 */
Label = label:Action _ symbol_Label _ name:Name {
  return new Node.LabelledNameNode(name.name, label.action);
}

/**
 * 
 */
Relabel = label:Action _ symbol_Relabel _ a:Action symbol_DefinitionListEnd b:Action {
  var oldLabel = a.action + "." + b.action;
  return {"new-label":label.action, "old-label":oldLabel};
}
/ a:Action _ symbol_Relabel _ b:Action {
  return {"new-label":a.action, "old-label": b.action};
}

Comment = ([\x20-\x7F]*)

/**
 * TERMINALS
 */
symbol_BracketLeft = '('
symbol_BracketRight = ')'
symbol_BraceLeft = '{'
symbol_BraceRight = '}'
symbol_DefinitionListEnd = '.'
symbol_DefinitionListSeparator = ','
symbol_DefinitionAssignment = '='
symbol_Parallel = '||'
symbol_Choice = '|'
symbol_Sequence = '->'
symbol_Label = ':'
symbol_Relabel = '/'
symbol_Hide = '\\'
symbol_Abstraction = 'abs'
symbol_Simplification = 'simp'
symbol_Bisimulation = '~'
symbol_SingleLineComment = '//'
symbol_MultiLineCommentStart = '/*'
symbol_MultiLineCommentEnd = '*/'

_ 'optional whitespace' = [ \t\n\r]*