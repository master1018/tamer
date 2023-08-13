public class Polygon implements Shape, Serializable {
    private static final long serialVersionUID = -6460061437900069969L;
    private static final int BUFFER_CAPACITY = 4;
    public int npoints;
    public int[] xpoints;
    public int[] ypoints;
    protected Rectangle bounds;
    class Iterator implements PathIterator {
        public Polygon p;
        public AffineTransform t;
        public int index;
        public Iterator(AffineTransform at, Polygon p) {
            this.p = p;
            this.t = at;
            if (p.npoints == 0) {
                index = 1;
            }
        }
        public int getWindingRule() {
            return WIND_EVEN_ODD;
        }
        public boolean isDone() {
            return index > p.npoints;
        }
        public void next() {
            index++;
        }
        public int currentSegment(double[] coords) {
            if (isDone()) {
                throw new NoSuchElementException(Messages.getString("awt.110")); 
            }
            if (index == p.npoints) {
                return SEG_CLOSE;
            }
            coords[0] = p.xpoints[index];
            coords[1] = p.ypoints[index];
            if (t != null) {
                t.transform(coords, 0, coords, 0, 1);
            }
            return index == 0 ? SEG_MOVETO : SEG_LINETO;
        }
        public int currentSegment(float[] coords) {
            if (isDone()) {
                throw new NoSuchElementException(Messages.getString("awt.110")); 
            }
            if (index == p.npoints) {
                return SEG_CLOSE;
            }
            coords[0] = p.xpoints[index];
            coords[1] = p.ypoints[index];
            if (t != null) {
                t.transform(coords, 0, coords, 0, 1);
            }
            return index == 0 ? SEG_MOVETO : SEG_LINETO;
        }
    }
    public Polygon() {
        xpoints = new int[BUFFER_CAPACITY];
        ypoints = new int[BUFFER_CAPACITY];
    }
    public Polygon(int[] xpoints, int[] ypoints, int npoints) {
        if (npoints > xpoints.length || npoints > ypoints.length) {
            throw new IndexOutOfBoundsException(Messages.getString("awt.111")); 
        }
        if (npoints < 0) {
            throw new NegativeArraySizeException(Messages.getString("awt.112")); 
        }
        this.npoints = npoints;
        this.xpoints = new int[npoints];
        this.ypoints = new int[npoints];
        System.arraycopy(xpoints, 0, this.xpoints, 0, npoints);
        System.arraycopy(ypoints, 0, this.ypoints, 0, npoints);
    }
    public void reset() {
        npoints = 0;
        bounds = null;
    }
    public void invalidate() {
        bounds = null;
    }
    public void addPoint(int px, int py) {
        if (npoints == xpoints.length) {
            int[] tmp;
            tmp = new int[xpoints.length + BUFFER_CAPACITY];
            System.arraycopy(xpoints, 0, tmp, 0, xpoints.length);
            xpoints = tmp;
            tmp = new int[ypoints.length + BUFFER_CAPACITY];
            System.arraycopy(ypoints, 0, tmp, 0, ypoints.length);
            ypoints = tmp;
        }
        xpoints[npoints] = px;
        ypoints[npoints] = py;
        npoints++;
        if (bounds != null) {
            bounds.setFrameFromDiagonal(Math.min(bounds.getMinX(), px), Math.min(bounds.getMinY(),
                    py), Math.max(bounds.getMaxX(), px), Math.max(bounds.getMaxY(), py));
        }
    }
    public Rectangle getBounds() {
        if (bounds != null) {
            return bounds;
        }
        if (npoints == 0) {
            return new Rectangle();
        }
        int bx1 = xpoints[0];
        int by1 = ypoints[0];
        int bx2 = bx1;
        int by2 = by1;
        for (int i = 1; i < npoints; i++) {
            int x = xpoints[i];
            int y = ypoints[i];
            if (x < bx1) {
                bx1 = x;
            } else if (x > bx2) {
                bx2 = x;
            }
            if (y < by1) {
                by1 = y;
            } else if (y > by2) {
                by2 = y;
            }
        }
        return bounds = new Rectangle(bx1, by1, bx2 - bx1, by2 - by1);
    }
    @Deprecated
    public Rectangle getBoundingBox() {
        return getBounds();
    }
    public Rectangle2D getBounds2D() {
        return getBounds().getBounds2D();
    }
    public void translate(int mx, int my) {
        for (int i = 0; i < npoints; i++) {
            xpoints[i] += mx;
            ypoints[i] += my;
        }
        if (bounds != null) {
            bounds.translate(mx, my);
        }
    }
    @Deprecated
    public boolean inside(int x, int y) {
        return contains((double)x, (double)y);
    }
    public boolean contains(int x, int y) {
        return contains((double)x, (double)y);
    }
    public boolean contains(double x, double y) {
        return Crossing.isInsideEvenOdd(Crossing.crossShape(this, x, y));
    }
    public boolean contains(double x, double y, double width, double height) {
        int cross = Crossing.intersectShape(this, x, y, width, height);
        return cross != Crossing.CROSSING && Crossing.isInsideEvenOdd(cross);
    }
    public boolean intersects(double x, double y, double width, double height) {
        int cross = Crossing.intersectShape(this, x, y, width, height);
        return cross == Crossing.CROSSING || Crossing.isInsideEvenOdd(cross);
    }
    public boolean contains(Rectangle2D rect) {
        return contains(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }
    public boolean contains(Point point) {
        return contains(point.getX(), point.getY());
    }
    public boolean contains(Point2D point) {
        return contains(point.getX(), point.getY());
    }
    public boolean intersects(Rectangle2D rect) {
        return intersects(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }
    public PathIterator getPathIterator(AffineTransform t) {
        return new Iterator(t, this);
    }
    public PathIterator getPathIterator(AffineTransform t, double flatness) {
        return new Iterator(t, this);
    }
}
