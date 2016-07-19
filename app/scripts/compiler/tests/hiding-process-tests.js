'use strict';

describe('Grammer to Data Structure Hiding Tests', function(){

	it('Should interpret \'petrinet A = (a -> STOP)\\{a}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(2, 0);
		var transitions = generateTransitions([TAU], 0);
		constructSequentialPetriNet(places, transitions);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 2, transitionMap, {}, 1, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> STOP)\\{a}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = (a -> STOP)\\{a, b, c}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(2, 0);
		var transitions = generateTransitions([TAU], 0);
		constructSequentialPetriNet(places, transitions);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 2, transitionMap, {}, 1, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> STOP)\\{a, b, c}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = (a -> STOP)\\{b}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(2, 0);
		var transitions = generateTransitions(['a'], 0);
		constructSequentialPetriNet(places, transitions);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 2, transitionMap, {}, 1, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> STOP)\\{b}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = (x -> STOP)\\{a, b, c}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(2, 0);
		var transitions = generateTransitions([TAU], 0);
		constructSequentialPetriNet(places, transitions);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 2, transitionMap, {}, 1, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> STOP)\\{a, b, c}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = (a -> b -> c -> STOP)\\{a}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(4, 0);
		var transitions = generateTransitions([TAU, 'b', 'c'], 0);
		constructSequentialPetriNet(places, transitions);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 4, transitionMap, {}, 3, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> b -> c -> STOP)\\{a}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = (a -> b -> c -> STOP)\\{a , b}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(4, 0);
		var transitions = generateTransitions([TAU, TAU, 'c'], 0);
		constructSequentialPetriNet(places, transitions);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 4, transitionMap, {}, 3, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> b -> c -> STOP)\\{a, b}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = (a -> b -> c -> STOP)\\{a, b, c}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(4, 0);
		var transitions = generateTransitions([TAU, TAU, TAU], 0);
		constructSequentialPetriNet(places, transitions);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 4, transitionMap, {}, 3, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> b -> c -> STOP)\\{a, b, c}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = (a -> b -> c -> STOP)\\{a, b}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(4, 0);
		var transitions = generateTransitions([TAU, TAU, 'c'], 0);
		constructSequentialPetriNet(places, transitions);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 4, transitionMap, {}, 3, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> b -> c -> STOP)\\{a, b}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = (a -> b -> c -> STOP)\\{a, c}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(4, 0);
		var transitions = generateTransitions([TAU, 'b', TAU], 0);
		constructSequentialPetriNet(places, transitions);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 4, transitionMap, {}, 3, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> b -> c -> STOP)\\{a, c}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = (a -> b -> c -> STOP)\\{b, c}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(4, 0);
		var transitions = generateTransitions(['a', TAU, TAU], 0);
		constructSequentialPetriNet(places, transitions);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 4, transitionMap, {}, 3, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = (a -> b -> c -> STOP)\\{b, c}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = ([0..2] -> STOP)\\{[0]}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(4, 0);
		var transitions = generateTransitions([TAU, '[1]', '[2]'], 0);
		constructSequentialPetriNet([places[0], places[1]], [transitions[0]]);
		constructSequentialPetriNet([places[0], places[2]], [transitions[1]]);
		constructSequentialPetriNet([places[0], places[3]], [transitions[2]]);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 4, transitionMap, {}, 3, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = ([0..2] -> STOP)\\{[0]}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = ([0..2] -> STOP)\\{[1]}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(4, 0);
		var transitions = generateTransitions(['[0]', TAU, '[2]'], 0);
		constructSequentialPetriNet([places[0], places[1]], [transitions[0]]);
		constructSequentialPetriNet([places[0], places[2]], [transitions[1]]);
		constructSequentialPetriNet([places[0], places[3]], [transitions[2]]);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 4, transitionMap, {}, 3, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = ([0..2] -> STOP)\\{[1]}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = ([0..2] -> STOP)\\{[2]}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(4, 0);
		var transitions = generateTransitions(['[0]', '[1]', TAU], 0);
		constructSequentialPetriNet([places[0], places[1]], [transitions[0]]);
		constructSequentialPetriNet([places[0], places[2]], [transitions[1]]);
		constructSequentialPetriNet([places[0], places[3]], [transitions[2]]);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 4, transitionMap, {}, 3, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = ([0..2] -> STOP)\\{[2]}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = ([0..2] -> STOP)\\{[0], [1]}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(4, 0);
		var transitions = generateTransitions([TAU, TAU, '[2]'], 0);
		constructSequentialPetriNet([places[0], places[1]], [transitions[0]]);
		constructSequentialPetriNet([places[0], places[2]], [transitions[1]]);
		constructSequentialPetriNet([places[0], places[3]], [transitions[2]]);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 4, transitionMap, {}, 3, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = ([0..2] -> STOP)\\{[0], [1]}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = ([0..2] -> STOP)\\{[0], [2]}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(4, 0);
		var transitions = generateTransitions([TAU, '[1]', TAU], 0);
		constructSequentialPetriNet([places[0], places[1]], [transitions[0]]);
		constructSequentialPetriNet([places[0], places[2]], [transitions[1]]);
		constructSequentialPetriNet([places[0], places[3]], [transitions[2]]);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 4, transitionMap, {}, 3, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = ([0..2] -> STOP)\\{[0], [2]}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = ([0..2] -> STOP)\\{[1], [2]}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(4, 0);
		var transitions = generateTransitions(['[0]', TAU, TAU], 0);
		constructSequentialPetriNet([places[0], places[1]], [transitions[0]]);
		constructSequentialPetriNet([places[0], places[2]], [transitions[1]]);
		constructSequentialPetriNet([places[0], places[3]], [transitions[2]]);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 4, transitionMap, {}, 3, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = ([0..2] -> STOP)\\{[1], [2]}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = ([0..2] -> STOP)\\{[0], [1], [2]}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(4, 0);
		var transitions = generateTransitions([TAU, TAU, TAU], 0);
		constructSequentialPetriNet([places[0], places[1]], [transitions[0]]);
		constructSequentialPetriNet([places[0], places[2]], [transitions[1]]);
		constructSequentialPetriNet([places[0], places[3]], [transitions[2]]);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 4, transitionMap, {}, 3, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = ([0..2] -> STOP)\\{[0], [1], [2]}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

	it('Should interpret \'petrinet A = ([0..2] -> STOP)\\{[0..2]}.\' correctly', function(){
		// construct the expected petri net
		var places = generatePlaces(4, 0);
		var transitions = generateTransitions([TAU, TAU, TAU], 0);
		constructSequentialPetriNet([places[0], places[1]], [transitions[0]]);
		constructSequentialPetriNet([places[0], places[2]], [transitions[1]]);
		constructSequentialPetriNet([places[0], places[3]], [transitions[2]]);
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 4, transitionMap, {}, 3, ['test.0']);

		// construct received petri net
		var processesMap = constructProcessesMap('petrinet A = ([0..2] -> STOP)\\{[0..2]}.');

		// compare the received petri net with the expected petri net
		var id = 0;
		var graph1 = breadthFirstTraversal(id++, expected);
		var graph2 = breadthFirstTraversal(id, processesMap['A']);
		var result = compareBFTGraphs(graph1, graph2);
		assert(result, 'processes should be identical');
	});

})