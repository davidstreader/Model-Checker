'use strict';

function automataAbstraction(automaton, isFair, prune){
	if(prune){
		pruneAutomaton(automaton);
	}

	const observableEdgeMap = {};
	const walker = new AutomatonWalker(automaton);

	const hiddenEdges = automaton.edges.filter(e => e.label === TAU);

	if(hiddenEdges.length === 0){
		return automaton;
	}

	for(let i = 0; i < hiddenEdges.length; i++){
		constructIncomingObservableEdges(hiddenEdges[i]);
		constructOutgoingObservableEdges(hiddenEdges[i]);
	}

	// add the observable edges to the automaton
	for(let key in observableEdgeMap){
		const edge = observableEdgeMap[key];
		automaton.addEdge(automaton.nextEdgeId, edge.label, automaton.getNode(edge.from), automaton.getNode(edge.to));
	}
	// remove the hidden edges from the automaton
	for(let i = 0; i < hiddenEdges.length; i++){
		automaton.removeEdge(hiddenEdges[i].id);
	}

	automaton.trim();
	return automaton;

	function constructIncomingObservableEdges(hiddenEdge){
		const outgoingNodes = getOutgoingNodes(hiddenEdge);
		const incomingNodes = walker.getIncomingNodes(automaton.getNode(hiddenEdge.from));
		for(let i = 0; i < incomingNodes.length; i++){
			const {edge, node} = incomingNodes[i];

			// construct observable actions if the edge's action is observable
			if(edge.label !== TAU){
				for(let j = 0; j < outgoingNodes.length; j++){
					const next = outgoingNodes[j];
					constructObservableEdge(node.id, next.id, edge.label, edge, hiddenEdge);
				}
			}
		}
	}

	function constructOutgoingObservableEdges(hiddenEdge){
		const incomingNodes = getIncomingNodes(hiddenEdge);
		const outgoingNodes = walker.getOutgoingNodes(automaton.getNode(hiddenEdge.to));
		for(let i = 0; i < outgoingNodes.length; i++){
			const {edge, node} = outgoingNodes[i];

			// construct observable actions if the edge's action is observable
			if(edge.label !== TAU){
				for(let j = 0; j < incomingNodes.length; j++){
					const next = incomingNodes[j];
					constructObservableEdge(next.id, node.id, edge.label, edge, hiddenEdge);
				}
			}
		}
	}

	function getOutgoingNodes(hiddenEdge){
		const nodes = [];

		const visited = {};
		visited[hiddenEdge.from] = true;

		const fringe = [hiddenEdge];
		while(fringe.length !== 0){
			const current = fringe.pop();
			const node = automaton.getNode(current.to);
			nodes.push(node);

			// add any hidden edges from this node to the fringe
			const edges = walker.getOutgoingEdges(node).filter(e => e.label === TAU);
			for(let i = 0; i < edges.length; i++){
				const next = automaton.getNode(edges[i].to);

				// add the next node to the fringe if it has not been visited already
				if(!visited[next.id]){
					fringe.push(edges[i]);
				}
			}

			// mark this node as visited
			visited[node.id] = true;
		}

		return nodes;
	}

	function getIncomingNodes(hiddenEdge){
		const nodes = [];

		const visited = {};
		visited[hiddenEdge.to] = true;

		const fringe = [hiddenEdge];
		while(fringe.length !== 0){
			const current = fringe.pop();
			const node = automaton.getNode(current.from);
			nodes.push(node);

			// add any hidden edges to this node to the fringe
			const edges = walker.getIncomingEdges(node).filter(e => e.label === TAU);
			for(let i = 0; i < edges.length; i++){
				const next = automaton.getNode(edges[i].from);

				// add the next node to the fringe if it has not been visited already
				if(!visited[next.id]){
					fringe.push(edges[i]);
				}
			}

			// mark this node as visited
			visited[node.id] = true;
		}

		return nodes;
	}

	function pruneAutomaton(automaton){
		const walker = new AutomatonWalker(automaton);

		const nodes = automaton.nodes;
		for(let i = 0; i < nodes.length; i++){
			const node = nodes[i];
			const edges = walker.getOutgoingEdges(node);
			const hidden = edges.filter(e => e.label === TAU);

			if(edges.length === hidden.length && edges.length !== 0){
				const incomingNodes = walker.getIncomingNodes(node);
				const outgoingNodes = walker.getOutgoingNodes(node);
				for(let j = 0; j < incomingNodes.length; j++){
					const incoming = incomingNodes[j];
					for(let k = 0; k < outgoingNodes.length; k++){
						const outgoing = outgoingNodes[k];

						if(incoming.node.id !== outgoing.node.id){
							const id = automaton.nextEdgeId;
							const edge = automaton.addEdge(id, incoming.edge.label, incoming.node, outgoing.node);
							edge.locations = incoming.edge.locations;
						}
					}
				}

				automaton.removeNode(node.id);
			}
		}
	}

  /**
   * Constructs an observable edge object and adds it to the
   * observable edge map.
   *
   * @param {string} from - the node id the edge transitions from
   * @param {string} to - the node id the edge transitions to
   * @param {string} label - the action the edge represents
   * @param {string} edge - the edge that we are merging the hidden edge with
   * @param {string} hiddenEdge - the hidden edge
   */
	function constructObservableEdge(from, to, label, edge, hiddenEdge){
		var key = constructEdgeKey(from, to, label);
		if(observableEdgeMap[key] === undefined){
			observableEdgeMap[key] = new ObservableEdge(from, to, label,  edge, hiddenEdge);
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
	function ObservableEdge(from, to, label, edge, hiddenEdge){
	  if (combineEdges) {
      console.log(combineEdges(edge,hiddenEdge));
    }
		return {
      from : from,
      to : to,
      label : label
    };
	}
}
