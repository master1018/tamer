public abstract class EnumControl extends Control {
    public static class Type extends Control.Type {
        public static final Type REVERB = new Type("Reverb"); 
        protected Type(String name) {
            super(name);
        }
    }
    private Object[] values;
    private Object value;
    protected EnumControl(EnumControl.Type type, Object[] values, Object value) {
        super(type);
        this.value = value;
        this.values = values;
    }
    public void setValue(Object value) {
        for (Object val : values) {
            if (val.equals(value)) {
                this.value = value;
                return;
            }
        }
        throw new IllegalArgumentException(Messages.getString("sound.0D")); 
    }
    public Object getValue() {
        return value;
    }
    public Object[] getValues() {
        return values;
    }
    public String toString() {
        return getType() + " with current value: " + value; 
    }
}
