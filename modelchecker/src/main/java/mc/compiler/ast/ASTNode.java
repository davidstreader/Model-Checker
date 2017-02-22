package mc.compiler.ast;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.rits.cloning.Cloner;
import lombok.Getter;
import mc.util.Location;

public abstract class ASTNode implements Serializable {

	private static final long serialVersionUID = 1L;

	// fields
	private Set<String> references;
	private Location location;
    @Getter
	private HashMap<String,Object> metaData = new HashMap<>();

	public ASTNode(Location location){
		references = null;
		this.location = location;
	}

	public Set<String> getReferences(){
		return references;
	}

	public void addReference(String reference){
		if(references == null){
            references = new HashSet<String>();
        }

        references.add(reference);
	}

	public boolean hasReferences(){
		return references != null;
	}

	public Location getLocation(){
		return location;
	}

    public boolean equals(Object obj){
        if(obj == null){
            return false;
        }
        if(obj == this){
            return true;
        }
        if(obj instanceof ASTNode){
            ASTNode node = (ASTNode)obj;
            if(hasReferences() && !node.hasReferences() || !hasReferences() && node.hasReferences()){
                return false;
            }
            if(!hasReferences() && !node.hasReferences()){
                return true;
            }

            return references.equals(node.getReferences());
        }

        return false;
    }

	public ASTNode copy(){
        Cloner cloner = new Cloner();
        return cloner.deepClone(this);
	}
}
