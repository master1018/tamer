public class TestUtils extends TestCase  {
    public static byte[] b(int... array) {
        if (array == null) {
            return null;
        }
        byte[] ret = new byte[array.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (byte) array[i];
        }
        return ret;
    }
    public void testB() {
        assertNull(b(null));
        MoreAsserts.assertEquals(new byte[] {}, b());
        MoreAsserts.assertEquals(new byte[] {1, 2, (byte) 0xff}, b(1, 2, 0xff));
    }
}
