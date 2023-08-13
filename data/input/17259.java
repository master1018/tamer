import sun.jvm.hotspot.ui.classbrowser.*;
public class JavaStackTracePanel extends JPanel {
    private JSplitPane          splitPane;
    private SAEditorPane        stackTraceEditor;
    private SAEditorPane        contentEditor;
    private HTMLGenerator htmlGen = new HTMLGenerator();
    public JavaStackTracePanel() {
        initUI();
    }
    private void initUI() {
        setLayout(new BorderLayout());
        HyperlinkListener hyperListener = new HyperlinkListener() {
                         public void hyperlinkUpdate(HyperlinkEvent e) {
                            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                               setContentText(htmlGen.genHTMLForHyperlink(e.getDescription()));
                            }
                         }
                      };
        stackTraceEditor = new SAEditorPane();
        stackTraceEditor.addHyperlinkListener(hyperListener);
        contentEditor = new SAEditorPane();
        contentEditor.addHyperlinkListener(hyperListener);
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(new JScrollPane(stackTraceEditor), BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(new JScrollPane(contentEditor), BorderLayout.CENTER);
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, bottomPanel);
        splitPane.setDividerLocation(0.4);
        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
    }
    public void setJavaThread(final JavaThread thread) {
        setStackTraceText(htmlGen.genHTMLForJavaStackTrace(thread));
    }
    private void setStackTraceText(String text) {
        stackTraceEditor.setText(text);
    }
    private void setContentText(String text) {
        contentEditor.setText(text);
    }
}
