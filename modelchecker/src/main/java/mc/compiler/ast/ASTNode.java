package mc.compiler.ast;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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

	public ASTNode copy(){
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(output);
			out.writeObject(this);
			out.close();
			output.close();

			ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
			ObjectInputStream in = new ObjectInputStream(input);
			ASTNode node = (ASTNode)in.readObject();
			in.close();
			input.close();
			return node;

		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
}
