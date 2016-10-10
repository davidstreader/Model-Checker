'use strict';

function petriNetBisimulation(processes){
	var placeMap = colourPlaces(processes);
	var lastColourCount = 0;
	var colourCount = 1;

	var placeColours;
	while(lastColourCount <= colourCount){
		var colourMap = {};
		var visited = {};
		placeColours = {};
		placeColours[0] = []; // for terminals
		colourCount = 1;

		for(var p = 0; p < processes.length; p++){
			var process = processes[p];
			var fringe = process.roots;
			var index = 0;

			while(index < fringe.length){
				var current = fringe[index++];

				// check if the current place has already been visited
				if(visited[current.id] !== undefined){
					continue;
				}
				visited[current.id] = true;

				// check if the current place is a terminal
				if(current.getMetaData('isTerminal') !== undefined){
					placeColours[0].push(current);
				}

				var colouring = constructColouring(current, placeMap);

				// check if this colour already exists
				var colourId = -1;
				for(var id in colourMap){
					var result = compareColourings(colouring, colourMap[id]);
					if(result){
						colourId = id;
						break;
					}
				}

				if(colourId === -1){
					colourId = colourCount++;
					colourMap[colourId] = colouring;
					placeColours[colourId] = [];
				}

				placeColours[colourId].push(current);

				var transitions = current.outgoingTransitions.map(id => process.getTransition(id));
				for(var i = 0; i < transitions.length; i++){
					var outgoing = transitions[i].outgoingPlaces;
					for(var j = 0; j < outgoing.length; j++){
						fringe.push(outgoing[j]);
					}
				}
			}
		}

		// apply the colourings to the places
		for(var colour in placeColours){
			var places = placeColours[colour];
			for(var i = 0; i < places.length; i++){
				placeMap[places[i].id].colour = colour;
			}
		}

		// break if no new colours were added
		if(lastColourCount === colourCount){
			break;
		}

		lastColourCount = colourCount;
	}

	// check if this is a simplification or bisimular equivalence
	if(processes.length === 1){
		// merge places with the same colour
		for(var colour in placeColours){
			if(placeColours[colour].length > 1){
				var places = placeColours[colour];
				var place = places[0];
				for(var i = 1; i < places.length; i++){
					place = process.combinePlaces(place, places[i]);
					process.removePlace(places[i].id);
				}
				process.removePlace(places[0].id);
			}
		}

		// merge transitions that have the same entry and exit places
		var labelSets = process.labelSets;
		for(var label in labelSets){
			var conditions = {};
			var transitions = labelSets[label];
			for(var i = 0; i < transitions.length; i++){
				var condition = new PrePostCondition(transitions[i]);
				var key = JSON.stringify(condition);
				if(conditions[key] === undefined){
					conditions[key] = [];
				}
				conditions[key].push(transitions[i]);
			}

			for(var key in conditions){
				if(conditions[key].length > 1){
					var condition = JSON.parse(key);
					var incoming = [];
					for(var id in condition.pre){
						incoming.push(process.getPlace(id));
					}

					var outgoing = [];
					for(var id in condition.post){
						outgoing.push(process.getPlace(id));
					}

					process.addTransition(process.nextTransitionId, label, incoming, outgoing);

					for(var i = 0; i < conditions[key].length; i++){
						process.removeTransition(conditions[key][i].id);
					}
				}
			}
		}

		return process;
	}
	else{
		// TODO
	}

	function colourPlaces(processes){
		var placeMap = {};
		for(var j = 0; j < processes.length; j++){
			var places = processes[j].places;
			for(var i = 0; i < places.length; i++){
				placeMap[places[i].id] = { place: places[i], colour: 0 };
			}
		}

		return placeMap;
	}

	function constructColouring(place, placeMap){
		var colouring = {};
		var transitions = place.outgoingTransitions.map(id => process.getTransition(id));
		var from = placeMap[place.id].colour;
		for(var i = 0; i < transitions.length; i++){
			var colour = from + ' -> |' + transitions[i].label + '|';
			colouring[colour] = true;
		}

		return colouring;
	}

	function compareColourings(colouring1, colouring2){
		for(var key in colouring1){
			if(colouring2[key] === undefined){
				return false;
			}
		}

		for(var key in colouring2){
			if(colouring1[key] === undefined){
				return false;
			}
		}

		return true;
	}

	function PrePostCondition(transition){
		var incoming = transition.incomingPlaces.map(p => p.id).sort();
		var pre = {};
		for(var i = 0; i < incoming.length; i++){
			pre[incoming[i]] = true;
		}

		var outgoing = transition.outgoingPlaces.map(p => p.id).sort();
		var post = {};
		for(var i = 0; i < outgoing.length; i++){
			post[outgoing[i]] = true;
		}

		return {pre:pre, post:post};
	}
}