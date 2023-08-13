public class LCDScale extends Component {
    public static void main(String args[]) {
        Frame f = new Frame("TL TEST");
        LCDScale td = new LCDScale();
        f.add("Center", td);
        f.pack(); f.setVisible(true);
    }
    public LCDScale() {
        super();
    }
    public Dimension getPreferredSize() {
        return new Dimension(500,500);
    }
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(
                 RenderingHints.KEY_TEXT_ANTIALIASING,
                 RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        Font f = new Font("Dialog", Font.PLAIN, 40);
        g.setFont(f);
        FontRenderContext frc = g2d.getFontRenderContext();
        GlyphVector gv = f.createGlyphVector(frc, "Help");
        g2d.drawGlyphVector(gv, 10f, 50f);
        Rectangle2D bds1 = gv.getLogicalBounds();
        f = new Font("Arial", Font.PLAIN, 25);
        g.setFont(f);
        double s = 2.0;
        AffineTransform tx = AffineTransform.getScaleInstance(s,s);
        g2d.transform(tx);
        frc = g2d.getFontRenderContext();
        gv = f.createGlyphVector(frc, "Help");
        g2d.drawGlyphVector(gv, 10f, 100f);
        Rectangle2D bds2 = gv.getLogicalBounds();
        System.out.println(bds1);
        System.out.println(bds2);
        if (bds2.getWidth()*s < bds1.getWidth()) {
            throw new RuntimeException("Bounds too small");
        }
    }
}
