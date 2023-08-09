@TestTargetClass(GesturePoint.class)
public class GesturePointTest extends TestCase {
   public void testGesturePointConstructor() {
        final float expected_x = 1.1f;
        final float expected_y = 1.1f;
        final long expected_timestamp = 1;
        GesturePoint point = new GesturePoint(expected_x, expected_y, expected_timestamp);
        assertEquals(expected_x, point.x);
        assertEquals(expected_y, point.y);
        assertEquals(expected_timestamp, point.timestamp);
    }
}
