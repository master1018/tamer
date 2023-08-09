public class DebuggerConsolePanel extends JPanel {
  private Debugger debugger;
  private JTextComponent editor;
  private boolean updating;
  private int     mark;
  private String  curText;  
  private static final boolean DEBUGGING = false;
  public DebuggerConsolePanel(Debugger debugger) {
    this.debugger = debugger;
    if (!DEBUGGING) {
      if (Assert.ASSERTS_ENABLED) {
        Assert.that(debugger.hasConsole(), "should not create a DebuggerConsolePanel for non-console debuggers");
      }
    }
    setLayout(new BorderLayout());
    editor = new JTextArea();
    editor.setDocument(new EditableAtEndDocument());
    editor.setFont(GraphicsUtilities.lookupFont("Courier"));
    JScrollPane scroller = new JScrollPane();
    scroller.getViewport().add(editor);
    add(scroller, BorderLayout.CENTER);
    editor.getDocument().addDocumentListener(new DocumentListener() {
        public void changedUpdate(DocumentEvent e) {
        }
        public void insertUpdate(DocumentEvent e) {
          if (updating) return;
          beginUpdate();
          editor.setCaretPosition(editor.getDocument().getLength());
          if (insertContains(e, '\n')) {
            String cmd = getMarkedText();
            if ((cmd.length() == 0) || (cmd.charAt(cmd.length() - 1) != '\\')) {
              cmd = trimContinuations(cmd);
              final String result;
              if (DEBUGGING) {
                System.err.println("Entered command: \"" + cmd + "\"");
                result = "";
              } else {
                result = DebuggerConsolePanel.this.debugger.consoleExecuteCommand(cmd);
              }
              SwingUtilities.invokeLater(new Runnable() {
                  public void run() {
                    print(result);
                    printPrompt();
                    setMark();
                    endUpdate();
                  }
                });
            }
          } else {
            endUpdate();
          }
        }
        public void removeUpdate(DocumentEvent e) {
        }
      });
    editor.addCaretListener(new CaretListener() {
        public void caretUpdate(CaretEvent e) {
          int len = editor.getDocument().getLength();
          if (e.getDot() > len) {
            editor.setCaretPosition(len);
          }
        }
      });
    Box hbox = Box.createHorizontalBox();
    hbox.add(Box.createGlue());
    JButton button = new JButton("Clear Saved Text");
    button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          clear();
        }
      });
    hbox.add(button);
    hbox.add(Box.createGlue());
    add(hbox, BorderLayout.SOUTH);
    clear();
  }
  public void requestFocus() {
    editor.requestFocus();
  }
  public void clear() {
    EditableAtEndDocument d = (EditableAtEndDocument) editor.getDocument();
    d.clear();
    printPrompt();
    setMark();
    editor.requestFocus();
  }
  public void setMark() {
    ((EditableAtEndDocument) editor.getDocument()).setMark();
  }
  public String getMarkedText() {
    try {
      String s = ((EditableAtEndDocument) editor.getDocument()).getMarkedText();
      int i = s.length();
      while ((i > 0) && (s.charAt(i - 1) == '\n')) {
        i--;
      }
      return s.substring(0, i);
    }
    catch (BadLocationException e) {
      e.printStackTrace();
      return null;
    }
  }
  private void beginUpdate() {
    updating = true;
  }
  private void endUpdate() {
    updating = false;
  }
  private void print(String s) {
    Document d = editor.getDocument();
    try {
      d.insertString(d.getLength(), s, null);
    }
    catch (BadLocationException e) {
      e.printStackTrace();
    }
  }
  private void printPrompt() {
    if (DEBUGGING) {
      print("foo> ");
    } else {
      print(debugger.getConsolePrompt());
    }
  }
  private boolean insertContains(DocumentEvent e, char c) {
    String s = null;
    try {
      s = editor.getText(e.getOffset(), e.getLength());
      for (int i = 0; i < e.getLength(); i++) {
        if (s.charAt(i) == c) {
          return true;
        }
      }
    }
    catch (BadLocationException ex) {
      ex.printStackTrace();
    }
    return false;
  }
  private String trimContinuations(String text) {
    int i;
    while ((i = text.indexOf("\\\n")) >= 0) {
      text = text.substring(0, i) + text.substring(i+2, text.length());
    }
    return text;
  }
  public static void main(String[] args) {
    JFrame frame = new JFrame();
    frame.getContentPane().setLayout(new BorderLayout());
    DebuggerConsolePanel panel = new DebuggerConsolePanel(null);
    frame.getContentPane().add(panel, BorderLayout.CENTER);
    frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          System.exit(0);
        }
      });
    frame.setSize(500, 500);
    frame.setVisible(true);
    panel.requestFocus();
  }
}
