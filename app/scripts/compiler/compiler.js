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
				return this.clientSideCompile(ast, context);
			}
			else{
				return this.serverSideCompile(ast, context);
			}

		}catch(error){
			error.type = 'error';
			return error;
		}
	},

	clientSideCompile: function(ast, context){
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

		return { processes:processes, operations:operations };
	},

	serverSideCompile: function(ast, context){
		socket.emit('compile',{ast:ast,context:context},function(results) {
		  console.log(results);
      var graphs = [];
      for(var id in results.processes){
        graphs.push(results.processes[id]);
      }

      app.set('automata.values', graphs.reverse());
      if(results.operations.length !== 0){
        app.$.console.log('Operations:');
        for(var i = 0; i < results.operations.length; i++){
          app.$.console.log(results.operations[i]);
        }
      }
    });
		return {type: "serverSide"};
	}
}
