public class ResourceNameTest extends AndroidTestCase {
    @SmallTest
    public void testGetResourceName() {
        final Resources res = mContext.getResources();
        final String fullName = res.getResourceName(R.configVarying.simple);
        assertEquals("com.android.cts.stub:configVarying/simple", fullName);
        final String packageName = res.getResourcePackageName(R.configVarying.simple);
        assertEquals("com.android.cts.stub", packageName);
        final String typeName = res.getResourceTypeName(R.configVarying.simple);
        assertEquals("configVarying", typeName);
        final String entryName = res.getResourceEntryName(R.configVarying.simple);
        assertEquals("simple", entryName);
    }
    @SmallTest
    public void testGetResourceIdentifier() {
        final Resources res = mContext.getResources();
        int resid = res.getIdentifier(
                "com.android.cts.stub:configVarying/simple",
                null, null);
        assertEquals(R.configVarying.simple, resid);
        resid = res.getIdentifier("configVarying/simple", null,
                "com.android.cts.stub");
        assertEquals(R.configVarying.simple, resid);
        resid = res.getIdentifier("simple", "configVarying",
                "com.android.cts.stub");
        assertEquals(R.configVarying.simple, resid);
    }
}
