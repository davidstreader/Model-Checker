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
    RelabelNode:      function(relabels)         { this.type = 'relabel';     this.relabels = relabels;                              },
    HideNode:       function(hidden)             { this.type = 'hide';        this.hidden = hidden;                                  },
    StopNode:       function()                   { this.type = 'stop';                                                               },
    ErrorNode:      function()                   { this.type = 'error';                                                              }
  };
}

File
  =  Model*

Model
  =  _ definition:Definition _ symbol_DefinitionListEnd _ { return new Node.ModelNode(definition); }
  /  _ definition:Definition _ relabel:Process_Relabel _ symbol_DefinitionListEnd _ { return new Node.ModelNode(definition, undefined, relabel, undefined); }
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

Process_Relabel
  =  symbol_Relabel _ symbol_BraceLeft _ relabel:Relabel_OR_Brace { return new Node.RelabelNode(relabel); }

Process_Hide
  =  symbol_Hide _ symbol_BraceLeft _ hide:Action_OR_Brace { return new Node.HideNode(hide); }

Name_OR_Choice
  = Name
  / Process_Choice

Name_OR_Sequence
  =  Process_Sequence
  /  Name

Relabel_OR_Brace
  =  relabel:Relabel _ symbol_BraceRight { return relabel; }
  /  a:Relabel _ symbol_DefinitionListSeparator _ b:Relabel_OR_Brace {
      return new Node.RelabelNode([a].concat(b));
  }

Action_OR_Brace
  =  a:Action _ symbol_BraceRight { return new Node.HideNode(a.action); }
  / a:Action _ symbol_DefinitionListSeparator _ b:Action_OR_Brace {
      return new Node.HideNode([a.action].concat(b.hidden));
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
  
Relabel
  =  a:Action _ symbol_Relabel _ b:Action {
          var relabel = {"old-label":a.action, "new-label": b.action};
          return new Node.RelabelNode(relabel);
     }

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
symbol_Relabel                 = '/'
symbol_Hide                    = '\\'

_ 'optional whitespace'
  =  [ \t\n\r]*
