package mc;

import lombok.Getter;
import lombok.Setter;
import mc.commands.CommandManager;
import mc.commands.PassThroughCommandManager;
import mc.gui.MainGui;
import mc.util.Utils;
import mc.webserver.NativesManager;
import mc.webserver.WebServer;
import mc.webserver.webobjects.LogMessage;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.fusesource.jansi.Ansi.ansi;
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
    @Getter
    private boolean reloaded = false;
    private boolean autoKill = false;
    @Getter
    private static Main instance;

    public Main() {
        AnsiConsole.systemInstall();
        MainGui.registerConsoleAppender();
    }
    public Main(boolean reloaded, boolean autoKill) {
        instance = this;
        this.autoKill = autoKill;
        AnsiConsole.systemInstall();
        //Make sure that we kill the sub-process when this process exits.
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        this.reloaded = reloaded;
        //If this is a sub process, or we are running with a console, don't start the gui.
        if (!reloaded && Utils.isJavaw()) {
            gui = new MainGui(this);
        } else {
            MainGui.registerConsoleAppender();
        }
        if (!reloaded) {
            new NativesManager().copyNatives();
            new AutoUpdate().checkForUpdates();
        }
        //Start the server if we aren't running from a jar or are in a sub process
        if (!Utils.isJar() || reloaded) {
            commandManager = new CommandManager(this);
            webServer = new WebServer();
            webServer.startServer();
            //Listen for commands
            commandManager.registerInput();
            Logger logger = LoggerFactory.getLogger(Main.class);
            logger.info(""+ansi().render("@|green Started Server!|@"));
            return;
        }
        commandManager = new PassThroughCommandManager(this);
        //Start the wrapped process with all the native libraries added.
        spawnProcess(createWrappedProcess());
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

        while (!Thread.currentThread().isInterrupted()) {
            try {
                subProcess = builder.start();
                //Redirect the terminal to the gui
                if (gui != null)
                    gui.redirectTerminalProcess(subProcess);
                if (!autoKill) {
                    subProcess.waitFor();
                    System.exit(0);
                }
                System.out.println("Restarting sub-process");
                Thread.sleep(TimeUnit.MINUTES.toMillis(1));
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            subProcess.destroyForcibly();

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
        boolean autoKill = (args.length > 0 && args[0].equals("autoKill"));
        new Main(reloaded,autoKill);
    }

    /**
     * Since the jar is normally not started with the libraries loaded, we can just load it again with the libraries
     * in place.
     */
    public ProcessBuilder createWrappedProcess() {

        String nativePath = NativesManager.getNativesDir().toAbsolutePath().toString();
        //Set java.library.path to the native path for windows
        //Set jansi.passthrough as the parent application will handle the ansi chars, not the child.
        //Set the reloaded flag so that we know that the application has been loaded twice.
        //Set UseG1GC so that ram usage is dropped after peaks
        ProcessBuilder builder = new ProcessBuilder("java","-Djansi.passthrough=true","-XX:+UseG1GC","-Djava.library.path="+nativePath,"-jar",Utils.getJarPath(),"reloaded");
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
