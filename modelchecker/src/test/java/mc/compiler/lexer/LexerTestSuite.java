package mc.compiler.lexer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        ProcessTypeTests.class,
        KeywordTests.class,
        FunctionTests.class,
        OperatorTests.class,
        SymbolTests.class
})

public class LexerTestSuite {
}
