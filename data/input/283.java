public class XMLKitTest {
    private static JEditorPane editor = null;
    public static void main(String[] args) {
        try {
            editor = new JEditorPane();
            XMLEditorKit kit = new XMLEditorKit();
            editor.setEditorKit(kit);
            editor.setFont(new Font("Courier", Font.PLAIN, 12));
            editor.getDocument().putProperty(PlainDocument.tabSizeAttribute, new Integer(4));
            kit.setAutoIndentation(true);
            editor.getDocument().putProperty(XMLEditorKit.ERROR_HIGHLIGHTING_ATTRIBUTE, new Boolean(true));
            kit.setTagCompletion(true);
            kit.setStyle(XMLStyleConstants.ATTRIBUTE_NAME, new Color(255, 0, 0), Font.BOLD);
            ScrollableEditorPanel editorPanel = new ScrollableEditorPanel(editor);
            JScrollPane scroller = new JScrollPane(editorPanel);
            JPanel rowHeader = new JPanel(new BorderLayout());
            rowHeader.add(new XMLFoldingMargin(editor), BorderLayout.EAST);
            rowHeader.add(new LineNumberMargin(editor), BorderLayout.WEST);
            scroller.setRowHeaderView(rowHeader);
            editor.read(new XmlReader(XMLKitTest.class.getResourceAsStream("/test.xml")), null);
            JFrame f = new JFrame("XmlEditorKitTest: " + "demo");
            f.getContentPane().setLayout(new BorderLayout());
            f.getContentPane().add(scroller, BorderLayout.CENTER);
            f.setSize(600, 600);
            f.setVisible(true);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
