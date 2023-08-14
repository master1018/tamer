@TestTargetClass(Security.class)
public class SecurityTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "SecurityException checking missed",
        method = "insertProviderAt",
        args = {java.security.Provider.class, int.class}
    )
    public final void test_insertProviderAtLjava_security_ProviderLI() {
        try {
            Security.insertProviderAt(null, 1);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        Provider p = new MyProvider();
        int initNum = Security.getProviders().length; 
        Provider initialSecondProviderName = Security.getProviders()[1];
        try {
            assertEquals(initNum + 1, Security.insertProviderAt(p, -1));
            assertSame(p, Security.getProviders()[initNum]);
            assertEquals(-1, Security.insertProviderAt(p, 1));
            Security.removeProvider(p.getName());
            assertEquals(initNum + 1, Security.insertProviderAt(p,
                    initNum + 100));
            assertSame(p, Security.getProviders()[initNum]);
            Security.removeProvider(p.getName());
            assertEquals(1, Security.insertProviderAt(p, 1));
            assertSame(p, Security.getProviders()[0]);
            assertSame(initialSecondProviderName, 
                    Security.getProviders()[2]);
        } finally { 
            Security.removeProvider(p.getName());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "SecurityException checking missed",
        method = "addProvider",
        args = {java.security.Provider.class}
    )
    public final void test_addProviderLjava_security_Provider() {
        try {
            Security.addProvider(null);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        Provider p = new MyProvider();
        int initNum = Security.getProviders().length; 
        try {
            assertEquals(initNum + 1, Security.addProvider(p));
            assertSame(p, Security.getProviders()[initNum]);
            assertEquals(-1, Security.addProvider(p));
        } finally { 
            Security.removeProvider(p.getName());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithmProperty",
        args = {java.lang.String.class, java.lang.String.class}
    )
    @SuppressWarnings("deprecation")
    public final void testGetAlgorithmPropertyLjava_lang_String_java_lang_String() {
        Provider provider = new MyProvider();
        Map<String, String> m = new HashMap<String, String>();
        m.clear();
        m.put("Alg.propName.algName", "value");
        provider.putAll(m);
        try {
            Security.addProvider(provider);
            assertNotNull(Security.getAlgorithmProperty("algName", "propName"));
            assertNull(Security.getAlgorithmProperty("DSA", null));
            assertNull(Security.getAlgorithmProperty("DSA", "propName"));
        } finally {
            Security.removeProvider(provider.getName());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getAlgorithms",
        args = {java.lang.String.class}
    )
    public final void testGetAlgorithmsLjava_lang_String() {
        String[] servicesNames = { "Signature", "MessageDigest", "Cipher",
                "Mac", "KeyStore" };
        String[] invalidServiceNames = { "Rubbish", "", null };
        for (int i = 0; i < servicesNames.length; i++) {
            Set<String> algs = Security.getAlgorithms(servicesNames[i]);
            assertTrue("no services with specified name: " + servicesNames[i], algs.size() > 0);
        }
        for (int i = 0; i < invalidServiceNames.length; i++) {
            Set<String> algs = Security.getAlgorithms(invalidServiceNames[i]);
            assertTrue("services with specified name: " + invalidServiceNames[i], algs.size() == 0);
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "removeProvider",
        args = {java.lang.String.class}
    )
    public final void testRemoveProvider() {
        Provider[] providers;
        Provider[] providers1;
        providers = Security.getProviders();
        try {
            for (int i = 0; i < providers.length; i++) {
                Security.removeProvider(providers[i].getName());
            }
            assertEquals("Providers not removed", 0,
                    Security.getProviders().length);
        } finally {    
            for (int i = 0; i < providers.length; i++) {
                Security.addProvider(providers[i]);
            }
            providers1 = Security.getProviders();
            for (int i = 0; i < providers1.length; i++) {
                assertEquals("Providers not restored correctly", providers[i],
                        providers1[i]);
            }
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getProvider",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "getProviders",
            args = {}
        )
    })
    public final void test_getProviderLjava_lang_String() {
        assertNull(Security.getProvider("SOMEINCORRECTPROVIDERNAME"));
        assertNull(Security.getProvider(null));
        Provider[] providers = Security.getProviders();
        assertTrue("getProviders returned zero length array",
                providers.length > 0);
        for (Provider p : providers) {
            String providerName = p.getName();
            assertSame(p, Security.getProvider(providerName));
        }
        Provider p = new MyProvider();
        try {
            Security.addProvider(p);
            assertSame(p, Security.getProvider(p.getName()));
        } finally { 
            Security.removeProvider(p.getName());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProviders",
        args = {java.lang.String.class}
    )
    public void test_getProvidersLjava_lang_String() {
        try {
            Security.getProviders("");
            fail("No expected InvalidParameterException");
        } catch (InvalidParameterException e) {
        }
        try {
            Security.getProviders((String) null);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        Provider p = new MyProvider();
        try {
            Security.addProvider(p);
            String filter = "MyService.MyAlgorithm";
            assertTrue(filter, Arrays.equals(new Provider[] { p }, Security
                    .getProviders(filter)));
            filter = "MyService.MyAlgorithm KeySize:512";
            assertTrue(filter, Arrays.equals(new Provider[] { p }, Security
                    .getProviders(filter)));
            filter = "MyService.MyAlgorithm KeySize:1025";
            assertNull(filter, Security.getProviders(filter));
            filter = "MyService.MyAlgorithm imPLementedIn:softWARE";
            assertTrue(filter, Arrays.equals(new Provider[] { p }, Security
                    .getProviders(filter)));
            filter = "MyService.MyAlgorithm ATTribute:attributeVALUE";
            assertTrue(filter, Arrays.equals(new Provider[] { p }, Security
                    .getProviders(filter)));
            filter = "MyService.NoKeySize KeySize:512";
            assertNull(filter, Security.getProviders(filter));
            filter = "MyService.NoImplementedIn ImplementedIn:Software";
            assertNull(filter, Security.getProviders(filter));
            filter = "ABCService.NoAttribute Attribute:ABC";
            assertNull(filter, Security.getProviders(filter));
        } finally { 
            Security.removeProvider(p.getName());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProviders",
        args = {java.util.Map.class}
    )
    public void test_getProvidersLjava_util_Map() {
        Map<String, String> m = new HashMap<String, String>();
        Security.getProviders(m);
        assertNull("Not null result on empty map", Security.getProviders(m));
        try {
            Security.getProviders((Map<String, String>) null);
            fail("No expected NullPointerException");
        } catch (NullPointerException e) {
        }
        m.put("AAA.BBB.CCC", "aaaa"); 
        try {
            Security.getProviders(m);
            fail("No expected InvalidParameterException");
        } catch (InvalidParameterException e) {
        }
        Provider p = new MyProvider();
        try {
            Security.addProvider(p);
            m.clear();
            m.put("MyService.MyAlgorithm", "");
            m.put("MessageDigest.SHA-1", "");
            assertTrue("MyService.MyAlgorithm", Arrays.equals(
                    new Provider[] { p }, Security.getProviders(m)));
            m.clear();
            m.put("MyService.MyAlgorithm KeySize", "512");
            m.put("MessageDigest.SHA-1", "");
            assertTrue("MyService.MyAlgorithm KeySize:512", Arrays.equals(
                    new Provider[] { p }, Security.getProviders(m)));
            m.clear();
            m.put("MyService.MyAlgorithm KeySize", "1025");
            m.put("MessageDigest.SHA-1", "");
            assertNull("MyService.MyAlgorithm KeySize:1025", Security
                    .getProviders(m));
            m.clear();
            m.put("MyService.MyAlgorithm imPLementedIn", "softWARE");
            assertTrue(Arrays.equals(new Provider[] { p }, Security
                    .getProviders(m)));
            m.clear();
            m.put("MyService.MyAlgorithm ATTribute", "attributeVALUE");
            assertTrue(Arrays.equals(new Provider[] { p }, Security
                    .getProviders(m)));
            m.clear();
            m.put("MyService.NoKeySize KeySize", "512");
            assertNull("No KeySize attribute", Security.getProviders(m));
            m.clear();
            m.put("MyService.NoImplementedIn ImplementedIn", "Software");
            assertNull("No ImplementedIn attribute", Security.getProviders(m));
            m.clear();
            m.put("ABCService.NoAttribute Attribute", "ABC");
            assertNull(Security.getProviders(m));
        } finally { 
            Security.removeProvider(p.getName());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getProviders",
        args = {}
    )
    public void test_getProviders() {
        Provider[] prv;
        MyProvider provider = new MyProvider();
        try {
            prv = Security.getProviders();
            int len1 = prv.length;
            if (len1 == 0) {
                fail("Array of providers is ampty");
            }
            Security.addProvider(provider);
            prv = Security.getProviders();
            int len2 = prv.length;
            if ((len2 == len1 + 1) && (prv[len2-1].toString().equals("MyProvider version 1.0"))) {
            } else {
                fail("Method getProviders() returned incorrect values");
            }
        } catch (Exception ex) {
            fail("Unexpected exception");
        }
        finally {
            Security.removeProvider(provider.getName());
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verification of null parameter only.",
        method = "getProperty",
        args = {java.lang.String.class}
    )
    public void test_getPropertyLjava_lang_String() {
        try {
            Security.getProperty(null);
            fail("No expected NullPointerException.");
        } catch (NullPointerException e) {
        }
        Security.setProperty("myprop","test white space    ");
        assertEquals("test white space", Security.getProperty("myprop"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "SecurityException checking missed",
        method = "setProperty",
        args = {java.lang.String.class, java.lang.String.class}
    )
    public void test_setPropertyLjava_lang_StringLjava_lang_String() {
        try {
            Security.setProperty(null, "");
            fail("No expected NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            Security.setProperty("", null);
            fail("No expected NullPointerException.");
        } catch (NullPointerException e) {
        }
        Security.setProperty("", "");
        assertEquals("Empty property", "", Security.getProperty(""));
        Security.setProperty("My Test Property", "My property value");
        assertEquals("My property value", Security
                .getProperty("My Test Property"));
    }
    @SuppressWarnings("serial")
    class MyProvider extends Provider {
        MyProvider() {
            super("MyProvider", 1.0, "Provider for testing");
            put("MessageDigest.SHA-1", "SomeClassName");
            put("MyService.MyAlgorithm", "SomeClassName");
            put("MyService.MyAlgorithm KeySize", "1024");
            put("MyService.MyAlgorithm ImplementedIn", "Software");
            put("MyService.MyAlgorithm Attribute", "AttributeValue");
            put("MyService.NoKeySize", "SomeClassName");
            put("MyService.NoImplementedIn", "SomeClassName");
            put("ABCService.NoAttribute", "SomeClassName");
        }
    }
}
