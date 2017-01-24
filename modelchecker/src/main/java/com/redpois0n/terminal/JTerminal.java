package com.redpois0n.terminal;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.*;
import java.util.List;

@SuppressWarnings("serial")
public class JTerminal extends JTextPane {

  private static final String RESET = "0";
  private static final String BOLD = "1";
  private static final String DIM = "2";
  private static final String UNDERLINED = "4";
  private static final String INVERTED = "7";

  private static final Font DEFAULT_FONT;
  private static final Color DEFAULT_FOREGROUND = Color.white;
  private static final Color DEFAULT_BACKGROUND = Color.black;

  private static final char ESCAPE = 27;

  private static final Map<String, Color> COLORS = new HashMap<>();

  static {
    DEFAULT_FONT = new Font("monospaced", Font.PLAIN, 14);

    // Default colors
    COLORS.put("30", Color.black);
    COLORS.put("31", Color.red.darker());
    COLORS.put("32", Color.green.darker());
    COLORS.put("33", Color.yellow.darker());
    COLORS.put("34", Color.blue);
    COLORS.put("35", Color.magenta.darker());
    COLORS.put("36", Color.cyan.darker());
    COLORS.put("37", Color.lightGray);
    COLORS.put("39", DEFAULT_FOREGROUND);

    // Bright colors
    COLORS.put("90", Color.gray);
    COLORS.put("91", Color.red);
    COLORS.put("92", Color.green);
    COLORS.put("93", Color.yellow);
    COLORS.put("94", Color.blue.brighter());
    COLORS.put("95", Color.magenta);
    COLORS.put("96", Color.cyan);
    COLORS.put("97", Color.white);

    // Background

    // Default colors
    COLORS.put("40", Color.black);
    COLORS.put("41", Color.red.darker());
    COLORS.put("42", Color.green.darker());
    COLORS.put("43", Color.yellow.darker());
    COLORS.put("44", Color.blue);
    COLORS.put("45", Color.magenta.darker());
    COLORS.put("46", Color.cyan.darker());
    COLORS.put("47", Color.lightGray);
    COLORS.put("49", DEFAULT_FOREGROUND);

    // Bright colors
    COLORS.put("100", Color.gray);
    COLORS.put("101", Color.red);
    COLORS.put("102", Color.green);
    COLORS.put("103", Color.yellow);
    COLORS.put("104", Color.blue.brighter());
    COLORS.put("105", Color.magenta);
    COLORS.put("106", Color.cyan);
    COLORS.put("107", Color.white);
  }

  private static boolean isBackground(String s) {
    return s.startsWith("4") || s.startsWith("10");
  }

  private static Color getColor(String s) {
    Color color = DEFAULT_FOREGROUND;

    boolean bright = s.contains("1;");
    s = s.replace("1;", "");

    if (s.endsWith("m")) {
      s = s.substring(0, s.length() - 1);
    }

    if (COLORS.containsKey(s)) {
      color = COLORS.get(s);
    }

    if (bright) {
      color = color.brighter();
    }

    return color;
  }

  private List<InputListener> inputListeners = new ArrayList<>();

  private AbstractDocument doc;
  private DocumentFilter filter;
  public JTerminal() {
    this.doc = (AbstractDocument) getStyledDocument();
    setFont(DEFAULT_FONT);
    setForeground(DEFAULT_FOREGROUND);
    setBackground(DEFAULT_BACKGROUND);
    setCaret(new TerminalCaret());
    append("");
    doc.setDocumentFilter(filter = new DocumentFilter(){
      //If someone attemps to remove something on a different line, dont allow it to happen
      public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
        //If someone tries to remove something on a different line, beep.
        if (offset < lastLine) {
          Toolkit.getDefaultToolkit().beep();
        } else {
          //Otherwise remove it.
          super.remove(fb,offset,length);
        }
      }
      public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        //Catch the new line entered by the user and ignore it. we place it in ourselves in a different place.
        if (Objects.equals(text, "\n")) {
          return;
        }
        //If someone tries to replace a block on another line, block it and beep.
        if (offset < lastLine) {
          Toolkit.getDefaultToolkit().beep();
        } else {
          super.replace(fb,offset,length,text,attrs);
        }
      }
    });
    addKeyListener(new KeyEventListener());
  }

  /**
   * Gets main key listener
   * @return the key listener
   */
  public KeyListener getKeyListener() {
    return super.getKeyListeners()[0];
  }

  public synchronized void append(String s) {
    boolean fg = true;
    Color foreground = DEFAULT_FOREGROUND;
    Color background = DEFAULT_BACKGROUND;
    boolean bold = false;
    boolean underline = false;
    boolean dim = false;

    String s1 = "";

    for (int cp = 0; cp < s.toCharArray().length; cp++) {
      char c = s.charAt(cp);

      if (c == ESCAPE) {
        append(s1, foreground, background, bold, underline);
        char next = s.charAt(cp + 1);

        if (next == '[') {
          s1 = "";
          cp++;
          while ((c = s.charAt(++cp)) != 'm') {
            s1 += c;
          }

          String[] attributes = s1.split(";");

          for (String at : attributes) {
            if (at.equals(RESET) || s1.length() == 0) {
              foreground = DEFAULT_FOREGROUND;
              background = DEFAULT_BACKGROUND;
              fg = true;
              underline = false;
              dim = false;
              bold = false;
            } else if (at.equals(BOLD)) {
              bold = !bold;
            } else if (at.equals(DIM)) {
              dim = !dim;
            } else if (at.equals(INVERTED)) {
              fg = !fg;
              if (fg) {
                Color temp = foreground;
                foreground = background;
                background = temp;
              } else {
                Color temp = background;
                background = foreground;
                foreground = temp;
              }
            } else if (at.equals(UNDERLINED)) {
              underline = !underline;
            } else if (s1.length() > 0) {
              Color color = getColor(at);

              if (isBackground(at)) {
                background = color;
              } else {
                foreground = color;
              }

              if (!fg) { // inverted
                Color temp = background;
                background = foreground;
                foreground = temp;
              }

              if (dim) {
                foreground = foreground.brighter();
              }
            }
          }

          s1 = "";
          continue;
        }
      }

      s1 += c;
    }

    if (s1.length() > 0) {
      append(s1, foreground, background, bold, underline);
    }

    setCursorInEnd();

  }
  private int lastLine;
  private String getCommand() {
    try {
      return doc.getText(lastLine,doc.getLength()-lastLine);
    } catch (BadLocationException e) {
      return "";
    }
  }
  private void append(String s, Color fg, Color bg, boolean bold, boolean underline) {
    try {
      //If there is text in the document
      if (doc.getLength() > 0) {
        //If the document ends with >
        if (Objects.equals(getCommand(), "")) {
          //Remove the filter as it will block this operation.
          doc.setDocumentFilter(null);
          //Remove the > as its just a placeholder
          doc.remove(lastLine - 1, 1);
          //Add the filter back
          doc.setDocumentFilter(filter);
        } else {
          //Insert a line break
          doc.insertString(doc.getLength(),"\n",null);
        }
      }
      StyleContext sc = StyleContext.getDefaultStyleContext();

      setCursorInEnd();

      setCharacterAttributes(sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, fg), false);
      setCharacterAttributes(sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Background, bg), false);
      setCharacterAttributes(sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Bold, bold), false);
      setCharacterAttributes(sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Underline, underline), false);
      doc.setDocumentFilter(null);
      replaceSelection(s);
      doc.setDocumentFilter(filter);
      //Update the position of the >
      lastLine = doc.getLength()+1;
      doc.insertString(doc.getLength(),">",null);
    } catch (BadLocationException ignored) {}
  }

  private void setCursorInEnd() {
    setCaretPosition(doc.getLength());
  }

  public class KeyEventListener extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        String c = getCommand();
        if (c.length() == 0) return;
        for (InputListener l : inputListeners) {
          l.processCommand(JTerminal.this, c);
        }
        return;
      }
      if (e.getKeyCode() == KeyEvent.VK_C && e.isControlDown()) {
        for (InputListener l : inputListeners) {
          l.onTerminate(JTerminal.this);
        }
      }
    }
  }

  public void addInputListener(InputListener listener) {
    inputListeners.add(listener);
  }

  public void removeInputListener(InputListener listener) {
    inputListeners.remove(listener);
  }

}
