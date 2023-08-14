public class CnapTest extends AndroidTestCase {
    private static final String TAG = "CnapTest";
    private Context mContext;
    private CallerInfo mCallerInfo;
    private String mUnknown = "Unknown";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        mCallerInfo = new CallerInfo();
    }
    @SmallTest
    public void testAbsentNumberIsMappedToUnknown() throws Exception {
        String num = modifyForSpecialCnapCases("ABSENT NUMBER", PRESENTATION_ALLOWED);
        assertIsUnknown(num);
    }
    private void assertIsUnknown(String number) {
        assertEquals(mUnknown, number);
        assertEquals(PRESENTATION_UNKNOWN, mCallerInfo.numberPresentation);
    }
    private String modifyForSpecialCnapCases(String number, int presentation) {
        return PhoneUtils.modifyForSpecialCnapCases(
            mContext, mCallerInfo, number, presentation);
    }
}
