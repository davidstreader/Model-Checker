'use strict';

function interpretAutomaton(process, processesMap, variableMap){
	var graph = new Graph();
	graph.root = graph.addNode(graph.nextNodeId);
	graph.root.addMetaData('startNode', true);
	processesMap[process.ident] = graph;
	interpretNode(process.process, graph.root, process.ident);

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

		var graph = processesMap[ident];
		var next = graph.addNode(graph.nextNodeId);
		var action = processActionLabel(astNode.from.action);
		processesMap[ident].addEdge(graph.nextEdgeId, action, currentNode.id, next.id);
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
		// check whether this is a locally or globally defined reference, or a reference to the current process
		if(astNode.ident === ident){
			var root = processesMap[ident].root;
			processesMap[ident].mergeNodes([root, currentNode]);
		}
		else if(processesMap[astNode.ident] !== undefined){
			// check that referenced process is of the same type
			if(processesMap[ident].type === processesMap[astNode.ident].type){
				processesMap[ident].addGraph(processesMap[astNode.ident].clone, currentNode);
			}
			else{
				throw new InterpreterException('Cannot reference type \'' + processesMap[astNode.ident].type + '\' from type \'automata\'');
			}
		}
		else{
			throw new InterpreterException('The identifier \'' + astNode.ident + '\' has not been defined');
		}
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

	function relabelNodes(graph){
		
	}

	function reset(){
		processesMap = {};
		variableMap = {};
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
			return 'InterpreterException: ' + message;
		};	
	}
}