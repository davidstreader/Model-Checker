
package mc.compiler.token;

  import lombok.Data;
  import lombok.EqualsAndHashCode;
  import mc.Constant;
  import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
/**
 * Token for Stop node that may have exiting edges
 * Initiall used for testing but built by Gal fap2bc
 */
public class EndToken extends TerminalToken {

  public EndToken(Location location){
    super(location);
  }

  @Override
  public String toString(){
    return Constant.END;
  }
}
