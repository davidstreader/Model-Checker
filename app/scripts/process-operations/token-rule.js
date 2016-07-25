'use strict';

function tokenRule(process, type){
	// check that the process is a petri net
	if(process.type !== 'petrinet'){
		// throw error
	}

	if(type === 'unreachable'){
		// TODO
	}
	else if(type === 'toAutomaton'){
		return convertPetriNetToAutomaton(process);
	}
	else{
		// throw error
	}

	function findUnreachableStates(process){
		// TODO
	}

	function convertPetriNetToAutomaton(process){
		var transitionMap = constructTransitionMap(process.transitions);
		var graph = new Graph(process.id);
		var root = graph.addNode();
		graph.root = root;
		var visitedPlaces = {};
		var visitedTransitions = {};
		var fringe = [[]];

		var roots = process.roots;
		for(var i = 0; i < roots.length; i++){
			fringe[0].push({ node:root, place:roots[i] });
		}

		var index = 0;
		while(index < fringe.length){
			var places = fringe[index++];
			// iterate over places in the current position
			for(var i = 0; i < places.length; i++){
				var place = places[i];
				
				// iterate over transitions for the current place
				var transitions = place.outgoingTransitions;
				for(var j = 0; j < transitions.length; j++){
					var transition = transitionMap[transitions[j]];
					
					// check if this transition already has a token from the current place
					if(transition.tokenMap[place.place.id] !== undefined){
						transition.tokenMap[place.place.id] = true;
						transition.tokenCount++;

						// check if the total number of tokens has been acquired
						if(transition.tokenCount === transition.requiredTokens){
							var nextNode = graph.addNode();
							graph.addEdge(graph.nextEdgeId, transition.transition.label, place.node.id, nextNode.id);

							// add to the fringe
							var outgoing = transition.transition.outgoingPlaces;
							if(outgoingPlaces.length !== 0){
								fringe.push(constructFringeElement(current, i, outgoing, nextNode));
							}
						}
					}
				}
			}
		}

		return graph;

		function constructTransitionMap(transitions){
			var transitionMap = {};
			for(var i = 0; i < transitions.length; i++){
				var required = transitions[i].outgoingPlaces.length;
				transitionMap[transition.id] = { tokenMap:{}, tokenCount:0, requiredTokens:required, transition:transitions[i] };
			}

			return transitionMap;
		}

		function constructFringeElement(current, index, places, node){
			var element = [];
			for(var i = 0; i < current.length; i++){
				if(i === index){
					for(var j = 0; j < places.length; j++){
						element.push({ node:node, place:places[j] });
					}
				}
				else{
					element.push(current[i]);
				}
			}

			return element;
		}
	}
}