package mc.compiler.parser;

import mc.compiler.Lexer;
import mc.compiler.Parser;
import mc.compiler.TestBase;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ProcessNode;
import mc.compiler.token.Token;
import mc.exceptions.CompilationException;

import java.util.List;

/**
 * Created by sheriddavi on 1/02/17.
 */
abstract class ParserTests extends TestBase {

    private final Lexer lexer = new Lexer();
    private final Parser parser = new Parser();

    public ParserTests() throws InterruptedException {
    }

    ProcessNode constructProcessNode(String code, int index) throws InterruptedException {
        try {
            List<Token> tokens = lexer.tokenise(code);
            AbstractSyntaxTree ast = parser.parse(tokens);
            return ast.getProcesses().get(index);
        } catch (CompilationException e) {
            e.printStackTrace();
        }

        return null;
    }

    ProcessNode constructProcessNode(String code) throws InterruptedException {
    	return constructProcessNode(code, 0);
    }

    List<ProcessNode> constructProcessList(String code) throws InterruptedException {
        try {
            List<Token> tokens = lexer.tokenise(code);
            AbstractSyntaxTree ast = parser.parse(tokens);
            return ast.getProcesses();
        } catch (CompilationException e) {
            e.printStackTrace();
        }

        return null;
    }
}
