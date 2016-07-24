'use strict';

function bisimulation(process){
	var type = process.type;
	if(type === 'automata'){
		return automataBisimulation(process);
	}
	else if(type === 'petrinet'){
		return petriNetBisimulation(process);
	}
	else{
		// throw error
	}

	function automataBisimulation(process){
		// construct a coloured node map
		var nodeMap = constructColouredNodeMap(process.nodes);
		var colourMap = constructColourMap();
		var lastColourCount = 0;
		var colourCount = 1; // colour map is initialised with one colour
		
		while(lastColourCount <= colourCount){
			var fringe = process.terminals;
			var visited = {};
			var index = 0;
			
			while(index < fringe.length){
				var current = fringe[index++];
				// check if the current node has already been visited
				if(visited[current.id] !== undefined){
					continue;
				}
				
				if(current.getMetaData('isTerminal') === undefined){
					// construct colouring for the current node
					var colouring = constructColouring(current);

					// check if this colour already exists
					var colourId = -1;
					for(var id in colourMap){
						var result = compareColours(colouring, colourMap[id]);
						if(result){
							colourId = id;
							break;
						}
					}

					// add colour to colour map if necessary
					if(colourId !== -1){
						nodeMap[current.id].colour = colourId;
					}
					else{
						colourMap[colourCount] = colouring;
						nodeMap[current.id].colour = colourCount++; 
					}
				}

				// mark current node as visited
				visited[current.id] = true;

				var edges = current.edgesToMe;
				for(var i = 0; i < edges.length; i++){
					fringe.push(process.getNode(edges[i].from));
				}
			}

			// break if no new colours were added
			if(lastColourCount === colourCount){
				break;
			}

			lastColourCount = colourCount;
		}

		// group nodes by colour
		var colourGroups = {};
		for(var id in nodeMap){
			var colour = nodeMap[id].colour;
			if(colourGroups[colour] !== undefined){
				colourGroups[colour].push(nodeMap[id].node);
			}
			else{
				colourGroups[colour] = [nodeMap[id].node];
			}
		}

		// merge nodes with the same colour
		for(var colour in colourGroups){
			process.mergeNodes(colourGroups[colour]);
		}

		process.removeDuplicateEdges();
		return process;

		/**
		 * Constructs a colour map from the specified array of nodes. The mapping is
		 * from the node id to a coloured node object, which is an object containing a
		 * colour id and a node.
		 *
		 * @param {node[]} nodes - an array of nodes
		 * @return {id -> colouredNode} - a mapping from node id to coloured node
		 */
		function constructColouredNodeMap(nodes){
			var nodeMap = {};
			for(var i = 0; i < nodes.length; i++){
				nodeMap[nodes[i].id] = { colour:0, node:nodes[i] };
			}

			return nodeMap;
		}

		/**
		 * Constructs and returns an initial colour map for the
		 * bisimulation function.
		 *
		 * @return {id -> colour} - a colour map
		 */
		function constructColourMap(){
			var colourMap = {};
			colourMap[0] = [constructColour(0, undefined, [])];
			return colourMap;
		}

		/**
		 * Constructs and returns a colouring for the specified node. Does
		 * not consider duplicate edges in the colouring.
		 *
		 * @param {node} node - the node to colour
		 * @return {colour} - the node colouring
		 */
		function constructColouring(node){
			var colouring = [];
			var edges = node.edgesFromMe;
			for(var i = 0; i < edges.length; i++){
				var from = nodeMap[node.id].colour;
				var to = nodeMap[edges[i].to].colour;
				var colour = constructColour(from, to, edges[i].label);

				// check that the colouring does not contain the current colour
				var match = false;
				for(var j = 0; j < colouring.length; j++){
					if(colouring[j].to !== colour.to){
						continue;
					}

					if(colouring[j].label !== colour.label){
						continue;
					}

					match = true;
					break;
				}

				// only add colour if it is not already contained in the colouring
				if(!match){
					colouring.push(colour);
				}
			}

			return colouring;
		}

		/**
		 * Constructs and returns a colour object with the specified values.
		 *
		 * @param {string} from - id for the node where the edge starts
		 * @param {string} to - id for the node where the edge ends
		 * @param {string} label - the label for the edge
		 * @return {colour} - a colour object
		 */
		function constructColour(from, to, label){
			return { from:from, to:to, label:label };
		}

		/**
		 * Compares the specified colours together and determines if they are
		 * equivalent. Returns true if the colours match, otherwise returns false.
		 * This function does not take into account the id for the node where each
		 * colour starts.
		 *
		 * @param {colour} colour1 - the first colour to compare
		 * @param {colour} colour2 - the second colour to compare
		 * @return {boolean} - whether or not the colours match
		 */
		function compareColours(colour1, colour2){
			if(colour1.length !== colour2.length){
				return false;
			}

			for(var i = 0; i < colour1.length; i++){
				if(colour1[i].to !== colour2[i].to){
					return false;
				}

				if(colour1[i].label !== colour2[i].label){
					return false;
				}
			}

			return true;
		}
	}

	function petriNetBisimulation(process){
		// TODO
	}
}