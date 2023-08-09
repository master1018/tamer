class LineGestureStrokeHelper {
    final static float LINE_START_POINT = 0;
    final static int START_TIMESTAMP = 0;
    final static float LINE_END_POINT = 20;
    final static float LINE_MIDWAY_POINT = LINE_END_POINT/2;
    final static float LINE_QUARTER_POINT = LINE_MIDWAY_POINT/2;
    final static int END_TIMESTAMP = 1;
    final static float LINE_ANGLE = 45;
    GestureStroke createLineGesture() {
        return createGestureStroke(
                new GesturePoint(LINE_START_POINT, LINE_START_POINT, START_TIMESTAMP),
                new GesturePoint(LINE_END_POINT, LINE_END_POINT, END_TIMESTAMP));
    }
    void assertLineBoundingBox(Path linePath) {
        Assert.assertFalse(linePath.isEmpty());
        RectF bounds = new RectF();
        linePath.computeBounds(bounds, true);
        Assert.assertEquals(LINE_MIDWAY_POINT, bounds.bottom);
        Assert.assertEquals(LINE_START_POINT, bounds.left);
        Assert.assertEquals(LINE_MIDWAY_POINT, bounds.right);
        Assert.assertEquals(LINE_START_POINT, bounds.top);
    }
    void assertLineBoundingBox(RectF bounds) {
        Assert.assertEquals(LINE_END_POINT, bounds.bottom);
        Assert.assertEquals(LINE_START_POINT, bounds.left);
        Assert.assertEquals(LINE_END_POINT, bounds.right);
        Assert.assertEquals(LINE_START_POINT, bounds.top);
    }
    GestureStroke createGestureStroke(GesturePoint... points) {
        ArrayList<GesturePoint> list = new ArrayList<GesturePoint>(points.length);
        for (GesturePoint point : points) {
            list.add(point);
        }
        return new GestureStroke(list);
    }
}
