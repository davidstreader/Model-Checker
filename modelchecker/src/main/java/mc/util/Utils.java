package mc.util;

import com.google.common.base.Ascii;
import mc.Main;
import org.sosy_lab.common.NativeLibraries;

public class Utils {
  public static String getArch() {
    String arch = Ascii.toLowerCase(NativeLibraries.Architecture.guessVmArchitecture().name());
    String os = Ascii.toLowerCase(NativeLibraries.OS.guessOperatingSystem().name());
    return arch + "-" + os;
  }
  public static boolean isWin() {
    return NativeLibraries.OS.guessOperatingSystem() == NativeLibraries.OS.WINDOWS;
  }
  public static boolean isJar() {
    //getResource will add a jar: to the start of files inside jars.
    return Main.class.getResource("Main.class").toString().startsWith("jar");
  }
}
