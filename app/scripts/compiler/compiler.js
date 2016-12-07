'use strict';

const Compiler = {
  lastAst: {},
  lastAnalysis: {},
  lastProcesses: {},
  lastAbstraction: true,

  compile: function(code, context){
    if (!this.worker) {
      this.worker = new Worker("scripts/compiler/asyncCompiler.js")
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
        const graphs = [];
        for(let id in e.data.result.processes){
          const graph = e.data.result.processes[id];
          if (graph.type === 'automata') {
            graphs.push(AUTOMATON.convert(graph));
          } else if (graph.type === 'petrinet') {
            graphs.push(PETRI_NET.convert(graph));
          }

        }
        app.finalizeBuild(e.data.result, graphs);
      }
    };
  },

  localCompile: function(ast, context){
    ast = expand(ast);

    const abstractionChanged = context.isFairAbstraction !== this.lastAbstraction;
    const analysis = performAnalysis(ast.processes, this.lastAnalysis, abstractionChanged);
    ast.processes = replaceReferences(ast.processes);
    const processes = interpret(ast.processes, analysis, this.lastProcesses, context);
    const operations = evaluateOperations(ast.operations, processes, ast.variableMap);
    this.lastAst = ast;
    this.lastAnalysis = analysis;
    this.lastProcesses = processes;
    this.lastAbstraction = context.isFairAbstraction;

    return { processes:processes, operations:operations, analysis:analysis, context:context };
  },

  //We still need to do remote compilation sync, but its not like that's a problem since sockets are async by nature
  remoteCompile: function(ast, context){
    app.socket.off("analyse");
    app.socket.off("interpret");
    app.socket.on("analyse",data=>{app.$.console.clear();app.$.console.log("Analysing: "+data.ident+ " (" +(data.i+1) +"/"+ast.processes.length+")");});
    app.socket.on("interpret",data=>{app.$.console.clear();app.$.console.log("Interpreting:"+data.ident+ " (" +(data.i+1) +"/"+ast.processes.length+")");});
    app.socket.on("replacer",data=>{app.$.console.clear();app.$.console.log("Replacing References:"+data.ident+ " (" +(data.i+1) +"/"+ast.processes.length+")");});
    app.socket.on("expander",data=>{app.$.console.clear();app.$.console.log("Expanding:"+data.ident+ " (" +(data.i+1) +"/"+ast.processes.length+")");});
    app.socket.emit('compile',{ast:ast,context:context},function(results) {
      if (results.type === 'error') {
        app.finalizeBuild(results);
        return;
      }
      const graphs = [];
      for(let id in results.processes){
        const graph = results.processes[id];
        if (graph.type === 'automata') {
          graphs.push(AUTOMATON.convert(graph));
        } else if (graph.type === 'petrinet') {
          graphs.push(PETRI_NET.convert(graph));
        }

      }
      app.finalizeBuild(results,graphs);
    });
  }
}
