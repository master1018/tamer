public class bug6670274 {
    private static void createGui() {
        final JTabbedPane pane = new JTabbedPane();
        TestTabbedPaneUI ui = new TestTabbedPaneUI();
        pane.setUI(ui);
        pane.add("one", new JPanel());
        pane.add("<html><i>Two</i></html>", new JPanel());
        pane.add("three", new JPanel());
        pane.setTitleAt(0, "<html><i>ONE</i></html>");
        check(ui, 0, 1);
        pane.setTitleAt(1, "hello");
        check(ui, 0);
        pane.setTitleAt(0, "<html>html</html>");
        pane.setTitleAt(2, "<html>html</html>");
        check(ui, 0, 2);
    }
    private static void check(TestTabbedPaneUI ui, int... indices) {
        for(int i = 0; i < ui.getTabbedPane().getTabCount(); i++) {
            System.out.print("Checking tab #" + i);
            View view = ui.getTextViewForTab(i);
            boolean found = false;
            for (int j = 0; j < indices.length; j++) {
                if (indices[j]== i) {
                    found = true;
                    break;
                }
            }
            System.out.print("; view = " + view);
            if (found) {
                if (view == null) {
                    throw new RuntimeException("View is unexpectedly null");
                }
            } else if (view != null) {
                throw new RuntimeException("View is unexpectedly not null");
            }
            System.out.println(" ok");
        }
        System.out.println("");
    }
    static class TestTabbedPaneUI extends BasicTabbedPaneUI {
        public View getTextViewForTab(int tabIndex) {
            return super.getTextViewForTab(tabIndex);
        }
        public JTabbedPane getTabbedPane() {
            return tabPane;
        }
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                bug6670274.createGui();
            }
        });
    }
}
