package mc;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mc.commands.CommandManager;
import mc.gui.MainGui;
import mc.webserver.DependencyManager;
import mc.webserver.WebServer;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import static mc.util.Utils.getArch;
import static org.fusesource.jansi.Ansi.ansi;
@NoArgsConstructor
public class Main {
  @Getter
  private CommandManager commandManager;
  @Getter
  private MainGui gui;
  @Getter
  private Process subProcess;
  @Getter
  @Setter
  private boolean stopped = false;
  @Getter
  private WebServer webServer;
  /**
   * Is this application currently running from a jar file
   */
  @Getter
  private boolean isJar = false;
  @Getter
  private boolean reloaded = false;
  private Main(boolean reloaded) {
    //Make sure that we kill the subprocess when this process exits.
    Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    commandManager = new CommandManager(this);
    Ansi.setEnabled(true);
    //getResource will add a jar: to the start of files inside jars.
    isJar = Main.class.getResource("Main.class").toString().startsWith("jar");
    this.reloaded = reloaded;
    //If this is a sub process, or we are running headless, dont start the gui.
    if (!reloaded && !GraphicsEnvironment.isHeadless()) {
      gui = new MainGui(this);
    }
    //If we are not running from a jar, but are running headless, enable ANSI normally.
    if (!isJar && GraphicsEnvironment.isHeadless()) {
      AnsiConsole.systemInstall();
    }
    //Start the server if we aren't running from a jar or are in a sub process
    if (!isJar || reloaded) {
      //If bower has not loaded, init it now.
      if (!new File("bower_components").exists())
        new DependencyManager(this).initBower();
      webServer = new WebServer();
      webServer.startServer();
      commandManager.registerInput();
      System.out.println(ansi().render("@|green Started Server!|@"));
      return;
    }
    //Load all the bower dependencies
    //Dont build node deps in production
    if (new File("executables").exists())
      new DependencyManager(this).initBower();
    startWrappedProcess();
  }
  /**
   * Spawn a process and redirect its output to the right place
   * @param builder a ProcessBuilder
   */
  public void spawnProcess(ProcessBuilder builder) {
    if (stopped) return;
    //If headless, redirect the output to the standard terminal
    if (gui == null) builder.inheritIO();
      //Else redirect error so we can get the processes output stream
    else builder.redirectErrorStream(true);
    try {
      subProcess = builder.start();
      //Redirect the terminal to the gui
      if (gui != null)
        gui.redirectTerminalProcess(subProcess);
      subProcess.waitFor();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void stop() {
    //Kill the sub prcess if it exists
    if (subProcess != null) {
      subProcess.destroy();
    }
    Runtime.getRuntime().halt(0);
  }

  public static void main(String[] args) {
    //The easiest way to tell if we have reloaded the application is to set a flag.
    boolean reloaded = (args.length > 0 && args[0].equals("reloaded"));
    new Main(reloaded);
  }

  /**
   * Since the jar is normally not started with the libraries loaded, we can just load it again with the libraries
   * in place.
   */
  private void startWrappedProcess() {

    System.out.println(ansi().render("@|red Native arguments not found!|@"));
    System.out.println(ansi().render("@|yellow Starting sub-process with native arguments|@"));
    String nativePath = Paths.get("native", getArch()).toString();
    //Easy way to get the current jar file
    String jarPath = WebServer.class.getResource("WebServer.class").toString().split("!")[0].replace("jar:file:/","");
    //Set java.library.path to the native path for windows
    ProcessBuilder builder = new ProcessBuilder("java","-Djava.library.path="+nativePath,"-jar",jarPath,"reloaded");
    Map<String, String> envs = builder.environment();
    //Set the linux native path
    envs.put("LD_LIBRARY_PATH", nativePath);
    //Set the mac native path
    envs.put("DYLD_LIBRARY_PATH", nativePath);
    spawnProcess(builder);
  }
}
