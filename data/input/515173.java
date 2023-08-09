public class FloatDoubleTest extends TestCase {
    @SmallTest
    public void testFloatDouble() throws Exception {
        Double d = Double.valueOf(1.0);
        Float f = Float.valueOf(1.0f);
        Object o = new Object();
        assertFalse(f.equals(d));
        assertFalse(d.equals(f));
        assertFalse(f.equals(o));
        assertFalse(d.equals(o));
        assertFalse(f.equals(null));
        assertFalse(d.equals(null));
    }
    @SmallTest
    public void testFloat() throws Exception {
        float pz = 0.0f;
        float nz = -0.0f;
        float pzero = 1.0f / Float.POSITIVE_INFINITY;
        float nzero = 1.0f / Float.NEGATIVE_INFINITY;
        assertTrue(pz == pz);
        assertTrue(pz == nz);
        assertTrue(pz == pzero);
        assertTrue(pz == nzero);
        assertTrue(nz == pz);
        assertTrue(nz == nz);
        assertTrue(nz == pzero);
        assertTrue(nz == nzero);
        assertTrue(pzero == pz);
        assertTrue(pzero == nz);
        assertTrue(pzero == pzero);
        assertTrue(pzero == nzero);
        assertTrue(nzero == pz);
        assertTrue(nzero == nz);
        assertTrue(nzero == pzero);
        assertTrue(nzero == nzero);
        assertEquals(Float.valueOf(pz), Float.valueOf(pz));
        assertTrue(!Float.valueOf(pz).equals(Float.valueOf(nz)));
        assertEquals(Float.valueOf(pz), Float.valueOf(pzero));
        assertTrue(!Float.valueOf(pz).equals(Float.valueOf(nzero)));
        assertTrue(!Float.valueOf(nz).equals(Float.valueOf(pz)));
        assertEquals(Float.valueOf(nz), Float.valueOf(nz));
        assertTrue(!Float.valueOf(nz).equals(Float.valueOf(pzero)));
        assertEquals(Float.valueOf(nz), Float.valueOf(nzero));
        assertEquals(Float.valueOf(pzero), Float.valueOf(pz));
        assertTrue(!Float.valueOf(pzero).equals(Float.valueOf(nz)));
        assertEquals(Float.valueOf(pzero), Float.valueOf(pzero));
        assertTrue(!Float.valueOf(pzero).equals(Float.valueOf(nzero)));
        assertTrue(!Float.valueOf(nzero).equals(Float.valueOf(pz)));
        assertEquals(Float.valueOf(nzero), Float.valueOf(nz));
        assertTrue(!Float.valueOf(nzero).equals(Float.valueOf(pzero)));
        assertEquals(Float.valueOf(nzero), Float.valueOf(nzero));
        Float sqrtm2 = Float.valueOf((float) Math.sqrt(-2.0f));
        Float sqrtm3 = Float.valueOf((float) Math.sqrt(-3.0f));
        assertEquals(sqrtm2, sqrtm3);
    }
    @SmallTest
    public void testDouble() throws Exception {
        double pz = 0.0;
        double nz = -0.0;
        double pzero = 1.0 / Double.POSITIVE_INFINITY;
        double nzero = 1.0 / Double.NEGATIVE_INFINITY;
        assertTrue(pz == pz);
        assertTrue(pz == nz);
        assertTrue(pz == pzero);
        assertTrue(pz == nzero);
        assertTrue(nz == pz);
        assertTrue(nz == nz);
        assertTrue(nz == pzero);
        assertTrue(nz == nzero);
        assertTrue(pzero == pz);
        assertTrue(pzero == nz);
        assertTrue(pzero == pzero);
        assertTrue(pzero == nzero);
        assertTrue(nzero == pz);
        assertTrue(nzero == nz);
        assertTrue(nzero == pzero);
        assertTrue(nzero == nzero);
        assertEquals(Double.valueOf(pz), Double.valueOf(pz));
        assertTrue(!Double.valueOf(pz).equals(Double.valueOf(nz)));
        assertEquals(Double.valueOf(pz), Double.valueOf(pzero));
        assertTrue(!Double.valueOf(pz).equals(Double.valueOf(nzero)));
        assertTrue(!Double.valueOf(nz).equals(Double.valueOf(pz)));
        assertEquals(Double.valueOf(nz), Double.valueOf(nz));
        assertTrue(!Double.valueOf(nz).equals(Double.valueOf(pzero)));
        assertEquals(Double.valueOf(nz), Double.valueOf(nzero));
        assertEquals(Double.valueOf(pzero), Double.valueOf(pz));
        assertTrue(!Double.valueOf(pzero).equals(Double.valueOf(nz)));
        assertEquals(Double.valueOf(pzero), Double.valueOf(pzero));
        assertTrue(!Double.valueOf(pzero).equals(Double.valueOf(nzero)));
        assertTrue(!Double.valueOf(nzero).equals(Double.valueOf(pz)));
        assertEquals(Double.valueOf(nzero), Double.valueOf(nz));
        assertTrue(!Double.valueOf(nzero).equals(Double.valueOf(pzero)));
        assertEquals(Double.valueOf(nzero), Double.valueOf(nzero));
        Double sqrtm2 = Double.valueOf(Math.sqrt(-2.0));
        Double sqrtm3 = Double.valueOf(Math.sqrt(-3.0));
        assertEquals(sqrtm2, sqrtm3);
    }
}
