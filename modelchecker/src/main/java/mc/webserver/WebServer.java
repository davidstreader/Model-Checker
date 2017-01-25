package mc.webserver;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.fusesource.jansi.Ansi.ansi;
import static spark.Spark.get;


public class WebServer {
  Logger logger = LoggerFactory.getLogger(WebServer.class);
  @Getter
  private WebSocketServer socket;
  public void startServer() {
    logger.info(""+ansi().render("@|green Starting Web Server|@"));
    if (checkPortInUse()) return;
    Spark.externalStaticFileLocation("app");
    Spark.port(5000);
    get("/bower_components/*", (req, res) -> String.join("\n",Files.readAllLines(Paths.get(req.pathInfo().substring(1)))));
    logger.info(""+ansi().render("@|green Starting Socket.IO Server|@"));
    socket = new WebSocketServer();
  }

  private boolean checkPortInUse() {
    try {
      new ServerSocket(5000).close();
      return false;
    } catch (IOException e) {
      logger.error(""+ansi().render("@|red Port 5000 is already in use. Unable to start WebServer.|@"));
      logger.info(""+ansi().render("@|yellow Type exit to close the program.|@"));
      return true;
    }
  }


}
