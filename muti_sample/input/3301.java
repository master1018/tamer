public class SpinnerNumberModel extends AbstractSpinnerModel implements Serializable
{
    private Number stepSize, value;
    private Comparable minimum, maximum;
    public SpinnerNumberModel(Number value, Comparable minimum, Comparable maximum, Number stepSize) {
        if ((value == null) || (stepSize == null)) {
            throw new IllegalArgumentException("value and stepSize must be non-null");
        }
        if (!(((minimum == null) || (minimum.compareTo(value) <= 0)) &&
              ((maximum == null) || (maximum.compareTo(value) >= 0)))) {
            throw new IllegalArgumentException("(minimum <= value <= maximum) is false");
        }
        this.value = value;
        this.minimum = minimum;
        this.maximum = maximum;
        this.stepSize = stepSize;
    }
    public SpinnerNumberModel(int value, int minimum, int maximum, int stepSize) {
        this(Integer.valueOf(value), Integer.valueOf(minimum), Integer.valueOf(maximum), Integer.valueOf(stepSize));
    }
    public SpinnerNumberModel(double value, double minimum, double maximum, double stepSize) {
        this(new Double(value), new Double(minimum), new Double(maximum), new Double(stepSize));
    }
    public SpinnerNumberModel() {
        this(Integer.valueOf(0), null, null, Integer.valueOf(1));
    }
    public void setMinimum(Comparable minimum) {
        if ((minimum == null) ? (this.minimum != null) : !minimum.equals(this.minimum)) {
            this.minimum = minimum;
            fireStateChanged();
        }
    }
    public Comparable getMinimum() {
        return minimum;
    }
    public void setMaximum(Comparable maximum) {
        if ((maximum == null) ? (this.maximum != null) : !maximum.equals(this.maximum)) {
            this.maximum = maximum;
            fireStateChanged();
        }
    }
    public Comparable getMaximum() {
        return maximum;
    }
    public void setStepSize(Number stepSize) {
        if (stepSize == null) {
            throw new IllegalArgumentException("null stepSize");
        }
        if (!stepSize.equals(this.stepSize)) {
            this.stepSize = stepSize;
            fireStateChanged();
        }
    }
    public Number getStepSize() {
        return stepSize;
    }
    private Number incrValue(int dir)
    {
        Number newValue;
        if ((value instanceof Float) || (value instanceof Double)) {
            double v = value.doubleValue() + (stepSize.doubleValue() * (double)dir);
            if (value instanceof Double) {
                newValue = new Double(v);
            }
            else {
                newValue = new Float(v);
            }
        }
        else {
            long v = value.longValue() + (stepSize.longValue() * (long)dir);
            if (value instanceof Long) {
                newValue = Long.valueOf(v);
            }
            else if (value instanceof Integer) {
                newValue = Integer.valueOf((int)v);
            }
            else if (value instanceof Short) {
                newValue = Short.valueOf((short)v);
            }
            else {
                newValue = Byte.valueOf((byte)v);
            }
        }
        if ((maximum != null) && (maximum.compareTo(newValue) < 0)) {
            return null;
        }
        if ((minimum != null) && (minimum.compareTo(newValue) > 0)) {
            return null;
        }
        else {
            return newValue;
        }
    }
    public Object getNextValue() {
        return incrValue(+1);
    }
    public Object getPreviousValue() {
        return incrValue(-1);
    }
    public Number getNumber() {
        return value;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        if ((value == null) || !(value instanceof Number)) {
            throw new IllegalArgumentException("illegal value");
        }
        if (!value.equals(this.value)) {
            this.value = (Number)value;
            fireStateChanged();
        }
    }
}
