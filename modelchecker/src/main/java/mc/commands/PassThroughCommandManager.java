package mc.commands;

import mc.Main;
import mc.commands.CommandManager;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

public class PassThroughCommandManager extends CommandManager {
  public PassThroughCommandManager(Main main) {
    super(main);
  }
  public void executeCommand(String command) {
    if (StringUtils.isEmpty(command)) return;
    String[] split = command.split("\\s+");
    String cmd = split[0];
    if (commandMap.containsKey(cmd)) {
      if (split.length > 1) split = Arrays.copyOfRange(split,1,split.length);
      else split = new String[0];
      commandMap.get(cmd).run(split);
    } else {
      PrintStream stream = new PrintStream(Main.getInstance().getSubProcess().getOutputStream());
      stream.println(command);
      stream.flush();
    }
  }
}
