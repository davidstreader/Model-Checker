package mc.compiler.token;

import mc.util.Location;

public class ProcessTypeToken extends Token {

	private String processType;

	public ProcessTypeToken(String processType, Location location){
		super(location);
		this.processType = processType;
	}

	public String getProcessType(){
		return processType;
	}

	public String toString(){
		return processType;
	}

}