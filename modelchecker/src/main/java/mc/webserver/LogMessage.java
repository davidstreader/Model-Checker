package mc.webserver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.Main;

import static org.fusesource.jansi.Ansi.ansi;

@AllArgsConstructor
@Getter
public class LogMessage {
  private String message;
  private boolean clear = false;
  private boolean error = false;
  public LogMessage(String message) {
    this.message = ansi().render(message).toString();
  }
  public void send() {
    Main.getInstance().getWebServer().getSocket().send("log",this);

  }
}
