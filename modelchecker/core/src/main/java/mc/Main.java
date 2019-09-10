package mc;

import mc.client.ui.UserInterfaceApplication;
import mc.plugins.PluginManager;
import mc.util.NativesManager;
import mc.util.Utils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class Main {

    private Process subProcess;

    private boolean stopped = false;

    private boolean reloaded = false;
    private boolean autoKill = false;
    private static Main instance;

    /*
       Starting point  Stuff here to allow auto restarting the process if it crashes!
     */
    public static void main(String[] args) {
        //The easiest way to tell if we have reloaded the application is to set a flag.
        boolean reloaded = (args.length > 0 && args[0].equals("reloaded"));
        boolean autoKill = (args.length > 0 && args[0].equals("autoKill"));
        new Main(reloaded, autoKill);
    }

    private Main(boolean reloaded, boolean autoKill) {
        instance = this;
        this.autoKill = autoKill;
        //Make sure that we kill the sub-process when this process exits.
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
        this.reloaded = reloaded;
        //If this is a sub process, or we are running with a console, don't start the gui.
        //Start the server if we aren't running from a jar or are in a sub process
        if (!Utils.isJar() || reloaded) {
            PluginManager.getInstance().registerPlugins();
            UserInterfaceApplication.main(new String[0]);
            //Listen for commands
            return;
        }

        //Start the wrapped process with all the native libraries added.
        spawnProcess(createWrappedProcess());
    }

    public static Main getInstance() {
        return Main.instance;
    }

    /**
     * Spawn a process and redirect its output to the right place
     *
     * @param builder a ProcessBuilder
     */
    private void spawnProcess(ProcessBuilder builder) {
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


    /**
     * Since the jar is normally not started with the libraries loaded, we can just load it again with the libraries
     * in place.
     * ProcessBuilder is oracles platform independent library to manage a process
     */
    private ProcessBuilder createWrappedProcess() {
        String jarDir = Utils.getJarPath();
        System.out.println(" jarDir "+jarDir);
        String homeDir = System.getProperty("user.dir");
        System.out.println("userDir " +homeDir);

        Path libDir = Paths.get(homeDir, Utils.getArch());
        String nativePath = libDir.toString();
       // String nativePath = NativesManager.getNativesDir().toAbsolutePath().toString();
        //Set java.library.path to the native path for windows
        //Set the reloaded flag so that we know that the application has been loaded twice.
        //Set UseG1GC so that ram usage is dropped after peaks

        ProcessBuilder builder = new ProcessBuilder("java", "-XX:+UseG1GC", "-Djava.library.path=" + nativePath, "-jar", Utils.getJarPath(), "reloaded");
        Map<String, String> environment = builder.environment();
        if (Utils.isMac()) {
            //Set the mac native path
            environment.put("DYLD_LIBRARY_PATH", nativePath);
        } else if (Utils.isWin()) {
            //Set the windows native path
            environment.put("PATH", nativePath);
        } else {
            //Set the linux native path
            environment.put("LD_LIBRARY_PATH", nativePath);
        }
        //System.out.println("\n\n*******\n "+builder.command()+"\n*******\n");
        System.out.println("user.dir " + System.getProperty("user.dir") + "  " +
            System.getProperty("os.name") + " " + System.getProperty("os.arch"));
        System.out.println(" ENVIRONMENT\n " + environment.keySet().stream().map(x -> x + " -> " + environment.get(x) + ",\n").collect(Collectors.joining()));
        System.out.println("\n*******\n " + builder.command() + "\n*******\n");

        return builder;
    }

    public Process getSubProcess() {
        return this.subProcess;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public boolean isReloaded() {
        return this.reloaded;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
}
