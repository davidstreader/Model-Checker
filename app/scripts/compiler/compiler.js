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
      if (e.data.ast) {
          app.socket.emit('compile',{ast:e.data.ast,context:context},function(results) {
              if (results == undefined) return;
              if (results.type == "error") {
                  app.showError(results);
                  return;
              }
              app.finalizeBuild(results);
          });
      } else if (e.data.message) {
        if (e.data.clear) app.$.console.clear();
        app.$.console.log(e.data.message);
      } else if (e.data.error) {
        app.showError(e.data);
      }
    };
  },
}
