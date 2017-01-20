package mc.util.expr;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        AdditionTests.class,
        BooleanTests.class,
        MultiplicationTests.class,
        NegationTests.class,
        ShiftTests.class
})

/**
 * Created by sheriddavi on 20/01/17.
 */
public class ExpressionTestSuite {
}
