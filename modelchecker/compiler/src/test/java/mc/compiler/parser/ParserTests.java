package mc.compiler.parser;

import mc.compiler.Lexer;
import mc.compiler.Parser;
import mc.compiler.TestBase;

/**
 * Created by sheriddavi on 1/02/17.
 */
abstract class ParserTests extends TestBase {

    private final Lexer lexer = new Lexer();
    private final Parser parser = new Parser();

    public ParserTests() throws InterruptedException {
    }


}
