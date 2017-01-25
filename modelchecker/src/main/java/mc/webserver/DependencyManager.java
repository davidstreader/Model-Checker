package mc.webserver;

import lombok.AllArgsConstructor;
import mc.Main;
import mc.util.Utils;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static mc.util.Utils.getArch;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Manage loading dependencies for bower/node and vulcanize.
 */
@AllArgsConstructor
public class DependencyManager {
  /**
   * Run a copy of the node manager on its own.
   * @param args command arguments
   */
  public static void main(String[] args) {
    new DependencyManager(new Main()).initBower();
  }
  private Main main;
  public void initBower() {
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
    System.out.println(ansi().render("@|yellow Updating Bower Dependencies|@ - @|yellow This may take a while.|@"));
    ProcessBuilder builder = new ProcessBuilder(new File("bower_install","bower") + Utils.getNodeExtension(), "install","-d");
    main.spawnProcess(builder);
  }

  private void installBower() {
    File bowerInstall = new File("bower_install","bower");
    if (!bowerInstall.exists()) {
      System.out.println(ansi().render("@|green Installing bower|@"));
      ProcessBuilder builder = new ProcessBuilder("npm" + Utils.getNodeExtension(), "install", "bower","-d");
      builder.directory(new File("bower_install"));
      main.spawnProcess(builder);
      if (!Utils.isWin()) {
        chmod("bower");
      }
    }
  }
  private void chmod(String app) {
    ProcessBuilder builder = new ProcessBuilder("chmod","+x",app);
    builder.directory(new File("bower_install"));
    main.spawnProcess(builder);
  }
  private void unzipNPM() throws IOException, ZipException {
    File bowerInstall = new File("bower_install");
    if (bowerInstall.mkdir()) {
      System.out.println(ansi().render("@|red Node install not found!|@\n@|yellow Copying files for Node|@"));
      File nodeExes = new File("executables", getArch());
      for (File f : nodeExes.listFiles()) {
        System.out.println("Copying: "+f.getName());
        Files.copy(f.toPath(), Paths.get(bowerInstall.toPath().toString(), f.getName()));
      }
      File nodeModules = new File(bowerInstall,"node_modules");
      File npmdir = new File(nodeModules,"npm");
      nodeModules.mkdir();
      System.out.println(ansi().render("@|yellow Extracting NPM|@"));
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
      System.out.println(ansi().render("@|yellow Copying NPM executables|@"));
      for (File f: new File(npmdir,"bin").listFiles()) {
        System.out.println("Copying: "+f.getName());
        Files.copy(f.toPath(), Paths.get(bowerInstall.toPath().toString(), f.getName()));
      }
      if (!Utils.isWin()) {
        chmod("npm");
      }
    }

  }
  public void vulcanize() {
    ProcessBuilder builder = new ProcessBuilder(new File("bower_install", "npm")+ Utils.getNodeExtension(),"run-script","vulcanize");
    main.spawnProcess(builder);
  }
}
