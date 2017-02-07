package mc.compiler.ast;

import java.io.*;
import java.util.HashMap;

import lombok.Getter;
import mc.util.Location;

public abstract class ASTNode implements Serializable {

	private static final long serialVersionUID = 1L;

	// fields
	private Integer referenceId;
	private Location location;
	@Getter
	private HashMap<String,Object> metaData = new HashMap<>();

	public ASTNode(Location location){
		referenceId = null;
		this.location = location;
	}

	public int getReferenceId(){
		return referenceId;
	}

	public void setReferenceId(int referenceId){
		this.referenceId = referenceId;
	}

	public boolean hasReferenceId(){
		return referenceId != null;
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
