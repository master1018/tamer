@TestTargetClass(Surface.class)
public class SurfaceTest extends ActivityInstrumentationTestCase2<SurfaceViewStubActivity> {
    public SurfaceTest() {
        super("com.android.cts.stub", SurfaceViewStubActivity.class);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Test Surface",
            method = "lockCanvas",
            args = {android.graphics.Rect.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Test Surface",
            method = "unlockCanvas",
            args = {android.graphics.Canvas.class}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Test Surface",
            method = "unlockCanvasAndPost",
            args = {android.graphics.Canvas.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test Surface",
            method = "clear",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_NECESSARY,
            notes = "Test Surface",
            method = "describeContents",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL,
            notes = "Test Surface",
            method = "isValid",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test Surface",
            method = "setLayer",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test Surface",
            method = "setPosition",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test Surface",
            method = "setSize",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test Surface",
            method = "hide",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test Surface",
            method = "show",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test Surface",
            method = "setTransparentRegionHint",
            args = {android.graphics.Region.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test Surface",
            method = "setAlpha",
            args = {float.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test Surface",
            method = "setMatrix",
            args = {float.class, float.class, float.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test Surface",
            method = "freeze",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test Surface",
            method = "unfreeze",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test Surface",
            method = "setFreezeTint",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test Surface",
            method = "setFlags",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Surface",
            method = "setOrientation",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Surface",
            method = "toString",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Test Surface",
            method = "readFromParcel",
            args = {android.os.Parcel.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test Surface",
            method = "writeToParcel",
            args = {android.os.Parcel.class, int.class}
        )
    })
    @ToBeFixed(bug = "1698482", explanation = "The APIs which set surface parameters should be " +
        " set '@hide', because they needs to be inside open/closeTransaction block, " +
        "while the APIs that open/closeTransaction block is set '@hide'")
    @BrokenTest("needs investigation")
    public void testSurface() throws OutOfResourcesException {
        Surface surface = getActivity().getSurfaceView().getHolder().getSurface();
        surface.describeContents();
        assertTrue(surface.isValid());
        Canvas canvas = surface.lockCanvas(new Rect(0, 0, 10, 10));
        assertNotNull(canvas);
        surface.unlockCanvas(canvas);
        surface.unlockCanvasAndPost(canvas);
        Parcel p = Parcel.obtain();
        surface.writeToParcel(p, 0);
        p.setDataPosition(0);
        Surface s = Surface.CREATOR.createFromParcel(p);
        assertEquals(surface.isValid(), s.isValid());
        surface.readFromParcel(p);
        assertNotNull(surface.toString());
        Surface.setOrientation(0, Surface.ROTATION_90);
    }
}
