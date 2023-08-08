public class FloatWidget extends Widget {
    public static final float FLOAT_EPSILON = 1e-5f;
    public static final double DOUBLE_EPSILON = 1e-14d;
    public static final float MIN_FLOAT_VALUE = 1e-22f;
    public static final float MAX_FLOAT_VALUE = 1e17f;
    public static final double MIN_DOUBLE_VALUE = 1e-62d;
    public static final double MAX_DOUBLE_VALUE = 1e62d;
    private float floatField;
    private Float floatObjField;
    private double doubleField;
    private Double doubleObjField;
    public FloatWidget() {
        super();
    }
    public float getFloatField() {
        return floatField;
    }
    public Float getFloatObjField() {
        return floatObjField;
    }
    public double getDoubleField() {
        return doubleField;
    }
    public Double getDoubleObjField() {
        return doubleObjField;
    }
    private float nextFloat() {
        float f;
        do {
            f = Float.intBitsToFloat(r.nextInt());
        } while (Float.isNaN(f) || Float.isInfinite(f) || f < MIN_FLOAT_VALUE || f > MAX_FLOAT_VALUE);
        return f;
    }
    private double nextDouble() {
        double d;
        do {
            d = Double.longBitsToDouble(r.nextLong());
        } while (Double.isNaN(d) || Double.isInfinite(d) || d < MIN_DOUBLE_VALUE || d > MAX_DOUBLE_VALUE);
        return d;
    }
    public void fillRandom() {
        super.fillRandom();
        floatField = nextFloat();
        floatObjField = nextNull() ? null : new Float(nextFloat());
        doubleField = nextDouble();
        doubleObjField = nextNull() ? null : new Double(nextDouble());
    }
    public boolean compareTo(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof FloatWidget) || !super.compareTo(obj)) return false;
        FloatWidget w = (FloatWidget) obj;
        if (floatObjField == null) {
            if (w.floatObjField != null) return false;
        } else if (!approximates(floatObjField.floatValue(), w.floatObjField.floatValue())) return false;
        if (doubleObjField == null) {
            if (w.doubleObjField != null) return false;
        } else if (!approximates(doubleObjField.doubleValue(), w.doubleObjField.doubleValue())) return false;
        return approximates(floatField, w.floatField) && approximates(doubleField, w.doubleField);
    }
    public static boolean approximates(float x, float y) {
        return Math.abs(x - y) / Math.max(Math.max(Math.abs(x), Math.abs(y)), Float.MIN_VALUE) < FLOAT_EPSILON;
    }
    public static boolean approximates(double x, double y) {
        return Math.abs(x - y) / Math.max(Math.max(Math.abs(x), Math.abs(y)), Double.MIN_VALUE) < DOUBLE_EPSILON;
    }
    public String toString() {
        StringBuffer s = new StringBuffer(super.toString());
        s.append("  floatField = ").append(floatField);
        s.append('\n');
        s.append("  floatObjField = ").append(floatObjField);
        s.append('\n');
        s.append("  doubleField = ").append(doubleField);
        s.append('\n');
        s.append("  doubleObjField = ").append(doubleObjField);
        s.append('\n');
        return s.toString();
    }
}
