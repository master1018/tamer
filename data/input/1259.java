final class Dasher implements sun.awt.geom.PathConsumer2D {
    private final PathConsumer2D out;
    private final float[] dash;
    private final float startPhase;
    private final boolean startDashOn;
    private final int startIdx;
    private boolean starting;
    private boolean needsMoveTo;
    private int idx;
    private boolean dashOn;
    private float phase;
    private float sx, sy;
    private float x0, y0;
    private float[] curCurvepts;
    public Dasher(PathConsumer2D out, float[] dash, float phase) {
        if (phase < 0) {
            throw new IllegalArgumentException("phase < 0 !");
        }
        this.out = out;
        int idx = 0;
        dashOn = true;
        float d;
        while (phase >= (d = dash[idx])) {
            phase -= d;
            idx = (idx + 1) % dash.length;
            dashOn = !dashOn;
        }
        this.dash = dash;
        this.startPhase = this.phase = phase;
        this.startDashOn = dashOn;
        this.startIdx = idx;
        this.starting = true;
        curCurvepts = new float[8 * 2];
    }
    public void moveTo(float x0, float y0) {
        if (firstSegidx > 0) {
            out.moveTo(sx, sy);
            emitFirstSegments();
        }
        needsMoveTo = true;
        this.idx = startIdx;
        this.dashOn = this.startDashOn;
        this.phase = this.startPhase;
        this.sx = this.x0 = x0;
        this.sy = this.y0 = y0;
        this.starting = true;
    }
    private void emitSeg(float[] buf, int off, int type) {
        switch (type) {
        case 8:
            out.curveTo(buf[off+0], buf[off+1],
                        buf[off+2], buf[off+3],
                        buf[off+4], buf[off+5]);
            break;
        case 6:
            out.quadTo(buf[off+0], buf[off+1],
                       buf[off+2], buf[off+3]);
            break;
        case 4:
            out.lineTo(buf[off], buf[off+1]);
        }
    }
    private void emitFirstSegments() {
        for (int i = 0; i < firstSegidx; ) {
            emitSeg(firstSegmentsBuffer, i+1, (int)firstSegmentsBuffer[i]);
            i += (((int)firstSegmentsBuffer[i]) - 1);
        }
        firstSegidx = 0;
    }
    private float[] firstSegmentsBuffer = new float[7];
    private int firstSegidx = 0;
    private void goTo(float[] pts, int off, final int type) {
        float x = pts[off + type - 4];
        float y = pts[off + type - 3];
        if (dashOn) {
            if (starting) {
                firstSegmentsBuffer = Helpers.widenArray(firstSegmentsBuffer,
                                      firstSegidx, type - 2);
                firstSegmentsBuffer[firstSegidx++] = type;
                System.arraycopy(pts, off, firstSegmentsBuffer, firstSegidx, type - 2);
                firstSegidx += type - 2;
            } else {
                if (needsMoveTo) {
                    out.moveTo(x0, y0);
                    needsMoveTo = false;
                }
                emitSeg(pts, off, type);
            }
        } else {
            starting = false;
            needsMoveTo = true;
        }
        this.x0 = x;
        this.y0 = y;
    }
    public void lineTo(float x1, float y1) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float len = (float) Math.sqrt(dx*dx + dy*dy);
        if (len == 0) {
            return;
        }
        float cx = dx / len;
        float cy = dy / len;
        while (true) {
            float leftInThisDashSegment = dash[idx] - phase;
            if (len <= leftInThisDashSegment) {
                curCurvepts[0] = x1;
                curCurvepts[1] = y1;
                goTo(curCurvepts, 0, 4);
                phase += len;
                if (len == leftInThisDashSegment) {
                    phase = 0f;
                    idx = (idx + 1) % dash.length;
                    dashOn = !dashOn;
                }
                return;
            }
            float dashdx = dash[idx] * cx;
            float dashdy = dash[idx] * cy;
            if (phase == 0) {
                curCurvepts[0] = x0 + dashdx;
                curCurvepts[1] = y0 + dashdy;
            } else {
                float p = leftInThisDashSegment / dash[idx];
                curCurvepts[0] = x0 + p * dashdx;
                curCurvepts[1] = y0 + p * dashdy;
            }
            goTo(curCurvepts, 0, 4);
            len -= leftInThisDashSegment;
            idx = (idx + 1) % dash.length;
            dashOn = !dashOn;
            phase = 0;
        }
    }
    private LengthIterator li = null;
    private void somethingTo(int type) {
        if (pointCurve(curCurvepts, type)) {
            return;
        }
        if (li == null) {
            li = new LengthIterator(4, 0.01f);
        }
        li.initializeIterationOnCurve(curCurvepts, type);
        int curCurveoff = 0; 
        float lastSplitT = 0;
        float t = 0;
        float leftInThisDashSegment = dash[idx] - phase;
        while ((t = li.next(leftInThisDashSegment)) < 1) {
            if (t != 0) {
                Helpers.subdivideAt((t - lastSplitT) / (1 - lastSplitT),
                                    curCurvepts, curCurveoff,
                                    curCurvepts, 0,
                                    curCurvepts, type, type);
                lastSplitT = t;
                goTo(curCurvepts, 2, type);
                curCurveoff = type;
            }
            idx = (idx + 1) % dash.length;
            dashOn = !dashOn;
            phase = 0;
            leftInThisDashSegment = dash[idx];
        }
        goTo(curCurvepts, curCurveoff+2, type);
        phase += li.lastSegLen();
        if (phase >= dash[idx]) {
            phase = 0f;
            idx = (idx + 1) % dash.length;
            dashOn = !dashOn;
        }
    }
    private static boolean pointCurve(float[] curve, int type) {
        for (int i = 2; i < type; i++) {
            if (curve[i] != curve[i-2]) {
                return false;
            }
        }
        return true;
    }
    private static class LengthIterator {
        private enum Side {LEFT, RIGHT};
        private float[][] recCurveStack;
        private Side[] sides;
        private int curveType;
        private final int limit;
        private final float ERR;
        private final float minTincrement;
        private float nextT;
        private float lenAtNextT;
        private float lastT;
        private float lenAtLastT;
        private float lenAtLastSplit;
        private float lastSegLen;
        private int recLevel;
        private boolean done;
        private float[] curLeafCtrlPolyLengths = new float[3];
        public LengthIterator(int reclimit, float err) {
            this.limit = reclimit;
            this.minTincrement = 1f / (1 << limit);
            this.ERR = err;
            this.recCurveStack = new float[reclimit+1][8];
            this.sides = new Side[reclimit];
            this.nextT = Float.MAX_VALUE;
            this.lenAtNextT = Float.MAX_VALUE;
            this.lenAtLastSplit = Float.MIN_VALUE;
            this.recLevel = Integer.MIN_VALUE;
            this.lastSegLen = Float.MAX_VALUE;
            this.done = true;
        }
        public void initializeIterationOnCurve(float[] pts, int type) {
            System.arraycopy(pts, 0, recCurveStack[0], 0, type);
            this.curveType = type;
            this.recLevel = 0;
            this.lastT = 0;
            this.lenAtLastT = 0;
            this.nextT = 0;
            this.lenAtNextT = 0;
            goLeft(); 
            this.lenAtLastSplit = 0;
            if (recLevel > 0) {
                this.sides[0] = Side.LEFT;
                this.done = false;
            } else {
                this.sides[0] = Side.RIGHT;
                this.done = true;
            }
            this.lastSegLen = 0;
        }
        private int cachedHaveLowAcceleration = -1;
        private boolean haveLowAcceleration(float err) {
            if (cachedHaveLowAcceleration == -1) {
                final float len1 = curLeafCtrlPolyLengths[0];
                final float len2 = curLeafCtrlPolyLengths[1];
                if (!Helpers.within(len1, len2, err*len2)) {
                    cachedHaveLowAcceleration = 0;
                    return false;
                }
                if (curveType == 8) {
                    final float len3 = curLeafCtrlPolyLengths[2];
                    if (!(Helpers.within(len2, len3, err*len3) &&
                          Helpers.within(len1, len3, err*len3))) {
                        cachedHaveLowAcceleration = 0;
                        return false;
                    }
                }
                cachedHaveLowAcceleration = 1;
                return true;
            }
            return (cachedHaveLowAcceleration == 1);
        }
        private float[] nextRoots = new float[4];
        private float[] flatLeafCoefCache = new float[] {0, 0, -1, 0};
        public float next(final float len) {
            final float targetLength = lenAtLastSplit + len;
            while(lenAtNextT < targetLength) {
                if (done) {
                    lastSegLen = lenAtNextT - lenAtLastSplit;
                    return 1;
                }
                goToNextLeaf();
            }
            lenAtLastSplit = targetLength;
            final float leaflen = lenAtNextT - lenAtLastT;
            float t = (targetLength - lenAtLastT) / leaflen;
            if (!haveLowAcceleration(0.05f)) {
                if (flatLeafCoefCache[2] < 0) {
                    float x = 0+curLeafCtrlPolyLengths[0],
                          y = x+curLeafCtrlPolyLengths[1];
                    if (curveType == 8) {
                        float z = y + curLeafCtrlPolyLengths[2];
                        flatLeafCoefCache[0] = 3*(x - y) + z;
                        flatLeafCoefCache[1] = 3*(y - 2*x);
                        flatLeafCoefCache[2] = 3*x;
                        flatLeafCoefCache[3] = -z;
                    } else if (curveType == 6) {
                        flatLeafCoefCache[0] = 0f;
                        flatLeafCoefCache[1] = y - 2*x;
                        flatLeafCoefCache[2] = 2*x;
                        flatLeafCoefCache[3] = -y;
                    }
                }
                float a = flatLeafCoefCache[0];
                float b = flatLeafCoefCache[1];
                float c = flatLeafCoefCache[2];
                float d = t*flatLeafCoefCache[3];
                int n = Helpers.cubicRootsInAB(a, b, c, d, nextRoots, 0, 0, 1);
                if (n == 1 && !Float.isNaN(nextRoots[0])) {
                    t = nextRoots[0];
                }
            }
            t = t * (nextT - lastT) + lastT;
            if (t >= 1) {
                t = 1;
                done = true;
            }
            lastSegLen = len;
            return t;
        }
        public float lastSegLen() {
            return lastSegLen;
        }
        private void goToNextLeaf() {
            recLevel--;
            while(sides[recLevel] == Side.RIGHT) {
                if (recLevel == 0) {
                    done = true;
                    return;
                }
                recLevel--;
            }
            sides[recLevel] = Side.RIGHT;
            System.arraycopy(recCurveStack[recLevel], 0, recCurveStack[recLevel+1], 0, curveType);
            recLevel++;
            goLeft();
        }
        private void goLeft() {
            float len = onLeaf();
            if (len >= 0) {
                lastT = nextT;
                lenAtLastT = lenAtNextT;
                nextT += (1 << (limit - recLevel)) * minTincrement;
                lenAtNextT += len;
                flatLeafCoefCache[2] = -1;
                cachedHaveLowAcceleration = -1;
            } else {
                Helpers.subdivide(recCurveStack[recLevel], 0,
                                  recCurveStack[recLevel+1], 0,
                                  recCurveStack[recLevel], 0, curveType);
                sides[recLevel] = Side.LEFT;
                recLevel++;
                goLeft();
            }
        }
        private float onLeaf() {
            float[] curve = recCurveStack[recLevel];
            float polyLen = 0;
            float x0 = curve[0], y0 = curve[1];
            for (int i = 2; i < curveType; i += 2) {
                final float x1 = curve[i], y1 = curve[i+1];
                final float len = Helpers.linelen(x0, y0, x1, y1);
                polyLen += len;
                curLeafCtrlPolyLengths[i/2 - 1] = len;
                x0 = x1;
                y0 = y1;
            }
            final float lineLen = Helpers.linelen(curve[0], curve[1], curve[curveType-2], curve[curveType-1]);
            if (polyLen - lineLen < ERR || recLevel == limit) {
                return (polyLen + lineLen)/2;
            }
            return -1;
        }
    }
    @Override
    public void curveTo(float x1, float y1,
                        float x2, float y2,
                        float x3, float y3)
    {
        curCurvepts[0] = x0;        curCurvepts[1] = y0;
        curCurvepts[2] = x1;        curCurvepts[3] = y1;
        curCurvepts[4] = x2;        curCurvepts[5] = y2;
        curCurvepts[6] = x3;        curCurvepts[7] = y3;
        somethingTo(8);
    }
    @Override
    public void quadTo(float x1, float y1, float x2, float y2) {
        curCurvepts[0] = x0;        curCurvepts[1] = y0;
        curCurvepts[2] = x1;        curCurvepts[3] = y1;
        curCurvepts[4] = x2;        curCurvepts[5] = y2;
        somethingTo(6);
    }
    public void closePath() {
        lineTo(sx, sy);
        if (firstSegidx > 0) {
            if (!dashOn || needsMoveTo) {
                out.moveTo(sx, sy);
            }
            emitFirstSegments();
        }
        moveTo(sx, sy);
    }
    public void pathDone() {
        if (firstSegidx > 0) {
            out.moveTo(sx, sy);
            emitFirstSegments();
        }
        out.pathDone();
    }
    @Override
    public long getNativeConsumer() {
        throw new InternalError("Dasher does not use a native consumer");
    }
}
