public class BigMetrics {
  public static void main(String args[]) {
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    Font[] fonts = ge.getAllFonts();
    Toolkit tk = Toolkit.getDefaultToolkit();
    for (int i=0; i<fonts.length;i++) {
       Font f = fonts[i].deriveFont(240f);
       FontMetrics fm = tk.getFontMetrics(f);
       int sw = fm.stringWidth("{}[]Hello world!");
    }
  }
}
