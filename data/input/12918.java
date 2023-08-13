public class bug6739756 {
    public static void main(String[] args) throws Exception {
        try {
           UIManager.setLookAndFeel(
                   "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JToolBar tb = new JToolBar();
                Dimension preferredSize = tb.getPreferredSize();
                JButton button = new JButton("Test");
                button.setVisible(false);
                tb.add(button);
                if (!preferredSize.equals(tb.getPreferredSize())) {
                    throw new RuntimeException("Toolbar's preferredSize is wrong");
                }
            }
        });
    }
}
