'use strict';

/**
 * Represents an edge within an automaton.
 */

var TAU = 'tau';
var DELTA = 'delta';

/**
 * Constructs and returns a new edge object.
 *
 * @param {int} id - the unique identifier for this edge
 * @param {node} from - the node this edge transitions from
 * @param {node} to - the node this edge transitions to
 * @param {string} label - the action label given to this edge
 */
function Edge(id, from, to, label){
	var edge = {};

	// fields
	edge.id = id;
	edge.from = from;
	edge.to = to;
	edge.label = label;

	/**
	 * Returns true if this edge is hidden, otherwise
	 * returns false.
	 *
	 * @return {boolean} - whether this edge is hidden or not
	 */
	edge.isHidden = function(){
		return edge.label === TAU;
	}

	/**
	 * Returns true if this edge is deadlocked, otherwise
	 * returns false.
	 *
	 * @return {boolean} - whether this edge is deadlocked or not
	 */
	edge.isDeadlocked = function(){
		return edge.label === DELTA;
	}

	return edge;
}