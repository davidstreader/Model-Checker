package mc.webserver;

import lombok.AllArgsConstructor;
import lombok.Getter;
import mc.Main;
import mc.compiler.ast.ProcessNode;
import mc.util.Location;

import javax.annotation.Nullable;

import static org.fusesource.jansi.Ansi.ansi;

@AllArgsConstructor
@Getter
public class LogMessage {
    private String message;
    private boolean clear = false;
    private boolean error = false;
    private Location location = null;
    public LogMessage(String message) {
        this.message = message;
    }

    public LogMessage(String function, ProcessNode process) {
        this(function+" @|black "+process.getIdentifier()+" "+formatLocation(process.getLocation())+"|@",true,false,null);
    }

    public LogMessage(String message, boolean clear, boolean error) {
        this(message,clear,error,null);
    }

    private static String formatLocation(Location location) {
        return "("+location.getLineStart()+":"+location.getColStart()+")";
    }

    public void send() {
        boolean hasClient = Main.getInstance() != null && Main.getInstance().getWebServer().getSocket().hasClient();
        if (!hasClient) message = message.replace("@|black","@|white");
        this.message = ansi().render(message).toString();
        if (hasClient)
            Main.getInstance().getWebServer().getSocket().send("log",this);
        else
            System.out.println(message);
    }
}
