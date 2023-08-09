public class XRRenderer implements PixelDrawPipe, PixelFillPipe, ShapeDrawPipe {
    XRDrawHandler drawHandler;
    MaskTileManager tileManager;
    public XRRenderer(MaskTileManager tileManager) {
        this.tileManager = tileManager;
        this.drawHandler = new XRDrawHandler();
    }
    private final void validateSurface(SunGraphics2D sg2d) {
        XRSurfaceData xrsd = (XRSurfaceData) sg2d.surfaceData;
        xrsd.validateAsDestination(sg2d, sg2d.getCompClip());
        xrsd.maskBuffer.validateCompositeState(sg2d.composite, sg2d.transform,
                                               sg2d.paint, sg2d);
    }
    public void drawLine(SunGraphics2D sg2d, int x1, int y1, int x2, int y2) {
        try {
            SunToolkit.awtLock();
            validateSurface(sg2d);
            int transx = sg2d.transX;
            int transy = sg2d.transY;
            XRSurfaceData xrsd = (XRSurfaceData) sg2d.surfaceData;
            tileManager.addLine(x1 + transx, y1 + transy,
                                x2 + transx, y2 + transy);
            tileManager.fillMask(xrsd);
        } finally {
            SunToolkit.awtUnlock();
        }
    }
    public void drawRect(SunGraphics2D sg2d,
                         int x, int y, int width, int height) {
        draw(sg2d, new Rectangle2D.Float(x, y, width, height));
    }
    public void drawPolyline(SunGraphics2D sg2d,
                             int xpoints[], int ypoints[], int npoints) {
        Path2D.Float p2d = new Path2D.Float();
        if (npoints > 1) {
            p2d.moveTo(xpoints[0], ypoints[0]);
            for (int i = 1; i < npoints; i++) {
                p2d.lineTo(xpoints[i], ypoints[i]);
            }
        }
        draw(sg2d, p2d);
    }
    public void drawPolygon(SunGraphics2D sg2d,
                            int xpoints[], int ypoints[], int npoints) {
        draw(sg2d, new Polygon(xpoints, ypoints, npoints));
    }
    public synchronized void fillRect(SunGraphics2D sg2d,
                                      int x, int y, int width, int height) {
        SunToolkit.awtLock();
        try {
            validateSurface(sg2d);
            XRSurfaceData xrsd = (XRSurfaceData) sg2d.surfaceData;
            x += sg2d.transform.getTranslateX();
            y += sg2d.transform.getTranslateY();
            tileManager.addRect(x, y, width, height);
            tileManager.fillMask(xrsd);
        } finally {
            SunToolkit.awtUnlock();
        }
    }
    public void fillPolygon(SunGraphics2D sg2d,
                            int xpoints[], int ypoints[], int npoints) {
        fill(sg2d, new Polygon(xpoints, ypoints, npoints));
    }
    public void drawRoundRect(SunGraphics2D sg2d,
                              int x, int y, int width, int height,
                              int arcWidth, int arcHeight) {
        draw(sg2d, new RoundRectangle2D.Float(x, y, width, height,
                                              arcWidth, arcHeight));
    }
    public void fillRoundRect(SunGraphics2D sg2d, int x, int y,
                              int width, int height,
                              int arcWidth, int arcHeight) {
        fill(sg2d, new RoundRectangle2D.Float(x, y, width, height,
                                              arcWidth, arcHeight));
    }
    public void drawOval(SunGraphics2D sg2d,
                         int x, int y, int width, int height) {
        draw(sg2d, new Ellipse2D.Float(x, y, width, height));
    }
    public void fillOval(SunGraphics2D sg2d,
                         int x, int y, int width, int height) {
        fill(sg2d, new Ellipse2D.Float(x, y, width, height));
    }
    public void drawArc(SunGraphics2D sg2d,
                       int x, int y, int width, int height,
                        int startAngle, int arcAngle) {
        draw(sg2d, new Arc2D.Float(x, y, width, height,
                                   startAngle, arcAngle, Arc2D.OPEN));
    }
    public void fillArc(SunGraphics2D sg2d,
                         int x, int y, int width, int height,
                         int startAngle, int arcAngle) {
        fill(sg2d, new Arc2D.Float(x, y, width, height,
             startAngle, arcAngle, Arc2D.PIE));
    }
    private class XRDrawHandler extends ProcessPath.DrawHandler {
        XRDrawHandler() {
            super(0, 0, 0, 0);
        }
        void validate(SunGraphics2D sg2d) {
            Region clip = sg2d.getCompClip();
            setBounds(clip.getLoX(), clip.getLoY(),
                      clip.getHiX(), clip.getHiY(), sg2d.strokeHint);
            validateSurface(sg2d);
        }
        public void drawLine(int x1, int y1, int x2, int y2) {
            tileManager.addLine(x1, y1, x2, y2);
        }
        public void drawPixel(int x, int y) {
            tileManager.addRect(x, y, 1, 1);
        }
        public void drawScanline(int x1, int x2, int y) {
            tileManager.addRect(x1, y, x2 - x1 + 1, 1);
        }
    }
    protected void drawPath(SunGraphics2D sg2d, Path2D.Float p2df,
                            int transx, int transy) {
        SunToolkit.awtLock();
        try {
            validateSurface(sg2d);
            drawHandler.validate(sg2d);
            ProcessPath.drawPath(drawHandler, p2df, transx, transy);
            tileManager.fillMask(((XRSurfaceData) sg2d.surfaceData));
        } finally {
            SunToolkit.awtUnlock();
        }
    }
    protected void fillPath(SunGraphics2D sg2d, Path2D.Float p2df,
                            int transx, int transy) {
        SunToolkit.awtLock();
        try {
            validateSurface(sg2d);
            drawHandler.validate(sg2d);
            ProcessPath.fillPath(drawHandler, p2df, transx, transy);
            tileManager.fillMask(((XRSurfaceData) sg2d.surfaceData));
        } finally {
            SunToolkit.awtUnlock();
        }
    }
    protected void fillSpans(SunGraphics2D sg2d, SpanIterator si,
                             int transx, int transy) {
        SunToolkit.awtLock();
        try {
            validateSurface(sg2d);
            int[] spanBox = new int[4];
            while (si.nextSpan(spanBox)) {
                tileManager.addRect(spanBox[0] + transx,
                                    spanBox[1] + transy,
                                    spanBox[2] - spanBox[0],
                                    spanBox[3] - spanBox[1]);
            }
            tileManager.fillMask(((XRSurfaceData) sg2d.surfaceData));
        } finally {
            SunToolkit.awtUnlock();
        }
    }
    public void draw(SunGraphics2D sg2d, Shape s) {
        if (sg2d.strokeState == SunGraphics2D.STROKE_THIN) {
            Path2D.Float p2df;
            int transx, transy;
            if (sg2d.transformState <= SunGraphics2D.TRANSFORM_INT_TRANSLATE) {
                if (s instanceof Path2D.Float) {
                    p2df = (Path2D.Float) s;
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
        } else if (sg2d.strokeState < SunGraphics2D.STROKE_CUSTOM) {
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
        if (sg2d.strokeState == SunGraphics2D.STROKE_THIN) {
            Path2D.Float p2df;
            if (sg2d.transformState <= SunGraphics2D.TRANSFORM_INT_TRANSLATE) {
                if (s instanceof Path2D.Float) {
                    p2df = (Path2D.Float) s;
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
        if (sg2d.transformState <= SunGraphics2D.TRANSFORM_INT_TRANSLATE) {
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
