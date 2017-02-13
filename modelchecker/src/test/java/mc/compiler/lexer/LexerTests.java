package mc.compiler.lexer;

import mc.compiler.Lexer;
import mc.compiler.token.Token;
import mc.exceptions.LexerException;

import java.util.List;

/**
 * Created by sheriddavi on 13/02/17.
 */
public abstract class LexerTests {

    private Lexer lexer = new Lexer();

    protected List<Token> tokenise(String input) throws LexerException {
        return lexer.tokenise(input);
    }

}
