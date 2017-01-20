package mc.commands;

import mc.Main;
import org.fusesource.jansi.Ansi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CommandManager {
  private Map<String,Command> commandMap = new HashMap<>();
  //Load commands
  public CommandManager(Main main) {
    commandMap.put("eval",new EvalCommand());
    commandMap.put("simp",new SimplifyCommand());
    commandMap.put("simplify",commandMap.get("simp"));
    commandMap.put("exit",new ExitCommand(main));
  }
  public void executeCommand(String command) {
    System.out.println();
    if (command.matches("\\s+") || Objects.equals(command, "")) return;
    String cmd = command.split(" ")[0];
    if (command.length() <= cmd.length()) command = "";
    else command = command.substring(cmd.length()+1);
    if (commandMap.containsKey(cmd)) commandMap.get(cmd).run(command.split(" "));
    else System.out.println(Ansi.ansi().render("@|red Unable to find command |@"));
  }

  /**
   * Listens to System.in, parsing commands and passing them to executeCommand
   */
  public void registerInput() {
    Thread thread = new Thread(()->{
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      while (!Thread.interrupted()) {
        try {
          String line = reader.readLine();
          if (line != null) {
            executeCommand(line);
          }
        } catch (IOException e) {
          e.printStackTrace();
        }

      }
    });
    thread.start();
  }
}
