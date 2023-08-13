@TestTargetClass(targets.Signatures.SHA1withRSA.class)
public class SignatureTestSHA1withRSA extends SignatureTest {
    public SignatureTestSHA1withRSA() {
        super("SHA1withRSA", "RSA");
    }
}
