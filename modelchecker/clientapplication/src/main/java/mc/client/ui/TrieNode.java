package mc.client.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Jacob Beal on 17/03/2017.
 *  No meaningful documentation so must be removed some time soon
 */
public class TrieNode<E> {
    private HashMap<String, TrieNode> children = new HashMap<String, TrieNode>();
    private ArrayList<E> values = new ArrayList<E>();

    public TrieNode(){}


    /**
     *
     * @param inputValues Strings to populate the autocompletion structure
     * @param size        Number of strings present.
     */
    public TrieNode(String[] inputValues, int size){
        for(int i = 0; i < size; i++)
            add(inputValues[i], (E)inputValues[i]);
    }

    /**
     * Populate the autocompletion structure using an arraylist

     */
    public TrieNode(ArrayList<String> inputValues) {
        inputValues.forEach(s -> {add(s, (E)s);});
    }


    public boolean add(String id, E val){
        if(id == null || id.equals("")){
            values.add(val);
            return true;
        }
        id = id.toLowerCase();
        String childId = id.substring(0,1);
        TrieNode<E> child;
        if(children.containsKey(childId)){
            child = children.get(childId);
        } else {
            children.put(childId,new TrieNode());
            child = children.get(childId);
        }
        return child.add(id.substring(1),val);
    }

    public void add(ArrayList<String> input) {
        for(String elementToAdd : input)
            add(elementToAdd, (E)elementToAdd);
    }

    public ArrayList<E> getId(String id){
        if(id == null || id.equals("")){
            ArrayList<E> returnVal = new ArrayList<E>();//temporary variable to prevent values getting alot of junk
            returnVal.addAll(values);
            for (Map.Entry<String,TrieNode> entry: children.entrySet()) {//add every child's
                returnVal.addAll(entry.getValue().getId(""));
            }
            return returnVal;
        }
        String childId = id.substring(0,1);
        TrieNode<E> child;
        if(children.containsKey(childId)){
            return children.get(childId).getId(id.substring(1));//recurses the function on the appropriate child with
            //the first character of the string removed
        } else {
            return new ArrayList<E>();//makes it so I dont have to deal with null values on the other end
        }
    }
}
