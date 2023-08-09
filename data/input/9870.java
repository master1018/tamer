public class ReasonTest {
    private static volatile boolean failed = false;
    public static void main(String[] args) throws Exception {
        CertPathValidatorException cpve = new CertPathValidatorException("abc");
        if (cpve.getReason() != BasicReason.UNSPECIFIED) {
            failed = true;
            System.err.println("FAILED: unexpected reason: " + cpve.getReason());
        }
        cpve = new CertPathValidatorException
            ("abc", null, null, -1, BasicReason.REVOKED);
        if (cpve.getReason() != BasicReason.REVOKED) {
            failed = true;
            System.err.println("FAILED: unexpected reason: " + cpve.getReason());
        }
        try {
            cpve = new CertPathValidatorException("abc", null, null, -1, null);
            failed = true;
            System.err.println("ctor did not throw NPE for null reason");
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                failed = true;
                System.err.println("FAILED: unexpected exception: " + e);
            }
        }
        if (failed) {
            throw new Exception("Some tests FAILED");
        }
    }
}
