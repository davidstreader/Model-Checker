package mc.compiler;

import java.util.*;
import java.util.regex.Pattern;

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
            replaceReferences(processes.get(i));
		}

		return ast;
	}
	//We can use this to replace references after the initial ast is compiled.
    //Because of that it is public, and it should NOT be reset.
    public ProcessNode replaceReferences(ProcessNode process) throws CompilationException {
        references.clear();
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
        return process;
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
		String localReference = findLocalReference(identifier + "." + reference,localReferences);
		// check if the identifier is referencing a local process
		if(localReference != null){
			// check if this local process has been referenced before
			if(references.contains(localReference)){
				return new ReferenceNode(localReference, astNode.getLocation());
			}
			else{
				ASTNode node = localReferences.get(localReference).getProcess();
                addReference(node, localReference);

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

    /**
     * Search for a reference, swapping any undefined variables for regexps that match anything.
     * @param identifier The identifier to find
     * @param localReferences A list of all local references
     * @return The local reference key, or null if no match is found.
     */
    private String findLocalReference(String identifier, Map<String, LocalProcessNode> localReferences) {
        for (String key: localReferences.keySet()) {
            String testKey = key.replaceAll("\\$[a-z][a-zA-Z0-9_]*",".+").replace("[","\\[").replace("]","\\]");
            if (Pattern.compile(testKey).matcher(identifier).matches()) {
                return key;
            }
        }
        return null;
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
