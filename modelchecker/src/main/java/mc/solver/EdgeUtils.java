package mc.solver;

import com.google.common.base.Ascii;
import mc.util.expr.Expression;
import org.sosy_lab.common.NativeLibraries;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class allows you to solve guard information via an SMTSolver.
 */
public class EdgeUtils {
  public static void main(String[] args) {
    try {
      Field nativePath = NativeLibraries.class.getDeclaredField("nativePath");
      nativePath.setAccessible(true);
      nativePath.set(null,getNativeLibraryPath());
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
    JavaSMTConverter converter = new JavaSMTConverter();
    System.out.println((1 + 1 == 2) && (2 == 2));
    System.out.println(converter.convert(Expression.constructExpression("($i+1==2)&&($i==1)")));
    System.out.println(converter.simplify(Expression.constructExpression("($i+1==2)&&($i==1)")));
  }
  public static Path getNativeLibraryPath() {
    String arch = Ascii.toLowerCase(NativeLibraries.Architecture.guessVmArchitecture().name());
    String os = Ascii.toLowerCase(NativeLibraries.OS.guessOperatingSystem().name());
    return Paths.get("native", arch + "-" + os);
  }
}
