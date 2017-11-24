package mc.util;

import mc.compiler.ast.ProcessNode;
import mc.webserver.LogMessage;

/**
 * Created by bealjaco on 24/11/17.
 */
public class LogAST extends LogMessage {
    public LogAST(String function, ProcessNode process) {
        super(function+" @|black "+process.getIdentifier()+" "+ formatLocation(process.getLocation())+"|@",true,false,null,-1,Thread.currentThread());
    }
}
