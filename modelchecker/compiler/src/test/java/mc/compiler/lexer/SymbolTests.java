package mc.compiler.lexer;

import mc.compiler.token.*;
import mc.exceptions.LexerException;
import org.junit.Test;

import java.util.List;

import static junit.framework.TestCase.fail;

/**
 * Created by sheriddavi on 13/02/17.
 */
public class SymbolTests extends LexerTests {

    @Test
    public void tokeniseAtTest() throws LexerException {
        List<Token> tokens = tokenise("@");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof AtToken)){
            fail("expecting to parse a \"@\" token");
        }
    }

    @Test
    public void tokeniseDollarTest() throws LexerException {
        List<Token> tokens = tokenise("$");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof DollarToken)){
            fail("expecting to parse a \"$\" token");
        }
    }

    @Test
    public void tokeniseOpenParenTest() throws LexerException {
        List<Token> tokens = tokenise("(");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof OpenParenToken)){
            fail("expecting to parse a \"(\" token");
        }
    }

    @Test
    public void tokeniseCloseParenTest() throws LexerException {
        List<Token> tokens = tokenise(")");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof CloseParenToken)){
            fail("expecting to parse a \")\" token");
        }
    }

    @Test
    public void tokeniseOpenBraceTest() throws LexerException {
        List<Token> tokens = tokenise("{");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof OpenBraceToken)){
            fail("expecting to parse a \"{\" token");
        }
    }

    @Test
    public void tokeniseCloseBraceTest() throws LexerException {
        List<Token> tokens = tokenise("}");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof CloseBraceToken)){
            fail("expecting to parse a \"}\" token");
        }
    }

    @Test
    public void tokeniseOpenBracketTest() throws LexerException {
        List<Token> tokens = tokenise("[");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof OpenBracketToken)){
            fail("expecting to parse a \"[\" token");
        }
    }

    @Test
    public void tokeniseCloseBracketTest() throws LexerException {
        List<Token> tokens = tokenise("]");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof CloseBracketToken)){
            fail("expecting to parse a \"]\" token");
        }
    }

    @Test
    public void tokeniseAssignTest() throws LexerException {
        List<Token> tokens = tokenise("=");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof AssignToken)){
            fail("expecting to parse a \"=\" token");
        }
    }

    @Test
    public void tokeniseColonTest() throws LexerException {
        List<Token> tokens = tokenise(":");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof ColonToken)){
            fail("expecting to parse a \":\" token");
        }
    }

    @Test
    public void tokeniseQuestionMarkTest() throws LexerException {
        List<Token> tokens = tokenise("?");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof QuestionMarkToken)){
            fail("expecting to parse a \"?\" token");
        }
    }

    @Test
    public void tokeniseCommaTest() throws LexerException {
        List<Token> tokens = tokenise(",");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof CommaToken)){
            fail("expecting to parse a \",\" token");
        }
    }

    @Test
    public void tokeniseDotTest() throws LexerException {
        List<Token> tokens = tokenise(".");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof DotToken)){
            fail("expecting to parse a \".\" token");
        }
    }

    @Test
    public void tokeniseHideTest() throws LexerException {
        List<Token> tokens = tokenise("\\");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof HideToken)){
            fail("expecting to parse a \"\\\" token");
        }
    }

    @Test
    public void tokeniseRangeSeparatorTest() throws LexerException {
        List<Token> tokens = tokenise("..");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof RangeSeparatorToken)){
            fail("expecting to parse a \"..\" token");
        }
    }

}
