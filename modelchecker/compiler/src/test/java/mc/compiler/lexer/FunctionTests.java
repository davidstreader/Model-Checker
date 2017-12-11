package mc.compiler.lexer;

import mc.compiler.token.FunctionToken;
import mc.compiler.token.Token;
import mc.exceptions.LexerException;
import mc.plugins.PluginManager;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.fail;

public class FunctionTests extends LexerTests {
    @BeforeClass
    public static void initialise(){
        PluginManager.getInstance().registerPlugins();
    }

    @Test
    public void tokeniseAbsTest() throws LexerException {
        List<Token> tokens = tokenise("abs");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof FunctionToken)){
            fail("expecting to parse a function token");
        }

        FunctionToken token = (FunctionToken)tokens.get(0);

        // ensure that the token is an abs token
        if(!token.getFunction().equals("abs")){
            fail("expecting process type token to be an \"abs\" token");
        }
    }

    @Test
    public void tokeniseSimpTest() throws LexerException {
        List<Token> tokens = tokenise("simp");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof FunctionToken)){
            fail("expecting to parse a function token");
        }

        FunctionToken token = (FunctionToken)tokens.get(0);

        // ensure that the token is a simp token
        if(!token.getFunction().equals("simp")){
            fail("expecting process type token to be an \"simp\" token");
        }
    }

    @Test
    public void tokenisePruneTest() throws LexerException {
        List<Token> tokens = tokenise("prune");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof FunctionToken)){
            fail("expecting to parse a function token");
        }

        FunctionToken token = (FunctionToken)tokens.get(0);

        // ensure that the token is a prune token
        if(!token.getFunction().equals("prune")){
            fail("expecting process type token to be an \"prune\" token");
        }
    }

    @Test
    public void tokeniseSafeTest() throws LexerException {
        List<Token> tokens = tokenise("safe");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof FunctionToken)){
            fail("expecting to parse a function token");
        }

        FunctionToken token = (FunctionToken)tokens.get(0);

        // ensure that the token is a safe token
        if(!token.getFunction().equals("safe")){
            fail("expecting process type token to be an \"safe\" token");
        }
    }
}
