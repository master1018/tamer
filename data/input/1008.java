public class AAShapePipe
    implements ShapeDrawPipe, ParallelogramPipe
{
    static RenderingEngine renderengine = RenderingEngine.getInstance();
    CompositePipe outpipe;
    public AAShapePipe(CompositePipe pipe) {
        outpipe = pipe;
    }
    public void draw(SunGraphics2D sg, Shape s) {
        BasicStroke bs;
        if (sg.stroke instanceof BasicStroke) {
            bs = (BasicStroke) sg.stroke;
        } else {
            s = sg.stroke.createStrokedShape(s);
            bs = null;
        }
        renderPath(sg, s, bs);
    }
    public void fill(SunGraphics2D sg, Shape s) {
        renderPath(sg, s, null);
    }
    private static Rectangle2D computeBBox(double ux1, double uy1,
                                           double ux2, double uy2)
    {
        if ((ux2 -= ux1) < 0) {
            ux1 += ux2;
            ux2 = -ux2;
        }
        if ((uy2 -= uy1) < 0) {
            uy1 += uy2;
            uy2 = -uy2;
        }
        return new Rectangle2D.Double(ux1, uy1, ux2, uy2);
    }
    public void fillParallelogram(SunGraphics2D sg,
                                  double ux1, double uy1,
                                  double ux2, double uy2,
                                  double x, double y,
                                  double dx1, double dy1,
                                  double dx2, double dy2)
    {
        Region clip = sg.getCompClip();
        int abox[] = new int[4];
        AATileGenerator aatg =
            renderengine.getAATileGenerator(x, y, dx1, dy1, dx2, dy2, 0, 0,
                                            clip, abox);
        if (aatg == null) {
            return;
        }
        renderTiles(sg, computeBBox(ux1, uy1, ux2, uy2), aatg, abox);
    }
    public void drawParallelogram(SunGraphics2D sg,
                                  double ux1, double uy1,
                                  double ux2, double uy2,
                                  double x, double y,
                                  double dx1, double dy1,
                                  double dx2, double dy2,
                                  double lw1, double lw2)
    {
        Region clip = sg.getCompClip();
        int abox[] = new int[4];
        AATileGenerator aatg =
            renderengine.getAATileGenerator(x, y, dx1, dy1, dx2, dy2, lw1, lw2,
                                            clip, abox);
        if (aatg == null) {
            return;
        }
        renderTiles(sg, computeBBox(ux1, uy1, ux2, uy2), aatg, abox);
    }
    private static byte[] theTile;
    public synchronized static byte[] getAlphaTile(int len) {
        byte[] t = theTile;
        if (t == null || t.length < len) {
            t = new byte[len];
        } else {
            theTile = null;
        }
        return t;
    }
    public synchronized static void dropAlphaTile(byte[] t) {
        theTile = t;
    }
    public void renderPath(SunGraphics2D sg, Shape s, BasicStroke bs) {
        boolean adjust = (bs != null &&
                          sg.strokeHint != SunHints.INTVAL_STROKE_PURE);
        boolean thin = (sg.strokeState <= sg.STROKE_THINDASHED);
        Region clip = sg.getCompClip();
        int abox[] = new int[4];
        AATileGenerator aatg =
            renderengine.getAATileGenerator(s, sg.transform, clip,
                                            bs, thin, adjust, abox);
        if (aatg == null) {
            return;
        }
        renderTiles(sg, s, aatg, abox);
    }
    public void renderTiles(SunGraphics2D sg, Shape s,
                            AATileGenerator aatg, int abox[])
    {
        Object context = null;
        byte alpha[] = null;
        try {
            context = outpipe.startSequence(sg, s,
                                            new Rectangle(abox[0], abox[1],
                                                          abox[2] - abox[0],
                                                          abox[3] - abox[1]),
                                            abox);
            int tw = aatg.getTileWidth();
            int th = aatg.getTileHeight();
            alpha = getAlphaTile(tw * th);
            byte[] atile;
            for (int y = abox[1]; y < abox[3]; y += th) {
                for (int x = abox[0]; x < abox[2]; x += tw) {
                    int w = Math.min(tw, abox[2] - x);
                    int h = Math.min(th, abox[3] - y);
                    int a = aatg.getTypicalAlpha();
                    if (a == 0x00 ||
                        outpipe.needTile(context, x, y, w, h) == false)
                    {
                        aatg.nextTile();
                        outpipe.skipTile(context, x, y);
                        continue;
                    }
                    if (a == 0xff) {
                        atile = null;
                        aatg.nextTile();
                    } else {
                        atile = alpha;
                        aatg.getAlpha(alpha, 0, tw);
                    }
                    outpipe.renderPathTile(context, atile, 0, tw,
                                           x, y, w, h);
                }
            }
        } finally {
            aatg.dispose();
            if (context != null) {
                outpipe.endSequence(context);
            }
            if (alpha != null) {
                dropAlphaTile(alpha);
            }
        }
    }
}
