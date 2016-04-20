'use strict';

function Graph(){
	var graph = {};

	// fields
	graph.root = new Node(0);
	graph.nodes = {};
	graph.nodes[graph.root.id] = graph.root;
	graph.edges = {};
	graph.nodeCount = 1;
	graph.edgeCount = 0;

	graph.addNode = function(){
		var node = new Node(graph.nodeCount++);
		graph.nodes[node.id] = node;
		return node;
	}

	graph.addEdge = function(from, to, label){
		var edge = new Edge(graph.edgeCount++, from, to, label);
		graph.edges[edge.id] = edge;
		return edge;
	}

	return graph;
}