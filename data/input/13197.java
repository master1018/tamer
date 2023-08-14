public class Test7036754 {
    public static void main(String argv[]) {
        Shape s = new QuadCurve2D.Float(839.24677f, 508.97888f,
                                        839.2953f, 508.97122f,
                                        839.3438f, 508.96353f);
        s = new BasicStroke(10f).createStrokedShape(s);
        float nsegs[] = {2, 2, 4, 6, 0};
        float coords[] = new float[6];
        PathIterator pi = s.getPathIterator(null);
        while (!pi.isDone()) {
            int type = pi.currentSegment(coords);
            for (int i = 0; i < nsegs[type]; i++) {
                float c = coords[i];
                if (Float.isNaN(c) || Float.isInfinite(c)) {
                    throw new RuntimeException("bad value in stroke");
                }
            }
            pi.next();
        }
    }
}
