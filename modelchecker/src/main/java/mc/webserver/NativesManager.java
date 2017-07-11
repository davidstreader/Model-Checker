package mc.webserver;

import lombok.AllArgsConstructor;
import mc.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Manage loading dependencies for bower/node and vulcanize.
 */
@AllArgsConstructor
public class NativesManager {
    private static Logger logger = LoggerFactory.getLogger(NativesManager.class);
     /**
     * When dealing with mac computers, we can not set a library folder. To resolve this, we can just copy the libraries
     * we want to the users library folder.
     */
    public void copyNatives() {
        if (!Utils.isMac()) return;
        logger.info(""+ansi().render("@|yellow Copying natives|@"));
        String homeDir = System.getProperty("user.home");
        File libDir = new File(homeDir,"lib");
        libDir.mkdirs();
        File natives = new File("native",Utils.getArch());
        for (File n: natives.listFiles()) {
            try {
                Files.copy(n.toPath(),Paths.get(libDir.toPath().toString(),n.getName()), REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
