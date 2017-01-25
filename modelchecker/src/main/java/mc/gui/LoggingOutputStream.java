package mc.gui;

import org.slf4j.Logger;

public class LoggingOutputStream extends StringOutputStream {

  /**
   * The logger to write to.
   */
  private Logger log;

  /**
   * Creates the Logging instance to flush to the given logger.
   *
   * @param log         the Logger to write to
   * @throws IllegalArgumentException in case if one of arguments
   *                                  is  null.
   */
  public LoggingOutputStream(final Logger log)
    throws IllegalArgumentException {
    if (log == null) {
      throw new IllegalArgumentException(
        "Logger or log level must be not null");
    }
    this.log = log;
  }

  @Override
  public void renderText(String text) {
    log.info(text);
  }
}
