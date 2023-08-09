public abstract class FloatControl extends Control {
    public static class Type extends Control.Type {
        public static final Type MASTER_GAIN = new Type("Master Gain"); 
        public static final Type AUX_SEND = new Type("AUX Send"); 
        public static final Type AUX_RETURN = new Type("AUX Return"); 
        public static final Type REVERB_SEND = new Type("Reverb Send"); 
        public static final Type REVERB_RETURN = new Type("Reverb Return"); 
        public static final Type VOLUME = new Type("Volume"); 
        public static final Type PAN = new Type("Pan"); 
        public static final Type BALANCE = new Type("Balance"); 
        public static final Type SAMPLE_RATE = new Type("Sample Rate"); 
        protected Type(String name) {
            super(name);
        }
    }
    private float value;
    private float maximum;
    private float minimum;
    private String units;
    private String minLabel;
    private String midLabel;
    private String maxLabel;
    private float precision;
    private int updatePeriod;
    protected FloatControl(FloatControl.Type type, float minimum,
            float maximum, float precision, int updatePeriod,
            float initialValue, String units, String minLabel, String midLabel,
            String maxLabel) {
        super(type);
        this.maximum = maximum;
        this.maxLabel = maxLabel;
        this.midLabel = midLabel;
        this.minLabel = minLabel;
        this.minimum = minimum;
        this.precision = precision;
        this.units = units;
        this.updatePeriod = updatePeriod;
        this.value = initialValue;
    }
    protected FloatControl(FloatControl.Type type, float minimum,
            float maximum, float precision, int updatePeriod,
            float initialValue, String units) {
        this(type, minimum, maximum, precision, updatePeriod, initialValue,
                units, "", "", ""); 
    }
    public void setValue(float newValue) {
        if (newValue > maximum || newValue < minimum) {
            throw new IllegalArgumentException(Messages.getString("sound.0F")); 
        }
        this.value = newValue;
    }
    public float getValue() {
        return this.value;
    }
    public float getMaximum() {
        return this.maximum;
    }
    public float getMinimum() {
        return this.minimum;
    }
    public String getUnits() {
        return this.units;
    }
    public String getMinLabel() {
        return this.minLabel;
    }
    public String getMidLabel() {
        return this.midLabel;
    }
    public String getMaxLabel() {
        return this.maxLabel;
    }
    public float getPrecision() {
        return this.precision;
    }
    public int getUpdatePeriod() {
        return this.updatePeriod;
    }
    public void shift(float from, float to, int microseconds) {
        setValue(to);
    }
    public String toString() {
        return getType() + " with current value: "+ value + " " + units  
            + " (range: " + minimum + " - " + maximum + ")"; 
    }
}
