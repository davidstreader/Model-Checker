'use strict';
const Compiler = {
  lastAst: {},
  lastAnalysis: {},
  lastProcesses: {},
  lastAbstraction: true,

  compile: function(code, context){
    if (!this.worker) {
      this.worker = new Worker("scripts/compiler/compiler-worker.js")
    }
    //Pass compilation on to the worker
    this.worker.postMessage({code:code,context:context});
    this.worker.onmessage = e => {
      if (e.data.remoteCompile) {
        this.remoteCompile(e.data.ast,context);
      } else if (e.data.message) {
        if (e.data.clear) app.$.console.clear();
        app.$.console.log(e.data.message);
      } else if (e.data.result) {
        app.finalizeBuild(e.data.result);
      }
    };
  },
  compileWithoutWorker: function (code, context) {
    try{
      const tokens = Lexer.tokenise(code);
      const ast = Parser.parse(tokens);
      return this.localCompile(ast, context);
    }catch(error){
      return {type:'error',message: error.toString(), stack: error.stack};
    }
  },
  localCompile: function(ast, context){
    ast = expand(ast);
    const abstractionChanged = context.isFairAbstraction !== this.lastAbstraction;
    const analysis = performAnalysis(ast.processes, this.lastAnalysis, abstractionChanged);
    ast.processes = hideVariables(ast.processes);
    ast.processes = replaceReferences(ast.processes);
    const processes = interpret(ast.processes, analysis, this.lastProcesses, context);
    const operations = evaluateOperations(ast.operations, processes, ast.variableMap);
    this.lastAst = ast;
    this.lastAnalysis = analysis;
    this.lastProcesses = processes;
    this.lastAbstraction = context.isFairAbstraction;
    const skipped = [];
    if (context.graphSettings) {
      for(let id in processes) {
        const graph = processes[id];
        graph.compiledAlphabet = graph.alphabet;
        if (graph.type === 'automata') {
          if (graph.dontRender) {
            delete graph.edgeMap;
            delete graph.nodeMap;
            skipped.push({
              id: graph.id,
              type: "user"
            });
            continue;
          }
          if (graph.nodeCount > context.graphSettings.autoMaxNode) {
            skipped.push({
              id: graph.id,
              length: graph.nodeCount,
              type: "nodes",
              maxLength: context.graphSettings.autoMaxNode
            });
            delete graph.edgeMap;
            delete graph.nodeMap;
          }
        } else if (graph.type === 'petrinet') {
          if (graph.dontRender) {
            delete graph.places;
            delete graph.transitions;
            skipped.push({
              id: graph.id,
              type: "user"
            });
            continue;
          }
          if (graph.placeCount > context.graphSettings.petriMaxPlace) {
            skipped.push({
              id: graph.id,
              length: graph.placeCount,
              type: "places",
              maxLength: context.graphSettings.petriMaxPlace
            });
            delete graph.places;
            delete graph.transitions;
          }
          if (graph.transitionCount > context.graphSettings.petriMaxTrans) {
            skipped.push({
              id: graph.id,
              length: graph.transitionCount,
              type: "transitions",
              maxLength: context.graphSettings.petriMaxTrans
            });
            delete graph.placeMap;
            delete graph.transitionMap;
          }
        }
      }
    }
    return { processes:processes, operations:operations, analysis:analysis, context:context, skipped:skipped  };
  },

  //We still need to do remote compilation sync, but its not like that's a problem since sockets are async by nature
  remoteCompile: function(ast, context) {
    app.socket.emit('compile',{ast:ast,context:context},function(results) {
      console.log(results);
      app.finalizeBuild(results);
    });
  }
}
