public class Shear extends Component {
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().add("Center", new Shear());
        f.pack();
        f.setVisible(true);
    }
    public Dimension getPreferredSize() {
      return new Dimension(400,300);
    }
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g.setColor(Color.white);
        g.fillRect(0,0,400,300);
        g.setColor(Color.black);
        Font origFont = new Font(Font.DIALOG, Font.BOLD, 30);
        for (int i=0;i<=360;i++) {
            double sv = i*180.0/Math.PI;
            AffineTransform tx = AffineTransform.getShearInstance(sv, sv);
            Font font = origFont.deriveFont(tx);
            g.setFont(font);
            GlyphVector gv =
                  font.createGlyphVector(g2.getFontRenderContext(), "JavaFX");
            g.drawString("JavaFX", 100, 100);
        }
    }
}
