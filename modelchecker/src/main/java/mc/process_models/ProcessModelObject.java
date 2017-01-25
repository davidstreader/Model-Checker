package mc.process_models;

import lombok.Getter;
import mc.compiler.ast.ASTNode;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by sheriddavi on 24/01/17.
 */
public abstract class ProcessModelObject implements Serializable {

    // fields
    private String id;
    @Getter
    private String type;
    @Getter
    private Map<String, Object> metaData;

    public ProcessModelObject(String id, String type){
        this.id = id;
        this.type = type;
        this.metaData = new HashMap<String, Object>();
    }

    public String getId(){
        return id;
    }

    public Object getMetaData(String key){
        if(metaData.containsKey(key)){
            return metaData.get(key);
        }

        return null;
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

    public Set<String> getMetaDataKeys(){
        return metaData.keySet();
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
