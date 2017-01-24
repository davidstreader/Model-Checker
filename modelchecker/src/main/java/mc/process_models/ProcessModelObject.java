package mc.process_models;

import mc.compiler.ast.ASTNode;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sheriddavi on 24/01/17.
 */
public abstract class ProcessModelObject implements Serializable {

    // fields
    private String id;
    private Map<String, Object> metaData;

    public ProcessModelObject(String id){
        this.id = id;
        this.metaData = new HashMap<String, Object>();
    }

    public String getId(){
        return id;
    }

    public void addMetaData(String key, Object value){
        metaData.put(key, value);
    }

    public void removeMetaData(String key){
        if(metaData.containsKey(key)){
            metaData.remove(key);
        }
    }

    public boolean hasMetaData(String key){
        return metaData.containsKey(key);
    }

    public ProcessModelObject clone(){
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(output);
            out.writeObject(this);
            out.close();
            output.close();

            ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
            ObjectInputStream in = new ObjectInputStream(input);
            ProcessModelObject obj = (ProcessModelObject)in.readObject();
            in.close();
            input.close();
            return obj;

        } catch (IOException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }
}
