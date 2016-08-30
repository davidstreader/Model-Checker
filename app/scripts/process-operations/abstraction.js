'use strict';

function abstraction(process, isFair){
	// check if fair abstraction has been declared
	isFair = (isFair !== undefined) ? isFair : true;
	var type = process.type;
	if(type === 'automata'){
		return automataAbstraction(process, isFair);
	}
	else if(type === 'petrinet'){
		return petriNetAbstraction(process, isFair);
	}
	else{
		// throw error
	}
}