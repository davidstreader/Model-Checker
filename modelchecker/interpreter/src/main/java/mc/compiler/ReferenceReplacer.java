package mc.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;
import mc.compiler.ast.ASTNode;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ChoiceNode;
import mc.compiler.ast.CompositeNode;
import mc.compiler.ast.FunctionNode;
import mc.compiler.ast.IdentifierNode;
import mc.compiler.ast.LocalProcessNode;
import mc.compiler.ast.ProcessNode;
import mc.compiler.ast.ProcessRootNode;
import mc.compiler.ast.ReferenceNode;
import mc.compiler.ast.SequenceNode;
import mc.compiler.interpreters.PetrinetInterpreter;
import mc.exceptions.CompilationException;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.util.LogAST;

public class ReferenceReplacer {
  private static String indent = "";
  private Set<String> globalReferences;
  private Set<String> references;
  private Set<String> globalRequirements;

  /** replacer.replaceReferences  LOCAL Referances NOT Global Referances
   * Expands references i.e Initally we are now at: P1 = a->P2,
   *                                                P2 = b->c->x.
   *  Then it expands it to, P1 = a->b->c->x. If it needs it
   *
   * TODO NOTE currenlty we assume single Place markings to extend to any Marking each reference
   * would need to be unique at the point of adding the reference. Henc allowing us to distinguish
   * two nodes in same marking from two nodes in different markings
   */
  public ReferenceReplacer() {
    globalReferences = new HashSet<>();
    references = new HashSet<>();
    globalRequirements = new HashSet<>();
  }

  public AbstractSyntaxTree replaceReferences(AbstractSyntaxTree ast, BlockingQueue<Object> messageQueue)
    throws CompilationException, InterruptedException {
    reset();

    List<ProcessNode> processes = ast.getProcesses();
    if (ast.getProcessHierarchy() == null) {
      ast.setProcessHierarchy(new ProcessHierarchy());
    }

    for (ProcessNode process : processes) {
      globalRequirements.clear();
      replaceReferences(process, messageQueue);
      ast.getProcessHierarchy().getDependencies().
        putAll(process.getIdentifier(), globalRequirements);
    }

    return ast;
  }

  //We can use this to replace references after the initial ast is compiled.
  //Because of that it is public, and it should NOT be reset.
  private ProcessNode replaceReferences(ProcessNode process, BlockingQueue<Object> messageQueue) throws CompilationException, InterruptedException {
    references.clear();
//System.out.println("START Process Replacing ");
    //messageQueue.add(new LogAST("Replacing references:", process));
    String identifier = process.getIdentifier();
    addReference(process.getProcess(), identifier);

    Map<String, LocalProcessNode> localReferences = new HashMap<>();
    List<LocalProcessNode> localProcesses = process.getLocalProcesses();
    for (LocalProcessNode localProcess : localProcesses) {
      String localIdentifier = identifier + "." + localProcess.getIdentifier();
//System.out.println("Replacing "+ localIdentifier);
      localReferences.put(localIdentifier, localProcess);
    }

    ASTNode root = replaceReferences(process.getProcess(), identifier, localReferences);
    process.setProcess(root);
    process.setLocalProcesses(new ArrayList<>());
    return process;
  }

  private ASTNode replaceReferences(ASTNode astNode,
                                    String identifier,
                                    Map<String, LocalProcessNode> localReferences)
    throws CompilationException, InterruptedException {

    if (Thread.currentThread().isInterrupted()) {
      throw new InterruptedException();
    }
    // Debugging do not remove
/*    ReferenceReplacer.indent = ReferenceReplacer.indent.concat("-");
    String className = astNode.getClass().getSimpleName();
    System.out.println("RRast "+ ReferenceReplacer.indent + className+
      " refs "+astNode.getReferences()+" from "+astNode.getFromReferences()); */

    if (astNode instanceof ProcessRootNode) {
      astNode = replaceReferences((ProcessRootNode) astNode, identifier, localReferences);
    } else if (astNode instanceof SequenceNode) {
      astNode = replaceReferences((SequenceNode) astNode, identifier, localReferences);
    } else if (astNode instanceof ChoiceNode) {
      astNode = replaceReferences((ChoiceNode) astNode, identifier, localReferences);
    } else if (astNode instanceof CompositeNode) {
      astNode = replaceReferences((CompositeNode) astNode, identifier, localReferences);
    } else if (astNode instanceof IdentifierNode) {
      //System.out.println("Pingo RR I");
      astNode = replaceReferences((IdentifierNode) astNode, identifier, localReferences);
      //System.out.println("Ident  refs "+astNode.getReferences());

    } else if (astNode instanceof FunctionNode) {
      astNode = replaceReferences((FunctionNode) astNode, identifier, localReferences);
    }

    //Debugging do not remove
 /*   if (ReferenceReplacer.indent.length()> 1)
      ReferenceReplacer.indent = ReferenceReplacer.indent.substring(1);
    System.out.println("RRast<"+ ReferenceReplacer.indent + className+" "+astNode.getReferences());
*/
    return astNode;
  }

  private ProcessRootNode replaceReferences(ProcessRootNode astNode, String identifier, Map<String, LocalProcessNode> localReferences) throws CompilationException, InterruptedException {
    ASTNode process = replaceReferences(astNode.getProcess(), identifier, localReferences);
    astNode.setProcess(process);
    return astNode;
  }

  private SequenceNode replaceReferences(SequenceNode astNode, String identifier, Map<String, LocalProcessNode> localReferences) throws CompilationException, InterruptedException {
    ASTNode to = replaceReferences(astNode.getTo(), identifier, localReferences);
    astNode.setTo(to);
    return astNode;
  }

  private ChoiceNode replaceReferences(ChoiceNode astNode, String identifier, Map<String, LocalProcessNode> localReferences) throws CompilationException, InterruptedException {
    ASTNode process1 = replaceReferences(astNode.getFirstProcess(), identifier, localReferences);
    ASTNode process2 = replaceReferences(astNode.getSecondProcess(), identifier, localReferences);
    astNode.setFirstProcess(process1);
    astNode.setSecondProcess(process2);
    return astNode;
  }

  private CompositeNode replaceReferences(CompositeNode astNode, String identifier, Map<String, LocalProcessNode> localReferences) throws CompilationException, InterruptedException {
    ASTNode process1 = replaceReferences(astNode.getFirstProcess(), identifier, localReferences);
    ASTNode process2 = replaceReferences(astNode.getSecondProcess(), identifier, localReferences);
    astNode.setFirstProcess(process1);
    astNode.setSecondProcess(process2);
    return astNode;
  }

  private ASTNode replaceReferences(IdentifierNode astNode, String identifier,
                                    Map<String, LocalProcessNode> localReferences) throws CompilationException, InterruptedException {
    //System.out.println("RR IdentifierNode "+ astNode.getIdentifier());
    String reference = astNode.getIdentifier();
    String localReference = findLocalReference(identifier + "." + reference, localReferences);
    // check if the identifier is referencing a local process
    if (localReference != null && localReference.length()>0) {
      // check if this local process has been referenced before
      if (references.contains(localReference)) {
        //this is where identifier is used
        //System.out.println("OLD ping "+localReference);
        return new ReferenceNode(localReference, astNode.getLocation());
      } else {
        ASTNode node = localReferences.get(localReference).getProcess();
        addReference(node, localReference);

        if (astNode.hasReferences()) {
          astNode.getReferences().forEach(node::addReference);
        }
        //this is where identifier is defined
        //System.out.println("New ping "+localReference);
        return replaceReferences(node, identifier, localReferences);
      }
    }
    // check if the identifier is referencing itself
    else if (reference.equals(identifier)) {
      astNode.addFromReference(identifier);
      return new ReferenceNode(identifier, astNode.getLocation());
    }
    // check if the identifier is referencing a global process
    else if (globalReferences.contains(reference)) {
      globalRequirements.add(reference);
      astNode.addFromReference(identifier);
      return astNode;
    }
    throw new CompilationException(getClass(), "Unable to find reference for node: " + reference, astNode.getLocation());
  }

  /**
   * Search for a reference, swapping any undefined variables for regexps that match anything.
   *
   * @param identifier      The identifier to find
   * @param localReferences A list of all local references
   * @return The local reference key, or null if no match is found.
   */
  private String findLocalReference(String identifier, Map<String, LocalProcessNode> localReferences) {
    for (String key : localReferences.keySet()) {
      String testKey = key.replaceAll("\\$[a-z][a-zA-Z0-9_]*", ".+").replace("[", "\\[").replace("]", "\\]");
      if (Pattern.compile(testKey).matcher(identifier).matches()) {
        return key;
      }
    }
    return null;
  }

  private FunctionNode replaceReferences(FunctionNode astNode, String identifier, Map<String, LocalProcessNode> localReferences) throws CompilationException, InterruptedException {
    for (int i = 0; i < astNode.getProcesses().size(); i++) {
      ASTNode process = replaceReferences(astNode.getProcesses().get(i), identifier, localReferences);
      astNode.getProcesses().set(i, process);
    }
    return astNode;
  }

  private void addReference(ASTNode process, String identifier) {
    globalReferences.add(identifier);
    while (process instanceof ProcessRootNode) {
      process = ((ProcessRootNode) process).getProcess();
    }

    process.addReference(identifier);
    references.add(identifier);
  }

  private void reset() {
    globalReferences.clear();
    references.clear();
    globalRequirements.clear();
  }
}
