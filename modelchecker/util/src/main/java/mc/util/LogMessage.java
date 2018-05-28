package mc.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.io.Serializable;

/**
 * A message to send back to the client
 */
@AllArgsConstructor
@Getter
public class LogMessage {
  private String message;
  /**
   * Clear the log window
   */
  private boolean clear = false;
  /**
   * Tell the client this is an error
   */
  private boolean error = false;
  private Location location = null;
  private int clearAmt = -1;

  @Getter
  private Thread thread;

  public LogMessage(String message) {
    this.message = message;
    this.thread = Thread.currentThread();
  }

  public LogMessage(String message, boolean clear, boolean error) {
    this(message, clear, error, null, -1, Thread.currentThread());
  }

  public LogMessage(String message, int clearAmt) {
    this(message);
    this.clearAmt = clearAmt;
    this.clear = true;
  }

  protected static String formatLocation(Location location) {
    return "(" + location.getLineStart() + ":" + location.getColStart() + ")";
  }

  public void printToConsole() {
    message = message.replace("@|black", "@|white");
    System.out.println(message);
  }

  public boolean hasExpired() {
    return thread.isInterrupted();
  }
}
