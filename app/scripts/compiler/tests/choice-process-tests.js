'use strict';

describe('Choice Petri Net Interpretation Tests', function(){
	
	it('Should interpret \'petrinet A = (a -> STOP | b -> STOP).\' correctly', function(){
		// construct expected petri net
		var places = generatePlaces(3, 0);
		var transitions = generateTransitions(['a', 'b'], 0);
		setupConnection(places[0], transitions[0]);
		setupConnection(places[0], transitions[1]);
		setupConnection(transitions[0], places[1]);
		setupConnection(transitions[1], places[2]);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 3, transitionMap, {}, 2, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> STOP | b -> STOP).');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = (a -> b -> c -> STOP | x -> y -> z -> STOP).\' correctly', function(){
		// construct expected petri net
		var places = generatePlaces(1, 0);
		var places1 = generatePlaces(3, 1);
		var transitions1 = generateTransitions(['a', 'b', 'c'], 0);
		var places2 = generatePlaces(3, 4); 
		var transitions2 = generateTransitions(['x', 'y', 'z'], 3);
		constructSequentialPetriNet(places.concat(places1), transitions1);
		constructSequentialPetriNet(places.concat(places2), transitions2);
		var placeMap = convertArrayToMap(places.concat(places1, places2));
		var transitionMap = convertArrayToMap(transitions1.concat(transitions2));
		var expected = new PetriNet('test', placeMap, 7, transitionMap, {}, 6, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> b -> c -> STOP | x -> y -> z -> STOP).');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = (a -> STOP | x -> y -> z -> STOP).\' correctly', function(){
		// construct expected petri net
		var places = generatePlaces(1, 0);
		var places1 = generatePlaces(1, 1);
		var transitions1 = generateTransitions(['a'], 0);
		var places2 = generatePlaces(3, 2); 
		var transitions2 = generateTransitions(['x', 'y', 'z'], 1);
		constructSequentialPetriNet(places.concat(places1), transitions1);
		constructSequentialPetriNet(places.concat(places2), transitions2);
		var placeMap = convertArrayToMap(places.concat(places1, places2));
		var transitionMap = convertArrayToMap(transitions1.concat(transitions2));
		var expected = new PetriNet('test', placeMap, 5, transitionMap, {}, 4, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> STOP | x -> y -> z -> STOP).');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = (a -> b -> c -> STOP | x -> STOP).\' correctly', function(){
		// construct expected petri net
		var places = generatePlaces(1, 0);
		var places1 = generatePlaces(3, 1);
		var transitions1 = generateTransitions(['a', 'b', 'c'], 0);
		var places2 = generatePlaces(1, 4); 
		var transitions2 = generateTransitions(['x'], 3);
		constructSequentialPetriNet(places.concat(places1), transitions1);
		constructSequentialPetriNet(places.concat(places2), transitions2);
		var placeMap = convertArrayToMap(places.concat(places1, places2));
		var transitionMap = convertArrayToMap(transitions1.concat(transitions2));
		var expected = new PetriNet('test', placeMap, 5, transitionMap, {}, 4, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> b -> c -> STOP | x -> STOP).');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = (a -> (b -> STOP | c -> STOP)).\' correctly', function(){
		// construct expected petri net
		var places = generatePlaces(4, 0);
		var transitions = generateTransitions(['a', 'b', 'c'], 0);
		setupConnection(places[1], transitions[1]);
		setupConnection(transitions[1], places[2]);
		setupConnection(places[1], transitions[2]);
		setupConnection(transitions[2], places[3]);
		constructSequentialPetriNet(places.slice(0,2), transitions.slice(0,1));

		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 4, transitionMap, {}, 3, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> (b -> STOP | c -> STOP)).');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

});