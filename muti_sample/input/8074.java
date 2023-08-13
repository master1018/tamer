public class Test4353056 implements PropertyChangeListener {
    private static final int COUNT = 100;
    private static final String COLOR = "color";
    private static final String BOOLEAN = "boolean";
    private static final String INTEGER = "integer";
    public static void main(String[] args) throws Exception {
        Test4353056 test = new Test4353056();
        test.addPropertyChangeListener(test);
        for (int i = 0; i < COUNT; i++) {
            boolean even = i % 2 == 0;
            test.setColor(i, i % 3 == 0 ? Color.RED : Color.BLUE);
            test.setBoolean(i, even);
            test.setInteger(i, i);
        }
    }
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private Color color;
    private boolean flag;
    private int value;
    private String name;
    private int index = -1;
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }
    public void setColor(int index, Color color) {
        Color oldColor = this.color;
        this.color = color;
        this.index = index;
        this.name = COLOR;
        this.pcs.fireIndexedPropertyChange(COLOR, index,
                oldColor, color);
    }
    public void setBoolean(int index, boolean flag) {
        boolean oldBool = this.flag;
        this.flag = flag;
        this.index = index;
        this.name = BOOLEAN;
        this.pcs.fireIndexedPropertyChange(BOOLEAN, index,
                oldBool, flag);
    }
    public void setInteger(int index, int value) {
        int oldInt = this.value;
        this.value = value;
        this.index = index;
        this.name = INTEGER;
        this.pcs.fireIndexedPropertyChange(INTEGER, index,
                oldInt, value);
    }
    public void propertyChange(PropertyChangeEvent event) {
        Object value = event.getNewValue();
        if (value.equals(event.getOldValue())) {
            throw new Error("new value is equal to old one");
        }
        if (!this.name.equals(event.getPropertyName())) {
            throw new Error("unexpected property name");
        } else if (this.name.equals(COLOR)) {
            if (!value.equals(this.color)) {
                throw new Error("unexpected object value");
            }
        } else if (this.name.equals(BOOLEAN)) {
            if (!value.equals(Boolean.valueOf(this.flag))) {
                throw new Error("unexpected boolean value");
            }
        } else if (this.name.equals(INTEGER)) {
            if (!value.equals(Integer.valueOf(this.value))) {
                throw new Error("unexpected integer value");
            }
        } else {
            throw new Error("unexpected property name");
        }
        if (event instanceof IndexedPropertyChangeEvent) {
            IndexedPropertyChangeEvent ipce = (IndexedPropertyChangeEvent) event;
            if (this.index != ipce.getIndex()) {
                throw new Error("unexpected property index");
            }
        } else {
            throw new Error("unexpected event type");
        }
        System.out.println(this.name + " at " + this.index + " is " + value);
        this.name = null;
        this.index = -1;
    }
}
