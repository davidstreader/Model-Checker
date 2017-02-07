package mc.compiler.parser;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        SequenceTests.class,
        IdentifierTests.class,
        FunctionTests.class,
        IndexTests.class,
        RootTests.class
})

/**
 * Created by sheriddavi on 2/02/17.
 */
public class ParserTestSuite {
}
