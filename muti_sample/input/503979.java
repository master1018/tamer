@TestTargetClass(Parameters.class)
public class Camera_ParametersTest extends TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "tested indirectly",
            method = "get",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "tested indirectly",
            method = "set",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "tested indirectly",
            method = "getInt",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "tested indirectly",
            method = "set",
            args = {java.lang.String.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPictureFormat",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setPictureFormat",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPictureSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setPictureSize",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPreviewFormat",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setPreviewFormat",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPreviewFrameRate",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setPreviewFrameRate",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getPreviewSize",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "setPreviewSize",
            args = {int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "tested indirectly",
            method = "flatten",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            notes = "tested indirectly",
            method = "unflatten",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.TODO,
            notes = "test removed due to invalid assumptions",
            method = "remove",
            args = {java.lang.String.class}
        )
    })
    public void testAccessMethods() {
    }
}
