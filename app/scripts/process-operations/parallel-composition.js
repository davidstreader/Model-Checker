'use strict';

function parallelComposition(id, process1, process2){
	// check that processes are of the same type
	if(process1.type !== process2.type){
		// throw error
	}

	var type = process1.type;
	if(type === 'automata'){
		return automataParallelComposition(id, process1, process2);
	}
	else if(type === 'petrinet'){
		return petriNetParallelComposition(id, process1, process2);
	}
	else{
		// throw error
	}

	function automataParallelComposition(id, automaton1, automaton2){
		var graph = new Graph(id);
		var nodes1 = automaton1.nodes;
		var nodes2 = automaton2.nodes;
		combineStates(graph, nodes1, nodes2);
		var alphabet1 = automaton1.alphabet;
		var alphabet2 = automaton2.alphabet;
		var alphabet = alphabetUnion(alphabet1, alphabet2);

		for(var i = 0; i < nodes1.length; i++){
			var node1 = nodes1[i];
			for(var j = 0; j < nodes2.length; j++){
				var node2 = nodes2[j];

				// the id for the current combined state
				var fromId = node1.id + '.' + node2.id;

				for(var action in alphabet){
					var coaccessible1 = node1.coaccessible(action);
					coaccessible1 = (coaccessible1.length !== 0) ? coaccessible1 : [undefined];
					var coaccessible2 = node2.coaccessible(action);
					coaccessible2 = (coaccessible2.length !== 0) ? coaccessible2 : [undefined];

					for(var x = 0; x < coaccessible1.length; x++){
						var c1 = coaccessible1[x];
						for(var y = 0; y < coaccessible2.length; y++){
							var c2 = coaccessible2[y];

							if(c1 !== undefined && c2 !== undefined){
								var toId = c1 + '.' + c2;
								graph.addEdge(graph.nextEdgeId, action, fromId, toId);
							}
							else if(c1 !== undefined && alphabet2[action] === undefined){
								var toId = c1 + '.' + node2.id;
								graph.addEdge(graph.nextEdgeId, action, fromId, toId);
							}
							else if(c2 !== undefined && alphabet1[action] === undefined){
								var toId = node1.id + '.' + c2;
								graph.addEdge(graph.nextEdgeId, action, fromId, toId);
							}
						}
					}
				}
			}
		}

		// remove unreachable nodes from the composition
		graph.trim();
		return graph;

		/**
		 * Helper function for automataParallelComposition that creates a new
		 * node in the specified automaton for each combined state from the specified
		 * node arrays. Each node from the first array is combined with each node from
		 * the second array.
		 *
		 * @param {automaton} graph - the automaton to add nodes to
		 * @param {node[]} nodes1 - the first array of nodes
		 * @param {node[]} nodes2 - the second array of nodes
		 */
		function combineStates(graph, nodes1, nodes2){
			for(var i = 0; i < nodes1.length; i++){
				for(var j = 0; j < nodes2.length; j++){
					var id = nodes1[i].id + '.' + nodes2[j].id;
					var metaData = metaDataIntersection(nodes1[i].metaData, nodes2[j].metaData);
					graph.addNode(id, '', metaData);

					// check if this is the first node constructed
					if(i === 0 && j === 0){
						graph.rootId = id;
					}
				}
			}
		}
	}

	/**
	 * Performs parallel composition of the two specified petri nets. Constructs
	 * a new petri net with the specified id representing the composition. Returns
	 * the petri net constructed by the composition.
	 *
	 * @param {int} id - the id of the composition
	 * @param {petrinet} net1 - the first petri net
	 * @param {petrinet} net2 - the second petri net
	 * @return {petrinet} - the petri net formed by the composition
	 */
	function petriNetParallelComposition(id, net1, net2){
		// construct place map for the composed net
		var placeMap = {};
		var placeCount = 0;
		var places = net1.places.concat(net2.places);
		for(var i = 0; i < places.length; i++){
			placeMap[places[i].id] = places[i];
			placeCount++;
		}

		var rootIds = net1.roots.concat(net2.roots).filter(function(place){
			return place.getMetaData('startPlace') !== undefined;
		})

		rootIds = rootIds.map(function(place){
			return place.id;
		})

		// construct transition map and label sets for the composed net
		var transitionMap = {};
		var labelSets = {};
		var transitionCount = 0;

		// add the transitions from the first petri net
		var labelSets1 = net1.labelSets;
		for(var i = 0; i < labelSets1.length; i++){
			var label = labelSets1[i].label;
			var transitions = labelSets1[i].transitions;
			for(var j = 0; j < transitions.length; j++){
				transitionMap[transitions[j].id] = transitions[j];
				transitionCount++;
			}
			labelSets[label] = transitions;
		}

		// add transitions from the second petri net and check for duplicate labels
		var labelSets2 = net2.labelSets;
		for(var i = 0; i < labelSets2.length; i++){
			var label = labelSets2[i].label;
			var transitions = labelSets2[i].transitions;

			// check if this is a duplicate label
			if(labelSets[label] !== undefined){
				// sync actions that occur in both petri nets
				var set = labelSets[label];
				for(j = 0; j < set.length; j++){
					for(var k = 0; k < transitions.length; k++){
						transitionMap[transitions[k].id] = transitions[k];
						transitionCount++;
						syncTransitions(set[j], transitions[k]);
					}
				}

				labelSets[label] = labelSets[label].concat(transitions);
			}
			else{
				// proceed as normal
				for(var j = 0; j < transitions.length; j++){
					transitionMap[transitions[j].id] = transitions[j];
					transitionCount++;
				}

				labelSets[label] = transitions;
			}
		}

		// return composed net
		return new PetriNet(id, placeMap, placeCount, transitionMap, labelSets, transitionCount, rootIds);

		/**
		 * Helper function for processing the parallel composition of petri nets
		 * which syncs the incoming and outgoing places from the two specified
		 * transitions
		 */
		function syncTransitions(transition1, transition2){
			var places = transition1.outgoingPlaces;
			for(var i = 0; i < places.length; i++){
				transition2.addOutgoingPlace(places[i]);
				places[i].addIncomingTransition(transition2.id);
			}
			places = transition2.outgoingPlaces;
			for(var i = 0; i < places.length; i++){
				transition1.addOutgoingPlace(places[i]);
				places[i].addIncomingTransition(transition1.id);
			}

			places = transition1.incomingPlaces;
			for(var i = 0; i < places.length; i++){
				transition2.addIncomingPlace(places[i]);
				places[i].addOutgoingTransition(transition2.id);
			}
			places = transition2.incomingPlaces;
			for(var i = 0; i < places.length; i++){
				transition1.addIncomingPlace(places[i]);
				places[i].addOutgoingTransition(transition1.id);
			}
		}
	}

	/**
	 * The following are helper functions that can be utilised by parallel
	 * composition functions.
	 */

	/**
	 * Helper function for parallel composition functions that creates
	 * and returns a union of the data stored in the two alphabet sets.
	 *
	 * @param {string{}} alphabet1 - the first alphabet set
	 * @oaram {string{}} alphabet2 - the second alphabet set
	 * @return {string{}} - the unioned alphabet set
	 */
	function alphabetUnion(alphabet1, alphabet2){
		var alphabet = {};
		// add actions from first alphabet
		for(var action in alphabet1){
			alphabet[action] = true;
		}

		// add actions from second alphabet
		for(var action in alphabet2){
			alphabet[action] = true;
		}

		return alphabet;
	}

	/**
	 * Helper function for parallel composition functions that creates
	 * and returnds an intersection of the data stored in two meta data objects.
	 *
	 * @param {object} metaData1 - first meta data set
	 * @param {object} metaData2 - second meta data set
	 * @return {object} - the intersected meta data
	 */
	function metaDataIntersection(metaData1, metaData2){
		var metaData = {};
		// check if there are any matching keys between the two meta data sets
		for(var key in metaData1){
			// check if there is a match
			if(metaData2[key] !== undefined){
				// check that the value stored is the same for both sets
				if(metaData1[key] === metaData2[key]){
					metaData[key] = metaData1[key];
				}
			}
		}

		return metaData;
	}
}