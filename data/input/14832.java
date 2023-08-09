public class TestTransform {
    public static void testTransformedFont(AffineTransform a, Object textHint) {
        BufferedImage bi = new BufferedImage(200, 200,
                                   BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) bi.getGraphics();
        g2.setFont(g2.getFont().deriveFont(12.0f));
        g2.setTransform(a);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, textHint);
        g2.drawString("test", 100, 100);
    }
    public static void testFontOfSize(float sz, Object textHint) {
        BufferedImage bi = new BufferedImage(200, 200,
                                   BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) bi.getGraphics();
        g2.setFont(g2.getFont().deriveFont(sz));
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, textHint);
        g2.drawString("test", 100, 100);
    }
    public static void main(String[] args) {
        Object aahints[] = {RenderingHints.VALUE_TEXT_ANTIALIAS_OFF,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB};
        int i, j, k;
        AffineTransform a = new AffineTransform();
        for (i=0; i<aahints.length; i++) {
            for(j=0; j<8; j++) {
                System.out.println("Testing hint "+i+" angle="+j);
                a.setToRotation(j*Math.PI/4);
                testTransformedFont(a, aahints[i]);
            }
            testFontOfSize(0.0f, aahints[i]);
            testFontOfSize(-10.0f, aahints[i]);
        }
    }
}
