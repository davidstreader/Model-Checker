package mc.compiler;

import mc.compiler.expander.ExpanderTestSuite;
import mc.compiler.lexer.LexerTestSuite;
import mc.compiler.parser.ParserTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        LexerTestSuite.class,
	    ParserTestSuite.class,
	    ExpanderTestSuite.class
})
public class CompilerTestSuite {

}
