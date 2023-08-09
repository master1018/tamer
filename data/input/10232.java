public class HeapParametersPanel extends JPanel {
  public HeapParametersPanel() {
    super();
    setLayout(new BorderLayout());
    JScrollPane scroller = new JScrollPane();
    JTextArea textArea = new JTextArea();
    textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    scroller.getViewport().add(textArea);
    add(scroller, BorderLayout.CENTER);
    Universe u = VM.getVM().getUniverse();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream tty = new PrintStream(bos);
    tty.println("Heap Parameters:");
    u.heap().printOn(tty);
    textArea.setText(bos.toString());
  }
}
