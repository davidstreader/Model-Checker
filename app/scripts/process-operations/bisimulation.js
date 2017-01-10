'use strict';

function bisimulation(){
	if(arguments.length === 0){
		return;
	}

	var type = arguments[0].type;
	if(type === 'automata'){
		return automataBisimulation(arguments);
	}
	else if(type === 'petrinet'){
		return petriNetBisimulation(arguments);
	}
	else{
		// throw error
	}
}

function areBisimular(processes){
	return automataBisimulation(processes);
}