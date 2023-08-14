@TestTargetClass(PKIXCertPathChecker.class)
public class PKIXCertPathCheckerTest extends TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "PKIXCertPathChecker",
        args = {}
    )
    public final void testConstructor() {
        try {
            new MyPKIXCertPathChecker();
        } catch(Exception e) {
            fail("Unexpected exception " + e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public final void testClone() {
        PKIXCertPathChecker pc1 = TestUtils.getTestCertPathChecker();
        PKIXCertPathChecker pc2 = (PKIXCertPathChecker) pc1.clone();
        assertNotSame("notSame", pc1, pc2);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isForwardCheckingSupported",
        args = {}
    )
    public final void testIsForwardCheckingSupported() {
        PKIXCertPathChecker pc = TestUtils.getTestCertPathChecker();
        pc.isForwardCheckingSupported();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "init",
        args = {boolean.class}
    )
    public final void testInit()
        throws CertPathValidatorException {
        PKIXCertPathChecker pc = TestUtils.getTestCertPathChecker();
        pc.init(true);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getSupportedExtensions",
        args = {}
    )
    public final void testGetSupportedExtensions() {
        PKIXCertPathChecker pc = TestUtils.getTestCertPathChecker();
        pc.getSupportedExtensions();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "check",
        args = {java.security.cert.Certificate.class, java.util.Collection.class}
    )
    public final void testCheck() throws CertPathValidatorException {
        PKIXCertPathChecker pc = TestUtils.getTestCertPathChecker();
        pc.check(new MyCertificate("", null), new HashSet<String>());
    }
    class MyPKIXCertPathChecker extends PKIXCertPathChecker {
        public MyPKIXCertPathChecker() {
            super();
        }
        @Override
        public void check(Certificate cert,
                Collection<String> unresolvedCritExts)
        throws CertPathValidatorException {
        }
        @Override
        public Set<String> getSupportedExtensions() {
            return null;
        }
        @Override
        public void init(boolean forward) throws CertPathValidatorException {
        }
        @Override
        public boolean isForwardCheckingSupported() {
            return false;
        }
    }
}
