public class TypeScript extends JPanel {
    private static final long serialVersionUID = -983704841363534885L;
    private JTextArea history;
    private JTextField entry;
    private JLabel promptLabel;
    private JScrollBar historyVScrollBar;
    private JScrollBar historyHScrollBar;
    private boolean echoInput = false;
    private static String newline = System.getProperty("line.separator");
    public TypeScript(String prompt) {
        this(prompt, true);
    }
    public TypeScript(String prompt, boolean echoInput) {
        this.echoInput = echoInput;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        history = new JTextArea(0, 0);
        history.setEditable(false);
        JScrollPane scroller = new JScrollPane(history);
        historyVScrollBar = scroller.getVerticalScrollBar();
        historyHScrollBar = scroller.getHorizontalScrollBar();
        add(scroller);
        JPanel cmdLine = new JPanel();
        cmdLine.setLayout(new BoxLayout(cmdLine, BoxLayout.X_AXIS));
        promptLabel = new JLabel(prompt + " ");
        cmdLine.add(promptLabel);
        entry = new JTextField();
entry.setMaximumSize(new Dimension(1000, 20));
        cmdLine.add(entry);
        add(cmdLine);
    }
    public void setPrompt(String prompt) {
        promptLabel.setText(prompt + " ");
    }
    public void append(String text) {
        history.append(text);
        historyVScrollBar.setValue(historyVScrollBar.getMaximum());
        historyHScrollBar.setValue(historyHScrollBar.getMinimum());
    }
    public void newline() {
        history.append(newline);
        historyVScrollBar.setValue(historyVScrollBar.getMaximum());
        historyHScrollBar.setValue(historyHScrollBar.getMinimum());
    }
    public void flush() {}
    public void addActionListener(ActionListener a) {
        entry.addActionListener(a);
    }
    public void removeActionListener(ActionListener a) {
        entry.removeActionListener(a);
    }
    public String readln() {
        String text = entry.getText();
        entry.setText("");
        if (echoInput) {
            history.append(">>>");
            history.append(text);
            history.append(newline);
            historyVScrollBar.setValue(historyVScrollBar.getMaximum());
            historyHScrollBar.setValue(historyHScrollBar.getMinimum());
        }
        return text;
    }
}
