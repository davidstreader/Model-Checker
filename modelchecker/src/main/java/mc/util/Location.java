package mc.util;

import java.io.Serializable;

public class Location implements Serializable {

	// fields
	private int lineStart;
	private int colStart;
	private int lineEnd;
	private int colEnd;

	public Location(int lineStart, int colStart, int lineEnd, int colEnd){
		this.lineStart = lineStart;
		this.colStart = colStart;
		this.lineEnd = lineEnd;
		this.colEnd = colEnd;
	}

	public int getLineStart(){
		return lineStart;
	}

	public int getColStart(){
		return colStart;
	}

	public int getLineEnd(){
		return lineEnd;
	}

	public int getColEnd(){
		return colEnd;
	}

	public String toString(){
		return "(" + lineStart + ":" + colStart + "-" + lineEnd + ":" + colEnd +")";
	}
}
