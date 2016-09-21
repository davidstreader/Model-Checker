'use strict';

function interpretPetriNet(process, processesMap, variableMap, processId, isFairAbstraction){
	var identifier = process.ident.ident;
	var root = new Net(processId, identifier);
	var localProcessesMap = new LocalProcessesMap(identifier, process.local);

	// interpret the main process
	interpretNode(process.process, undefined, root, identifier);

	// interpret locally defined processes
	for(var ident in localProcessesMap){
		var localProcess = localProcessesMap[ident];
		interpretNode(localProcess.process, undefined, localProcess.place, identifier);
	}

	// interpret hiding set if one was defined
	if(process.hiding !== undefined){
		processHiding(processesMap[identifier], process.hiding);
	}

	function Net(id, ident){
		var net = new PetriNet(id);
		var root = net.addPlace();
		net.addRoot(root.id);
		processesMap[ident] = net;
		return root;
	}

	function processTemporaryPetriNet(id, ident, astNode){
		var root = new Net(id, ident);
		interpretNode(astNode, undefined, root, ident);
	}

	function LocalProcessesMap(ident, localProcesses){
		var processes = {};
		for(var i = 0; i < localProcesses.length; i++){
			var astNode = localProcesses[i].ident;
			processes[astNode.ident] = {
				place: processesMap[ident].addPlace(),
				process: localProcesses[i].process
			}
		}

		return processes;
	}

	function interpretNode(astNode, currentTransition, root, ident){
		var type = astNode.type;
		if(type === 'sequence'){
			interpretSequence(astNode, currentTransition, root, ident);
		}
		else if(type === 'choice'){
			interpretChoice(astNode, currentTransition, root, ident);
		}
		else if(type === 'composite'){
			interpretComposite(astNode, currentTransition, root, ident);
		}
		else if(type === 'function'){
			interpretFunction(astNode, currentTransition, root, ident);
		}
		else if(type === 'identifier'){
			interpretIdentifier(astNode, currentTransition, root, ident);
		}
		else if(type === 'terminal'){
			interpretTerminal(astNode, currentTransition, root, ident);
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

	function interpretSequence(astNode, currentTransition, root, ident){
		var net = processesMap[ident];
		var place = root;
		// check if the current transition has been defined
		if(currentTransition !== undefined){
			var place = net.addPlace();
			constructConnection(currentTransition, [place], ident);
		}

		// construct the new transition
		var id = net.nextTransitionId;
		var transition = net.addTransition(id, astNode.from.action, [place]);

		interpretNode(astNode.to, transition, root, ident);
	}

	function interpretChoice(astNode, currentTransition, root, ident){
		var net = processesMap[ident];
		// process both branches of the choice
		var process1 = ident + '.process1';
		processTemporaryPetriNet(net.id + '.a', process1, astNode.process1);
		var process2 = ident + '.process2';
		processTemporaryPetriNet(net.id + '.b', process2, astNode.process2);

		// add branches to the main petri net
		net.addPetriNet(processesMap[process1]);
		net.addPetriNet(processesMap[process2]);

		// get the roots for the two choice branches
		var roots1 = processesMap[process1].roots.map(p => net.getPlace(p.id));
		var roots2 = processesMap[process2].roots.map(p => net.getPlace(p.id));

		// create a cross product of the roots from both branches
		var crossProducts = [];
		for(var i = 0; i < roots1.length; i++){
			for(var j = 0; j < roots2.length; j++){
				crossProducts.push(net.combinePlaces(roots1[i], roots2[j]));
			}
			net.removePlace(roots1[i].id);
		}

		for(var i = 0; i < roots2.length; i++){
			net.removePlace(roots2[i].id);
		}

		// check if the current transition has been defined
		if(currentTransition !== undefined){
			// connection current transition to the cross product places
			constructConnection(currentTransition, crossProducts, ident);
		}
		else{
			// remove the root and make the cross product places roots
			net.removePlace(root.id);
			for(var i = 0; i < crossProducts.length; i++){
				crossProducts[i].addMetaData('startPlace', true);
				net.addRoot(crossProducts[i].id);
			}
		}

		// delete temporary petri nets
		delete processesMap[process1];
		delete processesMap[process2];
	}

	function interpretComposite(astNode, currentTransition, root, ident){
		var net = processesMap[ident];
		// interpret the two processes to be composed together
		var process1 = ident + '.process1';
		processTemporaryPetriNet(net.id + '.a', process1, astNode.process1);
		var process2 = ident + '.process2';
		processTemporaryPetriNet(net.id + '.b', process2, astNode.process2);

		// compose the processes together
		var composite = parallelComposition(net.id, processesMap[process1], processesMap[process2]);
		net.addPetriNet(composite);

		// check if the current transition has been defined
		if(currentTransition !== undefined){
			// connect current transition to the roots of the composition
			var roots = composite.roots.map(p => net.getPlace(p.id));
			constructConnection(currentTransition, roots, ident);
		}
		else{
			// remove the root
			net.removePlace(root.id);
		}

		// delete temporary petri nets
		delete processesMap[process1];
		delete processesMap[process2];
	}

	function interpretFunction(astNode, currentTransition, root, ident){
		var net = processesMap[ident];
		// process the process to have the function executed on
		var funcIdent = ident + '.f';
		processTemporaryPetriNet(net.id, funcIdent, astNode.process);

		// determine what function to execute
		var type = astNode.func;
		var processedNet;
		if(type === 'abs'){
			processedNet = abstraction(processMap[funcIdent], isFairAbstraction);
		}
		else if(type === 'simp'){
			// error
		}
		else{
			// error
		}

		// check if the current transition has been defined
		if(currentTransition !== undefined){
			// connect current transition to the roots of the processed petri net
			var roots = processedNet.roots.map(p => net.getPlace(p.id));
			constructConnection(currentTransition, roots, ident);
		}
		else{
			// remove the root
			net.removePlace(root.id);
		}

		// delete the temporary petri net
		delete processesMap[funcIdent];
	}

	function interpretIdentifier(astNode, currentTransition, root, ident){
		var current = astNode.ident;
		// check if this process is referencing itself
		if(current === ident){
			if(currentTransition !== undefined){
				constructConnection(currentTransition, [root], ident);
				processesMap[ident].addRoot(root.id);
			}
			else{
				root.addMetaData('isTerminal', 'stop');
			}
		}
		// check if the process is referencing a locally defined process
		else if(localProcessesMap[current] !== undefined){
			if(currentTransition !== undefined){
				constructConnection(currentTransition, [localProcessesMap[current].place], ident);
			}
			else{
				// make the referenced place the new start place and remove the current start place
				processesMap[ident].addRoot(localProcessesMap[current].place.id);
				processesMap[ident].removePlace(root.id);
			}
		}
		// check if the process is referencing a globally defined process
		else if(processesMap[current] !== undefined){
			var referencedNet = processesMap[current].clone;
			// make sure the referenced process is a petri net
			if(processesMap[current].type !== 'petrinet'){
				// throw error
			}

			// add the referneced net to the current one
			processesMap[ident].addPetriNet(referencedNet);
			var roots = referencedNet.roots.map(p => processesMap[ident].getPlace(p.id));

			if(currentTransition !== undefined){	
				constructConnection(currentTransition, roots, ident);
			}
			else{
				// remove the current start place
				processesMap[ident].removePlace(root.id);
				for(var i = 0; i < roots.length; i++){
					processesMap[ident].addRoot(roots[i].id);
				}
			}
		}
		else{
			// identifier has not been defined
		}
	}

	function interpretTerminal(astNode, currentTransition, root, ident){
		var net = processesMap[ident];
		var place = (currentTransition !== undefined) ? net.addPlace() : root;
		if(astNode.terminal === 'STOP'){
			place.addMetaData('isTerminal', 'stop');
		}
		else if(astNode.terminal === 'ERROR'){
			var nextPlace = net.addPlace();
			var deadlock = net.addTransition(net.nextTransitionId, DELTA, [place], [nextPlace]);
			nextPlace.addMetaData('isTerminal', 'error');
		}

		// check if the current transition has been defined
		if(currentTransition !== undefined){
			constructConnection(currentTransition, [place], ident);
		}
		else{
			net.addRoot(place.id);
		}
	}

	function constructConnection(transition, places, ident){
		// connect each place to the transition
		for(var i = 0; i < places.length; i++){
			transition.addOutgoingPlace(places[i]);
			places[i].addIncomingTransition(transition.id);
			processesMap[ident].removeRoot(places[i].id);
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
		for(var oldLabel in labelSets){
			net.relabelTransition(oldLabel, label + '.' + oldLabel);
		}
	}

	/** 
	 * Relabels transtions in the specified Petri net based on the contents of
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
	 * Relabels transitions in the specified Petri net based on the contents of the
	 * specified hiding set. Depending on the type of the hiding set, all the transitions
	 * with labels in the hiding set are marked as hidden or all the transitions with labels
	 * not in the hiding set are marked as hidden.
	 *
	 * @param {petrinet} net - the petri net to process
	 * @param {object} hidingSet - an object containing a hiding type and an array of actions
	 */
	function processHiding(net, hidingSet){
		var labelSets = net.labelSets;
		var set = hidingSet.set;
		if(hidingSet.type === 'includes'){
			processInclusionHiding(labelSets, set);
		}
		else if(hidingSet.type === 'excludes'){
			processExclusionHiding(labelSets, set);
		}

		/**
		 * Sets all the transitions with labels in the specified set to be hidden.
		 */
		function processInclusionHiding(labelSets, set){
			for(var label in labelSets){
				for(var j = 0; j < set.length; j++){
					// check if the labels match
					if(label === set[j]){
						net.relabelTransition(set[j], TAU);
					}
				}
			}	
		}

		/**
		 * Sets all the transitions with labels not in the specified set to be hidden.
		 */
		function processExclusionHiding(labelSets, set){
			for(var label in labelSets){
				var match = false;
				for(var j = 0; j < set.length; j++){
					// check if the labels match
					if(label === set[j]){
						match = true;
						break;
					}
				}

				// relabel if the current label did not match any labels in the set
				if(!match){
					net.relabelTransition(labelSets[i].label, TAU);
				}
			}	
		}
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