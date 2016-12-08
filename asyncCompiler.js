//Worker scripts run in a completely different context, so lets import all the required scripts.
importScripts("app/scripts/compiler/lexer.js");
importScripts("app/scripts/compiler/parser.js");
importScripts("app/scripts/compiler/expander.js");
importScripts("app/scripts/compiler/analyser.js");
importScripts("app/scripts/compiler/referenceReplacer.js");
importScripts("app/scripts/compiler/interpreter.js");
importScripts("app/scripts/compiler/automatonInterpreter.js");
importScripts("app/scripts/compiler/petrinetInterpreter.js");
importScripts("app/scripts/compiler/operation-evaluator.js");
importScripts("app/scripts/compiler/reconstructor.js");
importScripts("app/scripts/compiler/compiler.js");
<!-- the wrapper scripts for the process operations -->
importScripts("app/scripts/process-operations/abstraction.js");
importScripts("app/scripts/process-operations/bisimulation.js");
importScripts("app/scripts/process-operations/parallel-composition.js");
importScripts("app/scripts/process-operations/automata-to-petrinet.js");

<!-- automata process operations -->
importScripts("app/scripts/process-operations/automata/automaton-parallel-composition.js");
importScripts("app/scripts/process-operations/automata/automaton-abstraction.js");
importScripts("app/scripts/process-operations/automata/automaton-bisimulation.js");

<!-- petri net process operations -->
importScripts("app/scripts/process-operations/petri-net/petri-net-parallel-composition.js");
importScripts("app/scripts/process-operations/petri-net/petri-net-abstraction.js");
importScripts("app/scripts/process-operations/petri-net/petri-net-bisimulation.js");
importScripts("app/scripts/constants.js");
importScripts("app/scripts/helper-functions.js");
importScripts("app/scripts/index-iterator.js");
importScripts("app/scripts/process-models/petrinet.js");
importScripts("app/scripts/process-models/walkers/petrinet-walker.js");
importScripts("app/scripts/process-operations/token-rule.js");
importScripts("app/scripts/process-models/automaton.js");
importScripts("app/scripts/process-models/walkers/automaton-walker.js");
importScripts("app/scripts/process-operations/abstraction.js");
importScripts("app/scripts/process-operations/bisimulation.js");
importScripts("app/scripts/process-operations/parallel-composition.js");
onmessage = function (e) {

  //Node appears to handle exceptions differently. Lets catch them and pass them back instead of killing the app.
  try {
    postMessage({result:Compiler.localCompile(e.data.ast, e.data.context)});
  } catch (ex) {
    postMessage({result:{type: 'error', message: ex.toString(), stack: ex.stack}});
  }
  terminate();
}
