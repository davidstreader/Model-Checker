'use strict';

/**
 * Represents a node within an automaton.
 */

/**
 * Constructs and returns a new node object.
 *
 * @param {int} id - the unique identifier for this node
 * @param {string} label - a label for the node to display
 * @param {map} metaData - extra data regarding this node
 * @return {node} - the constructed node
 */
function Node(id){
	var node = {};

	// fields
	node.id = id;
	node.label = '' + id;
	node.metaData = {};
	node.fromEdges = {};
	node.toEdges = {};

	/**
	 * Returns the neighbouring nodes to this node.
	 * A neighbouring node is a node that is traversable to
	 * via a single transition from this node.
	 *
	 * @return {node[]} - array of neighbouring nodes
	 */
	node.getNeighbours = function(){
		var nodes = [];
		for(var i in node.edgesFromMe){
			nodes.push(node.edgesFromMe[i].to);
		}

		return nodes;
	}

	return node;
}