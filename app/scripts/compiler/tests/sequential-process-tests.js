'use strict';

describe('Sequential Petri Net Interpretation Tests', function(){

	it('Shound interpret petrinet A = (STOP). correctly', function(){
		var places = generatePlaces(1, 0);
	});

	it('Should interpret petrinet A = (a -> STOP). correctly', function(){
		var places = generatePlaces(2, 0);
		var transitions = generateTransitions(['a'], 0);
		constructSequentialPetriNet(places, transitions);
		
		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 2, transitionMap, {}, 1, ['test.0']);
	});

	it('Should interpret petrinet A = (a -> b -> c -> STOP). correctly', function(){
		var places = generatePlaces(4, 0);
		var transitions = generateTransitions(['a', 'b', 'c'], 0);
		constructSequentialPetriNet(places, transitions);

		var placeMap = convertArrayToMap(places);
		var transitionMap = convertArrayToMap(transitions);
		var expected = new PetriNet('test', placeMap, 4, transitionMap, {}, 3, ['test.0']);
	});

});