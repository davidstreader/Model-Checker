package mc;

import mc.util.Utils;
import mc.webserver.DependencyManager;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.fusesource.jansi.AnsiConsole;

import java.io.File;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Build all required applications and bundle it into a single package for deployment
 */
public class Publish extends Main{
  private Publish() {
    super();

    AnsiConsole.systemInstall();
    //If we are running from the jar, its a bad idea to replace the jar we are running from.
    if (!Utils.isJar()) {
      System.out.println(ansi().render("@|yellow Building gradle project|@"));
      String gradle = Utils.isWin() ? "gradlew.bat" : "gradlew";
      ProcessBuilder builder = new ProcessBuilder(new File("modelchecker", gradle).getAbsolutePath(), "build", "shadowJar");
      builder.directory(new File("modelchecker"));
      spawnProcess(builder);
    }
    System.out.println(ansi().render("@|yellow Downloading bower dependencies|@"));
    //Load all the bower dependencies
    new DependencyManager(this).initBower();
    try {
      new File("dist.zip").delete();
      System.out.println(ansi().render("@|yellow Compressing distribution|@"));
      ZipFile file = new ZipFile("dist.zip");
      file.addFile(new File("ModelChecker.jar"),new ZipParameters());
      file.addFolder(new File("bower_components"),new ZipParameters());
      file.addFolder(new File("native"),new ZipParameters());
      file.addFolder(new File("app"),new ZipParameters());
    } catch (ZipException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    new Publish();
  }
}
