public class JoinMiterTest {
  public static void main(String[] args) throws Exception {
    BufferedImage image = new BufferedImage(200, 200,
BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();
    g.setPaint(Color.WHITE);
    g.fill(new Rectangle(image.getWidth(), image.getHeight()));
    g.translate(25, 100);
    g.setPaint(Color.BLACK);
    g.setStroke(new BasicStroke(20, BasicStroke.CAP_BUTT,
                                BasicStroke.JOIN_MITER));
    g.draw(new Polygon(new int[] {0, 150, 0}, new int[] {75, 0, -75}, 3));
    if (image.getRGB(16, 10) == Color.WHITE.getRGB()) {
      throw new RuntimeException("Miter is not rendered.");
    }
  }
}
