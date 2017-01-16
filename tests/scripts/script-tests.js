const fs = require('fs');
const vm = require("vm");
const assert = require('chai').assert;
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
fs.writeFileSync("tests/results.txt","");
fs.readdirSync("tests").forEach(result => testScript("tests/"+result));

function testScript(script) {
  if (script.endsWith("results.txt") || !script.endsWith("txt") || script.endsWith("js")) return;
  const code = fs.readFileSync(script, 'utf-8');
  let compile = Compiler.compileWithoutWorker(code, {isLocal: true, isFairAbstraction: true});

  describe(script, function () {
    //Fail.txt should not compile successfully
    if (script.endsWith("fail.txt")) {
      failTxt(compile);
      return;
    }
    it('The script should compile successfully', function () {
      assert(compile.type !== 'error', "Error compiling, Message: " + compile.message);
    });
    const operations = compile.operations;
    if (operations && operations.length > 0) {
      for (let i = 0; i < operations.length; i++) {
        const {operation, process1, process2, result} = operations[i];
        const op = process1 + ' ' + operation + ' ' + process2;
        if (result == "notfound") {
          let msg = "Error processing operation, "+process1.ident + ' ' + operation + ' ' + process2.ident;
          if (!process1.exists) msg+="\n"+process1.ident+" does not exist!";
          if (!process2.exists) msg+="\n"+process2.ident+" does not exist!";
          if (script.endsWith("nonExistantOperation.txt")) {
            continue;
          }
          it("Error processing operation, "+process1.ident + ' ' + operation + ' ' + process2.ident, function () {
            assert(process1.exists, process1.ident+" does not exist");
            assert(process2.exists, process2.ident+" does not exist");
          });
          continue;
        }
        if (script.endsWith("nonExistantOperation.txt")) {
          it('The script should not compile successfully', function () {
            assert(compile.type ==='error', "nonExistantOperations.txt compiled successfully");
          });
        }
        if (script.endsWith("failOperations.txt")) {
          failOperationsTxt(result, op);
          continue;
        }
        it("Operation '"+op+"' should pass", function () {
          assert(result, "'"+op+"' failed");
        });
      }
    }
  })
}
function failOperationsTxt(result, op) {
  if (op == "A ~ B") {
    it('The script should not pass', function () {
      assert(!result, "'"+op+"' passed");
    });
    return;
  }
  it('The script should pass', function () {
    assert(result, "'"+op+"' failed");
  });
}
function failTxt(compile) {
  it('The script should not compile successfully', function () {
    assert(compile.type ==='error', "fail.txt compiled successfully");
  });
}
function combineEdges(edge1,edge2) {
  const java = require("java");
  const baseDir = "lib";
  const dependencies = fs.readdirSync(baseDir);
  //Load java dependancies
  dependencies.forEach(function(dependency){
    java.classpath.push(baseDir + "/" + dependency);
  });
  //Initilize a solver
  const EdgeMerger = java.import('net.modelsolver.EdgeMerger');
  //Solve

  return JSON.parse(new EdgeMerger().mergeEdgesSync(JSON.stringify(edge1),JSON.stringify(edge2)));
}
