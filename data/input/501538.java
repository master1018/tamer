public final class GeneralPath implements Shape, Cloneable {
    public static final int WIND_EVEN_ODD = PathIterator.WIND_EVEN_ODD;
    public static final int WIND_NON_ZERO = PathIterator.WIND_NON_ZERO;
    private static final int BUFFER_SIZE = 10;
    private static final int BUFFER_CAPACITY = 10;
    byte[] types;
    float[] points;
    int typeSize;
    int pointSize;
    int rule;
    static int pointShift[] = {
            2, 
            2, 
            4, 
            6, 
            0
    }; 
    class Iterator implements PathIterator {
        int typeIndex;
        int pointIndex;
        GeneralPath p;
        AffineTransform t;
        Iterator(GeneralPath path) {
            this(path, null);
        }
        Iterator(GeneralPath path, AffineTransform at) {
            this.p = path;
            this.t = at;
        }
        public int getWindingRule() {
            return p.getWindingRule();
        }
        public boolean isDone() {
            return typeIndex >= p.typeSize;
        }
        public void next() {
            typeIndex++;
        }
        public int currentSegment(double[] coords) {
            if (isDone()) {
                throw new NoSuchElementException(Messages.getString("awt.4B")); 
            }
            int type = p.types[typeIndex];
            int count = GeneralPath.pointShift[type];
            for (int i = 0; i < count; i++) {
                coords[i] = p.points[pointIndex + i];
            }
            if (t != null) {
                t.transform(coords, 0, coords, 0, count / 2);
            }
            pointIndex += count;
            return type;
        }
        public int currentSegment(float[] coords) {
            if (isDone()) {
                throw new NoSuchElementException(Messages.getString("awt.4B")); 
            }
            int type = p.types[typeIndex];
            int count = GeneralPath.pointShift[type];
            System.arraycopy(p.points, pointIndex, coords, 0, count);
            if (t != null) {
                t.transform(coords, 0, coords, 0, count / 2);
            }
            pointIndex += count;
            return type;
        }
    }
    public GeneralPath() {
        this(WIND_NON_ZERO, BUFFER_SIZE);
    }
    public GeneralPath(int rule) {
        this(rule, BUFFER_SIZE);
    }
    public GeneralPath(int rule, int initialCapacity) {
        setWindingRule(rule);
        types = new byte[initialCapacity];
        points = new float[initialCapacity * 2];
    }
    public GeneralPath(Shape shape) {
        this(WIND_NON_ZERO, BUFFER_SIZE);
        PathIterator p = shape.getPathIterator(null);
        setWindingRule(p.getWindingRule());
        append(p, false);
    }
    public void setWindingRule(int rule) {
        if (rule != WIND_EVEN_ODD && rule != WIND_NON_ZERO) {
            throw new java.lang.IllegalArgumentException(Messages.getString("awt.209")); 
        }
        this.rule = rule;
    }
    public int getWindingRule() {
        return rule;
    }
    void checkBuf(int pointCount, boolean checkMove) {
        if (checkMove && typeSize == 0) {
            throw new IllegalPathStateException(Messages.getString("awt.20A")); 
        }
        if (typeSize == types.length) {
            byte tmp[] = new byte[typeSize + BUFFER_CAPACITY];
            System.arraycopy(types, 0, tmp, 0, typeSize);
            types = tmp;
        }
        if (pointSize + pointCount > points.length) {
            float tmp[] = new float[pointSize + Math.max(BUFFER_CAPACITY * 2, pointCount)];
            System.arraycopy(points, 0, tmp, 0, pointSize);
            points = tmp;
        }
    }
    public void moveTo(float x, float y) {
        if (typeSize > 0 && types[typeSize - 1] == PathIterator.SEG_MOVETO) {
            points[pointSize - 2] = x;
            points[pointSize - 1] = y;
        } else {
            checkBuf(2, false);
            types[typeSize++] = PathIterator.SEG_MOVETO;
            points[pointSize++] = x;
            points[pointSize++] = y;
        }
    }
    public void lineTo(float x, float y) {
        checkBuf(2, true);
        types[typeSize++] = PathIterator.SEG_LINETO;
        points[pointSize++] = x;
        points[pointSize++] = y;
    }
    public void quadTo(float x1, float y1, float x2, float y2) {
        checkBuf(4, true);
        types[typeSize++] = PathIterator.SEG_QUADTO;
        points[pointSize++] = x1;
        points[pointSize++] = y1;
        points[pointSize++] = x2;
        points[pointSize++] = y2;
    }
    public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {
        checkBuf(6, true);
        types[typeSize++] = PathIterator.SEG_CUBICTO;
        points[pointSize++] = x1;
        points[pointSize++] = y1;
        points[pointSize++] = x2;
        points[pointSize++] = y2;
        points[pointSize++] = x3;
        points[pointSize++] = y3;
    }
    public void closePath() {
        if (typeSize == 0 || types[typeSize - 1] != PathIterator.SEG_CLOSE) {
            checkBuf(0, true);
            types[typeSize++] = PathIterator.SEG_CLOSE;
        }
    }
    public void append(Shape shape, boolean connect) {
        PathIterator p = shape.getPathIterator(null);
        append(p, connect);
    }
    public void append(PathIterator path, boolean connect) {
        while (!path.isDone()) {
            float coords[] = new float[6];
            switch (path.currentSegment(coords)) {
                case PathIterator.SEG_MOVETO:
                    if (!connect || typeSize == 0) {
                        moveTo(coords[0], coords[1]);
                        break;
                    }
                    if (types[typeSize - 1] != PathIterator.SEG_CLOSE
                            && points[pointSize - 2] == coords[0]
                            && points[pointSize - 1] == coords[1]) {
                        break;
                    }
                case PathIterator.SEG_LINETO:
                    lineTo(coords[0], coords[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    quadTo(coords[0], coords[1], coords[2], coords[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    closePath();
                    break;
            }
            path.next();
            connect = false;
        }
    }
    public Point2D getCurrentPoint() {
        if (typeSize == 0) {
            return null;
        }
        int j = pointSize - 2;
        if (types[typeSize - 1] == PathIterator.SEG_CLOSE) {
            for (int i = typeSize - 2; i > 0; i--) {
                int type = types[i];
                if (type == PathIterator.SEG_MOVETO) {
                    break;
                }
                j -= pointShift[type];
            }
        }
        return new Point2D.Float(points[j], points[j + 1]);
    }
    public void reset() {
        typeSize = 0;
        pointSize = 0;
    }
    public void transform(AffineTransform t) {
        t.transform(points, 0, points, 0, pointSize / 2);
    }
    public Shape createTransformedShape(AffineTransform t) {
        GeneralPath p = (GeneralPath)clone();
        if (t != null) {
            p.transform(t);
        }
        return p;
    }
    public Rectangle2D getBounds2D() {
        float rx1, ry1, rx2, ry2;
        if (pointSize == 0) {
            rx1 = ry1 = rx2 = ry2 = 0.0f;
        } else {
            int i = pointSize - 1;
            ry1 = ry2 = points[i--];
            rx1 = rx2 = points[i--];
            while (i > 0) {
                float y = points[i--];
                float x = points[i--];
                if (x < rx1) {
                    rx1 = x;
                } else if (x > rx2) {
                    rx2 = x;
                }
                if (y < ry1) {
                    ry1 = y;
                } else if (y > ry2) {
                    ry2 = y;
                }
            }
        }
        return new Rectangle2D.Float(rx1, ry1, rx2 - rx1, ry2 - ry1);
    }
    public Rectangle getBounds() {
        return getBounds2D().getBounds();
    }
    boolean isInside(int cross) {
        if (rule == WIND_NON_ZERO) {
            return Crossing.isInsideNonZero(cross);
        }
        return Crossing.isInsideEvenOdd(cross);
    }
    public boolean contains(double px, double py) {
        return isInside(Crossing.crossShape(this, px, py));
    }
    public boolean contains(double rx, double ry, double rw, double rh) {
        int cross = Crossing.intersectShape(this, rx, ry, rw, rh);
        return cross != Crossing.CROSSING && isInside(cross);
    }
    public boolean intersects(double rx, double ry, double rw, double rh) {
        int cross = Crossing.intersectShape(this, rx, ry, rw, rh);
        return cross == Crossing.CROSSING || isInside(cross);
    }
    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }
    public boolean contains(Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }
    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }
    public PathIterator getPathIterator(AffineTransform t) {
        return new Iterator(this, t);
    }
    public PathIterator getPathIterator(AffineTransform t, double flatness) {
        return new FlatteningPathIterator(getPathIterator(t), flatness);
    }
    @Override
    public Object clone() {
        try {
            GeneralPath p = (GeneralPath)super.clone();
            p.types = types.clone();
            p.points = points.clone();
            return p;
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}
