@TestTargetClass(BaseSavedState.class)
public class View_BaseSavedStateTest extends InstrumentationTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "BaseSavedState",
            args = {android.os.Parcel.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "BaseSavedState",
            args = {android.os.Parcelable.class}
        )
    })
    public void testConstructors() {
        BaseSavedState state = new BaseSavedState(Parcel.obtain());
        new BaseSavedState(state);
    }
}
