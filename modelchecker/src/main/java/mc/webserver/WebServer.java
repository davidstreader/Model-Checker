package mc.webserver;

import lombok.Getter;
import spark.Spark;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.fusesource.jansi.Ansi.ansi;
import static spark.Spark.get;


public class WebServer {
  @Getter
  private WebSocketServer socket;
  public void startServer() {
    System.out.println(ansi().render("@|green Starting Web Server|@"));
    Spark.externalStaticFileLocation("app");
    Spark.port(5000);
    get("/bower_components/*", (req, res) -> String.join("\n",Files.readAllLines(Paths.get(req.pathInfo().substring(1)))));
    System.out.println(ansi().render("@|green Starting Socket.IO Server|@"));
    socket = new WebSocketServer();
  }


}
