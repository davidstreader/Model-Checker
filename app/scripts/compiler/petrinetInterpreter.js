'use strict';

function interpretPetriNet(process, processesMap, variableMap, processId){
	var root = constructPetriNet(processId, process.ident);
	var localProcessesMap = setupLocalProcesses(process.ident, process.local);
	console.log(localProcessesMap);
	// interpret the main process
	interpretNode(process.process, root, process.ident);

	// process locally defined processes
	var local = process.local;
	for(var i = 0; i < local.length; i++){
		var localProcess = local[i];
		interpretNode(localProcess.process, localProcessesMap[localProcess.ident], process.ident);
	}

	function constructPetriNet(id, ident){
		var net = new PetriNet(id);
		var root = net.addPlace();
		root.addMetaData('startPlace', true);
		net.addRoot(root.id);
		processesMap[ident] = net;
		return root;	
	}

	function setupLocalProcesses(ident, localProcesses){
		var processes = {};
		for(var i = 0; i < localProcesses.length; i++){
			var place = processesMap[ident].addPlace();
			processes[localProcesses[i].ident] = place;
		}

		return processes;
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
		else if(type === 'composite'){
			interpretComposite(astNode, currentNode, ident);
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

	function interpretComposite(astNode, currentPlace, ident){
		// interpret the two processes to be composed together
		var process1 = ident + '.process1';
		var root1 = constructPetriNet(processesMap[ident].id + 'a', process1);
		interpretNode(astNode.process1, root1, process1);

		var process2 = ident + '.process2';
		var root2 = constructPetriNet(processesMap[ident].id + 'b', process2);
		interpretNode(astNode.process2, root2, process2);
		
		// compose processes together
		processesMap[ident] = parallelComposition(processesMap[ident].id, processesMap[process1], processesMap[process2]);

		// check if a labelling has been defined
		if(astNode.label !== undefined){
			// label is an action label node
			processLabelling(processesMap[ident], astNode.label.action);
		}

		// check if a relabelling has been defined
		if(astNode.relabel !== undefined){
			processRelabelling(processesMap[ident], astNode.relabel.set);
		}

		// delete unneeded processes
		delete processesMap[process1];
		delete processesMap[process2];
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
		// check if the process is referencing itself
		if(astNode.ident === ident){
			processesMap[ident].mergePlaces(processesMap[ident].roots, [currentPlace]);
		}
		// check if the process is referencing a locally defined process
		else if(localProcessesMap[astNode.ident] !== undefined){
			processesMap[ident].mergePlaces([localProcessesMap[astNode.ident]], [currentPlace]);
		}
		// check if the process is referencing a globally defined process
		else if(processesMap[astNode.ident] !== undefined){
			// check that referenced process is of the same type
			if(processesMap[ident].type === processesMap[astNode.ident].type){
				processesMap[ident].addPetriNet(processesMap[astNode.ident].clone, [currentPlace]);
			}
			else{
				throw new InterpreterException('Cannot reference type \'' + processesMap[astNode.ident].type + '\' from type \'petrinet\'');
			}
		}
		else{
			throw new InterpreterException('The identifier \'' + astNode.ident + '\' has not been defined');
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
	 * Labels each of the transitions in the specified petri net with
	 * the specified label.
	 *
	 * @param {petrinet} net - the petri net to label
	 * @param {string} label - the new label;
	 */
	function processLabelling(net, label){
		var labelSets = net.labelSets;
		// give every transition in the petri net the new label
		for(var i = 0; i < labelSets.length; i++){
			var oldLabel = labelSets[i].label;
			net.relabelTransition(oldLabel, label + '.' + oldLabel);
		}
	}

	/** 
	 * Relabels transtions in the specified petri net base on the contents of
	 * the specified relabel set. The relabel set is made up of objects containing
	 * the old transition label and the new transition label.
	 *
	 * @param {petrinet} net - the petrinet to relabel
	 * @param {object[]} relabelSet - an array of objects { oldLabel, newLabel }
	 */
	function processRelabelling(net, relabelSet){
		for(var i = 0; i < relabelSet.length; i++){
			// labels are defined as action label nodes
			net.relabelTransition(relabelSet[i].oldLabel.action, relabelSet[i].newLabel.action);
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
			return 'InterpreterException: ' + message;
		};	
	}
}