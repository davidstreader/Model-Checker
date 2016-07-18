'use strict';

/**
 * Unit tests to ensure that the breadth first traversal algorithm works correctly.
 */
 describe('Breadth First Traversal Tests', function(){

 	it('Two empty BFT Graphs should be considered identical', function(){
 		var graph1 = new BFTGraph(0);
 		var graph2 = new BFTGraph(1);
 		var result = compareBFTGraphs(graph1, graph2);
 		assert(result, 'both processes should be identical');
 	});

 	it('Two identical sequential BFT Graphs should be considered identical (1)', function(){
 		var graph1 = new BFTGraph(0);
 		var graph2 = new BFTGraph(1);
 		var node1 = graph1.addNode('a0', 'a');
 		var node2 = graph2.addNode('a0', 'a');
 		graph1.root.addChild(node1.id);
 		graph2.root.addChild(node2.id);
 		var result = compareBFTGraphs(graph1, graph2);
 		assert(result, 'both processes should be identical');
 	});

 	it('Two identical sequential BFT Graphs should be considered identical (2)', function(){
 		var graphs = [new BFTGraph(0), new BFTGraph(1)];
 		var actions = ['a', 'b', 'c'];
 		for(var i = 0; i < graphs.length; i++){
 			for(var j = 0; j < actions.length; j++){
 				var node = graphs[i].addNode(actions[j] + 0, actions[j]);
 				if(j === 0){
 					graphs[i].root.addChild(node.id);
 				}
 				else{
 					graphs[i].getNode(actions[j - 1] + 0).addChild(node.id);
 				}
 			}
 		}

 		var result = compareBFTGraphs(graphs[0], graphs[1]);
 		assert(result, 'both processes should be identical');
 	});

 	it('Two identical sequential BFT Graphs should be considered identical (3)', function(){
 		var graphs = [new BFTGraph(0), new BFTGraph(1)];
 		var actions = ['a', 'b', 'c', 'd', 'e'];
 		for(var i = 0; i < graphs.length; i++){
 			for(var j = 0; j < actions.length; j++){
 				var node = graphs[i].addNode(actions[j] + 0, actions[j]);
 				if(j === 0){
 					graphs[i].root.addChild(node.id);
 				}
 				else{
 					graphs[i].getNode(actions[j - 1] + 0).addChild(node.id);
 				}
 			}
 		}

 		var result = compareBFTGraphs(graphs[0], graphs[1]);
 		assert(result, 'both processes should be identical');
 	});

 	it('Two not identical sequential BFT Graphs should not be considered identical (1)', function(){
 		var graph1 = new BFTGraph(0);
 		var graph2 = new BFTGraph(1);
 		var node1 = graph1.addNode('a0', 'a');
 		var node2 = graph2.addNode('b0', 'b');
 		graph1.root.addChild(node1.id);
 		graph2.root.addChild(node2.id);
 		var result = compareBFTGraphs(graph1, graph2);
 		assert(!result, 'both processes should not be identical');
 	});

 	it('Two not identical sequential BFT Graphs should not be considered identical (2)', function(){
 		var graph1 = new BFTGraph(0);
 		var graph2 = new BFTGraph(1);
 		var actions1 = ['a', 'b', 'c'];
 		var actions2 = ['x', 'y', 'z'];
 		for(var i = 0; i < actions1.length; i++){
 			var node1 = graph1.addNode(actions1[i] + 0, actions1[i]);
 			var node2 = graph2.addNode(actions2[i] + 0, actions2[i]);
 			if(i === 0){
 				graph1.root.addChild(node1.id);
 				graph2.root.addChild(node2.id);
 			}
 			else{
 				graph1.getNode(actions1[i - 1] + 0).addChild(node1.id);
 				graph2.getNode(actions2[i - 1] + 0).addChild(node2.id);
 			}
 		}

 		var result = compareBFTGraphs(graph1, graph2);
 		assert(!result, 'both processes should not be identical');
 	});

 	it('Two almost identical sequential BFT Graphs should not be considered identical', function(){
 		var graph1 = new BFTGraph(0);
 		var graph2 = new BFTGraph(1);
 		var actions1 = ['a', 'b', 'c'];
 		var actions2 = ['a', 'b', 'z'];
 		for(var i = 0; i < actions1.length; i++){
 			var node1 = graph1.addNode(actions1[i] + 0, actions1[i]);
 			var node2 = graph2.addNode(actions2[i] + 0, actions2[i]);
 			if(i === 0){
 				graph1.root.addChild(node1.id);
 				graph2.root.addChild(node2.id);
 			}
 			else{
 				graph1.getNode(actions1[i - 1] + 0).addChild(node1.id);
 				graph2.getNode(actions2[i - 1] + 0).addChild(node2.id);
 			}
 		}

 		var result = compareBFTGraphs(graph1, graph2);
 		assert(!result, 'both processes should not be identical');
 	});

 	it('Two identical choice BFT Graphs should be considered identical (1)', function(){
 		var graphs = [new BFTGraph(0), new BFTGraph(1)];
 		for(var i = 0; i < graphs.length; i++){
 			var node1 = graphs[i].addNode('a0', 'a');
 			var node2 = graphs[i].addNode('b0', 'b');
 			graphs[i].root.addChild(node1.id);
 			graphs[i].root.addChild(node2.id);
 		}

 		var result = compareBFTGraphs(graphs[0], graphs[1]);
 		assert(result, 'both processes should be identical');
 	});

 	it('Two identical choice BFT Graphs should be considered identical (2)', function(){
 		var graph1 = new BFTGraph(0);
 		var node1 = graph1.addNode('a0', 'a');
 		var node2 = graph1.addNode('b0', 'b');
 		graph1.root.addChild(node1.id);
 		graph1.root.addChild(node2.id);

 		var graph2 = new BFTGraph(1);
 		node1 = graph2.addNode('b0', 'b');
 		node2 = graph2.addNode('a0', 'a');
 		graph2.root.addChild(node1.id);
 		graph2.root.addChild(node2.id);

 		var result = compareBFTGraphs(graph1, graph2);
 		assert(result, 'both processes should be identical');
 	});

 	it('Two identical choice BFT Graphs should be considered identical (2)', function(){
 		var graphs = [new BFTGraph(0), new BFTGraph(1)];
 		var actions = [['a', 'b', 'c'], ['x', 'y', 'z']];
 		for(var i = 0; i < graphs.length; i++){
 			for(var j = 0; j < actions[0].length; j++){
 			var node1 = graphs[i].addNode(actions[0][j] + 0, actions[0][j]);
 			var node2 = graphs[i].addNode(actions[1][j] + 0, actions[1][j]);
 			if(j === 0){
 				graphs[i].root.addChild(node1.id);
 				graphs[i].root.addChild(node2.id);
 			}
 			else{
 				graphs[i].getNode(actions[0][j - 1] + 0).addChild(node1.id);
 				graphs[i].getNode(actions[1][j - 1] + 0).addChild(node2.id);
 			} 				
 			}
 		}

 		var result = compareBFTGraphs(graphs[0], graphs[1]);
 		assert(result, 'both processes should be identical');
 	});

 	it('Two almost identical choice BFT Graphs should not be considered identical (1)', function(){
 		var graph1 = new BFTGraph(0);
 		var node1 = graph1.addNode('a0', 'a');
 		var node2 = graph1.addNode('b0', 'b');
 		graph1.root.addChild(node1.id);
 		graph1.root.addChild(node2.id);

 		var graph2 = new BFTGraph(1);
 		node1 = graph2.addNode('a0', 'a');
 		node2 = graph2.addNode('c0', 'c');
 		graph2.root.addChild(node1.id);
 		graph2.root.addChild(node2.id);

 		var result = compareBFTGraphs(graph1, graph2);
 		assert(!result, 'both processes should be identical');
 	});

 });