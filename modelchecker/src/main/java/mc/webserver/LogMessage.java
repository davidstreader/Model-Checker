package mc.webserver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.Main;

@AllArgsConstructor
@Getter
public class LogMessage {
  private String message;
  private boolean clear = false;
  public LogMessage(String message) {
    this.message = message;
  }
  public void send() {
    Main.getInstance().getWebServer().getSocket().send("log",this);
  }
}
