public class BoldSpace {
    public static void main(String[] s) {
        BufferedImage bi = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        String errMsg = "ZWJ Space char should have 0 advance\n";
        Graphics g = bi.getGraphics();
        int errorMargin = 4;
        g.setFont(new Font("monospaced", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        System.out.println("Bold: " + fm.charWidth('\u200b'));
        int cwid = fm.charWidth('\u200b');
        if (cwid > 0 && cwid < errorMargin) {
            throw new RuntimeException(errMsg);
        }
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                 RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        fm = g.getFontMetrics();
        System.out.println("Bold + LCD: "+fm.charWidth('\u200b'));
        cwid = fm.charWidth('\u200b');
        if (cwid > 0 && cwid < errorMargin) {
            throw new RuntimeException(errMsg);
        }
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                                 RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                 RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        fm = g.getFontMetrics();
        System.out.println("Bold FM OFF + AA: " + fm.charWidth('\u200b'));
        cwid = fm.charWidth('\u200b');
        if (cwid > 0 && cwid < errorMargin) {
            throw new RuntimeException(errMsg);
        }
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                                 RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                 RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        fm = g.getFontMetrics();
        System.out.println("Bold FM ON + AA: " + fm.charWidth('\u200b'));
        cwid = fm.charWidth('\u200b');
        if (cwid > 0 && cwid < errorMargin) {
            throw new RuntimeException(errMsg);
        }
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                                 RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                 RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        fm = g.getFontMetrics();
        System.out.println("Bold FM ON + nonAA: " + fm.charWidth('\u200b'));
        cwid = fm.charWidth('\u200b');
        if (cwid > 0 && cwid < errorMargin) {
            throw new RuntimeException(errMsg);
        }
        System.out.println("All printed values should be 0 to PASS");
    }
}
