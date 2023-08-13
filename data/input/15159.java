public class KernCrash extends Frame {
    private static Font font0;
    private static Font font1;
    private static Font font2;
    public static void main(String[] args) throws Exception {
        HashMap attrs = new HashMap();
        font0 = Font.createFont(Font.TRUETYPE_FONT, new File("Vera.ttf"));
        System.out.println("using " + font0);
        attrs.put(TextAttribute.SIZE, new Float(58f));
        font1 = font0.deriveFont(attrs);
        attrs.put(TextAttribute.KERNING, TextAttribute.KERNING_ON);
        font2 = font0.deriveFont(attrs);
        KernCrash f = new KernCrash();
        f.setTitle("Kerning Crash");
        f.setSize(600, 300);
        f.setForeground(Color.black);
        f.show();
    }
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        FontRenderContext frc = g2.getFontRenderContext();
        TextLayout layout = new TextLayout("text", font2, frc);
        layout.draw(g2, 10, 150);
        String s = "WAVATastic";
        TextLayout layout2 = new TextLayout(s, font1, frc);
        layout2.draw(g2, 10, 200);
        TextLayout layout3 = new TextLayout(s, font2, frc);
        layout3.draw(g2, 10, 100);
    }
}
