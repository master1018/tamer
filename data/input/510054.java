@TestTargetClass(X509CRLSelector.class)
public class X509CRLSelectorTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "addIssuer",
        args = {javax.security.auth.x500.X500Principal.class}
    )
    public void test_addIssuerLjavax_security_auth_x500_X500Principal01()
            throws Exception {
        X509CRLSelector obj = new X509CRLSelector();
        try {
            obj.addIssuer((X500Principal) null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IOException.",
        method = "addIssuerName",
        args = {java.lang.String.class}
    )
    public void test_addIssuerNameLjava_lang_String01() throws Exception {
        X509CRLSelector obj = new X509CRLSelector();
        try {
            obj.addIssuerName("234");
            fail("IOException expected");
        } catch (IOException e) {
        }
        try {
            new X509CRLSelector().addIssuerName("w=y");
            fail("IOException expected");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "addIssuerName",
        args = {java.lang.String.class}
    )
    public void test_addIssuerNameLjava_lang_String02() throws IOException {
        X509CRLSelector selector = new X509CRLSelector();
        selector.addIssuerName((String) null);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies IOException.",
        method = "addIssuerName",
        args = {byte[].class}
    )
    public void test_addIssuerName$B_3() throws Exception {
        X509CRLSelector obj = new X509CRLSelector();
        try {
            obj.addIssuerName(new byte[] { (byte) 2, (byte) 3, (byte) 4 });
            fail("IOException expected");
        } catch (IOException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "addIssuerName",
        args = {byte[].class}
    )
    public void test_addIssuerName$B_4() throws Exception {
        X509CRLSelector obj = new X509CRLSelector();
        try {
            obj.addIssuerName((byte[]) null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Regression test.",
        method = "setIssuerNames",
        args = {java.util.Collection.class}
    )
    public void test_setIssuerNamesLjava_util_Collection01() throws IOException {
        X509CRLSelector selector = new X509CRLSelector();
        selector.setIssuerNames(new TreeSet<Comparable>() {
            private static final long serialVersionUID = 6009545505321092498L;
            public Iterator<Comparable> iterator() {
                return null;
            }
        });
    }
}
