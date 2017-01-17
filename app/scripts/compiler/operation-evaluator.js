'use strict';

let results;
let opId;
let idents;

function evaluateOperations(operations, processesMap, variableMap){
	reset();

	for(let i = 0; i < operations.length; i++){
	  let op;
	  switch (operations[i].operation) {
      case "bisimular":
        op = "~";
        break;
      case "traceequivilant":
        op = "#";
        break;
    }
    if (operations[i].isNegated) op = "!"+op;
		let {process1, process2} = operations[i];
		const process1Err = !processesMap[getIdent(process1)];
    const process2Err = !processesMap[getIdent(process2)];

    if (process1Err || process2Err) {
      process1.ident = getIdent(process1);
      process1.exists = !process1Err;
      process2.ident = getIdent(process2);
      process2.exists = !process2Err;
      results.push({ operation:op, process1:process1, process2:process2, result:"notfound" });
		  continue;
    }
		const graph1 = interpretOneOff(generateProcessIdent(), process1, 'automata', processesMap, variableMap);
		const graph2 = interpretOneOff(generateProcessIdent(), process2, 'automata', processesMap, variableMap);
		let result;
		switch (operations[i].operation) {
      case "bisimular":
        result = areBisimular([graph1, graph2], 'automata');
        break;
      case "traceequivilant":
        result = areTraceEquivilant([graph1, graph2], 'automata');
        break;
    }
		if(operations[i].isNegated){
			result = !result;
		}

		process1 = reconstruct(process1);
		process2 = reconstruct(process2);

		results.push({ operation:op, process1:process1, process2:process2, result:result });
	}

	for(let i = 0; i < idents.length; i++){
		delete processesMap[idents[i]];
	}

	return results;

	function generateProcessIdent(){
		const ident = 'op' + opId++;
		idents.push(ident);
		return ident;
	}

	function reset(){
		results = [];
		opId = 0;
		idents = [];
	}

  function getIdent(process) {
	  if (process.type =="function") return getIdent(process.process);
	  return process.ident;
  }
}
