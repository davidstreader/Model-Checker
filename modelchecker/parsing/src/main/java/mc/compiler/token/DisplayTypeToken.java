package mc.compiler.token;

import mc.util.Location;

public class DisplayTypeToken extends Token {

    private String type;

    public DisplayTypeToken(String processType, Location location){
        super(location);
        this.type = processType;
    }

    public String getProcessType(){
        return type;
    }

    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj instanceof DisplayTypeToken){
            DisplayTypeToken token = (DisplayTypeToken)obj;
            return type.equals(token.getProcessType());
        }

        return false;
    }

    public String toString(){
        return type;
    }

}