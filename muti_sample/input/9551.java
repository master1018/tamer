public class Test7019861 {
    public static void main(String[] argv) throws Exception {
        BufferedImage im = getWhiteImage(30, 30);
        Graphics2D g2 = (Graphics2D)im.getGraphics();
        g2.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(KEY_STROKE_CONTROL, VALUE_STROKE_PURE);
        g2.setStroke(new BasicStroke(10, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        g2.setBackground(Color.white);
        g2.setColor(Color.black);
        Path2D p = getPath(0, 0, 20);
        g2.draw(p);
        if (!(new Color(im.getRGB(20, 19))).equals(Color.black)) {
            throw new Exception("This pixel should be black");
        }
    }
    private static Path2D getPath(int x, int y, int len) {
        Path2D p = new Path2D.Double();
        p.moveTo(x, y);
        p.quadTo(x + len, y, x + len, y + len);
        return p;
    }
    private static BufferedImage getWhiteImage(int w, int h) {
        BufferedImage ret = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        final int[] white = new int[w * h];
        Arrays.fill(white, 0xffffff);
        ret.setRGB(0, 0, w, h, white, 0, w);
        return ret;
    }
}
