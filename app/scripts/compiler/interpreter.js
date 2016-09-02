'use strict';

var processesMap;
var variableMap;
var nextProcessId;

function interpret(processes, variables, analysis, lastProcessesMap, isFairAbstraction){
	reset();
	variableMap = variables;

	// build up a set of id numbers that cannot be used
	var usedIds = {};
	for(var ident in lastProcessesMap){
		if(analysis[ident] !== undefined && !analysis[ident].isUpdated){
			usedIds[lastProcessesMap[ident].id] = true;
		}
	}

	for(var i = 0; i < processes.length; i++){
		// check if the current process has been updated since last compilation
		var ident = processes[i].ident.ident;
		if(analysis[ident] !== undefined && analysis[ident].isUpdated){
			// interpret the process
			var type = processes[i].processType;
			if(type === 'automata'){
				interpretAutomaton(processes[i], processesMap, variableMap, getNextProcessId(), isFairAbstraction);
			}
			else if(type === 'petrinet'){
				interpretPetriNet(processes[i], processesMap, variableMap, getNextProcessId(), isFairAbstraction);
			}
			else{
				// throw error
			}
		}
		else{
			// use the last interpretation
			processesMap[ident] = lastProcessesMap[ident];
		}
	}

	return processesMap;

	/**
	 * Returns the next unique process id
	 *
	 * @return {int} - the next process id
	 */
	function getNextProcessId(){
		while(usedIds[nextProcessId] !== undefined){
			nextProcessId++;
		}

		usedIds[nextProcessId] = true;
		return nextProcessId;
	}

	function reset(){
		processesMap = {};
		variableMap = {};
		nextProcessId = 0;
	}
}

function interpretOneOff(ident, process, processType, processes, variables){
	reset();

	var process = new ProcessNode(ident, process, processType);
	process = expand({ processes:[process], variableMap:variables });

	if(processType === 'automata'){
		interpretAutomaton(process.processes[0], processes, variableMap, ident, true);
	}
	else if(processType === 'petrinet'){
		interpretPetriNet(process.processes[0], processes, variableMap, ident, true);
	}
	else{
		// throw error
	}

	return processes[ident];

	function ProcessNode(ident, process, type){
		var node = {
			type:'process',
			processType:type,
			ident:{
				type:'identifier',
				ident:ident
			},
			process:process,
			local:[]
		};

		return node;
	}

	function reset(){
		processesMap = {};
		variableMap = {};
		nextProcessId = 0;
	}
}