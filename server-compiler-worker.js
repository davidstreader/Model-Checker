const fs = require('fs');
const vm = require("vm");
const stringify = require('fast-stable-stringify');;
global.importScripts = (...files) => {
  let scripts;

  if (files.length > 0) {
    scripts = files.map(file => {
      //Essentially, we copy pasted from tiny-worker but changed it to use app/scripts/compiler as the source
      return fs.readFileSync("app/scripts/compiler/"+file, "utf8");
    }).join("\n");

    vm.createScript(scripts).runInThisContext();
  }
};
importScripts("includes.js");
onmessage = function (e) {
  //Node appears to handle exceptions differently. Lets catch them and pass them back instead of killing the app.
  try {
    const compile = Compiler.localCompile(e.data.ast, e.data.context);
    //There really is no point to storing everything twice
    for (let process in compile.analysis) {
      delete compile.analysis[process].process;
    }
    for (let process in compile.processes) {
      if (compile.processes[process].id.indexOf("*") != -1)
        delete compile.processes[process];
    }
    postMessage({clear:true,message:"Finished Compiling. Sending data to client"});
    postMessage({result:compile});
  } catch (ex) {
    postMessage({result:{type: 'error', message: ex.toString(), stack: ex.stack, location: ex.location}});
  }
  //Kill the worker as we start a new worker for each compilation
  terminate();
}
function combineEdges(edge1,edge2) {
  const java = require("java");
  const baseDir = "lib";
  const dependencies = fs.readdirSync(baseDir);

  dependencies.forEach(function(dependency){
    java.classpath.push(baseDir + "/" + dependency);
  });
  const Solver = java.import('net.modelsolver.Solver');
  const ret = JSON.parse(new Solver().solveSync(JSON.stringify({edge1:edge1,edge2:edge2})));
  console.log(ret);
  return ret;
}
