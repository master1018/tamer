public class bug6796710 {
    public static final String TEXT = "<html>" +
            "<body>" +
            "<table cellpadding=\"0\" cellspacing=\"0\" border=\"1\">" +
            "    <tbody>" +
            "        <tr>" +
            "            <td>Col1</td>" +
            "            <td>Col2</td>" +
            "            <td>Col3</td>" +
            "        </tr>" +
            "        <tr>" +
            "            <td>1. It's a regression from CR 4419748. The problem is in the CSSBorder#paintBorder, which ignores clip area while painting.</td>" +
            "            <td>2. It's a regression from CR 4419748. The problem is in the CSSBorder#paintBorder, which ignores clip area while painting.</td>" +
            "            <td>3. It's a regression from CR 4419748. The problem is in the CSSBorder#paintBorder, which ignores clip area while painting.</td>" +
            "        </tr>" +
            "    </tbody>" +
            "</table>" +
            "</body>" +
            "</html>";
    private static Robot robot;
    private static JFrame frame;
    private static JPanel pnBottom;
    public static void main(String[] args) throws Exception {
        robot = new Robot();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                frame = new JFrame();
                pnBottom = new JPanel();
                pnBottom.add(new JLabel("Some label"));
                pnBottom.add(new JButton("A button"));
                JEditorPane editorPane = new JEditorPane();
                editorPane.setContentType("text/html");
                editorPane.setText(TEXT);
                editorPane.setEditable(false);
                JPanel pnContent = new JPanel(new BorderLayout());
                pnContent.add(new JScrollPane(editorPane), BorderLayout.CENTER);
                pnContent.add(pnBottom, BorderLayout.SOUTH);
                frame.setContentPane(pnContent);
                frame.setSize(400, 600);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
        ((SunToolkit) SunToolkit.getDefaultToolkit()).realSync();
        BufferedImage bufferedImage = getPnBottomImage();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                frame.setSize(400, 150);
            }
        });
        ((SunToolkit) SunToolkit.getDefaultToolkit()).realSync();
        Thread.sleep(1000);
        if (!Util.compareBufferedImages(bufferedImage, getPnBottomImage())) {
            throw new RuntimeException("The test failed");
        }
        System.out.println("The test bug6796710 passed.");
    }
    private static BufferedImage getPnBottomImage() {
        Rectangle rect = pnBottom.getBounds();
        Util.convertRectToScreen(rect, pnBottom.getParent());
        return robot.createScreenCapture(rect);
    }
}
