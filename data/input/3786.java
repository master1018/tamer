public class TestMethods extends PropertyChangeSupport implements PropertyChangeListener {
    private static final String NAME = "property";
    public static void main(String[] args) {
        Object source = new Object();
        new TestMethods(source).firePropertyChange(new PropertyChangeEvent(source, NAME, null, null));
        new TestMethods(source).firePropertyChange(NAME, null, null);
        new TestMethods(source).firePropertyChange(NAME, 0, 1);
        new TestMethods(source).firePropertyChange(NAME, true, false);
        new TestMethods(source).fireIndexedPropertyChange(NAME, 0, null, null);
        new TestMethods(source).fireIndexedPropertyChange(NAME, 0, 0, 1);
        new TestMethods(source).fireIndexedPropertyChange(NAME, 0, true, false);
    }
    private Fire state;
    public TestMethods(Object source) {
        super(source);
        addPropertyChangeListener(this);
    }
    public void propertyChange(PropertyChangeEvent event) {
        if (this.state != Fire.PropertyChangeEvent)
            throw new Error("Illegal state: " + this.state);
    }
    @Override
    public void firePropertyChange(String property, Object oldValue, Object newValue) {
        if ((this.state != null) && (this.state != Fire.PropertyBoolean) && (this.state != Fire.PropertyInteger))
            throw new Error("Illegal state: " + this.state);
        this.state = Fire.PropertyObject;
        super.firePropertyChange(property, oldValue, newValue);
    }
    @Override
    public void firePropertyChange(String property, int oldValue, int newValue) {
        if (this.state != null)
            throw new Error("Illegal state: " + this.state);
        this.state = Fire.PropertyInteger;
        super.firePropertyChange(property, oldValue, newValue);
    }
    @Override
    public void firePropertyChange(String property, boolean oldValue, boolean newValue) {
        if (this.state != null)
            throw new Error("Illegal state: " + this.state);
        this.state = Fire.PropertyBoolean;
        super.firePropertyChange(property, oldValue, newValue);
    }
    @Override
    public void firePropertyChange(PropertyChangeEvent event) {
        if ((this.state != null) && (this.state != Fire.PropertyObject) && (this.state != Fire.IndexedPropertyObject))
            throw new Error("Illegal state: " + this.state);
        this.state = Fire.PropertyChangeEvent;
        super.firePropertyChange(event);
    }
    @Override
    public void fireIndexedPropertyChange(String property, int index, Object oldValue, Object newValue) {
        if ((this.state != null) && (this.state != Fire.IndexedPropertyBoolean) && (this.state != Fire.IndexedPropertyInteger))
            throw new Error("Illegal state: " + this.state);
        this.state = Fire.IndexedPropertyObject;
        super.fireIndexedPropertyChange(property, index, oldValue, newValue);
    }
    @Override
    public void fireIndexedPropertyChange(String property, int index, int oldValue, int newValue) {
        if (this.state != null)
            throw new Error("Illegal state: " + this.state);
        this.state = Fire.IndexedPropertyInteger;
        super.fireIndexedPropertyChange(property, index, oldValue, newValue);
    }
    @Override
    public void fireIndexedPropertyChange(String property, int index, boolean oldValue, boolean newValue) {
        if (this.state != null)
            throw new Error("Illegal state: " + this.state);
        this.state = Fire.IndexedPropertyBoolean;
        super.fireIndexedPropertyChange(property, index, oldValue, newValue);
    }
}
