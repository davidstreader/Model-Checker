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
		var net = new PetriNet(id);

		// add places to the composed net
		var places = net1.places.concat(net2.places);
		for(var i = 0; i < places.length; i++){
			net.addPlace(places[i].id, places[i].metaData);
			// check if this place is a start or terminal place
			if(places[i].getMetaData('startPlace') !== undefined){
				net.addRoot(places[i].id);
			}
			if(places[i].getMetaData('isTerminal') !== undefined){
				net.addTerminal(places[i].id);
			}
		}

		var labelSets1 = net1.labelSets;
		var labelSets2 = net2.labelSets;

		for(var action in labelSets1){
			var transitions = labelSets1[action];
			for(var i = 0; i < transitions.length; i++){
				var incoming = transitions[i].incomingPlaces;
				var outgoing = transitions[i].outgoingPlaces;
				// check if the actions are synced
				if(labelSets2[action] !== undefined){
					var synced = labelSets2[action];
					for(var j = 0; j < synced.length; j++){
						incoming = incoming.concat(synced[j].incomingPlaces);
						outgoing = outgoing.concat(synced[j].outgoingPlaces);
					}
				}

				// update references to incoming and outgoing places
				for(var j = 0; j < incoming.length; j++){
					incoming[j] = net.getPlace(incoming[j].id);
				}
				for(var j = 0; j < outgoing.length; j++){
					outgoing[j] = net.getPlace(outgoing[j].id);
				}

				net.addTransition(transitions[i].id, action, incoming, outgoing);
			}
		}

		for(var action in labelSets2){
			if(labelSets1[action] === undefined){
				var transitions = labelSets2[action];
				for(var i = 0; i < transitions.length; i++){
					// update references to incoming and outgoing places
					var incoming = transitions[i].incomingPlaces;
					for(var j = 0; j < incoming.length; j++){
						incoming[j] = net.getPlace(incoming[j].id);
					}
					var outgoing = transitions[i].outgoingPlaces;
					for(var j = 0; j < outgoing.length; j++){
						outgoing[j] = net.getPlace(outgoing[j].id);
					}

					net.addTransition(transitions[i].id, action, incoming, outgoing);
				}
			}
		}

		return net;

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