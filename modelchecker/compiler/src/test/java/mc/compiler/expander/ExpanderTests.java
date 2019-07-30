package mc.compiler.expander;

import mc.compiler.Expander;
import mc.compiler.Lexer;
import mc.compiler.Parser;
import mc.compiler.TestBase;
import mc.plugins.PluginManager;
import org.junit.BeforeClass;

public class ExpanderTests extends TestBase {

    @BeforeClass
    public static void initialise(){
        PluginManager.getInstance().registerPlugins();
    }

    private final Lexer lexer = new Lexer();
	private final Parser parser = new Parser();
	private final Expander expander = new Expander();
    public ExpanderTests() throws InterruptedException {
    }

}
