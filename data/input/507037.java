import org.apache.harmony.security.tests.java.security.Identity2Test.IdentitySubclass;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
@SuppressWarnings("deprecation")
@TestTargetClass(IdentityScope.class)
public class IdentityScope2Test extends junit.framework.TestCase {
    static PublicKey pubKey;
    static {
        try {
            pubKey = KeyPairGenerator.getInstance("DSA").genKeyPair().getPublic();
        } catch (Exception e) {
            fail(e.toString());
        }
    }
    public static class IdentityScopeSubclass extends IdentityScope {
        private static final long serialVersionUID = 1L;
        Hashtable<Identity, Identity> identities;
        public IdentityScopeSubclass(String name, PublicKey pk) {
            super(name);
            try {
                setPublicKey(pk);
            } catch (KeyManagementException e) {
            }
            identities = new Hashtable<Identity, Identity>();
        }
        public IdentityScopeSubclass() {
            super();
            identities = new Hashtable<Identity, Identity>();
        }
        public IdentityScopeSubclass(String name) {
            super(name);
            identities = new Hashtable<Identity, Identity>();
        }
        public IdentityScopeSubclass(String name, IdentityScope scope)
                throws KeyManagementException {
            super(name, scope);
            identities = new Hashtable<Identity, Identity>();
        }
        public int size() {
            return identities.size();
        }
        public Identity getIdentity(String name) {
            Enumeration<Identity> en = identities();
            while (en.hasMoreElements()) {
                Identity current = (Identity) en.nextElement();
                if (current.getName().equals(name))
                    return current;
            }
            return null;
        }
        public Identity getIdentity(PublicKey pk) {
            Enumeration<Identity> en = identities();
            while (en.hasMoreElements()) {
                Identity current = (Identity) en.nextElement();
                if (current.getPublicKey() == pk)
                    return current;
            }
            return null;
        }
        public Enumeration<Identity> identities() {
            return identities.elements();
        }
        public void addIdentity(Identity id) throws KeyManagementException {
            if (identities.containsKey(id))
                throw new KeyManagementException(
                        "This Identity is already contained in the scope");
            if (getIdentity(id.getPublicKey()) != null)
                throw new KeyManagementException(
                        "This Identity's public key already exists in the scope");
            identities.put(id, id);
        }
        public void removeIdentity(Identity id) throws KeyManagementException {
            if (!identities.containsKey(id))
                throw new KeyManagementException(
                        "This Identity is not contained in the scope");
            identities.remove(id);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IdentityScope",
        args = {}
    )
    public void test_Constructor() {
        new IdentityScopeSubclass();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IdentityScope",
        args = {java.lang.String.class}
    )
    public void test_ConstructorLjava_lang_String() {
        String[] str = {"test", "", null};
        IdentityScopeSubclass iss;
        for (int i = 0; i < str.length; i++) {
            try {
                iss = new IdentityScopeSubclass(str[i]);
                assertNotNull(iss);
                assertTrue(iss instanceof IdentityScope);
            } catch (Exception e) {
                fail("Unexpected exception for parameter " + str[i]);
            }
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IdentityScope",
        args = {java.lang.String.class, java.security.IdentityScope.class}
    )
    public void test_ConstructorLjava_lang_StringLjava_security_IdentityScope() {
        String nameNull = null;
        String[] str = {"test", "", "!@#$%^&*()", "identity name"};
        IdentityScope is;
        IdentityScope iss = new IdentityScopeSubclass("test scope");
        for (int i = 0; i < str.length; i++) {
            try {
                is = new IdentityScopeSubclass(str[i], new IdentityScopeSubclass());
                assertNotNull(is);
                assertTrue(is instanceof IdentityScope);
            } catch (Exception e) {
                fail("Unexpected exception for parameter " + str[i]);
            }
        }
        try {
            is = new IdentityScopeSubclass(nameNull, new IdentityScopeSubclass());
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            fail("Incorrect exception " + e + " was thrown");
        }
        try {
            is = new IdentityScopeSubclass("test", iss);
            is = new IdentityScopeSubclass("test", iss);
            fail("KeyManagementException was not thrown");
        } catch (KeyManagementException npe) {
        } catch (Exception e) {
            fail("Incorrect exception " + e + " was thrown instead of KeyManagementException");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "addIdentity",
        args = {java.security.Identity.class}
    )
    public void test_addIdentityLjava_security_Identity() throws Exception {
               IdentityScopeSubclass sub = new IdentityScopeSubclass("test",
                       new IdentityScopeSubclass());
               Identity id = new IdentitySubclass("id1");
               id.setPublicKey(pubKey);
               sub.addIdentity(id);
               try {
                   Identity id2 = new IdentitySubclass("id2");
                   id2.setPublicKey(pubKey);
                   sub.addIdentity(id2);
                   fail("KeyManagementException should have been thrown");
               } catch (KeyManagementException e) {
               }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "removeIdentity",
        args = {java.security.Identity.class}
    )
    public void test_removeIdentityLjava_security_Identity() throws Exception {
               IdentityScopeSubclass sub = new IdentityScopeSubclass("test",
                       new IdentityScopeSubclass());
               Identity id = new IdentitySubclass();
               id.setPublicKey(pubKey);
               sub.addIdentity(id);
               sub.removeIdentity(id);
               try {
                   sub.removeIdentity(id);
                   fail("KeyManagementException should have been thrown");
               } catch (KeyManagementException e) {
               }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "identities",
        args = {}
    )
    public void test_identities() throws Exception {
               IdentityScopeSubclass sub = new IdentityScopeSubclass("test",
                       new IdentityScopeSubclass());
               Identity id = new IdentitySubclass();
               id.setPublicKey(pubKey);
               sub.addIdentity(id);
               Enumeration<Identity> en = sub.identities();
               assertTrue("Wrong object contained in identities", en.nextElement()
                       .equals(id));
               assertTrue("Contains too many elements", !en.hasMoreElements());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getIdentity",
        args = {java.security.Principal.class}
    )
    public void test_getIdentityLjava_security_Principal() throws Exception {
        Identity id = new IdentitySubclass("principal name");
        id.setPublicKey(pubKey);
        IdentityScopeSubclass sub = new IdentityScopeSubclass("test",
                new IdentityScopeSubclass());
        try {
            sub.getIdentity((java.security.Principal) null);
            fail("Test 1: NullPointerException expected.");
        } catch (NullPointerException e) {
        }
        sub.addIdentity(id);
        Identity returnedId = sub.getIdentity(id);
        assertEquals("Test 2: Returned Identity not the same as the added one;", 
                id, returnedId);
        Identity id2 = new IdentitySubclass("Another identity");
        id2.setPublicKey(pubKey);
        assertNull("Test 3: Null value expected.", 
                sub.getIdentity(id2));
        try {
            sub.getIdentity((java.security.Principal) null);
            fail("Test 4: NullPointerException expected.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getIdentity",
        args = {java.security.PublicKey.class}
    )
    public void test_getIdentityLjava_security_PublicKey() throws Exception {
        IdentityScopeSubclass sub = new IdentityScopeSubclass("test",
                new IdentityScopeSubclass());
        Identity id = new IdentitySubclass();
        id.setPublicKey(pubKey);
        sub.addIdentity(id);
        Identity returnedId = sub.getIdentity(pubKey);
        assertEquals("Test 1: Returned Identity not the same as the added one;", 
                id, returnedId);
        assertNull("Test 2: Null value expected.", 
                sub.getIdentity((PublicKey) null));
        PublicKey anotherKey = KeyPairGenerator.getInstance("DSA").genKeyPair().getPublic();
        assertNull("Test 3: Null value expected.", 
                sub.getIdentity(anotherKey));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getIdentity",
        args = {java.lang.String.class}
    )
    public void test_getIdentityLjava_lang_String() throws Exception {
               IdentityScopeSubclass sub = new IdentityScopeSubclass("test",
                       new IdentityScopeSubclass());
               Identity id = new IdentitySubclass("test");
               id.setPublicKey(pubKey);
               sub.addIdentity(id);
               Identity returnedId = sub.getIdentity("test");
               assertEquals("Returned Identity not the same as the added one", id,
                       returnedId);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "size",
        args = {}
    )
    public void test_size() throws Exception {
               IdentityScopeSubclass sub = new IdentityScopeSubclass("test",
                       new IdentityScopeSubclass());
               Identity id = new IdentitySubclass();
               id.setPublicKey(pubKey);
               sub.addIdentity(id);
               assertEquals("Wrong size", 1, sub.size());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public void test_toString() throws Exception {
            IdentityScopeSubclass sub = new IdentityScopeSubclass("test",
                    new IdentityScopeSubclass());
            Identity id = new IdentitySubclass();
            id.setPublicKey(pubKey);
            sub.addIdentity(id);
            assertNotNull("toString returned a null", sub.toString());
            assertTrue("Not a valid String ", sub.toString().length() > 0);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression test",
        method = "getIdentity",
        args = {java.lang.String.class}
    )
    public void test_getIdentity() throws Exception {
        IdentityScope scope = IdentityScope.getSystemScope(); 
        try {
            scope.getIdentity((String) null);
            fail("NPE expected");
        } catch (NullPointerException npe) {}
    }
}
