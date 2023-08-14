public class bug6636983 {
    private Robot robot;
    private final AttributedString Hiragana_A = new AttributedString("\u3042");
    void sendInputMethodEvent() {
        InputMethodEvent ime = new InputMethodEvent(
                ep,
                InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                Hiragana_A.getIterator(),
                0,
                null,
                null);
        ep.dispatchEvent(ime);
    }
    void checkComposedTextRun() {
        HTMLDocument d = (HTMLDocument) ep.getDocument();
        ElementIterator it = new ElementIterator(d.getDefaultRootElement());
        while (true) {
            Element e = it.next();
            if (e == null) {
                throw new RuntimeException("no composed text found");
            }
            AttributeSet a = e.getAttributes();
            if (a.isDefined(StyleConstants.ComposedTextAttribute)) {
                if (!AbstractDocument.ContentElementName.equals(a.getAttribute(StyleConstants.NameAttribute))) {
                    throw new RuntimeException("AbstractDocument.ContentElementName.equals(a.getAttribute(StyleConstants.NameAttribute)) is false");
                }
                if (a.isDefined(SwingUtilities2.IMPLIED_CR)) {
                    throw new RuntimeException("a.isDefined(SwingUtilities2.IMPLIED_CR) is true");
                }
                return;
            }
        }
    }
    JEditorPane ep;
    void initAtParagraphStart() {
        ep.setText("A<p>B");
        hitKey(KeyEvent.VK_LEFT);
    }
    void sendAtParagraphStart() {
        sendInputMethodEvent();
    }
    void checkAtParagraphStart() {
        checkComposedTextRun();
    }
    void initAfterBRElement() {
        ep.setText("A<br>B");
        hitKey(KeyEvent.VK_LEFT);
    }
    void sendAtBRElement() {
        sendInputMethodEvent();
    }
    void checkAtBrElement() {
        checkComposedTextRun();
    }
    private void hitKey(int keycode) {
        robot.keyPress(keycode);
        robot.keyRelease(keycode);
        robot.delay(550); 
    }
    private void run() throws Exception {
        robot = new Robot();
        ep = new JEditorPane();
        ep.setContentType("text/html");
        ep.setPreferredSize(new Dimension(100, 100));
        JFrame frame = new JFrame("Test: " + getClass().getName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(ep);
        frame.setVisible(true);
    }
    public static void main(String[] args) throws Throwable {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    bug6636983 bug6636983 = new bug6636983();
                    bug6636983.run();
                    bug6636983.initAtParagraphStart();
                    bug6636983.sendAtParagraphStart();
                    bug6636983.checkAtParagraphStart();
                    bug6636983.initAfterBRElement();
                    bug6636983.sendAtBRElement();
                    bug6636983.checkAtBrElement();
                    System.out.println("OK");
                } catch (Exception e) {
                    throw new RuntimeException("The test failed", e);
                }
            }
        });
    }
}
