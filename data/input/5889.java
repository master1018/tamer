public class Test6855215 {
    private double m;
    private double b;
    public static double log10(double x) {
        return Math.log(x) / Math.log(10);
    }
    void calcMapping(double xmin, double xmax, double ymin, double ymax) {
        m = (ymax - ymin) / (log10(xmax) - log10(xmin));
        b = (log10(xmin) * ymax - log10(xmax) * ymin);
    }
    public static void main(String[] args) {
        Test6855215 c = new Test6855215();
        for (int i = 0; i < 30000; i++) {
            c.calcMapping(91, 121, 177, 34);
            if (c.m != c.m) {
                throw new InternalError();
            }
        }
    }
}
