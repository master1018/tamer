public class StringWidth extends Frame {
  public StringWidth() {
    Font plain = new Font("Dialog", Font.PLAIN, 10);
    Font bold = new Font("Dialog", Font.BOLD, 10);
    Properties props = new Properties();
    int x, y;
    setSize(400, 300);
    setVisible(true);
    PrintJob pj = getToolkit().getPrintJob(this, "", props);
    if (pj == null) {
        return;
    }
    Graphics  pg = pj.getGraphics();
    String test = "Hello World!";
    FontMetrics plainFm = pg.getFontMetrics(plain);
    FontMetrics boldFm = pg.getFontMetrics(bold);
    Dimension size = pj.getPageDimension();
    int center = size.width/2;
    y = 150;
    x = center - plainFm.stringWidth(test);
    pg.setFont(plain);
    pg.drawString(test, x-1, y);
    pg.dispose();
    pj.end();
    setVisible(false);
  }
  public static void main(String[] args) {
    StringWidth sw = new StringWidth();
    sw.dispose();
  }
}
