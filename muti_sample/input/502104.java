public class MetaDataTest extends AndroidTestCase {
    private void checkMetaData(ComponentName cn, PackageItemInfo ci)
            throws IOException, XmlPullParserException {
        assertNotNull("Unable to find component " + cn, ci);
        Bundle md = ci.metaData;
        assertNotNull("No meta data found", md);
        assertEquals("foo", md.getString("com.android.frameworks.coretests.string"));
        assertTrue(md.getBoolean("com.android.frameworks.coretests.boolean"));
        assertEquals(100, md.getInt("com.android.frameworks.coretests.integer"));
        assertEquals(0xff000000, md.getInt("com.android.frameworks.coretests.color"));
        assertEquals((double) 1001,
                Math.floor(md.getFloat("com.android.frameworks.coretests.float") * 10 + .5));
        assertEquals(R.xml.metadata, md.getInt("com.android.frameworks.coretests.reference"));
        XmlResourceParser xml = ci.loadXmlMetaData(mContext.getPackageManager(),
                "com.android.frameworks.coretests.reference");
        assertNotNull(xml);
        int type;
        while ((type = xml.next()) != XmlPullParser.START_TAG
                && type != XmlPullParser.END_DOCUMENT) {
        }
        assertEquals(XmlPullParser.START_TAG, type);
        assertEquals("thedata", xml.getName());
        final String rawAttr = xml.getAttributeValue(null, "rawText");
        assertEquals("some raw text", rawAttr);
        final int rawColorIntAttr = xml.getAttributeIntValue(null, "rawColor", 0);
        assertEquals(0xffffff00, rawColorIntAttr);
        final String rawColorStrAttr = xml.getAttributeValue(null, "rawColor");
        assertEquals("#ffffff00", rawColorStrAttr);
        final String nameSpace = "http:
        final int colorIntAttr = xml.getAttributeIntValue(nameSpace, "color", 0);
        assertEquals(0xffff0000, colorIntAttr);
        final String colorStrAttr = xml.getAttributeValue(nameSpace, "color");
        assertEquals("#ffff0000", colorStrAttr);
        TypedArray a = mContext.obtainStyledAttributes(xml,
                android.R.styleable.TextView);
        String styledAttr = a.getString(android.R.styleable.TextView_text);
        assertEquals("text", styledAttr);
        a.recycle();
        xml.close();
    }
    @SmallTest
    public void testActivityWithData() throws Exception {
        ComponentName cn = new ComponentName(mContext, LocalActivity.class);
        ActivityInfo ai = mContext.getPackageManager().getActivityInfo(
                cn, PackageManager.GET_META_DATA);
        checkMetaData(cn, ai);
        ai = mContext.getPackageManager().getActivityInfo(cn, 0);
        assertNull("Meta data returned when not requested", ai.metaData);
    }
    @SmallTest
    public void testReceiverWithData() throws Exception {
        ComponentName cn = new ComponentName(mContext, LocalReceiver.class);
        ActivityInfo ai = mContext.getPackageManager().getReceiverInfo(
                cn, PackageManager.GET_META_DATA);
        checkMetaData(cn, ai);
        ai = mContext.getPackageManager().getReceiverInfo(cn, 0);
        assertNull("Meta data returned when not requested", ai.metaData);
    }
    @SmallTest
    public void testServiceWithData() throws Exception {
        ComponentName cn = new ComponentName(mContext, LocalService.class);
        ServiceInfo si = mContext.getPackageManager().getServiceInfo(
                cn, PackageManager.GET_META_DATA);
        checkMetaData(cn, si);
        si = mContext.getPackageManager().getServiceInfo(cn, 0);
        assertNull("Meta data returned when not requested", si.metaData);
    }
    @MediumTest
    public void testProviderWithData() throws Exception {
        ComponentName cn = new ComponentName(mContext, LocalProvider.class);
        ProviderInfo pi = mContext.getPackageManager().resolveContentProvider(
                "com.android.frameworks.coretests.LocalProvider",
                PackageManager.GET_META_DATA);
        checkMetaData(cn, pi);
        pi = mContext.getPackageManager().resolveContentProvider(
                "com.android.frameworks.coretests.LocalProvider", 0);
        assertNull("Meta data returned when not requested", pi.metaData);
    }
    @SmallTest
    public void testPermissionWithData() throws Exception {
        ComponentName cn = new ComponentName("foo",
                "com.android.frameworks.coretests.permission.TEST_GRANTED");
        PermissionInfo pi = mContext.getPackageManager().getPermissionInfo(
                cn.getClassName(), PackageManager.GET_META_DATA);
        checkMetaData(cn, pi);
        pi = mContext.getPackageManager().getPermissionInfo(
                cn.getClassName(), 0);
        assertNull("Meta data returned when not requested", pi.metaData);
    }
}
