public abstract class Composer<T> {
    private static final Map<Class<?>, Composer> impls = new HashMap<Class<?>, Composer>();
    static {
        register(Byte.class, ComposerByte.class);
        register(Short.class, ComposerShort.class);
        register(Integer.class, ComposerInteger.class);
        register(Long.class, ComposerLong.class);
        register(Float.class, ComposerFloat.class);
        register(Double.class, ComposerDouble.class);
        register(Boolean.class, ComposerBoolean.class);
        register(Color.class, ComposerColor.class);
        register(Point2D.class, ComposerPoint2D.class);
        register(Line2D.class, ComposerLine2D.class);
        register(Dimension2D.class, ComposerDimension2D.class);
        register(Rectangle2D.class, ComposerRectangle2D.class);
        register(RoundRectangle2D.class, ComposerRoundRectangle2D.class);
        register(Ellipse2D.class, ComposerEllipse2D.class);
        register(Arc2D.class, ComposerArc2D.class);
        register(QuadCurve2D.class, ComposerQuadCurve2D.class);
        register(CubicCurve2D.class, ComposerCubicCurve2D.class);
    }
    public static void register(Class<?> type, Class<? extends Composer> implClass) {
        Composer impl;
        try {
            Constructor<? extends Composer> ctor = implClass.getConstructor();
            impl = (Composer) ctor.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Problem constructing " + "appropriate Composer for type " + type + ":", e);
        }
        impls.put(type, impl);
    }
    public static <T> Composer<T> getInstance(Class<?> type) {
        Class<? extends Composer> compClass = null;
        for (Class<?> klass : impls.keySet()) {
            if (klass.isAssignableFrom(type)) {
                return impls.get(klass);
            }
        }
        throw new IllegalArgumentException("No Composer" + " can be found for type " + type + "; consider using" + " different types for your values or supplying a custom" + " Composer");
    }
    private final int numVals;
    protected Composer(int numVals) {
        this.numVals = numVals;
    }
    public int getNumVals() {
        return numVals;
    }
    public abstract double[] decompose(T o, double[] v);
    public abstract T compose(double[] v);
}
class ComposerByte extends Composer<Byte> {
    public ComposerByte() {
        super(1);
    }
    public double[] decompose(Byte o, double[] v) {
        v[0] = o;
        return v;
    }
    public Byte compose(double[] v) {
        return (byte) v[0];
    }
}
class ComposerShort extends Composer<Short> {
    public ComposerShort() {
        super(1);
    }
    public double[] decompose(Short o, double[] v) {
        v[0] = o;
        return v;
    }
    public Short compose(double[] v) {
        return (short) v[0];
    }
}
class ComposerInteger extends Composer<Integer> {
    public ComposerInteger() {
        super(1);
    }
    public double[] decompose(Integer o, double[] v) {
        v[0] = o;
        return v;
    }
    public Integer compose(double[] v) {
        return (int) v[0];
    }
}
class ComposerLong extends Composer<Long> {
    public ComposerLong() {
        super(1);
    }
    public double[] decompose(Long o, double[] v) {
        v[0] = o;
        return v;
    }
    public Long compose(double[] v) {
        return (long) v[0];
    }
}
class ComposerFloat extends Composer<Float> {
    public ComposerFloat() {
        super(1);
    }
    public double[] decompose(Float o, double[] v) {
        v[0] = o;
        return v;
    }
    public Float compose(double[] v) {
        return (float) v[0];
    }
}
class ComposerDouble extends Composer<Double> {
    public ComposerDouble() {
        super(1);
    }
    public double[] decompose(Double o, double[] v) {
        v[0] = o;
        return v;
    }
    public Double compose(double[] v) {
        return v[0];
    }
}
class ComposerBoolean extends Composer<Boolean> {
    public ComposerBoolean() {
        super(1);
    }
    public double[] decompose(Boolean o, double[] v) {
        v[0] = o ? 1.0 : 0.0;
        return v;
    }
    public Boolean compose(double[] v) {
        return (v[0] == 1.0 ? true : false);
    }
}
class ComposerColor extends Composer<Color> {
    private float[] comps = new float[4];
    public ComposerColor() {
        super(4);
    }
    public double[] decompose(Color o, double[] v) {
        comps = o.getComponents(comps);
        for (int i = 0; i < 4; i++) {
            v[i] = comps[i];
        }
        return v;
    }
    public Color compose(double[] v) {
        return new Color((float) v[0], (float) v[1], (float) v[2], (float) v[3]);
    }
}
class ComposerPoint2D extends Composer<Point2D> {
    public ComposerPoint2D() {
        super(2);
    }
    public double[] decompose(Point2D o, double[] v) {
        v[0] = o.getX();
        v[1] = o.getY();
        return v;
    }
    public Point2D compose(double[] v) {
        return new Point2D.Float((float) v[0], (float) v[1]);
    }
}
class ComposerLine2D extends Composer<Line2D> {
    public ComposerLine2D() {
        super(4);
    }
    public double[] decompose(Line2D o, double[] v) {
        v[0] = o.getX1();
        v[1] = o.getY1();
        v[2] = o.getX2();
        v[3] = o.getY2();
        return v;
    }
    public Line2D compose(double[] v) {
        return new Line2D.Float((float) v[0], (float) v[1], (float) v[2], (float) v[3]);
    }
}
class ComposerDimension2D extends Composer<Dimension2D> {
    public ComposerDimension2D() {
        super(2);
    }
    public double[] decompose(Dimension2D o, double[] v) {
        v[0] = o.getWidth();
        v[1] = o.getHeight();
        return v;
    }
    public Dimension2D compose(double[] v) {
        return new Dimension((int) v[0], (int) v[1]);
    }
}
class ComposerRectangle2D extends Composer<Rectangle2D> {
    public ComposerRectangle2D() {
        super(4);
    }
    public double[] decompose(Rectangle2D o, double[] v) {
        v[0] = o.getX();
        v[1] = o.getY();
        v[2] = o.getWidth();
        v[3] = o.getHeight();
        return v;
    }
    public Rectangle2D compose(double[] v) {
        return new Rectangle2D.Float((float) v[0], (float) v[1], (float) v[2], (float) v[3]);
    }
}
class ComposerRoundRectangle2D extends Composer<RoundRectangle2D> {
    public ComposerRoundRectangle2D() {
        super(6);
    }
    public double[] decompose(RoundRectangle2D o, double[] v) {
        v[0] = o.getX();
        v[1] = o.getY();
        v[2] = o.getWidth();
        v[3] = o.getHeight();
        v[4] = o.getArcWidth();
        v[5] = o.getArcHeight();
        return v;
    }
    public RoundRectangle2D compose(double[] v) {
        return new RoundRectangle2D.Float((float) v[0], (float) v[1], (float) v[2], (float) v[3], (float) v[4], (float) v[5]);
    }
}
class ComposerEllipse2D extends Composer<Ellipse2D> {
    public ComposerEllipse2D() {
        super(4);
    }
    public double[] decompose(Ellipse2D o, double[] v) {
        v[0] = o.getX();
        v[1] = o.getY();
        v[2] = o.getWidth();
        v[3] = o.getHeight();
        return v;
    }
    public Ellipse2D compose(double[] v) {
        return new Ellipse2D.Float((float) v[0], (float) v[1], (float) v[2], (float) v[3]);
    }
}
class ComposerArc2D extends Composer<Arc2D> {
    public ComposerArc2D() {
        super(6);
    }
    public double[] decompose(Arc2D o, double[] v) {
        v[0] = o.getX();
        v[1] = o.getY();
        v[2] = o.getWidth();
        v[3] = o.getHeight();
        v[4] = o.getAngleStart();
        v[5] = o.getAngleExtent();
        return v;
    }
    public Arc2D compose(double[] v) {
        return new Arc2D.Float((float) v[0], (float) v[1], (float) v[2], (float) v[3], (float) v[4], (float) v[5], Arc2D.OPEN);
    }
}
class ComposerQuadCurve2D extends Composer<QuadCurve2D> {
    public ComposerQuadCurve2D() {
        super(6);
    }
    public double[] decompose(QuadCurve2D o, double[] v) {
        v[0] = o.getX1();
        v[1] = o.getY1();
        v[2] = o.getCtrlX();
        v[3] = o.getCtrlY();
        v[4] = o.getX2();
        v[5] = o.getY2();
        return v;
    }
    public QuadCurve2D compose(double[] v) {
        return new QuadCurve2D.Float((float) v[0], (float) v[1], (float) v[2], (float) v[3], (float) v[4], (float) v[5]);
    }
}
class ComposerCubicCurve2D extends Composer<CubicCurve2D> {
    public ComposerCubicCurve2D() {
        super(8);
    }
    public double[] decompose(CubicCurve2D o, double[] v) {
        v[0] = o.getX1();
        v[1] = o.getY1();
        v[2] = o.getCtrlX1();
        v[3] = o.getCtrlY1();
        v[4] = o.getCtrlX2();
        v[5] = o.getCtrlY2();
        v[6] = o.getX2();
        v[7] = o.getY2();
        return v;
    }
    public CubicCurve2D compose(double[] v) {
        return new CubicCurve2D.Float((float) v[0], (float) v[1], (float) v[2], (float) v[3], (float) v[4], (float) v[5], (float) v[6], (float) v[7]);
    }
}
