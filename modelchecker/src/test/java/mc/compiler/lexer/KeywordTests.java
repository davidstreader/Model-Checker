package mc.compiler.lexer;

import mc.compiler.token.*;
import mc.exceptions.LexerException;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.fail;

/**
 * Created by sheriddavi on 13/02/17.
 */
public class KeywordTests extends LexerTests {

    @Test
    public void tokeniseConstTest() throws LexerException {
        List<Token> tokens = tokenise("const");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof ConstToken)){
            fail("expecting to parse a \"const\" token");
        }
    }

    @Test
    public void tokeniseRangeTest() throws LexerException {
        List<Token> tokens = tokenise("range");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof RangeToken)){
            fail("expecting to parse a \"range\" token");
        }
    }

    @Test
    public void tokeniseSetTest() throws LexerException {
        List<Token> tokens = tokenise("set");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof SetToken)){
            fail("expecting to parse a \"set\" token");
        }
    }

    @Test
    public void tokeniseIfTest() throws LexerException {
        List<Token> tokens = tokenise("if");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof IfToken)){
            fail("expecting to parse a \"if\" token");
        }
    }

    @Test
    public void tokeniseThenTest() throws LexerException {
        List<Token> tokens = tokenise("then");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof ThenToken)){
            fail("expecting to parse a \"then\" token");
        }
    }

    @Test
    public void tokeniseElseTest() throws LexerException {
        List<Token> tokens = tokenise("else");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof ElseToken)){
            fail("expecting to parse a \"else\" token");
        }
    }

    @Test
    public void tokeniseWhenTest() throws LexerException {
        List<Token> tokens = tokenise("when");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof WhenToken)){
            fail("expecting to parse a \"when\" token");
        }
    }

    @Test
    public void tokeniseForAllTest() throws LexerException {
        List<Token> tokens = tokenise("forall");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof ForAllToken)){
            fail("expecting to parse a \"forall\" token");
        }
    }

    @Test
    public void tokeniseStopTest() throws LexerException {
        List<Token> tokens = tokenise("STOP");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof StopToken)){
            fail("expecting to parse a \"STOP\" token");
        }
    }

    @Test
    public void tokeniseErrorTest() throws LexerException {
        List<Token> tokens = tokenise("ERROR");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof ErrorToken)){
            fail("expecting to parse a \"ERROR\" token");
        }
    }
}
