package mc.compiler.lexer;

import mc.compiler.token.ModuloToken;
import mc.compiler.token.ProcessTypeToken;
import mc.compiler.token.Token;
import mc.exceptions.LexerException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.fail;

/**
 * Created by sheriddavi on 13/02/17.
 */
public class ProcessTypeTests extends LexerTests {

    @Test
    public void tokeniseAutomataTest() throws LexerException {
        List<Token> tokens = tokenise("automata");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof ProcessTypeToken)){
            fail("expecting to parse a process type token");
        }

        ProcessTypeToken token = (ProcessTypeToken)tokens.get(0);

        // ensure that the token is an automata token
        if(!token.getProcessType().equals("automata")){
            fail("expecting process type token to be an \"automata\" token");
        }
    }

    @Test
    public void tokenisePetriNetTest() throws LexerException {
        List<Token> tokens = tokenise("petrinet");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof ProcessTypeToken)){
            fail("expecting to parse a process type token");
        }

        ProcessTypeToken token = (ProcessTypeToken)tokens.get(0);

        // ensure that the token is a petrinet token
        if(!token.getProcessType().equals("petrinet")){
            fail("expecting process type token to be an \"petrinet\" token");
        }
    }

}
