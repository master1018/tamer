public class CRLDistributionPointsExtension extends Extension
        implements CertAttrSet<String> {
    public static final String IDENT =
                                "x509.info.extensions.CRLDistributionPoints";
    public static final String NAME = "CRLDistributionPoints";
    public static final String POINTS = "points";
    private List<DistributionPoint> distributionPoints;
    private String extensionName;
    public CRLDistributionPointsExtension(
        List<DistributionPoint> distributionPoints) throws IOException {
        this(false, distributionPoints);
    }
    public CRLDistributionPointsExtension(boolean isCritical,
        List<DistributionPoint> distributionPoints) throws IOException {
        this(PKIXExtensions.CRLDistributionPoints_Id, isCritical,
            distributionPoints, NAME);
    }
    protected CRLDistributionPointsExtension(ObjectIdentifier extensionId,
        boolean isCritical, List<DistributionPoint> distributionPoints,
            String extensionName) throws IOException {
        this.extensionId = extensionId;
        this.critical = isCritical;
        this.distributionPoints = distributionPoints;
        encodeThis();
        this.extensionName = extensionName;
    }
    public CRLDistributionPointsExtension(Boolean critical, Object value)
            throws IOException {
        this(PKIXExtensions.CRLDistributionPoints_Id, critical, value, NAME);
    }
    protected CRLDistributionPointsExtension(ObjectIdentifier extensionId,
        Boolean critical, Object value, String extensionName)
            throws IOException {
        this.extensionId = extensionId;
        this.critical = critical.booleanValue();
        if (!(value instanceof byte[])) {
            throw new IOException("Illegal argument type");
        }
        extensionValue = (byte[])value;
        DerValue val = new DerValue(extensionValue);
        if (val.tag != DerValue.tag_Sequence) {
            throw new IOException("Invalid encoding for " + extensionName +
                                  " extension.");
        }
        distributionPoints = new ArrayList<DistributionPoint>();
        while (val.data.available() != 0) {
            DerValue seq = val.data.getDerValue();
            DistributionPoint point = new DistributionPoint(seq);
            distributionPoints.add(point);
        }
        this.extensionName = extensionName;
    }
    public String getName() {
        return extensionName;
    }
    public void encode(OutputStream out) throws IOException {
        encode(out, PKIXExtensions.CRLDistributionPoints_Id, false);
    }
    protected void encode(OutputStream out, ObjectIdentifier extensionId,
        boolean isCritical) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = extensionId;
            this.critical = isCritical;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }
    public void set(String name, Object obj) throws IOException {
        if (name.equalsIgnoreCase(POINTS)) {
            if (!(obj instanceof List)) {
                throw new IOException("Attribute value should be of type List.");
            }
            distributionPoints = (List<DistributionPoint>)obj;
        } else {
            throw new IOException("Attribute name [" + name +
                                "] not recognized by " +
                                "CertAttrSet:" + extensionName + ".");
        }
        encodeThis();
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(POINTS)) {
            return distributionPoints;
        } else {
            throw new IOException("Attribute name [" + name +
                                "] not recognized by " +
                                "CertAttrSet:" + extensionName + ".");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(POINTS)) {
            distributionPoints = new ArrayList<DistributionPoint>();
        } else {
            throw new IOException("Attribute name [" + name +
                                "] not recognized by " +
                                "CertAttrSet:" + extensionName + ".");
        }
        encodeThis();
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(POINTS);
        return elements.elements();
    }
    private void encodeThis() throws IOException {
        if (distributionPoints.isEmpty()) {
            this.extensionValue = null;
        } else {
            DerOutputStream pnts = new DerOutputStream();
            for (DistributionPoint point : distributionPoints) {
                point.encode(pnts);
            }
            DerOutputStream seq = new DerOutputStream();
            seq.write(DerValue.tag_Sequence, pnts);
            this.extensionValue = seq.toByteArray();
        }
    }
    public String toString() {
        return super.toString() + extensionName + " [\n  "
               + distributionPoints + "]\n";
    }
}
