package mc.compiler.token;

import mc.util.Location;

public class ProcessesDefintionToken extends Token {

    public ProcessesDefintionToken(Location location) {
        super(location);
    }

    public boolean equals(Object obj){
            return (obj == this);
    }

}