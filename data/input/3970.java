public class DashStrokeTest extends Component {
    static BufferedImage bi;
    static boolean printed = false;
    public Dimension getPreferredSize() {
      return new Dimension(200,200);
    }
    public static void drawGui() {
        bi = new BufferedImage(200, 20, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bi.createGraphics();
        BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] { 0.0f, 200 },
                1.0f);
        g2d.setStroke(dashStroke);
        g2d.setColor(Color.RED);
        g2d.drawLine(5,10, 100,10);
        printed =true;
    }
    public static void main(String[] args) {
            try {
            SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                drawGui();
            }
            });
            } catch (Exception e) {
            }
            if (printed) {
                checkBI(bi, Color.RED);
            }
    }
    static void checkBI(BufferedImage bi, Color badColor) {
      int badrgb = badColor.getRGB();
      int col = bi.getRGB(6, 9);
      if (col == badrgb) {
          throw new RuntimeException("A pixel was turned on. ");
      }
   }
}
