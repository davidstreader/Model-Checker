package mc.compiler.ast;

import java.io.*;

import mc.util.Location;

public abstract class ASTNode implements Serializable {

	private static final long serialVersionUID = 1L;

	// fields
	private String label;
	private RelabelNode relabel;
	private Integer referenceId;
	private Location location;

	public ASTNode(Location location){
		label = null;
		relabel = null;
		referenceId = null;
		this.location = location;
	}

	public String getLabel(){
		return label;
	}

	public void setLabel(String label){
		this.label = label;
	}

	public boolean hasLabel(){
		return label != null;
	}

	public RelabelNode getRelabel(){
		return relabel;
	}

	public void setRelabelNode(RelabelNode relabel){
		this.relabel = relabel;
	}

	public boolean hasRelabel(){
		return relabel != null;
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

	public ASTNode clone(){
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
