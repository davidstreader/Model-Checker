'use strict';

function interpretPetriNet(process, processesMap, variableMap, processId, isFairAbstraction){
	var identifier = process.ident.ident;
	var globalRoot = new Net(processId, identifier);
	var localProcessesMap = new LocalProcessesMap(identifier, process.local);

	// interpret the main process
	interpretNode(process.process, undefined, globalRoot, identifier);

	// interpret hiding set if one was defined
	if(process.hiding !== undefined){
		processHiding(processesMap[identifier], process.hiding);
	}

	// find the roots for the interpreted petri net
	var key = 'isRoot'
	var roots = processesMap[identifier].places.filter(p => p.getMetaData('isRoot') !== undefined);
	if(roots.length === 0){
		key = 'potentialRoot'
		roots = processesMap[identifier].places.filter(p => p.getMetaData('potentialRoot') !== undefined);
	}
	for(var i = 0; i < roots.length; i++){
		var tokens = roots[i].getMetaData(key);
		roots[i].deleteMetaData(key);
		processesMap[identifier].getPlace(roots[i].id).addMetaData('startPlace', tokens);
		processesMap[identifier].addRoot(roots[i].id);
	}

	processesMap[identifier].trim();

	function Net(id, ident){
		var net = new PetriNet(id);
		var root = net.addPlace();
		root.addMetaData('isRoot', 1);
		processesMap[ident] = net;
		return root;
	}

	function processSubPetriNet(ident, astNode, root){
		var net = processesMap[ident];
		// check if a root has already been defined
		if(root === undefined){
			root = net.addPlace();
			root.addMetaData('potentialRoot', 1);
		}

		var potentialRoots = net.places.filter(p => p.getMetaData('potentialRoot') !== undefined);
		var rootSet = {};
		for(var i = 0; i < potentialRoots.length; i++){
			rootSet[potentialRoots[i].id] = potentialRoots[i];
		}

		// interpret the astNode
		interpretNode(astNode, undefined, root, ident);

		// work out the new potential roots
		var roots = net.places.filter(p => rootSet[p.id] === undefined && p.getMetaData('potentialRoot') !== undefined);

		// return the defined root if there are no new roots found
		if(roots.length === 0){
			return [root];
		}

		return roots;
	}

	function TempNet(id, ident, astNode){
		var root = new Net(id, ident);
		interpretNode(astNode, undefined, root, ident);
		return processesMap[ident];
	}

	function LocalProcessesMap(ident, localProcesses){
		var processes = {};
		for(var i = 0; i < localProcesses.length; i++){
			var astNode = localProcesses[i].ident;
			processes[astNode.ident] = {
				process : localProcesses[i].process,
				root : undefined
			}
		}

		return processes;
	}

	function interpretNode(astNode, currentTransition, root, ident){
		switch(astNode.type){
			case 'sequence':
				interpretSequence(astNode, currentTransition, root, ident);
				break;
			case 'choice':
				interpretChoice(astNode, currentTransition, root, ident);
				break;
			case 'composite':
				interpretComposite(astNode, currentTransition, root, ident);
				break;
			case 'function':
				interpretFunction(astNode, currentTransition, root, ident);
				break;
			case 'identifier':
				interpretIdentifier(astNode, currentTransition, root, ident);
				break;
			case 'terminal':
				interpretTerminal(astNode, currentTransition, root, ident);
				break;
			default:
				break;
		}

		// check if a labelling has been defined
		if(astNode.label !== undefined){
			// label is an action label node
			var net = processesMap[ident].clone(astNode.label.action);
			processLabelling(net, astNode.label.action);
			processesMap[ident] = net;
		}

		// check if a relabelling has been defined
		if(astNode.relabel !== undefined){
			processRelabelling(processesMap[ident], astNode.relabel.set);
		}
	}

	function interpretSequence(astNode, currentTransition, root, ident){
		var net = processesMap[ident];
		var place = root;
		// check if the currentTransition has been defined
		if(currentTransition !== undefined){
			var place = net.addPlace();
			constructConnection(currentTransition, [place]);
		}

		// construct the new transition
		var id = net.nextTransitionId;
		var transition = net.addTransition(id, astNode.from.action, [place]);

		interpretNode(astNode.to, transition, root, ident);
	}

	function interpretChoice(astNode, currentTransition, root, ident){
		var net = processesMap[ident];

		// setup each branch
		var roots1;
		if(astNode.process1.type === 'identifier'){
			if(localProcessesMap[astNode.process1.ident] !== undefined){
				var previousRoot = localProcessesMap[astNode.process1.ident].root;
				var process = localProcessesMap[astNode.process1.ident].process;
				var tempRoot = net.addPlace();
				tempRoot.addMetaData('potentialRoot', 1);
				localProcessesMap[astNode.process1.ident].root = tempRoot;
				roots1 = processSubPetriNet(ident, process, tempRoot);
				localProcessesMap[astNode.process1.ident].root = previousRoot;
			}
			else{
				roots1 = processSubPetriNet(ident, astNode.process1);
			}
		}
		else{
			roots1 = processSubPetriNet(ident, astNode.process1);
		}

		var roots2;
		if(astNode.process2.type === 'identifier'){
			if(localProcessesMap[astNode.process2.ident] !== undefined){
				var previousRoot = localProcessesMap[astNode.process2.ident].root;
				var process = localProcessesMap[astNode.process2.ident].process;
				var tempRoot = net.addPlace();
				tempRoot.addMetaData('potentialRoot', 1);
				localProcessesMap[astNode.process2.ident].root = tempRoot;
				roots2 = processSubPetriNet(ident, process, tempRoot);
				localProcessesMap[astNode.process2.ident].root = previousRoot;
			}
			else{
				roots2 = processSubPetriNet(ident, astNode.process2);
			}
		}
		else{
			roots2 = processSubPetriNet(ident, astNode.process2);
		}

		// perform a cross product of the roots from each branch
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
			constructConnection(currentTransition, crossProducts);
		}
		else{
			var transitions = root.incomingTransitions.map(id => net.getTransition(id));
			if(transitions.length !== 0){
				for(var i = 0; i < transitions.length; i++){
					constructConnection(transitions[i], crossProducts);
					transitions[i].deleteOutgoingPlace(root);
				}
			}
			else{
				for(var i = 0; i < crossProducts.length; i++){
					crossProducts[i].addMetaData('potentialRoot', 1);
				}
			}

			net.removePlace(root.id);
		}
	}

	function interpretComposite(astNode, currentTransition, root, ident){
		var net = processesMap[ident];

		// interpret both branches
		var net1 = new TempNet(net.id + '.a', ident + '.a', astNode.process1);
		var net2 = new TempNet(net.id + '.b', ident + '.b', astNode.process2);
		var composedNet = parallelComposition(net.id + '.comp', net1, net2);

		net.addPetriNet(composedNet);
		var roots = composedNet.places.filter(p => p.getMetaData('potentialRoot') !== undefined);
		for(var i = 0; i < roots.length; i++){
			roots[i].deleteMetaData('isRoot');
			net.removeRoot(roots[i].id);
		}
		// check and see if the current transition has been defined
		if(currentTransition !== undefined){
			constructConnection(currentTransition, roots);
		}
		else{
			var transitions = root.incomingTransitions.map(id => net.getTransition(id));
			if(transitions.length !== 0){
				for(var i = 0; i < transitions.length; i++){
					constructConnection(transitions[i], roots);
				}
			}
			else{
				for(var i = 0; i < roots.length; i++){
					roots[i].addMetaData('potentialRoot', 1);
				}
			}

			net.removePlace(root.id);
		}

		delete processesMap[ident + '.a'];
		delete processesMap[ident + '.b'];		
	}

	function interpretFunction(astNode, currentTransition, root, ident){
		var net = processesMap[ident];

		var processedNet = new TempNet(net.id + '.f', ident + '.f', astNode.process);
		var roots = processedNet.places.filter(p => p.getMetaData('potentialRoot') !== undefined);
		for(var i = 0; i < roots.length; i++){
			roots[i].addMetaData('startPlace', 1);
			processedNet.addRoot(roots[i].id);
		}

		var type = astNode.func;
		switch(type){
			case 'abs':
				processedNet = abstraction(processedNet, isFairAbstraction);
				break;
			case 'simp':
				processedNet = bisimulation(processedNet);
				break;
			default:
				break;
		}

		roots = processedNet.roots;
		for(var i = 0; i < roots.length; i++){
			var tokens = roots[i].getMetaData('startPlace');
			roots[i].addMetaData('potentialRoot', tokens);
			roots[i].deleteMetaData('startPlace');
			processedNet.removeRoot(roots[i].id);
		}

		net.addPetriNet(processedNet);

		// check if the current transition has been defined
		if(currentTransition !== undefined){
			constructConnection(currentTransition, roots);
		}
		else{
			var transitions = root.incomingTransitions.map(id => net.getTransition(id));
			if(transitions.length !== 0){
				for(var i = 0; i < transitions.length; i++){
					constructConnection(transitions[i], roots);
				}
				for(var i = 0; i < roots.length; i++){
					roots[i].deleteMetaData('potentialRoot');
				}
			}

			net.removePlace(root.id);
		}

		delete processesMap[ident + '.f'];
	}

	function interpretIdentifier(astNode, currentTransition, root, ident){
		var net = processesMap[ident];
		var current = astNode.ident;

		// check if this process is referencing itself
		if(current === ident){
			// check if the current transition is defined
			if(currentTransition !== undefined){
				constructConnection(currentTransition, [globalRoot]);
			}
			else{
				root.addMetaData('isTerminal', 'stop');
			}
		}
		else if(localProcessesMap[current] !== undefined){
			if(localProcessesMap[current].root !== undefined){
				// check if the current transition is defined
				if(currentTransition !== undefined){
					constructConnection(currentTransition, [localProcessesMap[current].root]);
				}
				else{
					localProcessesMap[current].root.addMetaData('potentialRoot', 1);
				}
			}
			else{
				var place = root;
				// check if the current transition is 
				if(currentTransition !== undefined){
					place = net.addPlace();
					constructConnection(currentTransition, [place]);
				}

				localProcessesMap[current].root = place;
				interpretNode(localProcessesMap[current].process, undefined, place, ident);
			}
		}
		else if(processesMap[ident] !== undefined){
			var referencedNet = processesMap[current].clone();
			var roots = net.addPetriNet(referencedNet);
			for(var i = 0; i < roots.length; i++){
				var tokens = roots[i].getMetaData('startPlace');
				roots[i].deleteMetaData('startPlace');
				roots[i].addMetaData('potentialRoot', tokens);
				net.removeRoot(roots[i].id);
			}

			// check that the current transition is defined
			if(currentTransition !== undefined){
				constructConnection(currentTransition, roots);
				for(var i = 0; i < roots.length; i++){
					roots[i].deleteMetaData('potentialRoot');
				}
			}
			else{
				net.removePlace(root.id);
			}
		}

	}

	function interpretTerminal(astNode, currentTransition, root, ident){
		var net = processesMap[ident];
		var place = currentTransition !== undefined ? net.addPlace() : root;
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
			constructConnection(currentTransition, [place]);
		}
		else if(root.incomingTransitions.length === 0){
			place.addMetaData('potentialRoot', 1);
		}
	}

	function constructConnection(transition, places){
		// connect each place to the transition
		for(var i = 0; i < places.length; i++){
			transition.addOutgoingPlace(places[i]);
			places[i].addIncomingTransition(transition.id);
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