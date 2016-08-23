'use strict';

function automataBisimulation(process){
	var nodeMap = colourNodes(process.nodes);
	var lastColourCount = 0;
	var colourCount = 1;

	var nodeColours;
	while(lastColourCount <= colourCount){
		var colourMap = {};
		var visited = {};
		nodeColours = {};
		nodeColours[0] = []; // for terminals
		var index = 0;
		colourCount = 1;
		var fringe = [process.root];
			
		while(index < fringe.length){
			var current = fringe[index++];

			// check if the current node has already been visited
			if(visited[current.id] !== undefined){
				continue;
			}
			visited[current.id] = true;

			// check if the current node is a terminal
			if(current.getMetaData('isTerminal') !== undefined){
				nodeColours[0].push(current);
				continue;
			}

			var colouring = constructColouring(current, nodeMap);

			// check if this colour already exists
			var colourId = -1;
			for(var id in colourMap){
				var result = compareColourings(colouring, colourMap[id]);
				if(result){
					colourId = id;
					break;
				}
			}

			if(colourId === -1){
				colourId = colourCount++;
				colourMap[colourId] = colouring;
				nodeColours[colourId] = [];
			}

			nodeColours[colourId].push(current);

			var edges = current.edgesFromMe;
			for(var i = 0; i < edges.length; i++){
				if(visited[edges[i].to] === undefined){
					fringe.push(process.getNode(edges[i].to));
				}
			}
		}

		// apply the colourings to the nodes
		for(var colour in nodeColours){
			var nodes = nodeColours[colour];
			for(var i = 0; i < nodes.length; i++){
				nodeMap[nodes[i].id].colour =  colour;
			}
		}

		// break if no new colours were added
		if(lastColourCount === colourCount){
			break;
		}

		lastColourCount = colourCount;
	}

	// merge nodes with the same colour
	for(var colour in nodeColours){
		if(nodeColours[colour].length > 1){
			process.mergeNodes(nodeColours[colour]);
		}
	}

	process.removeDuplicateEdges();
	return process;

	function colourNodes(nodes){
		var nodeMap = {};
		for(var i = 0; i < nodes.length; i++){
			nodeMap[nodes[i].id] = { node:nodes[i], colour:0 };
		}

		return nodeMap;
	}

	function constructColouring(node, nodeMap){
		var colouring = {};
		var edges = node.edgesFromMe;
		var from = nodeMap[node.id].colour
		for(var i = 0; i < edges.length; i++){
			var to = nodeMap[edges[i].to].colour;
			var colour = from + ' -' + edges[i].label + '> ' + to;
			colouring[colour] = true;
		}


		return colouring;
	}

	function compareColourings(colouring1, colouring2){
		for(var key in colouring1){
			if(colouring2[key] === undefined){
				return false;
			}
		}

		for(var key in colouring2){
			if(colouring1[key] === undefined){
				return false;
			}
		}

		return true;
	}
}