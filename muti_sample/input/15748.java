public class FreshestCRLExtension extends CRLDistributionPointsExtension {
    public static final String NAME = "FreshestCRL";
    public FreshestCRLExtension(List<DistributionPoint> distributionPoints)
        throws IOException {
        super(PKIXExtensions.FreshestCRL_Id, false, distributionPoints, NAME);
    }
    public FreshestCRLExtension(Boolean critical, Object value)
    throws IOException {
        super(PKIXExtensions.FreshestCRL_Id, critical.booleanValue(), value,
            NAME);
    }
    public void encode(OutputStream out) throws IOException {
        super.encode(out, PKIXExtensions.FreshestCRL_Id, false);
    }
}
