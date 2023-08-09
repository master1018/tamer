public class LCDTextAndGraphicsState extends Component {
    String text = "This test passes only if this text appears SIX TIMES";
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g.create();
        g2d.setColor(Color.white);
        g2d.fillRect(0,0,getSize().width, getSize().height);
        test1(g.create(0,   0, 500, 200));
        test2(g.create(0, 200, 500, 200));
        test3(g.create(0, 400, 500, 200));
    }
    public void test1(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setColor(Color.black);
        g2d.drawString(text, 10, 50);
        g2d.setComposite(AlphaComposite.getInstance(
                         AlphaComposite.SRC_OVER, 0.9f));
        g2d.drawString(text, 10, 80);
    }
    public void test2(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setColor(Color.black);
        g2d.drawString(text, 10, 50);
        g2d.setPaint(new GradientPaint(
                     0f, 0f, Color.BLACK, 100f, 100f, Color.GRAY));
        g2d.drawString(text, 10, 80);
    }
    public void test3(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                             RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setColor(Color.black);
        g2d.drawString(text, 10, 50);
        Shape s = new RoundRectangle2D.Double(0, 60, 400, 50, 5, 5);
        g2d.clip(s);
        g2d.drawString(text, 10, 80);
    }
    public Dimension getPreferredSize() {
        return new Dimension(500,600);
    }
    public static void main(String[] args) throws Exception {
        Frame f = new Frame("Composite and Text Test");
        f.add(new LCDTextAndGraphicsState(), BorderLayout.CENTER);
        f.pack();
        f.setVisible(true);
    }
}
