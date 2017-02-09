package mc.compiler.expander;

import java.util.List;

import mc.compiler.Expander;
import mc.compiler.Lexer;
import mc.compiler.Parser;
import mc.compiler.TestBase;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ProcessNode;
import mc.compiler.token.Token;
import mc.exceptions.CompilationException;

public class ExpanderTests extends TestBase {

	private Lexer lexer = new Lexer();
	private Parser parser = new Parser();
	private Expander expander = new Expander();
	
	protected ProcessNode constructProcessNode(String code, int index){
		try{
			List<Token> tokens = lexer.tokenise(code);
			AbstractSyntaxTree ast = parser.parse(tokens);
			ast = expander.expand(ast);
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
            return expander.expand(ast).getProcesses();
        }catch(CompilationException e){
            e.printStackTrace();
        }

        return null;
    }
}
