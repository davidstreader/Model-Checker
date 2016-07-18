'use strict';

/**
 * Performs a breadth first traversal of the specified process and returns the resulting 
 * breadth first traversal graph.
 *
 * @param {int} id - the id for the breadth first traversal graph
 * @param {object} process - the process to traverse
 * @return {bft graph} - the breadth first traversal graph
 */
function breadthFirstTraversal(id, process){
	var type = process.type;
	if(type === 'automata'){
		return automataBFT(id, process);
	}
	else if(type === 'petrinet'){
		return petriNetBFT(id, process);
	}
	else{
		// throw error
	}

	/**
	 * Performs a breadth first traversal of the specified automaton and returns the resulting
	 * breadth first traversal graph.
	 *
	 * @param {int} id - the id for the breadth first traversal graph
	 * @param {automaton} process - the auomtaton to traverse
	 * @return {bft graph} - the breadth first traversal graph
	 */
	function automataBFT(id, process){
		// TODO
	}

	/**
	 * Performs a breadth first traversal of the specified Petri net and returns the resulting
	 * breadth first traversal graph.
	 *
	 * @param {int} id - the id for the breadth first traversal graph
	 * @param {petrinet} process - the petri net to traverse
	 * @return {bft graph} - the breadth first traversal graph
	 */
	function petriNetBFT(id, process){
		var graph = new BFTGraph(id);
		var transitionToNode = constructTransitionToNodeMap(graph, process.transitions);

		var visitedPlaces = {};
		var visitedTransitions = {};
		var placeToNode = {};
		var roots = process.roots;

		// perform the breadth first traversal
		for(var i = 0; i < roots.length; i++){
			var fringe = [];
			
			// push transitions from the current root place to the fringe
			var neighbours = roots[i].outgoingTransitions;
			for(var j = 0; j < neighbours.length; j++){
				fringe.push(neighbours[j]);
				graph.root.addChild(transitionToNode[neighbours[j]]);
			}

			// continue traversal while there are still transitions to process
			var index = 0;
			while(index < fringe.length){
				var currentTransition = process.getTransition(fringe[index++]);
				var currentNode = graph.getNode(transitionToNode[currentTransition.id]);

				// check if the current transition has been visited
				if(visitedTransitions[currentTransition.id] !== undefined){
					continue;
				}

				neighbours = currentTransition.outgoingPlaces;
				for(var j = 0; j < neighbours.length; j++){
					// check if current transition has been visited
					if(visitedPlaces[neighbours[j].id] !== undefined){
						continue;
					}

					var outgoing = neighbours[j].outgoingTransitions;
					// construct children for the current node
					for(var k = 0; k < outgoing.length; k++){
						currentNode.addChild(transitionToNode[outgoing[k]]);
						// push current transition to the fringe
						fringe.push(outgoing[k]);
					}

					// mark current place as being visited
					visitedPlaces[neighbours[j].id] = true;
				}

				// mark current transition as being visited
				visitedTransitions[currentTransition.id] = true;
			}
		}

		return graph;

		/**
		 * Constructs and returns a mapping from the transition ids from a Petri net to the nodes
		 * in a breadth first traversal graph that will be representing each transition.
		 *
		 * @param {bft graph} - the breadth first traversal graph
		 * @param {transition[]} - an array of transitions
		 * @return {string -> string} - a mapping from transition id to node id
		 */
		function constructTransitionToNodeMap(graph, transitions){
			var count = {};
			var transitionToNode = {};

			// construct nodes for each transition in the petri net
			for(var i = 0; i < transitions.length; i++){
				var transition = transitions[i];
				// check if there has not been a transition with this label before
				if(count[transition.label] === undefined){
					count[transition.label] = 0;
				}

				var id = transition.label + count[transition.label]++;
				graph.addNode(id, transition.label);
				transitionToNode[transition.id] = id;
			}

			return transitionToNode;
		}
	}
}