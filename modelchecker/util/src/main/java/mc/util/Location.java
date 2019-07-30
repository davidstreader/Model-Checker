package mc.util;

import java.io.Serializable;

/**
 * dose not add to semantics of model
 *
 * used as map to source code
 *  helpful with error reporting
 */
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

  public Location(int lineStart, int colStart, int lineEnd, int colEnd, int startIndex, int endIndex) {
    this.lineStart = lineStart;
    this.colStart = colStart;
    this.lineEnd = lineEnd;
    this.colEnd = colEnd;
    this.startIndex = startIndex;
    this.endIndex = endIndex;
  }

  public String toString(){
		return "(" + lineStart + ":" + colStart + "-" + lineEnd + ":" + colEnd +")";
	}

  public int getColStart() {
    return this.colStart;
  }

  public int getLineEnd() {
    return this.lineEnd;
  }

  public int getColEnd() {
    return this.colEnd;
  }

  public int getStartIndex() {
    return this.startIndex;
  }

  public int getEndIndex() {
    return this.endIndex;
  }

  public int getLineStart() {
    return this.lineStart;
  }
}
