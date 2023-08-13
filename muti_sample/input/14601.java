public class Test6963870 implements Runnable {
    final static String[] UI_NAMES = {
        "List.focusCellHighlightBorder",
        "List.focusSelectedCellHighlightBorder",
        "List.noFocusBorder",
        "Table.focusCellHighlightBorder",
        "Table.focusSelectedCellHighlightBorder",
    };
    public void run() {
        for (String uiName: UI_NAMES) {
            test(uiName);
        }
    }
    void test(String uiName) {
        Border b = UIManager.getBorder(uiName);
        Insets i = b.getBorderInsets(null);
        if (i == null) {
            throw new RuntimeException("getBorderInsets() returns null for " + uiName);
        }
    }
    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
        } catch (Exception e) {
            System.out.println("GTKLookAndFeel cannot be set, skipping this test");
            return;
        }
        SwingUtilities.invokeAndWait(new Test6963870());
    }
}
