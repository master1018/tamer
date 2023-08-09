public class bug6824395 {
    static JScrollPane scrollPane;
    public static void main(String[] args) throws Exception {
        SunToolkit toolkit = (SunToolkit) Toolkit.getDefaultToolkit();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("testing");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                JEditorPane editorPane = new JEditorPane();
                String str = "hello\n";
                for(int i = 0; i<5; i++) {
                    str += str;
                }
                editorPane.setText(str);
                JLayer<JEditorPane> editorPaneLayer = new JLayer<JEditorPane>(editorPane);
                LayerUI<JComponent> layerUI = new LayerUI<JComponent>();
                editorPaneLayer.setUI(layerUI);
                scrollPane = new JScrollPane(editorPaneLayer);
                scrollPane.setPreferredSize(new Dimension(200, 250));
                frame.add(scrollPane);
                frame.setSize(200, 200);
                frame.pack();
                frame.setVisible(true);
            }
        });
        toolkit.realSync();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                if (scrollPane.getViewportBorderBounds().width != scrollPane.getViewport().getView().getWidth()) {
                    throw new RuntimeException("Wrong component's width!");
                }
            }
        });
    }
}
