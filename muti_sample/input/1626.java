public class DeltaCRLIndicatorExtension extends CRLNumberExtension {
    public static final String NAME = "DeltaCRLIndicator";
    private static final String LABEL = "Base CRL Number";
    public DeltaCRLIndicatorExtension(int crlNum) throws IOException {
        super(PKIXExtensions.DeltaCRLIndicator_Id, true,
            BigInteger.valueOf(crlNum), NAME, LABEL);
    }
    public DeltaCRLIndicatorExtension(BigInteger crlNum) throws IOException {
        super(PKIXExtensions.DeltaCRLIndicator_Id, true, crlNum, NAME, LABEL);
    }
    public DeltaCRLIndicatorExtension(Boolean critical, Object value)
    throws IOException {
        super(PKIXExtensions.DeltaCRLIndicator_Id, critical.booleanValue(),
            value, NAME, LABEL);
    }
    public void encode(OutputStream out) throws IOException {
       DerOutputStream  tmp = new DerOutputStream();
        super.encode(out, PKIXExtensions.DeltaCRLIndicator_Id, true);
    }
}
