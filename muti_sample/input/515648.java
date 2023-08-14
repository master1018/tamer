class MockContentProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return true;
    }
    @Override
    public int bulkInsert(Uri url, ContentValues[] initialValues) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @SuppressWarnings("unused")
    public IBulkCursor bulkQuery(Uri url, String[] projection, String selection,
            String[] selectionArgs, String sortOrder, IContentObserver observer,
            CursorWindow window) throws RemoteException {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @Override
    @SuppressWarnings("unused")
    public int delete(Uri url, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @Override
    public String getType(Uri url) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @Override
    public ParcelFileDescriptor openFile(Uri url, String mode) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @Override
    public AssetFileDescriptor openAssetFile(Uri uri, String mode) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @Override
    public Cursor query(Uri url, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    @Override
    public int update(Uri url, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
    public IBinder asBinder() {
        throw new UnsupportedOperationException("unimplemented mock method");
    }
}
 class VCardTestsBase extends AndroidTestCase {
    public static final int V21 = VCardConfig.VCARD_TYPE_V21_GENERIC_UTF8;
    public static final int V30 = VCardConfig.VCARD_TYPE_V30_GENERIC_UTF8;
    protected final ContentValues mContentValuesForQP;
    protected final ContentValues mContentValuesForSJis;
    protected final ContentValues mContentValuesForUtf8;
    protected final ContentValues mContentValuesForQPAndSJis;
    protected final ContentValues mContentValuesForQPAndUtf8;
    protected final ContentValues mContentValuesForBase64V21;
    protected final ContentValues mContentValuesForBase64V30;
    protected VCardVerifier mVerifier;
    private boolean mSkipVerification;
    public VCardTestsBase() {
        super();
        mContentValuesForQP = new ContentValues();
        mContentValuesForQP.put("ENCODING", "QUOTED-PRINTABLE");
        mContentValuesForSJis = new ContentValues();
        mContentValuesForSJis.put("CHARSET", "SHIFT_JIS");
        mContentValuesForUtf8 = new ContentValues();
        mContentValuesForUtf8.put("CHARSET", "UTF-8");
        mContentValuesForQPAndSJis = new ContentValues();
        mContentValuesForQPAndSJis.put("ENCODING", "QUOTED-PRINTABLE");
        mContentValuesForQPAndSJis.put("CHARSET", "SHIFT_JIS");
        mContentValuesForQPAndUtf8 = new ContentValues();
        mContentValuesForQPAndUtf8.put("ENCODING", "QUOTED-PRINTABLE");
        mContentValuesForQPAndUtf8.put("CHARSET", "UTF-8");
        mContentValuesForBase64V21 = new ContentValues();
        mContentValuesForBase64V21.put("ENCODING", "BASE64");
        mContentValuesForBase64V30 = new ContentValues();
        mContentValuesForBase64V30.put("ENCODING", "b");
    }
    @Override
    public void testAndroidTestCaseSetupProperly() {
        super.testAndroidTestCaseSetupProperly();
        mSkipVerification = true;
    }
    @Override
    public void setUp() throws Exception{
        super.setUp();
        mVerifier = new VCardVerifier(this);
        mSkipVerification = false;
    }
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (!mSkipVerification) {
            mVerifier.verify();
        }
    }
}
