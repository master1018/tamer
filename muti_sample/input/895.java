public class ScaleTest {
  public static void main(String[] args) throws Exception {
    BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g.setPaint(Color.WHITE);
    g.fill(new Rectangle(image.getWidth(), image.getHeight()));
    g.scale(.9, .9);
    g.setPaint(Color.BLACK);
    g.setStroke(new BasicStroke(0.5f));
    g.draw(new Ellipse2D.Double(25, 25, 150, 150));
    boolean nonWhitePixelFound = false;
    for (int x = 100; x < 200; ++x) {
      if (image.getRGB(x, 90) != Color.WHITE.getRGB()) {
        nonWhitePixelFound = true;
        break;
      }
    }
    if (!nonWhitePixelFound) {
      throw new RuntimeException("A circle is rendered like a 'C' shape.");
    }
  }
}
