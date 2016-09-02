'use strict';

function reconstruct(astNode){
	var text = '';
	reconstructNode(astNode);
	return text;

	function reconstructNode(astNode){
		var type = astNode.type;
		if(type === 'action-label'){
			reconstructActionLabelNode(astNode);
		}
		else if(type === 'sequence'){
			reconstructSequenceNode(astNode);
		}
		else if(type === 'choice'){
			reconstructChoiceNode(astNode);
		}
		else if(type === 'composite'){
			reconstructCompositeNode(astNode);
		}
		else if(type === 'terminal'){
			reconstructTerminalNode(astNode);
		}
		else if(type === 'if-statement'){
			reconstructIfStatementNode(astNode);
		}
		else if(type === 'function'){
			reconstructFunctionNode(astNode);
		}
		else if(type === 'identifier'){
			reconstructIdentifierNode(astNode);
		}
		else if(type === 'forall'){
			reconstructForallNode(astNode);
		}
	}

	function reconstructActionLabelNode(astNode){
		text += astNode.action;
	}

	function reconstructSequenceNode(astNode){
		reconstructNode(astNode.from);
		text += ' -> ';
		reconstructNode(astNode.to);
	}

	function reconstructChoiceNode(astNode){
		reconstructNode(astNode.process1);
		text += ' | ';
		reconstructNode(astNode.process2);
	}

	function reconstructCompositeNode(astNode){
		if(astNode.label !== undefined){
			text += astNode.label + ':';
		}
		reconstructNode(astNode.process1);
		text += ' || ';
		reconstructNode(astNode.process2);
		if(astNode.relabel !== undefined){
			text += '\\{';
			for(var i = 0; i < astNode.relabel.length; i++){
				text += astNode.relabel[i].newLabel;
				text += '\\';
				text += astNode.relabel[i].oldLabel;
				if(i < astNode.relabel.length - 1){
					text += ', ';
				}
			}
			text += '}';
		}
	}

	function reconstructTerminalNode(astNode){
		text += astNode.terminal;
	}

	function reconstructFunctionNode(astNode){
		text += astNode.func + '(';
		reconstructNode(astNode.process);
		text += ')';
	}

	function reconstructIdentifierNode(astNode){
		text += astNode.ident;
	}
}