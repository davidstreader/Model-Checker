'use strict'

function tokenRule(process, operation){
	if(operation === 'unreachableStates'){

	}
	else if(operation === 'toAutomaton'){
		return petriNetToAutomaton(process);
	}

	function petriNetToAutomaton(process){
		// setup the automaton to construct
		var graph = new Graph(process.id);
		var root = graph.addNode();
		root.addMetaData('startNode', true);
		graph.root = root;

		// setup transitions
		var transitionMap = new TransitionMap(process.transitions);
		var visitedStates = {};

		// setup the fringe
		var fringe = [];
		var roots = process.roots;
		fringe.push(new FringeElement(new PlaceSet(roots), root));

		while(fringe.length !== 0){
			var current = fringe.pop();
			var {places, node} = current // deconstruct the fringe element

			// get the outgoing transitions the current state
			var transitionIds = {};
			for(var id in places){
				var outgoing = process.getPlace(id).outgoingTransitions;
				for(var i = 0; i < outgoing.length; i++){
					transitionIds[outgoing[i]] = true;
				}
			}

			var transitions = [];
			for(var id in transitionIds){
				transitions.push(process.getTransition(id));
			}

			// check if there are any transitions
			if(transitions.length === 0){
				node.addMetaData('isTerminal', 'stop');
				continue;
			}

			// find the transitions that can be executed from the current state
			for(var i = 0; i < transitions.length; i++){
				var transition = transitions[i];
				var incoming = new PlaceSet(transition.incomingPlaces);

				// check if this transition is executable from the current state
				var isExecutable = true;
				for(var id in incoming){
					if(places[id] === undefined){
						isExecutable = false;
						break;
					}
				}

				if(!isExecutable){
					continue;
				}

				// construct the state that the Petri net is in after execution
				var nextState = JSON.parse(JSON.stringify(places));
				// remove the places that were transiitoned from
				for(var id in incoming){
					delete nextState[id];
				}

				// add the states that were transitioned to
				var outgoing = transition.outgoingPlaces;
				for(var j = 0; j < outgoing.length; j++){
					nextState[outgoing[j].id] = true;
				}

				var nextStateKey = constructStateKey(nextState);

				// check if this state has already been visited
				if(visitedStates[nextStateKey] !== undefined){
					graph.addEdge(graph.nextEdgeId, transition.label, node.id, visitedStates[nextStateKey].id);
				}
				else{
				// execute the transition
					var next = graph.addNode();
					graph.addEdge(graph.nextEdgeId, transition.label, node.id, next.id);

					// push the new state to the fringe
					fringe.push(new FringeElement(nextState, next));

					// mark state as visited
					visitedStates[nextStateKey] = next;
				}
			}
		}

		return graph;
	}
}

function TransitionMap(transitions){
	var transitionMap = {};
	for(var i = 0; i < transitions.length; i++){
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
	var placeSet = {};
	for(var i = 0; i < places.length; i++){
		placeSet[places[i].id] = true;
	}

	return placeSet;
}

function constructStateKey(placeSet){
	var states = [];
	for(var id in placeSet){
		states.push(id);
	}

	return JSON.stringify(states.sort());
}