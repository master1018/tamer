public class TranslatedOutlineTest {
   public static void main(String a[]) {
        BufferedImage bi = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) bi.getGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, 50, 50);
        FontRenderContext frc = new FontRenderContext(null, false, false);
        g2.setColor(Color.RED);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        GlyphVector gv = g2.getFont().createGlyphVector(frc, "test");
        g2.fill(gv.getOutline(20, 20));
        int bgcolor = Color.WHITE.getRGB();
        for (int i=0; i<bi.getWidth(); i++) {
            for(int j=0; j<bi.getHeight(); j++) {
               if (bi.getRGB(i, j) != bgcolor) {
                   System.out.println("Test passed.");
                   return;
               }
            }
        }
        throw new RuntimeException("Outline was not detected");
    }
}
