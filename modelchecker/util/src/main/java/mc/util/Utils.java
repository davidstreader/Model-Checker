package mc.util;

import com.google.common.base.Ascii;

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
        return Utils.class.getResource("Utils.class").toString().startsWith("jar");
    }
    public static String getJarPath() {
        if (!isJar()) throw new UnsupportedOperationException("The application currently is not running from a jar file.");
        try {
            return URLDecoder.decode(Utils.class.getResource("Utils.class").toString(),"UTF-8").split("!")[0].replace("jar:file:"+(Utils.isWin()?"/":""),"");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <V> V instantiateClass(Class<V> clazz){
        V instance = null;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return instance;
    }
}
