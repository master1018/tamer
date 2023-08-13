public class ScaledLCDTextMetrics extends Component {
    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add("Center", new ScaledLCDTextMetrics());
        f.pack();
        f.setVisible(true);
    }
    public Dimension getPreferredSize() {
      return new Dimension(200,100);
    }
    public void paint(Graphics g) {
       Graphics2D g2 = (Graphics2D)g;
       Font f = new Font("Tahoma", Font.PLAIN, 11);
       g.setFont(f);
       g.setColor(Color.white);
       g.fillRect(0,0,400,300);
       g.setColor(Color.black);
       g2.setRenderingHint(KEY_TEXT_ANTIALIASING,VALUE_TEXT_ANTIALIAS_LCD_HRGB);
       String text = "ABCDEFGHIJKLI";
       FontMetrics fm1 = g2.getFontMetrics();
       int adv1 = fm1.stringWidth(text);
       g.drawString(text, 5, 20);
       g2.scale(2,2);
       FontMetrics fm2 = g2.getFontMetrics();
       int adv2 = fm2.stringWidth(text);
       g.drawString(text, 5, 40);
       double frac = Math.abs(adv1/(double)adv2);
       System.out.println("scalex1: " + adv1);
       System.out.println("scalex2: " + adv2);
       System.out.println("Fraction : "+ frac);
       if (frac < 0.8 || frac > 1.2) {
           throw new RuntimeException("Metrics differ " +
           "Adv1="+adv1+" Adv2="+adv2+" Fraction="+frac);
       }
    }
}
