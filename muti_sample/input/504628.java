public class MyCertPathValidatorSpi extends CertPathValidatorSpi {
    private int sw = 0;
    public CertPathValidatorResult engineValidate(CertPath certPath,
            CertPathParameters params) throws CertPathValidatorException,
            InvalidAlgorithmParameterException {
        ++sw; 
        if (certPath == null) {
            if ((sw % 2) == 0) {
                throw new CertPathValidatorException("certPath null");
            }
        }
        if (params == null) {
            if ((sw % 3) == 0) {
                throw new InvalidAlgorithmParameterException("params null");
            }
        }
        return null;
    }
}
