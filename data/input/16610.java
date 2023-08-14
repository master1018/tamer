public abstract class CertPathBuilderSpi {
    public CertPathBuilderSpi() { }
    public abstract CertPathBuilderResult engineBuild(CertPathParameters params)
        throws CertPathBuilderException, InvalidAlgorithmParameterException;
}
