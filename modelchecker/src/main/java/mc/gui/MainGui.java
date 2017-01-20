package mc.gui;

import lombok.Getter;
import mc.Main;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;

public class MainGui {
  @Getter
  private TerminalWindow terminal;
  public MainGui(Main main) {
    terminal = new TerminalWindow(main);
    //When the window is closed, kill the sub process and the main app.
    terminal.getFrame().addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        super.windowClosed(e);
        main.stop();
      }
    });
    //Redirect System.out and System.err to the console
    System.setOut(new PrintStream(new TerminalOutputStream(terminal)));
    System.setErr(System.out);
  }
  /**
   * Show the progress bar
   */
  public void showProgressBar() {
    terminal.getProgressPanel().setVisible(true);
  }

  /**
   * Hide the progress bar
   */
  public void hideProgressBar() {
    terminal.getProgressPanel().setVisible(false);
  }
  /**
   * Set a value on the progress bar
   * @param value a value from 0 to 100
   */
  public void setProgressBarValue(int value) {
    terminal.getProgressBar1().setValue(value);
  }

  /**
   * Get the progress bar value
   * @return a value from 0 to 100
   */
  public int getProgressBarValue() {
    return terminal.getProgressBar1().getValue();
  }

  /**
   * Redirect a terminal's IO to the GUI
   * @param process The process to redirect
   */
  public void redirectTerminalProcess(Process process) {
    terminal.wrapProcess(process);
  }
}
