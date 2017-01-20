package mc.webserver;

import lombok.AllArgsConstructor;
import mc.Main;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.sosy_lab.common.NativeLibraries;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static mc.util.Utils.getArch;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Manage loading dependencies for bower
 */
@AllArgsConstructor
public class NodeManager {
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
    boolean isWin = NativeLibraries.OS.guessOperatingSystem() == NativeLibraries.OS.WINDOWS;
    ProcessBuilder builder = new ProcessBuilder(new File("bower_install","bower") + (isWin ? ".cmd" : ""), "install","-d");
    main.spawnProcess(builder);
  }

  private void installBower() {
    File bowerInstall = new File("bower_install","bower");
    if (!bowerInstall.exists()) {
      System.out.println(ansi().render("@|green Installing bower|@"));
      boolean isWin = NativeLibraries.OS.guessOperatingSystem() == NativeLibraries.OS.WINDOWS;
      ProcessBuilder builder = new ProcessBuilder("npm" + (isWin ? ".cmd" : ""), "install", "bower","-d");
      builder.directory(new File("bower_install"));
      main.spawnProcess(builder);
    }
  }

  private void unzipNPM() throws IOException, ZipException {
    File bowerInstall = new File("bower_install");
    if (bowerInstall.mkdir()) {
      System.out.println(ansi().render("@|red Node install not found!|@ @|yellow Copying files for Node|@"));
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
      if (!GraphicsEnvironment.isHeadless()) {
        main.getGui().showProgressBar();
        monitor = new Thread(()->{
          while (!Thread.interrupted()) {
            main.getGui().setProgressBarValue(file.getProgressMonitor().getPercentDone());
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
    }

  }
  public void vulcanize() {
    boolean isWin = NativeLibraries.OS.guessOperatingSystem() == NativeLibraries.OS.WINDOWS;
    ProcessBuilder builder = new ProcessBuilder(Paths.get("bower_install", "npm"+ (isWin ? ".cmd" : "")).toAbsolutePath().toString(),"run-script","vulcanize");
    main.spawnProcess(builder);
  }
}
