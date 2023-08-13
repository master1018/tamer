public class TestSynchronization {
    private static boolean isCalled;
    public static void main(String[] args) {
        final JButton button = new JButton();
        button.addPropertyChangeListener("name", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                isCalled = true;
            }
        });
        button.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                for (PropertyChangeListener listener : button.getPropertyChangeListeners())
                    button.removePropertyChangeListener(listener);
            }
        });
        button.setName(null);
        if (!isCalled)
            throw new Error("test failed");
    }
}
