package mc.util;

import com.google.common.base.Ascii;
import mc.Main;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Paths;

public class Utils {
    public static String getArch() {
        String arch = Ascii.toLowerCase(System.getProperty("os.arch"));
        String os = isWin()?"windows":(isMac()?"macosx":"linux");
        return arch + "-" + os;
    }
    public static boolean isWin() {
        return OSUtils.getOperatingSystemType() == OSUtils.OSType.Windows;
    }
    public static boolean isMac() {
        return OSUtils.getOperatingSystemType() == OSUtils.OSType.MacOS;
    }
    public static boolean isJavaw() {
        try {
            System.in.available();
            return false;
        } catch (IOException e) {
            // invalid handle in case of javaw
            return true;
        }
    }
    public static boolean isJar() {
        //getResource will add a jar: to the start of files inside jars.
        return Main.class.getResource("Main.class").toString().startsWith("jar");
    }
    public static String getJarPath() {
        if (!isJar()) throw new UnsupportedOperationException("The application currently is not running from a jar file.");
        try {
            return URLDecoder.decode(Main.class.getResource("Main.class").toString(),"UTF-8").split("!")[0].replace("jar:file:"+(Utils.isWin()?"/":""),"");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * Windows requires a .cmd appended to the node executables, linux does not.
     * @return ".cmd" on windows or an empty string.
     */
    public static String getNPMExtension() {
        if (isWin()) return ".cmd";
        return "";
    }

    public static String getNodeExtension() {
        if (isWin()) return ".exe";
        return "";
    }

    public static String getJavaExecutable() {
        String javaHome = System.getProperty("java.home");
        return Paths.get(javaHome,"bin",isJavaw() ? "javaw" : "java").toAbsolutePath().toString();
    }
}
