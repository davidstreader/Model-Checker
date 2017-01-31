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
      }
    };
  },

  //We still need to do remote compilation sync, but its not like that's a problem since sockets are async by nature
  remoteCompile: function(ast, context) {
    app.socket.emit('compile',{ast:ast,context:context},function(results) {
      console.log(results);
      app.finalizeBuild(results);
    });
  }
}
