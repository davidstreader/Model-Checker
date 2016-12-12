'use strict';

/**
 * Performs parallel composition of the two specified automata. Constructs
 * a new automaton with the specified id representing the composition. Returns
 * the automaton constructed by the composition.
 *
 * @param {int} id - the id of the composition
 * @param {automaton} net1 - the first automaton
 * @param {automaton} net2 - the second automaton
 * @return {automaton} - the automaton formed by the composition
 */
function automataParallelComposition(id, automaton1, automaton2){
	const automaton = new Automaton(id);
	const walker1 = new AutomatonWalker(automaton1);
	const walker2 = new AutomatonWalker(automaton2);
	const stateMap = constructStates(automaton1.nodes, automaton2.nodes);

	const alphabet1 = automaton1.alphabet;
	const alphabet2 = automaton2.alphabet;

	const synced = {};
	const unsynced = {};

	const actions = Object.keys(alphabet1).concat(Object.keys(alphabet2));
	for(let i = 0; i < actions.length; i++){
		const action = actions[i];

		if(action === TAU || action === DELTA){
			unsynced[action] = true;
		}
		else if(alphabet1[action] !== undefined && alphabet2[action] !== undefined){
			synced[action] = true;
		}
		else{
			unsynced[action] = true;
		}
	}

	const edges = automaton1.edges.concat(automaton2.edges);
	
	for(let action in unsynced){
		const unsyncedEdges = edges.filter(e => e.label === action);
		for(let i = 0; i < unsyncedEdges.length; i++){
			const edge = unsyncedEdges[i];
			const from = Object.keys(stateMap[edge.from]).map(id => automaton.getNode(id));
			const to = Object.keys(stateMap[edge.to]).map(id => automaton.getNode(id));
			for(let j = 0; j < from.length; j++){
				const newEdge = automaton.addEdge(automaton.nextEdgeId, action, from[j], to[j]);
				newEdge.locations = edge.locations;
				newEdge.metaData.originId = edge.id;
			}
		} 
	}

	for(let action in synced){
		const syncedEdges1 = automaton1.edges.filter(e => e.label === action);
		const syncedEdges2 = automaton2.edges.filter(e => e.label === action);

		for(let i = 0; i < syncedEdges1.length; i++){
			const edge1 = syncedEdges1[i];

			for(let j = 0; j < syncedEdges2.length; j++){
				const edge2 = syncedEdges2[j];

				const fromId = edge1.from + '||' + edge2.from;
				const toId = edge1.to + '||' + edge2.to;

				const newEdge = automaton.addEdge(automaton.nextEdgeId, action, automaton.getNode(fromId), automaton.getNode(toId));
				newEdge.locations = locationUnion(edge1.locations, edge2.locations);

				// check if the current first edge is a broadcaster (only once)
				if(j === 0 && edge1.metaData.broadcaster){
					const receivers = syncedEdges2.filter(e => e.metaData.receiver);
					processBroadcasting(edge1, receivers, fromId);
				}

				// check if the current second edge is a broadcaster
				if(edge2.metaData.broadcaster){
					const receivers = syncedEdges1.filter(e => e.metaData.receiver);
					processBroadcasting(edge2, receivers, fromId);
				}
			}
		}
	}

	automaton.trim();
	return automaton;

	function constructStates(nodes1, nodes2){
		const stateMap = {};

		for(let i = 0; i < nodes1.length; i++){
			const node1 = nodes1[i];

			stateMap[node1.id] = {};

			for(let j = 0; j < nodes2.length; j++){
				const node2 = nodes2[j];

				if(stateMap[node2.id] === undefined){
					stateMap[node2.id] = {};
				}

				const node = automaton.addNode(node1.id + '||' + node2.id);
				stateMap[node1.id][node.id] = true;
				stateMap[node2.id][node.id] = true;

				if(node1.metaData.startNode && node2.metaData.startNode){
					node.metaData.startNode = true;
					automaton.root = node.id;
				}

				node.locations = locationUnion(node1.locations, node2.locations);
			}
		}

		return stateMap;
	}

	function processBroadcasting(broadcastEdge, receiverEdges, fromId){
		for(let i = 0; i < receiverEdges.length; i++){
			const receiver = receiverEdges[i];
			const from = Object.keys(stateMap[receiver.from]).map(id => automaton.getNode(id));
			const to = Object.keys(stateMap[receiver.to]).map(id => automaton.getNode(id));
			for(let j = 0; j < from.length; j++){
				if(from[j].id !== fromId){
					const newEdge = automaton.addEdge(automaton.nextEdgeId, receiver.label, from[j], to[j]);
					newEdge.locations = receiver.locations;
				}
			}
		}
	}

	function locationUnion(loc1, loc2){
		for(var x in loc1){
			loc2[x] = true;
		}

		return loc2;
	}
}