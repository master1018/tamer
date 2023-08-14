public class bug6559589 {
    private static void createGui() {
        JScrollPane sp = new JScrollPane();
        int listenerCount = sp.getPropertyChangeListeners().length;
        sp.updateUI();
        if(listenerCount != sp.getPropertyChangeListeners().length) {
            throw new RuntimeException("Listeners' leak");
        }
    }
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                bug6559589.createGui();
            }
        });
    }
}
