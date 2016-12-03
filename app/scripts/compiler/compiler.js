'use strict';

const Compiler = {
	lastAst: {},
	lastAnalysis: {},
	lastProcesses: {},
	lastAbstraction: true,

	compile: function(code, context){
		try{
			const tokens = Lexer.tokenise(code);
			const ast = parse(tokens);
			// check if this is to be compiled client side or server side
			if(context.isClientSide){
				return this.localCompile(ast, context);
			} else if (context.isLocal) {
        return this.localCompile(ast, context);
      } else{
				return this.remoteCompile(ast, context);
			}

		}catch(error){
			error.type = 'error';
			return error;
		}
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

	remoteCompile: function(ast, context){
    app.socket.emit('compile',{ast:ast,context:context},function(results) {
      if (results.type === 'error') {
        if (results.stack) {
          app.$.console.error("An exception was thrown that was not related to your script.");
          app.$.console.error(results.stack);
          throw results;
        } else {
          app.$.console.error(results.message);
        }
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
		return {type: "serverSide"};
	}
}
