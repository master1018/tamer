@TestTargetClass(ScaleToFit.class)
public class Matrix_ScaleToFitTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "values",
        args = {}
    )
    public void testValues() {
        ScaleToFit[] scaleToFits = ScaleToFit.values();
        assertEquals(ScaleToFit.FILL,scaleToFits[0]);
        assertEquals( ScaleToFit.START,scaleToFits[1]);
        assertEquals( ScaleToFit.CENTER,scaleToFits[2]);
        assertEquals( ScaleToFit.END,scaleToFits[3]);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf() {
        assertEquals(ScaleToFit.FILL,ScaleToFit.valueOf("FILL"));
        assertEquals( ScaleToFit.START,ScaleToFit.valueOf("START"));
        assertEquals( ScaleToFit.CENTER,ScaleToFit.valueOf("CENTER"));
        assertEquals(ScaleToFit.END,ScaleToFit.valueOf("END") );
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "valueOf",
        args = {android.graphics.Matrix.ScaleToFit.class, java.lang.String.class}
    )
    public void testValueOf2() {
        assertEquals(ScaleToFit.FILL, ScaleToFit.valueOf(ScaleToFit.class,
                "FILL"));
        assertEquals(ScaleToFit.START, ScaleToFit.valueOf(ScaleToFit.class,
                "START"));
        assertEquals(ScaleToFit.CENTER, ScaleToFit.valueOf(ScaleToFit.class,
                "CENTER"));
        assertEquals(ScaleToFit.END, ScaleToFit
                .valueOf(ScaleToFit.class, "END"));
    }
}
