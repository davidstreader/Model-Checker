'use strict';

var nextPetriNetId;

function interpretPetriNet(process, processesMap, variableMap){
	reset();
	var net = new PetriNet(nextPetriNetId++);
	var root = net.addPlace();
	root.addMetaData('startPlace', true);
	net.addRoot(root);
	processesMap[process.ident] = net;
	interpretNode(process.process, net.root, process.ident);

	function interpretNode(astNode, currentNode, ident){
		var type = astNode.type;
		// determine the type of node to process
		if(type === 'process'){
			interpretLocalProcess(astNode, currentNode, ident);
		}
		else if(type === 'index'){
			interpretIndex(astNode, currentNode, ident);
		}
		else if(type === 'sequence'){
			interpretSequence(astNode, currentNode, ident);
		}
		else if(type === 'choice'){
			interpretChoice(astNode, currentNode, ident);
		}
		else if(type === 'if-statement'){
			interpretIfStatement(astNode, currentNode, ident);
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

	function interpretIndex(astNode, currentNode, ident){
		var iterator = new IndexIterator(astNode.range);
		while(iterator.hasNext){
			var element = iterator.next;
			variableMap[astNode.variable] = element;
			interpretNode(astNode.process, currentNode, ident);
		}

		//throw new InterpreterException('Functionality for interpreting a range is currently not implemented');
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

		var action = processActionLabel(astNode.from.action);
		var next = processesMap[ident].addTransition(action, currentNode);
		interpretNode(astNode.to, next, ident);
	}

	function interpretChoice(astNode, currentPlace, ident){
		interpretNode(astNode.process1, currentPlace, ident);
		interpretNode(astNode.process2, currentPlace, ident);
	}

	function interpretIfStatement(astNode, currentPlace, ident){
		var guard = processGuardExpression(astNode.guard);
		if(guard){
			interpretNode(astNode.trueBranch, currentPlace, ident);
		}
		else if(astNode.falseBranch !== undefined){
			interpretNode(astNode.falseBranch, currentPlace, ident);
		}
		else{
			currentPlace.addMetaData('isTerminal', 'stop');
		}
	}

	function interpretFunction(astNode, currentPlace, ident){
		throw new InterpreterException('Functionality for interpreting functions is currently not implemented');
	}

	function interpretIdentifier(astNode, currentPlace, ident){
		if(astNode.ident === ident){
			processesMap[ident].mergePlaces([processesMap[ident].root, currentPlace]);
			// check that referenced process is of the same type
			if(processesMap[ident].type === processesMap[astNode.ident].type){
				processesMap[ident].addPetriNet(processesMap[astNode.ident], currentPlace);
			}
			else{
				throw new InterpreterException('Cannot reference type \'' + processesMap[astNode.ident].type + '\' from type \'petrinet\'');
			}
		}
		else if(processesMap[astNode.ident] !== undefined){
			// check that referenced process is of the same type
			if(processesMap[ident].type === processesMap[astNode.ident].type){
				processesMap[ident].addPetriNet(processesMap[astNode.ident], currentPlace);
			}
			else{
				throw new InterpreterException('Cannot reference type \'' + processesMap[astNode.ident].type + '\' from type \'petrinet\'');
			}
		}
		else{
			throw new InterpreterException('The identifier \'' + astNode.ident + '\' has not been defined');
		}
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

	/**
	 * Evaluates and returns the specified expression. Returns the result as a boolean if
	 * specified, otherwise returns the result as a number.
	 *
	 * @param {string} - the expression to evaluate
	 * @return {string} - the processed action label
	 */
	function processActionLabel(action){
		// replace any variables declared in the expression with its value
		var regex = '[\$][<]*[a-zA-Z0-9]*[>]*';
		var match = action.match(regex);
		while(match !== null){
			var expr = evaluate(variableMap[match[0]]);
			action = action.replace(match[0], expr);
			match = action.match(regex);
		}

		return action;
	}

	function processGuardExpression(expr){
		// replace any variables declared in the expression with its value
		var regex = '[\$][<]*[a-zA-Z0-9]*[>]*';
		var match = expr.match(regex);
		while(match !== null){
			expr = expr.replace(match[0], variableMap[match[0]]);
			match = expr.match(regex);
		}

		expr = evaluate(expr);
		return (expr === 0) ? false : true;
	}

	function reset(){
		nextPetriNetId = 0;
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
			return 'InterpreterException: ' + message;
		};	
	}
}