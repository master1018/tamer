public class ImageComparator {
    double accuracy;
    int errorCounter = 0;
    double maxError = 0f;
    int rMask = 0x00FF0000;
    int gMask = 0x0000FF00;
    int bMask = 0x000000FF;
    int rShift = 16;
    int gShift = 8;
    int bShift = 0;
    public ImageComparator() {
        accuracy = 0;
    }
    public ImageComparator(double accuracy) {
        this.accuracy = accuracy;
    }
    public ImageComparator(double accuracy, int rBits, int gBits, int bBits) {
        this.accuracy = accuracy;
        rShift += (8 - rBits);
        gShift += (8 - gBits);
        bShift += (8 - bBits);
    }
    public boolean compare(int c1, int c2) {
        int d1 = Math.abs(((c1&bMask)>>bShift) - ((c2&bMask)>>bShift));
        int d2 = Math.abs(((c1&gMask)>>gShift) - ((c2&gMask)>>gShift));
        int d3 = Math.abs(((c1&rMask)>>rShift) - ((c2&rMask)>>rShift));
        if (d1 < d2) d1 = d2;
        if (d1 < d3) d1 = d3;
        if (d1 >= accuracy) {
            errorCounter++;
            if (d1 > maxError) maxError = d1;
            return false;
        }
        return true;
    }
    public boolean compare(double r1, double g1, double b1,
                           double r2, double g2, double b2)
    {
        double d1 = Math.abs(r1 - r2);
        double d2 = Math.abs(g1 - g2);
        double d3 = Math.abs(b1 - b2);
        if (d1 < d2) d1 = d2;
        if (d1 < d3) d1 = d3;
        if (d1 >= accuracy) {
            errorCounter++;
            if (d1 > maxError) maxError = d1;
            return false;
        }
        return true;
    }
    public boolean compare(Color c1, Color c2) {
        return compare(c1.getRed(), c1.getGreen(), c1.getBlue(),
                       c2.getRed(), c2.getGreen(), c2.getBlue());
    }
    public boolean compare(BufferedImage img1, BufferedImage img2) {
        boolean result = true;
        if (img1.getWidth() != img2.getWidth() ||
            img1.getHeight() != img2.getHeight()) {
            throw new IllegalArgumentException(
                "Images have different width or height");
        }
        for (int i = 0; i < img1.getWidth(); i++) {
            for (int j = 0; j < img1.getHeight(); j++) {
                boolean cmp = compare(img1.getRGB(i,j), img2.getRGB(i,j));
                result = cmp && result;
            }
        }
        return result;
    }
    public void resetStat() {
        errorCounter = 0;
        maxError = 0;
    }
    public String getStat() {
        return "Accuracy " + accuracy + ". Errors " + errorCounter +
               ". Max error " + maxError;
    }
    boolean compare(BufferedImage dst, BufferedImage gldImage, int x0, int y0,
                    int dx, int dy)
    {
        int width = gldImage.getWidth();
        int height = gldImage.getHeight();
        if (x0 < 0) x0 = 0;
        if (x0 > width - dx) x0 = width - dx;
        if (y0 < 0) y0 = 0;
        if (y0 > height - dy) y0 = height - dy;
        int c = 0;
        boolean result = true;
        for (int i = x0; i < x0 + dx; i++) {
            for (int j = y0; j < y0 + dy; j++) {
                boolean cmp = compare(dst.getRGB(i-x0,j-y0),
                                      gldImage.getRGB(i,j));
                result = cmp && result;
            }
        }
        return result;
    }
}
