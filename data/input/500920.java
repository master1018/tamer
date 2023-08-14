@TestTargetClass(X509ExtendedKeyManager.class) 
public class X509ExtendedKeyManagerTest extends TestCase {
    private class MockX509ExtendedKeyManager extends X509ExtendedKeyManager {
        public MockX509ExtendedKeyManager() {
            super();
        }
        public String chooseClientAlias(String[] arg0, Principal[] arg1, Socket arg2) {
            return null;
        }
        public String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2) {
            return null;
        }
        public X509Certificate[] getCertificateChain(String arg0) {
            return null;
        }
        public String[] getClientAliases(String arg0, Principal[] arg1) {
            return null;
        }
        public PrivateKey getPrivateKey(String arg0) {
            return null;
        }
        public String[] getServerAliases(String arg0, Principal[] arg1) {
            return null;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "X509ExtendedKeyManager",
        args = {}
    )
    public final void test_Constructor() {
        try {
            new MockX509ExtendedKeyManager();
        } catch (Exception e) {
            fail("Unexpected exception " + e.toString());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "chooseEngineClientAlias",
        args = {java.lang.String[].class, java.security.Principal[].class, javax.net.ssl.SSLEngine.class}
    )
    public final void test_chooseEngineClientAlias() {
        X509ExtendedKeyManager km = new MyX509ExtendedKeyManager();
        if (km.chooseEngineClientAlias(null, null, null) != null) {
            fail("non null result");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "chooseEngineServerAlias",
        args = {java.lang.String.class, java.security.Principal[].class, javax.net.ssl.SSLEngine.class}
    )
    public final void test_chooseEngineServerAlias() {
        X509ExtendedKeyManager km = new MyX509ExtendedKeyManager();
        if (km.chooseEngineServerAlias(null, null, null) != null) {
            fail("non null result");
        }
    }
}
class MyX509ExtendedKeyManager extends X509ExtendedKeyManager {
    public String chooseClientAlias(String[] keyType, Principal[] issuers,
            Socket socket) {
        return null;
    }
    public String chooseServerAlias(String keyType, Principal[] issuers,
            Socket socket) {
        return null;
    }
    public X509Certificate[] getCertificateChain(String alias) {
        return null;
    }
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return null;
    }
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return null;
    }
    public PrivateKey getPrivateKey(String alias) {
        return null;
    }
}
