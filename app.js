const isWin = /^win/.test(process.platform);
const fs = require('fs');
const Worker = require("tiny-worker");
const workerMap = {};
//Allow executing programs
const execSync = require('child_process').execSync;
//run gradlew and build the project to generate a lib folder.

execSync((isWin?"":"./")+'gradlew build', {cwd: 'modelsolver', stdio:'inherit'});
const express = require('express');
const app = express();
const http = require('http').Server(app);
const io = require('socket.io')(http);
const port = 5000;
app.use(express.static('app'))
app.use('/bower_components', express.static('bower_components'));
io.on('connection', function (socket) {
  socket.on('compile', function (obj, ack) {
    if (workerMap[socket.id]) {
      workerMap[socket.id].terminate();
    }
    //Compile in another thread, so we do not hang the server  from accepting other requests
    let worker = new Worker("server-compiler-worker.js");
    workerMap[socket.id] = worker;
    worker.onmessage = function(e) {
      if (e.data.result) {
        ack(e.data.result);
        worker.terminate();
      } else if (e.data.message) {
        socket.emit("log",e.data);
      }
    };
    worker.postMessage(obj);
  });
  socket.on("disconnect", () => {
    if (workerMap[socket.id]) {
      workerMap[socket.id].terminate();
      delete workerMap[socket.id];
    }
  })
});

http.listen(port, function () {
  console.log('Server started on: *:' + port);
});

