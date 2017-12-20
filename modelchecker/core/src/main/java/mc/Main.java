package mc;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import mc.client.ui.UserInterfaceApplication;
import mc.commands.CommandManager;
import mc.commands.PassThroughCommandManager;
import mc.plugins.PluginManager;
import mc.util.Utils;
import mc.webserver.NativesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  @Getter
  private CommandManager commandManager;

  @Getter
  private Process subProcess;

  @Getter
  @Setter
  private boolean stopped = false;

  @Getter
  private boolean reloaded = false;
  private boolean autoKill = false;
  @Getter
  private static Main instance;

  private Main(boolean reloaded, boolean autoKill) {
    instance = this;
    this.autoKill = autoKill;
    //Make sure that we kill the sub-process when this process exits.
    Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    this.reloaded = reloaded;
    //If this is a sub process, or we are running with a console, don't start the gui.
    //Start the server if we aren't running from a jar or are in a sub process
    if (!Utils.isJar() || reloaded) {
      commandManager = new CommandManager(this);

      PluginManager.getInstance().registerPlugins();

      UserInterfaceApplication.main(new String[0]);
      //Listen for commands
      commandManager.registerInput();
      Logger logger = LoggerFactory.getLogger(Main.class);
      logger.info("Started Server!");
      return;
    }
    commandManager = new PassThroughCommandManager(this);
    //Start the wrapped process with all the native libraries added.
    spawnProcess(createWrappedProcess());
  }

  /**
   * Spawn a process and redirect its output to the right place
   *
   * @param builder a ProcessBuilder
   */
  public void spawnProcess(ProcessBuilder builder) {
    if (stopped) {
      return;
    }

    //If headless, redirect the output to the standard terminal
    builder.inheritIO();
    Thread current = Thread.currentThread();
    new Thread(() -> {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          Thread.sleep(100);
          if (subProcess != null) {
            subProcess.waitFor();
            current.interrupt();
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }).start();
    while (true) {
      try {
        subProcess = builder.start();
        //Redirect the terminal to the gui
        if (!autoKill) {
          subProcess.waitFor();
          System.exit(0);
        }
        System.out.println("Restarting sub-process");
        Thread.sleep(TimeUnit.MINUTES.toMillis(5));
      } catch (IOException | InterruptedException e) {
        System.out.println("Sub process died, Restarting sub-process!");
      }
      subProcess.destroyForcibly();
    }
  }

  public void stop() {
    //Kill the sub process if it exists
    if (subProcess != null) {
      subProcess.destroy();
    }
    Runtime.getRuntime().halt(0);
  }

  public static void main(String[] args) {
    //The easiest way to tell if we have reloaded the application is to set a flag.
    boolean reloaded = (args.length > 0 && args[0].equals("reloaded"));
    boolean autoKill = (args.length > 0 && args[0].equals("autoKill"));
    new Main(reloaded, autoKill);
  }

  /**
   * Since the jar is normally not started with the libraries loaded, we can just load it again with the libraries
   * in place.
   */
  public ProcessBuilder createWrappedProcess() {

    String nativePath = NativesManager.getNativesDir().toAbsolutePath().toString();
    //Set java.library.path to the native path for windows
    //Set the reloaded flag so that we know that the application has been loaded twice.
    //Set UseG1GC so that ram usage is dropped after peaks

    ProcessBuilder builder = new ProcessBuilder("java", "-XX:+UseG1GC", "-Djava.library.path=" + nativePath, "-jar", Utils.getJarPath(), "reloaded");
    Map<String, String> environment = builder.environment();
    //Set the linux native path
    environment.put("LD_LIBRARY_PATH", nativePath);
    //Set the windows native path
    environment.put("PATH", nativePath);
    //Set the mac native path
    environment.put("DYLD_LIBRARY_PATH", nativePath);
    return builder;
  }

}
