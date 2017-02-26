package mc.commands;

import mc.compiler.OperationResult;
import org.fusesource.jansi.Ansi;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TestCommand implements Command {
    @Override
    public void run(String[] args) {
        File f = new File(String.join(" ",args));
        if (!f.isDirectory()) {
            System.out.println(Ansi.ansi().render("@|red ERROR: Expected a directory|@"));
            return;
        }
        mc.compiler.Compiler compiler = new mc.compiler.Compiler();
        for (File file:f.listFiles()) {
            System.out.println(Ansi.ansi().render("Testing script: "+file));
            if (file.getName().endsWith("results.txt") || !file.getName().endsWith("txt")) return;

            List<OperationResult> operations = Collections.emptyList();
            try {
                operations = compiler.compile(String.join("\n", Files.readAllLines(file.toPath()))).getOperationResults();
                System.out.println(Ansi.ansi().render("File @|yellow `"+file.getName()+"`|@: @|green COMPILED |@"));
            } catch (Exception ex) {
                System.out.println(Ansi.ansi().render("File @|yellow `"+file.getName()+"`|@: @|red FAILED |@"));
                System.out.println(Ansi.ansi().render("Reason: Timeout"));
            }
            if (operations.size() > 0) {
                for (OperationResult result : operations) {
                    String op = result.getProcess1().getIdent() + ' ' + result.getOperation() + ' ' + result.getProcess2().getIdent();
                    if (Objects.equals(result.getResult(), "false")) {
                        System.out.println(Ansi.ansi().render("Operation @|yellow `"+op+"|@ @|green PASSED @|"));
                    } else {
                        System.out.println(Ansi.ansi().render("Operation @|yellow `"+op+"`|@ @|green FAILED @|"));
                    }

                }
            }
        }

    }
}
