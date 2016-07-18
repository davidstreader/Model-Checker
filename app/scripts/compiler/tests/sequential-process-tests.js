'use strict';

describe('Sequential Petri Net Interpretation Tests', function(){

	it('Shound interpret petrinet A = (STOP). correctly', function(){
		var places = generatePlaces(1, 0);
	});

	it('Should interpret petrinet A = (a -> STOP). correctly', function(){
		// construct expected petri net
		var places = generatePlaces(2, 0);
		var transitions = generateTransitions(['a'], 0);
		constructSequentialPetriNet(places, transitions);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 2, transitionMap, {}, 1, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> STOP).');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret petrinet A = (a -> b -> c -> STOP). correctly', function(){
		// construct expected petri net
		var places = generatePlaces(4, 0);
		var transitions = generateTransitions(['a', 'b', 'c'], 0);
		constructSequentialPetriNet(places, transitions);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 4, transitionMap, {}, 3, ['test.0']);
		
		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> b -> c -> STOP).');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret petrinet A = (a -> b -> c -> d -> e -> STOP). correctly', function(){
		// construct expected petri net
		var places = generatePlaces(6, 0);
		var transitions = generateTransitions(['a', 'b', 'c', 'd', 'e'], 0);
		constructSequentialPetriNet(places, transitions);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 6, transitionMap, {}, 5, ['test.0']);
		
		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> b -> c -> d -> e -> STOP).');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

});