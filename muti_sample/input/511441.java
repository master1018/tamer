 class ContentValuesVerifier implements VCardEntryHandler {
    private AndroidTestCase mTestCase;
    private List<ContentValuesVerifierElem> mContentValuesVerifierElemList =
        new ArrayList<ContentValuesVerifierElem>();
    private int mIndex;
    public ContentValuesVerifierElem addElem(AndroidTestCase androidTestCase) {
        mTestCase = androidTestCase;
        ContentValuesVerifierElem importVerifier = new ContentValuesVerifierElem(androidTestCase);
        mContentValuesVerifierElemList.add(importVerifier);
        return importVerifier;
    }
    public void verify(int resId, int vCardType) throws IOException, VCardException {
        verify(mTestCase.getContext().getResources().openRawResource(resId), vCardType);
    }
    public void verify(int resId, int vCardType, final VCardParser vCardParser)
            throws IOException, VCardException {
        verify(mTestCase.getContext().getResources().openRawResource(resId),
                vCardType, vCardParser);
    }
    public void verify(InputStream is, int vCardType) throws IOException, VCardException {
        final VCardParser vCardParser;
        if (VCardConfig.isV30(vCardType)) {
            vCardParser = new VCardParser_V30(true);  
        } else {
            vCardParser = new VCardParser_V21();
        }
        verify(is, vCardType, vCardParser);
    }
    public void verify(InputStream is, int vCardType, final VCardParser vCardParser)
            throws IOException, VCardException {
        VCardEntryConstructor builder =
            new VCardEntryConstructor(null, null, false, vCardType, null);
        builder.addEntryHandler(this);
        try {
            vCardParser.parse(is, builder);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }
    public void onStart() {
        for (ContentValuesVerifierElem elem : mContentValuesVerifierElemList) {
            elem.onParsingStart();
        }
    }
    public void onEntryCreated(VCardEntry entry) {
        mTestCase.assertTrue(mIndex < mContentValuesVerifierElemList.size());
        mContentValuesVerifierElemList.get(mIndex).onEntryCreated(entry);
        mIndex++;
    }
    public void onEnd() {
        for (ContentValuesVerifierElem elem : mContentValuesVerifierElemList) {
            elem.onParsingEnd();
            elem.verifyResolver();
        }
    }
}
