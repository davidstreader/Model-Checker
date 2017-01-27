package mc.gui;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.OutputStreamAppender;
import lombok.Getter;
import mc.Main;
import org.slf4j.LoggerFactory;

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
    addTerminalLoggerAppender();
    redirectIO();
  }
  private void redirectIO() {
    System.setOut(new PrintStream(new TerminalOutputStream(terminal),true));
    System.setErr(System.out);
  }
  private void addTerminalLoggerAppender() {
    //Get a logger context for logback
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    //Get the root logger
    Logger log = context.getLogger(Logger.ROOT_LOGGER_NAME);
    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
    encoder.setContext(context);
    encoder.setPattern("[%thread] %highlight(%-5level) %cyan(%logger{15}) - %msg %n");
    encoder.start();
    //Create an OutputStreamAppender that logs to the TerminalWindow
    OutputStreamAppender<ILoggingEvent> appender= new OutputStreamAppender<>();
    appender.setName( "OutputStream Appender" );
    appender.setContext(context);
    appender.setEncoder(encoder);
    appender.setOutputStream(new PrintStream(new TerminalOutputStream(terminal)));
    appender.start();
    log.addAppender(appender);
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

  public static void registerConsoleAppender() {
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    Logger log = context.getLogger(Logger.ROOT_LOGGER_NAME);
    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
    encoder.setContext(context);
    encoder.setPattern("[%thread] %highlight(%-5level) %cyan(%logger{15}) - %msg %n");
    encoder.start();
    //Instead of logging to the window, log to the normal console as
    //we are running headless
    OutputStreamAppender<ILoggingEvent> appender= new ConsoleAppender<>();
    appender.setName( "OutputStream Appender" );
    appender.setContext(context);
    appender.setEncoder(encoder);
    appender.start();
    log.addAppender(appender);
  }
}
