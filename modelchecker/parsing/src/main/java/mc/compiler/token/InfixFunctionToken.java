package mc.compiler.token;

import lombok.EqualsAndHashCode;
import mc.util.Location;

@EqualsAndHashCode
public class InfixFunctionToken extends Token{

    private String label;

    public InfixFunctionToken(String label, Location location) {
        super(location);
        this.label = label;
    }

    @Override
    public String toString(){
        return label;
    }
}
