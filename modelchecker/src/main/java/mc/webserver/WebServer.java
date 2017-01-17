package mc.webserver;

import spark.Spark;

import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.get;


/**
 * Created by sanjay on 18/01/2017.
 */
public class WebServer {
  public static void main(String[] args) {
    Spark.externalStaticFileLocation("app");
    Spark.port(80);
    new WebSocketServer();
    get("/bower_components/*", (req, res) -> String.join("\n",Files.readAllLines(Paths.get(req.pathInfo().substring(1)))));
  }
}
