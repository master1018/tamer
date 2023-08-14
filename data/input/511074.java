class LineVerifier implements VCardComposer.OneEntryHandler {
    private final TestCase mTestCase;
    private final ArrayList<LineVerifierElem> mLineVerifierElemList;
    private int mVCardType;
    private int index;
    public LineVerifier(TestCase testCase, int vcardType) {
        mTestCase = testCase;
        mLineVerifierElemList = new ArrayList<LineVerifierElem>();
        mVCardType = vcardType;
    }
    public LineVerifierElem addLineVerifierElem() {
        LineVerifierElem lineVerifier = new LineVerifierElem(mTestCase, mVCardType);
        mLineVerifierElemList.add(lineVerifier);
        return lineVerifier;
    }
    public void verify(String vcard) {
        if (index >= mLineVerifierElemList.size()) {
            mTestCase.fail("Insufficient number of LineVerifier (" + index + ")");
        }
        LineVerifierElem lineVerifier = mLineVerifierElemList.get(index);
        lineVerifier.verify(vcard);
        index++;
    }
    public boolean onEntryCreated(String vcard) {
        verify(vcard);
        return true;
    }
    public boolean onInit(Context context) {
        return true;
    }
    public void onTerminate() {
    }
}
