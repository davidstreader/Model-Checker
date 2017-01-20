package mc.commands;

public interface Command {
  void run(String[] args);

  /**
   * Should this command pass through to the sub process?
   * @return true for yes, false for no
   */
  default boolean passthrough() {
    return false;
  }
}
