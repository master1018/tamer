@TestTargetClass(LayerRasterizer.class)
public class LayerRasterizerTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "LayerRasterizer",
        args = {}
    )
    public void testConstructor() {
        new LayerRasterizer();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addLayer",
        args = {android.graphics.Paint.class, float.class, float.class}
    )
    public void testAddLayer1() {
        LayerRasterizer layerRasterizer = new LayerRasterizer();
        Paint p = new Paint();
        layerRasterizer.addLayer(p);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "addLayer",
        args = {android.graphics.Paint.class}
    )
    public void testAddLayer2() {
        LayerRasterizer layerRasterizer = new LayerRasterizer();
        layerRasterizer.addLayer(new Paint(), 1.0f, 1.0f);
    }
}
