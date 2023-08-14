 class ContentValuesVerifierElem {
    private final AndroidTestCase mTestCase;
    private final ImportTestResolver mResolver;
    private final VCardEntryHandler mHandler;
    public ContentValuesVerifierElem(AndroidTestCase androidTestCase) {
        mTestCase = androidTestCase;
        mResolver = new ImportTestResolver(androidTestCase);
        mHandler = new VCardEntryCommitter(mResolver);
    }
    public ContentValuesBuilder addExpected(String mimeType) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Data.MIMETYPE, mimeType);
        mResolver.addExpectedContentValues(contentValues);
        return new ContentValuesBuilder(contentValues);
    }
    public void verify(int resId, int vCardType)
            throws IOException, VCardException {
        verify(mTestCase.getContext().getResources().openRawResource(resId), vCardType);
    }
    public void verify(InputStream is, int vCardType) throws IOException, VCardException {
        final VCardParser vCardParser;
        if (VCardConfig.isV30(vCardType)) {
            vCardParser = new VCardParser_V30(true);  
        } else {
            vCardParser = new VCardParser_V21();
        }
        VCardEntryConstructor builder =
                new VCardEntryConstructor(null, null, false, vCardType, null);
        builder.addEntryHandler(mHandler);
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
        verifyResolver();
    }
    public void verifyResolver() {
        mResolver.verify();
    }
    public void onParsingStart() {
        mHandler.onStart();
    }
    public void onEntryCreated(VCardEntry entry) {
        mHandler.onEntryCreated(entry);
    }
    public void onParsingEnd() {
        mHandler.onEnd();
    }
}
