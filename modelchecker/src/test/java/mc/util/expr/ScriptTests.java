package mc.util.expr;

import mc.compiler.OperationResult;
import mc.exceptions.CompilationException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScriptTests {
    @Test
    public void testScripts() throws IOException {
//        for (File file:new File("tests").listFiles()) {
//            if (file.getName().endsWith("results.txt") || !file.getName().endsWith("txt")) return;
//            List<OperationResult> operations = Collections.emptyList();
//            boolean failed = false;
//            try {
//                mc.compiler.Compiler compiler = new mc.compiler.Compiler();
//                operations = compiler.compile(String.join("\n",Files.readAllLines(file.toPath()))).getOperationResults();
//                failed = true;
//            } catch (CompilationException e) {
//                e.printStackTrace();
//            }
//            if (shouldFail(file.getName())) {
//                assertTrue("Test script: "+file.getName()+" should not compile!",failed);
//            } else {
//                assertFalse("Test script: "+file.getName()+" should compile!",failed);
//            }
//
//            if (operations.size() > 0) {
//                for (int i = 0; i < operations.size(); i++) {
//                    OperationResult result = operations.get(i);
//                    String op = result.getProcess1().getIdent() + ' ' + result.getOperation() + ' ' + result.getProcess2().getIdent();
//                    if (Objects.equals(result.getResult(), "notfound")) {
//                        if (file.getName().endsWith("nonExistantOperation.txt")) {
//                            continue;
//                        }
//                        System.out.println(op);
//                        assertTrue(result.getProcess1().getIdent()+" does not exist", result.getProcess1().isExists());
//                        assertTrue(result.getProcess2().getIdent()+" does not exist", result.getProcess1().isExists());
//                        continue;
//                    }
//                    if (shouldFailOperations(file.getName(),op)) {
//                        assertFalse("Operation '"+op+"' should fail",Objects.equals(result.getResult(), "false"));
//                    } else {
//                        assertTrue("Operation '"+op+"' should pass",Objects.equals(result.getResult(), "true"));
//                    }
//
//                }
//            }
//        }
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
