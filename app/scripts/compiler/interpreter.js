'use strict';

var processesMap;

function interpret(processes){

	while(processes.length !== 0){
		reset();
		var process = processes.pop();
		if(process.type === 'process'){
			interpretProcess(process);
		}
		else if(proces.type === 'operation'){

		}
		else{
			// throw error
		}
	}

	function interpretProcess(process){
		var graph = new Graph();
		processesMap[process.ident] = graph;
		interpretNode(process.process, graph.root, process.ident);
	}

	function interpretNode(astNode, currentNode, ident){
		var type = astNode.type;
		// determine the type of node to process
		if(type === 'process'){
			interpretLocalProcess(astNode, currentNode, ident);
		}
		else if(type === 'sequence'){
			interpretSequence(astNode, currentNode, ident);
		}
		else if(type === 'choice'){
			interpretChoice(astNode, currentNode, ident);
		}
		else if(type === 'function'){
			interpretFunction(astNode, currentNode, ident);
		}
		else if(type === 'identifier'){
			interpretIdentifier(astNode, currentNode, ident);
		}
		else if(type === 'terminal'){
			interpretTerminal(astNode, currentNode, ident);
		}
		else{
			// throw error
		}
	}

	function interpretLocalProcess(astNode, currentNode, ident){

	}

	function interpretRange(astNode, currentNode, ident){

	}

	function interpretSequence(astNode, currentNode, ident){
		// check that the first or second part of the sequence is defined
		if(astNode.from === undefined){
			// throw error
		}

		if(astNode.to === undefined){
			// throw error
		}

		// check that from is an action label
		if(astNode.from.type !== 'action-label'){
			// throw error
		}

		var next = processesMap[ident].addNode();
		processesMap[ident].addEdge(currentNode, next, astNode.from.action);
		interpretNode(astNode.to, next, ident);
	}

	function interpretChoice(astNode, currentNode, ident){
		interpretProcess(astNode.option1, currentNode, ident);
		interpretProcess(astNode.option2, currentNode, ident);
	}

	function interpretFunction(astNode, currentNode, ident){

	}

	function interpretIdentifier(astNode, currentNode, ident){

	}

	function interpretTerminal(astNode, currentNode, ident){
		if(astNode.terminal === 'STOP'){
			currentNode.metaData['isTerminal'] = 'stop';
		}
		else if(astNode.terminal === 'ERROR'){

		}
		else{
			// throw error
		}
	}

	function reset(){
		processesMap = {};
	}
}