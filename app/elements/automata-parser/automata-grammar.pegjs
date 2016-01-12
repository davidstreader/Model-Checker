{
  var Node = {
      ModelNode: function(definition){
        this.type = 'model';
        this.definitions = processLocalDefinitions(definition);
        //this.position = location();
      },
      ConstantNode: function(name, expression){
        this.type = 'constant';
        this.name = name;
        this.expression = expression;
        //this.position = location();
      },
      RangeNode: function(name, start, end){
        this.type = 'range';
        this.name = name;
        this.start = start;
        this.end = end;
        //this.position = location();
      },
      SetNode: function(name, set){
        this.type = 'set';
        this.name = name;
        this.set = set;
        //this.position = location();
      },
      DefinitionNode: function(name, process, relabel, hidden, isVisible){
        this.type = 'definition'; // needs to be changed later on
        this.name = name;
        this.process = process;
        this.relabel = (relabel === null) ? undefined : relabel;
        this.hidden = (hidden === null) ? undefined : hidden;
        this.isVisible = (isVisible === null) ? true : false;
        //this.position = location();
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
        //this.position = location();
      },
      NameNode: function(name){
        this.type = 'name';
        this.name = name;
        //this.position = location();
      },
      LabelNode: function(name, label){
        this.type = 'label';
        this.name = name;
        this.label = (label === null) ? undefined : label;
        //this.position = location();
      },
      VariableNode: function(name){
        this.type = 'variable'
        this.name = name;
        //this.position = location();
      },
      ActionNode: function(action){
        this.type = 'action';
        this.action = action;
        //this.position = location();
      },
      SequenceNode: function(from, to){
        this.type = 'sequence';
        //TODO: this.guard = guard;
        this.from = from;
        this.to = to;
        //this.position = location();
      },
      ChoiceNode: function(option1, option2){
        this.type = 'choice';
        this.option1 = option1;
        this.option2 = option2;
        //this.position = location();
      },
      ParallelNode: function(definition1, definition2){
        this.type = 'parallel';
        this.definition1 = definition1;
        this.definition2 = definition2;
        //this.position = location();
      },
      FunctionNode: function(type, process){
        this.type = type;
        this.process = process;
        //this.position = location();
      },
      CompositeNode: function(label, composite, relabel){
        this.type = 'composite';
        this.label = (label === null) ? undefined : label;
        this.composite = composite;
        this.relabel = (relabel === null) ? undefined : relabel;
        //this.position = location();
      },
      StopNode: function(){
        this.type = 'stop';
        //this.position = location();
      },
      ErrorNode: function(){
        this.type = 'error';
        //this.position = location();
      },
      CommentNode: function(comment){
        this.type = 'comment';
        this.comment = comment;
        //this.position = location();
      },
      ExpressionNode: function(operator, operand1, operand2){
        this.type = 'expression';
        this.operator = operator;
        this.operand1 = operand1;
        this.operand2 = operand2;
        //this.position = location();
      },
      SimpleExpressionNode: function(expression){
        this.type = 'simple-expression';
        this.expression = expression;
        //this.position = location();
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
    
    function constructJoinedActionNode(action1, action2){
        var node = new Node.ActionNode();
        delete node.action;
        node.subtype = 'joined';
        node.actions = [action1, action2];
        return node;
    };
    
    function constructExpressionActionNode(action, expressions){
      var node = new Node.ActionNode();
        delete node.action;
        node.subtype = 'expression';
        node.action = action;
        node.expressions = expressions;
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

ParseTree = (FSP / ConstantDefinition / RangeDefinition / SetDefinition / OperationDefinition / Comment)*

FSP = _ definition:(ProcessDefinition / ReferenceDefinition / FunctionDefinition / CompositeDefinition) _ {
  return new Node.ModelNode(definition);
}

/**
 * IDENTIFIERS
 */

Name = name:UpperCaseIdentifier { return new Node.NameNode(name); }
Identifier = name:UpperCaseIdentifier { return new Node.NameNode(name); }

Variable = variable:LowerCaseIdentifier { return new Node.VariableNode(variable); }

UpperCaseIdentifier = $([A-Z][A-Za-z0-9_]*) { return text(); }
LowerCaseIdentifier = $([a-z][A-Za-z0-9_]*) { return text(); }
IntegerLiteral = [-]?[0-9]+ { return parseInt(text(), 10); }
Terminal = 'STOP' / '(' _ 'STOP' _ ')' / 'ERROR' / '(' _ 'ERROR' _ ')'

/**
 * ACTION LABELS
 */

ActionLabel = a:LowerCaseIdentifier _ b:JoinedAction { return constructJoinedActionNode(new Node.ActionNode(a), b); }
            / a:LowerCaseIdentifier _ b:ExpressionAction { return constructExpressionActionNode(a, b); }
            / action:LowerCaseIdentifier { return new Node.ActionNode(action); }
            / '[' _ exp:Expression _ ']' { return constructExpressionActionNode('', [exp]); }

_ActionLabel = JoinedAction 
             / ExpressionAction

JoinedAction = '.' _ action:ActionLabel { return action; }

ExpressionAction = '[' _ exp:Expression _ ']' _ action:(_ActionLabel ?) {
  if(action === null){
        var result = (exp.length !== undefined) ? exp : [exp];
        return result;
    }
  
    return [exp].concat(action);
}

ActionLabels = a:ActionLabel _ b:_ActionLabels {
          a.subtype = 'index';
                    a.variable = b.variable; 
                    a.index = b.index;
                    return a;
             }
             / ActionLabel
             / Set
             / '[' _ range:ActionRange _ ']' { return range; }

_ActionLabels = '.' _ action:ActionLabel { return action; }
              / '.' _ set:Set { return set; }
              / '[' _ range:ActionRange _ ']' { return range; }
              / '[' _ exp:Expression _ ']' { return exp; }

DottedAction = a:LowerCaseIdentifier '.' b:LowerCaseIdentifier { return a + '.' + b; }

ActionRange = Range
            / Set
            / variable:Variable _ ':' _ range:Range { return {variable:variable, index: range}; }
            / variable:Variable _ ':' _ set:Set { return {variable:variable, index: set}; }

Range = start:SimpleExpression _ '..' _ end:SimpleExpression { return new Node.RangeNode(undefined, start, end); }
      / Terminal _ Identifier
      
Set = '{' _ a:SetElements _ '}' { return new Node.SetNode(undefined, a); }
    / Terminal _ Identifier

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

SetDefinition = 'set' _ name:Name _ '=' _ '{' _ set:SetElements _ '}' { return new Node.SetNode(name, set); }

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

BaseLocalProcess = ('STOP' / '(' _ 'STOP' _ ')') { return new Node.StopNode(); }
                 / ('ERROR' / '(' _ 'ERROR' _ ')') { return new Node.ErrorNode(); }
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

Expression = OrExpression

OrExpression = base:BaseExpression _ or:(_OrExpression) { return new Node.ExpressionNode('||', base, or); }
             / AndExpression

_OrExpression = '||' _ and:AndExpression _ or:(_OrExpression ?) {
  if(or === null){
    return and;
  }

  return new Node.ExpressionNode('||', and, or);
}

AndExpression = base:BaseExpression _ and:(_AndExpression) { return new Node.ExpressionNode('&&', base, and); }
              / BitOrExpression

_AndExpression = '&&' _ or:BitOrExpression _ and:(_AndExpression ?) {
  if(and === null){
      return or;
    }
    
    return new Node.ExpressionNode('&&', or, and);
}

BitOrExpression = base:BaseExpression _ or:(_BitOrExpression) {return new Node.ExpressionNode('|', base, or); }
                / BitExclOrExpression

_BitOrExpression = '|' _ excl:BitExclOrExpression _ or:(_BitOrExpression ?) {
  if(or === null){
      return excl;
    }
    
    return new Node.ExpressionNode('|', excl, or);
} 

BitExclOrExpression = base:BaseExpression _ excl:(_BitExclOrExpression) { return new Node.ExpressionNode('^', base, excl); }
                    / BitAndExpression
                    
_BitExclOrExpression = '^' _ and:BitAndExpression _ excl:(_BitExclOrExpression ?) {
  if(excl === null){
      return and;
    }
    
    return new Node.ExpressionNode('^', and, excl);
}

BitAndExpression = base:BaseExpression _ and:(_BitAndExpression) { return new Node.ExpressionNode('&', base, and); }
                 / EqualityExpression

_BitAndExpression = '&' _ equal:EqualityExpression _ and:(_BitAndExpression ?) {
  if(and === null){
      return equal;
    }
    
    return new Node.ExpressionNode('&', equal, and);
}

EqualityExpression = EqualExpression / NotEqualExpression
                   / RelationalExpression

EqualExpression = base:BaseExpression  _ equal:_EqualExpression { return new Node.ExpressionNode('==', base, equal); }
                   
_EqualExpression = op:'==' _ rel:RelationalExpression _ equal:(_EqualExpression ?) {
  if(equal === null){
      return rel;
    }
    
    return new Node.ExpressionNode('==', rel, equal);
}

NotEqualExpression = base:BaseExpression  _ not:_NotEqualExpression { return new Node.ExpressionNode('!=', base, not); }
                   
_NotEqualExpression = op:'!=' _ rel:RelationalExpression _ not:(_NotEqualExpression ?) {
  if(not === null){
      return rel;
    }
    
    return new Node.ExpressionNode('!=', rel, not);
}

RelationalExpression = LessThanExpression / LessThanEqualExpression / GreatThanExpression / GreatThanEqualExpression
                     / ShiftExpression

LessThanExpression = base:BaseExpression _ less:(_LessThanExpression) { return new Node.ExpressionNode('<', base, less); }

_LessThanExpression = '<' _ shift:ShiftExpression _ less:(_LessThanExpression ?) {
  if(less === null){
      return shift;
    }
    
    return new Node.ExpressionNode('<', shift, less);
}

LessThanEqualExpression = base:BaseExpression _ less:(_LessThanEqualExpression) { return new Node.ExpressionNode('<=', base, less); }

_LessThanEqualExpression = '<=' _ shift:ShiftExpression _ less:(_LessThanEqualExpression ?) {
  if(less === null){
      return shift
    }
    
    return new Node.ExpressionNode('<=', shift, less);
}

GreatThanExpression = base:BaseExpression _ great:(_GreatThanExpression) { return new Node.ExpressionNode('>', base, great); }

_GreatThanExpression = '>' _ shift:ShiftExpression _ great:(_GreatThanExpression ?) {
  if(great === null){
      return shift;
    }
    
    return new Node.ExpressionNode('>', shift, great);
}

GreatThanEqualExpression = base:BaseExpression _ great:(_GreatThanEqualExpression) { return new Node.ExpressionNode('>=', base, great); }

_GreatThanEqualExpression = '>=' _ shift:ShiftExpression _ great:(_GreatThanEqualExpression ?) {
  if(great === null){
      return shift;
    }
    
    return new Node.ExpressionNode('>=', shift, great);
}

ShiftExpression = RightShiftExpression / LeftShiftExpression
                / AdditiveExpression

RightShiftExpression = base:BaseExpression _ shift:_RightShiftExpression { return new Node.ExpressionNode('>>', base, shift); }

_RightShiftExpression = '>>' _ add:AdditiveExpression _ shift:(_RightShiftExpression ?) {
  if(shift === null){
      return add;
    }
    
    return new Node.ExpressionNode('>>', add, shift);
}

LeftShiftExpression = base:BaseExpression _ shift:_LeftShiftExpression { return new Node.ExpressionNode('<<', base, shift); }

_LeftShiftExpression = '<<' _ add:AdditiveExpression _ shift:(_LeftShiftExpression ?) {
  if(shift === null){
      return add;
    }
    
    return new Node.ExpressionNode('<<', add, shift);
}

AdditiveExpression = base:BaseExpression _ add:(_AdditiveExpression) { return new Node.SimpleExpressionNode(base + ' ' + add); }
                   / exp:MultiplicativeExpression { return new Node.SimpleExpressionNode(exp); }

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
      return multi + ' ' + unary + ' ' + operator;
    }
    return unary + ' ' + operator;
}

UnaryExpression = operator:('+' / '-')  _ base:BaseExpression { return base + ' 0 ' + operator; }
                / BaseExpression

BaseExpression = IntegerLiteral / LowerCaseIdentifier / UpperCaseIdentifier / '(' _ exp:Expression _ ')' { return exp; }

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