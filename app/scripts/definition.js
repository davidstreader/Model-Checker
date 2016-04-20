'use strict';

/**
 * Represents the definition of an automaton.
 *
 * @param {array|undefined} relabel - a set of relabelling actions
 * @param {array|undefined} hidden - a set of hidden actions
 * @param {boolean|undefined} isVisible - whether or not this automaton is visible 
 */
function Definition(relabel, hidden, isVisible){
	// fields
	this.graph = undefined;
	this.label = undefined;
	this.relabel = relabel;
	this.hidden = hidden;
	this.isVisible = isVisible;
	this.unprocessedNodes = [];
	this._rootId = undefined;
	this.variableMap = {};
	this.referenceMap = {};

	// define the getter and setter for rootId
	Object.defineProperty(this, 'rootId', {

		/**
		 * Returns the graph's root id. If the graph has not been
		 * defined it will return the root id it will use.
		 *
		 * @returns {number} - the root id
		 */
		get: function(){
			// if there is a graph and it has a root
			if(this.graph && this.graph.root){
				return this.graph.root.id;
			}

			// otherwise return the current value of rootId
			return this._rootId;
		},

		/**
		 * Sets the graph's root id to the specified id. If the
		 * graph has not been defined yet it will set the root id
		 * to be what it will be.
		 */
		set: function(id){
			// if there is a graph and it has a root
			if(this.graph && this.graph.root){
				this.graph.root.id = id;
				return this.graph.root.id;
			}

			// otherwise set to root id
			this._rootId = id;
			return this._rootId;
		}
	});
}