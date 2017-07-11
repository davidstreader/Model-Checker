package mc.webserver;

import lombok.AllArgsConstructor;
import mc.Main;
import mc.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
        if (!new File("native").exists() && Utils.isJar()) {
            try {
                File f =new File(NativesManager.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath());
                java.util.jar.JarFile jarfile = new java.util.jar.JarFile(f);
                java.util.Enumeration<java.util.jar.JarEntry> enu = jarfile.entries();
                while (enu.hasMoreElements()) {
                    java.util.jar.JarEntry je = enu.nextElement();
                    if (!je.getName().startsWith("native")) continue;
                    if (je.isDirectory()) {
                        continue;
                    }
                    java.io.File fl = new java.io.File(je.getName());
                    if (!fl.exists()) {
                        fl.getParentFile().mkdirs();
                    }
                    java.io.InputStream is = jarfile.getInputStream(je);
                    Files.copy(is,fl.toPath());
                    is.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
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
