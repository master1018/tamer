public class ContainsTest {
    public static void main(String[] args) throws Exception {
        CubicCurve2D c = new CubicCurve2D.Double(0, 0, 4, -4, -2, -4, 2, 0);
        Rectangle2D r = new Rectangle2D.Double(0.75, -2.5, 0.5, 2);
        if (c.contains(r)) {
            throw new Exception("The rectangle should not be contained in the curve");
        }
    }
}
