class GradientPaintContext implements PaintContext {
    static int LOOKUP_SIZE = 256;
    static int LOOKUP_MASK = 0x1FF;
    static double ZERO = 1E-10;
    ColorModel cm;
    boolean cyclic;
    int c1;
    int c2;
    int[] table;
    int dx;
    int dy;
    int delta;
    GradientPaintContext(ColorModel cm, AffineTransform t, Point2D point1, Color color1, Point2D point2, Color color2, boolean cyclic) {
        this.cyclic = cyclic;
        this.cm = ColorModel.getRGBdefault();
        c1 = color1.getRGB();
        c2 = color2.getRGB();
        double px = point2.getX() - point1.getX();
        double py = point2.getY() - point1.getY();
        Point2D p = t.transform(point1, null);
        Point2D bx = new Point2D.Double(px, py);
        Point2D by = new Point2D.Double(py, -px);
        t.deltaTransform(bx, bx);
        t.deltaTransform(by, by);
        double vec = bx.getX() * by.getY() - bx.getY() * by.getX();
        if (Math.abs(vec) < ZERO) {
            dx = dy = delta = 0;
            table = new int[1];
            table[0] = c1;
        } else {
            double mult = LOOKUP_SIZE * 256 / vec;
            dx = (int)(by.getX() * mult);
            dy = (int)(by.getY() * mult);
            delta = (int)((p.getX() * by.getY() - p.getY() * by.getX()) * mult);
            createTable();
        }
    }
    void createTable() {
        double ca = (c1 >> 24) & 0xFF;
        double cr = (c1 >> 16) & 0xFF;
        double cg = (c1 >> 8) & 0xFF;
        double cb = c1 & 0xFF;
        double k = 1.0 / LOOKUP_SIZE;
        double da = (((c2 >> 24) & 0xFF) - ca) * k;
        double dr = (((c2 >> 16) & 0xFF) - cr) * k;
        double dg = (((c2 >> 8) & 0xFF) - cg) * k;
        double db = ((c2 & 0xFF) - cb) * k;
        table = new int[cyclic ? LOOKUP_SIZE + LOOKUP_SIZE : LOOKUP_SIZE];
        for(int i = 0; i < LOOKUP_SIZE; i++) {
            table[i] =
                (int)ca << 24 |
                (int)cr << 16 |
                (int)cg << 8 |
                (int)cb;
            ca += da;
            cr += dr;
            cg += dg;
            cb += db;
        }
        if (cyclic) {
            for(int i = 0; i < LOOKUP_SIZE; i++) {
                table[LOOKUP_SIZE + LOOKUP_SIZE - 1 - i] = table[i];
            }
        }
    }
    public ColorModel getColorModel() {
        return cm;
    }
    public void dispose() {
    }
    public Raster getRaster(int x, int y, int w, int h) {
        WritableRaster rast = cm.createCompatibleWritableRaster(w, h);
        int[] buf = ((DataBufferInt)rast.getDataBuffer()).getData();
        int c = x * dy - y * dx - delta;
        int cx = dy;
        int cy = - w * dy - dx;
        int k = 0;
        if (cyclic) {
            for(int j = 0; j < h; j++) {
                for(int i = 0; i < w; i++) {
                    buf[k++] = table[(c >> 8) & LOOKUP_MASK];
                    c += cx;
                }
                c += cy;
            }
        } else {
            for(int j = 0; j < h; j++) {
                for(int i = 0; i < w; i++) {
                    int index = c >> 8;
                    buf[k++] = index < 0 ? c1 : index >= LOOKUP_SIZE ? c2 : table[index];
                    c += cx;
                }
                c += cy;
            }
        }
        return rast;
    }
}
