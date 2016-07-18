'use strict';

/**
 * A graph data structure representing the breadth first traversal of a process. Used
 * to determine if two processes are equivalent.
 */
class BFTGraph {
	
	/**
	 * Constructs an empty breadth first traversal graph.
	 */
	constructor(id){
		this._id = id;
		this._rootId = id + '.<root>';
		this._nodeMap = {};
		this._nodeCount = 0;
		this.addNode(this._id + '<root>', '<root>');
	}

	/**
	 * Returns the root node of this breadth first traversal graph.
	 *
	 * @return {node} - the root node
	 */
	get root(){
		return this._nodeMap[this._id + '<root>'];
	}

	/**
	 * Adds and returns a new node with the specified id and label to this 
	 * breadth first traversal graph.
	 *
	 * @param {string} id - the id
	 * @param {string} label - the label
	 * @param {node} - the constructed node
	 */
	addNode(id, label){
		if(this._nodeMap[id] !== undefined){
			// throw error
		}

		var node = new BFTGraph.Node(id, label);
		this._nodeMap[id] = node;
		this._nodeCount++;
		return node;
	}

	/**
	 * Returns the node with the specified id from this breadth first traversal
	 * graph.
	 *
	 * @param {string} id - the node id
	 * @return {node} - the node with the specified id
	 */
	getNode(id){
		if(this._nodeMap[id] !== undefined){
			return this._nodeMap[id];
		}

		// throw error
	}

	/**
	 * Returns the number of nodes in this breadth first traversal graph.
	 *
	 * @return {int} - node count
	 */
	get nodeCount(){
		return this._nodeCount;
	}

	/**
	 * Returns the alphabet of actions from this breadth first traversal graph.
	 *
	 * @return {string{}} - set of actions
	 */
	get alphabet(){
		var alphabet = {};
		for(var id in this._nodeMap){
			var label = this._nodeMap[id].label;
			alphabet[label] = true;
		}

		return alphabet;
	}
}

/**
 * Represents a node in a breadth first traversal graph.
 */
BFTGraph.Node = class {
	
	/**
	 * Constructs a new node with the specified id and label.
	 *
	 * @param{string} id - the id
	 * @param{string} label - the label
	 */
	constructor(id, label){
		this._id = id;
		this._label = label;
		this._children = [];
	}

	/**
	 * Returns the id associated with this node.
	 *
	 * @return {string} - the node id
	 */
	get id(){
		return this._id;
	}

	/**
	 * Returns the label associated with this node.
	 *
	 * @return {string} - the node label
	 */
	get label(){
		return this._label;
	}

	/**
	 * Returns an array of the node ids that this node can
	 * traverse to.
	 *
	 * @return {node[]} - array of node ids
	 */
	get children(){
		return this._children;
	}

	/**
	 * Adds the specified node id to the array of node ids that
	 * this node can traverse to.
	 *
	 * @param {string} id - the node id
	 */
	addChild(id){
		this._children.push(id);
		this._children.sort();
	}
}