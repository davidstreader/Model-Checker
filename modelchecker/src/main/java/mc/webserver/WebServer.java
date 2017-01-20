package mc.webserver;

import com.google.common.base.Ascii;
import mc.solver.JavaSMTConverter;
import mc.util.TextAreaOutputStream;
import mc.util.expr.Expression;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import spark.Spark;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.sosy_lab.common.NativeLibraries.Architecture;
import static org.sosy_lab.common.NativeLibraries.OS;
import static spark.Spark.get;


public class WebServer {
  Process subProcess;
  WebSocketServer socket;
  PrintStream screen;
  public static void main(String[] args) {
    boolean isJar = WebServer.class.getResource("WebServer.class").toString().startsWith("file");
    boolean hasReloaded = (args.length > 0 && args[0].equals("reloaded"));
    //If we are running from a jar, and have a single arg, reload the jar with linux / mac system paths
    new WebServer(isJar,hasReloaded);
  }

  private void initConsole() {
    JFrame jFrame = new JFrame("Web Server");
    Console console = new Console();
    jFrame.setContentPane(console.$$$getRootComponent$$$());
    jFrame.pack();
    jFrame.setSize(640,480);
    jFrame.setVisible(true);
    jFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        super.windowClosed(e);
        if (subProcess != null) {
          subProcess.destroy();
        }
        System.exit(0);
      }
    });
    System.setOut(screen=new PrintStream(new TextAreaOutputStream(console.getTextArea1())));
    System.setErr(System.out);
  }

  private WebServer(boolean isJar, boolean hasReloaded) {
    if (!hasReloaded) {
      initConsole();
    }
    if (isJar || hasReloaded) {
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
    System.out.println("Updating Bower Dependencies - This may take a while.");
    boolean isWin = OS.guessOperatingSystem() == OS.WINDOWS;
    ProcessBuilder builder = new ProcessBuilder(new File("bower_install","bower") + (isWin ? ".cmd" : ""), "install");
    builder.inheritIO();
    try {
      Process p = builder.start();
      p.waitFor();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void installBower() {
    File bowerInstall = new File("bower_install","bower");
    if (!bowerInstall.exists()) {
      System.out.println("Installing bower");
      boolean isWin = OS.guessOperatingSystem() == OS.WINDOWS;
      ProcessBuilder builder = new ProcessBuilder("npm" + (isWin ? ".cmd" : ""), "install", "bower");
      builder.directory(new File("bower_install"));
      builder.inheritIO();
      try {
        Process p = builder.start();
        p.waitFor();
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private void unzipNPM() throws IOException, ZipException {
    File bowerInstall = new File("bower_install");
    if (bowerInstall.mkdir()) {
      System.out.println("Node install not found! Copying files for Node");
      File nodeExes = new File("executables", getArch());
      for (File f : nodeExes.listFiles()) {
        System.out.println("Copying: "+f.getName());
        Files.copy(f.toPath(), Paths.get(bowerInstall.toPath().toString(), f.getName()));
      }
      File nodeModules = new File(bowerInstall,"node_modules");
      File npmdir = new File(nodeModules,"npm");
      nodeModules.mkdir();
      System.out.println("Extracting NPM");
      ZipFile file = new ZipFile(Paths.get("executables","npm-4.1.1.zip").toString());
      file.extractAll(nodeModules.toString());
      Files.move(new File(nodeModules,"npm-4.1.1").toPath(),npmdir.toPath());
      System.out.println("Copying NPM executables");
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
    System.out.println("Native arguments not found!");
    System.out.println("Starting subprocess with native arguments");
    String nativePath =Paths.get("native", getArch()).toString();
    String jarPath = WebServer.class.getResource("WebServer.class").toString().split("!")[0].replace("jar:file:/","");
    ProcessBuilder builder = new ProcessBuilder("java","-Djava.library.path="+nativePath,"-jar",jarPath,"reloaded");
    Map<String, String> envs = builder.environment();
    builder.redirectErrorStream(true); // This is the important part
    envs.put("LD_LIBRARY_PATH", nativePath);
    envs.put("DYLD_LIBRARY_PATH", nativePath);
    try {
      subProcess = builder.start();
      subProcess.waitFor();
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }

    //Pass all the io to the original shell
    Thread thread = new Thread(()->{
      BufferedReader reader = new BufferedReader(new InputStreamReader(subProcess.getInputStream()));
      while (true) {
        try {
          String input;
          while((input = reader.readLine()) != null){
            // Print the input
            screen.println(input);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
    thread.start();
  }
  public void startServer() {
    System.out.println("Starting Web Server");
    System.out.println(new JavaSMTConverter().simplify(Expression.constructExpression("1+1")));
    Spark.externalStaticFileLocation("app");
    Spark.port(5000);
    get("/bower_components/*", (req, res) -> String.join("\n",Files.readAllLines(Paths.get(req.pathInfo().substring(1)))));
    socket = new WebSocketServer();
  }
}
