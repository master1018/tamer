public class PhoneNumberUtilsUnitTest extends AndroidTestCase {
    private String mVoiceMailNumber;
    private static final String TAG = "PhoneNumberUtilsUnitTest";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @SmallTest
    public void testWithNumberNotEqualToVoiceMail() throws Exception {
        assertFalse(PhoneNumberUtils.isVoiceMailNumber("911"));
        assertFalse(PhoneNumberUtils.isVoiceMailNumber("tel:911"));
        assertFalse(PhoneNumberUtils.isVoiceMailNumber("+18001234567"));
        assertFalse(PhoneNumberUtils.isVoiceMailNumber(""));
        assertFalse(PhoneNumberUtils.isVoiceMailNumber(null));
    }
}
