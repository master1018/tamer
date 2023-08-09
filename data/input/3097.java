public class Polygon implements Shape, java.io.Serializable {
    public int npoints;
    public int xpoints[];
    public int ypoints[];
    protected Rectangle bounds;
    private static final long serialVersionUID = -6460061437900069969L;
    private static final int MIN_LENGTH = 4;
    public Polygon() {
        xpoints = new int[MIN_LENGTH];
        ypoints = new int[MIN_LENGTH];
    }
    public Polygon(int xpoints[], int ypoints[], int npoints) {
        if (npoints > xpoints.length || npoints > ypoints.length) {
            throw new IndexOutOfBoundsException("npoints > xpoints.length || "+
                                                "npoints > ypoints.length");
        }
        if (npoints < 0) {
            throw new NegativeArraySizeException("npoints < 0");
        }
        this.npoints = npoints;
        this.xpoints = Arrays.copyOf(xpoints, npoints);
        this.ypoints = Arrays.copyOf(ypoints, npoints);
    }
    public void reset() {
        npoints = 0;
        bounds = null;
    }
    public void invalidate() {
        bounds = null;
    }
    public void translate(int deltaX, int deltaY) {
        for (int i = 0; i < npoints; i++) {
            xpoints[i] += deltaX;
            ypoints[i] += deltaY;
        }
        if (bounds != null) {
            bounds.translate(deltaX, deltaY);
        }
    }
    void calculateBounds(int xpoints[], int ypoints[], int npoints) {
        int boundsMinX = Integer.MAX_VALUE;
        int boundsMinY = Integer.MAX_VALUE;
        int boundsMaxX = Integer.MIN_VALUE;
        int boundsMaxY = Integer.MIN_VALUE;
        for (int i = 0; i < npoints; i++) {
            int x = xpoints[i];
            boundsMinX = Math.min(boundsMinX, x);
            boundsMaxX = Math.max(boundsMaxX, x);
            int y = ypoints[i];
            boundsMinY = Math.min(boundsMinY, y);
            boundsMaxY = Math.max(boundsMaxY, y);
        }
        bounds = new Rectangle(boundsMinX, boundsMinY,
                               boundsMaxX - boundsMinX,
                               boundsMaxY - boundsMinY);
    }
    void updateBounds(int x, int y) {
        if (x < bounds.x) {
            bounds.width = bounds.width + (bounds.x - x);
            bounds.x = x;
        }
        else {
            bounds.width = Math.max(bounds.width, x - bounds.x);
        }
        if (y < bounds.y) {
            bounds.height = bounds.height + (bounds.y - y);
            bounds.y = y;
        }
        else {
            bounds.height = Math.max(bounds.height, y - bounds.y);
        }
    }
    public void addPoint(int x, int y) {
        if (npoints >= xpoints.length || npoints >= ypoints.length) {
            int newLength = npoints * 2;
            if (newLength < MIN_LENGTH) {
                newLength = MIN_LENGTH;
            } else if ((newLength & (newLength - 1)) != 0) {
                newLength = Integer.highestOneBit(newLength);
            }
            xpoints = Arrays.copyOf(xpoints, newLength);
            ypoints = Arrays.copyOf(ypoints, newLength);
        }
        xpoints[npoints] = x;
        ypoints[npoints] = y;
        npoints++;
        if (bounds != null) {
            updateBounds(x, y);
        }
    }
    public Rectangle getBounds() {
        return getBoundingBox();
    }
    @Deprecated
    public Rectangle getBoundingBox() {
        if (npoints == 0) {
            return new Rectangle();
        }
        if (bounds == null) {
            calculateBounds(xpoints, ypoints, npoints);
        }
        return bounds.getBounds();
    }
    public boolean contains(Point p) {
        return contains(p.x, p.y);
    }
    public boolean contains(int x, int y) {
        return contains((double) x, (double) y);
    }
    @Deprecated
    public boolean inside(int x, int y) {
        return contains((double) x, (double) y);
    }
    public Rectangle2D getBounds2D() {
        return getBounds();
    }
    public boolean contains(double x, double y) {
        if (npoints <= 2 || !getBoundingBox().contains(x, y)) {
            return false;
        }
        int hits = 0;
        int lastx = xpoints[npoints - 1];
        int lasty = ypoints[npoints - 1];
        int curx, cury;
        for (int i = 0; i < npoints; lastx = curx, lasty = cury, i++) {
            curx = xpoints[i];
            cury = ypoints[i];
            if (cury == lasty) {
                continue;
            }
            int leftx;
            if (curx < lastx) {
                if (x >= lastx) {
                    continue;
                }
                leftx = curx;
            } else {
                if (x >= curx) {
                    continue;
                }
                leftx = lastx;
            }
            double test1, test2;
            if (cury < lasty) {
                if (y < cury || y >= lasty) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - curx;
                test2 = y - cury;
            } else {
                if (y < lasty || y >= cury) {
                    continue;
                }
                if (x < leftx) {
                    hits++;
                    continue;
                }
                test1 = x - lastx;
                test2 = y - lasty;
            }
            if (test1 < (test2 / (lasty - cury) * (lastx - curx))) {
                hits++;
            }
        }
        return ((hits & 1) != 0);
    }
    private Crossings getCrossings(double xlo, double ylo,
                                   double xhi, double yhi)
    {
        Crossings cross = new Crossings.EvenOdd(xlo, ylo, xhi, yhi);
        int lastx = xpoints[npoints - 1];
        int lasty = ypoints[npoints - 1];
        int curx, cury;
        for (int i = 0; i < npoints; i++) {
            curx = xpoints[i];
            cury = ypoints[i];
            if (cross.accumulateLine(lastx, lasty, curx, cury)) {
                return null;
            }
            lastx = curx;
            lasty = cury;
        }
        return cross;
    }
    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }
    public boolean intersects(double x, double y, double w, double h) {
        if (npoints <= 0 || !getBoundingBox().intersects(x, y, w, h)) {
            return false;
        }
        Crossings cross = getCrossings(x, y, x+w, y+h);
        return (cross == null || !cross.isEmpty());
    }
    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }
    public boolean contains(double x, double y, double w, double h) {
        if (npoints <= 0 || !getBoundingBox().intersects(x, y, w, h)) {
            return false;
        }
        Crossings cross = getCrossings(x, y, x+w, y+h);
        return (cross != null && cross.covers(y, y+h));
    }
    public boolean contains(Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }
    public PathIterator getPathIterator(AffineTransform at) {
        return new PolygonPathIterator(this, at);
    }
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return getPathIterator(at);
    }
    class PolygonPathIterator implements PathIterator {
        Polygon poly;
        AffineTransform transform;
        int index;
        public PolygonPathIterator(Polygon pg, AffineTransform at) {
            poly = pg;
            transform = at;
            if (pg.npoints == 0) {
                index = 1;
            }
        }
        public int getWindingRule() {
            return WIND_EVEN_ODD;
        }
        public boolean isDone() {
            return index > poly.npoints;
        }
        public void next() {
            index++;
        }
        public int currentSegment(float[] coords) {
            if (index >= poly.npoints) {
                return SEG_CLOSE;
            }
            coords[0] = poly.xpoints[index];
            coords[1] = poly.ypoints[index];
            if (transform != null) {
                transform.transform(coords, 0, coords, 0, 1);
            }
            return (index == 0 ? SEG_MOVETO : SEG_LINETO);
        }
        public int currentSegment(double[] coords) {
            if (index >= poly.npoints) {
                return SEG_CLOSE;
            }
            coords[0] = poly.xpoints[index];
            coords[1] = poly.ypoints[index];
            if (transform != null) {
                transform.transform(coords, 0, coords, 0, 1);
            }
            return (index == 0 ? SEG_MOVETO : SEG_LINETO);
        }
    }
}
