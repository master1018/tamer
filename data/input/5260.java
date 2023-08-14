public class FRCTest {
    static AttributedString vanGogh = new AttributedString(
        "Many people believe that Vincent van Gogh painted his best works " +
        "during the two-year period he spent in Provence. Here is where he " +
        "painted The Starry Night--which some consider to be his greatest " +
        "work of all. However, as his artistic brilliance reached new " +
        "heights in Provence, his physical and mental health plummeted. ",
        new Hashtable());
    public static void main(String[] args) {
        BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bi.createGraphics();
        AffineTransform g2dTx = new AffineTransform(2,0,2,0,1,1);
        g2d.setTransform(g2dTx);
        AffineTransform frcTx = g2d.getFontRenderContext().getTransform();
        AffineTransform frcExpected = new AffineTransform(2,0,2,0,0,0);
        if (!frcTx.equals(frcExpected)) {
            throw new RuntimeException("FRC Tx may have translate?");
        }
        for (int x=0;x<100;x++) {
            for (int y=0;y<100;y++) {
                AttributedCharacterIterator aci = vanGogh.getIterator();
                AffineTransform tx = AffineTransform.getTranslateInstance(x, y);
                FontRenderContext frc = new FontRenderContext(tx, false, false);
                LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
                lbm.setPosition(aci.getBeginIndex());
                while (lbm.getPosition() < aci.getEndIndex()) {
                    lbm.nextLayout(100f);
                }
            }
        }
        for (int x=0;x<25;x++) {
            for (int y=0;y<25;y++) {
                AttributedCharacterIterator aci = vanGogh.getIterator();
                double rot = Math.random()*.4*Math.PI - .2*Math.PI;
                AffineTransform tx = AffineTransform.getRotateInstance(rot);
                FontRenderContext frc = new FontRenderContext(tx, false, false);
                LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
                lbm.setPosition(aci.getBeginIndex());
                while (lbm.getPosition() < aci.getEndIndex()) {
                    lbm.nextLayout(100f);
                }
            }
        }
    }
}
