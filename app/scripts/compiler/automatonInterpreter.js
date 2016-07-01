'use strict';

function interpretAutomaton(process, processesMap, variableMap, processId){
	var root = constructAutomaton(processId, process.ident.ident);
	var localProcessesMap = constructLocalProcesses(process.ident.ident, process.local);

	// interpret the main process
	interpretNode(process.process, root, process.ident.ident);

	// interpret the locally defined processes
	var local = process.local;
	for(var ident in localProcessesMap){
		var localProcess = localProcessesMap[ident];
		interpretNode(localProcess.process, localProcess.node, process.ident.ident);
	}

	function constructAutomaton(id, ident){
		var graph = new Graph(id);
		graph.root = graph.addNode();
		graph.root.addMetaData('startNode', true);
		processesMap[ident] = graph;
		return graph.root;
	}

	function constructLocalProcesses(ident, localProcesses){
		var processes = {};
		for(var i = 0; i < localProcesses.length; i++){
			var astNode = localProcesses[i].ident;
			// check if process has indices defined
			if(astNode.ranges !== undefined){
				constructIndexedLocalProcess(astNode.ident, ident, astNode.ranges.ranges, localProcesses[i].process);
			}
			else{
				processes[astNode.ident] = { node:processesMap[ident].addNode(), process:localProcesses[i].process };
			}
		}

		return processes;

		function constructIndexedLocalProcess(ident, globalIdent, ranges, process){
			if(ranges.length !== 0){
				var iterator = new IndexIterator(ranges[0]);
				while(iterator.hasNext){
					var element = iterator.next;
					var newIdent = ident + '[' + element + ']';
					ranges = (ranges.length > 1) ? ranges.slice(1) : [];
					constructIndexedLocalProcess(newIdent, globalIdent, ranges.slice(1), process);
				}
			}
			else{
				processes[ident] = { node:processesMap[globalIdent].addNode(), process:process };
			}
		}
	}

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

		// check if a labelling has been defined
		if(astNode.label !== undefined){
			// label is an action label node
			processLabelling(processesMap[ident], astNode.label.action);
		}

		// check if a relabelling has been defined
		if(astNode.relabel !== undefined){
			processRelabelling(processesMap[ident], astNode.relabel.set);
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
		var next = graph.addNode();
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
		var current = astNode.ident;
		current = processActionLabel(current);
		// check if this process is referencing itself
		if(current === ident){
			var root = processesMap[ident].root;
			processesMap[ident].mergeNodes([root, currentNode]);
		}
		// check if the process is referencing a locally defined process
		else if(localProcessesMap[current] !== undefined){
			processesMap[ident].mergeNodes([localProcessesMap[current].node, currentNode]);
		}
		// check if the process is referencing a globally defined process
		else if(processesMap[current] !== undefined){
			// check that referenced process is of the same type
			if(processesMap[ident].type === processesMap[current].type){
				processesMap[ident].addGraph(processesMap[current].clone, currentNode);
			}
			else{
				throw new InterpreterException('Cannot reference type \'' + processesMap[current].type + '\' from type \'automata\'');
			}
		}
		else{
			throw new InterpreterException('The identifier \'' + current + '\' has not been defined');
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
	 * Labels each of the edges in the specified graph with the specified label.
	 *
	 * @param {graph} graph - the graph to label
	 * @param {string} label - the new label;
	 */
	function processLabelling(graph, label){
		graph.labelEdges(label);
	}

	/** 
	 * Relabels edges in the specified graph base on the contents of the specified 
	 * relabel set. The relabel set is made up of objects containing the old transition
	 * label and the new transition label.
	 *
	 * @param {graph} graph - the graph to relabel
	 * @param {object[]} relabelSet - an array of objects { oldLabel, newLabel }
	 */
	function processRelabelling(graph, relabelSet){
		for(var i = 0; i < relabelSet.length; i++){
			graph.relabelEdge(relabelSet[i].oldLabel.action, relabelSet[i].newLabel.action);
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