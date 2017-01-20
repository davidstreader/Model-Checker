package mc.gui;

import java.io.IOException;
import java.io.OutputStream;
class TerminalOutputStream extends OutputStream {
  private TerminalWindow terminal;
  TerminalOutputStream(TerminalWindow terminal) {
    this.terminal = terminal;
  }
  //Create a buffer to hold the string until we want to write it
  private char[] buffer = new char[4096];
  //Index we are at in the buffer
  private int i = 0;
  @Override
  public void write(int b) throws IOException {
    //Buffer all chars written to this stream
    buffer[i++] = (char)b;
    //Write when we hit a newline or run out of space in the buffer.
    if ((char) b == '\n' || i == 4096) {
      terminal.getTerminal().append(new String(buffer,0,i));
      i = 0;
    }
  }
}
