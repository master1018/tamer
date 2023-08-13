public class CertificateVerify extends Message {
    byte[] signedHash;
    public CertificateVerify(byte[] hash) {
        if (hash == null || hash.length == 0) {
            fatalAlert(AlertProtocol.INTERNAL_ERROR,
                    "INTERNAL ERROR: incorrect certificate verify hash");
        }
        this.signedHash = hash;
        length = hash.length + 2;
    }
    public CertificateVerify(HandshakeIODataStream in, int length)
            throws IOException {
        if (length == 0) {
            fatalAlert(AlertProtocol.DECODE_ERROR,
                    "DECODE ERROR: incorrect CertificateVerify");
        } else {
            if (in.readUint16() != length - 2) {
                fatalAlert(AlertProtocol.DECODE_ERROR,
                        "DECODE ERROR: incorrect CertificateVerify");
            }
            signedHash = in.read(length -2);
        }
        this.length = length;
    }
    @Override
    public void send(HandshakeIODataStream out) {
        if (signedHash.length != 0) {
            out.writeUint16(signedHash.length);
            out.write(signedHash);
        }
    }
    @Override
    public int getType() {
        return Handshake.CERTIFICATE_VERIFY;
    }
}
