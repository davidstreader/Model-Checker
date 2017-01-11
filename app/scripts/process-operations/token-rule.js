'use strict'

function tokenRule(process, operation){
	if(process.type !== 'petrinet'){
		return process;
	}

	if(operation === 'unreachableStates'){
		return removeUnreachableStates(process);
	}
	else if(operation === 'toAutomaton'){
		return petriNetToAutomaton(process);
	}

	function removeUnreachableStates(process){
		const walker = new PetriNetWalker(process);
		const visitedPlaces = {};
		const visitedTransitions = {};
		const markings = {};

		const rootSet = walker.initialMarking
		const rootKey = walker.markingKey(rootSet);

		// push root marking the the set of visited states
		markings[rootKey] = true;

		const fringe = [rootSet];
		while(fringe.length !== 0){
			const places = fringe.pop();

			// get the outgoing transitions from the current marking
			const transitions = walker.getOutgoingTransitions(places);

			for(let i = 0; i < transitions.length; i++){
				const transition = transitions[i];

				// check if this transition is executable from the current marking
				const nextState = walker.executeTransition(transition, places);
				if(nextState === undefined){
					continue;
				}

				// mark the transition as being visited
				visitedTransitions[transition.id] = true;

				const nextStateKey = walker.markingKey(nextState);

				// check if this state has already been visited
				if(markings[nextStateKey] === undefined){
					fringe.push(nextState);
				}
			}

			// mark these states as being visited
			for(let id in places){
				visitedPlaces[id] = true;
			}

			const stateKey = walker.markingKey(places);
			markings[stateKey] = true;
		}

		// remove any unvisted places from the petri net
		const places = process.places;
		for(let i = 0; i < places.length; i++){
			const place = places[i];
			if(visitedPlaces[place.id] === undefined){
				process.removePlace(place.id);
			}
		}

		// remove any unvisited transitions from the petri net
		const transitions = process.transitions;
		for(let i = 0; i < transitions.length; i++){
			const transition = transitions[i];
			if(visitedTransitions[transition.id] === undefined){
				process.removeTransition(transition.id);
			}
		}

		return process;
	}

	function petriNetToAutomaton(net){
		const automaton = new Automaton(net.id);
		const root = automaton.addNode();
		automaton.root = root.id;
		root.metaData.startNode = true;

		const walker = new PetriNetWalker(net);

		const markings = {};

		const roots = net.roots;
		const rootSet = {};
		for(let i = 0; i < roots.length; i++){
			const root = roots[i];
			rootSet[root.id] = root.metaData.startPlace;
		}
		const rootKey = walker.markingKey(rootSet);

		markings[rootKey] = root;

		const fringe = [new FringeElement(rootSet, root)];
		while(fringe.length !== 0){
			const {places, node} = fringe.pop();

			const transitions = walker.getOutgoingTransitions(places);

			// check if there are any transitions
			if(transitions.length === 0){
				node.metaData.isTerminal = 'stop';
				continue;
			}

			for(let i = 0; i < transitions.length; i++){
				const transition = transitions[i];
				const nextState = walker.executeTransition(transition, places);

				// check if transition was executable
				if(nextState === undefined){
					continue;
				}

				const nextStateKey = walker.markingKey(nextState);

				// check if this state has already been visited
				if(markings[nextStateKey] !== undefined){
					const edge = automaton.addEdge(automaton.nextEdgeId, transition.label, node, markings[nextStateKey], transition.metaDataSet);
					edge.locations = transition.locations;
				}
				else{
					const next = automaton.addNode();
					const nextLocations = {};
					for(let id in nextState){
						const place = net.getPlace(id);
						for(let location in place.locations){
							nextLocations[location] = true;
						}
					}
					next.locations = nextLocations;

					const edge = automaton.addEdge(automaton.nextEdgeId, transition.label, node, next, transition.metaDataSet);

					// push the new state to the fringe
					fringe.push(new FringeElement(nextState, next));

					// mark state as visited
					markings[nextStateKey] = next;
				}
			}
		}

		if(root.outgoingEdges.length === 0){
			root.metaData.isTerminal = 'stop';
		}

		for(let id in markings){
			delete markings[id];
		}

		return automaton;
	}
}

function FringeElement(places, node){
	return {
		places: places,
		node: node
	}
}