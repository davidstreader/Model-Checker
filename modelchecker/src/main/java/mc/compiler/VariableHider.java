package mc.compiler;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;
import mc.compiler.ast.*;
import mc.util.Location;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VariableHider {
    AbstractSyntaxTree hideVariables(AbstractSyntaxTree ast) {
        List<ProcessNode> processes = ast.getProcesses();
        for (ProcessNode process : processes) {
            HashMap<String, HiddenObject> processMap = new HashMap<>();
            for (LocalProcessNode localProcessNode : process.getLocalProcesses()) {
                constructMap(localProcessNode, processMap, process.getVariables());
            }
            hideInProcess(process,processMap,new ArrayList<>());
        }
        return ast;
    }

    private void constructMap(LocalProcessNode process, HashMap<String, HiddenObject> processMap, VariableSetNode variables) {
        List<IndexNode> ranges = process.getRanges().getRanges();
        if (!ranges.isEmpty() && variables != null) {
            for (int i = 0; i < ranges.size(); i++) {
                String v = ranges.get(i).getVariable().substring(1);
                if (variables.getVariables().contains(v)) {
                    //Get the original identifier, and split out the number at range index.
                    //We can then join everything back up after replacing that with the variable name.
                    String oldIdent = process.getIdentifier();
                    String[] tmp = oldIdent.split("\\[");
                    int rangeIdx = i + 1;
                    tmp[rangeIdx] = tmp[rangeIdx].substring(tmp[rangeIdx].indexOf("]"));
                    tmp[rangeIdx] = v + tmp[rangeIdx];
                    process.setIdentifier(String.join("[", tmp));
                    //Store a mapping from the original name to the new name
                    processMap.put(oldIdent, new HiddenObject(process.getIdentifier(), false));
                }
            }
        }
        process.getMetaData().remove("ranges");
    }
    private ProcessNode hideInProcess(ProcessNode process,HashMap<String, HiddenObject> processMap, List<String> actionMap) {
        //Hide local processes first, as the main process references the local processes.
        if (!process.getLocalProcesses().isEmpty()) {
            for (int i = 0; i < process.getLocalProcesses().size(); i++) {
                process.getLocalProcesses().set(i,hideInProcess(process.getLocalProcesses().get(i),processMap, process.getVariables(), actionMap));
            }
        }
        //replace the process with a version with all hidden variables hidden
        process.setProcess(hideNode(process.getProcess(),processMap, process.getVariables(), actionMap));
        if (!process.getLocalProcesses().isEmpty()) {
            List<LocalProcessNode> toDel = new ArrayList<>();
            HashMap<String,LocalProcessNode> localMap = new HashMap<>();
            //Look through all local processes, and then merge together processes with identical identifiers
            for (LocalProcessNode local : process.getLocalProcesses()) {
                if (localMap.containsKey(local.getIdentifier())) {
                    localMap.get(local.getIdentifier()).setProcess(mergeProcess(localMap.get(local.getIdentifier()).getProcess(),local.getProcess()));
                    toDel.add(local);
                } else {
                    localMap.put(local.getIdentifier(),local);
                }
            }
            List<LocalProcessNode> processNodes = process.getLocalProcesses();
            processNodes.removeAll(toDel);
            process.setLocalProcesses(processNodes);
        }
        return process;
    }

    private LocalProcessNode hideInProcess(LocalProcessNode process, HashMap<String, HiddenObject> processMap, VariableSetNode variables, List<String> actionMap) {
        //replace the process with a version with all hidden variables hidden
        process.setProcess(hideNode(process.getProcess(),processMap, variables, actionMap));
        return process;
    }

    //Process a node for hiding
    ASTNode hideNode(ASTNode node, HashMap<String, HiddenObject> processMap, VariableSetNode variableSet, List<String> actionMap) {
        if (node instanceof ChoiceNode || node instanceof CompositeNode) {
            ASTNode p1 = processBranch(node, "firstProcess", processMap, actionMap, variableSet);
            ASTNode p2 = processBranch(node, "secondProcess", processMap, actionMap, variableSet);
            //When we want to delete something, it will be set to null.
            //In that case, we can just return the other node
            if (p1 == null) node = p2;
            if (p2 == null) node = p1;
            //It is possible that both processes are null.
            //In this case, the parent will get to this exact point and return the other process.
            if (node == null) return null;
        }
        if (node instanceof SequenceNode) {
            SequenceNode sequenceNode = (SequenceNode) node;
            if (sequenceNode.getTo() instanceof IdentifierNode) {
                IdentifierNode idNode = (IdentifierNode) sequenceNode.getTo();
                if (processMap.containsKey(idNode.getIdentifier())) {
                    Location l = node.getLocation();
                    String key = l.getLineStart() + "," +
                        l.getColStart() + l.getLineEnd() + "," +
                        l.getColEnd() + "->" + processMap.get(idNode.getIdentifier()).ident;
                    if (actionMap.contains(key)) return null;
                    actionMap.add(key);
                }
            }
            processBranch(node, "to", processMap, actionMap, variableSet);
        }
        if (node instanceof FunctionNode) {
            processBranch(node, "process", processMap, actionMap, variableSet);
        }
        if (node instanceof IdentifierNode) {
            if (processMap.containsKey(((IdentifierNode) node).getIdentifier())) {
                ((IdentifierNode) node).setIdentifer(processMap.get(((IdentifierNode) node).getIdentifier()).ident);
            }
        }
        if (node.getMetaData().containsKey("guard")) {
            Guard guard = (Guard) node.getMetaData().get("guard");
            if (variableSet != null) {
                guard.getVariables().keySet().removeIf(s -> variableSet.getVariables().contains(s.substring(1)));
                guard.setShouldDisplay(true);
            }
        }
        return node;
    }
    ASTNode mergeProcess(ASTNode process1, ASTNode process2) {
        if (process1 == null) return process2;
        if (process2 == null) return process1;
        //If both nodes are identifiers, nothing needs to be done as they are identical
        if (process1 instanceof IdentifierNode && process2 instanceof IdentifierNode) {
            //the identifier itself will be identical.
            return process1;
        } else {
            //At this point, location information is hopefully not needed.
            //the easiest way to merge two unrelated nodes is to create a choice from them.
            return new ChoiceNode(process1,process2,null);
        }
    }
    @SneakyThrows
    ASTNode processBranch(ASTNode parent, String child, HashMap<String, HiddenObject> processMap, List<String> actionMap,VariableSetNode variableSet) {
        Method set = createSetter(parent,child);
        Method get = createGetter(parent,child);
        ASTNode childNode = (ASTNode) get.invoke(parent);
        String ident = childNode instanceof IdentifierNode?((IdentifierNode) childNode).getIdentifier():null;
        set.invoke(parent,hideNode(childNode, processMap, variableSet, actionMap));
        childNode = (ASTNode) get.invoke(parent);
        if (ident == null) return childNode;
        if (processMap.containsKey(ident)) {
            if (processMap.get(ident).matched) {
                //There is already a node with this identifier. Instead of duplicating it, we merge it
                //With the new node
                ASTNode parent2 = processMap.get(ident).parent;
                String child2 = processMap.get(ident).child;
                Method get2 = createGetter(parent2,child2);
                Method set2 = createSetter(parent2, child2);
                ASTNode childNode2 = (ASTNode) get2.invoke(parent2);
                ASTNode newNode = mergeProcess(childNode,childNode2);
                set2.invoke(parent2,newNode);
                set.invoke(parent,newNode);
            }
            String newIdent = processMap.get(ident).ident;
            processMap.put(ident,new HiddenObject(child, parent, newIdent, true));
        }
        return (ASTNode) get.invoke(parent);
    }
    @SneakyThrows
    private Method createSetter(ASTNode parent, String child) {
        return parent.getClass().getMethod("set"+child.substring(0,1).toUpperCase()+child.substring(1),ASTNode.class);
    }
    @SneakyThrows
    private Method createGetter(ASTNode parent, String child) {
        return parent.getClass().getMethod("get"+child.substring(0,1).toUpperCase()+child.substring(1));
    }
    @ToString
    @Getter
    @Setter
    private class HiddenObject {
        private String ident;
        private boolean matched;
        private ASTNode parent;
        private String child;

        public HiddenObject(String identifier, boolean matched) {
            this.ident = identifier;
            this.matched = matched;
        }

        public HiddenObject(String child, ASTNode parent, String ident, boolean matched) {
            this.matched = matched;
            this.child = child;
            this.parent = parent;
            this.ident = ident;
        }
    }
}
