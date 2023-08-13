@TestTargetClass(Canvas.VertexMode.class)
public class Canvas_VertexModeTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "valueOf",
        args = {java.lang.String.class}
    )
    public void testValueOf(){
        assertEquals(VertexMode.TRIANGLES, VertexMode.valueOf("TRIANGLES"));
        assertEquals(VertexMode.TRIANGLE_STRIP, VertexMode.valueOf("TRIANGLE_STRIP"));
        assertEquals(VertexMode.TRIANGLE_FAN, VertexMode.valueOf("TRIANGLE_FAN"));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "values",
            args = {}
        )
    })
    public void testValues(){
        VertexMode[] verMode = VertexMode.values();
        assertEquals(3, verMode.length);
        assertEquals(VertexMode.TRIANGLES, verMode[0]);
        assertEquals(VertexMode.TRIANGLE_STRIP, verMode[1]);
        assertEquals(VertexMode.TRIANGLE_FAN, verMode[2]);
        float[] verts = new float[10];
        float[] texs = new float[10];
        short[] indices = { 0, 1, 2, 3, 4, 1 };
        Bitmap bitmap = Bitmap.createBitmap(10, 27, Config.RGB_565);
        Canvas c = new Canvas(bitmap);
        c.drawVertices( VertexMode.TRIANGLES,
                        2,
                        verts,
                        0,
                        texs,
                        0,
                        new int[]{10, 24},
                        0,
                        indices,
                        0,
                        4,
                        new Paint());
        c.drawVertices( VertexMode.TRIANGLE_STRIP,
                        2,
                        verts,
                        0,
                        texs,
                        0,
                        new int[]{10, 24},
                        0,
                        indices,
                        0,
                        4,
                        new Paint());
        c.drawVertices( VertexMode.TRIANGLE_FAN,
                        10,
                        verts,
                        0,
                        texs,
                        0,
                        null,
                        0,
                        null,
                        0,
                        0,
                        new Paint());
    }
}
