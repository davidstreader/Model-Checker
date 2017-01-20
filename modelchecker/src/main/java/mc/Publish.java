package mc;

import mc.webserver.NodeManager;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.commons.io.FileUtils;
import org.fusesource.jansi.AnsiConsole;
import org.sosy_lab.common.NativeLibraries;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Build all required applications and bundle it into a single package for deployment
 */
public class Publish extends Main{
  public Publish() {
    super();
    AnsiConsole.systemInstall();
    System.out.println(ansi().render("@|yellow Building gradle project|@"));
    String gradle = NativeLibraries.OS.guessOperatingSystem() == NativeLibraries.OS.WINDOWS?"gradlew.bat":"gradlew";
    ProcessBuilder builder = new ProcessBuilder(new File("modelchecker", gradle).getAbsolutePath(),"build","shadowJar");
    builder.directory(new File("modelchecker"));
    spawnProcess(builder);
    System.out.println(ansi().render("@|yellow Downloading bower dependencies|@"));
    //Load all the bower dependencies
    new NodeManager(this).initBower();
    try {
      FileUtils.deleteDirectory(new File("dist"));
      new File("dist").mkdir();
      new File("dist.zip").delete();
      System.out.println(ansi().render("@|yellow Copying jar|@"));
      Files.copy(Paths.get("ModelChecker.jar"),Paths.get("dist","ModelChecker.jar"));
      System.out.println(ansi().render("@|yellow Copying bower_components|@"));
      FileUtils.copyDirectory(new File("bower_components"), new File("dist","bower_components"));
      System.out.println(ansi().render("@|yellow Copying native|@"));
      FileUtils.copyDirectory(new File("native"), new File("dist","native"));
      System.out.println(ansi().render("@|yellow Copying app|@"));
      FileUtils.copyDirectory(new File("app"), new File("dist","app"));
      System.out.println(ansi().render("@|yellow Zipping up distribution|@"));
      ZipFile file = new ZipFile("dist.zip");
      file.createZipFileFromFolder("dist",new ZipParameters(),false,0);
      FileUtils.deleteDirectory(new File("dist"));
    } catch (IOException | ZipException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    new Publish();
  }
}
