package mc.compiler.ast;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Object;
import com.rits.cloning.Cloner;
import lombok.Getter;
import mc.util.Location;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
            references = new HashSet<>();
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
            if(node.hasReferences() != hasReferences()){
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
        cloner.dontClone(Context.class);
        cloner.dontClone(Z3Object.class);
        cloner.dontClone(Expr.class);
        cloner.dontClone(BoolExpr.class);
        return cloner.deepClone(this);
	}

    public Object getMetaData(String key) {
        return metaData.get(key);
    }
}
