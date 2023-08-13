public class PhoneNumberWatcherTest extends TestCase {
    @SmallTest
    public void testHyphenation() throws Exception {
        SpannableStringBuilder number = new SpannableStringBuilder();
        TextWatcher tw = new PhoneNumberFormattingTextWatcher();
        number.append("555-1212");
        Selection.setSelection(number, 0);
        tw.beforeTextChanged(number, 0, 0, 1);
        number.insert(0, "8");
        tw.afterTextChanged(number);
        assertEquals("855-512-12", number.toString());
    }
    @SmallTest
    public void testHyphenDeletion() throws Exception {
        SpannableStringBuilder number = new SpannableStringBuilder();
        TextWatcher tw = new PhoneNumberFormattingTextWatcher();
        number.append("555-1212");
        Selection.setSelection(number, 4);
        tw.beforeTextChanged(number, 3, 1, 0);
        number.delete(3, 4);
        tw.afterTextChanged(number);
        assertEquals("551-212", number.toString());
        number.insert(0, "-");
        Selection.setSelection(number, 1);
        tw.beforeTextChanged(number, 0, 1, 0);
        number.delete(0, 1);
        tw.afterTextChanged(number);
        assertEquals("551-212", number.toString());
    }
}
