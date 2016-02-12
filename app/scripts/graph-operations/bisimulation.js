'use strict';

/**
 * Constructs and returns a simplifed version of the specified graph by performing a 
 * bisimulation colouring of the graph an merging the nodes that are of the same color.
 *
 * @param {!object} graph - the graph to be simplified
 * @returns {!object} - a simplified version of the graph
 */
function simplification(graph) {
  // perform the bisimulation
  var result = _bisimulation([graph]);
  return result.graphs[0];
}

/**
 * Determines if the specified graphs are equivalent by performing a bisimulation colouring
 * for each graph and comparing the colours of the root of each graph. If the roots from each graph
 * all have the same colour then all the specified graphs are equivalent.
 *
 * @param {!array} graphs - an array of graphs to check for equivalency
 * @returns {!boolean} - true if all graphs are equivalent, otherwise false.
 */
function isEquivalent(graphs) {
  // perform the bisimulataion
  var result = _bisimulation(graphs);

  // compare the colors of each root node
  var coloredNodes = result.coloredNodes;
  for(var i = 0; i < graphs.length - 1; i++){
    var rootId1 = graphs[i].rootId;
    var rootId2 = graphs[i + 1].rootId;
      
    // if root nodes do not match the graphs are not equivalent
    if(coloredNodes[rootId1].color !== coloredNodes[rootId2].color){
      return false;
    }
  }

  // if this point is reached then all graphs were equivalent
  return true;
}

/**
 * Performs a bisimulation coloring on the specified graphs, which gives the nodes in each graph a colouring
 * based on transitions it makes to neighbouring nodes. Once the colouring is completed any nodes
 * within the same graph with the same colour are considered equivalent and are merged together.
 *
 * @private
 * @param {!array} - an array of graphs to perform bisimulation colouring on.
 * @returns {!object} - the coloured nodes and the simplified graphs
 */
function _bisimulation(graphs) {
  var clones = [];
  for(let i in graphs){
    clones.push(graphs[i].deepClone());
  }

  // construct map of nodes and give them all the same color
  var coloredNodes = [];
  for(let i in clones){
    var clone = clones[i];
    var nodes = clone.nodes;
    for(let n in nodes){
      var node = nodes[n];
      coloredNodes[node.id] = new Graph.ColoredNode(node);
    }
  }

  // continue process until color map does not increase in size
  var previousLength = -1;
  var colorMap = []
  while(previousLength < colorMap.length){
    previousLength = colorMap.length;
    colorMap = _constructColoring(coloredNodes);
    coloredNodes = _applyColoring(coloredNodes, colorMap);
  }

  // merge nodes together that have the same colors
  for(let i in clones){
    var clone = clones[i];
    var nodes = clone.nodes;
     
    for(let i in colorMap){
      var nodeIds = [];
        
      for(let k in nodes){
        var node = coloredNodes[nodes[k].id];
        if(node.color === i){
          nodeIds.push(node.node.id);
        }
      }
      if(nodeIds.length > 1){
        clone.mergeNodes(nodeIds);
      }
    }
  }

  // remove duplicate edges
  for(let i in clones){
    clones[i].removeDuplicateEdges();
  }

  return {coloredNodes: coloredNodes, graphs: clones};
}

/**
 * Helper function for the bisimulation function which constructs and returns
 * a color map for the specified colored nodes.
 *
 * @param {!Array} coloredNodes - The nodes to construct a color map for
 * @returns {!Array} A color map to color the specified nodes with
 */
function _constructColoring(coloredNodes){
  var colorMap = [];
  // get coloring for each node in the graph
  for(let n in coloredNodes){
    var node = coloredNodes[n];
    var coloring = node.constructNodeColoring(coloredNodes);

    // only add coloring if it is not a duplicate
    var equals = false;
    for(let c in colorMap){
      equals = colorMap[c].equals(coloring.coloring);
      if(equals){
        break;
      }
    }
    if(!equals){
      colorMap.push(coloring);
    }
  }

  return colorMap;
}

/**
 * Helper function for the bisimulation function which applies a coloring to
 * the specified colored nodes based on the specified color map.
 *
 * @param {!Array} coloredNodes - Array of colored nodes
 * @param {!Array} colorMap - map of colors
 * @returns {!Array} The new coloring of the colored nodes
 */
function _applyColoring(coloredNodes, colorMap) {
  var newColors = []
  // get new color for each node in the graph
  for(let n in coloredNodes){
    var node = coloredNodes[n];

    // work out new color for the current node
    var coloring = node.constructNodeColoring(coloredNodes);
    for(let c in colorMap){
      if(colorMap[c].equals(coloring.coloring)){
        newColors[n] = c;
        break;
      }
    }
  }

  // apply new color to each node
  for(let i in newColors){
    coloredNodes[i].color = newColors[i];
  }

  return coloredNodes;
}