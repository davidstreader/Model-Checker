'use strict';

var processesMap;

function interpretPetriNet(processes){
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
	
	return constructPetriNetsArray();

	function interpretProcess(process){
		var net = new PetriNet();
		net.root = net.addPlace();
		net.root.addMetaData('startPlace', true);
		processesMap[process.ident] = net;
		interpretNode(process.process, net.root, process.ident);
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

		var next = processesMap[ident].addTransition(astNode.from.action, currentNode);
		interpretNode(astNode.to, next, ident);
	}

	function interpretChoice(astNode, currentPlace, ident){
		interpretNode(astNode.process1, currentPlace, ident);
		interpretNode(astNode.process2, currentPlace, ident);
	}

	function interpretFunction(astNode, currentPlace, ident){
		throw new InterpreterException('Functionality for interpreting functions is currently not implemented');
	}

	function interpretIdentifier(astNode, currentPlace, ident){
		throw new InterpreterException('Functionality for interpreting identifiers is currently not implemented');
	}

	function interpretLabel(astNode, currentPlace, ident){
		interpretNode(astNode.process, currentPlace, ident);
		
		// add the label associated with this ast node to each transition in the petri net
		var transitions = processesMap[ident].transitions;
		for(var i = 0; i < transitions.length; i++){
			transitions[i].label = astNode.label.action + ':' + transitions[i].label;
		}
	}

	function interpretTerminal(astNode, currentPlace, ident){
		if(astNode.terminal === 'STOP'){
			currentPlace.addMetaData('isTerminal', 'stop');
		}
		else if(astNode.terminal === 'ERROR'){
			throw new InterpreterException('Functionality for interpreting error terminals is currently not implemented');
		}
		else{
			// throw error
		}
	}

	function constructPetriNetsArray(){
		var nets = [];
		for(var ident in processesMap){
			nets.push(new Net(ident, processesMap[ident]));
		}

		return { automata:nets };
	}

	function reset(){
		processesMap = {};
	}

	/**
	 * Constructs and returns an 'InterpreterException' based off of the
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