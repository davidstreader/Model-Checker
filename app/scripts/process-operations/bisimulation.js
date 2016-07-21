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

		return process;

		function constructColouredNodeMap(nodes){
			var nodeMap = {};
			for(var i = 0; i < nodes.length; i++){
				nodeMap[nodes[i].id] = { colour:0, node:nodes[i] };
			}

			return nodeMap;
		}

		function constructColourMap(){
			var colourMap = {};
			colourMap[0] = [constructColour(0, undefined, [])];
			return colourMap;
		}

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

		function constructColour(from, to, label){
			return { from:from, to:to, label:label };
		}

		function compareColours(colour1, colour2){
			if(colour1.length !== colour2.length){
				return false;
			}

			for(var i = 0; i < colour1.length; i++){
				/*if(colour1[i].from !== colour2[i].from){
					return false;
				}*/

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