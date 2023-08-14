public class TestPropertyChangeSupport implements PropertyChangeListener {
    private static final String NAME = "property";
    public static void main(String[] args) {
        for (int i = 1; i <= 3; i++) {
            test(i, 1, 10000000);
            test(i, 10, 1000000);
            test(i, 100, 100000);
            test(i, 1000, 10000);
            test(i, 10000, 1000);
            test(i, 20000, 1000);
        }
    }
    private static void test(int step, int listeners, int attempts) {
        TestPropertyChangeSupport test = new TestPropertyChangeSupport();
        PropertyChangeSupport pcs = new PropertyChangeSupport(test);
        PropertyChangeEvent eventNull = new PropertyChangeEvent(test, null, null, null);
        PropertyChangeEvent eventName = new PropertyChangeEvent(test, NAME, null, null);
        long time1 = System.currentTimeMillis();
        for (int i = 0; i < listeners; i++) {
            pcs.addPropertyChangeListener(test);
            pcs.addPropertyChangeListener(NAME, test);
        }
        long time2 = System.currentTimeMillis();
        for (int i = 0; i < attempts; i++) {
            pcs.firePropertyChange(eventNull);
            pcs.firePropertyChange(eventName);
        }
        long time3 = System.currentTimeMillis();
        time1 = time2 - time1; 
        time2 = time3 - time2; 
        System.out.println("Step: " + step
                        + "; Listeners: " + listeners
                        + "; Attempts: " + attempts
                        + "; Time (ms): " + time1 + "/" + time2);
    }
    public void propertyChange(PropertyChangeEvent event) {
    }
}
