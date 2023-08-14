public class CertificateMessage extends Message {
    X509Certificate[] certs;
    byte[][] encoded_certs;
    public CertificateMessage(HandshakeIODataStream in, int length)
            throws IOException {
        int l = in.readUint24(); 
        if (l == 0) {  
            if (length != 3) { 
                fatalAlert(AlertProtocol.DECODE_ERROR,
                        "DECODE ERROR: incorrect CertificateMessage");
            }
            certs = new X509Certificate[0];
            encoded_certs = new byte[0][0];
            this.length = 3;
            return;
        }
        CertificateFactory cf;
        try {
            cf = CertificateFactory.getInstance("X509");
        } catch (CertificateException e) {
            fatalAlert(AlertProtocol.INTERNAL_ERROR, "INTERNAL ERROR", e);
            return;
        }
        Vector<Certificate> certs_vector = new Vector<Certificate>();
        int size = 0;
        int enc_size = 0;
        while (l > 0) {
            size = in.readUint24();
            l -= 3;
            try {
                certs_vector.add(cf.generateCertificate(in));
            } catch (CertificateException e) {
                fatalAlert(AlertProtocol.DECODE_ERROR, "DECODE ERROR", e);
            }
            l -= size;
            enc_size += size;
        }
        certs = new X509Certificate[certs_vector.size()];
        for (int i = 0; i < certs.length; i++) {
            certs[i] = (X509Certificate) certs_vector.elementAt(i);
        }
        this.length = 3 + 3 * certs.length + enc_size;
        if (this.length != length) {
            fatalAlert(AlertProtocol.DECODE_ERROR,
                    "DECODE ERROR: incorrect CertificateMessage");
        }
    }
    public CertificateMessage(X509Certificate[] certs) {
        if (certs == null) {
            this.certs = new X509Certificate[0];
            encoded_certs = new byte[0][0];
            length = 3;
            return;
        }
        this.certs = certs;
        if (encoded_certs == null) {
            encoded_certs = new byte[certs.length][];
            for (int i = 0; i < certs.length; i++) {
                try {
                    encoded_certs[i] = certs[i].getEncoded();
                } catch (CertificateEncodingException e) {
                    fatalAlert(AlertProtocol.INTERNAL_ERROR, "INTERNAL ERROR",
                            e);
                }
            }
        }
        length = 3 + 3 * encoded_certs.length;
        for (int i = 0; i < encoded_certs.length; i++) {
            length += encoded_certs[i].length;
        }
    }
    @Override
    public void send(HandshakeIODataStream out) {
        int total_length = 0;
        if (encoded_certs == null) {
            encoded_certs = new byte[certs.length][];
            for (int i = 0; i < certs.length; i++) {
                try {
                    encoded_certs[i] = certs[i].getEncoded();
                } catch (CertificateEncodingException e) {
                    fatalAlert(AlertProtocol.INTERNAL_ERROR, "INTERNAL ERROR",
                            e);
                }
            }
        }
        total_length = 3 * encoded_certs.length;
        for (int i = 0; i < encoded_certs.length; i++) {
            total_length += encoded_certs[i].length;
        }
        out.writeUint24(total_length);
        for (int i = 0; i < encoded_certs.length; i++) {
            out.writeUint24(encoded_certs[i].length);
            out.write(encoded_certs[i]);
        }
    }
    @Override
    public int getType() {
        return Handshake.CERTIFICATE;
    }
}
