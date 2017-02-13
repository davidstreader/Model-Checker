package mc.compiler.lexer;

import com.sun.org.apache.bcel.internal.generic.LXOR;
import mc.compiler.token.*;
import mc.exceptions.LexerException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.fail;

/**
 * Created by sheriddavi on 13/02/17.
 */
public class OperatorTests extends LexerTests {

    @Test
    public void tokeniseOrTest() throws LexerException {
        List<Token> tokens = tokenise("||");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof OrToken)){
            fail("expecting to parse a \"||\" token");
        }
    }

    @Test
    public void tokeniseAndTest() throws LexerException {
        List<Token> tokens = tokenise("&&");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof AndToken)){
            fail("expecting to parse a \"&&\" token");
        }
    }

    @Test
    public void tokeniseBitOrTest() throws LexerException {
        List<Token> tokens = tokenise("|");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof BitOrToken)){
            fail("expecting to parse a \"|\" token");
        }
    }

    @Test
    public void tokeniseExclOrTest() throws LexerException {
        List<Token> tokens = tokenise("^");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof ExclOrToken)){
            fail("expecting to parse a \"^\" token");
        }
    }

    @Test
    public void tokeniseBitAndTest() throws LexerException {
        List<Token> tokens = tokenise("&");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof BitAndToken)){
            fail("expecting to parse a \"&\" token");
        }
    }

    @Test
    public void tokeniseEqualityTest() throws LexerException {
        List<Token> tokens = tokenise("==");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof EqualityToken)){
            fail("expecting to parse a \"==\" token");
        }
    }

    @Test
    public void tokeniseNotEqualTest() throws LexerException {
        List<Token> tokens = tokenise("!=");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof NotEqualToken)){
            fail("expecting to parse a \"!=\" token");
        }
    }

    @Test
    public void tokeniseLessThanTest() throws LexerException {
        List<Token> tokens = tokenise("<");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof LessThanToken)){
            fail("expecting to parse a \"<\" token");
        }
    }

    @Test
    public void tokeniseLessThanEqTest() throws LexerException {
        List<Token> tokens = tokenise("<=");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof LessThanEqToken)){
            fail("expecting to parse a \"<=\" token");
        }
    }

    @Test
    public void tokeniseGreaterThanTest() throws LexerException {
        List<Token> tokens = tokenise(">");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof GreaterThanToken)){
            fail("expecting to parse a \">\" token");
        }
    }

    @Test
    public void tokeniseGreaterThanEqTest() throws LexerException {
        List<Token> tokens = tokenise(">=");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof GreaterThanEqToken)){
            fail("expecting to parse a \">=\" token");
        }
    }

    @Test
    public void tokeniseLeftShiftTest() throws LexerException {
        List<Token> tokens = tokenise("<<");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof LeftShiftToken)){
            fail("expecting to parse a \"<<\" token");
        }
    }

    @Test
    public void tokeniseRightShiftTest() throws LexerException {
        List<Token> tokens = tokenise(">>");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof RightShiftToken)){
            fail("expecting to parse a \">>\" token");
        }
    }

    @Test
    public void tokeniseAdditionTest() throws LexerException {
        List<Token> tokens = tokenise("+");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof AdditionToken)){
            fail("expecting to parse a \"+\" token");
        }
    }

    @Test
    public void tokeniseSubtractionTest() throws LexerException {
        List<Token> tokens = tokenise("-");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof SubtractionToken)){
            fail("expecting to parse a \"-\" token");
        }
    }

    @Test
    public void tokeniseMultiplicationTest() throws LexerException {
        List<Token> tokens = tokenise("*");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof MultiplicationToken)){
            fail("expecting to parse a \"*\" token");
        }
    }

    @Test
    public void tokeniseDivisionTest() throws LexerException {
        List<Token> tokens = tokenise("/");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof DivisionToken)){
            fail("expecting to parse a \"/\" token");
        }
    }

    @Test
    public void tokeniseModuloTest() throws LexerException {
        List<Token> tokens = tokenise("%");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof ModuloToken)){
            fail("expecting to parse a \"%\" token");
        }
    }

    @Test
    public void tokeniseNegateTest() throws LexerException {
        List<Token> tokens = tokenise("!");

        // there should only be one token
        if(tokens.size() != 1){
            fail("expecting there to be only one token");
        }

        // ensure the correct token was constructed
        if(!(tokens.get(0) instanceof NegateToken)){
            fail("expecting to parse a \"!\" token");
        }
    }

}
