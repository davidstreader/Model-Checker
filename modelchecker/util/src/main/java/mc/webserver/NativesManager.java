package mc.webserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import lombok.AllArgsConstructor;
import mc.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage loading dependencies for bower/node and vulcanize.
 */
@AllArgsConstructor
public class NativesManager {
    private static Logger logger = LoggerFactory.getLogger(NativesManager.class);
    public static Path getNativesDir() {
        if (Utils.isMac()) {
            return getMacLibDir();
        } else {
            return Paths.get("resources/native",Utils.getArch());
        }

    }
    private static Path getMacLibDir() {
        String homeDir = System.getProperty("user.home");
        Path libDir = Paths.get(homeDir, "lib");
        libDir.toFile().mkdirs();
        return libDir;
    }
     /**
     * When dealing with mac computers, we can not set a library folder. To resolve this, we can just copy the libraries
     * we want to the users library folder.
     */
    public void copyNatives() {
        logger.info("Copying natives");
        //Where to copy the files from in the jar (jars always use /)
        String zipPrefix = "native/"+Utils.getArch();
        if (!new File("native",Utils.getArch()).exists() && Utils.isJar()) {
            JarFile jarfile = null;
            try {
                File f =new File(Utils.getJarPath());
                jarfile = new JarFile(f);
                Enumeration<JarEntry> enu = jarfile.entries();
                while (enu.hasMoreElements()) {
                    JarEntry je = enu.nextElement();
                    //Only copy natives
                    if (!je.getName().startsWith(zipPrefix)) continue;
                    if (je.isDirectory()) {
                        continue;
                    }
                    File fl = new File(je.getName());
                    //When dealing with mac, just copy the natives to the user's library folder
                    if (Utils.isMac()) {
                        //Skip prefix when copying for mac
                        fl = new File(getMacLibDir().toString(),je.getName().replace(zipPrefix,""));
                    }
                    if (!fl.exists()) {
                        fl.getParentFile().mkdirs();
                    }
                    InputStream is = jarfile.getInputStream(je);
                    Files.copy(is,fl.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    is.close();
                }
                jarfile.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                if(jarfile != null)
                    try {
                        jarfile.close();
                    } catch(IOException ignored){}
            }
        }
    }
}
