package mc.compiler;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import mc.compiler.tests.expander.ExpanderTestSuite;
import mc.compiler.tests.parser.ParserTestSuite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	ParserTestSuite.class,
	ExpanderTestSuite.class
})
public class CompilerTestSuite {

}
