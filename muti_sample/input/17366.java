public final class TestEquals implements PropertyChangeListener {
    private static final String PROPERTY = "property";
    public static void main(String[] args) {
        TestEquals one = new TestEquals(1);
        TestEquals two = new TestEquals(2);
        Object source = TestEquals.class;
        PropertyChangeSupport pcs = new PropertyChangeSupport(source);
        pcs.addPropertyChangeListener(PROPERTY, one);
        pcs.addPropertyChangeListener(PROPERTY, two);
        PropertyChangeEvent event = new PropertyChangeEvent(source, PROPERTY, one, two);
        pcs.firePropertyChange(event);
        test(one, two, 1); 
        pcs.firePropertyChange(PROPERTY, one, two);
        test(one, two, 2); 
        pcs.fireIndexedPropertyChange(PROPERTY, 1, one, two);
        test(one, two, 2); 
    }
    private static void test(TestEquals v1, TestEquals v2, int amount) {
        int count = v1.count + v2.count;
        if (amount < count)
            throw new Error("method equals() is called " + count + " times");
        v1.count = 0;
        v2.count = 0;
    }
    private final int value;
    private int count;
    private TestEquals(int value) {
        this.value = value;
    }
    @Override
    public boolean equals(Object object) {
        if (object instanceof TestEquals) {
            this.count++;
            TestEquals that = (TestEquals)object;
            return that.value == this.value;
        }
        return false;
    }
    public void propertyChange(PropertyChangeEvent event) {
    }
}
