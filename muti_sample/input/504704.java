@TestTargetClass(Provider.class)
public class Provider2Test extends junit.framework.TestCase {
    class TestProvider extends Provider {
        TestProvider(String name, double version, String info) {
            super(name, version, info);
        }
    }
    class MyEntry implements java.util.Map.Entry {
         public Object getKey() {
             return null;  
         }
         public Object getValue() {
             return null;  
         }
         public Object setValue(Object value) {
             return null;  
         }
    }
    TestProvider provTest = new TestProvider("provTest", 1.2,
            "contains nothings, purely for testing the class");
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "UnsupportedOperationException verification",
        method = "entrySet",
        args = {}
    )
    public void test_entrySet() {
        provTest.put("test.prop", "this is a test property");
        try {
            provTest.entrySet().add(new MyEntry());
            fail("was able to modify the entrySet");
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInfo",
        args = {}
    )
    public void test_getInfo() {
        assertEquals("the information of the provider is not stored properly",
                "contains nothings, purely for testing the class", provTest
                        .getInfo());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getName",
        args = {}
    )
    public void test_getName() {
        assertEquals("the name of the provider is not stored properly",
                "provTest", provTest.getName());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getVersion",
        args = {}
    )
    public void test_getVersion() {
        assertEquals("the version of the provider is not stored properly",
                1.2, provTest.getVersion(), 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "UnsupportedOperationException verification",
        method = "keySet",
        args = {}
    )
    public void test_keySet() {
        provTest.put("test.prop", "this is a test property");
        try {
            provTest.keySet().add("another property key");
            fail("was able to modify the keySet");
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "UnsupportedOperationException verification",
        method = "values",
        args = {}
    )
    public void test_values() {
        provTest.put("test.prop", "this is a test property");
        try {
            provTest.values().add("another property value");
            fail("was able to modify the values collection");
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Regression test",
        method = "toString",
        args = {}
    )
    public void test_toString() {
        assertEquals("provTest version 1.2", provTest.toString());
    }
    public void test_getAlias() throws Exception {
        MockProvider mockProvider = new MockProvider("MOCKNAME", 1.0,
                "MOCKINFO");
        Provider.Service service = new Provider.Service(mockProvider,
                "MOCKTYPE", "MOCKALGORITHM", "TESTCLASSNAME", null, null);
        mockProvider.putService(service);
        Security.addProvider(mockProvider);
        try {
            MessageDigest messageDigest = MessageDigest
                    .getInstance("NOTEXISTS");
        }
        catch (NoSuchAlgorithmException e) {
        } finally {
            Security.removeProvider("MOCKNAME");
        }
    }
    public static class MockProvider extends Provider {
        private static final long serialVersionUID = 1L;
        public MockProvider(String name, double version, String info) {
            super(name, version, info);
        }
        public void putService(Provider.Service service) {
            super.putService(service);
        }
        public void removeService(Provider.Service service) {
            super.removeService(service);
        }
    }
}
