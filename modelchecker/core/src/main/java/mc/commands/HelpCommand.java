package mc.commands;

import static org.fusesource.jansi.Ansi.ansi;

public class HelpCommand implements Command {
  @Override
  public void run(String[] args) {
    System.out.println(formatCommand("help","","Get a list of commands"));
    System.out.println(formatCommand("simplify, simp","expression","Simplify an expression"));
    System.out.println(formatCommand("evaluate, eval","expression","Evaluate an expression"));
    System.out.println(formatCommand("test","directory of files to compile and run","Tests files"));
    System.out.println(formatCommand("exit","","Exits"));

  }

  private String formatCommand(String command, String usage, String message) {
    if (!usage.isEmpty()) usage = " "+usage;

    return ansi().fgYellow().a(command).fgBrightBlack().a(usage).reset().a(" - ").a(message).toString();
  }
}
