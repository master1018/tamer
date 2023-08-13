public class LCDTextSrcEa extends Component {
    static int SZ=150;
    BufferedImage target =
        new BufferedImage(SZ, SZ, BufferedImage.TYPE_INT_RGB);
    public static void main(String args[]) {
        Frame f = new Frame("LCD Text SrcEa Test");
        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        LCDTextSrcEa td = new LCDTextSrcEa();
        f.add("Center", td);
        f.pack();
        f.setVisible(true);
    }
    public Dimension getPreferredSize() {
        return new Dimension(SZ,SZ);
    }
    public void paint(Graphics gx) {
        Graphics2D g2d = (Graphics2D) target.getGraphics();
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.01f));
        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR);
        g2d.setRenderingHint(
               RenderingHints.KEY_ANTIALIASING,
               RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.black);
        g2d.drawString("Some sample text.", 10, 20);
        gx.drawImage(target, 0, 0, null);
        boolean nongrey = false;
        for (int px=0;px<SZ;px++) {
            for (int py=0;py<SZ;py++) {
                int rgb = target.getRGB(px, py);
                int r = (rgb & 0xff0000) >> 16;
                int g = (rgb & 0x00ff00) >> 8;
                int b = (rgb & 0x0000ff);
                if (r != g || r !=b || g != b) {
                     nongrey=true;
                     break;
                }
            }
        }
        if (!nongrey) {
            throw new RuntimeException("No LCD text found");
        }
    }
}
