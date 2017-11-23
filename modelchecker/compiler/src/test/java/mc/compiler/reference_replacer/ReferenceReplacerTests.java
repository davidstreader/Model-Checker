package mc.compiler.reference_replacer;

import mc.compiler.*;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ProcessNode;
import mc.compiler.token.Token;
import mc.exceptions.CompilationException;
import mc.util.PrintQueue;

import java.util.List;

/**
 * Created by sheriddavi on 15/02/17.
 */
public class ReferenceReplacerTests extends TestBase {

    private final Lexer lexer = new Lexer();
    private final Parser parser = new Parser();
    private final Expander expander = new Expander();
    private final ReferenceReplacer replacer = new ReferenceReplacer();

    public ReferenceReplacerTests() throws InterruptedException {
    }

    ProcessNode constructProcessNode(String code) throws InterruptedException {
        try{
            List<Token> tokens = lexer.tokenise(code);
            AbstractSyntaxTree ast = parser.parse(tokens,context);
            ast = expander.expand(ast,new PrintQueue(),context);
            ast = replacer.replaceReferences(ast,new PrintQueue());
            return ast.getProcesses().get(0);
        }catch(CompilationException e){
            e.printStackTrace();
        }

        return null;
    }

    List<ProcessNode> constructProcessList(String code) throws InterruptedException {
        try{
            List<Token> tokens = lexer.tokenise(code);
            AbstractSyntaxTree ast = parser.parse(tokens,context);
            ast = expander.expand(ast,new PrintQueue(),context);
            return replacer.replaceReferences(ast,new PrintQueue()).getProcesses();
        }catch(CompilationException e){
            e.printStackTrace();
        }

        return null;
    }

}
