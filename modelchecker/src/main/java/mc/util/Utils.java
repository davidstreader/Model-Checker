package mc.util;

import com.google.common.base.Ascii;
import mc.Main;
import org.sosy_lab.common.NativeLibraries;

public class Utils {
  @SuppressWarnings("deprecation")
  public static String getArch() {
    String arch = Ascii.toLowerCase(NativeLibraries.Architecture.guessVmArchitecture().name());
    String os = Ascii.toLowerCase(NativeLibraries.OS.guessOperatingSystem().name());
    return arch + "-" + os;
  }
  @SuppressWarnings("deprecation")
  public static boolean isWin() {
    return NativeLibraries.OS.guessOperatingSystem() == NativeLibraries.OS.WINDOWS;
  }
  public static boolean isJar() {
    //getResource will add a jar: to the start of files inside jars.
    return Main.class.getResource("Main.class").toString().startsWith("jar");
  }
  public static String getJarPath() {
    if (!isJar()) throw new UnsupportedOperationException("The application currently is not running from a jar file.");
    return Main.class.getResource("Main.class").toString().split("!")[0].replace("jar:file:"+(Utils.isWin()?"/":""),"");
  }
  /**
   * Windows requires a .cmd appended to the node executables, linux does not.
   * @return ".cmd" on windows or an empty string.
   */
  public static String getNodeExtension() {
    if (isWin()) return ".cmd";
    return "";
  }
}
