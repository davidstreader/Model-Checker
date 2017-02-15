package mc.compiler;

import java.util.*;

import mc.compiler.ast.*;
import mc.exceptions.CompilationException;
import mc.webserver.LogMessage;

public class ReferenceReplacer {

	private Set<String> globalReferences;
	private Set<String> references;

	public ReferenceReplacer(){
        globalReferences = new HashSet<String>();
        references = new HashSet<String>();
    }

	public AbstractSyntaxTree replaceReferences(AbstractSyntaxTree ast) throws CompilationException {
		reset();

		List<ProcessNode> processes = ast.getProcesses();

		for(int i = 0; i < processes.size(); i++){
            references.clear();
			ProcessNode process = processes.get(i);

            new LogMessage("Replacing references:",process).send();

			String identifier = process.getIdentifier();
            addReference(process.getProcess(), identifier);

			Map<String, LocalProcessNode> localReferences = new HashMap<String, LocalProcessNode>();
			List<LocalProcessNode> localProcesses = process.getLocalProcesses();
			for(int j = 0; j < localProcesses.size(); j++){
				String localIdentifier = identifier + "." + localProcesses.get(j).getIdentifier();
				localReferences.put(localIdentifier, localProcesses.get(j));
			}

			ASTNode root = replaceReferences(process.getProcess(), identifier, localReferences);
			process.setProcess(root);
            process.setLocalProcesses(new ArrayList<LocalProcessNode>());
		}

		return ast;
	}

	private ASTNode replaceReferences(ASTNode astNode, String identifier, Map<String, LocalProcessNode> localReferences) throws CompilationException {
		if(astNode instanceof ProcessRootNode){
            return replaceReferences((ProcessRootNode)astNode, identifier, localReferences);
        }
        else if(astNode instanceof SequenceNode){
			return replaceReferences((SequenceNode)astNode, identifier, localReferences);
		}
		else if(astNode instanceof ChoiceNode){
			return replaceReferences((ChoiceNode)astNode, identifier, localReferences);
		}
		else if(astNode instanceof CompositeNode){
			return replaceReferences((CompositeNode)astNode, identifier, localReferences);
		}
		else if(astNode instanceof IdentifierNode){
			return replaceReferences((IdentifierNode)astNode, identifier, localReferences);
		}
		else if(astNode instanceof FunctionNode){
			return replaceReferences((FunctionNode)astNode, identifier, localReferences);
		}

		return astNode;
	}

    private ProcessRootNode replaceReferences(ProcessRootNode astNode, String identifier, Map<String, LocalProcessNode> localReferences) throws CompilationException {
        ASTNode process = replaceReferences(astNode.getProcess(), identifier, localReferences);
        astNode.setProcess(process);
        return astNode;
    }

	private SequenceNode replaceReferences(SequenceNode astNode, String identifier, Map<String, LocalProcessNode> localReferences) throws CompilationException {
		ASTNode to = replaceReferences(astNode.getTo(), identifier, localReferences);
		astNode.setTo(to);
		return astNode;
	}

	private ChoiceNode replaceReferences(ChoiceNode astNode, String identifier, Map<String, LocalProcessNode> localReferences) throws CompilationException {
		ASTNode process1 = replaceReferences(astNode.getFirstProcess(), identifier, localReferences);
		ASTNode process2 = replaceReferences(astNode.getSecondProcess(), identifier, localReferences);
		astNode.setFirstProcess(process1);
		astNode.setSecondProcess(process2);
		return astNode;
	}

	private CompositeNode replaceReferences(CompositeNode astNode, String identifier, Map<String, LocalProcessNode> localReferences) throws CompilationException {
		ASTNode process1 = replaceReferences(astNode.getFirstProcess(), identifier, localReferences);
		ASTNode process2 = replaceReferences(astNode.getSecondProcess(), identifier, localReferences);
		astNode.setFirstProcess(process1);
		astNode.setSecondProcess(process2);
		return astNode;
	}

	private ASTNode replaceReferences(IdentifierNode astNode, String identifier, Map<String, LocalProcessNode> localReferences) throws CompilationException {
		String reference = astNode.getIdentifier();
		// check if the identifier is referencing a local process
		if(localReferences.containsKey(identifier + "." + reference)){
			// check if this local process has been referenced before
			if(references.contains(identifier + "." + reference)){
				return new ReferenceNode(identifier + "." + reference, astNode.getLocation());
			}
			else{
				ASTNode node = localReferences.get(identifier + "." + reference).getProcess();
				String ident = identifier + "." + reference;
                addReference(node, ident);

                if(astNode.hasReferences()){
                    for(String r : astNode.getReferences()){
                        node.addReference(r);
                    }
                }

				return replaceReferences(node, identifier, localReferences);
			}
		}
		// check if the identifier is referencing itself
		else if(reference.equals(identifier)){
			return new ReferenceNode(identifier, astNode.getLocation());
		}
		// check if the identifier is referencing a global process
		else if(globalReferences.contains(reference)){
			return astNode;
		}

        throw new CompilationException(getClass(),"Unable to find reference for node: "+reference,astNode.getLocation());
	}

	private FunctionNode replaceReferences(FunctionNode astNode, String identifier, Map<String, LocalProcessNode> localReferences) throws CompilationException {
		ASTNode process = replaceReferences(astNode.getProcess(), identifier, localReferences);
		astNode.setProcess(process);
		return astNode;
	}

    private void addReference(ASTNode process, String identifier){
        globalReferences.add(identifier);
        while(process instanceof ProcessRootNode){
            process = ((ProcessRootNode)process).getProcess();
        }

        process.addReference(identifier);
        references.add(identifier);
    }

	private void reset() {
		globalReferences.clear();
		references.clear();
	}
}
