package mc.gui;

class TerminalOutputStream extends StringOutputStream {
  private TerminalWindow terminal;

  TerminalOutputStream(final TerminalWindow terminal) {
    this.terminal = terminal;
  }


  @Override
  public void renderText(String text) {
    terminal.getTerminal().append(text);
  }
}
