@TestTargetClass(targets.AlgorithmParameters.OAEP.class)
public class AlgorithmParametersTestOAEP extends AlgorithmParametersTest {
    public AlgorithmParametersTestOAEP() {
        super("OAEP", new AlgorithmParameterAsymmetricHelper("RSA"), new OAEPParameterSpec("SHA-1", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT));
    }
}
