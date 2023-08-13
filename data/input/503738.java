public class PhoneNumberComparisonTest extends AndroidTestCase {
    @SmallTest
    public void testCompareSmsShortcode() {
        Log.i(LogTag.APP, "testCompareSmsShortcode");
        assertFalse(PhoneNumberUtils.compare("321", "54321"));
        assertFalse(PhoneNumberUtils.compare("4321", "54321"));
        assertFalse(PhoneNumberUtils.compare("54321", "654321"));
        assertFalse(PhoneNumberUtils.compare("54321", "6505554321"));
        assertFalse(PhoneNumberUtils.compare("54321", "+16505554321"));
        assertFalse(PhoneNumberUtils.compare("654321", "6505654321"));
        assertFalse(PhoneNumberUtils.compare("654321", "+16505654321"));
    }
}