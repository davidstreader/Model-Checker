package mc.webserver;

import lombok.AllArgsConstructor;
import mc.Main;
import mc.util.Utils;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static mc.util.Utils.getArch;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Manage loading dependencies for bower/node and vulcanize.
 */
@AllArgsConstructor
public class DependencyManager {
    private static Logger logger = LoggerFactory.getLogger(DependencyManager.class);
    /**
     * Run a copy of the node manager on its own.
     * @param args command arguments
     */
    public static void main(String[] args) {
        new DependencyManager(new Main()).initDeps();
    }
    private Main main;

    /**
     * Initialize natives + bower
     */
    public void initDeps() {

        copyNatives();
        //If bower has not loaded, init it now.
        if (!new File("bower_components").exists())
            initBower();
    }
    /**
     * Initialize a bower workspace
     */
    private void initBower() {
        try {
            unzipNPM();
        } catch (IOException | ZipException e) {
            e.printStackTrace();
            return;
        }
        installBower();
        runBower();
        vulcanize();
    }

    private void runBower() {
        logger.info(""+ansi().render("@|yellow Updating Bower Dependencies|@ - @|yellow This may take a while.|@"));
        if (Utils.isWin()) {
            ProcessBuilder builder = new ProcessBuilder(new File("bower_install", "bower") + Utils.getNPMExtension(), "install", "-d");
            main.spawnProcess(builder);
        } else {
            ProcessBuilder builder = new ProcessBuilder(Paths.get("bower_install","node_modules","bower","bin","bower") + Utils.getNPMExtension(), "install", "-d");
            main.spawnProcess(builder);
        }
    }
    private String getNPMExec() {
        return new File("bower_install","npm"+Utils.getNPMExtension()).getAbsolutePath();
    }
    private String getNodeExec() {
        return new File("bower_install","node"+Utils.getNodeExtension()).getAbsolutePath();
    }
    private void installBower() {
        File bowerInstall = Paths.get("bower_install","node_modules","bower").toFile();
        if (!bowerInstall.exists()) {
            logger.info(""+ansi().render("@|green Installing bower|@"));
            ProcessBuilder builder = new ProcessBuilder(getNPMExec(), "install", "bower","-d");
            builder.directory(new File("bower_install").getAbsoluteFile());
            main.spawnProcess(builder);
            if (!Utils.isWin()) {
                chmod("bower");
            }
            installVulcanize();
        }
    }
    private void installVulcanize() {
        logger.info(""+ansi().render("@|green Installing vulcanize|@"));
        ProcessBuilder builder = new ProcessBuilder(getNPMExec(), "install", "vulcanize","-d");
        builder.directory(new File("bower_install").getAbsoluteFile());
        main.spawnProcess(builder);
    }
    private void chmod(String app) {
        ProcessBuilder builder = new ProcessBuilder("chmod","+x",app);
        builder.directory(new File("bower_install"));
        main.spawnProcess(builder);
    }

    /**
     * Unzip node, getting the correct executables for the current architecture.
     * Also make sure to fix permissions on unix systems.
     * @throws IOException An error occurred while copying files
     * @throws ZipException An error occurred while extracting files
     */
    private void unzipNPM() throws IOException, ZipException {
        File bowerInstall = new File("bower_install");
        if (bowerInstall.mkdir()) {
            logger.info(""+ansi().render("@|red Node install not found!|@"));
            logger.info(""+ansi().render("@|yellow Copying files for Node|@"));
            //Copy node executables
            File nodeExes = new File("executables", getArch());
            for (File f : nodeExes.listFiles()) {
                logger.info(""+"Copying: "+f.getName());
                Files.copy(f.toPath(), Paths.get(bowerInstall.toPath().toString(), f.getName()));
            }
            File nodeModules = new File(bowerInstall,"node_modules");
            File npmdir = new File(nodeModules,"npm");
            nodeModules.mkdir();
            logger.info(""+ansi().render("@|yellow Extracting NPM|@"));
            ZipFile file = new ZipFile(Paths.get("executables","npm-4.1.1.zip").toString());
            Thread monitor = null;
            //Start up a thread that monitors the following zip extraction in another thread so we can
            //have a progress bar
            if (main.getGui() != null) {
                main.getGui().showProgressBar();
                monitor = new Thread(()->{
                    while (!Thread.interrupted()) {
                        main.getGui().setProgressBarValue(file.getProgressMonitor().getPercentDone());
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                });
                monitor.start();
            }
            //Extract npm from a distribution zip
            file.extractAll(nodeModules.toString());
            if (monitor != null) {
                monitor.interrupt();
                main.getGui().hideProgressBar();
            }
            Files.move(new File(nodeModules,"npm-4.1.1").toPath(),npmdir.toPath());
            logger.info(""+ansi().render("@|yellow Copying NPM executables|@"));
            //copy the binaries from npm's install to the main node dir
            for (File f: new File(npmdir,"bin").listFiles()) {
                logger.info(""+"Copying: "+f.getName());
                Files.copy(f.toPath(), Paths.get(bowerInstall.toPath().toString(), f.getName()));
            }
            if (!Utils.isWin()) {
                chmod("npm");
                chmod("node");
            }
        }

    }

    /**
     * When someone requests the website, its best to send one giant file instead of thousands of small ones.
     * Vulcanization is the process that merges all the files together, and this will run a vulcanization routine
     * from node.
     */
    private void vulcanize() {
        logger.info(""+ansi().render("@|yellow Vulcanizing HTML|@"));
        ProcessBuilder builder = new ProcessBuilder(getNodeExec(),"node_modules/vulcanize/bin/vulcanize","-o","../app/elements/elements.vulcanized.html","../app/elements/elements.html","--strip-comments");
        builder.directory(new File("bower_install"));
        main.spawnProcess(builder);
    }

    /**
     * When dealing with mac computers, we can not set a library folder. To resolve this, we can just copy the libraries
     * we want to the users library folder.
     */
    private void copyNatives() {
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
