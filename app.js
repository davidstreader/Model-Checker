var express = require('express');
var app = express();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var fs = require('fs');
var port = 5000;
var walk = function(dir, done) {
  var results = [];
  fs.readdir(dir, function(err, list) {
    if (err) return done(err);
    var i = 0;
    (function next() {
      var file = list[i++];
      if (!file) return done(null, results);
      file = dir + '/' + file;
      fs.stat(file, function(err, stat) {
        if (stat && stat.isDirectory()) {
          walk(file, function(err, res) {
            results = results.concat(res);
            next();
          });
        } else {
          results.push(file);
          next();
        }
      });
    })();
  });
};
const vm = require('vm');
fs.readdir("app/scripts/", function( err, files ) {
  files.forEach( function( file, index ) {
    fs.stat("app/scripts/"+file, function(err, stat) {
      if (stat && stat.isDirectory()) {
        walk("app/scripts/"+file, function(err, results) {
          if (err) throw err;
          results.forEach(function (file) {
            if (file.endsWith(".js") && !file.endsWith("app.js") && file.indexOf("tests") === -1) {
              include(file);
            }
          });
        });
      }
    });
  });
});
include("app/scripts/helper-functions.js");
include("app/scripts/index-iterator.es6.js");
include("app/scripts/constants.js");

app.use(express.static('app'))
app.use('/bower_components',express.static('bower_components'));

io.on('connection', function(socket){
  socket.emit('connectedToServer', {});
  socket.on('compile', function(obj, ack){
    var lastAst = {},
      lastAnalysis = {},
      lastProcesses = {},
      lastAbstraction = true;
    var ast = obj.ast;
    var context = obj.context;
    ast = expand(ast);
    const abstractionChanged = context.isFairAbstraction !== lastAbstraction;
    const analysis = performAnalysis(ast.processes, lastAnalysis, abstractionChanged);
    ast.processes = replaceReferences(ast.processes);
    const processes = interpret(ast.processes, analysis, lastProcesses, context);
    const operations = evaluateOperations(ast.operations, processes, ast.variableMap);
    ack({ processes:processes, operations:operations });
  });
});

http.listen(port, function(){
  console.log('Server started on: *:' + port);
});
function include(path) {
  var code = fs.readFileSync(path, 'utf-8');
  vm.runInThisContext(code, path);
}
