package mc.compiler.reference_replacer;

import mc.compiler.*;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ProcessNode;
import mc.compiler.token.Token;
import mc.exceptions.CompilationException;

import java.util.List;

/**
 * Created by sheriddavi on 15/02/17.
 */
public class ReferenceReplacerTests extends TestBase {

    private Lexer lexer = new Lexer();
    private Parser parser = new Parser();
    private Expander expander = new Expander();
    private ReferenceReplacer replacer = new ReferenceReplacer();

    protected ProcessNode constructProcessNode(String code, int index){
        try{
            List<Token> tokens = lexer.tokenise(code);
            AbstractSyntaxTree ast = parser.parse(tokens);
            ast = expander.expand(ast);
            ast = replacer.replaceReferences(ast);
            return ast.getProcesses().get(index);
        }catch(CompilationException e){
            e.printStackTrace();
        }

        return null;
    }

    protected ProcessNode constructProcessNode(String code){
        return constructProcessNode(code, 0);
    }

    protected List<ProcessNode> constructProcessList(String code) {
        try{
            List<Token> tokens = lexer.tokenise(code);
            AbstractSyntaxTree ast = parser.parse(tokens);
            ast = expander.expand(ast);
            return replacer.replaceReferences(ast).getProcesses();
        }catch(CompilationException e){
            e.printStackTrace();
        }

        return null;
    }

}
