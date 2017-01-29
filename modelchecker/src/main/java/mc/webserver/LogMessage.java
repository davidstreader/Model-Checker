package mc.webserver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.Main;
import mc.compiler.ast.ProcessNode;
import mc.util.Location;

import static org.fusesource.jansi.Ansi.ansi;

@AllArgsConstructor
@Getter
public class LogMessage {
  private String message;
  private boolean clear = false;
  private boolean error = false;
  public LogMessage(String message) {
    this.message = message;
  }

  public LogMessage(String function, ProcessNode process) {
      this(function+" @|black "+process.getIdentifier()+" "+formatLocation(process.getLocation())+"|@",true,false);
  }

  private static String formatLocation(Location location) {
    return "("+location.getLineStart()+":"+location.getColStart()+")";
  }

  public void send() {
    this.message = ansi().render(message).toString();
    Main.getInstance().getWebServer().getSocket().send("log",this);
  }
}
