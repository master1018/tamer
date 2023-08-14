public class bug7003777 {
    private static final String[] TEST_STRINGS = {
            "&a",
            "&aa",
            "&a;",
            "&aa;",
    };
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JTextPane pane = new JTextPane();
                pane.setContentType("text/html");
                for (String testString : TEST_STRINGS) {
                    pane.setText(testString);
                    String parsedText;
                    try {
                        parsedText = pane.getDocument().getText(0, pane.getDocument().getLength());
                    } catch (BadLocationException e) {
                        throw new RuntimeException("The test failed.", e);
                    }
                    if (parsedText.charAt(0) != '\n') {
                        throw new RuntimeException("The first char should be \\n");
                    }
                    parsedText = parsedText.substring(1);
                    if (!testString.equals(parsedText)) {
                        throw new RuntimeException("The '" + testString +
                                "' string wasn't parsed correctly. Parsed value is '" + parsedText + "'");
                    }
                }
            }
        });
    }
}
