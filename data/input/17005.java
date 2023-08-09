public class DecorationBoundsTest {
    public static void main(String[] args) {
        BufferedImage bi =
           new BufferedImage(600, 300, BufferedImage.TYPE_INT_RGB);
       Graphics2D g2d = bi.createGraphics();
       g2d.setColor(Color.white);
       g2d.fillRect(0, 0, 600, 300);
       float x = 10;
       float y = 90;
       Map map = new HashMap();
       map.put(TextAttribute.STRIKETHROUGH,
               TextAttribute.STRIKETHROUGH_ON);
       map.put(TextAttribute.SIZE, new Float(80));
       FontRenderContext frc = g2d.getFontRenderContext();
       String text = "Welcome to ";
       TextLayout tl = new TextLayout(text, map, frc);
       g2d.translate(x, y);
       g2d.setColor(Color.RED);
       tl.draw(g2d, 0, 0);
       g2d.setColor(Color.GREEN);
       Rectangle2D bds = tl.getBounds();
       bds.setRect(bds.getX(), bds.getY()-1,
                   bds.getWidth(), bds.getHeight()+2);
       g2d.fill(bds);
       map = new HashMap();
       map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
       map.put(TextAttribute.SIZE, new Float(80));
       tl = new TextLayout(text, map, frc);
       g2d.translate(0, 100);
       g2d.setColor(Color.RED);
       tl.draw(g2d, 0, 0);
       g2d.setColor(Color.GREEN);
       bds = tl.getBounds();
       bds.setRect(bds.getX(), bds.getY()-1,
                   bds.getWidth(), bds.getHeight()+2);
       g2d.fill(bds);
       checkBI(bi, Color.RED);
   }
   static void checkBI(BufferedImage bi, Color badColor) {
      int badrgb = badColor.getRGB();
      int w = bi.getWidth(null);
      int h = bi.getHeight(null);
      for (int x=0; x<w; x++) {
          for (int y=0; y<h; y++) {
             int col = bi.getRGB(x, y);
             if (col == badrgb) {
                  throw new RuntimeException("Got " + col);
             }
          }
      }
   }
}
