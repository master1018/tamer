@TestTargetClass(PKIXCertPathValidatorResult.class)
public class PKIXCertPathValidatorResultTest extends TestCase {
    private static PublicKey testPublicKey = new PublicKey() {
        private static final long serialVersionUID = -737454523739489192L;
        public String getAlgorithm() {
            return "NeverMind";
        }
        public String getFormat() {
            return "NeverMind";
        }
        public byte[] getEncoded() {
            return new byte[] {};
        }
    };
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify NullPointerException.",
        method = "PKIXCertPathValidatorResult",
        args = {java.security.cert.TrustAnchor.class, java.security.cert.PolicyNode.class, java.security.PublicKey.class}
    )
    public final void testPKIXCertPathValidatorResult01()
        throws InvalidKeySpecException,
               NoSuchAlgorithmException {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        new PKIXCertPathValidatorResult(
                ta,
                TestUtils.getPolicyTree(),
                testPublicKey);
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "PKIXCertPathValidatorResult",
        args = {java.security.cert.TrustAnchor.class, java.security.cert.PolicyNode.class, java.security.PublicKey.class}
    )
    public final void testPKIXCertPathValidatorResult02() {
        try {
            new PKIXCertPathValidatorResult(
                    null,
                    TestUtils.getPolicyTree(),
                    testPublicKey);
            fail("NPE expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "PKIXCertPathValidatorResult",
        args = {java.security.cert.TrustAnchor.class, java.security.cert.PolicyNode.class, java.security.PublicKey.class}
    )
    public final void testPKIXCertPathValidatorResult03() {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        try {
            new PKIXCertPathValidatorResult(
                    ta,
                    TestUtils.getPolicyTree(),
                    null);
            fail("NPE expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "PKIXCertPathValidatorResult",
        args = {java.security.cert.TrustAnchor.class, java.security.cert.PolicyNode.class, java.security.PublicKey.class}
    )
    public final void testPKIXCertPathValidatorResult04() throws Exception {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        new PKIXCertPathValidatorResult(
                ta,
                null,
                testPublicKey);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getTrustAnchor",
        args = {}
    )
    public final void testGetTrustAnchor() throws Exception {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        PKIXCertPathValidatorResult vr =
            new PKIXCertPathValidatorResult(
                    ta,
                    null,
                    testPublicKey);
        assertSame(ta, vr.getTrustAnchor());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getPublicKey",
        args = {}
    )
    public final void testGetPublicKey() throws Exception {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        PublicKey pk = testPublicKey;
        PKIXCertPathValidatorResult vr =
            new PKIXCertPathValidatorResult(
                    ta,
                    null,
                    pk);
        assertSame(pk, vr.getPublicKey());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getPolicyTree method returns the root node of the valid policy tree.",
        method = "getPolicyTree",
        args = {}
    )
    public final void testGetPolicyTree01() throws Exception {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        PolicyNode pn = TestUtils.getPolicyTree();
        PKIXCertPathValidatorResult vr =
            new PKIXCertPathValidatorResult(
                    ta,
                    pn,
                    testPublicKey);
        assertSame(pn, vr.getPolicyTree());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies that getPolicyTree method returns null if there are no valid policies.",
        method = "getPolicyTree",
        args = {}
    )
    public final void testGetPolicyTree02() throws Exception {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        PKIXCertPathValidatorResult vr =
            new PKIXCertPathValidatorResult(
                    ta,
                    null,
                    testPublicKey);
        assertNull(vr.getPolicyTree());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public final void testClone() throws Exception {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        PKIXCertPathValidatorResult vr1 =
            new PKIXCertPathValidatorResult(
                    ta,
                    TestUtils.getPolicyTree(),
                    testPublicKey);
        PKIXCertPathValidatorResult vr2 =
            (PKIXCertPathValidatorResult) vr1.clone();
        assertNotSame("notSame", vr1, vr2);
        assertSame("trustAncor", vr1.getTrustAnchor(), vr2.getTrustAnchor());
        assertSame("policyTree", vr1.getPolicyTree(), vr2.getPolicyTree());
        assertSame("publicKey", vr1.getPublicKey(), vr2.getPublicKey());
        byte[] encoding = { 0x01 };
        MyPKIXCertPathBuilderResult my = new MyPKIXCertPathBuilderResult(ta,
                TestUtils.getPolicyTree(), testPublicKey, encoding);
        MyPKIXCertPathBuilderResult myClone = (MyPKIXCertPathBuilderResult) my
                .clone();
        assertSame(my.getPolicyTree(), myClone.getPolicyTree());
        assertSame(my.getPublicKey(), myClone.getPublicKey());
        assertSame(my.getTrustAnchor(), myClone.getTrustAnchor());
        assertSame(my.enc, myClone.enc);
    }
    class MyPKIXCertPathBuilderResult extends PKIXCertPathValidatorResult {
        public byte[] enc; 
        public MyPKIXCertPathBuilderResult(TrustAnchor trustAnchor,
                PolicyNode policyTree, PublicKey subjectPublicKey, byte[] enc) {
            super(trustAnchor, policyTree, subjectPublicKey);
            this.enc = enc;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public final void testToString01() throws Exception {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        PKIXCertPathValidatorResult vr =
            new PKIXCertPathValidatorResult(
                    ta,
                    TestUtils.getPolicyTree(),
                    testPublicKey);
        assertNotNull(vr.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toString",
        args = {}
    )
    public final void testToString02() throws Exception {
        TrustAnchor ta = TestUtils.getTrustAnchor();
        if (ta == null) {
            fail(getName() + ": not performed (could not create test TrustAnchor)");
        }
        PKIXCertPathValidatorResult vr =
            new PKIXCertPathValidatorResult(
                    ta,
                    null,
                    testPublicKey);
        assertNotNull(vr.toString());
    }
}
