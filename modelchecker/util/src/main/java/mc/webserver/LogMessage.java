package mc.webserver;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.compiler.ast.ProcessNode;
import mc.util.Location;

import static org.fusesource.jansi.Ansi.ansi;

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

    @Getter(onMethod = @__(@JsonIgnore))
    private Thread thread;
    public LogMessage(String message) {
        this.message = message;
        this.thread = Thread.currentThread();
    }

    public LogMessage(String function, ProcessNode process) {
        this(function+" @|black "+process.getIdentifier()+" "+formatLocation(process.getLocation())+"|@",true,false,null,-1,Thread.currentThread());
    }

    public LogMessage(String message, boolean clear, boolean error) {
        this(message,clear,error,null,-1,Thread.currentThread());
    }

    public LogMessage(String message, int clearAmt) {
        this(message);
        this.clearAmt = clearAmt;
        this.clear = true;
    }

    private static String formatLocation(Location location) {
        return "("+location.getLineStart()+":"+location.getColStart()+")";
    }
    public void render() {
        this.message = Ansi.ansi().render(message).toString();
    }
    public void printToConsole() {
        message = message.replace("@|black","@|white");
        render();
        System.out.println(message);
    }
    public boolean hasExpired() {
        return thread.isInterrupted();
    }
}
