public abstract class CubicCurve2D implements Shape, Cloneable {
    public static class Float extends CubicCurve2D implements Serializable {
        public float x1;
        public float y1;
        public float ctrlx1;
        public float ctrly1;
        public float ctrlx2;
        public float ctrly2;
        public float x2;
        public float y2;
        public Float() {
        }
        public Float(float x1, float y1,
                     float ctrlx1, float ctrly1,
                     float ctrlx2, float ctrly2,
                     float x2, float y2)
        {
            setCurve(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2);
        }
        public double getX1() {
            return (double) x1;
        }
        public double getY1() {
            return (double) y1;
        }
        public Point2D getP1() {
            return new Point2D.Float(x1, y1);
        }
        public double getCtrlX1() {
            return (double) ctrlx1;
        }
        public double getCtrlY1() {
            return (double) ctrly1;
        }
        public Point2D getCtrlP1() {
            return new Point2D.Float(ctrlx1, ctrly1);
        }
        public double getCtrlX2() {
            return (double) ctrlx2;
        }
        public double getCtrlY2() {
            return (double) ctrly2;
        }
        public Point2D getCtrlP2() {
            return new Point2D.Float(ctrlx2, ctrly2);
        }
        public double getX2() {
            return (double) x2;
        }
        public double getY2() {
            return (double) y2;
        }
        public Point2D getP2() {
            return new Point2D.Float(x2, y2);
        }
        public void setCurve(double x1, double y1,
                             double ctrlx1, double ctrly1,
                             double ctrlx2, double ctrly2,
                             double x2, double y2)
        {
            this.x1     = (float) x1;
            this.y1     = (float) y1;
            this.ctrlx1 = (float) ctrlx1;
            this.ctrly1 = (float) ctrly1;
            this.ctrlx2 = (float) ctrlx2;
            this.ctrly2 = (float) ctrly2;
            this.x2     = (float) x2;
            this.y2     = (float) y2;
        }
        public void setCurve(float x1, float y1,
                             float ctrlx1, float ctrly1,
                             float ctrlx2, float ctrly2,
                             float x2, float y2)
        {
            this.x1     = x1;
            this.y1     = y1;
            this.ctrlx1 = ctrlx1;
            this.ctrly1 = ctrly1;
            this.ctrlx2 = ctrlx2;
            this.ctrly2 = ctrly2;
            this.x2     = x2;
            this.y2     = y2;
        }
        public Rectangle2D getBounds2D() {
            float left   = Math.min(Math.min(x1, x2),
                                    Math.min(ctrlx1, ctrlx2));
            float top    = Math.min(Math.min(y1, y2),
                                    Math.min(ctrly1, ctrly2));
            float right  = Math.max(Math.max(x1, x2),
                                    Math.max(ctrlx1, ctrlx2));
            float bottom = Math.max(Math.max(y1, y2),
                                    Math.max(ctrly1, ctrly2));
            return new Rectangle2D.Float(left, top,
                                         right - left, bottom - top);
        }
        private static final long serialVersionUID = -1272015596714244385L;
    }
    public static class Double extends CubicCurve2D implements Serializable {
        public double x1;
        public double y1;
        public double ctrlx1;
        public double ctrly1;
        public double ctrlx2;
        public double ctrly2;
        public double x2;
        public double y2;
        public Double() {
        }
        public Double(double x1, double y1,
                      double ctrlx1, double ctrly1,
                      double ctrlx2, double ctrly2,
                      double x2, double y2)
        {
            setCurve(x1, y1, ctrlx1, ctrly1, ctrlx2, ctrly2, x2, y2);
        }
        public double getX1() {
            return x1;
        }
        public double getY1() {
            return y1;
        }
        public Point2D getP1() {
            return new Point2D.Double(x1, y1);
        }
        public double getCtrlX1() {
            return ctrlx1;
        }
        public double getCtrlY1() {
            return ctrly1;
        }
        public Point2D getCtrlP1() {
            return new Point2D.Double(ctrlx1, ctrly1);
        }
        public double getCtrlX2() {
            return ctrlx2;
        }
        public double getCtrlY2() {
            return ctrly2;
        }
        public Point2D getCtrlP2() {
            return new Point2D.Double(ctrlx2, ctrly2);
        }
        public double getX2() {
            return x2;
        }
        public double getY2() {
            return y2;
        }
        public Point2D getP2() {
            return new Point2D.Double(x2, y2);
        }
        public void setCurve(double x1, double y1,
                             double ctrlx1, double ctrly1,
                             double ctrlx2, double ctrly2,
                             double x2, double y2)
        {
            this.x1     = x1;
            this.y1     = y1;
            this.ctrlx1 = ctrlx1;
            this.ctrly1 = ctrly1;
            this.ctrlx2 = ctrlx2;
            this.ctrly2 = ctrly2;
            this.x2     = x2;
            this.y2     = y2;
        }
        public Rectangle2D getBounds2D() {
            double left   = Math.min(Math.min(x1, x2),
                                     Math.min(ctrlx1, ctrlx2));
            double top    = Math.min(Math.min(y1, y2),
                                     Math.min(ctrly1, ctrly2));
            double right  = Math.max(Math.max(x1, x2),
                                     Math.max(ctrlx1, ctrlx2));
            double bottom = Math.max(Math.max(y1, y2),
                                     Math.max(ctrly1, ctrly2));
            return new Rectangle2D.Double(left, top,
                                          right - left, bottom - top);
        }
        private static final long serialVersionUID = -4202960122839707295L;
    }
    protected CubicCurve2D() {
    }
    public abstract double getX1();
    public abstract double getY1();
    public abstract Point2D getP1();
    public abstract double getCtrlX1();
    public abstract double getCtrlY1();
    public abstract Point2D getCtrlP1();
    public abstract double getCtrlX2();
    public abstract double getCtrlY2();
    public abstract Point2D getCtrlP2();
    public abstract double getX2();
    public abstract double getY2();
    public abstract Point2D getP2();
    public abstract void setCurve(double x1, double y1,
                                  double ctrlx1, double ctrly1,
                                  double ctrlx2, double ctrly2,
                                  double x2, double y2);
    public void setCurve(double[] coords, int offset) {
        setCurve(coords[offset + 0], coords[offset + 1],
                 coords[offset + 2], coords[offset + 3],
                 coords[offset + 4], coords[offset + 5],
                 coords[offset + 6], coords[offset + 7]);
    }
    public void setCurve(Point2D p1, Point2D cp1, Point2D cp2, Point2D p2) {
        setCurve(p1.getX(), p1.getY(), cp1.getX(), cp1.getY(),
                 cp2.getX(), cp2.getY(), p2.getX(), p2.getY());
    }
    public void setCurve(Point2D[] pts, int offset) {
        setCurve(pts[offset + 0].getX(), pts[offset + 0].getY(),
                 pts[offset + 1].getX(), pts[offset + 1].getY(),
                 pts[offset + 2].getX(), pts[offset + 2].getY(),
                 pts[offset + 3].getX(), pts[offset + 3].getY());
    }
    public void setCurve(CubicCurve2D c) {
        setCurve(c.getX1(), c.getY1(), c.getCtrlX1(), c.getCtrlY1(),
                 c.getCtrlX2(), c.getCtrlY2(), c.getX2(), c.getY2());
    }
    public static double getFlatnessSq(double x1, double y1,
                                       double ctrlx1, double ctrly1,
                                       double ctrlx2, double ctrly2,
                                       double x2, double y2) {
        return Math.max(Line2D.ptSegDistSq(x1, y1, x2, y2, ctrlx1, ctrly1),
                        Line2D.ptSegDistSq(x1, y1, x2, y2, ctrlx2, ctrly2));
    }
    public static double getFlatness(double x1, double y1,
                                     double ctrlx1, double ctrly1,
                                     double ctrlx2, double ctrly2,
                                     double x2, double y2) {
        return Math.sqrt(getFlatnessSq(x1, y1, ctrlx1, ctrly1,
                                       ctrlx2, ctrly2, x2, y2));
    }
    public static double getFlatnessSq(double coords[], int offset) {
        return getFlatnessSq(coords[offset + 0], coords[offset + 1],
                             coords[offset + 2], coords[offset + 3],
                             coords[offset + 4], coords[offset + 5],
                             coords[offset + 6], coords[offset + 7]);
    }
    public static double getFlatness(double coords[], int offset) {
        return getFlatness(coords[offset + 0], coords[offset + 1],
                           coords[offset + 2], coords[offset + 3],
                           coords[offset + 4], coords[offset + 5],
                           coords[offset + 6], coords[offset + 7]);
    }
    public double getFlatnessSq() {
        return getFlatnessSq(getX1(), getY1(), getCtrlX1(), getCtrlY1(),
                             getCtrlX2(), getCtrlY2(), getX2(), getY2());
    }
    public double getFlatness() {
        return getFlatness(getX1(), getY1(), getCtrlX1(), getCtrlY1(),
                           getCtrlX2(), getCtrlY2(), getX2(), getY2());
    }
    public void subdivide(CubicCurve2D left, CubicCurve2D right) {
        subdivide(this, left, right);
    }
    public static void subdivide(CubicCurve2D src,
                                 CubicCurve2D left,
                                 CubicCurve2D right) {
        double x1 = src.getX1();
        double y1 = src.getY1();
        double ctrlx1 = src.getCtrlX1();
        double ctrly1 = src.getCtrlY1();
        double ctrlx2 = src.getCtrlX2();
        double ctrly2 = src.getCtrlY2();
        double x2 = src.getX2();
        double y2 = src.getY2();
        double centerx = (ctrlx1 + ctrlx2) / 2.0;
        double centery = (ctrly1 + ctrly2) / 2.0;
        ctrlx1 = (x1 + ctrlx1) / 2.0;
        ctrly1 = (y1 + ctrly1) / 2.0;
        ctrlx2 = (x2 + ctrlx2) / 2.0;
        ctrly2 = (y2 + ctrly2) / 2.0;
        double ctrlx12 = (ctrlx1 + centerx) / 2.0;
        double ctrly12 = (ctrly1 + centery) / 2.0;
        double ctrlx21 = (ctrlx2 + centerx) / 2.0;
        double ctrly21 = (ctrly2 + centery) / 2.0;
        centerx = (ctrlx12 + ctrlx21) / 2.0;
        centery = (ctrly12 + ctrly21) / 2.0;
        if (left != null) {
            left.setCurve(x1, y1, ctrlx1, ctrly1,
                          ctrlx12, ctrly12, centerx, centery);
        }
        if (right != null) {
            right.setCurve(centerx, centery, ctrlx21, ctrly21,
                           ctrlx2, ctrly2, x2, y2);
        }
    }
    public static void subdivide(double src[], int srcoff,
                                 double left[], int leftoff,
                                 double right[], int rightoff) {
        double x1 = src[srcoff + 0];
        double y1 = src[srcoff + 1];
        double ctrlx1 = src[srcoff + 2];
        double ctrly1 = src[srcoff + 3];
        double ctrlx2 = src[srcoff + 4];
        double ctrly2 = src[srcoff + 5];
        double x2 = src[srcoff + 6];
        double y2 = src[srcoff + 7];
        if (left != null) {
            left[leftoff + 0] = x1;
            left[leftoff + 1] = y1;
        }
        if (right != null) {
            right[rightoff + 6] = x2;
            right[rightoff + 7] = y2;
        }
        x1 = (x1 + ctrlx1) / 2.0;
        y1 = (y1 + ctrly1) / 2.0;
        x2 = (x2 + ctrlx2) / 2.0;
        y2 = (y2 + ctrly2) / 2.0;
        double centerx = (ctrlx1 + ctrlx2) / 2.0;
        double centery = (ctrly1 + ctrly2) / 2.0;
        ctrlx1 = (x1 + centerx) / 2.0;
        ctrly1 = (y1 + centery) / 2.0;
        ctrlx2 = (x2 + centerx) / 2.0;
        ctrly2 = (y2 + centery) / 2.0;
        centerx = (ctrlx1 + ctrlx2) / 2.0;
        centery = (ctrly1 + ctrly2) / 2.0;
        if (left != null) {
            left[leftoff + 2] = x1;
            left[leftoff + 3] = y1;
            left[leftoff + 4] = ctrlx1;
            left[leftoff + 5] = ctrly1;
            left[leftoff + 6] = centerx;
            left[leftoff + 7] = centery;
        }
        if (right != null) {
            right[rightoff + 0] = centerx;
            right[rightoff + 1] = centery;
            right[rightoff + 2] = ctrlx2;
            right[rightoff + 3] = ctrly2;
            right[rightoff + 4] = x2;
            right[rightoff + 5] = y2;
        }
    }
    public static int solveCubic(double eqn[]) {
        return solveCubic(eqn, eqn);
    }
    public static int solveCubic(double eqn[], double res[]) {
        final double d = eqn[3];
        if (d == 0) {
            return QuadCurve2D.solveQuadratic(eqn, res);
        }
        final double A = eqn[2] / d;
        final double B = eqn[1] / d;
        final double C = eqn[0] / d;
        double sq_A = A * A;
        double p = 1.0/3 * (-1.0/3 * sq_A + B);
        double q = 1.0/2 * (2.0/27 * A * sq_A - 1.0/3 * A * B + C);
        double cb_p = p * p * p;
        double D = q * q + cb_p;
        final double sub = 1.0/3 * A;
        int num;
        if (D < 0) { 
            double phi = 1.0/3 * Math.acos(-q / Math.sqrt(-cb_p));
            double t = 2 * Math.sqrt(-p);
            if (res == eqn) {
                eqn = Arrays.copyOf(eqn, 4);
            }
            res[ 0 ] =  ( t * Math.cos(phi));
            res[ 1 ] =  (-t * Math.cos(phi + Math.PI / 3));
            res[ 2 ] =  (-t * Math.cos(phi - Math.PI / 3));
            num = 3;
            for (int i = 0; i < num; ++i) {
                res[ i ] -= sub;
            }
        } else {
            double sqrt_D = Math.sqrt(D);
            double u = Math.cbrt(sqrt_D - q);
            double v = - Math.cbrt(sqrt_D + q);
            double uv = u+v;
            num = 1;
            double err = 1200000000*ulp(abs(uv) + abs(sub));
            if (iszero(D, err) || within(u, v, err)) {
                if (res == eqn) {
                    eqn = Arrays.copyOf(eqn, 4);
                }
                res[1] = -(uv / 2) - sub;
                num = 2;
            }
            res[ 0 ] =  uv - sub;
        }
        if (num > 1) { 
            num = fixRoots(eqn, res, num);
        }
        if (num > 2 && (res[2] == res[1] || res[2] == res[0])) {
            num--;
        }
        if (num > 1 && res[1] == res[0]) {
            res[1] = res[--num]; 
        }
        return num;
    }
    private static int fixRoots(double[] eqn, double[] res, int num) {
        double[] intervals = {eqn[1], 2*eqn[2], 3*eqn[3]};
        int critCount = QuadCurve2D.solveQuadratic(intervals, intervals);
        if (critCount == 2 && intervals[0] == intervals[1]) {
            critCount--;
        }
        if (critCount == 2 && intervals[0] > intervals[1]) {
            double tmp = intervals[0];
            intervals[0] = intervals[1];
            intervals[1] = tmp;
        }
        if (num == 3) {
            double xe = getRootUpperBound(eqn);
            double x0 = -xe;
            Arrays.sort(res, 0, num);
            if (critCount == 2) {
                res[0] = refineRootWithHint(eqn, x0, intervals[0], res[0]);
                res[1] = refineRootWithHint(eqn, intervals[0], intervals[1], res[1]);
                res[2] = refineRootWithHint(eqn, intervals[1], xe, res[2]);
                return 3;
            } else if (critCount == 1) {
                double fxe = eqn[3];
                double fx0 = -fxe;
                double x1 = intervals[0];
                double fx1 = solveEqn(eqn, 3, x1);
                if (oppositeSigns(fx0, fx1)) {
                    res[0] = bisectRootWithHint(eqn, x0, x1, res[0]);
                } else if (oppositeSigns(fx1, fxe)) {
                    res[0] = bisectRootWithHint(eqn, x1, xe, res[2]);
                } else  {
                    res[0] = x1;
                }
            } else if (critCount == 0) {
                res[0] = bisectRootWithHint(eqn, x0, xe, res[1]);
            }
        } else if (num == 2 && critCount == 2) {
            double goodRoot = res[0];
            double badRoot = res[1];
            double x1 = intervals[0];
            double x2 = intervals[1];
            double x = abs(x1 - goodRoot) > abs(x2 - goodRoot) ? x1 : x2;
            double fx = solveEqn(eqn, 3, x);
            if (iszero(fx, 10000000*ulp(x))) {
                double badRootVal = solveEqn(eqn, 3, badRoot);
                res[1] = abs(badRootVal) < abs(fx) ? badRoot : x;
                return 2;
            }
        } 
        return 1;
    }
    private static double refineRootWithHint(double[] eqn, double min, double max, double t) {
        if (!inInterval(t, min, max)) {
            return t;
        }
        double[] deriv = {eqn[1], 2*eqn[2], 3*eqn[3]};
        double origt = t;
        for (int i = 0; i < 3; i++) {
            double slope = solveEqn(deriv, 2, t);
            double y = solveEqn(eqn, 3, t);
            double delta = - (y / slope);
            double newt = t + delta;
            if (slope == 0 || y == 0 || t == newt) {
                break;
            }
            t = newt;
        }
        if (within(t, origt, 1000*ulp(origt)) && inInterval(t, min, max)) {
            return t;
        }
        return origt;
    }
    private static double bisectRootWithHint(double[] eqn, double x0, double xe, double hint) {
        double delta1 = Math.min(abs(hint - x0) / 64, 0.0625);
        double delta2 = Math.min(abs(hint - xe) / 64, 0.0625);
        double x02 = hint - delta1;
        double xe2 = hint + delta2;
        double fx02 = solveEqn(eqn, 3, x02);
        double fxe2 = solveEqn(eqn, 3, xe2);
        while (oppositeSigns(fx02, fxe2)) {
            if (x02 >= xe2) {
                return x02;
            }
            x0 = x02;
            xe = xe2;
            delta1 /= 64;
            delta2 /= 64;
            x02 = hint - delta1;
            xe2 = hint + delta2;
            fx02 = solveEqn(eqn, 3, x02);
            fxe2 = solveEqn(eqn, 3, xe2);
        }
        if (fx02 == 0) {
            return x02;
        }
        if (fxe2 == 0) {
            return xe2;
        }
        return bisectRoot(eqn, x0, xe);
    }
    private static double bisectRoot(double[] eqn, double x0, double xe) {
        double fx0 = solveEqn(eqn, 3, x0);
        double m = x0 + (xe - x0) / 2;
        while (m != x0 && m != xe) {
            double fm = solveEqn(eqn, 3, m);
            if (fm == 0) {
                return m;
            }
            if (oppositeSigns(fx0, fm)) {
                xe = m;
            } else {
                fx0 = fm;
                x0 = m;
            }
            m = x0 + (xe-x0)/2;
        }
        return m;
    }
    private static boolean inInterval(double t, double min, double max) {
        return min <= t && t <= max;
    }
    private static boolean within(double x, double y, double err) {
        double d = y - x;
        return (d <= err && d >= -err);
    }
    private static boolean iszero(double x, double err) {
        return within(x, 0, err);
    }
    private static boolean oppositeSigns(double x1, double x2) {
        return (x1 < 0 && x2 > 0) || (x1 > 0 && x2 < 0);
    }
    private static double solveEqn(double eqn[], int order, double t) {
        double v = eqn[order];
        while (--order >= 0) {
            v = v * t + eqn[order];
        }
        return v;
    }
    private static double getRootUpperBound(double[] eqn) {
        double d = eqn[3];
        double a = eqn[2];
        double b = eqn[1];
        double c = eqn[0];
        double M = 1 + max(max(abs(a), abs(b)), abs(c)) / abs(d);
        M += ulp(M) + 1;
        return M;
    }
    public boolean contains(double x, double y) {
        if (!(x * 0.0 + y * 0.0 == 0.0)) {
            return false;
        }
        double x1 = getX1();
        double y1 = getY1();
        double x2 = getX2();
        double y2 = getY2();
        int crossings =
            (Curve.pointCrossingsForLine(x, y, x1, y1, x2, y2) +
             Curve.pointCrossingsForCubic(x, y,
                                          x1, y1,
                                          getCtrlX1(), getCtrlY1(),
                                          getCtrlX2(), getCtrlY2(),
                                          x2, y2, 0));
        return ((crossings & 1) == 1);
    }
    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }
    public boolean intersects(double x, double y, double w, double h) {
        if (w <= 0 || h <= 0) {
            return false;
        }
        int numCrossings = rectCrossings(x, y, w, h);
        return numCrossings != 0;
    }
    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }
    public boolean contains(double x, double y, double w, double h) {
        if (w <= 0 || h <= 0) {
            return false;
        }
        int numCrossings = rectCrossings(x, y, w, h);
        return !(numCrossings == 0 || numCrossings == Curve.RECT_INTERSECTS);
    }
    private int rectCrossings(double x, double y, double w, double h) {
        int crossings = 0;
        if (!(getX1() == getX2() && getY1() == getY2())) {
            crossings = Curve.rectCrossingsForLine(crossings,
                                                   x, y,
                                                   x+w, y+h,
                                                   getX1(), getY1(),
                                                   getX2(), getY2());
            if (crossings == Curve.RECT_INTERSECTS) {
                return crossings;
            }
        }
        return Curve.rectCrossingsForCubic(crossings,
                                           x, y,
                                           x+w, y+h,
                                           getX2(), getY2(),
                                           getCtrlX2(), getCtrlY2(),
                                           getCtrlX1(), getCtrlY1(),
                                           getX1(), getY1(), 0);
    }
    public boolean contains(Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }
    public Rectangle getBounds() {
        return getBounds2D().getBounds();
    }
    public PathIterator getPathIterator(AffineTransform at) {
        return new CubicIterator(this, at);
    }
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new FlatteningPathIterator(getPathIterator(at), flatness);
    }
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}
