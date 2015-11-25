{
  var Node = {
    ModelNode: function(def, defs, relabel, hidden){
        this.type = 'model';
        this.definitions = defs ? [def].concat(defs) : [def];
        this.relabel = relabel;
        this.hidden = hidden;
    },
    ParallelModelNode: function(def, defs, relabel, hidden){
        this.type = 'parallel-model';
        this.definitions = defs ? [def].concat(defs) : [def];
        this.relabel = relabel;
        this.hidden = hidden;
    },
    DefinitionNode: function(name, process){
      this.type = 'definition';
      this.name = name;
      this.process = process;
    },
    ParallelDefinitionNode: function(name, process){
      this.type = 'parallel-definition';
      this.name = name;
      this.process = process;
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
    ActionNode: function(action){
      this.type = 'action';
      this.action = action;
    },
    RelabelNode: function(relabels){
      this.type = 'relabel';
      this.relabels = relabels;
    },
    HideNode: function(hidden){
      this.type = 'hide';
      this.hidden = hidden;
    },
    StopNode: function(){
      this.type = 'stop';
    },
    ErrorNode: function(){
      this.type = 'error';
    }
  };
}

/**
 * Represents the collection of Models parsed.
 */
File
  =  Model_OR_ParallelModel*

/**
 * Represents a single definition of a Model parsed.
 */
Model
  =  _ definition:Definition _ symbol_DefinitionListEnd _ {
      return new Node.ModelNode(definition);
     }
  /  _ definition:Definition _ relabel:Process_Relabel _ hide:Process_Hide _ symbol_DefinitionListEnd _ {
      return new Node.ModelNode(definition, undefined, relabel, hide);
     }
  /  _ definition:Definition _ relabel:Process_Relabel _ symbol_DefinitionListEnd _ {
      return new Node.ModelNode(definition, undefined, relabel, undefined);
     }
  /  _ definition:Definition _ symbol_DefinitionListSeparator _ model:Model _  {
      return new Node.ModelNode(definition, model.definitions);
     }
  /  _ definition:Definition _ hide:Process_Hide _ symbol_DefinitionListEnd _ {
      return new Node.ModelNode(definition, undefined, undefined, hide);
     }

/**
 * Represents a single definition of a Model comprised from parallel composition.
 */
ParallelModel
  =  symbol_Parallel _ definition:Parallel_Definition _ symbol_DefinitionListEnd _ {
      return new Node.ParallelModelNode(definition);
     }
  /  symbol_Parallel _ definition:Parallel_Definition _ relabel:Process_Relabel _ symbol_DefinitionListEnd _ {
      return new Node.ParallelModelNode(definition, undefined, relabel, undefined);
     }
  /  symbol_Parallel _ definition:Parallel_Definition _ hide:Process_Hide _ symbol_DefinitionListEnd _ {
      return new Node.ParallelModelNode(definition, undefined, undefined, hide);
     }

/**
 * Constructs a standard definition of an automaton.
 */
Definition
  =  name:Name _ symbol_DefinitionAssignment _ process:Process_Standard {
      return new Node.DefinitionNode(name, process);
     }

/**
 * Constructs a definition of a parallel composition of two automata.
 */
Parallel_Definition
  =  name:Name _ symbol_DefinitionAssignment _ process:Process_Parallel {
      var n = new Node.NameNode("||" + name.name);
      return new Node.ParallelDefinitionNode(n, process);
     }

/**
 * Processes the standard definition of an automaton.
 */
Process_Standard
  =  a:Name_OR_Choice _ b:Process_Standard_Nested {
      return new Node.ParallelNode(a, b);
     }
  /  Process_Choice

/**
 * Processes the standard definition of an automaton with a nested statement within.
 */
Process_Standard_Nested
  =  a:Name_OR_Choice _ b:Process_Standard_Nested {
      return new Node.ParallelNode(a, b);
     }
  / Process_Choice

/**
 * Processes the definition of a parallel composition of two automata.
 */
Process_Parallel
  =  symbol_BracketLeft _ a:Name_OR_Label _ symbol_Parallel _ b:Process_Parallel_Nested {
      console.log("parsing process parallel");
      return new Node.ParallelNode(a, b);
     }
  / Process_Parallel_Composition

/**
 * Processes the definition of a parallel composition of two automata
 * with a nested statement within.
 */
Process_Parallel_Nested
  =  a:Name_OR_Label _ symbol_Parallel _ b:Process_Parallel_Nested {
      console.log("process parallel nested");
      console.log(a);
      console.log(b);
      return new Node.ParallelNode(a, b);
     }
  /  Process_Parallel_Composition

/**
 * Processes a definition which has a choice within it.
 */
Process_Choice
  =  a:Process_Sequence _ symbol_Choice _ b:Process_Choice {
      return new Node.ChoiceNode(a, b);
     }
  /  Process_Sequence

/**
 * Processes a normal sequence from one state to another via an action.
 */
Process_Sequence
  =  from:Action _ symbol_Sequence _ to:Name_OR_Sequence {
      return new Node.SequenceNode(from, to);
     }
  /  Terminal_OR_Brackets

/**
 * Processes a single parallel composition.
 */
Process_Parallel_Composition
  =  a:Name_OR_Label _ symbol_Parallel _ b:Name_OR_Label {
      return new Node.ParallelNode(a, b);
     }
  /  Brackets

/**
 * Processes the defining of a new label for an action.
 */
Process_Relabel
  =  symbol_Relabel _ symbol_BraceLeft _ relabel:Relabel_OR_Brace {
      return new Node.RelabelNode(relabel);
     }

/**
 * Processes the hiding of an action
 */
Process_Hide
  =  symbol_Hide _ symbol_BraceLeft _ hide:Action_OR_Brace {
      return new Node.HideNode(hide);
     }

/**
 * Attempts to parse either a Model or a ParallelModel.
 */
Model_OR_ParallelModel
  =  Model
  /  ParallelModel

/**
 * Attempts to parse either a Label or a parallel process.
 */
Label_OR_Process_Parallel
  =  Label
  /  Process_Parallel

/**
 * Attempts to parse either a Name or a Choice.
 */
Name_OR_Choice
  = Name
  / Process_Choice

/**
 * Attempts to parse either a Name or a Sequence. 
 */
Name_OR_Sequence
  =  Process_Sequence
  /  Name

/**
 * Attempts to parse either a Name or a Label.
 */
Name_OR_Label
  =  Name
  /  Label

/**
 * Attempts to continue parsing a relabelling or the end of the relabelling.
 */
Relabel_OR_Brace
  =  relabel:Relabel _ symbol_BraceRight { return relabel; }
  /  a:Relabel _ symbol_DefinitionListSeparator _ b:Relabel_OR_Brace {
      return new Node.RelabelNode([a].concat(b));
  }

/**
 * Attempts to continue parsing actions to hide or the end of the actions
 * to be hidden.
 */
Action_OR_Brace
  =  a:Action _ symbol_BraceRight {
      return new Node.HideNode(a.action);
     }
  / a:Action _ symbol_DefinitionListSeparator _ b:Action_OR_Brace {
        return new Node.HideNode([a.action].concat(b.hidden));
     }

/**
 * Attempts to parse either a Terminal or Brackets for a standard definition.
 */
Terminal_OR_Brackets
  =  Terminal
  /  symbol_BracketLeft _ process:Process_Standard _ symbol_BracketRight {
      return process;
     }

/**
 * Attempts to parse a parallel composition within brackets.
 */
Brackets
  =  symbol_BracketLeft _ process:Process_Parallel _ symbol_BracketRight {
      return process;
     }

/**
 * Attempts to parse either a Stop or an Error.
 */
Terminal
  =  Stop
  /  Error

/**
 * Signifies the end of a sequence.
 */
Stop
  =  'STOP' {
      return new Node.StopNode();
     }

/**
 * Signifies an error in the parsing of a definition.
 */
Error
  =  'ERROR' {
      return new Node.ErrorNode();
     }

/**
 * The name given to a definition of an automaton.
 */
Name
  =  name:$([A-Z][A-Za-z0-9_]*) {
      return new Node.NameNode(name);
     }

/**
 * A process which takes an automaton from one state to another.
 */
Action
  =  action:$([a-z][A-Za-z0-9_]*) {
      return new Node.ActionNode(action);
     }

/**
 * A new label to be given to the definition of an automaton.
 */
Label
  =  label:Action _ symbol_Label _ name:Name {
      var temp = {"label":label.action, "name":name.name}; 
        return temp;
     }

/**
 * 
 */
Relabel
  =  a:Action _ symbol_Relabel _ b:Action {
      var relabel = {"new-label":a.action, "old-label": b.action};
        return new Node.RelabelNode(relabel);
     }

/**
 * Symbols used in parsing.
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

_ 'optional whitespace'
  =  [ \t\n\r]*