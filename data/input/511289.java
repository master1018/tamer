@TestTargetClass(TextView.SavedState.class)
public class TextView_SaveStateTest extends InstrumentationTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "toString",
        args = {}
    )
    public void testToString() {
        Parcel source = creatTestParcel(0, 0, true, "This is content");
        TextView.SavedState state = TextView.SavedState.CREATOR.createFromParcel(source);
        assertNotNull(state.toString());
        source = creatTestParcel(5, 10, false, "This is another content");
        state = TextView.SavedState.CREATOR.createFromParcel(source);
        assertNotNull(state.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "writeToParcel",
        args = {android.os.Parcel.class, int.class}
    )
    public void testWriteToParcel() {
        Parcel source = creatTestParcel(0, 0, true, "This is content");
        TextView.SavedState state = TextView.SavedState.CREATOR.createFromParcel(source);
        assertNotNull(state);
    }
    private Parcel creatTestParcel(int start, int end, boolean frozenWithFocus, String text) {
        Parcel source = Parcel.obtain();
        source.writeParcelable(AbsSavedState.EMPTY_STATE, 0);
        source.writeInt(start);
        source.writeInt(end);
        source.writeInt(frozenWithFocus ? 1 : 0);
        TextView textView = new TextView(getInstrumentation().getTargetContext());
        textView.setText(text);
        TextUtils.writeToParcel(textView.getText(), source, 0);
        source.setDataPosition(0);
        return source;
    }
}
