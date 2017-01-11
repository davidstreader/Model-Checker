'use strict';

function petriNetBisimulation(nets){
	const colourMap = {};
	for(let i = 0; i < nets.length; i++){
		colourPetriNet(nets[i]);
	}

	if(nets.length === 1){
		return nets[0];
	}

	function colourPetriNet(net){
		const walker = new PetriNetWalker(net);
		const markings = walker.findMarkings();
		const markingColours = {}; // marking -> current colour

		// apply the base colour to all the markings
		for(let i = 0; i < markings.length; i++){
			const marking = markings[i];
			const key = walker.markingKey(marking);
			markingColours[key] = 0;
		}

		let colourCount = 0; // the total number of colours constructed
		let lastColourCount = 0; // the total number of colours constructed in the last iteration
		let colourAmount = 0 // the number of colours constructed in the last iteration
		let markingMap; // colour -> [marking]
		while(lastColourCount <= colourCount){
			markingMap = {};

			// construct a colouring for each marking
			for(let i = 0; i < markings.length; i++){
				const marking = markings[i];

				// construct the colouring for this marking
				const colouring = constructColouring(walker, marking, markingColours);
			
				// check if this colouring has been constructed before
				const key = JSON.stringify(colouring);
				if(colourMap[key] === undefined){
					colourMap[key] = colourCount++;
				}

				const colour = colourMap[key];

				// add marking to the the marking map
				if(markingMap[colour] === undefined){
					markingMap[colour] = [];
				}
				markingMap[colour].push(marking);
			}

			// recolour the markings
			for(let colour in markingMap){
				const colouredMarkings = markingMap[colour];
				for(let i = 0; i < colouredMarkings.length; i++){
					const marking = colouredMarkings[i];
					const key = walker.markingKey(marking);
					markingColours[key] = colour;
				}
			}

			// check if any new colours were constructed during this iteration
			if(colourCount - lastColourCount === colourAmount){
				break;
			}

			// otherwise continue iterating
			colourAmount = colourCount - lastColourCount;
			lastColourCount = colourCount;		
		}

		// remove any unnecessary transitions from the petri net
		const toDelete = {};
		for(let colour in markingMap){
			const markings = markingMap[colour];
			
			// check if this marking has a unique colouring
			if(markings.length < 2){
				continue;
			}

			performCrossProduct(net, markings, toDelete);
		}

		for(let id in toDelete){
			net.removePlace(id);
			delete toDelete[id];
		}

		removeRedundantTransitions(net, toDelete);

		for(let id in toDelete){
			net.removeTransition(id);
		}
	}

	function constructColouring(walker, marking, markingColours){
		const colouring = [];
		const incoming = markingColours[walker.markingKey(marking)];

		const transitions = walker.getOutgoingTransitions(marking);
		for(let i = 0; i < transitions.length; i++){
			const transition = transitions[i];
			const nextMarking = walker.executeTransition(transition, marking);
			if(nextMarking === undefined){
				continue;
			}

			const outgoing = markingColours[walker.markingKey(nextMarking)];
			colouring.push(incoming + '-|' + transition.label + '|-' + outgoing);
		}

		return colouring.sort();
	}

	function PrePostCondition(transition){
		var incoming = transition.incomingPlaces.sort();
		var pre = {};
		for(var i = 0; i < incoming.length; i++){
			pre[incoming[i]] = true;
		}

		var outgoing = transition.outgoingPlaces.sort();
		var post = {};
		for(var i = 0; i < outgoing.length; i++){
			post[outgoing[i]] = true;
		}

		return {pre:pre, post:post};
	}

	function performCrossProduct(net, markings, toDelete){
		for(let i = 0; i < markings.length; i++){
			const marking = markings[i];
			for(let j = i + 1; j < markings.length; j++){
				const marking2 = markings[j];

				for(let id1 in marking){
					const place1 = net.getPlace(id1);
					for(let id2 in marking2){
						const place2 = net.getPlace(id2);

						net.combinePlaces(place1, place2);
						toDelete[id2] = 'place';
					}

					toDelete[id1] = 'place';
				}
			}
		}
	}

	function removeRedundantTransitions(net, toDelete){
		for(let label in net.labelSets){
			const transitions = net.labelSets[label];

			for(let i = 0; i < transitions.length; i++){
				const transition = transitions[i];
				let isSubSet = true;
				for(let j = 0; j < transitions.length; j++){
					if(i === j){
						continue;
					}

					if(!transitions[j].isSuperSetOf(transition)){
						isSubSet = false;
						break;
					}
				}

				// if the transition is a subset of all the other transitions it can be deleted
				if(isSubSet){
					toDelete[transition.id] = 'transition';
				}	
			}
		}
	}
}