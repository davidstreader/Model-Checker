package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class RelabelElementNode extends ASTNode {

	private String newLabel;
	private String oldLabel;
    private RangesNode ranges;

	public RelabelElementNode(String newLabel, String oldLabel, Location location){
		super(location);
		this.newLabel = newLabel;
		this.oldLabel = oldLabel;
        ranges = null;
	}

    public boolean hasRanges(){
        return ranges != null;
    }
}
