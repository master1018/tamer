public abstract class EnumControl extends Control {
    private Object[] values;
    private Object value;
    protected EnumControl(Type type, Object[] values, Object value) {
        super(type);
        this.values = values;
        this.value = value;
    }
    public void setValue(Object value) {
        if (!isValueSupported(value)) {
            throw new IllegalArgumentException("Requested value " + value + " is not supported.");
        }
        this.value = value;
    }
    public Object getValue() {
        return value;
    }
    public Object[] getValues() {
        Object[] localArray = new Object[values.length];
        for (int i = 0; i < values.length; i++) {
            localArray[i] = values[i];
        }
        return localArray;
    }
    private boolean isValueSupported(Object value) {
        for (int i = 0; i < values.length; i++) {
            if (value.equals(values[i])) {
                return true;
            }
        }
        return false;
    }
    public String toString() {
        return new String(getType() + " with current value: " + getValue());
    }
    public static class Type extends Control.Type {
        public static final Type REVERB         = new Type("Reverb");
        protected Type(String name) {
            super(name);
        }
    } 
} 
