'use strict';

const AUTOMATON_WALKER = {
	get root(){
		return this.automaton.root;
	},

	getIncomingNodes: function(node){
		const edgeNodes = [];

		const edges = node.incomingEdges.map(id => this.automaton.getEdge(id));
		
		for(let i = 0; i < edges.length; i++){
			const edge = edges[i];
			const nextNode = this.automaton.getNode(edge.from);
			edgeNodes.push(new this.EdgeNode(edge, nextNode));
		}

		return edgeNodes;
	},

	getOutgoingNodes: function(node){
		const edgeNodes = [];

		const edges = node.outgoingEdges.map(id => this.automaton.getEdge(id));
		
		for(let i = 0; i < edges.length; i++){
			const edge = edges[i];
			const nextNode = this.automaton.getNode(edge.to);
			edgeNodes.push(new this.EdgeNode(edge, nextNode));
		}

		return edgeNodes;
	},

	getIncomingEdges: function(node){
		return node.incomingEdges.map(id => this.automaton.getEdge(id));
	},

	getOutgoingEdges: function(node){
		return node.outgoingEdges.map(id => this.automaton.getEdges(id));
	},

	EdgeNode: function(edge, node){
		this.edge = edge;
		this.node = node;
	}
};

function AutomatonWalker(automaton){
	// check that an automata has been received
	if(automaton.type !== 'automata'){
		const message = 'Expecting an automata but received a ' + automaton.type;
		throw new PetriNetWalkerException(message);
	}

	this.automaton = automaton;
	Object.setPrototypeOf(this, AUTOMATON_WALKER);
}

function AutomatonWalkerException(message){
	this.message = message;
	this.toString = function(){
		return 'AutomatonWalkerException: ' + this.message;
	}
}