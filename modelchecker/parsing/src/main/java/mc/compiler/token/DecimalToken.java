package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class DecimalToken extends Token {

    private Double real;

    public DecimalToken(Double dbl, Location location){
        super(location);
        this.real = dbl;
    }

    @Override
    public String toString(){
        return "" + String.format("%.2f", real);
    }
}