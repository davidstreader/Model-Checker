package mc;

import mc.compiler.OperationResult;
import mc.exceptions.CompilationException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

public class ScriptTests {
    @Test(timeout=1000)
    public void testScripts() throws IOException, CompilationException {
        for (File file:new File("tests").listFiles()) {
            System.out.println("Running script: "+file);
            if (file.getName().endsWith("results.txt") || !file.getName().endsWith("txt")) return;
            mc.compiler.Compiler compiler = new mc.compiler.Compiler();
            List<OperationResult> operations = Collections.emptyList();
            try {
                operations = compiler.compile(String.join("\n", Files.readAllLines(file.toPath()))).getOperationResults();
                if (shouldFail(file.getName())) {
                    fail("Test script: " + file.getName() + " should not compile!");
                }
            } catch (Exception ex) {
                if (!shouldFail(file.getName())) {
                    fail("Test script: " + file.getName() + " should compile!");
                } else {
                    ex.printStackTrace();
                }
            }

            if (operations.size() > 0) {
                for (OperationResult result : operations) {
                    String op = result.getProcess1().getIdent() + ' ' + result.getOperation() + ' ' + result.getProcess2().getIdent();
                    if (Objects.equals(result.getResult(), "notfound")) {
                        if (file.getName().endsWith("nonExistantOperation.txt")) {
                            continue;
                        }
                        System.out.println(op);
                        assertTrue(result.getProcess1().getIdent() + " does not exist", result.getProcess1().isExists());
                        assertTrue(result.getProcess2().getIdent() + " does not exist", result.getProcess1().isExists());
                        continue;
                    }
                    if (shouldFailOperations(file.getName(), op)) {
                        assertFalse("Operation '" + op + "' should fail", Objects.equals(result.getResult(), "false"));
                    } else {
                        assertTrue("Operation '" + op + "' should pass", Objects.equals(result.getResult(), "true"));
                    }

                }
            }
        }
    }
    private boolean shouldFail(String fileName) {
        if (fileName.contains("fail.txt")) return true;
        return false;
    }
    private boolean shouldFailOperations(String fileName, String op) {
        if (fileName.contains("failOperations.txt") && Objects.equals(op, "A ~ B")) return true;
        return false;
    }

}
