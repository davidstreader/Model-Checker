{
  var Node = {
      ModelNode: function(definition){
          this.type = 'model';
          this.definitions = [definition];
        },
      ConstantNode: function(name, expression){
          this.type = 'constant';
          this.name = name;
            this.expression = expression;
        },
        RangeNode: function(name, start, end){
          this.type = 'range';
          this.name = name;
          this.start = start;
          this.end = end;
        },
        DefinitionNode: function(name, process, relabel, hidden){
          this.type = 'definition'; // needs to be changed later on
            this.name = name;
            this.process = process;
            this.relabel = (relabel === null) ? undefined : relabel;
            this.hidden = (hidden === null) ? undefined : hidden;
        },
        OperationNode: function(type, operation1, operation2, isNegated){
          this.type = type;
            this.operation1 = operation1;
            this.operation2 = operation2;
            this.isNegated = isNegated;
        },
        RelabelNode: function(relabel){
          this.type = 'relabel';
            this.relabel = relabel;
        },
        NameNode: function(name, label){
          this.type = 'name';
            this.name = name;
        },
        VariableNode: function(name){
          this.type = 'variable'
            this.name = name;
        },
        ActionNode: function(action){
          this.type = 'action';
            this.action = action;
        },
        SetNode: function(set){
          this.type = 'set';
            this.set = set;
        },
        SequenceNode: function(from, to){
          this.type = 'sequence';
            //TODO: this.guard = guard;
            this.from = from;
            this.to = to;
        },
        ChoiceNode: function(option1, option2){
          this.type = 'choice';
            this.option1 = option1;
            this.option2 = option2;
        },
        ParallelNode: function(definition1, definition2){
          this.type = 'parallel';
            this.definition1 = definition1;
            this.definition2 = definition2;
        },
        ReferenceNode: function(name, label){
          this.type = 'reference';
            this.name = name;
            this.label = (label === null) ? undefined : label;
        },
        FunctionNode: function(type, process){
          this.type = type;
            this.process = process;
        },
        CompositeNode: function(label, composite, relabel){
          this.type = 'composite';
            this.label = (label === null) ? undefined : label;
            this.composite = composite;
            this.relabel = (relabel === null) ? undefined : relabel;
        },
        StopNode: function(){
          this.type = 'stop';
        },
        ErrorNode: function(){
          this.type = 'error';
        }
    };
    
    function constructSequence(sequence){
      var to = sequence.pop();
        var from = sequence.pop();
        var node = new Node.SequenceNode(from, to);
        
        while(sequence.length !== 0){
          from = sequence.pop();
            node = new Node.SequenceNode(from, node);
        }
        
        return node;
    };
    
    function getOperation(operation){
      switch(operation){
          case '~':
              return 'bisimulation';
            default:
              return undefined;
        }
    };
}

ParseTree = (FSP / ConstantDefinition / RangeDefinition / OperationDefinition)*

FSP = _ definition:(ProcessDefinition / ReferenceDefinition / FunctionDefinition / CompositeDefinition) _ {
  return new Node.ModelNode(definition);
}

/**
 * IDENTIFIERS
 */

Name = name:UpperCaseIdentifier { return new Node.NameNode(name); }
Variable = variable:LowerCaseIdentifier { return new Node.VariableNode(variable); }

UpperCaseIdentifier = $([A-Z][A-Za-z0-9_]*) { return text(); }
LowerCaseIdentifier = $([a-z][A-Za-z0-9_]*) { return text(); }
IntegerLiteral = [-]?[0-9]+ { return parseInt(text(), 10); }

/**
 * ACTION LABELS
 */

ActionLabel = action:(DottedAction / LowerCaseIdentifier) {
  return new Node.ActionNode(action);
}

ActionLabels = ActionLabel

DottedAction = a:LowerCaseIdentifier '.' b:LowerCaseIdentifier { return a + '.' + b; }

Set = Name
    / '{' _ a:SetElements _ '}' { return new Node.SetNode(a); }

SetElements = a:ActionLabels _ b:_SetElements { return [a].concat(b); }
            / ActionLabels

_SetElements = ',' _ a:ActionLabels _ b:(_SetElements ?) {
  if(b === null){ return [a]; }
    return [a].concat(b);
}

/**
 * CONSTANT, RANGE AND SET
 */

ConstantDefinition = 'const' _ name:Name _ '=' _ exp:SimpleExpression { return new Node.ConstantNode(name, exp); }

RangeDefinition = 'range' _ name:Name _ '=' _ start:SimpleExpression _ '..' _ end:SimpleExpression { return new Node.RangeNode(name, start, end); }

/**
 * PROCESS DEFINITIONS
 */

ProcessDefinition = _ name:Name _ '=' _ process:ProcessBody _ relabel:(Relabel ?) _ hiding:(Hiding ?) _ '.' _ { return new Node.DefinitionNode(name, process, relabel, hiding); }// TODO: _ relabel:(Relabel ?) _ hide:(Hiding ?) _ '.' _ { return text(); }

ProcessBody = LocalProcess
LocalProcessDefinition = name:Name _ '=' _ process:LocalProcess { return new Node.DefinitionNode(name, process); }

LocalProcess = '(' _ choice:Choice _ ')' { return choice; }
             / Choice
             / BaseLocalProcess

BaseLocalProcess = 'STOP' { return new Node.StopNode(); }
                 / 'ERROR' { return new Node.ErrorNode(); }
                 / Name

Choice = a:ActionPrefix _ b:_Choice { return new Node.ChoiceNode(a, b); }
       / ActionPrefix
       
/* Used to remove left recursion from 'Choice' */       
_Choice = _ '|' _ a:ActionPrefix _ b:(_Choice ?) { if(b === null){ return a;} return new Node.ChoiceNode(a, b); }

ActionPrefix = a:PrefixActions _ '->' _ b:LocalProcess {
  if(a.constructor !== Array){
      return constructSequence([a].concat(b));
    }
    return constructSequence(a.concat(b));
}

PrefixActions = a:ActionLabels _ b:_PrefixActions _ { return [a].concat(b); }
              / ActionLabels

/* Used to remove left recursion from 'PrefixActions' */
_PrefixActions = _ '->' _ a:ActionLabels _ b:(_PrefixActions ?) {
  // return the action label if parser failed to parse another prefix action
    if(b === null){ return [a]; }
    // otherwise return new sequence node
  return [a].concat(b);
}

/**
 * REFERENCE DEFINITIONS
 */

ReferenceDefinition = name:Name _ '=' _ label:(PrefixLabel ?) _ ref:Name _ relabel:(Relabel ?) _ hide:(Hiding ?) _ '.' {
  return new Node.DefinitionNode(name, new Node.ReferenceNode(ref, label), relabel, hide);
}

ReferenceBody = label:(PrefixLabel ?) _ ref:Name { return new Node.ReferenceNode(ref, label); }

/**
 * PARALLEL COMPOSITION DEFINITONS
 */
 
CompositeDefinition = ('||' ?) _ name:Name _ '=' _ body:CompositeBody _ hide:(Hiding ?) _ '.' {
  return new Node.DefinitionNode(name, body, undefined, hide);
}

CompositeBody = label:(PrefixLabel ?) _ name:Name _ relabel:(Relabel ?) { return new Node.CompositeNode(label, new Node.NameNode(name), relabel); }
              / label:(PrefixLabel ?) _ '(' _ comp:ParallelComposition _ ')' _ relabel:(Relabel ?) { return new Node.CompositeNode(label, comp, relabel); }
              
PrefixLabel = label:ActionLabels _ '::' { return label; }
            / label:ActionLabels _ ':' { return label; }
            /// a:ActionLabels _ '::' _ b:ActionLabel _ ':' // TODO

ParallelComposition = body:CompositeBody _ comp:_ParallelComposition { return new Node.ParallelNode(body, comp); }

_ParallelComposition = '||' _ body:CompositeBody _ comp:(_ParallelComposition ?) {
  if(comp === null){
      return body;
    }
    return new Node.ParallelNode(body, comp);
}

/**
 * FUNCTION DEFINITONS
 */

FunctionDefinition = name:Name _ '=' _ type:FunctionType _ '(' _ body:FunctionBody _ ')' _ relabel:(Relabel ?) _ hide:(Hiding ?) _ '.' {
  return new Node.DefinitionNode(name, body, relabel, hide);
}

FunctionType = 'abs' { return 'abstraction'; }
             / 'simp' { return 'simplification'; }

FunctionBody = type:FunctionType _ '(' body:FunctionBody ')' _ relabel:(Relabel ?) _ hide:(Hiding ?) _ { return new Node.DefinitionNode(undefined, body, relabel, hide); }
             / _ body:ProcessBody _ relabel:(Relabel ?) _ hide:(Hiding ?) _ { return new Node.DefinitionNode(undefined, body, relabel, hide); }
             / _ body:CompositeBody _ relabel:(Relabel ?) { return new Node.DefinitionNode(undefined, body, relabel, undefined); }
/**
 * RELABELLING AND HIDING
 */

Relabel = '/' _ '{' a:RelabelDefinitions _ '}' { return a }

RelabelDefinitions = a:RelabelDefinition _ b:_RelabelDefinitions { return a.concat(b); }
                   / RelabelDefinition

_RelabelDefinitions = ',' _ a:RelabelDefinition _ b:(_RelabelDefinitions ?) {
  if(b === null){ return a; }
    return a.concat(b);
}

RelabelDefinition = a:ActionLabels _ '/' _ b:ActionLabels { return [{old: a, new: b}]; }

Hiding = '\\' _ a:Set { return {type: 'includes', set:a.set}; }
       / '@' _ a:Set { return {type: 'excludes', set:a.set}; }

/**
 * OPERATIONS
 */
 
OperationDefinition = a:OperationProcess _ negate:('!' ?) _ op:Operation _ b:OperationProcess _ '.' {
    var isNegated = (negate === null) ? false : true;
    return new Node.OperationNode(op, a, b, isNegated);
}

OperationProcess = ProcessBody / ReferenceBody / FunctionBody / CompositeBody

Operation = '~' { return 'bisimulation'; }

/**
 * EXPRESSIONS
 */

SimpleExpression = AdditiveExpression

AdditiveExpression = a:MultiplicativeExpression _ '+' _ b:AdditiveExpression { return a + ' + ' + b; }
                   / a:MultiplicativeExpression _ '-' _ b:AdditiveExpression { return a + ' - ' + b; }
                   / MultiplicativeExpression

MultiplicativeExpression = a:UnaryExpression _ '*' _ b:MultiplicativeExpression { return a + ' * ' + b; }
                         / a:UnaryExpression _ '/' _ b:MultiplicativeExpression { return a + ' / ' + b; }
                         / a:UnaryExpression _ '%' _ b:MultiplicativeExpression { return a + ' % ' + b; } 
                         / UnaryExpression

UnaryExpression = _ '+' _ base:BaseExpression { return ' + ' + base; }
                / _ '-' _ base:BaseExpression { return ' - ' + base; }
                / BaseExpression

BaseExpression = IntegerLiteral / LowerCaseIdentifier / UpperCaseIdentifier

/* Parses whitespace */
_ 'whitespace' = [ \t\n\r]*