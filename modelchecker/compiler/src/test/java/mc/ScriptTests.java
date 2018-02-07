package mc;

import com.microsoft.z3.Context;
import mc.compiler.OperationResult;
import mc.exceptions.CompilationException;
import mc.plugins.PluginManager;
import mc.util.PrintQueue;
import mc.util.expr.Expression;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
@RunWith(Parameterized.class)
public class ScriptTests {
    @BeforeClass
    public static void initialise(){
        PluginManager.getInstance().registerPlugins();
    }

    private final File file;
    public ScriptTests(String name, File file) {
        this.file = file;
    }
    @Test(timeout=100000)
    public void testScript() throws IOException, CompilationException, InterruptedException {
        if (file.getName().endsWith("results.txt") || !file.getName().endsWith("txt")) return;
        mc.compiler.Compiler compiler = new mc.compiler.Compiler();
        List<OperationResult> operations = Collections.emptyList();
        try (Context context = Expression.mkCtx()) {
            operations = compiler.compile(String.join("\n", Files.readAllLines(file.toPath())),context,new PrintQueue()).getOperationResults();
            if (shouldFail(file.getName()))
                fail("Test script: " + file.getName() + " should not compile!");

        } catch (Exception ex) {
            ex.printStackTrace();
            if (!shouldFail(file.getName())) {
                fail("Test script: " + file.getName() + " should compile! error: "+ex.getLocalizedMessage());
            }
        }


        if (operations.size() > 0) {
            for (OperationResult result : operations) {
                String op = result.getProcess1().getIdent() + ' ' + result.getOperation() + ' ' + result.getProcess2().getIdent();
                if (shouldFailOperations(file.getName())) {
                    assertTrue("Operation '" + op + "' should fail", Objects.equals(result.getResult(), "false"));
                } else {
                    assertTrue("Operation '" + op + "' should pass", Objects.equals(result.getResult(), "true"));
                }
            }

        }
    }
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        File[] testDirectoryFiles = new File("tests").listFiles();
        if(testDirectoryFiles != null)
            return Arrays.stream(testDirectoryFiles).map(s -> new Object[]{s.toString(),s}).collect(Collectors.toList());
        else
            return null;

    }
    private boolean shouldFail(String fileName) {
        return fileName.contains("_fail");

    }
    private boolean shouldFailOperations(String fileName) {
        return fileName.contains("_operationfail");
    }

}
