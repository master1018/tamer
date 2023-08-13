public class SigningCertificateInfo {
    private byte[] ber = null;
    private ESSCertId[] certId = null;
    public SigningCertificateInfo(byte[] ber) throws IOException {
        parse(ber);
    }
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[\n");
        for (int i = 0; i < certId.length; i++) {
            buffer.append(certId[i].toString());
        }
        buffer.append("\n]");
        return buffer.toString();
    }
    public void parse(byte[] bytes) throws IOException {
        DerValue derValue = new DerValue(bytes);
        if (derValue.tag != DerValue.tag_Sequence) {
            throw new IOException("Bad encoding for signingCertificate");
        }
        DerValue[] certs = derValue.data.getSequence(1);
        certId = new ESSCertId[certs.length];
        for (int i = 0; i < certs.length; i++) {
            certId[i] = new ESSCertId(certs[i]);
        }
        if (derValue.data.available() > 0) {
            DerValue[] policies = derValue.data.getSequence(1);
            for (int i = 0; i < policies.length; i++) {
            }
        }
    }
}
class ESSCertId {
    private static volatile HexDumpEncoder hexDumper;
    private byte[] certHash;
    private GeneralNames issuer;
    private SerialNumber serialNumber;
    ESSCertId(DerValue certId) throws IOException {
        certHash = certId.data.getDerValue().toByteArray();
        if (certId.data.available() > 0) {
            DerValue issuerSerial = certId.data.getDerValue();
            issuer = new GeneralNames(issuerSerial.data.getDerValue());
            serialNumber = new SerialNumber(issuerSerial.data.getDerValue());
        }
    }
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[\n\tCertificate hash (SHA-1):\n");
        if (hexDumper == null) {
            hexDumper = new HexDumpEncoder();
        }
        buffer.append(hexDumper.encode(certHash));
        if (issuer != null && serialNumber != null) {
            buffer.append("\n\tIssuer: " + issuer + "\n");
            buffer.append("\t" + serialNumber);
        }
        buffer.append("\n]");
        return buffer.toString();
    }
}
