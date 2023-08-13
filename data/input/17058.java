public class TransformedPaintTest {
    private static enum PaintType {
        COLOR,
        GRADIENT,
        LINEAR_GRADIENT,
        RADIAL_GRADIENT,
        TEXTURE
    };
    private static final int CELL_SIZE = 100;
    private static final int R_WIDTH = 3 * CELL_SIZE;
    private static final int R_HEIGHT = PaintType.values().length * CELL_SIZE;
    private Paint createPaint(PaintType type, int startx, int starty,
                              int w, int h)
    {
        w++; h++;
        int endx = startx + w;
        int endy = starty + h;
        Rectangle2D.Float r = new Rectangle2D.Float(startx, starty, w, h);
        switch (type) {
            case COLOR: return Color.red;
            case GRADIENT: return
                new GradientPaint(startx, starty, Color.red,
                                  endx, endy, Color.green);
            case LINEAR_GRADIENT: return
                new LinearGradientPaint(startx, starty, endx, endy,
                    new float[] { 0.0f, 0.999f, 1.0f },
                    new Color[] { Color.red, Color.green, Color.blue });
            case RADIAL_GRADIENT: return
                new RadialGradientPaint(startx, starty,
                    (float)Math.sqrt(w * w + h * h),
                    new float[] { 0.0f, 0.999f, 1.0f },
                    new Color[] { Color.red, Color.green, Color.blue },
                    CycleMethod.NO_CYCLE);
            case TEXTURE: {
                BufferedImage bi =
                    new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = (Graphics2D) bi.getGraphics();
                g.setPaint(createPaint(PaintType.LINEAR_GRADIENT, 0, 0, w, h));
                g.fillRect(0, 0, w, h);
                return new TexturePaint(bi, r);
            }
        }
        return Color.green;
    }
    private void renderLine(PaintType type, Graphics2D g,
                            int startx, int starty, int w, int h)
    {
        Paint p = createPaint(type, startx, starty, w, h);
        g.setPaint(p);
        g.fillRect(startx, starty, w, h);
        g.translate(w, 0);
        g.fillRect(startx, starty, w, h);
        g.translate(-w, 0);
        g.translate(startx + w*2, starty);
        g.rotate(Math.toRadians(90), w/2, h/2);
        g.translate(-startx, -starty);
        g.fillRect(startx, starty, w, h);
    }
    private void render(Graphics2D g, int w, int h) {
        int paintTypes = PaintType.values().length;
        int ystep = h / paintTypes;
        int y = 0;
        for (PaintType type : PaintType.values()) {
            renderLine(type, (Graphics2D)g.create(),
                       0, y, h / paintTypes, h / paintTypes);
            y += ystep;
        }
    }
    private void checkBI(BufferedImage bi) {
        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                if (bi.getRGB(x, y) == Color.blue.getRGB()) {
                    try {
                        String fileName = "TransformedPaintTest_res.png";
                        ImageIO.write(bi, "png", new File(fileName));
                        System.err.println("Dumped image to: " + fileName);
                    } catch (IOException ex) {}
                    throw new RuntimeException("Test failed, blue color found");
                }
            }
        }
    }
    private void runTest() {
        GraphicsConfiguration gc = GraphicsEnvironment.
            getLocalGraphicsEnvironment().getDefaultScreenDevice().
                getDefaultConfiguration();
        if (gc.getColorModel().getPixelSize() < 16) {
            System.out.println("8-bit desktop depth found, test passed");
            return;
        }
        VolatileImage vi = gc.createCompatibleVolatileImage(R_WIDTH, R_HEIGHT);
        BufferedImage bi = null;
        do {
            vi.validate(gc);
            Graphics2D g = vi.createGraphics();
            render(g, vi.getWidth(), vi.getHeight());
            bi = vi.getSnapshot();
        } while (vi.contentsLost());
        checkBI(bi);
        System.out.println("Test PASSED.");
    }
    private static void showFrame(final TransformedPaintTest t) {
        JFrame f = new JFrame("TransformedPaintTest");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final BufferedImage bi =
            new BufferedImage(R_WIDTH, R_HEIGHT, BufferedImage.TYPE_INT_RGB);
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                t.render(g2d, R_WIDTH, R_HEIGHT);
                t.render(bi.createGraphics(), R_WIDTH, R_HEIGHT);
                g2d.drawImage(bi, R_WIDTH + 5, 0, null);
                g.setColor(Color.black);
                g.drawString("Rendered to Back Buffer", 10, 20);
                g.drawString("Rendered to BufferedImage", R_WIDTH + 15, 20);
            }
        };
        p.setPreferredSize(new Dimension(2 * R_WIDTH + 5, R_HEIGHT));
        f.add(p);
        f.pack();
        f.setVisible(true);
    }
    public static void main(String[] args) throws
        InterruptedException, InvocationTargetException
    {
        boolean show = (args.length > 0 && "-show".equals(args[0]));
        final TransformedPaintTest t = new TransformedPaintTest();
        if (show) {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    showFrame(t);
                }
            });
        } else {
            t.runTest();
        }
    }
}
