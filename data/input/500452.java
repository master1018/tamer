public class FloatMathTest extends TestCase {
    @SmallTest
    public void testSqrt() {
        assertEquals(7, FloatMath.sqrt(49), 0);
        assertEquals(10, FloatMath.sqrt(100), 0);
        assertEquals(0, FloatMath.sqrt(0), 0);
        assertEquals(1, FloatMath.sqrt(1), 0);
    }
    @SmallTest
    public void testFloor() {
        assertEquals(78, FloatMath.floor(78.89f), 0);
        assertEquals(-79, FloatMath.floor(-78.89f), 0);
    }
    @SmallTest
    public void testCeil() {
        assertEquals(79, FloatMath.ceil(78.89f), 0);
        assertEquals(-78, FloatMath.ceil(-78.89f), 0);
    }
    @SmallTest
    public void testSin() {
        assertEquals(0.0, FloatMath.sin(0), 0);
        assertEquals(0.8414709848078965f, FloatMath.sin(1), 0);
    }
    @SmallTest
    public void testCos() {
        assertEquals(1.0f, FloatMath.cos(0), 0);
        assertEquals(0.5403023058681398f, FloatMath.cos(1), 0);
    }
}
