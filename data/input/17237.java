public abstract class BooleanControl extends Control {
    private final String trueStateLabel;
    private final String falseStateLabel;
    private boolean value;
    protected BooleanControl(Type type, boolean initialValue, String trueStateLabel, String falseStateLabel) {
        super(type);
        this.value = initialValue;
        this.trueStateLabel = trueStateLabel;
        this.falseStateLabel = falseStateLabel;
    }
    protected BooleanControl(Type type, boolean initialValue) {
        this(type, initialValue, "true", "false");
    }
    public void setValue(boolean value) {
        this.value = value;
    }
    public boolean getValue() {
        return value;
    }
    public String getStateLabel(boolean state) {
        return ((state == true) ? trueStateLabel : falseStateLabel);
    }
    public String toString() {
        return new String(super.toString() + " with current value: " + getStateLabel(getValue()));
    }
    public static class Type extends Control.Type {
        public static final Type MUTE                           = new Type("Mute");
        public static final Type APPLY_REVERB           = new Type("Apply Reverb");
        protected Type(String name) {
            super(name);
        }
    } 
}
