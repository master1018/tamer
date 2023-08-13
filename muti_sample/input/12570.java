public abstract class RenderingEngine {
    private static RenderingEngine reImpl;
    public static synchronized RenderingEngine getInstance() {
        if (reImpl != null) {
            return reImpl;
        }
        reImpl = (RenderingEngine)
            AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    final String ductusREClass = "sun.dc.DuctusRenderingEngine";
                    String reClass =
                        System.getProperty("sun.java2d.renderer", ductusREClass);
                    if (reClass.equals(ductusREClass)) {
                        try {
                            Class cls = Class.forName(ductusREClass);
                            return cls.newInstance();
                        } catch (ClassNotFoundException x) {
                        } catch (IllegalAccessException x) {
                        } catch (InstantiationException x) {
                        }
                    }
                    ServiceLoader<RenderingEngine> reLoader =
                        ServiceLoader.loadInstalled(RenderingEngine.class);
                    RenderingEngine service = null;
                    for (RenderingEngine re : reLoader) {
                        service = re;
                        if (re.getClass().getName().equals(reClass)) {
                            break;
                        }
                    }
                    return service;
                }
            });
        if (reImpl == null) {
            throw new InternalError("No RenderingEngine module found");
        }
        GetPropertyAction gpa =
            new GetPropertyAction("sun.java2d.renderer.trace");
        String reTrace = (String) AccessController.doPrivileged(gpa);
        if (reTrace != null) {
            reImpl = new Tracer(reImpl);
        }
        return reImpl;
    }
    public abstract Shape createStrokedShape(Shape src,
                                             float width,
                                             int caps,
                                             int join,
                                             float miterlimit,
                                             float dashes[],
                                             float dashphase);
    public abstract void strokeTo(Shape src,
                                  AffineTransform at,
                                  BasicStroke bs,
                                  boolean thin,
                                  boolean normalize,
                                  boolean antialias,
                                  PathConsumer2D consumer);
    public abstract AATileGenerator getAATileGenerator(Shape s,
                                                       AffineTransform at,
                                                       Region clip,
                                                       BasicStroke bs,
                                                       boolean thin,
                                                       boolean normalize,
                                                       int bbox[]);
    public abstract AATileGenerator getAATileGenerator(double x, double y,
                                                       double dx1, double dy1,
                                                       double dx2, double dy2,
                                                       double lw1, double lw2,
                                                       Region clip,
                                                       int bbox[]);
    public abstract float getMinimumAAPenSize();
    public static void feedConsumer(PathIterator pi, PathConsumer2D consumer) {
        float coords[] = new float[6];
        while (!pi.isDone()) {
            switch (pi.currentSegment(coords)) {
            case PathIterator.SEG_MOVETO:
                consumer.moveTo(coords[0], coords[1]);
                break;
            case PathIterator.SEG_LINETO:
                consumer.lineTo(coords[0], coords[1]);
                break;
            case PathIterator.SEG_QUADTO:
                consumer.quadTo(coords[0], coords[1],
                                coords[2], coords[3]);
                break;
            case PathIterator.SEG_CUBICTO:
                consumer.curveTo(coords[0], coords[1],
                                 coords[2], coords[3],
                                 coords[4], coords[5]);
                break;
            case PathIterator.SEG_CLOSE:
                consumer.closePath();
                break;
            }
            pi.next();
        }
    }
    static class Tracer extends RenderingEngine {
        RenderingEngine target;
        String name;
        public Tracer(RenderingEngine target) {
            this.target = target;
            name = target.getClass().getName();
        }
        public Shape createStrokedShape(Shape src,
                                        float width,
                                        int caps,
                                        int join,
                                        float miterlimit,
                                        float dashes[],
                                        float dashphase)
        {
            System.out.println(name+".createStrokedShape("+
                               src.getClass().getName()+", "+
                               "width = "+width+", "+
                               "caps = "+caps+", "+
                               "join = "+join+", "+
                               "miter = "+miterlimit+", "+
                               "dashes = "+dashes+", "+
                               "dashphase = "+dashphase+")");
            return target.createStrokedShape(src,
                                             width, caps, join, miterlimit,
                                             dashes, dashphase);
        }
        public void strokeTo(Shape src,
                             AffineTransform at,
                             BasicStroke bs,
                             boolean thin,
                             boolean normalize,
                             boolean antialias,
                             PathConsumer2D consumer)
        {
            System.out.println(name+".strokeTo("+
                               src.getClass().getName()+", "+
                               at+", "+
                               bs+", "+
                               (thin ? "thin" : "wide")+", "+
                               (normalize ? "normalized" : "pure")+", "+
                               (antialias ? "AA" : "non-AA")+", "+
                               consumer.getClass().getName()+")");
            target.strokeTo(src, at, bs, thin, normalize, antialias, consumer);
        }
        public float getMinimumAAPenSize() {
            System.out.println(name+".getMinimumAAPenSize()");
            return target.getMinimumAAPenSize();
        }
        public AATileGenerator getAATileGenerator(Shape s,
                                                  AffineTransform at,
                                                  Region clip,
                                                  BasicStroke bs,
                                                  boolean thin,
                                                  boolean normalize,
                                                  int bbox[])
        {
            System.out.println(name+".getAATileGenerator("+
                               s.getClass().getName()+", "+
                               at+", "+
                               clip+", "+
                               bs+", "+
                               (thin ? "thin" : "wide")+", "+
                               (normalize ? "normalized" : "pure")+")");
            return target.getAATileGenerator(s, at, clip,
                                             bs, thin, normalize,
                                             bbox);
        }
        public AATileGenerator getAATileGenerator(double x, double y,
                                                  double dx1, double dy1,
                                                  double dx2, double dy2,
                                                  double lw1, double lw2,
                                                  Region clip,
                                                  int bbox[])
        {
            System.out.println(name+".getAATileGenerator("+
                               x+", "+y+", "+
                               dx1+", "+dy1+", "+
                               dx2+", "+dy2+", "+
                               lw1+", "+lw2+", "+
                               clip+")");
            return target.getAATileGenerator(x, y,
                                             dx1, dy1,
                                             dx2, dy2,
                                             lw1, lw2,
                                             clip, bbox);
        }
    }
}
