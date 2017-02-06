package mc.util.expr;

import mc.Main;
import mc.solver.ExpressionSimplifier;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
    AdditionTests.class,
    BooleanTests.class,
    MultiplicationTests.class,
    NegationTests.class,
    ShiftTests.class,
    ScriptTests.class
})

public class ExpressionTestSuite {

}
