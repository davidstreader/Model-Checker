package mc.compiler.expander;

import mc.compiler.Expander;
import mc.compiler.Lexer;
import mc.compiler.Parser;
import mc.compiler.TestBase;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ProcessNode;
import mc.compiler.token.Token;
import mc.exceptions.CompilationException;
import mc.plugins.PluginManager;
import mc.util.PrintQueue;
import org.junit.BeforeClass;

import java.util.List;

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
