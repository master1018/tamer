final class Stroker implements PathConsumer2D {
    private static final int MOVE_TO = 0;
    private static final int DRAWING_OP_TO = 1; 
    private static final int CLOSE = 2;
    public static final int JOIN_MITER = 0;
    public static final int JOIN_ROUND = 1;
    public static final int JOIN_BEVEL = 2;
    public static final int CAP_BUTT = 0;
    public static final int CAP_ROUND = 1;
    public static final int CAP_SQUARE = 2;
    private final PathConsumer2D out;
    private final int capStyle;
    private final int joinStyle;
    private final float lineWidth2;
    private final float[][] offset = new float[3][2];
    private final float[] miter = new float[2];
    private final float miterLimitSq;
    private int prev;
    private float sx0, sy0, sdx, sdy;
    private float cx0, cy0, cdx, cdy; 
    private float smx, smy, cmx, cmy;
    private final PolyStack reverse = new PolyStack();
    public Stroker(PathConsumer2D pc2d,
                   float lineWidth,
                   int capStyle,
                   int joinStyle,
                   float miterLimit)
    {
        this.out = pc2d;
        this.lineWidth2 = lineWidth / 2;
        this.capStyle = capStyle;
        this.joinStyle = joinStyle;
        float limit = miterLimit * lineWidth2;
        this.miterLimitSq = limit*limit;
        this.prev = CLOSE;
    }
    private static void computeOffset(final float lx, final float ly,
                                      final float w, final float[] m)
    {
        final float len = (float) sqrt(lx*lx + ly*ly);
        if (len == 0) {
            m[0] = m[1] = 0;
        } else {
            m[0] = (ly * w)/len;
            m[1] = -(lx * w)/len;
        }
    }
    private static boolean isCW(final float dx1, final float dy1,
                                final float dx2, final float dy2)
    {
        return dx1 * dy2 <= dy1 * dx2;
    }
    private static final float ROUND_JOIN_THRESHOLD = 1000/65536f;
    private void drawRoundJoin(float x, float y,
                               float omx, float omy, float mx, float my,
                               boolean rev,
                               float threshold)
    {
        if ((omx == 0 && omy == 0) || (mx == 0 && my == 0)) {
            return;
        }
        float domx = omx - mx;
        float domy = omy - my;
        float len = domx*domx + domy*domy;
        if (len < threshold) {
            return;
        }
        if (rev) {
            omx = -omx;
            omy = -omy;
            mx = -mx;
            my = -my;
        }
        drawRoundJoin(x, y, omx, omy, mx, my, rev);
    }
    private void drawRoundJoin(float cx, float cy,
                               float omx, float omy,
                               float mx, float my,
                               boolean rev)
    {
        double cosext = omx * mx + omy * my;
        final int numCurves = cosext >= 0 ? 1 : 2;
        switch (numCurves) {
        case 1:
            drawBezApproxForArc(cx, cy, omx, omy, mx, my, rev);
            break;
        case 2:
            float nx = my - omy, ny = omx - mx;
            float nlen = (float) sqrt(nx*nx + ny*ny);
            float scale = lineWidth2/nlen;
            float mmx = nx * scale, mmy = ny * scale;
            if (rev) {
                mmx = -mmx;
                mmy = -mmy;
            }
            drawBezApproxForArc(cx, cy, omx, omy, mmx, mmy, rev);
            drawBezApproxForArc(cx, cy, mmx, mmy, mx, my, rev);
            break;
        }
    }
    private void drawBezApproxForArc(final float cx, final float cy,
                                     final float omx, final float omy,
                                     final float mx, final float my,
                                     boolean rev)
    {
        float cosext2 = (omx * mx + omy * my) / (2 * lineWidth2 * lineWidth2);
        float cv = (float) ((4.0 / 3.0) * sqrt(0.5-cosext2) /
                            (1.0 + sqrt(cosext2+0.5)));
        if (rev) { 
            cv = -cv;
        }
        final float x1 = cx + omx;
        final float y1 = cy + omy;
        final float x2 = x1 - cv * omy;
        final float y2 = y1 + cv * omx;
        final float x4 = cx + mx;
        final float y4 = cy + my;
        final float x3 = x4 + cv * my;
        final float y3 = y4 - cv * mx;
        emitCurveTo(x1, y1, x2, y2, x3, y3, x4, y4, rev);
    }
    private void drawRoundCap(float cx, float cy, float mx, float my) {
        final float C = 0.5522847498307933f;
        emitCurveTo(cx+mx,      cy+my,
                    cx+mx-C*my, cy+my+C*mx,
                    cx-my+C*mx, cy+mx+C*my,
                    cx-my,      cy+mx,
                    false);
        emitCurveTo(cx-my,      cy+mx,
                    cx-my-C*mx, cy+mx-C*my,
                    cx-mx-C*my, cy-my+C*mx,
                    cx-mx,      cy-my,
                    false);
    }
    private void computeIntersection(final float x0, final float y0,
                                     final float x1, final float y1,
                                     final float x0p, final float y0p,
                                     final float x1p, final float y1p,
                                     final float[] m, int off)
    {
        float x10 = x1 - x0;
        float y10 = y1 - y0;
        float x10p = x1p - x0p;
        float y10p = y1p - y0p;
        float den = x10*y10p - x10p*y10;
        float t = x10p*(y0-y0p) - y10p*(x0-x0p);
        t /= den;
        m[off++] = x0 + t*x10;
        m[off] = y0 + t*y10;
    }
    private void drawMiter(final float pdx, final float pdy,
                           final float x0, final float y0,
                           final float dx, final float dy,
                           float omx, float omy, float mx, float my,
                           boolean rev)
    {
        if ((mx == omx && my == omy) ||
            (pdx == 0 && pdy == 0) ||
            (dx == 0 && dy == 0))
        {
            return;
        }
        if (rev) {
            omx = -omx;
            omy = -omy;
            mx = -mx;
            my = -my;
        }
        computeIntersection((x0 - pdx) + omx, (y0 - pdy) + omy, x0 + omx, y0 + omy,
                            (dx + x0) + mx, (dy + y0) + my, x0 + mx, y0 + my,
                            miter, 0);
        float lenSq = (miter[0]-x0)*(miter[0]-x0) + (miter[1]-y0)*(miter[1]-y0);
        if (lenSq < miterLimitSq) {
            emitLineTo(miter[0], miter[1], rev);
        }
    }
    public void moveTo(float x0, float y0) {
        if (prev == DRAWING_OP_TO) {
            finish();
        }
        this.sx0 = this.cx0 = x0;
        this.sy0 = this.cy0 = y0;
        this.cdx = this.sdx = 1;
        this.cdy = this.sdy = 0;
        this.prev = MOVE_TO;
    }
    public void lineTo(float x1, float y1) {
        float dx = x1 - cx0;
        float dy = y1 - cy0;
        if (dx == 0f && dy == 0f) {
            dx = 1;
        }
        computeOffset(dx, dy, lineWidth2, offset[0]);
        float mx = offset[0][0];
        float my = offset[0][1];
        drawJoin(cdx, cdy, cx0, cy0, dx, dy, cmx, cmy, mx, my);
        emitLineTo(cx0 + mx, cy0 + my);
        emitLineTo(x1 + mx, y1 + my);
        emitLineTo(cx0 - mx, cy0 - my, true);
        emitLineTo(x1 - mx, y1 - my, true);
        this.cmx = mx;
        this.cmy = my;
        this.cdx = dx;
        this.cdy = dy;
        this.cx0 = x1;
        this.cy0 = y1;
        this.prev = DRAWING_OP_TO;
    }
    public void closePath() {
        if (prev != DRAWING_OP_TO) {
            if (prev == CLOSE) {
                return;
            }
            emitMoveTo(cx0, cy0 - lineWidth2);
            this.cmx = this.smx = 0;
            this.cmy = this.smy = -lineWidth2;
            this.cdx = this.sdx = 1;
            this.cdy = this.sdy = 0;
            finish();
            return;
        }
        if (cx0 != sx0 || cy0 != sy0) {
            lineTo(sx0, sy0);
        }
        drawJoin(cdx, cdy, cx0, cy0, sdx, sdy, cmx, cmy, smx, smy);
        emitLineTo(sx0 + smx, sy0 + smy);
        emitMoveTo(sx0 - smx, sy0 - smy);
        emitReverse();
        this.prev = CLOSE;
        emitClose();
    }
    private void emitReverse() {
        while(!reverse.isEmpty()) {
            reverse.pop(out);
        }
    }
    public void pathDone() {
        if (prev == DRAWING_OP_TO) {
            finish();
        }
        out.pathDone();
        this.prev = CLOSE;
    }
    private void finish() {
        if (capStyle == CAP_ROUND) {
            drawRoundCap(cx0, cy0, cmx, cmy);
        } else if (capStyle == CAP_SQUARE) {
            emitLineTo(cx0 - cmy + cmx, cy0 + cmx + cmy);
            emitLineTo(cx0 - cmy - cmx, cy0 + cmx - cmy);
        }
        emitReverse();
        if (capStyle == CAP_ROUND) {
            drawRoundCap(sx0, sy0, -smx, -smy);
        } else if (capStyle == CAP_SQUARE) {
            emitLineTo(sx0 + smy - smx, sy0 - smx - smy);
            emitLineTo(sx0 + smy + smx, sy0 - smx + smy);
        }
        emitClose();
    }
    private void emitMoveTo(final float x0, final float y0) {
        out.moveTo(x0, y0);
    }
    private void emitLineTo(final float x1, final float y1) {
        out.lineTo(x1, y1);
    }
    private void emitLineTo(final float x1, final float y1,
                            final boolean rev)
    {
        if (rev) {
            reverse.pushLine(x1, y1);
        } else {
            emitLineTo(x1, y1);
        }
    }
    private void emitQuadTo(final float x0, final float y0,
                            final float x1, final float y1,
                            final float x2, final float y2, final boolean rev)
    {
        if (rev) {
            reverse.pushQuad(x0, y0, x1, y1);
        } else {
            out.quadTo(x1, y1, x2, y2);
        }
    }
    private void emitCurveTo(final float x0, final float y0,
                             final float x1, final float y1,
                             final float x2, final float y2,
                             final float x3, final float y3, final boolean rev)
    {
        if (rev) {
            reverse.pushCubic(x0, y0, x1, y1, x2, y2);
        } else {
            out.curveTo(x1, y1, x2, y2, x3, y3);
        }
    }
    private void emitClose() {
        out.closePath();
    }
    private void drawJoin(float pdx, float pdy,
                          float x0, float y0,
                          float dx, float dy,
                          float omx, float omy,
                          float mx, float my)
    {
        if (prev != DRAWING_OP_TO) {
            emitMoveTo(x0 + mx, y0 + my);
            this.sdx = dx;
            this.sdy = dy;
            this.smx = mx;
            this.smy = my;
        } else {
            boolean cw = isCW(pdx, pdy, dx, dy);
            if (joinStyle == JOIN_MITER) {
                drawMiter(pdx, pdy, x0, y0, dx, dy, omx, omy, mx, my, cw);
            } else if (joinStyle == JOIN_ROUND) {
                drawRoundJoin(x0, y0,
                              omx, omy,
                              mx, my, cw,
                              ROUND_JOIN_THRESHOLD);
            }
            emitLineTo(x0, y0, !cw);
        }
        prev = DRAWING_OP_TO;
    }
    private static boolean within(final float x1, final float y1,
                                  final float x2, final float y2,
                                  final float ERR)
    {
        assert ERR > 0 : "";
        return (Helpers.within(x1, x2, ERR) &&  
                Helpers.within(y1, y2, ERR)); 
    }
    private void getLineOffsets(float x1, float y1,
                                float x2, float y2,
                                float[] left, float[] right) {
        computeOffset(x2 - x1, y2 - y1, lineWidth2, offset[0]);
        left[0] = x1 + offset[0][0];
        left[1] = y1 + offset[0][1];
        left[2] = x2 + offset[0][0];
        left[3] = y2 + offset[0][1];
        right[0] = x1 - offset[0][0];
        right[1] = y1 - offset[0][1];
        right[2] = x2 - offset[0][0];
        right[3] = y2 - offset[0][1];
    }
    private int computeOffsetCubic(float[] pts, final int off,
                                   float[] leftOff, float[] rightOff)
    {
        final float x1 = pts[off + 0], y1 = pts[off + 1];
        final float x2 = pts[off + 2], y2 = pts[off + 3];
        final float x3 = pts[off + 4], y3 = pts[off + 5];
        final float x4 = pts[off + 6], y4 = pts[off + 7];
        float dx4 = x4 - x3;
        float dy4 = y4 - y3;
        float dx1 = x2 - x1;
        float dy1 = y2 - y1;
        final boolean p1eqp2 = within(x1,y1,x2,y2, 6 * ulp(y2));
        final boolean p3eqp4 = within(x3,y3,x4,y4, 6 * ulp(y4));
        if (p1eqp2 && p3eqp4) {
            getLineOffsets(x1, y1, x4, y4, leftOff, rightOff);
            return 4;
        } else if (p1eqp2) {
            dx1 = x3 - x1;
            dy1 = y3 - y1;
        } else if (p3eqp4) {
            dx4 = x4 - x2;
            dy4 = y4 - y2;
        }
        float dotsq = (dx1 * dx4 + dy1 * dy4);
        dotsq = dotsq * dotsq;
        float l1sq = dx1 * dx1 + dy1 * dy1, l4sq = dx4 * dx4 + dy4 * dy4;
        if (Helpers.within(dotsq, l1sq * l4sq, 4 * ulp(dotsq))) {
            getLineOffsets(x1, y1, x4, y4, leftOff, rightOff);
            return 4;
        }
        float x = 0.125f * (x1 + 3 * (x2 + x3) + x4);
        float y = 0.125f * (y1 + 3 * (y2 + y3) + y4);
        float dxm = x3 + x4 - x1 - x2, dym = y3 + y4 - y1 - y2;
        computeOffset(dx1, dy1, lineWidth2, offset[0]);
        computeOffset(dxm, dym, lineWidth2, offset[1]);
        computeOffset(dx4, dy4, lineWidth2, offset[2]);
        float x1p = x1 + offset[0][0]; 
        float y1p = y1 + offset[0][1]; 
        float xi  = x + offset[1][0]; 
        float yi  = y + offset[1][1]; 
        float x4p = x4 + offset[2][0]; 
        float y4p = y4 + offset[2][1]; 
        float invdet43 = 4f / (3f * (dx1 * dy4 - dy1 * dx4));
        float two_pi_m_p1_m_p4x = 2*xi - x1p - x4p;
        float two_pi_m_p1_m_p4y = 2*yi - y1p - y4p;
        float c1 = invdet43 * (dy4 * two_pi_m_p1_m_p4x - dx4 * two_pi_m_p1_m_p4y);
        float c2 = invdet43 * (dx1 * two_pi_m_p1_m_p4y - dy1 * two_pi_m_p1_m_p4x);
        float x2p, y2p, x3p, y3p;
        x2p = x1p + c1*dx1;
        y2p = y1p + c1*dy1;
        x3p = x4p + c2*dx4;
        y3p = y4p + c2*dy4;
        leftOff[0] = x1p; leftOff[1] = y1p;
        leftOff[2] = x2p; leftOff[3] = y2p;
        leftOff[4] = x3p; leftOff[5] = y3p;
        leftOff[6] = x4p; leftOff[7] = y4p;
        x1p = x1 - offset[0][0]; y1p = y1 - offset[0][1];
        xi = xi - 2 * offset[1][0]; yi = yi - 2 * offset[1][1];
        x4p = x4 - offset[2][0]; y4p = y4 - offset[2][1];
        two_pi_m_p1_m_p4x = 2*xi - x1p - x4p;
        two_pi_m_p1_m_p4y = 2*yi - y1p - y4p;
        c1 = invdet43 * (dy4 * two_pi_m_p1_m_p4x - dx4 * two_pi_m_p1_m_p4y);
        c2 = invdet43 * (dx1 * two_pi_m_p1_m_p4y - dy1 * two_pi_m_p1_m_p4x);
        x2p = x1p + c1*dx1;
        y2p = y1p + c1*dy1;
        x3p = x4p + c2*dx4;
        y3p = y4p + c2*dy4;
        rightOff[0] = x1p; rightOff[1] = y1p;
        rightOff[2] = x2p; rightOff[3] = y2p;
        rightOff[4] = x3p; rightOff[5] = y3p;
        rightOff[6] = x4p; rightOff[7] = y4p;
        return 8;
    }
    private int computeOffsetQuad(float[] pts, final int off,
                                  float[] leftOff, float[] rightOff)
    {
        final float x1 = pts[off + 0], y1 = pts[off + 1];
        final float x2 = pts[off + 2], y2 = pts[off + 3];
        final float x3 = pts[off + 4], y3 = pts[off + 5];
        final float dx3 = x3 - x2;
        final float dy3 = y3 - y2;
        final float dx1 = x2 - x1;
        final float dy1 = y2 - y1;
        computeOffset(dx1, dy1, lineWidth2, offset[0]);
        computeOffset(dx3, dy3, lineWidth2, offset[1]);
        leftOff[0]  = x1 + offset[0][0];  leftOff[1] = y1 + offset[0][1];
        leftOff[4]  = x3 + offset[1][0];  leftOff[5] = y3 + offset[1][1];
        rightOff[0] = x1 - offset[0][0]; rightOff[1] = y1 - offset[0][1];
        rightOff[4] = x3 - offset[1][0]; rightOff[5] = y3 - offset[1][1];
        float x1p = leftOff[0]; 
        float y1p = leftOff[1]; 
        float x3p = leftOff[4]; 
        float y3p = leftOff[5]; 
        computeIntersection(x1p, y1p, x1p+dx1, y1p+dy1, x3p, y3p, x3p-dx3, y3p-dy3, leftOff, 2);
        float cx = leftOff[2];
        float cy = leftOff[3];
        if (!(isFinite(cx) && isFinite(cy))) {
            x1p = rightOff[0];
            y1p = rightOff[1];
            x3p = rightOff[4];
            y3p = rightOff[5];
            computeIntersection(x1p, y1p, x1p+dx1, y1p+dy1, x3p, y3p, x3p-dx3, y3p-dy3, rightOff, 2);
            cx = rightOff[2];
            cy = rightOff[3];
            if (!(isFinite(cx) && isFinite(cy))) {
                getLineOffsets(x1, y1, x3, y3, leftOff, rightOff);
                return 4;
            }
            leftOff[2] = 2*x2 - cx;
            leftOff[3] = 2*y2 - cy;
            return 6;
        }
        rightOff[2] = 2*x2 - cx;
        rightOff[3] = 2*y2 - cy;
        return 6;
    }
    private static boolean isFinite(float x) {
        return (Float.NEGATIVE_INFINITY < x && x < Float.POSITIVE_INFINITY);
    }
    private float[] middle = new float[2*8];
    private float[] lp = new float[8];
    private float[] rp = new float[8];
    private static final int MAX_N_CURVES = 11;
    private float[] subdivTs = new float[MAX_N_CURVES - 1];
    private static Curve c = new Curve();
    private static int findSubdivPoints(float[] pts, float[] ts, final int type, final float w)
    {
        final float x12 = pts[2] - pts[0];
        final float y12 = pts[3] - pts[1];
        if (y12 != 0f && x12 != 0f) {
            final float hypot = (float) sqrt(x12 * x12 + y12 * y12);
            final float cos = x12 / hypot;
            final float sin = y12 / hypot;
            final float x1 = cos * pts[0] + sin * pts[1];
            final float y1 = cos * pts[1] - sin * pts[0];
            final float x2 = cos * pts[2] + sin * pts[3];
            final float y2 = cos * pts[3] - sin * pts[2];
            final float x3 = cos * pts[4] + sin * pts[5];
            final float y3 = cos * pts[5] - sin * pts[4];
            switch(type) {
            case 8:
                final float x4 = cos * pts[6] + sin * pts[7];
                final float y4 = cos * pts[7] - sin * pts[6];
                c.set(x1, y1, x2, y2, x3, y3, x4, y4);
                break;
            case 6:
                c.set(x1, y1, x2, y2, x3, y3);
                break;
            }
        } else {
            c.set(pts, type);
        }
        int ret = 0;
        ret += c.dxRoots(ts, ret);
        ret += c.dyRoots(ts, ret);
        if (type == 8) {
            ret += c.infPoints(ts, ret);
        }
        ret += c.rootsOfROCMinusW(ts, ret, w, 0.0001f);
        ret = Helpers.filterOutNotInAB(ts, 0, ret, 0.0001f, 0.9999f);
        Helpers.isort(ts, 0, ret);
        return ret;
    }
    @Override public void curveTo(float x1, float y1,
                                  float x2, float y2,
                                  float x3, float y3)
    {
        middle[0] = cx0; middle[1] = cy0;
        middle[2] = x1;  middle[3] = y1;
        middle[4] = x2;  middle[5] = y2;
        middle[6] = x3;  middle[7] = y3;
        final float xf = middle[6], yf = middle[7];
        float dxs = middle[2] - middle[0];
        float dys = middle[3] - middle[1];
        float dxf = middle[6] - middle[4];
        float dyf = middle[7] - middle[5];
        boolean p1eqp2 = (dxs == 0f && dys == 0f);
        boolean p3eqp4 = (dxf == 0f && dyf == 0f);
        if (p1eqp2) {
            dxs = middle[4] - middle[0];
            dys = middle[5] - middle[1];
            if (dxs == 0f && dys == 0f) {
                dxs = middle[6] - middle[0];
                dys = middle[7] - middle[1];
            }
        }
        if (p3eqp4) {
            dxf = middle[6] - middle[2];
            dyf = middle[7] - middle[3];
            if (dxf == 0f && dyf == 0f) {
                dxf = middle[6] - middle[0];
                dyf = middle[7] - middle[1];
            }
        }
        if (dxs == 0f && dys == 0f) {
            lineTo(middle[0], middle[1]);
            return;
        }
        if (Math.abs(dxs) < 0.1f && Math.abs(dys) < 0.1f) {
            float len = (float) sqrt(dxs*dxs + dys*dys);
            dxs /= len;
            dys /= len;
        }
        if (Math.abs(dxf) < 0.1f && Math.abs(dyf) < 0.1f) {
            float len = (float) sqrt(dxf*dxf + dyf*dyf);
            dxf /= len;
            dyf /= len;
        }
        computeOffset(dxs, dys, lineWidth2, offset[0]);
        final float mx = offset[0][0];
        final float my = offset[0][1];
        drawJoin(cdx, cdy, cx0, cy0, dxs, dys, cmx, cmy, mx, my);
        int nSplits = findSubdivPoints(middle, subdivTs, 8, lineWidth2);
        int kind = 0;
        Iterator<Integer> it = Curve.breakPtsAtTs(middle, 8, subdivTs, nSplits);
        while(it.hasNext()) {
            int curCurveOff = it.next();
            kind = computeOffsetCubic(middle, curCurveOff, lp, rp);
            emitLineTo(lp[0], lp[1]);
            switch(kind) {
            case 8:
                emitCurveTo(lp[0], lp[1], lp[2], lp[3], lp[4], lp[5], lp[6], lp[7], false);
                emitCurveTo(rp[0], rp[1], rp[2], rp[3], rp[4], rp[5], rp[6], rp[7], true);
                break;
            case 4:
                emitLineTo(lp[2], lp[3]);
                emitLineTo(rp[0], rp[1], true);
                break;
            }
            emitLineTo(rp[kind - 2], rp[kind - 1], true);
        }
        this.cmx = (lp[kind - 2] - rp[kind - 2]) / 2;
        this.cmy = (lp[kind - 1] - rp[kind - 1]) / 2;
        this.cdx = dxf;
        this.cdy = dyf;
        this.cx0 = xf;
        this.cy0 = yf;
        this.prev = DRAWING_OP_TO;
    }
    @Override public void quadTo(float x1, float y1, float x2, float y2) {
        middle[0] = cx0; middle[1] = cy0;
        middle[2] = x1;  middle[3] = y1;
        middle[4] = x2;  middle[5] = y2;
        final float xf = middle[4], yf = middle[5];
        float dxs = middle[2] - middle[0];
        float dys = middle[3] - middle[1];
        float dxf = middle[4] - middle[2];
        float dyf = middle[5] - middle[3];
        if ((dxs == 0f && dys == 0f) || (dxf == 0f && dyf == 0f)) {
            dxs = dxf = middle[4] - middle[0];
            dys = dyf = middle[5] - middle[1];
        }
        if (dxs == 0f && dys == 0f) {
            lineTo(middle[0], middle[1]);
            return;
        }
        if (Math.abs(dxs) < 0.1f && Math.abs(dys) < 0.1f) {
            float len = (float) sqrt(dxs*dxs + dys*dys);
            dxs /= len;
            dys /= len;
        }
        if (Math.abs(dxf) < 0.1f && Math.abs(dyf) < 0.1f) {
            float len = (float) sqrt(dxf*dxf + dyf*dyf);
            dxf /= len;
            dyf /= len;
        }
        computeOffset(dxs, dys, lineWidth2, offset[0]);
        final float mx = offset[0][0];
        final float my = offset[0][1];
        drawJoin(cdx, cdy, cx0, cy0, dxs, dys, cmx, cmy, mx, my);
        int nSplits = findSubdivPoints(middle, subdivTs, 6, lineWidth2);
        int kind = 0;
        Iterator<Integer> it = Curve.breakPtsAtTs(middle, 6, subdivTs, nSplits);
        while(it.hasNext()) {
            int curCurveOff = it.next();
            kind = computeOffsetQuad(middle, curCurveOff, lp, rp);
            emitLineTo(lp[0], lp[1]);
            switch(kind) {
            case 6:
                emitQuadTo(lp[0], lp[1], lp[2], lp[3], lp[4], lp[5], false);
                emitQuadTo(rp[0], rp[1], rp[2], rp[3], rp[4], rp[5], true);
                break;
            case 4:
                emitLineTo(lp[2], lp[3]);
                emitLineTo(rp[0], rp[1], true);
                break;
            }
            emitLineTo(rp[kind - 2], rp[kind - 1], true);
        }
        this.cmx = (lp[kind - 2] - rp[kind - 2]) / 2;
        this.cmy = (lp[kind - 1] - rp[kind - 1]) / 2;
        this.cdx = dxf;
        this.cdy = dyf;
        this.cx0 = xf;
        this.cy0 = yf;
        this.prev = DRAWING_OP_TO;
    }
    @Override public long getNativeConsumer() {
        throw new InternalError("Stroker doesn't use a native consumer");
    }
    private static final class PolyStack {
        float[] curves;
        int end;
        int[] curveTypes;
        int numCurves;
        private static final int INIT_SIZE = 50;
        PolyStack() {
            curves = new float[8 * INIT_SIZE];
            curveTypes = new int[INIT_SIZE];
            end = 0;
            numCurves = 0;
        }
        public boolean isEmpty() {
            return numCurves == 0;
        }
        private void ensureSpace(int n) {
            if (end + n >= curves.length) {
                int newSize = (end + n) * 2;
                curves = Arrays.copyOf(curves, newSize);
            }
            if (numCurves >= curveTypes.length) {
                int newSize = numCurves * 2;
                curveTypes = Arrays.copyOf(curveTypes, newSize);
            }
        }
        public void pushCubic(float x0, float y0,
                              float x1, float y1,
                              float x2, float y2)
        {
            ensureSpace(6);
            curveTypes[numCurves++] = 8;
            curves[end++] = x2;    curves[end++] = y2;
            curves[end++] = x1;    curves[end++] = y1;
            curves[end++] = x0;    curves[end++] = y0;
        }
        public void pushQuad(float x0, float y0,
                             float x1, float y1)
        {
            ensureSpace(4);
            curveTypes[numCurves++] = 6;
            curves[end++] = x1;    curves[end++] = y1;
            curves[end++] = x0;    curves[end++] = y0;
        }
        public void pushLine(float x, float y) {
            ensureSpace(2);
            curveTypes[numCurves++] = 4;
            curves[end++] = x;    curves[end++] = y;
        }
        @SuppressWarnings("unused")
        public int pop(float[] pts) {
            int ret = curveTypes[numCurves - 1];
            numCurves--;
            end -= (ret - 2);
            System.arraycopy(curves, end, pts, 0, ret - 2);
            return ret;
        }
        public void pop(PathConsumer2D io) {
            numCurves--;
            int type = curveTypes[numCurves];
            end -= (type - 2);
            switch(type) {
            case 8:
                io.curveTo(curves[end+0], curves[end+1],
                           curves[end+2], curves[end+3],
                           curves[end+4], curves[end+5]);
                break;
            case 6:
                io.quadTo(curves[end+0], curves[end+1],
                           curves[end+2], curves[end+3]);
                 break;
            case 4:
                io.lineTo(curves[end], curves[end+1]);
            }
        }
        @Override
        public String toString() {
            String ret = "";
            int nc = numCurves;
            int end = this.end;
            while (nc > 0) {
                nc--;
                int type = curveTypes[numCurves];
                end -= (type - 2);
                switch(type) {
                case 8:
                    ret += "cubic: ";
                    break;
                case 6:
                    ret += "quad: ";
                    break;
                case 4:
                    ret += "line: ";
                    break;
                }
                ret += Arrays.toString(Arrays.copyOfRange(curves, end, end+type-2)) + "\n";
            }
            return ret;
        }
    }
}
