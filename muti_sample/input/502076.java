public class FlatteningPathIterator implements PathIterator {
    private static final int BUFFER_SIZE = 16;
    private static final int BUFFER_LIMIT = 16;
    private static final int BUFFER_CAPACITY = 16;
    int bufType;
    int bufLimit;
    int bufSize;
    int bufIndex;
    int bufSubdiv;
    double buf[];
    boolean bufEmpty = true;
    PathIterator p;
    double flatness;
    double flatness2;
    double px;
    double py;
    double coords[] = new double[6];
    public FlatteningPathIterator(PathIterator path, double flatness) {
        this(path, flatness, BUFFER_LIMIT);
    }
    public FlatteningPathIterator(PathIterator path, double flatness, int limit) {
        if (flatness < 0.0) {
            throw new IllegalArgumentException(Messages.getString("awt.206")); 
        }
        if (limit < 0) {
            throw new IllegalArgumentException(Messages.getString("awt.207")); 
        }
        if (path == null) {
            throw new NullPointerException(Messages.getString("awt.208")); 
        }
        this.p = path;
        this.flatness = flatness;
        this.flatness2 = flatness * flatness;
        this.bufLimit = limit;
        this.bufSize = Math.min(bufLimit, BUFFER_SIZE);
        this.buf = new double[bufSize];
        this.bufIndex = bufSize;
    }
    public double getFlatness() {
        return flatness;
    }
    public int getRecursionLimit() {
        return bufLimit;
    }
    public int getWindingRule() {
        return p.getWindingRule();
    }
    public boolean isDone() {
        return bufEmpty && p.isDone();
    }
    void evaluate() {
        if (bufEmpty) {
            bufType = p.currentSegment(coords);
        }
        switch (bufType) {
            case SEG_MOVETO:
            case SEG_LINETO:
                px = coords[0];
                py = coords[1];
                break;
            case SEG_QUADTO:
                if (bufEmpty) {
                    bufIndex -= 6;
                    buf[bufIndex + 0] = px;
                    buf[bufIndex + 1] = py;
                    System.arraycopy(coords, 0, buf, bufIndex + 2, 4);
                    bufSubdiv = 0;
                }
                while (bufSubdiv < bufLimit) {
                    if (QuadCurve2D.getFlatnessSq(buf, bufIndex) < flatness2) {
                        break;
                    }
                    if (bufIndex <= 4) {
                        double tmp[] = new double[bufSize + BUFFER_CAPACITY];
                        System.arraycopy(buf, bufIndex, tmp, bufIndex + BUFFER_CAPACITY, bufSize
                                - bufIndex);
                        buf = tmp;
                        bufSize += BUFFER_CAPACITY;
                        bufIndex += BUFFER_CAPACITY;
                    }
                    QuadCurve2D.subdivide(buf, bufIndex, buf, bufIndex - 4, buf, bufIndex);
                    bufIndex -= 4;
                    bufSubdiv++;
                }
                bufIndex += 4;
                px = buf[bufIndex];
                py = buf[bufIndex + 1];
                bufEmpty = (bufIndex == bufSize - 2);
                if (bufEmpty) {
                    bufIndex = bufSize;
                    bufType = SEG_LINETO;
                } else {
                    bufSubdiv--;
                }
                break;
            case SEG_CUBICTO:
                if (bufEmpty) {
                    bufIndex -= 8;
                    buf[bufIndex + 0] = px;
                    buf[bufIndex + 1] = py;
                    System.arraycopy(coords, 0, buf, bufIndex + 2, 6);
                    bufSubdiv = 0;
                }
                while (bufSubdiv < bufLimit) {
                    if (CubicCurve2D.getFlatnessSq(buf, bufIndex) < flatness2) {
                        break;
                    }
                    if (bufIndex <= 6) {
                        double tmp[] = new double[bufSize + BUFFER_CAPACITY];
                        System.arraycopy(buf, bufIndex, tmp, bufIndex + BUFFER_CAPACITY, bufSize
                                - bufIndex);
                        buf = tmp;
                        bufSize += BUFFER_CAPACITY;
                        bufIndex += BUFFER_CAPACITY;
                    }
                    CubicCurve2D.subdivide(buf, bufIndex, buf, bufIndex - 6, buf, bufIndex);
                    bufIndex -= 6;
                    bufSubdiv++;
                }
                bufIndex += 6;
                px = buf[bufIndex];
                py = buf[bufIndex + 1];
                bufEmpty = (bufIndex == bufSize - 2);
                if (bufEmpty) {
                    bufIndex = bufSize;
                    bufType = SEG_LINETO;
                } else {
                    bufSubdiv--;
                }
                break;
        }
    }
    public void next() {
        if (bufEmpty) {
            p.next();
        }
    }
    public int currentSegment(float[] coords) {
        if (isDone()) {
            throw new NoSuchElementException(Messages.getString("awt.4Bx")); 
        }
        evaluate();
        int type = bufType;
        if (type != SEG_CLOSE) {
            coords[0] = (float)px;
            coords[1] = (float)py;
            if (type != SEG_MOVETO) {
                type = SEG_LINETO;
            }
        }
        return type;
    }
    public int currentSegment(double[] coords) {
        if (isDone()) {
            throw new NoSuchElementException(Messages.getString("awt.4B")); 
        }
        evaluate();
        int type = bufType;
        if (type != SEG_CLOSE) {
            coords[0] = px;
            coords[1] = py;
            if (type != SEG_MOVETO) {
                type = SEG_LINETO;
            }
        }
        return type;
    }
}
