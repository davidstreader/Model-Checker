package mc.compiler;

import mc.compiler.ast.ASTNode;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ActionLabelNode;
import mc.compiler.ast.ChoiceNode;
import mc.compiler.ast.CompositeNode;
import mc.compiler.ast.ForAllStatementNode;
import mc.compiler.ast.FunctionNode;
import mc.compiler.ast.IfStatementNode;
import mc.compiler.ast.IndexNode;
import mc.compiler.ast.SequenceNode;

public class Expander {

	public AbstractSyntaxTree expand(AbstractSyntaxTree ast){

		return ast;
	}

	private ASTNode expand(ASTNode astNode){
		if(astNode instanceof ActionLabelNode){
			astNode = expand((ActionLabelNode)astNode);
		}
		else if(astNode instanceof IndexNode){
			astNode = expand((IndexNode)astNode);
		}
		else if(astNode instanceof SequenceNode){
			astNode = expand((SequenceNode)astNode);
		}
		else if(astNode instanceof ChoiceNode){
			astNode = expand((ChoiceNode)astNode);
		}
		else if(astNode instanceof CompositeNode){
			astNode = expand((CompositeNode)astNode);
		}
		else if(astNode instanceof IfStatementNode){
			astNode = expand((IfStatementNode)astNode);
		}
		else if(astNode instanceof FunctionNode){
			astNode = expand((FunctionNode)astNode);
		}
		else if(astNode instanceof ForAllStatementNode){
			astNode = expand((ForAllStatementNode)astNode);
		}

		return astNode;
	}
//
//	private ActionLabelNode expand(ActionLabelNode astNode){
//
//	}
//
//	private IndexNode expand(IndexNode astNode){
//
//	}
//
//	private SequenceNode expand(SequenceNode astNode){
//
//	}
//
//	private ChoiceNode expand(ChoiceNode astNode){
//
//	}
//
//	private CompositeNode expand(CompositeNode astNode){
//
//	}
//
//	private IfStatementNode expand(IfStatementNode astNode){
//
//	}
//
//	private FunctionNode expand(FunctionNode astNode){
//
//	}
//
//	private ForAllStatementNode(ForAllStatementnode astNode){
//
//	}
}
