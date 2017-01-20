package mc.webserver;

import com.google.common.base.Ascii;
import com.redpois0n.terminal.DebugTerminal;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import spark.Spark;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.fusesource.jansi.Ansi.ansi;
import static org.sosy_lab.common.NativeLibraries.Architecture;
import static org.sosy_lab.common.NativeLibraries.OS;
import static spark.Spark.get;


public class WebServer {
  Process subProcess;
  WebSocketServer socket;
  PrintStream screen;
  DebugTerminal terminal;
  boolean stopped = false;
  public static void main(String[] args) {
    //Inside the sub process, the above call will fail, this allows us to use the parent console to render.
    Ansi.setEnabled(true);
    //getResource will add a jar: to the start of files inside jars.
    boolean isJar = WebServer.class.getResource("WebServer.class").toString().startsWith("jar");
    boolean hasReloaded = (args.length > 0 && args[0].equals("reloaded"));
    //If we are running from a jar, and have a single arg, reload the jar with linux / mac system paths
    new WebServer(isJar,hasReloaded);
  }

  private void initConsole() {
    terminal = new DebugTerminal();
    //When the window is closed, kill the sub process and the main app.
    terminal.getFrame().addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        stopped = true;
        if (subProcess != null) {
          subProcess.destroy();
        }
        super.windowClosed(e);
        System.exit(0);
      }
    });
    //Redirect System.out and System.err to the console
    System.setOut(screen=new PrintStream(new OutputStream() {
      //Create a buffer to hold the string until we want to write it
      char[] buffer = new char[4096];
      //Index we are at in the buffer
      int i = 0;
      @Override
      public void write(int b) throws IOException {
        buffer[i++] = (char)b;
        //Write when we hit a newline.
        if ((char) b == '\n' || i == 4096) {
          terminal.getTerminal().append(new String(buffer,0,i));
          i = 0;
        }
      }
    }));
    System.setErr(System.out);
  }

  private WebServer(boolean isJar, boolean hasReloaded) {
    if (!hasReloaded && !GraphicsEnvironment.isHeadless()) {
      initConsole();
    }
    if (!isJar && GraphicsEnvironment.isHeadless()) {
      AnsiConsole.systemInstall();
    }
    if (!isJar || hasReloaded) {
      startServer();
      return;
    }
    initBower();
    startWrappedProcess();
  }

  private void initBower() {
    try {
      unzipNPM();
    } catch (IOException | ZipException e) {
      e.printStackTrace();
      return;
    }
    installBower();
    runBower();
  }

  private void runBower() {
    System.out.println(ansi().render("@|yellow Updating Bower Dependencies|@ - @|yellow This may take a while.|@"));
    boolean isWin = OS.guessOperatingSystem() == OS.WINDOWS;
    ProcessBuilder builder = new ProcessBuilder(new File("bower_install","bower") + (isWin ? ".cmd" : ""), "install","-d");
    spawnProcess(builder);
  }

  private void installBower() {
    File bowerInstall = new File("bower_install","bower");
    if (!bowerInstall.exists()) {
      System.out.println(ansi().render("@|green Installing bower|@"));
      boolean isWin = OS.guessOperatingSystem() == OS.WINDOWS;
      ProcessBuilder builder = new ProcessBuilder("npm" + (isWin ? ".cmd" : ""), "install", "bower","-d");
      builder.directory(new File("bower_install"));
      spawnProcess(builder);
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
        terminal.getProgressPanel().setVisible(true);
        monitor = new Thread(()->{
          while (!Thread.interrupted()) {
            terminal.getProgressBar1().setValue(file.getProgressMonitor().getPercentDone());
          }
        });
        monitor.start();
      }
      file.extractAll(nodeModules.toString());
      if (monitor != null) {
        monitor.interrupt();
        terminal.getProgressPanel().setVisible(false);
      }
      Files.move(new File(nodeModules,"npm-4.1.1").toPath(),npmdir.toPath());
      System.out.println(ansi().render("@|yellow Copying NPM executables|@"));
      for (File f: new File(npmdir,"bin").listFiles()) {
        System.out.println("Copying: "+f.getName());
        Files.copy(f.toPath(), Paths.get(bowerInstall.toPath().toString(), f.getName()));
      }
    }

  }
  private String getArch() {
    String arch = Ascii.toLowerCase(Architecture.guessVmArchitecture().name());
    String os = Ascii.toLowerCase(OS.guessOperatingSystem().name());
    return arch + "-" + os;
  }
  private void startWrappedProcess() {

    System.out.println(ansi().render("@|red Native arguments not found!|@"));
    System.out.println(ansi().render("@|yellow Starting sub-process with native arguments|@"));
    String nativePath =Paths.get("native", getArch()).toString();
    String jarPath = WebServer.class.getResource("WebServer.class").toString().split("!")[0].replace("jar:file:/","");
    ProcessBuilder builder = new ProcessBuilder("java","-Djava.library.path="+nativePath,"-jar",jarPath,"reloaded");
    Map<String, String> envs = builder.environment();
    envs.put("LD_LIBRARY_PATH", nativePath);
    envs.put("DYLD_LIBRARY_PATH", nativePath);
    spawnProcess(builder);
  }
  public void startServer() {
    System.out.println(ansi().render("@|green Starting Web Server|@"));
    Spark.externalStaticFileLocation("app");
    Spark.port(5000);
    get("/bower_components/*", (req, res) -> String.join("\n",Files.readAllLines(Paths.get(req.pathInfo().substring(1)))));
    socket = new WebSocketServer();
  }

  /**
   * Spawn a process and redirect its output to the right place
   * @param builder a ProcessBuilder
   */
  private void spawnProcess(ProcessBuilder builder) {
    if (stopped) return;
    //If headless, redirect the output to the standard terminal
    if (GraphicsEnvironment.isHeadless()) builder.inheritIO();
      //Else redirect error so we can get the processes output stream
    else builder.redirectErrorStream(true);
    try {
      subProcess = builder.start();
      //Redirect the terminal to the gui
      if (!GraphicsEnvironment.isHeadless())
        terminal.wrapProcess(subProcess);
      subProcess.waitFor();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}
