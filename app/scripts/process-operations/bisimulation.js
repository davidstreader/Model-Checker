'use strict';

function bisimulation(process){
	var type = process.type;
	if(type === 'automata'){
		return automataBisimulation([process]);
	}
	else if(type === 'petrinet'){
		return petriNetBisimulation([process]);
	}
	else{
		// throw error
	}
}

function areBisimular(processes){
	return automataBisimulation(processes);
}