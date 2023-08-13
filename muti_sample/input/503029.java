public class MyCertPathBuilderSpi extends CertPathBuilderSpi {
    private int swi = 0;
    public CertPathBuilderResult engineBuild(CertPathParameters params)
            throws CertPathBuilderException, InvalidAlgorithmParameterException {
        swi++;
        if ((params == null) && ((swi %2 ) != 0)) {
            throw new CertPathBuilderException("Null parameter");
        }
        return null;
    }
}