{
  var Node = {
    ModelNode:      function(def, defs)          { this.type = 'model';       this.definitions = defs ? [def].concat(defs) : [def];  },
    DefinitionNode: function(name, process)      { this.type = 'definition';  this.name = name;            this.process = process;   },
    SequenceNode:   function(from, to)           { this.type = 'sequence';    this.from = from;            this.to = to;             },
    ChoiceNode:     function(option1, option2)   { this.type = 'choice';      this.option1 = option1;      this.option2 = option2;   },
    ParallelNode:   function(def1, def2)         { this.type = 'parallel';    this.definition1 = def1;     this.definition2 = def2;  },
    NameNode:       function(name)               { this.type = 'name';        this.name = name;                                      },
    ActionNode:     function(action)             { this.type = 'action';      this.action = action;                                  },
    StopNode:       function()                   { this.type = 'stop';                                                               },
    ErrorNode:      function()                   { this.type = 'error';                                                              }
  };
}

File
  =  Model*

Model
  =  _ definition:Definition _ symbol_DefinitionListEnd _ { return new Node.ModelNode(definition); }
  /  _ definition:Definition _ symbol_DefinitionListSeparator _ model:Model _  { return new Node.ModelNode(definition, model.definitions); }

Definition
  =  name:Name _ symbol_DefinitionAssignment _ process:Process_A { return new Node.DefinitionNode(name, process); }

// Parallel
Process_A
  =  a:Process_B _ symbol_Parallel _ b:Process_A { return new Node.ParallelNode(a, b); }
  /  Process_B

// Choice
Process_B
  =  a:Process_C _ symbol_Choice _ b:Process_B { return new Node.ChoiceNode(a, b); }
  /  Process_C

// Sequence
Process_C
  =  from:Action _ symbol_Sequence _ to:Process_C { return new Node.SequenceNode(from, to); }
  /  Process_D

Process_D
  =  Stop
  /  Error
  /  Name
  /  symbol_BracketLeft _ process:Process_A _ symbol_BracketRight { return process; }

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
symbol_DefinitionListEnd       = '.'
symbol_DefinitionListSeparator = ','
symbol_DefinitionAssignment    = '='
symbol_Parallel                = '||'
symbol_Choice                  = '|'
symbol_Sequence                = '->'

_ 'optional whitespace'
  =  [ \t\n\r]*
