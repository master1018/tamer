public class bug7045593 {
    private static volatile JTextField jtf;
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                jtf = new JTextField("WW");
                JFrame frame = new JFrame();
                frame.getContentPane().add(jtf);
                frame.pack();
                frame.setVisible(true);
            }
        });
        ((SunToolkit) SunToolkit.getDefaultToolkit()).realSync();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    Rectangle r = jtf.modelToView(1);
                    int delta = 2;
                    for (int x = r.x - delta; x < r.x + delta; x++) {
                        assertEquals(jtf.viewToModel(new Point(x, r.y)), 1);
                    }
                    System.out.println("Passed.");
                } catch (BadLocationException e) {
                    throw new RuntimeException("Test failed", e);
                }
            }
        });
    }
    private static void assertEquals(int i1, int i2) {
        if (i1 != i2) {
            throw new RuntimeException("Test failed, " + i1 + " != " + i2);
        }
    }
}
