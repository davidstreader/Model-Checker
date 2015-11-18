{
  var Node = {
    ModelNode:function(def, defs, relabel, hidden){
    	this.type = 'model';
    	this.definitions = defs ? [def].concat(defs) : [def];
        this.relabel = relabel;
        this.hidden = hidden;
    },
    DefinitionNode: function(name, process)      { this.type = 'definition';  this.name = name;            this.process = process;   },
    SequenceNode:   function(from, to)           { this.type = 'sequence';    this.from = from;            this.to = to;             },
    ChoiceNode:     function(option1, option2)   { this.type = 'choice';      this.option1 = option1;      this.option2 = option2;   },
    ParallelNode:   function(def1, def2)         { this.type = 'parallel';    this.definition1 = def1;     this.definition2 = def2;  },
    NameNode:       function(name)               { this.type = 'name';        this.name = name;                                      },
    ActionNode:     function(action)             { this.type = 'action';      this.action = action;                                  },
    HideNode:       function(hidden)             { this.hidden = hidden;                                                             },
    StopNode:       function()                   { this.type = 'stop';                                                               },
    ErrorNode:      function()                   { this.type = 'error';                                                              }
  };
}

File
  =  Model*

Model
  =  _ definition:Definition _ symbol_DefinitionListEnd _ { return new Node.ModelNode(definition); }
  /  _ definition:Definition _ symbol_DefinitionListSeparator _ model:Model _  { return new Node.ModelNode(definition, model.definitions); }
  /  _ definition:Definition _ hide:Process_Hide _ symbol_DefinitionListEnd _ { return new Node.ModelNode(definition, undefined, undefined, hide); }

Definition
  =  name:Name _ symbol_DefinitionAssignment _ process:Process_Standard { return new Node.DefinitionNode(name, process); }

Process_Standard
  =  a:Name_OR_Choice _ b:Process_Standard_Nested { return new Node.ParallelNode(a, b); }
  /  Process_Choice

Process_Standard_Nested
  =  a:Name_OR_Choice _ b:Process_Standard_Nested { return new Node.ParallelNode(a, b); }
  / Process_Choice

Process_Choice
  =  a:Process_Sequence _ symbol_Choice _ b:Process_Choice { return new Node.ChoiceNode(a, b); }
  /  Process_Sequence

Process_Sequence
  =  from:Action _ symbol_Sequence _ to:Name_OR_Sequence { return new Node.SequenceNode(from, to); }
  /  Terminal_OR_Brackets

Process_Hide
  =  symbol_Hide _ symbol_BraceLeft _ hide:Action_OR_Brace { return new Node.HideNode(hide); }

Name_OR_Choice
  = Name
  / Process_Choice

Name_OR_Sequence
  =  Process_Sequence
  /  Name

Action_OR_Brace
  =  a:Action _ symbol_BraceRight { return new Node.HideNode(a.action); }
  / a:Action _ symbol_DefinitionListSeparator _ b:Action_OR_Brace {
  		return new Node.HideNode([a].concat(b));
  }

Terminal_OR_Brackets
  =  Terminal
  /  symbol_BracketLeft _ process:Process_Standard _ symbol_BracketRight { return process; }

Terminal
  =  Stop
  /  Error

Stop
  =  'STOP' { return new Node.StopNode(); }

Error
  =  'ERROR' { return new Node.ErrorNode(); }

Name
  =  name:$([A-Z][A-Za-z0-9_]*) { return new Node.NameNode(name); }

Action
  =  action:$([a-z][A-Za-z0-9_]*) { return new Node.ActionNode(action); }

symbol_BracketLeft             = '('
symbol_BracketRight            = ')'
symbol_BraceLeft               = '{'
symbol_BraceRight              = '}'
symbol_DefinitionListEnd       = '.'
symbol_DefinitionListSeparator = ','
symbol_DefinitionAssignment    = '='
symbol_Parallel                = '||'
symbol_Choice                  = '|'
symbol_Sequence                = '->'
symbol_Hide                    = '\\'

_ 'optional whitespace'
  =  [ \t\n\r]*
