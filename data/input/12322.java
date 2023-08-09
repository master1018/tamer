public class Test6849805 {
    static boolean pass = true;
    static class Minimbus extends javax.swing.plaf.nimbus.NimbusLookAndFeel {
        public void test(Color c1, Color c2, float f) {
            Color r = getDerivedColor(c1, c2, f);
            Color test = (f > 0 ? c2 : c1);
            System.out.printf("Got %s, need %s ", r, test);
            if (r.getRGB() == test.getRGB() &&
                r.getAlpha() == test.getAlpha()) {
                System.out.println("Ok");
            } else {
                System.out.println("FAIL");
                pass = false;
            }
        }
    }
    public static void main(String[] args) {
        Minimbus laf = new Minimbus();
        laf.test(Color.WHITE, Color.BLACK, 0f);
        laf.test(Color.WHITE, Color.BLACK, 1f);
        laf.test(Color.BLACK, Color.WHITE, 0f);
        laf.test(Color.BLACK, Color.WHITE, 1f);
        laf.test(Color.RED, Color.GREEN, 0f);
        laf.test(Color.RED, Color.GREEN, 1f);
        laf.test(new Color(127, 127, 127), new Color(51, 151, 212), 0f);
        laf.test(new Color(127, 127, 127), new Color(51, 151, 212), 1f);
        laf.test(new Color(221, 63, 189), new Color(112, 200, 89), 0f);
        laf.test(new Color(221, 63, 189), new Color(112, 200, 89), 1f);
        if (! pass) {
            throw new RuntimeException("Some testcases failed, see above");
        }
    }
}
