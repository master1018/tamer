public class CertificateRequest extends Message {
    public static final byte RSA_SIGN = 1;
    public static final byte DSS_SIGN = 2;
    public static final byte RSA_FIXED_DH = 3;
    public static final byte DSS_FIXED_DH = 4;
    final byte[] certificate_types;
    X500Principal[] certificate_authorities;
    private String[] types;
    private byte[][] encoded_principals;
    public CertificateRequest(byte[] certificate_types,
            X509Certificate[] accepted) {
        if (accepted == null) {
            fatalAlert(AlertProtocol.INTERNAL_ERROR,
                    "CertificateRequest: array of certificate authority certificates is null");
        }
        this.certificate_types = certificate_types;
        int totalPrincipalsLength = 0;
        certificate_authorities = new X500Principal[accepted.length];
        encoded_principals = new byte[accepted.length][];
        for (int i = 0; i < accepted.length; i++) {
            certificate_authorities[i] = accepted[i].getIssuerX500Principal();
            encoded_principals[i] = certificate_authorities[i].getEncoded();
            totalPrincipalsLength += encoded_principals[i].length + 2;
        }
        length = 3 + certificate_types.length + totalPrincipalsLength;
    }
    public CertificateRequest(HandshakeIODataStream in, int length)
            throws IOException {
        int size = in.readUint8();
        certificate_types = new byte[size];
        in.read(certificate_types, 0, size);
        size = in.readUint16();
        certificate_authorities = new X500Principal[size];
        int totalPrincipalsLength = 0;
        int principalLength = 0;
        Vector<X500Principal> principals = new Vector<X500Principal>();
        while (totalPrincipalsLength < size) {            
            principalLength = in.readUint16(); 
            principals.add(new X500Principal(in));
            totalPrincipalsLength += 2;
            totalPrincipalsLength += principalLength;
        }
        certificate_authorities = new X500Principal[principals.size()];
        for (int i = 0; i < certificate_authorities.length; i++) {
            certificate_authorities[i] = principals.elementAt(i);
        }
        this.length = 3 + certificate_types.length + totalPrincipalsLength;
        if (this.length != length) {
            fatalAlert(AlertProtocol.DECODE_ERROR,
                    "DECODE ERROR: incorrect CertificateRequest");
        }
    }
    @Override
    public void send(HandshakeIODataStream out) {
        out.writeUint8(certificate_types.length);
        for (int i = 0; i < certificate_types.length; i++) {
            out.write(certificate_types[i]);
        }
        int authoritiesLength = 0;
        for (int i = 0; i < certificate_authorities.length; i++) {
            authoritiesLength += encoded_principals[i].length +2;
        }
        out.writeUint16(authoritiesLength);
        for (int i = 0; i < certificate_authorities.length; i++) {
            out.writeUint16(encoded_principals[i].length);
            out.write(encoded_principals[i]);
        }
    }
    @Override
    public int getType() {
        return Handshake.CERTIFICATE_REQUEST;
    }
    public String[] getTypesAsString() {
        if (types == null) {
            types = new String[certificate_types.length];
            for (int i = 0; i < types.length; i++) {
                switch (certificate_types[i]) {
                case 1:
                    types[i] = "RSA";
                    break;
                case 2:
                    types[i] = "DSA";
                    break;
                case 3:
                    types[i] = "DH_RSA";
                    break;
                case 4:
                    types[i] = "DH_DSA";
                    break;
                default:
                    fatalAlert(AlertProtocol.DECODE_ERROR,
                            "DECODE ERROR: incorrect CertificateRequest");
                }
            }
        }
        return types;
    }
}
