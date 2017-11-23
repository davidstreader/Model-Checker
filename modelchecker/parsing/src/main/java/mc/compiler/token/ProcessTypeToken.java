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

	public boolean equals(Object obj){
		if(obj == this){
			return true;
		}
		if(obj instanceof ProcessTypeToken){
			ProcessTypeToken token = (ProcessTypeToken)obj;
			return processType.equals(token.getProcessType());
		}

		return false;
	}

	public String toString(){
		return processType;
	}

}