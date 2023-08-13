public class bug6994419 {
    public static void main(String... args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                JLayer<JComponent> l = new JLayer<JComponent>(new JButton());
                l.removeAll();
                l.addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        throw new RuntimeException("Property change event was unexpectedly fired");
                    }
                });
                l.removeAll();
            }
        });
    }
}
