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
    JavaSMTConverter converter = new JavaSMTConverter();
    System.out.println(converter.simplify(Expression.constructExpression("$i>>$k==1")));
  }
}
