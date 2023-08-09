public class ModelStandardTransform implements ModelTransform {
    public static final boolean DIRECTION_MIN2MAX = false;
    public static final boolean DIRECTION_MAX2MIN = true;
    public static final boolean POLARITY_UNIPOLAR = false;
    public static final boolean POLARITY_BIPOLAR = true;
    public static final int TRANSFORM_LINEAR = 0;
    public static final int TRANSFORM_CONCAVE = 1;
    public static final int TRANSFORM_CONVEX = 2;
    public static final int TRANSFORM_SWITCH = 3;
    public static final int TRANSFORM_ABSOLUTE = 4;
    private boolean direction = DIRECTION_MIN2MAX;
    private boolean polarity = POLARITY_UNIPOLAR;
    private int transform = TRANSFORM_LINEAR;
    public ModelStandardTransform() {
    }
    public ModelStandardTransform(boolean direction) {
        this.direction = direction;
    }
    public ModelStandardTransform(boolean direction, boolean polarity) {
        this.direction = direction;
        this.polarity = polarity;
    }
    public ModelStandardTransform(boolean direction, boolean polarity,
            int transform) {
        this.direction = direction;
        this.polarity = polarity;
        this.transform = transform;
    }
    public double transform(double value) {
        double s;
        double a;
        if (direction == DIRECTION_MAX2MIN)
            value = 1.0 - value;
        if (polarity == POLARITY_BIPOLAR)
            value = value * 2.0 - 1.0;
        switch (transform) {
            case TRANSFORM_CONCAVE:
                s = Math.signum(value);
                a = Math.abs(value);
                a = -((5.0 / 12.0) / Math.log(10)) * Math.log(1.0 - a);
                if (a < 0)
                    a = 0;
                else if (a > 1)
                    a = 1;
                return s * a;
            case TRANSFORM_CONVEX:
                s = Math.signum(value);
                a = Math.abs(value);
                a = 1.0 + ((5.0 / 12.0) / Math.log(10)) * Math.log(a);
                if (a < 0)
                    a = 0;
                else if (a > 1)
                    a = 1;
                return s * a;
            case TRANSFORM_SWITCH:
                if (polarity == POLARITY_BIPOLAR)
                    return (value > 0) ? 1 : -1;
                else
                    return (value > 0.5) ? 1 : 0;
            case TRANSFORM_ABSOLUTE:
                return Math.abs(value);
            default:
                break;
        }
        return value;
    }
    public boolean getDirection() {
        return direction;
    }
    public void setDirection(boolean direction) {
        this.direction = direction;
    }
    public boolean getPolarity() {
        return polarity;
    }
    public void setPolarity(boolean polarity) {
        this.polarity = polarity;
    }
    public int getTransform() {
        return transform;
    }
    public void setTransform(int transform) {
        this.transform = transform;
    }
}
