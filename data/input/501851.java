@TestTargetClass(BlurMaskFilter.Blur.class)
public class BlurMaskFilter_BlurTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf(){
        assertEquals(Blur.NORMAL, Blur.valueOf("NORMAL"));
        assertEquals(Blur.SOLID, Blur.valueOf("SOLID"));
        assertEquals(Blur.OUTER, Blur.valueOf("OUTER"));
        assertEquals(Blur.INNER, Blur.valueOf("INNER"));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "values",
        args = {}
    )
    public void testValues(){
        Blur[] bulr = Blur.values();
        assertEquals(4, bulr.length);
        assertEquals(Blur.NORMAL, bulr[0]);
        assertEquals(Blur.SOLID, bulr[1]);
        assertEquals(Blur.OUTER, bulr[2]);
        assertEquals(Blur.INNER, bulr[3]);
        assertNotNull(new BlurMaskFilter(10.24f, Blur.INNER));
        assertNotNull(new BlurMaskFilter(10.24f, Blur.NORMAL));
        assertNotNull(new BlurMaskFilter(10.24f, Blur.OUTER));
        assertNotNull(new BlurMaskFilter(10.24f, Blur.SOLID));
    }
}
