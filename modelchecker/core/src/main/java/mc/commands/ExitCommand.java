package mc.commands;

import lombok.AllArgsConstructor;
import mc.Main;
@AllArgsConstructor
public class ExitCommand implements Command {
  private Main main;
  @Override
  public void run(String[] args) {
      main.stop();
  }
}
