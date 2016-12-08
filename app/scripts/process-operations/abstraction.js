'use strict';

function abstraction(process, isFair, prune){
	// check if fair abstraction has been declared
	isFair = (isFair !== undefined) ? isFair : true;
	prune = (prune !== undefined) ? prune : false;

	var type = process.type;
	if(type === 'automata'){
		return automataAbstraction(process, isFair, prune);
	}
	else if(type === 'petrinet'){
		return petriNetAbstraction(process, isFair, prune);
	}
	else{
		// throw error
	}
}