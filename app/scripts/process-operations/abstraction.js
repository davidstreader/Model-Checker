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
					paths.push({ start:current.start, end:current.node });
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
		// TODO
	}
}