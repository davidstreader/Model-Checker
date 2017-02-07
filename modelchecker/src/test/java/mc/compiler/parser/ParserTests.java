package mc.compiler.parser;

import mc.compiler.Lexer;
import mc.compiler.Parser;
import mc.compiler.TestBase;
import mc.compiler.ast.*;
import mc.compiler.token.Token;
import mc.exceptions.CompilationException;

import java.util.List;

/**
 * Created by sheriddavi on 1/02/17.
 */
public abstract class ParserTests extends TestBase {

    private Lexer lexer = new Lexer();
    private Parser parser = new Parser();

    protected ProcessNode constructProcessNode(String code, int index){
        try {
            List<Token> tokens = lexer.tokenise(code);
            AbstractSyntaxTree ast = parser.parse(tokens);
            return ast.getProcesses().get(index);
        } catch (CompilationException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    protected ProcessNode constructProcessNode(String code){
    	return constructProcessNode(code, 0);
    }
}
