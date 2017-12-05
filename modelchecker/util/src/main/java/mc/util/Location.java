package mc.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
@Getter
@AllArgsConstructor
public class Location implements Serializable {

	// fields
	private int lineStart;
	private int colStart;
	private int lineEnd;
	private int colEnd;
	private int startIndex;
    private int endIndex;

    public Location(Location start, Location end){
        this.lineStart = start.getLineStart();
        this.colStart = start.getColStart();
        this.lineEnd = end.getLineEnd();
        this.colEnd = end.getColEnd();
        this.startIndex = start.getStartIndex();
        this.endIndex = end.getEndIndex();
    }
	public String toString(){
		return "(" + lineStart + ":" + colStart + "-" + lineEnd + ":" + colEnd +")";
	}
}
