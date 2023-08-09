public abstract class BooleanControl extends Control {
    public static class Type extends Control.Type {
        public static final Type APPLY_REVERB = new Type("Apply Reverb"); 
        public static final Type MUTE = new Type("Mute"); 
        protected Type(String name) {
            super(name);
        }
    }
    private boolean value;
    private String trueStateLabel;
    private String falseStateLabel;
    protected BooleanControl(BooleanControl.Type type, boolean initialValue,
            String trueStateLabel, String falseStateLabel) {
        super(type);
        this.value = initialValue;
        this.trueStateLabel = trueStateLabel;
        this.falseStateLabel = falseStateLabel;
    }
    protected BooleanControl(BooleanControl.Type type, boolean initialValue) {
        this(type, initialValue, "true", "false"); 
    }
    public void setValue(boolean value) {
        this.value = value;
    }
    public boolean getValue() {
        return this.value;
    }
    public String getStateLabel(boolean state) {
        if (state) {
            return this.trueStateLabel;
        } else {
            return this.falseStateLabel;
        }
    }
    public String toString() {
        return getType() + " Control with current value: " + getStateLabel(value); 
    }
}
