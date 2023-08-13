public class Test7027667 {
    public static void main(String[] args) throws Exception {
        BufferedImage bImg = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) bImg.getGraphics();
        g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g2d.setClip(new Ellipse2D.Double(0, 0, 100, 100));
        g2d.drawRect(10, 10, 100, 100);
        if (new Color(bImg.getRGB(50, 50)).equals(Color.white)) {
            throw new Exception("Rectangle should be drawn, not filled");
        }
    }
}
