public class Crossing {
    static final double DELTA = 1E-5;
    static final double ROOT_DELTA = 1E-10;
    public static final int CROSSING = 255;
    static final int UNKNOWN = 254;
    public static int solveQuad(double eqn[], double res[]) {
        double a = eqn[2];
        double b = eqn[1];
        double c = eqn[0];
        int rc = 0;
        if (a == 0.0) {
            if (b == 0.0) {
                return -1;
            }
            res[rc++] = -c / b;
        } else {
            double d = b * b - 4.0 * a * c;
            if (d < 0.0) {
                return 0;
            }
            d = Math.sqrt(d);
            res[rc++] = (- b + d) / (a * 2.0);
            if (d != 0.0) {
                res[rc++] = (- b - d) / (a * 2.0);
            }
        }
        return fixRoots(res, rc);
    }
    public static int solveCubic(double eqn[], double res[]) {
        double d = eqn[3];
        if (d == 0) {
            return solveQuad(eqn, res);
        }
        double a = eqn[2] / d;
        double b = eqn[1] / d;
        double c = eqn[0] / d;
        int rc = 0;
        double Q = (a * a - 3.0 * b) / 9.0;
        double R = (2.0 * a * a * a - 9.0 * a * b + 27.0 * c) / 54.0;
        double Q3 = Q * Q * Q;
        double R2 = R * R;
        double n = - a / 3.0;
        if (R2 < Q3) {
            double t = Math.acos(R / Math.sqrt(Q3)) / 3.0;
            double p = 2.0 * Math.PI / 3.0;
            double m = -2.0 * Math.sqrt(Q);
            res[rc++] = m * Math.cos(t) + n;
            res[rc++] = m * Math.cos(t + p) + n;
            res[rc++] = m * Math.cos(t - p) + n;
        } else {
            double A = Math.pow(Math.abs(R) + Math.sqrt(R2 - Q3), 1.0 / 3.0);
            if (R > 0.0) {
                A = -A;
            }
            if (-ROOT_DELTA < A && A < ROOT_DELTA) {
                res[rc++] = n;
            } else {
                double B = Q / A;
                res[rc++] = A + B + n;
                double delta = R2 - Q3;
                if (-ROOT_DELTA < delta && delta < ROOT_DELTA) {
                    res[rc++] = - (A + B) / 2.0 + n;
                }
            }
        }
        return fixRoots(res, rc);
    }
    static int fixRoots(double res[], int rc) {
        int tc = 0;
        for(int i = 0; i < rc; i++) {
            out: {
                for(int j = i + 1; j < rc; j++) {
                    if (isZero(res[i] - res[j])) {
                        break out;
                    }
                }
                res[tc++] = res[i];
            }
        }
        return tc;
    }
    public static class QuadCurve {
        double ax, ay, bx, by;
        double Ax, Ay, Bx, By;
        public QuadCurve(double x1, double y1, double cx, double cy, double x2, double y2) {
            ax = x2 - x1;
            ay = y2 - y1;
            bx = cx - x1;
            by = cy - y1;
            Bx = bx + bx;   
            Ax = ax - Bx;   
            By = by + by;   
            Ay = ay - By;   
        }
        int cross(double res[], int rc, double py1, double py2) {
            int cross = 0;
            for (int i = 0; i < rc; i++) {
                double t = res[i];
                if (t < -DELTA || t > 1 + DELTA) {
                    continue;
                }
                if (t < DELTA) {
                    if (py1 < 0.0 && (bx != 0.0 ? bx : ax - bx) < 0.0) {
                        cross--;
                    }
                    continue;
                }
                if (t > 1 - DELTA) {
                    if (py1 < ay && (ax != bx ? ax - bx : bx) > 0.0) {
                        cross++;
                    }
                    continue;
                }
                double ry = t * (t * Ay + By);
                if (ry > py2) {
                    double rxt = t * Ax + bx;
                    if (rxt > -DELTA && rxt < DELTA) {
                        continue;
                    }
                    cross += rxt > 0.0 ? 1 : -1;
                }
            } 
            return cross;
        }
        int solvePoint(double res[], double px) {
            double eqn[] = {-px, Bx, Ax};
            return solveQuad(eqn, res);
        }
        int solveExtrem(double res[]) {
            int rc = 0;
            if (Ax != 0.0) {
                res[rc++] = - Bx / (Ax + Ax);
            }
            if (Ay != 0.0) {
                res[rc++] = - By / (Ay + Ay);
            }
            return rc;
        }
        int addBound(double bound[], int bc, double res[], int rc, double minX, double maxX, boolean changeId, int id) {
            for(int i = 0; i < rc; i++) {
                double t = res[i];
                if (t > -DELTA && t < 1 + DELTA) {
                    double rx = t * (t * Ax + Bx);
                    if (minX <= rx && rx <= maxX) {
                        bound[bc++] = t;
                        bound[bc++] = rx;
                        bound[bc++] = t * (t * Ay + By);
                        bound[bc++] = id;
                        if (changeId) {
                            id++;
                        }
                    }
                }
            }
            return bc;
        }
    }
    public static class CubicCurve {
        double ax, ay, bx, by, cx, cy;
        double Ax, Ay, Bx, By, Cx, Cy;
        double Ax3, Bx2;
        public CubicCurve(double x1, double y1, double cx1, double cy1, double cx2, double cy2, double x2, double y2) {
            ax = x2 - x1;
            ay = y2 - y1;
            bx = cx1 - x1;
            by = cy1 - y1;
            cx = cx2 - x1;
            cy = cy2 - y1;
            Cx = bx + bx + bx;           
            Bx = cx + cx + cx - Cx - Cx; 
            Ax = ax - Bx - Cx;           
            Cy = by + by + by;           
            By = cy + cy + cy - Cy - Cy; 
            Ay = ay - By - Cy;           
            Ax3 = Ax + Ax + Ax;
            Bx2 = Bx + Bx;
        }
        int cross(double res[], int rc, double py1, double py2) {
            int cross = 0;
            for (int i = 0; i < rc; i++) {
                double t = res[i];
                if (t < -DELTA || t > 1 + DELTA) {
                    continue;
                }
                if (t < DELTA) {
                    if (py1 < 0.0 && (bx != 0.0 ? bx : (cx != bx ? cx - bx : ax - cx)) < 0.0) {
                        cross--;
                    }
                    continue;
                }
                if (t > 1 - DELTA) {
                    if (py1 < ay && (ax != cx ? ax - cx : (cx != bx ? cx - bx : bx)) > 0.0) {
                        cross++;
                    }
                    continue;
                }
                double ry = t * (t * (t * Ay + By) + Cy);
                if (ry > py2) {
                    double rxt = t * (t * Ax3 + Bx2) + Cx;
                    if (rxt > -DELTA && rxt < DELTA) {
                        rxt = t * (Ax3 + Ax3) + Bx2;
                        if (rxt < -DELTA || rxt > DELTA) {
                            continue;
                        }
                        rxt = ax;
                    }
                    cross += rxt > 0.0 ? 1 : -1;
                }
            } 
            return cross;
        }
        int solvePoint(double res[], double px) {
            double eqn[] = {-px, Cx, Bx, Ax};
            return solveCubic(eqn, res);
        }
        int solveExtremX(double res[]) {
            double eqn[] = {Cx, Bx2, Ax3};
            return solveQuad(eqn, res);
        }
        int solveExtremY(double res[]) {
            double eqn[] = {Cy, By + By, Ay + Ay + Ay};
            return solveQuad(eqn, res);
        }
        int addBound(double bound[], int bc, double res[], int rc, double minX, double maxX, boolean changeId, int id) {
            for(int i = 0; i < rc; i++) {
                double t = res[i];
                if (t > -DELTA && t < 1 + DELTA) {
                    double rx = t * (t * (t * Ax + Bx) + Cx);
                    if (minX <= rx && rx <= maxX) {
                        bound[bc++] = t;
                        bound[bc++] = rx;
                        bound[bc++] = t * (t * (t * Ay + By) + Cy);
                        bound[bc++] = id;
                        if (changeId) {
                            id++;
                        }
                    }
                }
            }
            return bc;
        }
    }
    public static int crossLine(double x1, double y1, double x2, double y2, double x, double y) {
        if ((x < x1 && x < x2) ||
            (x > x1 && x > x2) ||
            (y > y1 && y > y2) ||
            (x1 == x2))
        {
            return 0;
        }
        if (y < y1 && y < y2) {
        } else {
            if ((y2 - y1) * (x - x1) / (x2 - x1) <= y - y1) {
                return 0;
            }
        }
        if (x == x1) {
            return x1 < x2 ? 0 : -1;
        }
        if (x == x2) {
            return x1 < x2 ? 1 : 0;
        }
        return x1 < x2 ? 1 : -1;
    }
    public static int crossQuad(double x1, double y1, double cx, double cy, double x2, double y2, double x, double y) {
        if ((x < x1 && x < cx && x < x2) ||
            (x > x1 && x > cx && x > x2) ||
            (y > y1 && y > cy && y > y2) ||
            (x1 == cx && cx == x2))
        {
            return 0;
        }
        if (y < y1 && y < cy && y < y2 && x != x1 && x != x2) {
            if (x1 < x2) {
                return x1 < x && x < x2 ? 1 : 0;
            }
            return x2 < x && x < x1 ? -1 : 0;
        }
        QuadCurve c = new QuadCurve(x1, y1, cx, cy, x2, y2);
        double px = x - x1;
        double py = y - y1;
        double res[] = new double[3];
        int rc = c.solvePoint(res, px);
        return c.cross(res, rc, py, py);
    }
    public static int crossCubic(double x1, double y1, double cx1, double cy1, double cx2, double cy2, double x2, double y2, double x, double y) {
        if ((x < x1 && x < cx1 && x < cx2 && x < x2) ||
            (x > x1 && x > cx1 && x > cx2 && x > x2) ||
            (y > y1 && y > cy1 && y > cy2 && y > y2) ||
            (x1 == cx1 && cx1 == cx2 && cx2 == x2))
        {
            return 0;
        }
        if (y < y1 && y < cy1 && y < cy2 && y < y2 && x != x1 && x != x2) {
            if (x1 < x2) {
                return x1 < x && x < x2 ? 1 : 0;
            }
            return x2 < x && x < x1 ? -1 : 0;
        }
        CubicCurve c = new CubicCurve(x1, y1, cx1, cy1, cx2, cy2, x2, y2);
        double px = x - x1;
        double py = y - y1;
        double res[] = new double[3];
        int rc = c.solvePoint(res, px);
        return c.cross(res, rc, py, py);
    }
    public static int crossPath(PathIterator p, double x, double y) {
        int cross = 0;
        double mx, my, cx, cy;
        mx = my = cx = cy = 0.0;
        double coords[] = new double[6];
        while (!p.isDone()) {
            switch (p.currentSegment(coords)) {
            case PathIterator.SEG_MOVETO:
                if (cx != mx || cy != my) {
                    cross += crossLine(cx, cy, mx, my, x, y);
                }
                mx = cx = coords[0];
                my = cy = coords[1];
                break;
            case PathIterator.SEG_LINETO:
                cross += crossLine(cx, cy, cx = coords[0], cy = coords[1], x, y);
                break;
            case PathIterator.SEG_QUADTO:
                cross += crossQuad(cx, cy, coords[0], coords[1], cx = coords[2], cy = coords[3], x, y);
                break;
            case PathIterator.SEG_CUBICTO:
                cross += crossCubic(cx, cy, coords[0], coords[1], coords[2], coords[3], cx = coords[4], cy = coords[5], x, y);
                break;
            case PathIterator.SEG_CLOSE:
                if (cy != my || cx != mx) {
                    cross += crossLine(cx, cy, cx = mx, cy = my, x, y);
                }
                break;
            }
            p.next();
        }
        if (cy != my) {
            cross += crossLine(cx, cy, mx, my, x, y);
        }
        return cross;
    }
    public static int crossShape(Shape s, double x, double y) {
        if (!s.getBounds2D().contains(x, y)) {
            return 0;
        }
        return crossPath(s.getPathIterator(null), x, y);
    }
    public static boolean isZero(double val) {
        return -DELTA < val && val < DELTA;
    }
    static void sortBound(double bound[], int bc) {
        for(int i = 0; i < bc - 4; i += 4) {
            int k = i;
            for(int j = i + 4; j < bc; j += 4) {
                if (bound[k] > bound[j]) {
                    k = j;
                }
            }
            if (k != i) {
                double tmp = bound[i];
                bound[i] = bound[k];
                bound[k] = tmp;
                tmp = bound[i + 1];
                bound[i + 1] = bound[k + 1];
                bound[k + 1] = tmp;
                tmp = bound[i + 2];
                bound[i + 2] = bound[k + 2];
                bound[k + 2] = tmp;
                tmp = bound[i + 3];
                bound[i + 3] = bound[k + 3];
                bound[k + 3] = tmp;
            }
        }
    }
    static int crossBound(double bound[], int bc, double py1, double py2) {
        if (bc == 0) {
            return 0;
        }
        int up = 0;
        int down = 0;
        for(int i = 2; i < bc; i += 4) {
            if (bound[i] < py1) {
                up++;
                continue;
            }
            if (bound[i] > py2) {
                down++;
                continue;
            }
            return CROSSING;
        }
        if (down == 0) {
            return 0;
        }
        if (up != 0) {
            sortBound(bound, bc);
            boolean sign = bound[2] > py2;
            for(int i = 6; i < bc; i += 4) {
                boolean sign2 = bound[i] > py2;
                if (sign != sign2 && bound[i + 1] != bound[i - 3]) {
                    return CROSSING;
                }
                sign = sign2;
            }
        }
        return UNKNOWN;
    }
    public static int intersectLine(double x1, double y1, double x2, double y2, double rx1, double ry1, double rx2, double ry2) {
        if ((rx2 < x1 && rx2 < x2) ||
            (rx1 > x1 && rx1 > x2) ||
            (ry1 > y1 && ry1 > y2))
        {
            return 0;
        }
        if (ry2 < y1 && ry2 < y2) {
        } else {
            if (x1 == x2) {
                return CROSSING;
            }
            double bx1, bx2;
            if (x1 < x2) {
                bx1 = x1 < rx1 ? rx1 : x1;
                bx2 = x2 < rx2 ? x2 : rx2;
            } else {
                bx1 = x2 < rx1 ? rx1 : x2;
                bx2 = x1 < rx2 ? x1 : rx2;
            }
            double k = (y2 - y1) / (x2 - x1);
            double by1 = k * (bx1 - x1) + y1;
            double by2 = k * (bx2 - x1) + y1;
            if (by1 < ry1 && by2 < ry1) {
                return 0;
            }
            if (by1 > ry2 && by2 > ry2) {
            } else {
                return CROSSING;
            }
        }
        if (x1 == x2) {
            return 0;
        }
        if (rx1 == x1) {
            return x1 < x2 ? 0 : -1;
        }
        if (rx1 == x2) {
            return x1 < x2 ? 1 : 0;
        }
        if (x1 < x2) {
            return x1 < rx1 && rx1 < x2 ? 1 : 0;
        }
        return x2 < rx1 && rx1 < x1 ? -1 : 0;
    }
    public static int intersectQuad(double x1, double y1, double cx, double cy, double x2, double y2, double rx1, double ry1, double rx2, double ry2) {
        if ((rx2 < x1 && rx2 < cx && rx2 < x2) ||
            (rx1 > x1 && rx1 > cx && rx1 > x2) ||
            (ry1 > y1 && ry1 > cy && ry1 > y2))
        {
            return 0;
        }
        if (ry2 < y1 && ry2 < cy && ry2 < y2 && rx1 != x1 && rx1 != x2) {
            if (x1 < x2) {
                return x1 < rx1 && rx1 < x2 ? 1 : 0;
            }
            return x2 < rx1 && rx1 < x1 ? -1 : 0;
        }
        QuadCurve c = new QuadCurve(x1, y1, cx, cy, x2, y2);
        double px1 = rx1 - x1;
        double py1 = ry1 - y1;
        double px2 = rx2 - x1;
        double py2 = ry2 - y1;
        double res1[] = new double[3];
        double res2[] = new double[3];
        int rc1 = c.solvePoint(res1, px1);
        int rc2 = c.solvePoint(res2, px2);
        if (rc1 == 0 && rc2 == 0) {
            return 0;
        }
        double minX = px1 - DELTA;
        double maxX = px2 + DELTA;
        double bound[] = new double[28];
        int bc = 0;
        bc = c.addBound(bound, bc, res1, rc1, minX, maxX, false, 0);
        bc = c.addBound(bound, bc, res2, rc2, minX, maxX, false, 1);
        rc2 = c.solveExtrem(res2);
        bc = c.addBound(bound, bc, res2, rc2, minX, maxX, true, 2);
        if (rx1 < x1 && x1 < rx2) {
            bound[bc++] = 0.0;
            bound[bc++] = 0.0;
            bound[bc++] = 0.0;
            bound[bc++] = 4;
        }
        if (rx1 < x2 && x2 < rx2) {
            bound[bc++] = 1.0;
            bound[bc++] = c.ax;
            bound[bc++] = c.ay;
            bound[bc++] = 5;
        }
        int cross = crossBound(bound, bc, py1, py2);
        if (cross != UNKNOWN) {
            return cross;
        }
        return c.cross(res1, rc1, py1, py2);
    }
    public static int intersectCubic(double x1, double y1, double cx1, double cy1, double cx2, double cy2, double x2, double y2, double rx1, double ry1, double rx2, double ry2) {
        if ((rx2 < x1 && rx2 < cx1 && rx2 < cx2 && rx2 < x2) ||
            (rx1 > x1 && rx1 > cx1 && rx1 > cx2 && rx1 > x2) ||
            (ry1 > y1 && ry1 > cy1 && ry1 > cy2 && ry1 > y2))
        {
            return 0;
        }
        if (ry2 < y1 && ry2 < cy1 && ry2 < cy2 && ry2 < y2 && rx1 != x1 && rx1 != x2) {
            if (x1 < x2) {
                return x1 < rx1 && rx1 < x2 ? 1 : 0;
            }
            return x2 < rx1 && rx1 < x1 ? -1 : 0;
        }
        CubicCurve c = new CubicCurve(x1, y1, cx1, cy1, cx2, cy2, x2, y2);
        double px1 = rx1 - x1;
        double py1 = ry1 - y1;
        double px2 = rx2 - x1;
        double py2 = ry2 - y1;
        double res1[] = new double[3];
        double res2[] = new double[3];
        int rc1 = c.solvePoint(res1, px1);
        int rc2 = c.solvePoint(res2, px2);
        if (rc1 == 0 && rc2 == 0) {
            return 0;
        }
        double minX = px1 - DELTA;
        double maxX = px2 + DELTA;
        double bound[] = new double[40];
        int bc = 0;
        bc = c.addBound(bound, bc, res1, rc1, minX, maxX, false, 0);
        bc = c.addBound(bound, bc, res2, rc2, minX, maxX, false, 1);
        rc2 = c.solveExtremX(res2);
        bc = c.addBound(bound, bc, res2, rc2, minX, maxX, true, 2);
        rc2 = c.solveExtremY(res2);
        bc = c.addBound(bound, bc, res2, rc2, minX, maxX, true, 4);
        if (rx1 < x1 && x1 < rx2) {
            bound[bc++] = 0.0;
            bound[bc++] = 0.0;
            bound[bc++] = 0.0;
            bound[bc++] = 6;
        }
        if (rx1 < x2 && x2 < rx2) {
            bound[bc++] = 1.0;
            bound[bc++] = c.ax;
            bound[bc++] = c.ay;
            bound[bc++] = 7;
        }
        int cross = crossBound(bound, bc, py1, py2);
        if (cross != UNKNOWN) {
            return cross;
        }
        return c.cross(res1, rc1, py1, py2);
    }
    public static int intersectPath(PathIterator p, double x, double y, double w, double h) {
        int cross = 0;
        int count;
        double mx, my, cx, cy;
        mx = my = cx = cy = 0.0;
        double coords[] = new double[6];
        double rx1 = x;
        double ry1 = y;
        double rx2 = x + w;
        double ry2 = y + h;
        while (!p.isDone()) {
            count = 0;
            switch (p.currentSegment(coords)) {
            case PathIterator.SEG_MOVETO:
                if (cx != mx || cy != my) {
                    count = intersectLine(cx, cy, mx, my, rx1, ry1, rx2, ry2);
                }
                mx = cx = coords[0];
                my = cy = coords[1];
                break;
            case PathIterator.SEG_LINETO:
                count = intersectLine(cx, cy, cx = coords[0], cy = coords[1], rx1, ry1, rx2, ry2);
                break;
            case PathIterator.SEG_QUADTO:
                count = intersectQuad(cx, cy, coords[0], coords[1], cx = coords[2], cy = coords[3], rx1, ry1, rx2, ry2);
                break;
            case PathIterator.SEG_CUBICTO:
                count = intersectCubic(cx, cy, coords[0], coords[1], coords[2], coords[3], cx = coords[4], cy = coords[5], rx1, ry1, rx2, ry2);
                break;
            case PathIterator.SEG_CLOSE:
                if (cy != my || cx != mx) {
                    count = intersectLine(cx, cy, mx, my, rx1, ry1, rx2, ry2);
                }
                cx = mx;
                cy = my;
                break;
            }
            if (count == CROSSING) {
                return CROSSING;
            }
            cross += count;
            p.next();
        }
        if (cy != my) {
            count = intersectLine(cx, cy, mx, my, rx1, ry1, rx2, ry2);
            if (count == CROSSING) {
                return CROSSING;
            }
            cross += count;
        }
        return cross;
    }
    public static int intersectShape(Shape s, double x, double y, double w, double h) {
        if (!s.getBounds2D().intersects(x, y, w, h)) {
            return 0;
        }
        return intersectPath(s.getPathIterator(null), x, y, w, h);
    }
    public static boolean isInsideNonZero(int cross) {
        return cross != 0;
    }
    public static boolean isInsideEvenOdd(int cross) {
        return (cross & 1) != 0;
    }
}