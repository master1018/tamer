public class bug6771184 {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JTextArea textArea = new JTextArea("Tested string");
                Highlighter highlighter = textArea.getHighlighter();
                Highlighter.HighlightPainter myPainter = new Highlighter.HighlightPainter() {
                    public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
                    }
                };
                int negativeTestedData[][] = {{50, 0},
                        {-1, 1},
                        {-5, -4},
                        {Integer.MAX_VALUE, Integer.MIN_VALUE},
                        {Integer.MIN_VALUE, Integer.MAX_VALUE},
                        {Integer.MIN_VALUE, Integer.MIN_VALUE}};
                for (int[] data : negativeTestedData) {
                    try {
                        highlighter.addHighlight(data[0], data[1], myPainter);
                        throw new RuntimeException("Method addHighlight() does not throw BadLocationException for (" +
                                data[0] + ", " + data[1] + ") ");
                    } catch (BadLocationException e) {
                    }
                    Object objRef;
                    try {
                        objRef = highlighter.addHighlight(0, 1, myPainter);
                    } catch (BadLocationException e) {
                        throw new RuntimeException("highlighter.addHighlight(0, 1, myPainter) throws exception", e);
                    }
                    try {
                        highlighter.changeHighlight(objRef, data[0], data[1]);
                        throw new RuntimeException("Method changeHighlight() does not throw BadLocationException for (" +
                                data[0] + ", " + data[1] + ") ");
                    } catch (BadLocationException e) {
                    }
                }
            }
        });
    }
}
