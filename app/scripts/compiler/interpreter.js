'use strict';

var processesMap;

var nodeCount = 0;
var edgeCount = 0;

function interpret(processes){
	reset();
	while(processes.length !== 0){
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

	return constructAutomataArray();

	function interpretProcess(process){
		var graph = new Graph();
		graph.root = graph.addNode(nodeCount++);
		graph.root.addMetaData('startNode', true);
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
		else if(type === 'label'){
			interpretLabel(astNode, currentNode, ident);
		}
		else if(type === 'terminal'){
			interpretTerminal(astNode, currentNode, ident);
		}
		else{
			throw new InterpreterException('Invalid type \'' + type + '\' received');
		}
	}

	function interpretLocalProcess(astNode, currentNode, ident){
		throw new InterpreterException('Functionality for interpreting a local process is currently not implemented');
	}

	function interpretRange(astNode, currentNode, ident){
		throw new InterpreterException('Functionality for interpreting a range is currently not implemented');
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

		var next = processesMap[ident].addNode(nodeCount++);
		processesMap[ident].addEdge(edgeCount++, currentNode, next, astNode.from.action);
		interpretNode(astNode.to, next, ident);
	}

	function interpretChoice(astNode, currentNode, ident){
		interpretNode(astNode.process1, currentNode, ident);
		interpretNode(astNode.process2, currentNode, ident);
	}

	function interpretFunction(astNode, currentNode, ident){
		throw new InterpreterException('Functionality for interpreting functions is currently not implemented');
	}

	function interpretIdentifier(astNode, currentNode, ident){
		throw new InterpreterException('Functionality for interpreting identifiers is currently not implemented');

		// check whether this is a locally or globally defined reference

	}

	function interpretLabel(astNode, currentNode, ident){
		interpretNode(astNode.process, currentNode, ident);
		
		// add the label associated with this node to each edge in the automaton
		var edges = processesMap[ident].edges;
		for(var i = 0; i < edges.length; i++){
			edges[i].label = astNode.label.action + ':' + edges[i].label;
		}
	}

	function interpretTerminal(astNode, currentNode, ident){
		if(astNode.terminal === 'STOP'){
			currentNode.addMetaData('isTerminal', 'stop');
		}
		else if(astNode.terminal === 'ERROR'){
			throw new InterpreterException('Functionality for interpreting error terminals is currently not implemented');
		}
		else{
			// throw error
		}
	}

	function constructAutomataArray(){
		var automata = [];
		for(var ident in processesMap){
			automata.push(new Automaton(ident, processesMap[ident]));
		}

		return { automata:automata };
	}

	function reset(){
		processesMap = {};
	}

	/**
	 * Constructs and returns a 'ParserException' based off of the
	 * specified message. Also contains the location in the code being parsed
	 * where the error occured.
	 *
	 * @param {string} message - the cause of the exception
	 * @param {object} location - the location where the exception occured
	 */
	function InterpreterException(message, location){
		this.message = message;
		this.location = location;
		this.toString = function(){
			return 'ParserException: ' + message;
		};	
	}
}