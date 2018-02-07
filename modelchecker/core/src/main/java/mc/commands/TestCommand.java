package mc.commands;

import com.microsoft.z3.Context;
import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import mc.compiler.OperationResult;
import mc.util.PrintQueue;
import mc.util.expr.Expression;

public class TestCommand implements Command {
  @Override
  public void run(String[] args) {
    File f = new File(String.join(" ", args));
    if (!f.isDirectory()) {
      System.out.println("ERROR: Expected a directory");
      return;
    }
    mc.compiler.Compiler compiler = null;
    try {
      compiler = new mc.compiler.Compiler();
    } catch (InterruptedException e) {
      e.printStackTrace();
      return;
    }
    for (File file : f.listFiles()) {
      System.out.println("Testing script: `" + file + "`");
      if (file.getName().endsWith("results.txt") || !file.getName().endsWith("txt")) {
        return;
      }

      List<OperationResult> operations = Collections.emptyList();
      try (Context z3Context = Expression.mkCtx()) {
        operations = compiler.compile(String.join("\n", Files.readAllLines(file.toPath())), z3Context, new PrintQueue()).getOperationResults();
        System.out.println("File `" + file.getName() + "`: COMPILED ");
      } catch (Exception ex) {
        System.out.println("File `" + file.getName() + "`: FAILED");
        System.out.println("Reason: " + ex.getMessage());
      }
      if (operations.size() > 0) {
        for (OperationResult result : operations) {
          String op = result.getProcess1().getIdent() + ' ' + result.getOperation() + ' ' + result.getProcess2().getIdent();
          if (Objects.equals(result.getResult(), "true")) {
            System.out.println("Operation `" + op + " PASSED ");
          } else {
            System.out.println("Operation `" + op + "` FAILED");
          }

        }
      }
    }

  }
}
