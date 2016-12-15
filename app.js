const fs = require('fs');
const Worker = require("tiny-worker");
const workerMap = {};

const express = require('express');
const app = express();
const http = require('http').Server(app);
const io = require('socket.io')(http);
const port = 5000;
const msgpack = require('')
app.use(express.static('app'))
app.use('/bower_components', express.static('bower_components'));
io.on('connection', function (socket) {
  socket.on('compile', function (obj, ack) {
    if (workerMap[socket.id]) {
      workerMap[socket.id].terminate();
    }
    //Compile in another thread, so we do not hang the server  from accepting other requests
    let worker = new Worker("asyncCompiler.js");
    workerMap[socket.id] = worker;
    worker.onmessage = function(e) {
      if (e.data.result) {
        const out = new Uint8Array();
        ack(out);
        worker.terminate();
      } else if (e.data.message) {
        socket.emit("log",e.data);
      }
    }
    worker.postMessage(obj);
  })
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

