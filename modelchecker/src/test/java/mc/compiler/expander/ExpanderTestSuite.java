package mc.compiler.expander;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	    RangeIndexTests.class,
	    SetIndexTests.class,
	    ForAllTests.class,
	    SequenceTests.class,
        ExampleTests.class
})

public class ExpanderTestSuite {

}
