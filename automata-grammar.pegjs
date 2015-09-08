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
  =  _ definition:Definition _ '.' _ { return new Node.ModelNode(definition); }
  /  _ definition:Definition _ ',' _ model:Model _  { return new Node.ModelNode(definition, model.definitions); }

Definition
  =  name:Name _ '=' _ process:Process { return new Node.DefinitionNode(name, process); }

Process
  =  '(' _ process:Process _ ')' { return process; }
  /  r:Parallel { return r; }
  /  r:Sequence { return r; }
  /  r:Choice   { return r; }
  /  r:Terminal { return r; }
  /  r:Name     { return r; }

Sequence
  =  from:Label _ '->' _ to:Process { return new Node.SequenceNode(from, to); }

Choice
  =  '(' _ a:Process _ '|' _ b:Process _ ')' { return new Node.ChoiceNode(a, b); }
  /  '(' _ a:Process _ '|' _ b:Action _ ')'  { return new Node.ChoiceNode(a, b); }
  /  a:Action   _ '|' _ b:Process            { return new Node.ChoiceNode(a, b); }
  /  a:Action   _ '|' _ b:Action             { return new Node.ChoiceNode(a, b); }

Parallel
  =  a:Name _ '||' _ b:Name { return new Node.ParallelNode(a, b); }

Label
  =  Name
  /  Action

Name
  =  name:$([A-Z][A-Za-z0-9_]*) { return new Node.NameNode(name); }

Action
  =  action:$([a-z][A-Za-z0-9_]*) { return new Node.ActionNode(action); }

Terminal
  =  Stop
  /  Error

Stop
  =  'STOP' { return new Node.StopNode(); }

Error
  =  'ERROR' { return new Node.ErrorNode(); }

_ 'optional whitespace'
  =  [ \t\n\r]*
