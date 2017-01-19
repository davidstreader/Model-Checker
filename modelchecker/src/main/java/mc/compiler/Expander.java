package mc.compiler;

import mc.compiler.ast.*;
import mc.compiler.iterator.IndexIterator;
import mc.util.expr.Expression;
import mc.util.expr.ExpressionEvaluator;

import java.util.*;

public class Expander {
	
	public AbstractSyntaxTree expand(AbstractSyntaxTree ast){

		List<ProcessNode> processes = ast.getProcesses();
		for(int i = 0; i < processes.size(); i++){
			ProcessNode process = processes.get(i);
		}

		return ast;
	}
	
	private ASTNode expand(ASTNode astNode, Map<String, Object> variableMap){
		if(astNode instanceof ActionLabelNode){
			astNode = expand((ActionLabelNode)astNode, variableMap);
		}
		else if(astNode instanceof IndexNode){
			astNode = expand((IndexNode)astNode, variableMap);
		}
		else if(astNode instanceof SequenceNode){
			astNode = expand((SequenceNode)astNode, variableMap);
		}
		else if(astNode instanceof ChoiceNode){
			astNode = expand((ChoiceNode)astNode, variableMap);
		}
		else if(astNode instanceof CompositeNode){
			astNode = expand((CompositeNode)astNode, variableMap);
		}
		else if(astNode instanceof IfStatementNode){
			astNode = expand((IfStatementNode)astNode, variableMap);
		}
		else if(astNode instanceof FunctionNode){
			astNode = expand((FunctionNode)astNode, variableMap);
		}
        else if(astNode instanceof IdentifierNode){
            astNode = expand((IdentifierNode)astNode, variableMap);
        }
		else if(astNode instanceof ForAllStatementNode){
			astNode = expand((ForAllStatementNode)astNode, variableMap);
		}
		
		return astNode;
	}
	
	private ActionLabelNode expand(ActionLabelNode astNode, Map<String, Object> variableMap){
		return null;
	}
	
	private ASTNode expand(IndexNode astNode, Map<String, Object> variableMap){
		IndexIterator iterator = IndexIterator.construct(astNode.getRange());
		Stack<ASTNode> iterations = new Stack<ASTNode>();
        while(iterator.hasNext()){
			Object element = iterator.next();
            variableMap.put(astNode.getVariable(), element);
            iterations.push(expand(astNode.getProcess(), variableMap));
		}

        ASTNode node = iterations.pop();
        while(!iterations.isEmpty()){
            ASTNode nextNode = iterations.pop();
            node = new ChoiceNode(nextNode, node, astNode.getLocation());
        }

        return node;
	}
	
	private SequenceNode expand(SequenceNode astNode, Map<String, Object> variableMap){
		ActionLabelNode from = expand(astNode.getFrom(), variableMap);
		ASTNode to = expand(astNode.getTo(), variableMap);
		astNode.setFrom(from);
		astNode.setTo(to);
		return astNode;
	}
	
	private ASTNode expand(ChoiceNode astNode, Map<String, Object> variableMap){
		ASTNode process1 = expand(astNode.getFirstProcess(), variableMap);
		ASTNode process2 = expand(astNode.getSecondProcess(), variableMap);

		// check if either one of the branches is empty
		if(process1 instanceof EmptyNode || process1 instanceof TerminalNode){
			return process2;
		}
		else if(process2 instanceof EmptyNode || process2 instanceof TerminalNode){
			return process1;
		}

		astNode.setFirstProcess(process1);
		astNode.setSecondProcess(process2);
		return astNode;
	}
	
	private ASTNode expand(CompositeNode astNode, Map<String, Object> variableMap){
		ASTNode process1 = expand(astNode.getFirstProcess(), variableMap);
		ASTNode process2 = expand(astNode.getSecondProcess(), variableMap);

		// check if either one of the branches is empty
		if(process1 instanceof EmptyNode || process1 instanceof TerminalNode){
			return process2;
		}
		else if(process2 instanceof EmptyNode || process2 instanceof TerminalNode){
			return process1;
		}

		astNode.setFirstProcess(process1);
		astNode.setSecondProcess(process2);
		return astNode;
	}
	
	private ASTNode expand(IfStatementNode astNode, Map<String, Object> variableMap){
		boolean condition = evaluateExpression(astNode.getCondition(), variableMap);
		if(condition){
			return expand(astNode.getTrueBranch(), variableMap);
		}
		else if(astNode.hasFalseBranch()){
			return expand(astNode.getFalseBranch(), variableMap);
		}

		return new EmptyNode();
	}
	
	private FunctionNode expand(FunctionNode astNode, Map<String, Object> variableMap){
		ASTNode process = expand(astNode.getProcess(), variableMap);
		astNode.setProcess(process);
		return astNode;
	}

    private IdentifierNode expand(IdentifierNode astNode, Map<String, Object> variableMap){
        return null;
    }

	private ForAllStatementNode expand(ForAllStatementNode astNode, Map<String, Object> variableMap){
		return null;
	}

    private CompositeNode expand(ASTNode process, Map<String, Object> variableMap, List<ASTNode> ranges){
        return null;
    }

	private boolean evaluateExpression(Expression condition, Map<String, Object> variableMap){
		// remove all strings from the variableMap
		Map<String, Integer> variables = new HashMap<String, Integer>();
		for(String key : variableMap.keySet()){
			Object value = variableMap.get(key);
			if(value instanceof Integer){
				variables.put(key, (Integer)value);
			}
		}

		int result = new ExpressionEvaluator().evaluateExpression(condition, variables);
		return result != 0;
	}
}
