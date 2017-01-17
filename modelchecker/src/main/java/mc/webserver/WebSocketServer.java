package mc.webserver;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import mc.compiler.ast.ASTNode;

/**
 * Created by sanjay on 18/01/2017.
 */
public class WebSocketServer {
  public WebSocketServer() {
    Configuration config = new Configuration();
    config.setHostname("localhost");
    config.setPort(5000);

    final SocketIOServer server = new SocketIOServer(config);
    server.start();
    server.addEventListener("compile",SocketRecieveObject.class, (client, data, ackSender) -> {
      System.out.println(data);
    });
    /* socket.on('compile', function (obj, ack) {
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
  })*/
  }
}
