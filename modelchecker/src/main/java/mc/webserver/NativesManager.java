package mc.webserver;

import lombok.AllArgsConstructor;
import mc.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
        logger.info(""+ansi().render("@|yellow Copying natives|@"));
        String homeDir = System.getProperty("user.home");
        File libDir = new File(homeDir,"lib");
        libDir.mkdirs();
        if (!new File("native").exists() && Utils.isJar()) {
            try {
                File f =new File(NativesManager.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath());
                JarFile jarfile = new JarFile(f);
                Enumeration<JarEntry> enu = jarfile.entries();
                while (enu.hasMoreElements()) {
                    JarEntry je = enu.nextElement();
                    //Only copy natives
                    if (!je.getName().startsWith("native/"+Utils.getArch())) continue;
                    if (je.isDirectory()) {
                        continue;
                    }
                    File fl = new File(je.getName());
                    //When dealing with mac, just copy the natives to the user's library folder
                    if (Utils.isMac()) {
                        fl = new File(libDir.toPath().toString(),je.getName());
                    }
                    if (!fl.exists()) {
                        fl.getParentFile().mkdirs();
                    }
                    InputStream is = jarfile.getInputStream(je);
                    Files.copy(is,fl.toPath());
                    is.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
