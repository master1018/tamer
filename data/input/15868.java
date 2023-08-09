public class CommandProcessorPanel extends JPanel {
    private CommandProcessor commands;
    private JTextArea editor;
    private boolean updating;
    private int     mark;
    private String  curText;  
    private static final boolean DEBUGGING = false;
    ByteArrayOutputStream baos = new ByteArrayOutputStream(10240);
    public CommandProcessorPanel(CommandProcessor cp) {
        commands = cp;
        setLayout(new BorderLayout());
        editor = new JTextArea();
        editor.setDocument(new EditableAtEndDocument());
        editor.setFont(GraphicsUtilities.lookupFont("Courier"));
        JScrollPane scroller = new JScrollPane();
        scroller.getViewport().add(editor);
        add(scroller, BorderLayout.CENTER);
        PrintStream o = new PrintStream(baos, true);
        cp.setOutput(o);
        cp.setErr(o);
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
                            final String ln = trimContinuations(cmd);
                            SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        beginUpdate();
                                        try {
                                            commands.executeCommand(ln);
                                            commands.printPrompt();
                                            Document d = editor.getDocument();
                                            try {
                                                d.insertString(d.getLength(), baos.toString(), null);
                                            }
                                            catch (BadLocationException ble) {
                                                ble.printStackTrace();
                                            }
                                            baos.reset();
                                            editor.setCaretPosition(editor.getDocument().getLength());
                                            setMark();
                                        } finally {
                                            endUpdate();
                                        }
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
        commands.executeCommand("");
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
        CommandProcessorPanel panel = new CommandProcessorPanel(null);
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
