package mc.webserver;

import mc.AutoUpdate;
import mc.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.IOException;
import java.net.ServerSocket;

import static org.fusesource.jansi.Ansi.ansi;
import static spark.Spark.webSocket;


public class WebServer {
    private Logger logger = LoggerFactory.getLogger(WebServer.class);
    public void startServer() {
        logger.info(""+ansi().render("@|green Starting Web Server|@"));
        if (checkPortInUse()) return;
        if (Utils.isJar()) {
            Spark.staticFileLocation("app");
        } else {
            Spark.externalStaticFileLocation("../../../../site/app");
        }
        Spark.port(5000);
        webSocket("/socket",WebSocketServer.class);
        Spark.get("/update",(r,t) -> {
            AutoUpdate update = new AutoUpdate();
            if (update.isUpdated()) return "No updates available";
            update.checkForUpdates();
            return "Update successful";
        });
        Spark.init();
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
