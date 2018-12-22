package mc.compiler;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;

import com.microsoft.z3.BoolExpr;
import mc.compiler.ast.*;
import mc.compiler.interpreters.PetrinetInterpreter;
import mc.exceptions.CompilationException;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.util.LogAST;
//import sun.tools.tree.IfStatement;

public class ReferenceReplacer {
  private static String indent = "";
  private Set<String> globalReferences;
  private Set<String> references;
  private Set<String> globalRequirements;

  private List<String> bits = new ArrayList<>();  // store newly replaced bits for addition to edge

  /**
   * The expander places Global processes in a ProcessNode and all
   * local process in a list "localProcesses" on the ProcessNode
   *
   *  P1 = a->P2,   P2 = b->c->x.  will have ProcessNode P1 with localProcesses list [P2]
   *  RefReplacer   results in an empty localProcess list and ProcessNode P1 = a->b->c->x.
   *
   * RefReplacer also builds Hierarchy of Processes
   */
  public ReferenceReplacer() {
    globalReferences = new HashSet<>();
    references = new HashSet<>();
    globalRequirements = new HashSet<>();
  }
/*
  called from Compiler
 */
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
    System.out.println("Ref Rep "+ast.myString());
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
  //System.out.println(" INitial "+ localIdentifier+" "+ localProcess.myString());
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
/*   ReferenceReplacer.indent = ReferenceReplacer.indent.concat("-");
    String className = astNode.getClass().getSimpleName();
      System.out.println("RRast "+ ReferenceReplacer.indent + className+
      " refs "+astNode.getReferences()+" from "+astNode.getLeafRef());
*/
    if (astNode instanceof ProcessRootNode) {
      astNode = replaceReferences((ProcessRootNode) astNode, identifier, localReferences);
    } else if (astNode instanceof IndexExpNode) {
      astNode = replaceReferences((IndexExpNode) astNode, identifier, localReferences);
    }else if (astNode instanceof SequenceNode) {
      astNode = replaceReferences((SequenceNode) astNode, identifier, localReferences);
    } else if (astNode instanceof ChoiceNode) {
      astNode = replaceReferences((ChoiceNode) astNode, identifier, localReferences);
    } else if (astNode instanceof CompositeNode) {
      astNode = replaceReferences((CompositeNode) astNode, identifier, localReferences);
    } else if (astNode instanceof IdentifierNode) {
      astNode = replaceReferences((IdentifierNode) astNode, identifier, localReferences);
      //System.out.println("Ident  refs "+astNode.getReferences());
      //Next two options added for symbolic processing - they do not exits after the Expander
    } else if (astNode instanceof IndexExpNode) {
      astNode = replaceReferences((IndexExpNode) astNode, identifier, localReferences);
    } else if (astNode instanceof IfStatementExpNode) {
      astNode = replaceReferences((IfStatementExpNode) astNode, identifier, localReferences);

    } else if (astNode instanceof FunctionNode) {
      astNode = replaceReferences((FunctionNode) astNode, identifier, localReferences);
    }

    //Debugging do not remove  IndexExpNode
/*    if (ReferenceReplacer.indent.length()> 1)
      ReferenceReplacer.indent = ReferenceReplacer.indent.substring(1);
    System.out.println("RRast<"+ ReferenceReplacer.indent + className+" "+astNode.getReferences());
*/
    return astNode;
  }

  private IndexExpNode replaceReferences(IndexExpNode astNode, String identifier, Map<String, LocalProcessNode> localReferences) throws CompilationException, InterruptedException {
    ASTNode process = replaceReferences(astNode.getProcess(), identifier, localReferences);
    astNode.setProcess(process);
    return astNode;
  }
  private IfStatementExpNode replaceReferences(IfStatementExpNode astNode, String identifier, Map<String, LocalProcessNode> localReferences) throws CompilationException, InterruptedException {
    ASTNode trueProcess = replaceReferences(astNode.getTrueBranch(), identifier, localReferences);
    ((IfStatementExpNode) astNode).setTrueBranch(trueProcess);
    if (astNode.hasFalseBranch()) {
      ASTNode falseProcess = replaceReferences(astNode.getFalseBranch(), identifier, localReferences);
      ((IfStatementExpNode) astNode).setFalseBranch(falseProcess);
    }
    BoolExpr g = ((IfStatementExpNode) astNode).getCondition();
    ((IfStatementExpNode) astNode).setCondition(g); //  Should refreplaceing be done in here?
    return  astNode;
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
    System.out.println("RR IdentifierNode "+ astNode.myString()+" references "+references);
      bits =  astNode.getBits();
    String reference = astNode.getIdentifier();
    String localReference = findLocalReference(identifier + "." + reference, localReferences);
    //System.out.println("RR localReference "+localReference);
    // check if the identifier is referencing a local process
    if (localReference != null && localReference.length()>0) {
      // check if this local process has been referenced before
      if (references.contains(localReference)) {
        //this is where identifier is used
        ReferenceNode rn = new ReferenceNode(localReference, astNode.getLocation());
        rn.setSymbolicBits(astNode.getBits());
        //System.out.println("  RR  new local"+rn.myString());
        return rn;
      } else {
        // add astNode as a localReference
        ASTNode node = localReferences.get(localReference).getProcess();
        addReference(node, localReference);

        if (astNode.hasReferences()) {
          astNode.getReferences().forEach(node::addReference);
        }
        //this is where identifier is defined
        ASTNode rr = replaceReferences(node, identifier, localReferences);
        //System.out.println("RR "+rr.myString());
        return rr;
        //return replaceReferences(node, identifier, localReferences);
      }
    }
    // check if the identifier is referencing itself
    else if (reference.equals(identifier)) {
      astNode.addFromReference(identifier);
      ReferenceNode rn = new ReferenceNode(identifier, astNode.getLocation());
      rn.setSymbolicBits(astNode.getBits());
      //System.out.println("Self referance? "+identifier +" "+rn.myString());
      return rn;
    }
    // check if the identifier is referencing a global process
    else if (globalReferences.contains(reference)) {
      //globalRequirements.add(reference);
      astNode.addFromReference(identifier);
      //System.out.println("??? Identifier "+ identifier+" RR referencing global process "+ astNode.getBits());
      return astNode;
    }
    throw new CompilationException(getClass(), "Unable to find reference for node: " + reference, astNode.getLocation());
  }

  public List<String> getBits(String id) {

    //System.out.println("rr1 "+id);
    String name  = id.replaceAll("(\\[|\\])+", " ");
    //System.out.println("rr2 "+name);
    List<String> out = Arrays.asList(name.split(" "));
    //System.out.println("rr3 "+out.size()+"  "+out);

    return out;
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
      //System.out.println("RR findLocalReference "+identifier+" bits "+ getBits(identifier)+" find in "+key);
      String name;
      if (identifier.contains("["))

        name = identifier.substring(0,identifier.indexOf("["));
      else
        name = identifier;
      String testKey = key.replaceAll("\\$[a-z][a-zA-Z0-9_]*", ".+").replace("[", "\\[").replace("]", "\\]");
      //System.out.println("testKey "+testKey+" name "+name);
      if (Pattern.compile(testKey).matcher(identifier).matches()) {
        return key;
      }
      if (Pattern.compile(testKey).matcher(name).matches()) {
        return key;
      }
    }
    //System.out.println("findLocalReference null" );
    //Throwable t = new Throwable();
    //t.printStackTrace();
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
