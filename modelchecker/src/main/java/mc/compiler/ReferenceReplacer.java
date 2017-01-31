package mc.compiler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mc.compiler.ast.*;
import mc.webserver.LogMessage;

public class ReferenceReplacer {

	private Set<String> globalReferences;
	private int referenceId;
	private Map<String, Integer> referenceMap;

	public ReferenceReplacer(){
        globalReferences = new HashSet<String>();
        referenceId = 0;
        referenceMap = new HashMap<String, Integer>();
	}

	public AbstractSyntaxTree replaceReferences(AbstractSyntaxTree ast){
		reset();

		List<ProcessNode> processes = ast.getProcesses();

		for(int i = 0; i < processes.size(); i++){
            referenceMap.clear();
			ProcessNode process = processes.get(i);
      new LogMessage("Replacing references:",process).send();

			String identifier = process.getIdentifier();
			globalReferences.add(identifier);
			int id = referenceId++;
			process.getProcess().setReferenceId(id);
			referenceMap.put(identifier, id);

			Map<String, LocalProcessNode> localReferences = new HashMap<String, LocalProcessNode>();
			List<LocalProcessNode> localProcesses = process.getLocalProcesses();
			for(int j = 0; j < localProcesses.size(); j++){
				String localIdentifier = localProcesses.get(j).getIdentifier();
				localReferences.put(localIdentifier, localProcesses.get(j));
			}

			ASTNode root = replaceReferences(process.getProcess(), identifier, localReferences);
			process.setProcess(root);
		}

		return ast;
	}

	private ASTNode replaceReferences(ASTNode astNode, String identifier, Map<String, LocalProcessNode> localReferences){
		if(astNode instanceof SequenceNode){
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

	private SequenceNode replaceReferences(SequenceNode astNode, String identifier, Map<String, LocalProcessNode> localReferences){
		ASTNode to = replaceReferences(astNode.getTo(), identifier, localReferences);
		astNode.setTo(to);
		return astNode;
	}

	private ChoiceNode replaceReferences(ChoiceNode astNode, String identifier, Map<String, LocalProcessNode> localReferences){
		ASTNode process1 = replaceReferences(astNode.getFirstProcess(), identifier, localReferences);
		ASTNode process2 = replaceReferences(astNode.getSecondProcess(), identifier, localReferences);
		astNode.setFirstProcess(process1);
		astNode.setSecondProcess(process2);
		return astNode;
	}

	private CompositeNode replaceReferences(CompositeNode astNode, String identifier, Map<String, LocalProcessNode> localReferences){
		ASTNode process1 = replaceReferences(astNode.getFirstProcess(), identifier, localReferences);
		ASTNode process2 = replaceReferences(astNode.getSecondProcess(), identifier, localReferences);
		astNode.setFirstProcess(process1);
		astNode.setSecondProcess(process2);
		return astNode;
	}

	private ASTNode replaceReferences(IdentifierNode astNode, String identifier, Map<String, LocalProcessNode> localReferences){
		String reference = astNode.getIdentifier();
		// check if the identifier is referencing a local process
		if(localReferences.containsKey(reference)){
			// check if this local process has been referenced before
			if(referenceMap.containsKey(reference)){
				return new ReferenceNode(referenceMap.get(reference), astNode.getLocation());
			}
			else{
				ASTNode node = localReferences.get(reference).getProcess();
				int id = referenceId++;
				node.setReferenceId(id);
				referenceMap.put(reference, id);
				return replaceReferences(node, identifier, localReferences);
			}
		}
		// check if the identifier is referencing itself
		else if(reference.equals(identifier)){
			return new ReferenceNode(referenceMap.get(identifier), astNode.getLocation());
		}
		// check if the identifier is referencing a global process
		else if(globalReferences.contains(reference)){
			return astNode;
		}

		// TODO: should throw an error, identifier has not been defined either locally or globally
		return astNode;
	}

	private FunctionNode replaceReferences(FunctionNode astNode, String identifier, Map<String, LocalProcessNode> localReferences){
		ASTNode process = replaceReferences(astNode.getProcess(), identifier, localReferences);
		astNode.setProcess(process);
		return astNode;
	}

	private void reset() {
		globalReferences.clear();
		referenceId = 0;
		referenceMap.clear();
	}
}
