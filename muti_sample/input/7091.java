public class ThreadInfoPanel extends JPanel {
    private JTextArea textArea;
    public ThreadInfoPanel() {
        initUI();
    }
    private void initUI() {
        setLayout(new BorderLayout());
        JScrollPane scroller = new JScrollPane();
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        scroller.getViewport().add(textArea);
        add(scroller, BorderLayout.CENTER);
    }
    public ThreadInfoPanel(final JavaThread thread) {
        initUI();
        setJavaThread(thread);
    }
    public void setJavaThread(final JavaThread thread) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream tty = new PrintStream(bos);
        tty.println("Thread Info: " + thread.getThreadName());
        thread.printInfoOn(tty);
        textArea.setText(bos.toString());
    }
}
