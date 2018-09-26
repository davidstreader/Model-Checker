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


}
