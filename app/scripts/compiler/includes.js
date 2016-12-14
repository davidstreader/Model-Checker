//Worker scripts run in a completely different context, so lets import all the required scripts.
importScripts("lexer.js");
importScripts("parser.js");
importScripts("expander.js");
importScripts("analyser.js");
importScripts("referenceReplacer.js");
importScripts("interpreter.js");
importScripts("automatonInterpreter.js");
importScripts("petrinetInterpreter.js");
importScripts("operation-evaluator.js");
importScripts("reconstructor.js");
importScripts("compiler.js");
<!-- the wrapper scripts for the process operations -->
importScripts("../process-operations/abstraction.js");
importScripts("../process-operations/bisimulation.js");
importScripts("../process-operations/parallel-composition.js");
importScripts("../process-operations/automata-to-petrinet.js");

<!-- automata process operations -->
importScripts("../process-operations/automata/automaton-parallel-composition.js");
importScripts("../process-operations/automata/automaton-abstraction.js");
importScripts("../process-operations/automata/automaton-bisimulation.js");

<!-- petri net process operations -->
importScripts("../process-operations/petri-net/petri-net-parallel-composition.js");
importScripts("../process-operations/petri-net/petri-net-abstraction.js");
importScripts("../process-operations/petri-net/petri-net-bisimulation.js");
importScripts("../constants.js");
importScripts("../helper-functions.js");
importScripts("../index-iterator.js");
importScripts("../process-models/petrinet.js");
importScripts("../process-models/walkers/petrinet-walker.js");
importScripts("../process-operations/token-rule.js");
importScripts("../process-models/automaton.js");
importScripts("../process-models/walkers/automaton-walker.js");
importScripts("../process-operations/abstraction.js");
importScripts("../process-operations/bisimulation.js");
importScripts("../process-operations/parallel-composition.js");
