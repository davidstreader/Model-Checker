package mc;

import lombok.Getter;
import lombok.Setter;
import mc.commands.CommandManager;
import mc.commands.PassThroughCommandManager;
import mc.gui.MainGui;
import mc.util.Utils;
import mc.webserver.NativesManager;
import mc.webserver.WebServer;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

import static mc.util.Utils.getArch;
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
    @Getter
    private static Main instance;
    private Logger logger = LoggerFactory.getLogger(Main.class);
    public Main() {
        AnsiConsole.systemInstall();
        MainGui.registerConsoleAppender();
    }
    public Main(boolean reloaded) {
        instance = this;
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
        new NativesManager().copyNatives();
        //Start the server if we aren't running from a jar or are in a sub process
        if (!Utils.isJar() || reloaded) {
            commandManager = new CommandManager(this);
            webServer = new WebServer();
            webServer.startServer();
            //Listen for commands
            commandManager.registerInput();
            logger.info(""+ansi().render("@|green Started Server!|@"));
            return;
        }
        commandManager = new PassThroughCommandManager(this);
        //Start the wrapped process with all the native libraries added.
        startWrappedProcess(getClass());
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
    private void startWrappedProcess(Class<?> classToWrap) {

        logger.warn(ansi().render("@|red Native arguments not found!|@")+"");
        logger.info(ansi().render("@|yellow Starting sub-process with native arguments|@")+"");
        String nativePath = Paths.get("native", getArch()).toAbsolutePath().toString();
        //Set java.library.path to the native path for windows
        //Set jansi.passthrough as the parent application will handle the ansi chars, not the child.
        //Set the reloaded flag so that we know that the application has been loaded twice.
        ProcessBuilder builder = new ProcessBuilder("java","-Djansi.passthrough=true","-Djava.library.path="+nativePath,"-cp",Utils.getJarPath(),classToWrap.getName(),"reloaded");
        Map<String, String> environment = builder.environment();
        //Set the linux native path
        environment.put("LD_LIBRARY_PATH", nativePath);
        //Set the windows native path
        environment.put("PATH", nativePath);
        //Set the mac native path
        environment.put("DYLD_LIBRARY_PATH", nativePath);
        spawnProcess(builder);
    }
}
