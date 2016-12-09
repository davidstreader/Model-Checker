'use strict';

function petriNetBisimulation(processes){
	let automaton = tokenRule(processes[0], 'toAutomaton');
	automaton = bisimulation(automaton);
	return automatonToPetriNet(automaton);
}