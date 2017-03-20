package mc;

import mc.compiler.OperationResult;
import mc.exceptions.CompilationException;
import mc.webserver.webobjects.Context;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

public class ScriptTests {
    @Test(timeout=100000)
    public void testScripts() throws IOException, CompilationException {
        for (File file:new File("tests").listFiles()) {
            System.out.println("Running script: "+file);
            System.out.flush();
            if (file.getName().endsWith("results.txt") || !file.getName().endsWith("txt")) return;
            mc.compiler.Compiler compiler = new mc.compiler.Compiler();
            List<OperationResult> operations = Collections.emptyList();
            try {
                operations = compiler.compile(String.join("\n", Files.readAllLines(file.toPath())),new Context()).getOperationResults();
                if (shouldFail(file.getName())) {
                    fail("Test script: " + file.getName() + " should not compile!");
                }
            } catch (Exception ex) {
                if (!shouldFail(file.getName())) {
                    fail("Test script: " + file.getName() + " should compile!");
                }
            }

            if (operations.size() > 0) {
                for (OperationResult result : operations) {
                    String op = result.getProcess1().getIdent() + ' ' + result.getOperation() + ' ' + result.getProcess2().getIdent();
                    if (shouldFailOperations(file.getName(), op)) {
                        assertTrue("Operation '" + op + "' should fail", Objects.equals(result.getResult(), "false"));
                    } else {
                        assertTrue("Operation '" + op + "' should pass", Objects.equals(result.getResult(), "true"));
                    }

                }
            }
        }
    }
    private boolean shouldFail(String fileName) {
        return fileName.contains("fail.txt") || fileName.contains("nonExistantOperation.txt");
    }
    private boolean shouldFailOperations(String fileName, String op) {
        return fileName.contains("failOperations.txt") && Objects.equals(removeANSIEscape(op), "A ~ B");
    }
    private String removeANSIEscape(String str) {
        return str.replaceAll("\u001B\\[[\\d;]*[^\\d;]","");
    }

}
