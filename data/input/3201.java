public abstract class RenderTests extends GraphicsTests {
    static Group renderroot;
    static Group renderoptroot;
    static Group rendertestroot;
    static Group rendershaperoot;
    static Option paintList;
    static Option doAntialias;
    static Option doAlphaColors;
    static Option sizeList;
    static Option strokeList;
    static final int NUM_RANDOMCOLORS = 4096;
    static final int NUM_RANDOMCOLORMASK = (NUM_RANDOMCOLORS - 1);
    static Color randAlphaColors[];
    static Color randOpaqueColors[];
    static {
        randOpaqueColors = new Color[NUM_RANDOMCOLORS];
        randAlphaColors = new Color[NUM_RANDOMCOLORS];
        for (int i = 0; i < NUM_RANDOMCOLORS; i++) {
            int r = (int) (Math.random() * 255);
            int g = (int) (Math.random() * 255);
            int b = (int) (Math.random() * 255);
            randOpaqueColors[i] = new Color(r, g, b);
            randAlphaColors[i] = makeAlphaColor(randOpaqueColors[i], 32);
        }
    }
    static boolean hasMultiGradient;
    static {
        try {
            hasMultiGradient = (MultipleGradientPaint.class != null);
        } catch (NoClassDefFoundError e) {
        }
    }
    public static void init() {
        renderroot = new Group(graphicsroot, "render", "Rendering Benchmarks");
        renderoptroot = new Group(renderroot, "opts", "Rendering Options");
        rendertestroot = new Group(renderroot, "tests", "Rendering Tests");
        ArrayList paintStrs = new ArrayList();
        ArrayList paintDescs = new ArrayList();
        paintStrs.add("single");
        paintDescs.add("Single Color");
        paintStrs.add("random");
        paintDescs.add("Random Color");
        if (hasGraphics2D) {
            paintStrs.add("gradient2");
            paintDescs.add("2-color GradientPaint");
            if (hasMultiGradient) {
                paintStrs.add("linear2");
                paintDescs.add("2-color LinearGradientPaint");
                paintStrs.add("linear3");
                paintDescs.add("3-color LinearGradientPaint");
                paintStrs.add("radial2");
                paintDescs.add("2-color RadialGradientPaint");
                paintStrs.add("radial3");
                paintDescs.add("3-color RadialGradientPaint");
            }
            paintStrs.add("texture20");
            paintDescs.add("20x20 TexturePaint");
            paintStrs.add("texture32");
            paintDescs.add("32x32 TexturePaint");
        }
        String[] paintStrArr = new String[paintStrs.size()];
        paintStrArr = (String[])paintStrs.toArray(paintStrArr);
        String[] paintDescArr = new String[paintDescs.size()];
        paintDescArr = (String[])paintDescs.toArray(paintDescArr);
        paintList =
            new Option.ObjectList(renderoptroot,
                                  "paint", "Paint Type",
                                  paintStrArr, paintStrArr,
                                  paintStrArr, paintDescArr,
                                  0x1);
        ((Option.ObjectList) paintList).setNumRows(5);
        new RandomColorOpt();
        if (hasGraphics2D) {
            doAlphaColors =
                new Option.Toggle(renderoptroot, "alphacolor",
                                  "Set the alpha of the paint to 0.125",
                                  Option.Toggle.Off);
            doAntialias =
                new Option.Toggle(renderoptroot, "antialias",
                                  "Render shapes antialiased",
                                  Option.Toggle.Off);
            String strokeStrings[] = {
                "width0",
                "width1",
                "width5",
                "width20",
                "dash0_5",
                "dash1_5",
                "dash5_20",
                "dash20_50",
            };
            String strokeDescriptions[] = {
                "Solid Thin lines",
                "Solid Width 1 lines",
                "Solid Width 5 lines",
                "Solid Width 20 lines",
                "Dashed Thin lines",
                "Dashed Width 1 lines",
                "Dashed Width 5 lines",
                "Dashed Width 20 lines",
            };
            BasicStroke strokeObjects[] = {
                new BasicStroke(0f),
                new BasicStroke(1f),
                new BasicStroke(5f),
                new BasicStroke(20f),
                new BasicStroke(0f, BasicStroke.CAP_SQUARE,
                                BasicStroke.JOIN_MITER, 10f,
                                new float[] { 5f, 5f }, 0f),
                new BasicStroke(1f, BasicStroke.CAP_SQUARE,
                                BasicStroke.JOIN_MITER, 10f,
                                new float[] { 5f, 5f }, 0f),
                new BasicStroke(5f, BasicStroke.CAP_SQUARE,
                                BasicStroke.JOIN_MITER, 10f,
                                new float[] { 20f, 20f }, 0f),
                new BasicStroke(20f, BasicStroke.CAP_SQUARE,
                                BasicStroke.JOIN_MITER, 10f,
                                new float[] { 50f, 50f }, 0f),
            };
            strokeList =
                new Option.ObjectList(renderoptroot,
                                      "stroke", "Stroke Type",
                                      strokeStrings, strokeObjects,
                                      strokeStrings, strokeDescriptions,
                                      0x2);
            ((Option.ObjectList) strokeList).setNumRows(4);
        }
        new DrawDiagonalLines();
        new DrawHorizontalLines();
        new DrawVerticalLines();
        new FillRects();
        new DrawRects();
        new FillOvals();
        new DrawOvals();
        new FillPolys();
        new DrawPolys();
        if (hasGraphics2D) {
            rendershaperoot = new Group(rendertestroot, "shape",
                                        "Shape Rendering Tests");
            new FillCubics();
            new DrawCubics();
            new FillEllipse2Ds();
            new DrawEllipse2Ds();
        }
    }
    private static class RandomColorOpt extends Node {
        public RandomColorOpt() {
            super(renderoptroot, "randomcolor",
                  "Use random colors for each shape");
        }
        public JComponent getJComponent() {
            return null;
        }
        public void restoreDefault() {
        }
        public void write(PrintWriter pw) {
        }
        public String setOption(String key, String value) {
            String opts;
            if (value.equals("On")) {
                opts = "random";
            } else if (value.equals("Off")) {
                opts = "single";
            } else if (value.equals("Both")) {
                opts = "random,single";
            } else {
                return "Bad value";
            }
            return ((Option.ObjectList)paintList).setValueFromString(opts);
        }
    }
    public static class Context extends GraphicsTests.Context {
        int colorindex;
        Color colorlist[];
    }
    public RenderTests(Group parent, String nodeName, String description) {
        super(parent, nodeName, description);
        addDependencies(renderoptroot, true);
    }
    public GraphicsTests.Context createContext() {
        return new RenderTests.Context();
    }
    public void initContext(TestEnvironment env, GraphicsTests.Context ctx) {
        super.initContext(env, ctx);
        RenderTests.Context rctx = (RenderTests.Context) ctx;
        boolean alphacolor;
        if (hasGraphics2D) {
            Graphics2D g2d = (Graphics2D) rctx.graphics;
            if (env.isEnabled(doAntialias)) {
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     RenderingHints.VALUE_ANTIALIAS_ON);
            }
            alphacolor = env.isEnabled(doAlphaColors);
            g2d.setStroke((Stroke) env.getModifier(strokeList));
        } else {
            alphacolor = false;
        }
        String paint = (String)env.getModifier(paintList);
        if (paint.equals("single")) {
            Color c = Color.darkGray;
            if (alphacolor) {
                c = makeAlphaColor(c, 32);
            }
            rctx.graphics.setColor(c);
        } else if (paint.equals("random")) {
            rctx.colorlist = alphacolor ? randAlphaColors : randOpaqueColors;
        } else if (paint.equals("gradient2")) {
            Color[] colors = makeGradientColors(2, alphacolor);
            Graphics2D g2d = (Graphics2D)rctx.graphics;
            g2d.setPaint(new GradientPaint(0.0f, 0.0f, colors[0],
                                           10.0f, 10.0f, colors[1], true));
        } else if (paint.equals("linear2")) {
            Graphics2D g2d = (Graphics2D)rctx.graphics;
            g2d.setPaint(makeLinear(2, alphacolor));
        } else if (paint.equals("linear3")) {
            Graphics2D g2d = (Graphics2D)rctx.graphics;
            g2d.setPaint(makeLinear(3, alphacolor));
        } else if (paint.equals("radial2")) {
            Graphics2D g2d = (Graphics2D)rctx.graphics;
            g2d.setPaint(makeRadial(2, alphacolor));
        } else if (paint.equals("radial3")) {
            Graphics2D g2d = (Graphics2D)rctx.graphics;
            g2d.setPaint(makeRadial(3, alphacolor));
        } else if (paint.equals("texture20")) {
            Graphics2D g2d = (Graphics2D)rctx.graphics;
            g2d.setPaint(makeTexturePaint(20, alphacolor));
        } else if (paint.equals("texture32")) {
            Graphics2D g2d = (Graphics2D)rctx.graphics;
            g2d.setPaint(makeTexturePaint(32, alphacolor));
        } else {
            throw new InternalError("Invalid paint mode");
        }
    }
    private Color[] makeGradientColors(int numColors, boolean alpha) {
        Color[] colors = new Color[] {Color.red, Color.blue,
                                      Color.green, Color.yellow};
        Color[] ret = new Color[numColors];
        for (int i = 0; i < numColors; i++) {
            ret[i] = alpha ? makeAlphaColor(colors[i], 32) : colors[i];
        }
        return ret;
    }
    private LinearGradientPaint makeLinear(int numColors, boolean alpha) {
        float interval = 1.0f / (numColors - 1);
        float[] fractions = new float[numColors];
        for (int i = 0; i < fractions.length; i++) {
            fractions[i] = i * interval;
        }
        Color[] colors = makeGradientColors(numColors, alpha);
        return new LinearGradientPaint(0.0f, 0.0f,
                                       10.0f, 10.0f,
                                       fractions, colors,
                                       CycleMethod.REFLECT);
    }
    private RadialGradientPaint makeRadial(int numColors, boolean alpha) {
        float interval = 1.0f / (numColors - 1);
        float[] fractions = new float[numColors];
        for (int i = 0; i < fractions.length; i++) {
            fractions[i] = i * interval;
        }
        Color[] colors = makeGradientColors(numColors, alpha);
        return new RadialGradientPaint(0.0f, 0.0f, 10.0f,
                                       fractions, colors,
                                       CycleMethod.REFLECT);
    }
    private TexturePaint makeTexturePaint(int size, boolean alpha) {
        int s2 = size / 2;
        int type =
            alpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage img = new BufferedImage(size, size, type);
        Color[] colors = makeGradientColors(4, alpha);
        Graphics2D g2d = img.createGraphics();
        g2d.setComposite(AlphaComposite.Src);
        g2d.setColor(colors[0]);
        g2d.fillRect(0, 0, s2, s2);
        g2d.setColor(colors[1]);
        g2d.fillRect(s2, 0, s2, s2);
        g2d.setColor(colors[3]);
        g2d.fillRect(0, s2, s2, s2);
        g2d.setColor(colors[2]);
        g2d.fillRect(s2, s2, s2, s2);
        g2d.dispose();
        Rectangle2D bounds = new Rectangle2D.Float(0, 0, size, size);
        return new TexturePaint(img, bounds);
    }
    public static class DrawDiagonalLines extends RenderTests {
        public DrawDiagonalLines() {
            super(rendertestroot, "drawLine", "Draw Diagonal Lines");
        }
        public int pixelsTouched(GraphicsTests.Context ctx) {
            return Math.max(ctx.outdim.width, ctx.outdim.height);
        }
        public void runTest(Object ctx, int numReps) {
            RenderTests.Context rctx = (RenderTests.Context) ctx;
            int size = rctx.size - 1;
            int x = rctx.initX;
            int y = rctx.initY;
            Graphics g = rctx.graphics;
            g.translate(rctx.orgX, rctx.orgY);
            Color rCArray[] = rctx.colorlist;
            int ci = rctx.colorindex;
            if (rctx.animate) {
                do {
                    if (rCArray != null) {
                        g.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                    }
                    g.drawLine(x, y, x + size, y + size);
                    if ((x -= 3) < 0) x += rctx.maxX;
                    if ((y -= 1) < 0) y += rctx.maxY;
                } while (--numReps > 0);
            } else {
                do {
                    if (rCArray != null) {
                        g.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                    }
                    g.drawLine(x, y, x + size, y + size);
                } while (--numReps > 0);
            }
            rctx.colorindex = ci;
            g.translate(-rctx.orgX, -rctx.orgY);
        }
    }
    public static class DrawHorizontalLines extends RenderTests {
        public DrawHorizontalLines() {
            super(rendertestroot, "drawLineHoriz",
                  "Draw Horizontal Lines");
        }
        public int pixelsTouched(GraphicsTests.Context ctx) {
            return ctx.outdim.width;
        }
        public Dimension getOutputSize(int w, int h) {
            return new Dimension(w, 1);
        }
        public void runTest(Object ctx, int numReps) {
            RenderTests.Context rctx = (RenderTests.Context) ctx;
            int size = rctx.size - 1;
            int x = rctx.initX;
            int y = rctx.initY;
            Graphics g = rctx.graphics;
            g.translate(rctx.orgX, rctx.orgY);
            Color rCArray[] = rctx.colorlist;
            int ci = rctx.colorindex;
            if (rctx.animate) {
                do {
                    if (rCArray != null) {
                        g.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                    }
                    g.drawLine(x, y, x + size, y);
                    if ((x -= 3) < 0) x += rctx.maxX;
                    if ((y -= 1) < 0) y += rctx.maxY;
                } while (--numReps > 0);
            } else {
                do {
                    if (rCArray != null) {
                        g.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                    }
                    g.drawLine(x, y, x + size, y);
                } while (--numReps > 0);
            }
            rctx.colorindex = ci;
            g.translate(-rctx.orgX, -rctx.orgY);
        }
    }
    public static class DrawVerticalLines extends RenderTests {
        public DrawVerticalLines() {
            super(rendertestroot, "drawLineVert",
                  "Draw Vertical Lines");
        }
        public int pixelsTouched(GraphicsTests.Context ctx) {
            return ctx.outdim.height;
        }
        public Dimension getOutputSize(int w, int h) {
            return new Dimension(1, h);
        }
        public void runTest(Object ctx, int numReps) {
            RenderTests.Context rctx = (RenderTests.Context) ctx;
            int size = rctx.size - 1;
            int x = rctx.initX;
            int y = rctx.initY;
            Graphics g = rctx.graphics;
            g.translate(rctx.orgX, rctx.orgY);
            Color rCArray[] = rctx.colorlist;
            int ci = rctx.colorindex;
            if (rctx.animate) {
                do {
                    if (rCArray != null) {
                        g.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                    }
                    g.drawLine(x, y, x, y + size);
                    if ((x -= 3) < 0) x += rctx.maxX;
                    if ((y -= 1) < 0) y += rctx.maxY;
                } while (--numReps > 0);
            } else {
                do {
                    if (rCArray != null) {
                        g.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                    }
                    g.drawLine(x, y, x, y + size);
                } while (--numReps > 0);
            }
            rctx.colorindex = ci;
            g.translate(-rctx.orgX, -rctx.orgY);
        }
    }
    public static class FillRects extends RenderTests {
        public FillRects() {
            super(rendertestroot, "fillRect", "Fill Rectangles");
        }
        public void runTest(Object ctx, int numReps) {
            RenderTests.Context rctx = (RenderTests.Context) ctx;
            int size = rctx.size;
            int x = rctx.initX;
            int y = rctx.initY;
            Graphics g = rctx.graphics;
            g.translate(rctx.orgX, rctx.orgY);
            Color rCArray[] = rctx.colorlist;
            int ci = rctx.colorindex;
            if (rctx.animate) {
                do {
                    if (rCArray != null) {
                        g.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                    }
                    g.fillRect(x, y, size, size);
                    if ((x -= 3) < 0) x += rctx.maxX;
                    if ((y -= 1) < 0) y += rctx.maxY;
                } while (--numReps > 0);
            } else {
                do {
                    if (rCArray != null) {
                        g.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                    }
                    g.fillRect(x, y, size, size);
                } while (--numReps > 0);
            }
            rctx.colorindex = ci;
            g.translate(-rctx.orgX, -rctx.orgY);
        }
    }
    public static class DrawRects extends RenderTests {
        public DrawRects() {
            super(rendertestroot, "drawRect", "Draw Rectangles");
        }
        public int pixelsTouched(GraphicsTests.Context ctx) {
            int w = ctx.outdim.width;
            int h = ctx.outdim.height;
            if (w < 2 || h < 2) {
                return w * h;
            }
            return (w * 2) + ((h - 2) * 2);
        }
        public void runTest(Object ctx, int numReps) {
            RenderTests.Context rctx = (RenderTests.Context) ctx;
            int size = rctx.size - 1;
            int x = rctx.initX;
            int y = rctx.initY;
            Graphics g = rctx.graphics;
            g.translate(rctx.orgX, rctx.orgY);
            Color rCArray[] = rctx.colorlist;
            int ci = rctx.colorindex;
            if (rctx.animate) {
                do {
                    if (rCArray != null) {
                        g.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                    }
                    g.drawRect(x, y, size, size);
                    if ((x -= 3) < 0) x += rctx.maxX;
                    if ((y -= 1) < 0) y += rctx.maxY;
                } while (--numReps > 0);
            } else {
                do {
                    if (rCArray != null) {
                        g.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                    }
                    g.drawRect(x, y, size, size);
                } while (--numReps > 0);
            }
            rctx.colorindex = ci;
            g.translate(-rctx.orgX, -rctx.orgY);
        }
    }
    public static class FillOvals extends RenderTests {
        public FillOvals() {
            super(rendertestroot, "fillOval", "Fill Ellipses");
        }
        public int pixelsTouched(GraphicsTests.Context ctx) {
            double xaxis = ctx.outdim.width / 2.0;
            double yaxis = ctx.outdim.height / 2.0;
            return (int) (xaxis * yaxis * Math.PI);
        }
        public void runTest(Object ctx, int numReps) {
            RenderTests.Context rctx = (RenderTests.Context) ctx;
            int size = rctx.size;
            int x = rctx.initX;
            int y = rctx.initY;
            Graphics g = rctx.graphics;
            g.translate(rctx.orgX, rctx.orgY);
            Color rCArray[] = rctx.colorlist;
            int ci = rctx.colorindex;
            if (rctx.animate) {
                do {
                    if (rCArray != null) {
                        g.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                    }
                    g.fillOval(x, y, size, size);
                    if ((x -= 3) < 0) x += rctx.maxX;
                    if ((y -= 1) < 0) y += rctx.maxY;
                } while (--numReps > 0);
            } else {
                do {
                    if (rCArray != null) {
                        g.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                    }
                    g.fillOval(x, y, size, size);
                } while (--numReps > 0);
            }
            rctx.colorindex = ci;
            g.translate(-rctx.orgX, -rctx.orgY);
        }
    }
    public static class DrawOvals extends RenderTests {
        public DrawOvals() {
            super(rendertestroot, "drawOval", "Draw Ellipses");
        }
        public int pixelsTouched(GraphicsTests.Context ctx) {
            return (int)(Math.sqrt(2.0)*(ctx.outdim.width+ctx.outdim.height));
        }
        public void runTest(Object ctx, int numReps) {
            RenderTests.Context rctx = (RenderTests.Context) ctx;
            int size = rctx.size - 1;
            int x = rctx.initX;
            int y = rctx.initY;
            Graphics g = rctx.graphics;
            g.translate(rctx.orgX, rctx.orgY);
            Color rCArray[] = rctx.colorlist;
            int ci = rctx.colorindex;
            if (rctx.animate) {
                do {
                    if (rCArray != null) {
                        g.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                    }
                    g.drawOval(x, y, size, size);
                    if ((x -= 3) < 0) x += rctx.maxX;
                    if ((y -= 1) < 0) y += rctx.maxY;
                } while (--numReps > 0);
            } else {
                do {
                    if (rCArray != null) {
                        g.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                    }
                    g.drawOval(x, y, size, size);
                } while (--numReps > 0);
            }
            rctx.colorindex = ci;
            g.translate(-rctx.orgX, -rctx.orgY);
        }
    }
    public static class FillPolys extends RenderTests {
        public FillPolys() {
            super(rendertestroot, "fillPoly", "Fill Hexagonal Polygons");
        }
        public int pixelsTouched(GraphicsTests.Context ctx) {
            int size = ctx.outdim.width * ctx.outdim.height;
            return size - (size / 4);
        }
        public void runTest(Object ctx, int numReps) {
            RenderTests.Context rctx = (RenderTests.Context) ctx;
            int size = rctx.size;
            int x = rctx.initX;
            int y = rctx.initY;
            int hexaX[] = new int[6];
            int hexaY[] = new int[6];
            Graphics g = rctx.graphics;
            g.translate(rctx.orgX, rctx.orgY);
            Color rCArray[] = rctx.colorlist;
            int ci = rctx.colorindex;
            do {
                hexaX[0] = x;
                hexaX[1] = hexaX[5] = x+size/4;
                hexaX[2] = hexaX[4] = x+size-size/4;
                hexaX[3] = x+size;
                hexaY[1] = hexaY[2] = y;
                hexaY[0] = hexaY[3] = y+size/2;
                hexaY[4] = hexaY[5] = y+size;
                if (rCArray != null) {
                    g.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                }
                g.fillPolygon(hexaX, hexaY, 6);
                if ((x -= 3) < 0) x += rctx.maxX;
                if ((y -= 1) < 0) y += rctx.maxY;
            } while (--numReps > 0);
            rctx.colorindex = ci;
            g.translate(-rctx.orgX, -rctx.orgY);
        }
    }
    public static class DrawPolys extends RenderTests {
        public DrawPolys() {
            super(rendertestroot, "drawPoly", "Draw Hexagonal Polygons");
        }
        public int pixelsTouched(GraphicsTests.Context ctx) {
            int size = ctx.size;
            if (size <= 1) {
                return 1;
            } else {
                return (size - (size / 4) - 1) * 4;
            }
        }
        public void runTest(Object ctx, int numReps) {
            RenderTests.Context rctx = (RenderTests.Context) ctx;
            int size = rctx.size - 1;
            int x = rctx.initX;
            int y = rctx.initY;
            int hexaX[] = new int[6];
            int hexaY[] = new int[6];
            Graphics g = rctx.graphics;
            g.translate(rctx.orgX, rctx.orgY);
            Color rCArray[] = rctx.colorlist;
            int ci = rctx.colorindex;
            do {
                hexaX[0] = x;
                hexaX[1] = hexaX[5] = x+size/4;
                hexaX[2] = hexaX[4] = x+size-size/4;
                hexaX[3] = x+size;
                hexaY[1] = hexaY[2] = y;
                hexaY[0] = hexaY[3] = y+size/2;
                hexaY[4] = hexaY[5] = y+size;
                if (rCArray != null) {
                    g.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                }
                g.drawPolygon(hexaX, hexaY, 6);
                if ((x -= 3) < 0) x += rctx.maxX;
                if ((y -= 1) < 0) y += rctx.maxY;
            } while (--numReps > 0);
            rctx.colorindex = ci;
            g.translate(-rctx.orgX, -rctx.orgY);
        }
    }
    public static class FillCubics extends RenderTests {
        static final double relTmax = .5 - Math.sqrt(3) / 6;
        static final double relYmax = ((6*relTmax - 9)*relTmax + 3)*relTmax;
        public FillCubics() {
            super(rendershaperoot, "fillCubic", "Fill Bezier Curves");
        }
        public int pixelsTouched(GraphicsTests.Context ctx) {
            int size = ctx.size;
            if (size < 2) size = 2;
            return size * size * 5 / 16;
        }
        public static class Context extends RenderTests.Context {
            CubicCurve2D curve = new CubicCurve2D.Float();
        }
        public GraphicsTests.Context createContext() {
            return new FillCubics.Context();
        }
        public void runTest(Object ctx, int numReps) {
            FillCubics.Context cctx = (FillCubics.Context) ctx;
            int size = cctx.size;
            if (size < 2) size = 2;
            int x = cctx.initX;
            int y = cctx.initY;
            int cpoffset = (int) (size/relYmax/2);
            CubicCurve2D curve = cctx.curve;
            Graphics2D g2d = (Graphics2D) cctx.graphics;
            g2d.translate(cctx.orgX, cctx.orgY);
            Color rCArray[] = cctx.colorlist;
            int ci = cctx.colorindex;
            do {
                curve.setCurve(x, y+size/2.0,
                               x+size/2.0, y+size/2.0-cpoffset,
                               x+size/2.0, y+size/2.0+cpoffset,
                               x+size, y+size/2.0);
                if (rCArray != null) {
                    g2d.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                }
                g2d.fill(curve);
                if ((x -= 3) < 0) x += cctx.maxX;
                if ((y -= 1) < 0) y += cctx.maxY;
            } while (--numReps > 0);
            cctx.colorindex = ci;
            g2d.translate(-cctx.orgX, -cctx.orgY);
        }
    }
    public static class DrawCubics extends RenderTests {
        static final double relTmax = .5 - Math.sqrt(3) / 6;
        static final double relYmax = ((6*relTmax - 9)*relTmax + 3)*relTmax;
        public DrawCubics() {
            super(rendershaperoot, "drawCubic", "Draw Bezier Curves");
        }
        public int pixelsTouched(GraphicsTests.Context ctx) {
            int size = ctx.size;
            if (size < 2) size = 2;
            return size;
        }
        public static class Context extends RenderTests.Context {
            CubicCurve2D curve = new CubicCurve2D.Float();
        }
        public GraphicsTests.Context createContext() {
            return new DrawCubics.Context();
        }
        public void runTest(Object ctx, int numReps) {
            DrawCubics.Context cctx = (DrawCubics.Context) ctx;
            int size = cctx.size;
            if (size < 2) size = 2;
            int x = cctx.initX;
            int y = cctx.initY;
            int cpoffset = (int) (size/relYmax/2);
            CubicCurve2D curve = cctx.curve;
            Graphics2D g2d = (Graphics2D) cctx.graphics;
            g2d.translate(cctx.orgX, cctx.orgY);
            Color rCArray[] = cctx.colorlist;
            int ci = cctx.colorindex;
            do {
                curve.setCurve(x, y+size/2.0,
                               x+size/2.0, y+size/2.0-cpoffset,
                               x+size/2.0, y+size/2.0+cpoffset,
                               x+size, y+size/2.0);
                if (rCArray != null) {
                    g2d.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                }
                g2d.draw(curve);
                if ((x -= 3) < 0) x += cctx.maxX;
                if ((y -= 1) < 0) y += cctx.maxY;
            } while (--numReps > 0);
            cctx.colorindex = ci;
            g2d.translate(-cctx.orgX, -cctx.orgY);
        }
    }
    public static class FillEllipse2Ds extends RenderTests {
        public FillEllipse2Ds() {
            super(rendershaperoot, "fillEllipse2D", "Fill Ellipse2Ds");
        }
        public int pixelsTouched(GraphicsTests.Context ctx) {
            double xaxis = ctx.outdim.width / 2.0;
            double yaxis = ctx.outdim.height / 2.0;
            return (int) (xaxis * yaxis * Math.PI);
        }
        public static class Context extends RenderTests.Context {
            Ellipse2D ellipse = new Ellipse2D.Float();
        }
        public GraphicsTests.Context createContext() {
            return new FillEllipse2Ds.Context();
        }
        public void runTest(Object ctx, int numReps) {
            FillEllipse2Ds.Context cctx = (FillEllipse2Ds.Context) ctx;
            int size = cctx.size;
            int x = cctx.initX;
            int y = cctx.initY;
            Ellipse2D ellipse = cctx.ellipse;
            Graphics2D g2d = (Graphics2D) cctx.graphics;
            g2d.translate(cctx.orgX, cctx.orgY);
            Color rCArray[] = cctx.colorlist;
            int ci = cctx.colorindex;
            do {
                if (rCArray != null) {
                    g2d.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                }
                ellipse.setFrame(x, y, size, size);
                g2d.fill(ellipse);
                if ((x -= 3) < 0) x += cctx.maxX;
                if ((y -= 1) < 0) y += cctx.maxY;
            } while (--numReps > 0);
            cctx.colorindex = ci;
            g2d.translate(-cctx.orgX, -cctx.orgY);
        }
    }
    public static class DrawEllipse2Ds extends RenderTests {
        public DrawEllipse2Ds() {
            super(rendershaperoot, "drawEllipse2D", "Draw Ellipse2Ds");
        }
        public int pixelsTouched(GraphicsTests.Context ctx) {
            return (int)(Math.sqrt(2.0)*(ctx.outdim.width+ctx.outdim.height));
        }
        public static class Context extends RenderTests.Context {
            Ellipse2D ellipse = new Ellipse2D.Float();
        }
        public GraphicsTests.Context createContext() {
            return new DrawEllipse2Ds.Context();
        }
        public void runTest(Object ctx, int numReps) {
            DrawEllipse2Ds.Context cctx = (DrawEllipse2Ds.Context) ctx;
            int size = cctx.size;
            int x = cctx.initX;
            int y = cctx.initY;
            Ellipse2D ellipse = cctx.ellipse;
            Graphics2D g2d = (Graphics2D) cctx.graphics;
            g2d.translate(cctx.orgX, cctx.orgY);
            Color rCArray[] = cctx.colorlist;
            int ci = cctx.colorindex;
            do {
                if (rCArray != null) {
                    g2d.setColor(rCArray[ci++ & NUM_RANDOMCOLORMASK]);
                }
                ellipse.setFrame(x, y, size, size);
                g2d.draw(ellipse);
                if ((x -= 3) < 0) x += cctx.maxX;
                if ((y -= 1) < 0) y += cctx.maxY;
            } while (--numReps > 0);
            cctx.colorindex = ci;
            g2d.translate(-cctx.orgX, -cctx.orgY);
        }
    }
}
