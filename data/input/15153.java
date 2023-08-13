public abstract class BufferedRenderPipe
    implements PixelDrawPipe, PixelFillPipe, ShapeDrawPipe, ParallelogramPipe
{
    ParallelogramPipe aapgrampipe = new AAParallelogramPipe();
    static final int BYTES_PER_POLY_POINT = 8;
    static final int BYTES_PER_SCANLINE = 12;
    static final int BYTES_PER_SPAN = 16;
    protected RenderQueue rq;
    protected RenderBuffer buf;
    private BufferedDrawHandler drawHandler;
    public BufferedRenderPipe(RenderQueue rq) {
        this.rq = rq;
        this.buf = rq.getBuffer();
        this.drawHandler = new BufferedDrawHandler();
    }
    public ParallelogramPipe getAAParallelogramPipe() {
        return aapgrampipe;
    }
    protected abstract void validateContext(SunGraphics2D sg2d);
    protected abstract void validateContextAA(SunGraphics2D sg2d);
    public void drawLine(SunGraphics2D sg2d,
                         int x1, int y1, int x2, int y2)
    {
        int transx = sg2d.transX;
        int transy = sg2d.transY;
        rq.lock();
        try {
            validateContext(sg2d);
            rq.ensureCapacity(20);
            buf.putInt(DRAW_LINE);
            buf.putInt(x1 + transx);
            buf.putInt(y1 + transy);
            buf.putInt(x2 + transx);
            buf.putInt(y2 + transy);
        } finally {
            rq.unlock();
        }
    }
    public void drawRect(SunGraphics2D sg2d,
                         int x, int y, int width, int height)
    {
        rq.lock();
        try {
            validateContext(sg2d);
            rq.ensureCapacity(20);
            buf.putInt(DRAW_RECT);
            buf.putInt(x + sg2d.transX);
            buf.putInt(y + sg2d.transY);
            buf.putInt(width);
            buf.putInt(height);
        } finally {
            rq.unlock();
        }
    }
    public void fillRect(SunGraphics2D sg2d,
                         int x, int y, int width, int height)
    {
        rq.lock();
        try {
            validateContext(sg2d);
            rq.ensureCapacity(20);
            buf.putInt(FILL_RECT);
            buf.putInt(x + sg2d.transX);
            buf.putInt(y + sg2d.transY);
            buf.putInt(width);
            buf.putInt(height);
        } finally {
            rq.unlock();
        }
    }
    public void drawRoundRect(SunGraphics2D sg2d,
                              int x, int y, int width, int height,
                              int arcWidth, int arcHeight)
    {
        draw(sg2d, new RoundRectangle2D.Float(x, y, width, height,
                                              arcWidth, arcHeight));
    }
    public void fillRoundRect(SunGraphics2D sg2d,
                              int x, int y, int width, int height,
                              int arcWidth, int arcHeight)
    {
        fill(sg2d, new RoundRectangle2D.Float(x, y, width, height,
                                              arcWidth, arcHeight));
    }
    public void drawOval(SunGraphics2D sg2d,
                         int x, int y, int width, int height)
    {
        draw(sg2d, new Ellipse2D.Float(x, y, width, height));
    }
    public void fillOval(SunGraphics2D sg2d,
                         int x, int y, int width, int height)
    {
        fill(sg2d, new Ellipse2D.Float(x, y, width, height));
    }
    public void drawArc(SunGraphics2D sg2d,
                        int x, int y, int width, int height,
                        int startAngle, int arcAngle)
    {
        draw(sg2d, new Arc2D.Float(x, y, width, height,
                                   startAngle, arcAngle,
                                   Arc2D.OPEN));
    }
    public void fillArc(SunGraphics2D sg2d,
                        int x, int y, int width, int height,
                        int startAngle, int arcAngle)
    {
        fill(sg2d, new Arc2D.Float(x, y, width, height,
                                   startAngle, arcAngle,
                                   Arc2D.PIE));
    }
    protected void drawPoly(final SunGraphics2D sg2d,
                            final int[] xPoints, final int[] yPoints,
                            final int nPoints, final boolean isClosed)
    {
        if (xPoints == null || yPoints == null) {
            throw new NullPointerException("coordinate array");
        }
        if (xPoints.length < nPoints || yPoints.length < nPoints) {
            throw new ArrayIndexOutOfBoundsException("coordinate array");
        }
        if (nPoints < 2) {
            return;
        } else if (nPoints == 2 && !isClosed) {
            drawLine(sg2d, xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
            return;
        }
        rq.lock();
        try {
            validateContext(sg2d);
            int pointBytesRequired = nPoints * BYTES_PER_POLY_POINT;
            int totalBytesRequired = 20 + pointBytesRequired;
            if (totalBytesRequired <= buf.capacity()) {
                if (totalBytesRequired > buf.remaining()) {
                    rq.flushNow();
                }
                buf.putInt(DRAW_POLY);
                buf.putInt(nPoints);
                buf.putInt(isClosed ? 1 : 0);
                buf.putInt(sg2d.transX);
                buf.putInt(sg2d.transY);
                buf.put(xPoints, 0, nPoints);
                buf.put(yPoints, 0, nPoints);
            } else {
                rq.flushAndInvokeNow(new Runnable() {
                    public void run() {
                        drawPoly(xPoints, yPoints,
                                 nPoints, isClosed,
                                 sg2d.transX, sg2d.transY);
                    }
                });
            }
        } finally {
            rq.unlock();
        }
    }
    protected abstract void drawPoly(int[] xPoints, int[] yPoints,
                                     int nPoints, boolean isClosed,
                                     int transX, int transY);
    public void drawPolyline(SunGraphics2D sg2d,
                             int[] xPoints, int[] yPoints,
                             int nPoints)
    {
        drawPoly(sg2d, xPoints, yPoints, nPoints, false);
    }
    public void drawPolygon(SunGraphics2D sg2d,
                            int[] xPoints, int[] yPoints,
                            int nPoints)
    {
        drawPoly(sg2d, xPoints, yPoints, nPoints, true);
    }
    public void fillPolygon(SunGraphics2D sg2d,
                            int[] xPoints, int[] yPoints,
                            int nPoints)
    {
        fill(sg2d, new Polygon(xPoints, yPoints, nPoints));
    }
    private class BufferedDrawHandler
        extends ProcessPath.DrawHandler
    {
        BufferedDrawHandler() {
            super(0, 0, 0, 0);
        }
        void validate(SunGraphics2D sg2d) {
            Region clip = sg2d.getCompClip();
            setBounds(clip.getLoX(), clip.getLoY(),
                      clip.getHiX(), clip.getHiY(),
                      sg2d.strokeHint);
        }
        public void drawLine(int x1, int y1, int x2, int y2) {
            rq.ensureCapacity(20);
            buf.putInt(DRAW_LINE);
            buf.putInt(x1);
            buf.putInt(y1);
            buf.putInt(x2);
            buf.putInt(y2);
        }
        public void drawPixel(int x, int y) {
            rq.ensureCapacity(12);
            buf.putInt(DRAW_PIXEL);
            buf.putInt(x);
            buf.putInt(y);
        }
        private int scanlineCount;
        private int scanlineCountIndex;
        private int remainingScanlines;
        private void resetFillPath() {
            buf.putInt(DRAW_SCANLINES);
            scanlineCountIndex = buf.position();
            buf.putInt(0);
            scanlineCount = 0;
            remainingScanlines = buf.remaining() / BYTES_PER_SCANLINE;
        }
        private void updateScanlineCount() {
            buf.putInt(scanlineCountIndex, scanlineCount);
        }
        public void startFillPath() {
            rq.ensureCapacity(20); 
            resetFillPath();
        }
        public void drawScanline(int x1, int x2, int y) {
            if (remainingScanlines == 0) {
                updateScanlineCount();
                rq.flushNow();
                resetFillPath();
            }
            buf.putInt(x1);
            buf.putInt(x2);
            buf.putInt(y);
            scanlineCount++;
            remainingScanlines--;
        }
        public void endFillPath() {
            updateScanlineCount();
        }
    }
    protected void drawPath(SunGraphics2D sg2d,
                            Path2D.Float p2df, int transx, int transy)
    {
        rq.lock();
        try {
            validateContext(sg2d);
            drawHandler.validate(sg2d);
            ProcessPath.drawPath(drawHandler, p2df, transx, transy);
        } finally {
            rq.unlock();
        }
    }
    protected void fillPath(SunGraphics2D sg2d,
                            Path2D.Float p2df, int transx, int transy)
    {
        rq.lock();
        try {
            validateContext(sg2d);
            drawHandler.validate(sg2d);
            drawHandler.startFillPath();
            ProcessPath.fillPath(drawHandler, p2df, transx, transy);
            drawHandler.endFillPath();
        } finally {
            rq.unlock();
        }
    }
    private native int fillSpans(RenderQueue rq, long buf,
                                 int pos, int limit,
                                 SpanIterator si, long iterator,
                                 int transx, int transy);
    protected void fillSpans(SunGraphics2D sg2d, SpanIterator si,
                             int transx, int transy)
    {
        rq.lock();
        try {
            validateContext(sg2d);
            rq.ensureCapacity(24); 
            int newpos = fillSpans(rq, buf.getAddress(),
                                   buf.position(), buf.capacity(),
                                   si, si.getNativeIterator(),
                                   transx, transy);
            buf.position(newpos);
        } finally {
            rq.unlock();
        }
    }
    public void fillParallelogram(SunGraphics2D sg2d,
                                  double ux1, double uy1,
                                  double ux2, double uy2,
                                  double x, double y,
                                  double dx1, double dy1,
                                  double dx2, double dy2)
    {
        rq.lock();
        try {
            validateContext(sg2d);
            rq.ensureCapacity(28);
            buf.putInt(FILL_PARALLELOGRAM);
            buf.putFloat((float) x);
            buf.putFloat((float) y);
            buf.putFloat((float) dx1);
            buf.putFloat((float) dy1);
            buf.putFloat((float) dx2);
            buf.putFloat((float) dy2);
        } finally {
            rq.unlock();
        }
    }
    public void drawParallelogram(SunGraphics2D sg2d,
                                  double ux1, double uy1,
                                  double ux2, double uy2,
                                  double x, double y,
                                  double dx1, double dy1,
                                  double dx2, double dy2,
                                  double lw1, double lw2)
    {
        rq.lock();
        try {
            validateContext(sg2d);
            rq.ensureCapacity(36);
            buf.putInt(DRAW_PARALLELOGRAM);
            buf.putFloat((float) x);
            buf.putFloat((float) y);
            buf.putFloat((float) dx1);
            buf.putFloat((float) dy1);
            buf.putFloat((float) dx2);
            buf.putFloat((float) dy2);
            buf.putFloat((float) lw1);
            buf.putFloat((float) lw2);
        } finally {
            rq.unlock();
        }
    }
    private class AAParallelogramPipe implements ParallelogramPipe {
        public void fillParallelogram(SunGraphics2D sg2d,
                                      double ux1, double uy1,
                                      double ux2, double uy2,
                                      double x, double y,
                                      double dx1, double dy1,
                                      double dx2, double dy2)
        {
            rq.lock();
            try {
                validateContextAA(sg2d);
                rq.ensureCapacity(28);
                buf.putInt(FILL_AAPARALLELOGRAM);
                buf.putFloat((float) x);
                buf.putFloat((float) y);
                buf.putFloat((float) dx1);
                buf.putFloat((float) dy1);
                buf.putFloat((float) dx2);
                buf.putFloat((float) dy2);
            } finally {
                rq.unlock();
            }
        }
        public void drawParallelogram(SunGraphics2D sg2d,
                                      double ux1, double uy1,
                                      double ux2, double uy2,
                                      double x, double y,
                                      double dx1, double dy1,
                                      double dx2, double dy2,
                                      double lw1, double lw2)
        {
            rq.lock();
            try {
                validateContextAA(sg2d);
                rq.ensureCapacity(36);
                buf.putInt(DRAW_AAPARALLELOGRAM);
                buf.putFloat((float) x);
                buf.putFloat((float) y);
                buf.putFloat((float) dx1);
                buf.putFloat((float) dy1);
                buf.putFloat((float) dx2);
                buf.putFloat((float) dy2);
                buf.putFloat((float) lw1);
                buf.putFloat((float) lw2);
            } finally {
                rq.unlock();
            }
        }
    }
    public void draw(SunGraphics2D sg2d, Shape s) {
        if (sg2d.strokeState == sg2d.STROKE_THIN) {
            if (s instanceof Polygon) {
                if (sg2d.transformState < sg2d.TRANSFORM_TRANSLATESCALE) {
                    Polygon p = (Polygon)s;
                    drawPolygon(sg2d, p.xpoints, p.ypoints, p.npoints);
                    return;
                }
            }
            Path2D.Float p2df;
            int transx, transy;
            if (sg2d.transformState <= sg2d.TRANSFORM_INT_TRANSLATE) {
                if (s instanceof Path2D.Float) {
                    p2df = (Path2D.Float)s;
                } else {
                    p2df = new Path2D.Float(s);
                }
                transx = sg2d.transX;
                transy = sg2d.transY;
            } else {
                p2df = new Path2D.Float(s, sg2d.transform);
                transx = 0;
                transy = 0;
            }
            drawPath(sg2d, p2df, transx, transy);
        } else if (sg2d.strokeState < sg2d.STROKE_CUSTOM) {
            ShapeSpanIterator si = LoopPipe.getStrokeSpans(sg2d, s);
            try {
                fillSpans(sg2d, si, 0, 0);
            } finally {
                si.dispose();
            }
        } else {
            fill(sg2d, sg2d.stroke.createStrokedShape(s));
        }
    }
    public void fill(SunGraphics2D sg2d, Shape s) {
        int transx, transy;
        if (sg2d.strokeState == sg2d.STROKE_THIN) {
            Path2D.Float p2df;
            if (sg2d.transformState <= sg2d.TRANSFORM_INT_TRANSLATE) {
                if (s instanceof Path2D.Float) {
                    p2df = (Path2D.Float)s;
                } else {
                    p2df = new Path2D.Float(s);
                }
                transx = sg2d.transX;
                transy = sg2d.transY;
            } else {
                p2df = new Path2D.Float(s, sg2d.transform);
                transx = 0;
                transy = 0;
            }
            fillPath(sg2d, p2df, transx, transy);
            return;
        }
        AffineTransform at;
        if (sg2d.transformState <= sg2d.TRANSFORM_INT_TRANSLATE) {
            at = null;
            transx = sg2d.transX;
            transy = sg2d.transY;
        } else {
            at = sg2d.transform;
            transx = transy = 0;
        }
        ShapeSpanIterator ssi = LoopPipe.getFillSSI(sg2d);
        try {
            Region clip = sg2d.getCompClip();
            ssi.setOutputAreaXYXY(clip.getLoX() - transx,
                                  clip.getLoY() - transy,
                                  clip.getHiX() - transx,
                                  clip.getHiY() - transy);
            ssi.appendPath(s.getPathIterator(at));
            fillSpans(sg2d, ssi, transx, transy);
        } finally {
            ssi.dispose();
        }
    }
}
