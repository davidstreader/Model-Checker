package mc.compiler.ast;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Object;
import com.rits.cloning.Cloner;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import mc.util.Location;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(exclude = {"location","modelVariables","guard"})
public abstract class ASTNode implements Serializable {

    @Getter         private Set<String> references;
	@Getter         private Location location;
    @Getter @Setter private HashMap<String, Object> modelVariables;
    @Getter @Setter private Object guard;

    public ASTNode(Location location){
		references = null;
		this.location = location;
	}

	public void addReference(String reference){
		if(references == null)
			references = new HashSet<>();
        references.add(reference);
	}

	public boolean hasReferences(){
		return references != null;
	}

	public ASTNode copy(){
        Cloner cloner = new Cloner();
        cloner.dontClone(Context.class);
        cloner.dontClone(Z3Object.class);
        cloner.dontClone(Expr.class);
        cloner.dontClone(BoolExpr.class);
        return cloner.deepClone(this);
	}
}
