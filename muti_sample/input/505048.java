@TestTargetClass(PhoneNumberFormattingTextWatcher.class)
public class PhoneNumberFormattingTextWatcherTest extends AndroidTestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "PhoneNumberFormattingTextWatcher",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "beforeTextChanged",
            args = {java.lang.CharSequence.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "afterTextChanged",
            args = {android.text.Editable.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "onTextChanged",
            args = {java.lang.CharSequence.class, int.class, int.class, int.class}
        )
    })
    public void testPhoneNumberFormattingTextWatcher() {
        TextView text = new TextView(getContext());
        text.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        text.setText("+15551212");
        assertEquals("+1-555-1212", text.getText().toString());
        Editable edit = (Editable) text.getText();
        edit.delete(2, 3);
        assertEquals("+1-551-212", text.getText().toString());
        text.setText("+1-555-1212");
        assertEquals("+1-555-1212", text.getText().toString());
    }
}
