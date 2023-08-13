public class DeadlockDetectionPanel extends JPanel {
    public DeadlockDetectionPanel() {
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
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream tty = new PrintStream(bos);
        printDeadlocks(tty);
        textArea.setText(bos.toString());
    }
    private void printDeadlocks(PrintStream tty) {
        DeadlockDetector.print(tty);
    }
}
