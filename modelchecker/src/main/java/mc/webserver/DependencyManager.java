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
    new DependencyManager(new Main()).initBower();
  }
  private Main main;
  public void initBower() {
    logger.info(""+ansi().render("@|yellow Copying natives|@"));
    copyNatives();
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
        return new File("bower_install","node"+Utils.getNPMExtension()).getAbsolutePath();
    }
  private void installBower() {
    File bowerInstall = new File("bower_install","bower");
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
  private void unzipNPM() throws IOException, ZipException {
    File bowerInstall = new File("bower_install");
    if (bowerInstall.mkdir()) {
      logger.info(""+ansi().render("@|red Node install not found!|@"));
      logger.info(""+ansi().render("@|yellow Copying files for Node|@"));
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
      file.extractAll(nodeModules.toString());
      if (monitor != null) {
        monitor.interrupt();
        main.getGui().hideProgressBar();
      }
      Files.move(new File(nodeModules,"npm-4.1.1").toPath(),npmdir.toPath());
      logger.info(""+ansi().render("@|yellow Copying NPM executables|@"));
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
  public void vulcanize() {
    logger.info(""+ansi().render("@|yellow Vulcanizing HTML|@"));
    ProcessBuilder builder = new ProcessBuilder(getNodeExec(),"node_modules/vulcanize/bin/vulcanize","-o","../app/elements/elements.vulcanized.html","../app/elements/elements.html","--strip-comments");
    builder.directory(new File("bower_install"));
    main.spawnProcess(builder);
  }
  public void copyNatives() {
    if (!Utils.isMac()) return;
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
