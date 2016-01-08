{
  var Node = {
      ModelNode: function(definition){
        this.type = 'model';
        this.definitions = processLocalDefinitions(definition);
        this.position = location();
      },
      ConstantNode: function(name, expression){
        this.type = 'constant';
        this.name = name;
        this.expression = expression;
        this.position = location();
      },
      RangeNode: function(name, start, end){
        this.type = 'range';
        this.name = name;
        this.start = start;
        this.end = end;
        this.position = location();
      },
      DefinitionNode: function(name, process, relabel, hidden, isVisible){
        this.type = 'definition'; // needs to be changed later on
        this.name = name;
        this.process = process;
        this.relabel = (relabel === null) ? undefined : relabel;
        this.hidden = (hidden === null) ? undefined : hidden;
        this.isVisible = (isVisible === null) ? true : false;
        this.position = location();
      },
      OperationNode: function(process, input, definition1, definition2, isNegated){
        this.type = 'operation';
        this.process = process;
        this.input = input;
        this.definition1 = definition1;
        this.definition2 = definition2;
        this.isNegated = isNegated;
        this.position = location();
      },
      RelabelNode: function(relabel){
        this.type = 'relabel';
        this.relabel = relabel;
        this.position = location();
      },
      NameNode: function(name, label){
        this.type = 'name';
        this.name = name;
        this.position = location();
      },
      VariableNode: function(name){
        this.type = 'variable'
        this.name = name;
        this.position = location();
      },
      ActionNode: function(action){
        this.type = 'action';
        this.action = action;
        this.position = location();
      },
      SetNode: function(set){
        this.type = 'set';
        this.set = set;
        this.position = location();
      },
      SequenceNode: function(from, to){
        this.type = 'sequence';
        //TODO: this.guard = guard;
        this.from = from;
        this.to = to;
        this.position = location();
      },
      ChoiceNode: function(option1, option2){
        this.type = 'choice';
        this.option1 = option1;
        this.option2 = option2;
        this.position = location();
      },
      ParallelNode: function(definition1, definition2){
        this.type = 'parallel';
        this.definition1 = definition1;
        this.definition2 = definition2;
        this.position = location();
      },
      LabelNode: function(name, label){
        this.type = 'label';
        this.name = name;
        this.label = (label === null) ? undefined : label;
        this.position = location();
      },
      FunctionNode: function(type, process){
        this.type = type;
        this.process = process;
        this.position = location();
      },
      CompositeNode: function(label, composite, relabel){
        this.type = 'composite';
        this.label = (label === null) ? undefined : label;
        this.composite = composite;
        this.relabel = (relabel === null) ? undefined : relabel;
        this.position = location();
      },
      StopNode: function(){
        this.type = 'stop';
        this.position = location();
      },
      ErrorNode: function(){
        this.type = 'error';
        this.position = location();
      },
      CommentNode: function(comment){
        this.type = 'comment';
        this.comment = comment;
        this.position = location();
      },
      SimpleExpressionNode: function(expression){
        this.type = 'simple-expression';
        this.expression = expression;
      }
    };
    
    /**
     * Constructs and returns the JSON representation of a
     * sequence from the specified array of actions.
     */
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
    
    function processLocalDefinitions(definition){
      var definitions = [];
        var local = undefined;
    do{
          local = definition.process.local;
          delete definition.process['local'];
          definitions.push(definition);
          definition = local;
    }while(local !== undefined);
        
        return definitions;
    };
    
    /**
     * Returns the type of operation specified by the given operator.
     */
    function getOperation(operator){
      switch(operator){
        case '~':
          return 'bisimulation';
        default:
          return undefined;
      }
    };
}

ParseTree = (FSP / ConstantDefinition / RangeDefinition / OperationDefinition / Comment)*

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

ProcessDefinition = _ name:Name _ isVisible:('*' ?) _ '=' _ process:ProcessBody _ relabel:(Relabel ?) _ hiding:(Hiding ?) _ '.' _ { return new Node.DefinitionNode(name, process, relabel, hiding, isVisible); }

ProcessBody = a:LocalProcess _ ',' _ b:LocalProcessDefinitions { a.local = b; return a; }
            / LocalProcess

LocalProcessDefinitions = a:LocalProcessDefinition _ b:(_LocalProcessDefinitions ?) {
  if(b === null){
      return a;
    }
    a.process.local = b;
    return a;
}

_LocalProcessDefinitions = _ ',' _ a:LocalProcessDefinition _ b:(_LocalProcessDefinitions ?) {
  if(b === null){
      return a;
    }
    a.process.local = b;
    return a;
}

LocalProcessDefinition = name:Name _ '=' _ process:LocalProcess { return new Node.DefinitionNode(name, process); }

LocalProcess = '(' _ choice:Choice _ ')' { return choice; }
             / Choice
             / BaseLocalProcess

BaseLocalProcess = 'STOP' { return new Node.StopNode(); }
                 / 'ERROR' { return new Node.ErrorNode(); }
                 / '(' _ 'STOP' _ ')' { return new Node.StopNode(); }
                 / '(' _ 'ERROR' _ ')' { return new Node.ErrorNode(); }
                 / Name

Choice = a:ActionPrefix _ b:_Choice { return new Node.ChoiceNode(a, b); }
       / ActionPrefix
       
/* Used to remove left recursion from 'Choice' */       
_Choice = _ '|' _ a:ActionPrefix _ b:(_Choice ?) { if(b === null){ return a; } return new Node.ChoiceNode(a, b); }

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

ReferenceDefinition = name:Name _ isVisible:('*' ?) _ '=' _ label:(PrefixLabel ?) _ ref:Name _ relabel:(Relabel ?) _ hide:(Hiding ?) _ '.' {
  return new Node.DefinitionNode(name, new Node.CompositeNode(label, ref), relabel, hide, isVisible);
}

ReferenceBody = label:(PrefixLabel ?) _ ref:Name { return new Node.CompositeNode(label, ref); }

/**
 * PARALLEL COMPOSITION DEFINITONS
 */
 
CompositeDefinition = ('||' ?) _ name:Name _ isVisible:('*' ?) _ '=' _ body:CompositeBody _ hide:(Hiding ?) _ '.' {
  return new Node.DefinitionNode(name, body, undefined, hide, isVisible);
}

CompositeBody = label:(PrefixLabel ?) _ name:Name _ relabel:(Relabel ?) { return new Node.CompositeNode(label, name, relabel); }
              / label:(PrefixLabel ?) _ '(' _ comp:ParallelComposition _ ')' _ relabel:(Relabel ?) { return new Node.CompositeNode(label, comp, relabel); }
              
PrefixLabel = label:ActionLabels _ '::' { return label; }
            / label:ActionLabels _ ':' { return label; }
            /// a:ActionLabels _ '::' _ b:ActionLabel _ ':' // TODO

ParallelComposition = body:CompositeBody _ comp:_ParallelComposition { return new Node.ParallelNode(body, comp); }
                    / CompositeBody

_ParallelComposition = '||' _ body:CompositeBody _ comp:(_ParallelComposition ?) {
  if(comp === null){
      return body;
    }
    return new Node.ParallelNode(body, comp);
}

/**
 * FUNCTION DEFINITONS
 */

FunctionDefinition = name:Name _ isVisible:('*' ?) _ '=' _ type:FunctionType _ '(' _ body:FunctionBody _ ')' _ relabel:(Relabel ?) _ hide:(Hiding ?) _ '.' {
  var process = new Node.FunctionNode(type, body);
  return new Node.DefinitionNode(name, process, relabel, hide, isVisible);
}

FunctionType = 'abs' { return 'abstraction'; }
             / 'simp' { return 'simplification'; }

FunctionBody = type:FunctionType _ '(' body:FunctionBody ')' _ relabel:(Relabel ?) _ hide:(Hiding ?) _ { return new Node.DefinitionNode(undefined, new Node.FunctionNode(type, body), relabel, hide); }
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

RelabelDefinition = a:ActionLabels _ '/' _ b:ActionLabels { return [{new: a, old: b}]; }

Hiding = '\\' _ a:Set { return {type: 'includes', set:a.set}; }
       / '@' _ a:Set { return {type: 'excludes', set:a.set}; }

/**
 * OPERATIONS
 */
 
OperationDefinition = _ a:OperationProcess _ negate:('!' ?) _ op:Operation _ b:OperationProcess _ '.' _ {
    var isNegated = (negate === null) ? false : true;
    return new Node.OperationNode(op, text(), a, b, isNegated);
}

OperationProcess = BaseLocalProcess / ProcessBody / FunctionBody / CompositeBody / ReferenceBody

Operation = '~' { return 'bisimulation'; }

/**
 * EXPRESSIONS
 */

SimpleExpression = expr:AdditiveExpression { return new Node.SimpleExpressionNode(expr); }

AdditiveExpression = base:BaseExpression _ add:(_AdditiveExpression) { return base + ' ' + add; }
                   / MultiplicativeExpression

_AdditiveExpression = operator:('+' / '-')  _ multi:MultiplicativeExpression _ add:(_AdditiveExpression ?) {
  if(add !== null){
      return multi + ' ' + add + ' ' + operator;
    }
      return multi + ' ' + operator;
}

MultiplicativeExpression = base:BaseExpression _ multi:_MultiplicativeExpression { return base + ' ' + multi; }
                         / UnaryExpression

_MultiplicativeExpression = operator:('*' / '/' / '%') _ unary:UnaryExpression _ multi:(_MultiplicativeExpression ?) {
  if(multi !== null){
      return multi;
    }
    return unary + ' ' + operator;
}

UnaryExpression = operator:('+' / '-')  _ base:BaseExpression { return base + ' 0 ' + operator; }
                / BaseExpression

BaseExpression = IntegerLiteral / LowerCaseIdentifier / UpperCaseIdentifier

/**
 * COMMENTS
 */

/* Parses and returns a comment */
Comment = _ comment:(Single_Lined_Comment / Multi_Lined_Comment) _ {
    return new Node.CommentNode(comment);
}

/* Helper function for 'Comment' which parses and returns a single lined comment. */
Single_Lined_Comment = '//' (!LineTerminator SourceCharacter)* {
    return text();
}

/* Helper function for 'Comment' which parses and returns a multi lined comment. */
Multi_Lined_Comment = '/*' (!'*/' SourceCharacter)* '*/' {
    return text();
}

/* Parses the termination of a line. */
LineTerminator 'line terminator' = [\n\r\u2028\u2029]

/* Parses a source character for a comment. */
SourceCharacter 'source character' = .

/* Parses whitespace */
_ 'whitespace' = [ \t\n\r]*