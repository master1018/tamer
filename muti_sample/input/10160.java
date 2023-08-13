public class IntersectsTest {
    public static void main(String[] args) throws Exception {
        CubicCurve2D c = new CubicCurve2D.Double(50.0, 300.0,
                                                 150.0, 166.6666717529297,
                                                 238.0, 456.66668701171875,
                                                 350.0, 300.0);
        Rectangle2D r = new Rectangle2D.Double(260, 300, 10, 10);
        if (!c.intersects(r)) {
            throw new Exception("The rectangle is contained. " +
                                "intersects(Rectangle2D) should return true");
        }
    }
}
