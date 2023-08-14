public class PiscesRenderingEngine extends RenderingEngine {
    private static enum NormMode {OFF, ON_NO_AA, ON_WITH_AA}
    public Shape createStrokedShape(Shape src,
                                    float width,
                                    int caps,
                                    int join,
                                    float miterlimit,
                                    float dashes[],
                                    float dashphase)
    {
        final Path2D p2d = new Path2D.Float();
        strokeTo(src,
                 null,
                 width,
                 NormMode.OFF,
                 caps,
                 join,
                 miterlimit,
                 dashes,
                 dashphase,
                 new PathConsumer2D() {
                     public void moveTo(float x0, float y0) {
                         p2d.moveTo(x0, y0);
                     }
                     public void lineTo(float x1, float y1) {
                         p2d.lineTo(x1, y1);
                     }
                     public void closePath() {
                         p2d.closePath();
                     }
                     public void pathDone() {}
                     public void curveTo(float x1, float y1,
                                         float x2, float y2,
                                         float x3, float y3) {
                         p2d.curveTo(x1, y1, x2, y2, x3, y3);
                     }
                     public void quadTo(float x1, float y1, float x2, float y2) {
                         p2d.quadTo(x1, y1, x2, y2);
                     }
                     public long getNativeConsumer() {
                         throw new InternalError("Not using a native peer");
                     }
                 });
        return p2d;
    }
    public void strokeTo(Shape src,
                         AffineTransform at,
                         BasicStroke bs,
                         boolean thin,
                         boolean normalize,
                         boolean antialias,
                         final PathConsumer2D consumer)
    {
        NormMode norm = (normalize) ?
                ((antialias) ? NormMode.ON_WITH_AA : NormMode.ON_NO_AA)
                : NormMode.OFF;
        strokeTo(src, at, bs, thin, norm, antialias, consumer);
    }
    void strokeTo(Shape src,
                  AffineTransform at,
                  BasicStroke bs,
                  boolean thin,
                  NormMode normalize,
                  boolean antialias,
                  PathConsumer2D pc2d)
    {
        float lw;
        if (thin) {
            if (antialias) {
                lw = userSpaceLineWidth(at, 0.5f);
            } else {
                lw = userSpaceLineWidth(at, 1.0f);
            }
        } else {
            lw = bs.getLineWidth();
        }
        strokeTo(src,
                 at,
                 lw,
                 normalize,
                 bs.getEndCap(),
                 bs.getLineJoin(),
                 bs.getMiterLimit(),
                 bs.getDashArray(),
                 bs.getDashPhase(),
                 pc2d);
    }
    private float userSpaceLineWidth(AffineTransform at, float lw) {
        double widthScale;
        if ((at.getType() & (AffineTransform.TYPE_GENERAL_TRANSFORM |
                            AffineTransform.TYPE_GENERAL_SCALE)) != 0) {
            widthScale = Math.sqrt(at.getDeterminant());
        } else {
            double A = at.getScaleX();       
            double C = at.getShearX();       
            double B = at.getShearY();       
            double D = at.getScaleY();       
            double EA = A*A + B*B;          
            double EB = 2*(A*C + B*D);      
            double EC = C*C + D*D;          
            double hypot = Math.sqrt(EB*EB + (EA-EC)*(EA-EC));
            double widthsquared = ((EA + EC + hypot)/2.0);
            widthScale = Math.sqrt(widthsquared);
        }
        return (float) (lw / widthScale);
    }
    void strokeTo(Shape src,
                  AffineTransform at,
                  float width,
                  NormMode normalize,
                  int caps,
                  int join,
                  float miterlimit,
                  float dashes[],
                  float dashphase,
                  PathConsumer2D pc2d)
    {
        AffineTransform strokerat = null;
        AffineTransform outat = null;
        PathIterator pi = null;
        if (at != null && !at.isIdentity()) {
            final double a = at.getScaleX();
            final double b = at.getShearX();
            final double c = at.getShearY();
            final double d = at.getScaleY();
            final double det = a * d - c * b;
            if (Math.abs(det) <= 2 * Float.MIN_VALUE) {
                pc2d.moveTo(0, 0);
                pc2d.pathDone();
                return;
            }
            if (nearZero(a*b + c*d, 2) && nearZero(a*a+c*c - (b*b+d*d), 2)) {
                double scale = Math.sqrt(a*a + c*c);
                if (dashes != null) {
                    dashes = java.util.Arrays.copyOf(dashes, dashes.length);
                    for (int i = 0; i < dashes.length; i++) {
                        dashes[i] = (float)(scale * dashes[i]);
                    }
                    dashphase = (float)(scale * dashphase);
                }
                width = (float)(scale * width);
                pi = src.getPathIterator(at);
                if (normalize != NormMode.OFF) {
                    pi = new NormalizingPathIterator(pi, normalize);
                }
            } else {
                if (normalize != NormMode.OFF) {
                    strokerat = at;
                    pi = src.getPathIterator(at);
                    pi = new NormalizingPathIterator(pi, normalize);
                } else {
                    outat = at;
                    pi = src.getPathIterator(null);
                }
            }
        } else {
            pi = src.getPathIterator(null);
            if (normalize != NormMode.OFF) {
                pi = new NormalizingPathIterator(pi, normalize);
            }
        }
        pc2d = TransformingPathConsumer2D.transformConsumer(pc2d, outat);
        pc2d = TransformingPathConsumer2D.deltaTransformConsumer(pc2d, strokerat);
        pc2d = new Stroker(pc2d, width, caps, join, miterlimit);
        if (dashes != null) {
            pc2d = new Dasher(pc2d, dashes, dashphase);
        }
        pc2d = TransformingPathConsumer2D.inverseDeltaTransformConsumer(pc2d, strokerat);
        pathTo(pi, pc2d);
    }
    private static boolean nearZero(double num, int nulps) {
        return Math.abs(num) < nulps * Math.ulp(num);
    }
    private static class NormalizingPathIterator implements PathIterator {
        private final PathIterator src;
        private float curx_adjust, cury_adjust;
        private float movx_adjust, movy_adjust;
        private final float lval, rval;
        NormalizingPathIterator(PathIterator src, NormMode mode) {
            this.src = src;
            switch (mode) {
            case ON_NO_AA:
                lval = rval = 0.25f;
                break;
            case ON_WITH_AA:
                lval = 0f;
                rval = 0.5f;
                break;
            case OFF:
                throw new InternalError("A NormalizingPathIterator should " +
                         "not be created if no normalization is being done");
            default:
                throw new InternalError("Unrecognized normalization mode");
            }
        }
        public int currentSegment(float[] coords) {
            int type = src.currentSegment(coords);
            int lastCoord;
            switch(type) {
            case PathIterator.SEG_CUBICTO:
                lastCoord = 4;
                break;
            case PathIterator.SEG_QUADTO:
                lastCoord = 2;
                break;
            case PathIterator.SEG_LINETO:
            case PathIterator.SEG_MOVETO:
                lastCoord = 0;
                break;
            case PathIterator.SEG_CLOSE:
                curx_adjust = movx_adjust;
                cury_adjust = movy_adjust;
                return type;
            default:
                throw new InternalError("Unrecognized curve type");
            }
            float x_adjust = (float)Math.floor(coords[lastCoord] + lval) +
                         rval - coords[lastCoord];
            float y_adjust = (float)Math.floor(coords[lastCoord+1] + lval) +
                         rval - coords[lastCoord + 1];
            coords[lastCoord    ] += x_adjust;
            coords[lastCoord + 1] += y_adjust;
            switch(type) {
            case PathIterator.SEG_CUBICTO:
                coords[0] += curx_adjust;
                coords[1] += cury_adjust;
                coords[2] += x_adjust;
                coords[3] += y_adjust;
                break;
            case PathIterator.SEG_QUADTO:
                coords[0] += (curx_adjust + x_adjust) / 2;
                coords[1] += (cury_adjust + y_adjust) / 2;
                break;
            case PathIterator.SEG_LINETO:
                break;
            case PathIterator.SEG_MOVETO:
                movx_adjust = x_adjust;
                movy_adjust = y_adjust;
                break;
            case PathIterator.SEG_CLOSE:
                throw new InternalError("This should be handled earlier.");
            }
            curx_adjust = x_adjust;
            cury_adjust = y_adjust;
            return type;
        }
        public int currentSegment(double[] coords) {
            float[] tmp = new float[6];
            int type = this.currentSegment(tmp);
            for (int i = 0; i < 6; i++) {
                coords[i] = (float) tmp[i];
            }
            return type;
        }
        public int getWindingRule() {
            return src.getWindingRule();
        }
        public boolean isDone() {
            return src.isDone();
        }
        public void next() {
            src.next();
        }
    }
    static void pathTo(PathIterator pi, PathConsumer2D pc2d) {
        RenderingEngine.feedConsumer(pi, pc2d);
        pc2d.pathDone();
    }
    public AATileGenerator getAATileGenerator(Shape s,
                                              AffineTransform at,
                                              Region clip,
                                              BasicStroke bs,
                                              boolean thin,
                                              boolean normalize,
                                              int bbox[])
    {
        Renderer r;
        NormMode norm = (normalize) ? NormMode.ON_WITH_AA : NormMode.OFF;
        if (bs == null) {
            PathIterator pi;
            if (normalize) {
                pi = new NormalizingPathIterator(s.getPathIterator(at), norm);
            } else {
                pi = s.getPathIterator(at);
            }
            r = new Renderer(3, 3,
                             clip.getLoX(), clip.getLoY(),
                             clip.getWidth(), clip.getHeight(),
                             pi.getWindingRule());
            pathTo(pi, r);
        } else {
            r = new Renderer(3, 3,
                             clip.getLoX(), clip.getLoY(),
                             clip.getWidth(), clip.getHeight(),
                             PathIterator.WIND_NON_ZERO);
            strokeTo(s, at, bs, thin, norm, true, r);
        }
        r.endRendering();
        PiscesTileGenerator ptg = new PiscesTileGenerator(r, r.MAX_AA_ALPHA);
        ptg.getBbox(bbox);
        return ptg;
    }
    public AATileGenerator getAATileGenerator(double x, double y,
                                              double dx1, double dy1,
                                              double dx2, double dy2,
                                              double lw1, double lw2,
                                              Region clip,
                                              int bbox[])
    {
        double ldx1, ldy1, ldx2, ldy2;
        boolean innerpgram = (lw1 > 0 && lw2 > 0);
        if (innerpgram) {
            ldx1 = dx1 * lw1;
            ldy1 = dy1 * lw1;
            ldx2 = dx2 * lw2;
            ldy2 = dy2 * lw2;
            x -= (ldx1 + ldx2) / 2.0;
            y -= (ldy1 + ldy2) / 2.0;
            dx1 += ldx1;
            dy1 += ldy1;
            dx2 += ldx2;
            dy2 += ldy2;
            if (lw1 > 1 && lw2 > 1) {
                innerpgram = false;
            }
        } else {
            ldx1 = ldy1 = ldx2 = ldy2 = 0;
        }
        Renderer r = new Renderer(3, 3,
                clip.getLoX(), clip.getLoY(),
                clip.getWidth(), clip.getHeight(),
                PathIterator.WIND_EVEN_ODD);
        r.moveTo((float) x, (float) y);
        r.lineTo((float) (x+dx1), (float) (y+dy1));
        r.lineTo((float) (x+dx1+dx2), (float) (y+dy1+dy2));
        r.lineTo((float) (x+dx2), (float) (y+dy2));
        r.closePath();
        if (innerpgram) {
            x += ldx1 + ldx2;
            y += ldy1 + ldy2;
            dx1 -= 2.0 * ldx1;
            dy1 -= 2.0 * ldy1;
            dx2 -= 2.0 * ldx2;
            dy2 -= 2.0 * ldy2;
            r.moveTo((float) x, (float) y);
            r.lineTo((float) (x+dx1), (float) (y+dy1));
            r.lineTo((float) (x+dx1+dx2), (float) (y+dy1+dy2));
            r.lineTo((float) (x+dx2), (float) (y+dy2));
            r.closePath();
        }
        r.pathDone();
        r.endRendering();
        PiscesTileGenerator ptg = new PiscesTileGenerator(r, r.MAX_AA_ALPHA);
        ptg.getBbox(bbox);
        return ptg;
    }
    public float getMinimumAAPenSize() {
        return 0.5f;
    }
    static {
        if (PathIterator.WIND_NON_ZERO != Renderer.WIND_NON_ZERO ||
            PathIterator.WIND_EVEN_ODD != Renderer.WIND_EVEN_ODD ||
            BasicStroke.JOIN_MITER != Stroker.JOIN_MITER ||
            BasicStroke.JOIN_ROUND != Stroker.JOIN_ROUND ||
            BasicStroke.JOIN_BEVEL != Stroker.JOIN_BEVEL ||
            BasicStroke.CAP_BUTT != Stroker.CAP_BUTT ||
            BasicStroke.CAP_ROUND != Stroker.CAP_ROUND ||
            BasicStroke.CAP_SQUARE != Stroker.CAP_SQUARE)
        {
            throw new InternalError("mismatched renderer constants");
        }
    }
}
