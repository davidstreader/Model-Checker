'use strict';

function abstraction(process, isFair){
	// check if fair abstraction has been declared
	isFair = (isFair !== undefined) ? isFair : true;
	var type = process.type;
	if(type === 'automata'){
		return automataAbstraction(process);
	}
	else if(type === 'petrinet'){
		return petriNetAbstraction(process);
	}
	else{
		// throw error
	}

	/**
	 * Performs the abstraction process on the specified automaton. This process
	 * removes the hidden, unobservable events (represented as tau) in the automaton
	 * and replaces them with observable events.
	 *
	 * @param {automaton} process - the automaton to abstract
	 * @return {automaton} - the abstracted processs
	 */
	function automataAbstraction(process){
		// find all the hidden paths in the graph
		var paths = [];
		var visited = {};
		
		// descend through the automaton finding tau paths
		var fringe = [{ start:undefined, node:process.root }];
		while(fringe.length !== 0){
			var current = fringe.pop();

			// check if this node has been visited
			if(visited[current.node.id] !== undefined){
				// check if there is a path
				if(current.start !== undefined){
					paths.push({ start:current.start, end:current.start });
				}

				continue;
			}

			// push the next nodes to the fringe
			var edges = current.node.edgesFromMe;
			for(var i = 0; i < edges.length; i++){
				var to = process.getNode(edges[i].to);
				// check if the current edge represents a hidden event
				if(edges[i].label === TAU){
					// either continue an existing path or start a new one
					var start = (current.start !== undefined) ? current.start : current.node;
					fringe.push({ start:start, node:to });
				}
				// check if the current path has completed
				else if(edges[i].label !== TAU && current.start !== undefined){
					fringe.push({ start:undefined, node:to });

					// check if the path is a duplicate
					var match = false;
					for(var j = 0; j < paths.length; j++){
						if(paths[j].start === current.start && paths[j].end === current.node){
							match = true;
							break;
						}
					}

					if(!match){
						paths.push({ start:current.start, end:current.node });
					}
				}
				// push next node to the fringe
				else{
					fringe.push({ start:undefined, node:to });
				}

				visited[current.node.id] = true;
			}
		}

		// add the observable edges
		for(var i = 0; i < paths.length; i++){
			//check if this tau path is a loop
			if(paths[i].start.id === paths[i].end.id){
				// add a deadlocked state if this abstraction is not fair
				if(!isFair){
					var deadlock = process.addNode(process.nextNodeId);
					deadlock.addMetaData('isTerminal', 'error');
					process.addEdge(process.nextEdgeId, DELTA, paths[i].end.id, deadlock.id);
				}

				continue;
			}

			// add observable edges from nodes that transition to the start of the path
			var incoming = paths[i].start.edgesToMe;
			for(var j = 0; j < incoming.length; j++){
				var from = process.getNode(incoming[j].from);
				process.addEdge(process.nextEdgeId, incoming[j].label, from.id, paths[i].end.id);
			}

			// add observable edges to the nodes that transition from the start of the path
			var outgoing = paths[i].end.edgesFromMe;
			for(var j = 0; j < outgoing.length; j++){
				var to = process.getNode(outgoing[j].to);
				process.addEdge(process.nextEdgeId, outgoing[j].label, paths[i].start.id, to.id);
			}
		}

		// remove hidden edges from the automaton
		var edges = process.edges;
		var nodes = [];
		for(var i = 0; i < edges.length; i++){
			if(edges[i].label === TAU){
				// remove references to this edge
				var from = process.getNode(edges[i].from);
				from.deleteEdgeFromMe(edges[i]);
				var to = process.getNode(edges[i].to);
				to.deleteEdgeToMe(edges[i]);

				process.removeEdge(edges[i].id);

				// make from a terminal if there are no edges from it
				if(from.isTerminal){
					from.addMetaData('isTerminal', 'stop');
				}

				// delete to if it is now unreachable
				if(to.isUnreachable){
					nodes.push(to.id);
				}
			}
		}

		// remove unreachable nodes from the automaton
		for(var i = 0; i < nodes.length; i++){
			process.removeNode(nodes[i]);
		}

		return process;
	}

	function petriNetAbstraction(process){
		var paths = [];
		var visited = {};

		var fringe = [];
		var roots = process.roots;
		for(var i = 0; i < roots.length; i++){
			fringe.push({ start:undefined, place:roots[i] });
		}

		while(fringe.length !== 0){
			var current = fringe.pop();

			// check if this place has been visited
			if(visited[current.place.id] !== undefined){
				continue;
			}

			// push the next places to the fringe
			var transitions = getTransitions(process, current.place.outgoingTransitions);
			for(var i = 0; i < transitions.length; i++){
				var places = transitions[i].outgoingPlaces;
				// check if the current transition represents a hidden event
				if(transitions[i].label === TAU){
					for(var j = 0; j < places.length; j ++){
						// either continue an existing path or start a new one
						var start = (current.start !== undefined) ? current.start : current.place;
						fringe.push({ start:start, place:places[i] });
					}
				}
				// check if the current path has completed
				else if(transitions[i].label !== TAU && current.start !== undefined){
					for(var j = 0; j < places.length; j++){
						fringe.push({ start:undefined, place:places[i] });
						paths.push({ start:current.start, end:current.place });
					}
				}
				// push the next places to the fringe
				else{
					for(var j = 0; j < places.length; j++){
						fringe.push({ start:undefined, place:places[j] });
					}
				}
			}
		}

		// add observable actions
		for(var i = 0; i < paths.length; i++){
			// add observable actions from transitions that lead to the start place
			var incoming = getTransitions(process, paths[i].start.incomingTransitions);
			for(var j = 0; j < incoming.length; j++){
				var id = process.nextTransitionId;
				process.addTransition(id, incoming[j].label, incoming[j].incomingPlaces, [paths[i].end]);
			}

			// add observable actions from the start place to outgoing transitions from the end place
			var outgoing = getTransitions(process, paths[i].end.outgoingTransitions);
			for(var j = 0; j < outgoing.length; j++){
				var id = process.nextTransitionId;
				process.addTransition(id, outgoing[j].label, [paths[i].start], outgoing[j].outgoingPlaces);
			}
		}

		// remove hidden transitions
		var transitions = process.transitions;
		var places = [];
		for(var i = 0; i < transitions.length; i++){
			if(transitions[i].label === TAU){
				// remove references to this transition
				var incoming = transitions[i].incomingPlaces;
				for(var j = 0; j < incoming.length; j++){
					incoming[j].deleteOutgoingTransitions(transitions[i].id);
					// delete this place if it is now unreachable
					if(incoming[j].isUnreachable){
						places.push(incoing[j].id);
					}
				}

				var outgoing  = transitions[i].outgoingPlaces;
				for(var j = 0; j < outgoing.length; j++){
					outgoing[j].deleteIncomingTransitions(transitions[i].id);
					// make this place a terminal if there are no transitions from it
					if(outgoing[j].isTerminal){
						outgoing[j].addMetaData('isTerminal', 'stop');
					}
				}

				process.removeTransition(transitions[i].id);
			}
		}

		// remove unreachable places from the petri net
		for(var i = 0; i < places.length; i++){
			process.removePlace(places[i]);
		}

		return process;

		function getTransitions(process, ids){
			var transitions = [];
			for(var i = 0; i < ids.length; i++){
				transitions[i] = process.getTransition(ids[i]);
			}

			return transitions;
		}

	}
}