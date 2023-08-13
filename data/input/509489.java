public class VendorPolicyLoaderTest extends AndroidTestCase {
    public void testPackageNotExist() {
        VendorPolicyLoader pl = new VendorPolicyLoader(getContext(), "no.such.package",
                "no.such.Class", true);
        assertEquals(Bundle.EMPTY, pl.getPolicy(null, null));
    }
    public void testIsSystemPackage() {
        final Context c = getContext();
        assertEquals(false, VendorPolicyLoader.isSystemPackage(c, "no.such.package"));
        assertEquals(false, VendorPolicyLoader.isSystemPackage(c, "com.android.email.tests"));
        assertEquals(true, VendorPolicyLoader.isSystemPackage(c, "com.android.settings"));
    }
    public void testGetPolicy() {
        VendorPolicyLoader pl = new VendorPolicyLoader(getContext(), getContext().getPackageName(),
                MockVendorPolicy.class.getName(), true);
        Bundle result = new Bundle();
        result.putInt("ret", 1);
        MockVendorPolicy.mockResult = result;
        Bundle args = new Bundle();
        args.putString("arg1", "a");
        Bundle actualResult = pl.getPolicy("policy1", args);
        assertEquals("operation", "policy1", MockVendorPolicy.passedPolicy);
        assertEquals("arg", "a", MockVendorPolicy.passedBundle.getString("arg1"));
        assertEquals("result", 1, actualResult.getInt("ret"));
    }
    public void testGetPolicyNonSystem() {
        VendorPolicyLoader pl = new VendorPolicyLoader(getContext(), "com.android.email.tests",
                MockVendorPolicy.class.getName(), false);
        MockVendorPolicy.passedPolicy = null;
        assertEquals(Bundle.EMPTY, pl.getPolicy("policy1", null));
        assertNull(MockVendorPolicy.passedPolicy);
    }
    private static class MockVendorPolicy {
        public static String passedPolicy;
        public static Bundle passedBundle;
        public static Bundle mockResult;
        public static Bundle getPolicy(String operation, Bundle args) {
            passedPolicy = operation;
            passedBundle = args;
            return mockResult;
        }
    }
    public void testGetImapIdValues() {
        VendorPolicyLoader pl = VendorPolicyLoader.getInstance(getContext());
        String id = pl.getImapIdValues("user-name", "server.yahoo.com",
                "IMAP4rev1 STARTTLS AUTH=GSSAPI");
        if (id == null) return;
        assertEquals("\"", id.charAt(0));
        assertEquals("\"", id.charAt(id.length()-1));
        String[] elements = id.split("\"");
        assertEquals(0, elements.length % 4);
        for (int i = 0; i < elements.length; ) {
            assertTrue(elements[i] == null || elements[i].startsWith(" "));
            assertTrue(elements[i+1].charAt(0) != ' ');
            assertTrue(elements[i+2].startsWith(" "));
            assertTrue(elements[i+3].charAt(0) != ' ');
            i += 4;            
        }
    }
    public void testFindProviderForDomain() {
        VendorPolicyLoader pl = VendorPolicyLoader.getInstance(getContext());
        Provider p = pl.findProviderForDomain("yahoo.com");
        if (p == null) return;
        assertNull(p.id);
        assertNull(p.label);
        assertEquals("yahoo.com", p.domain);
        assertNotNull(p.incomingUriTemplate);
        assertNotNull(p.incomingUsernameTemplate);
        assertNotNull(p.outgoingUriTemplate);
        assertNotNull(p.outgoingUsernameTemplate);
        assertTrue(p.note == null || p.note.length() > 0);  
    }
}
