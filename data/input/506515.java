@TestTargetClass(targets.Signatures.SHA512withRSA.class)
public class SignatureTestSHA512withRSA extends SignatureTest {
    public SignatureTestSHA512withRSA() {
        super("SHA512withRSA", "RSA");
    }
}
