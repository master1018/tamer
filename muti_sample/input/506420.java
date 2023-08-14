public class SignedData {
    private int version;
    private List digestAlgorithms;
    private ContentInfo contentInfo;
    private List certificates;
    private List crls;
    private List signerInfos;
    public SignedData(int version, List digestAlgorithms, ContentInfo contentInfo,
            List certificates, List crls, List signerInfos) {
        this.version = version;
        this.digestAlgorithms = digestAlgorithms;
        this.contentInfo = contentInfo;
        this.certificates = certificates;
        this.crls = crls;
        this.signerInfos = signerInfos;
    }
    public List getCertificates() {
        return certificates;
    }
    public List getCRLs() {
        return crls;
    }
    public List getSignerInfos() {
        return signerInfos;
    }
    public ContentInfo getContentInfo() {
        return contentInfo;
    }
    public List getDigestAlgorithms() {
        return digestAlgorithms;
    }
    public int getVersion() {
        return version;
    }
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("---- SignedData:"); 
        res.append("\nversion: "); 
        res.append(version);
        res.append("\ndigestAlgorithms: "); 
        res.append(digestAlgorithms.toString());
        res.append("\ncontentInfo: "); 
        res.append(contentInfo.toString());
        res.append("\ncertificates: "); 
        if (certificates != null) {
            res.append(certificates.toString());
        }
        res.append("\ncrls: "); 
        if (crls != null) {
            res.append(crls.toString());
        }
        res.append("\nsignerInfos:\n"); 
        res.append(signerInfos.toString());
        res.append("\n---- SignedData End\n]"); 
        return res.toString();
    }
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            ASN1Integer.getInstance(), 
            new ASN1SetOf(AlgorithmIdentifier.ASN1),
            ContentInfo.ASN1,
            new ASN1Implicit(0, new ASN1SetOf(Certificate.ASN1)),
            new ASN1Implicit(1, new ASN1SetOf(CertificateList.ASN1)),
            new ASN1SetOf(SignerInfo.ASN1) 
            }) {
        {
            setOptional(3); 
            setOptional(4); 
        }
        protected void getValues(Object object, Object[] values) {
            SignedData sd = (SignedData) object;
            values[0] = new byte[] {(byte)sd.version};
            values[1] = sd.digestAlgorithms;
            values[2] = sd.contentInfo;
            values[3] = sd.certificates;
            values[4] = sd.crls;
            values[5] = sd.signerInfos;
        }
        protected Object getDecodedObject(BerInputStream in) {
            Object[] values = (Object[]) in.content;
            return new SignedData(
                        ASN1Integer.toIntValue(values[0]),
                        (List) values[1], 
                        (ContentInfo) values[2],
                        (List) values[3], 
                        (List) values[4], 
                        (List) values[5]
                    );
        }
    };
}
