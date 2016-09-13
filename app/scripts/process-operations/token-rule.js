'use strict';

function tokenRule(process, operation){
	if(operation === 'unreachableStates'){

	}
	else if(operation === 'toAutomaton'){
		return petrinetToAutomaton(process);
	}
}

function petrinetToAutomaton(process){
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
	var rootIds = process.roots.map(place => place.id);
	fringe.push(new FringeElement(rootIds, root));

	while(fringe.length !== 0){
		var current = fringe.pop();
		var {places, node} = current; // deconstruct the fringe element
		var tokenCount = {};

		for(var i = 0; i < places.length; i++){
			var place = process.getPlace(places[i]);
			var outgoingTransitions = place.outgoingTransitions;

			for(var j = 0; j < outgoingTransitions.length; j++){
				var transition = transitionMap[outgoingTransitions[j]];

				// pass a token from the current place to the transition
				if(tokenCount[transition.id] === undefined){
					tokenCount[transition.id] = 1;
				}
				else{
					tokenCount[transition.id]++;
				}

				// check if the transition is executable
				if(isExecutable(transition, tokenCount[transition.id])){
					var action = transition.label;
					var outgoingPlaces = transition.outgoingPlaces.map(place => place.id);

					// add the places that can be traversed to from this transition
					var start = places.slice(0, i);
					var end = places.slice(i + 1, places.length);
					var newPlaces = start.concat(outgoingPlaces, end);
					var key = JSON.stringify(newPlaces);

					// check if this state configuration has been executed
					if(visitedStates[key] === undefined){
						var nextNode = graph.addNode();
						graph.addEdge(graph.nextEdgeId, action, node.id, nextNode.id);

						// add current state to visited states
						visitedStates[key] = nextNode;

						// push state to fringe
						fringe.push(new FringeElement(newPlaces, nextNode));
					}
					else{
						var nextNode = visitedStates[key];
						graph.addEdge(graph.nextEdgeId, action, node.id, nextNode.id);
					}
				}
			}
		}
	}

	return graph;
}

function TransitionMap(transitions){
	var transitionMap = {};
	for(var i = 0; i < transitions.length; i++){
		transitionMap[transitions[i].id] = transitions[i];
	}

	return transitionMap;
}

function FringeElement(places, node){
	return { places:places, node:node };
}

function isExecutable(transition, tokens){
	var requiredTokens = transition.incomingPlaces.length;
	return requiredTokens === tokens;
}