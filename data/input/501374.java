@TestTargetClass(View.class)
public class View_IdsTest extends ActivityInstrumentationTestCase2<UsingViewsStubActivity> {
    public View_IdsTest() {
        super("com.android.cts.stub", UsingViewsStubActivity.class);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setId",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getId",
            args = {}
        )
    })
    @UiThreadTest
    public void testIds() {
        Activity activity = getActivity();
        EditText editText = (EditText) activity.findViewById(R.id.entry);
        Button buttonOk = (Button) activity.findViewById(R.id.ok);
        Button buttonCancel = (Button) activity.findViewById(R.id.cancel);
        TextView symbol = (TextView) activity.findViewById(R.id.symbolball);
        TextView warning = (TextView) activity.findViewById(R.id.warning);
        assertNotNull(editText);
        assertNotNull(buttonOk);
        assertNotNull(buttonCancel);
        assertNotNull(symbol);
        assertNotNull(warning);
        assertEquals(activity.getString(R.string.id_ok), buttonOk.getText().toString());
        assertEquals(activity.getString(R.string.id_cancel), buttonCancel.getText().toString());
        editText.setId(0x1111);
        assertEquals(0x1111, editText.getId());
        assertSame(editText, (EditText) activity.findViewById(0x1111));
        buttonCancel.setId(0x9999);
        assertEquals(0x9999, buttonCancel.getId());
        assertSame(buttonCancel, (Button) activity.findViewById(0x9999));
    }
}
