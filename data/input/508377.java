public class StoreTests extends AndroidTestCase {
    public void testStoreLookupPOP() throws MessagingException {
        final String storeUri = "pop3:
        Store.StoreInfo info = Store.StoreInfo.getStoreInfo(storeUri, getContext());
        assertNotNull("storeInfo null", info);
        assertNotNull("scheme null", info.mScheme);
        assertNotNull("classname null", info.mClassName);
        assertFalse(info.mPushSupported);
        assertEquals(Email.VISIBLE_LIMIT_DEFAULT, info.mVisibleLimitDefault);
        assertEquals(Email.VISIBLE_LIMIT_INCREMENT, info.mVisibleLimitIncrement);
        Store store = Store.getInstance(storeUri, getContext(), null);
    }
    public void testStoreLookupIMAP() throws MessagingException {
        final String storeUri = "imap:
        Store.StoreInfo info = Store.StoreInfo.getStoreInfo(storeUri, getContext());
        assertNotNull("storeInfo null", info);
        assertNotNull("scheme null", info.mScheme);
        assertNotNull("classname null", info.mClassName);
        assertFalse(info.mPushSupported);
        assertEquals(Email.VISIBLE_LIMIT_DEFAULT, info.mVisibleLimitDefault);
        assertEquals(Email.VISIBLE_LIMIT_INCREMENT, info.mVisibleLimitIncrement);
        Store store = Store.getInstance(storeUri, getContext(), null);
    }
    public void testStoreLookupEAS() throws MessagingException {
        final String storeUri = "eas:
        Store.StoreInfo info = Store.StoreInfo.getStoreInfo(storeUri, getContext());
        if (info != null) {
            assertNotNull("scheme null", info.mScheme);
            assertNotNull("classname null", info.mClassName);
            assertTrue(info.mPushSupported);
            assertEquals(-1, info.mVisibleLimitDefault);
            assertEquals(-1, info.mVisibleLimitIncrement);
            Store store = Store.getInstance(storeUri, getContext(), null);
        } else {
            try {
                Store store = Store.getInstance(storeUri, getContext(), null);
                fail("MessagingException expected when EAS not supported");
            } catch (MessagingException me) {
            }
        }
    }
    public void testStoreLookupUnknown() {
        final String storeUri = "bogus-scheme:
        Store.StoreInfo info = Store.StoreInfo.getStoreInfo(storeUri, getContext());
        assertNull(info);
        try {
            Store store = Store.getInstance(storeUri, getContext(), null);
            fail("MessagingException expected from bogus URI scheme");
        } catch (MessagingException me) {
        }
    }
}
