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
    ReferenceModelNode: function(def, defs, relabel, hidden){
      this.type = 'reference-model';
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
    ReferenceDefinitionNode: function(name, process){
      this.type = 'reference-definition';
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
    LabelledNameNode: function(name, label){
      this.type = 'labelled-name';
        this.name = name;
        this.label = label;
    },
    AbstractionNode: function(name){
        this.type = 'abstraction';
        this.name = name;
    },
    SimplificationNode: function(name){
        this.type = 'simplification';
        this.name = name;
    },
    ActionNode: function(action){
        this.type = 'action';
        this.action = action;
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
  =  _ definition:Definition _ process:Relabelling_AND_Hiding _ {
      return new Node.ModelNode(definition, undefined, process.relabel, process.hide);
     }
  /  _ definition:Definition _ symbol_DefinitionListSeparator _ model:Model _  {
        return new Node.ModelNode(definition, model.definitions, model.relabel, model.hidden);
     }

/**
 * Represents a single definition of a Model comprised from parallel composition.
 */
ParallelModel
  =  symbol_Parallel _ definition:Parallel_Definition _ symbol_DefinitionListEnd _ {
      return new Node.ParallelModelNode(definition);
     }
  /  symbol_Parallel _ definition:Parallel_Definition _ symbol_DefinitionListSeparator _ model:Model _ {
      return new Node.ParallelModelNode(definition, model.definitions, model.relabel, model.hidden);
     }
  /  symbol_Parallel _ definition:Parallel_Definition _ relabel:Process_Relabel _ hide:Process_Hide _ symbol_DefinitionListEnd _ {
      return new Node.ParallelModelNode(definition, undefined, relabel, hide); 
     }
  /  symbol_Parallel _ definition:Parallel_Definition _ relabel:Process_Relabel _ symbol_DefinitionListEnd _ {
      return new Node.ParallelModelNode(definition, undefined, relabel, undefined);
     }
  /  symbol_Parallel _ definition:Parallel_Definition _ hide:Process_Hide _ symbol_DefinitionListEnd _ {
      return new Node.ParallelModelNode(definition, undefined, undefined, hide);
     }

ReferenceModel
  =  definition:ReferenceDefinition _ process:Relabelling_AND_Hiding {
      return new Node.ReferenceModelNode(definition, undefined, process.relabel, process.hide);
     }

/**
 * Represents the process of relabelling and hiding after a definition.
 */
Relabelling_AND_Hiding
  =  relabel:Process_Relabel _ hide:Process_Hide _ symbol_DefinitionListEnd _ {
      return {relabel:relabel, hide:hide};
     }
  /  relabel:Process_Relabel _ symbol_DefinitionListEnd _ {
      return {relabel:relabel, hide:undefined};
     }
  /  hide:Process_Hide _ symbol_DefinitionListEnd _ {
      return {relabel:undefined, hide:hide};
     }
  /  symbol_DefinitionListEnd _ {
      return {relabel:undefined, hide:undefined};
     }

/**
 * Constructs a standard definition of an automaton.
 */
Definition
  =  name:Define_Name _ symbol_DefinitionAssignment _ process:Process_Standard {
        return new Node.DefinitionNode(name, process);
     }
  /  name:Define_Name _ symbol_DefinitionAssignment _ abstract:Process_Abstraction {
        return new Node.DefinitionNode(name, abstract);
     }
  /  name:Define_Name _ symbol_DefinitionAssignment _ simplify:Process_Simplification {
        return new Node.DefinitionNode(name, simplify);
     }

/**
 * Constructs a definition of a parallel composition of two automata.
 */
Parallel_Definition
  =  name:Define_Name _ symbol_DefinitionAssignment _ process:Process_Parallel {
      var n = new Node.NameNode("||" + name.name);
      return new Node.ParallelDefinitionNode(n, process);
     }
     
ReferenceDefinition
  =  name:Define_Name _ symbol_DefinitionAssignment _ defName:Name_OR_Label {
      return new Node.ReferenceDefinitionNode(name, defName);
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

Process_Parallel
  =  a:Name_OR_Label _ symbol_Parallel _ b:Process_Parallel_Nested {
      return new Node.ParallelNode(a, b);
     }
  / Process_Parallel_Composition
     
Process_Parallel_Nested
  =  a:Name_OR_Label _ symbol_Parallel _ b:Process_Parallel_Nested {
      return new Node.ParallelNode(a, b);
     }
  / Name_OR_Label

Process_Parallel_Composition
  =  a:Name_OR_Label _ symbol_Parallel _ b:Name_OR_Parallel_Composition {
      return new Node.ParallelNode(a, b);
     }
  /  Parallel_Brackets

/**
 * Processes the defining of a new label for an action.
 */
Process_Relabel
  =  symbol_Relabel _ symbol_BraceLeft _ relabel:Relabel_OR_Brace {
      return relabel;
     }

/**
 * Processes the hiding of an action
 */
Process_Hide
  =  symbol_Hide _ symbol_BraceLeft _ hide:Action_OR_Brace {
      return hide;
     }

Process_Abstraction
  =  symbol_Abstraction _ symbol_BracketLeft _ name:Process_Name _ symbol_BracketRight {
        return new Node.AbstractionNode(name);
     }
  /  symbol_Abstraction _ symbol_BracketLeft _ symbol_Parallel name:Process_Name _ symbol_BracketRight {
      console.log(name);
        return new Node.AbstractionNode(new Node.NameNode('||' + name.name));
     }
  
Process_Simplification
  =  symbol_Simplification _ symbol_BracketLeft _ name:Process_Name _ symbol_BracketRight {
        return new Node.SimplificationNode(name);
     }
  /  symbol_Simplification _ symbol_BracketLeft _ symbol_Parallel name:Process_Name _ symbol_BracketRight {
    return new Node.AbstractionNode(new Node.NameNode('||' + name.name));
     }

/**
 * Attempts to parse either a Model or a ParallelModel.
 */
Model_OR_ParallelModel
  =  Model
  /  ParallelModel
  /  ReferenceModel

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
  = Process_Name
  / Process_Choice

/**
 * Attempts to parse either a Name or a Sequence. 
 */
Name_OR_Sequence
  =  Process_Sequence
  /  Process_Name

Name_OR_Parallel_Composition
  =  Process_Parallel_Composition
  /  Name_OR_Label

/**
 * Attempts to parse either a Name or a Label.
 */
Name_OR_Label
  =  name:Define_Name {
      return new Node.LabelledNameNode(name.name, undefined);
     }
  /  Label

/**
 * Attempts to continue parsing a relabelling or the end of the relabelling.
 */
Relabel_OR_Brace
  =  relabel:Relabel _ symbol_BraceRight { return [relabel]; }
  /  a:Relabel _ symbol_DefinitionListSeparator _ b:Relabel_OR_Brace {
      return b.concat(a);
     }

/**
 * Attempts to continue parsing actions to hide or the end of the actions
 * to be hidden.
 */
Action_OR_Brace
  =  a:Action _ symbol_BraceRight {
      return [a.action];
     }
  / a:Action _ symbol_DefinitionListSeparator _ b:Action_OR_Brace {
        return b.concat(a.action);
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
Parallel_Brackets
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
Define_Name
  =  name:$([A-Z][A-Za-z0-9_]*) {
      return new Node.NameNode(name);
     }

Process_Name
  =  Define_Name
  /  Process_Abstraction
  /  Process_Simplification

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
  =  label:Action _ symbol_Label _ name:Define_Name {
      return new Node.LabelledNameNode(name.name, label.action);
     }

/**
 * 
 */
Relabel
  =  label:Action _ symbol_Relabel _ a:Action symbol_DefinitionListEnd b:Action {
      var oldLabel = a.action + "." + b.action;
      return {"new-label":label.action, "old-label":oldLabel};
     }
  /  a:Action _ symbol_Relabel _ b:Action {
      return {"new-label":a.action, "old-label": b.action};
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
symbol_Abstraction = 'abs'
symbol_Simplification = 'simp'

_ 'optional whitespace'
  =  [ \t\n\r]*
