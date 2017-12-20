package mc.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import mc.Main;
import mc.util.Utils;
import org.apache.commons.lang3.StringUtils;

public class CommandManager {
    protected Map<String,Command> commandMap = new HashMap<>();
    //Load commands
    public CommandManager(Main main) {
        commandMap.put("help", new HelpCommand());
        commandMap.put("exit",new ExitCommand(main));
        if (main.isReloaded() || !Utils.isJar()) {
            commandMap.put("eval", new EvalCommand());
            commandMap.put("evaluate", commandMap.get("eval"));
            commandMap.put("test", new TestCommand());
        }
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
            System.out.println("Unable to find command ");
            System.out.println("For a list of commands, type help.");
        }
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
                    } else {
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
    }
}
