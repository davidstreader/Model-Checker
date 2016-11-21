'use strict'

/**
 * Performs the abstraction process on the specified automaton. This process
 * removes the hidden, unobservable events (represented as tau) in the automaton
 * and replaces them with observable events.
 *
 * @param {automaton} process - the automaton to abstract
 * @return {automaton} - the abstracted processs
 */
function automataAbstraction(process, isFair){
	var observableEdgeMap = {};

	// get all nodes that have at least one hidden tau event traversing from it
	var nodes = process.edges.filter(edge => edge.label === TAU).map(edge => edge.from);

	// populate the obvservable edge map
	for(var id = 0; id < nodes.length; id++){
		constructObservableEdges(process.getNode(nodes[id]));
	}

	// add the observable edges to the process
	for(var key in observableEdgeMap){
		var edge = observableEdgeMap[key];
		process.addEdge(process.nextEdgeId, edge.label, process.getNode(edge.from), process.getNode(edge.to));
	}

	// delete the hidden tau events from the process
	var tauEdges = process.edges.filter(edge => edge.label === TAU);
	for (var i = 0; i < tauEdges.length; i++){
		var from = process.getNode(tauEdges[i].from);
		from.removeOutgoingEdge(tauEdges[i].id);
		var to = process.getNode(tauEdges[i].to);
		to.removeIncomingEdge(tauEdges[i].id); // to was from before???
		process.removeEdge(tauEdges[i].id);
	}

	nodes = process.nodes;
	for(var i = 0; i < nodes.length; i++){
		var current = nodes[i];
		if(current.incomingEdges.map(id => process.getEdge(id))
			.length === 0 && current.getMetaData('startNode') === undefined){
			process.removeNode(current.id);
		}
		else if(current.outgoingEdges.map(id => process.getEdge(id)).length === 0 && current.getMetaData('isTerminal') === undefined){
			current.addMetaData('isTerminal', 'stop');
		}
	}

	return process;

	function constructObservableEdges(node){
		// get observable events (edges) that transition to the specified node
		var incomingObservableEdges = node.incomingEdges.map(id => process.getEdge(id))
		.filter(edge => edge.label !== TAU);

		var visited = {};
		var fringe = [node];
		while(fringe.length !== 0){
			var current = fringe.pop();
			// get neighbouring nodes from the current node that are transitionable to via a hidden tau event
			var neighbours = current.outgoingEdges.map(id => process.getEdge(id)).filter(edge => edge.label === TAU).map(edge => process.getNode(edge.to));

			// iterate over the neigbouring nodes and add observable edges if possible
			for(var i = 0; i < neighbours.length; i++){
				var neighbour = neighbours[i];

				// add dead locked state if the abstraction is defined as unfair
				if(neighbour.id === node.id){
					if(!isFair){
						var deadState = process.addNode(process.nextNodeId);
						deadState.addMetaData('isTerminal', 'error');
						process.addEdge(process.nextEdgeId, DELTA, node.id, deadState.id);
					}
					
					continue;
				}

				// check if the current node has been visited
				if(visited[neighbour.id] !== undefined){
					continue;
				}

				// push the neighbour to the fringe
				fringe.push(neighbour);

				var outgoingObservableEdges = neighbours[i].outgoingEdges.map(id => process.getEdge(id)).filter(edge => edge.label !== TAU);

				for(var j = 0; j < incomingObservableEdges.length; j++){
					var edge = incomingObservableEdges[j];
					constructObservableEdge(edge.from, neighbour.id, edge.label);
				}

				for(var j = 0; j < outgoingObservableEdges.length; j++){
					var edge = outgoingObservableEdges[j];
					constructObservableEdge(node.id, edge.to, edge.label);
				}
			}

			// mark the current node as visited
			visited[current.id] = true;
		}
	}

	/**
	 * Constructs an observable edge object and adds it to the
	 * observable edge map.
	 *
	 * @param {string} from - the node id the edge transitions from
	 * @param {string} to - the node id the edge transitions to
	 * @param {string} label - the action the edge represents
	 */
	function constructObservableEdge(from, to, label){
		var key = constructEdgeKey(from, to, label);
		if(observableEdgeMap[key] === undefined){
			observableEdgeMap[key] = new ObservableEdge(from, to, label);
		}
	}

	/**
	 * Constructs and returns key that refers to an observable edge.
	 *
	 * @param {string} from - the node id the edge transitions from
	 * @param {string} to - the node id the edge transitions to
	 * @param {string} label - the action the edge represents
	 * @return {string} - the key for the edge
	 */
	function constructEdgeKey(from, to, label){
		return from + ' -' + label + '> ' + to;
	}

	/**
	 * Constructs and returns an observable edge object.
	 *
	 * @param {string} from - the node id the edge transitions from
	 * @param {string} to - the node id the edge transitions to
	 * @param {string} label - the action the edge represents
	 * @return {object} - object representing an observable edge
	 */
	function ObservableEdge(from, to, label){
		var edge = {
			from : from,
			to : to,
			label : label
		}

		return edge;
	}
}