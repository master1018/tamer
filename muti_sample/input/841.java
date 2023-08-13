public class PgramUserBoundsTest {
    static final int MinX = 10;
    static final int MinY = 20;
    static final int MaxX = 30;
    static final int MaxY = 50;
    static AffineTransform identity = new AffineTransform();
    public static void main(String argv[]) {
        BufferedImage bimg =
            new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bimg.createGraphics();
        g2d.setPaint(new BoundsCheckerPaint(MinX, MinY, MaxX, MaxY));
        testAll(g2d);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                             RenderingHints.VALUE_ANTIALIAS_ON);
        testAll(g2d);
    }
    static void testAll(Graphics2D g2d) {
        g2d.setTransform(identity);
        g2d.translate(100, 100);
        testPrimitives(g2d);
        g2d.setTransform(identity);
        g2d.scale(10, 10);
        testPrimitives(g2d);
        g2d.setTransform(identity);
        g2d.rotate(Math.PI/6);
        testPrimitives(g2d);
    }
    static void testPrimitives(Graphics2D g2d) {
        testLine(g2d);
        testRect(g2d);
    }
    static void testLine(Graphics2D g2d) {
        testLine(g2d, MinX, MinY, MaxX, MaxY);
        testLine(g2d, MaxX, MinY, MinX, MaxY);
        testLine(g2d, MinX, MaxY, MaxX, MinY);
        testLine(g2d, MaxX, MaxY, MinX, MinY);
    }
    static void testRect(Graphics2D g2d) {
        g2d.fillRect(MinX, MinY, MaxX - MinX, MaxY - MinY);
        g2d.fill(new Rectangle(MinX, MinY, MaxX - MinX, MaxY - MinY));
    }
    static void testLine(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        g2d.drawLine(x1, y1, x2, y2);
        g2d.draw(new Line2D.Double(x1, y1, x2, y2));
    }
    static class BoundsCheckerPaint implements Paint {
        private Color c = Color.WHITE;
        private Rectangle2D expectedBounds;
        public BoundsCheckerPaint(double x1, double y1,
                                  double x2, double y2)
        {
            expectedBounds = new Rectangle2D.Double();
            expectedBounds.setFrameFromDiagonal(x1, y1, x2, y2);
        }
        public int getTransparency() {
            return c.getTransparency();
        }
        public PaintContext createContext(ColorModel cm,
                                          Rectangle deviceBounds,
                                          Rectangle2D userBounds,
                                          AffineTransform xform,
                                          RenderingHints hints)
        {
            System.out.println("user bounds = "+userBounds);
            if (!userBounds.equals(expectedBounds)) {
                throw new RuntimeException("bounds fail to match");
            }
            return c.createContext(cm, deviceBounds, userBounds, xform, hints);
        }
    }
}
