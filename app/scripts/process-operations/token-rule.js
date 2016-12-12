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

	function petriNetToAutomaton(process){
		// setup the automaton to construct
		const graph = new Automaton(process.id);
		const root = graph.addNode();
		root.metaData.startNode = true;
		graph.root = root.id;

		// setup transitions
		const transitionMap = new TransitionMap(process.transitions);
		const visitedStates = {};

		// setup the fringe
		const fringe = [];
		const roots = process.roots;

		// construct root key and locations
		const rootLocations = {};
		const rootSet = {};
		for(let i = 0; i < roots.length; i++){
			const tokens = roots[i].getMetaData('startPlace');
			for(let j = 0; j < tokens; j++){
				if(rootSet[roots[i].id] === undefined){
					rootSet[roots[i].id] = 0;
				}
				rootSet[roots[i].id]++;
				for(let id in roots[i].locations){
					rootLocations[id] = true;
				}
			}
		}
		root.locations = rootLocations;

		const rootKey = constructStateKey(rootSet);
		// add the root to the visited state map
		visitedStates[rootKey] = root;

		fringe.push(new FringeElement(rootSet, root));

		while(fringe.length !== 0){
			const current = fringe.pop();
			const {places, node} = current // deconstruct the fringe element

			// get the outgoing transitions the current state
			const transitionIds = {};
			for(let id in places){
				const outgoing = process.getPlace(id).outgoingTransitions;
				for(let i = 0; i < outgoing.length; i++){
					transitionIds[outgoing[i]] = true;
				}
			}

			const transitions = [];
			for(let id in transitionIds){
				transitions.push(process.getTransition(id));
			}

			// check if there are any transitions
			if(transitions.length === 0){
				node.metaData.isTerminal = 'stop';
				continue;
			}

			// find the transitions that can be executed from the current state
			for(let i = 0; i < transitions.length; i++){
				const transition = transitions[i];
				const incoming = new PlaceSet(transition.incomingPlaces.map(id => process.getPlace(id)));

				// check if this transition is executable from the current state
				let isExecutable = true;
				for(let id in incoming){
					if(places[id] === undefined){
						isExecutable = false;
						break;
					}
				}

				if(!isExecutable){
					continue;
				}

				// construct the state that the Petri net is in after execution
				const nextState = JSON.parse(JSON.stringify(places));
				// remove the places that were transiitoned from
				for(let id in incoming){
					nextState[id]--;
					if(nextState[id] === 0){
						delete nextState[id];
					}
				}

				// add the states that were transitioned to
				const outgoing = transition.outgoingPlaces.map(id => process.getPlace(id));
				for(let j = 0; j < outgoing.length; j++){
					const nextId = outgoing[j].id;
					if(nextState[nextId] === undefined){
						nextState[nextId] = 0;
					}
					nextState[nextId]++;
				}

				const nextStateKey = constructStateKey(nextState);

				// check if this state has already been visited
				if(visitedStates[nextStateKey] !== undefined){
					const edge = graph.addEdge(graph.nextEdgeId, transition.label, node, visitedStates[nextStateKey], transition.metaDataSet);
					edge.locations = transition.locations;
				}
				else{
				// execute the transition
					const next = graph.addNode();
					const nextLocations = {};
					for(let id in nextState){
						const place = process.getPlace(id);
						for(let l in place.locations){
							nextLocations[l] = true;
						}
					}
					next.locations = nextLocations;

					const edge = graph.addEdge(graph.nextEdgeId, transition.label, node, next, transition.metaDataSet);
					edge.locations = transition.locations;

					// push the new state to the fringe
					fringe.push(new FringeElement(nextState, next));

					// mark state as visited
					visitedStates[nextStateKey] = next;
				}
			}
		}

		if(root.outgoingEdges.length === 0){
			root.addMetaData('isTerminal', 'stop');
		}

		return graph;
	}
}

function TransitionMap(transitions){
	const transitionMap = {};
	for(let i = 0; i < transitions.length; i++){
		transitionMap[transitions[i].id] = transitions[i];
	}

	return transitionMap;
}

function FringeElement(places, node){
	return {
		places: places,
		node: node
	}
}

function PlaceSet(places){
	const placeSet = {};
	for(let i = 0; i < places.length; i++){
		const id = places[i].id;
		if(placeSet[id] === undefined){
			placeSet[places[i].id] = 1;
		}
		else{
			placeSet[id]++;
		}
	}

	return placeSet;
}

function constructStateKey(placeSet){
	const states = [];
	for(let id in placeSet){
		for(let i = 0; i < placeSet[id]; i++){
			states.push(id);
		}
	}

	return JSON.stringify(states.sort());
}