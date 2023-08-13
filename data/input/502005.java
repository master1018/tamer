public class ServerKeyExchange extends Message {
    final BigInteger par1; 
    final byte[] bytes1;
    final BigInteger par2; 
    final byte[] bytes2;
    final BigInteger par3; 
    final byte[] bytes3;
    final byte[] hash;
    private RSAPublicKey key;
    public ServerKeyExchange(BigInteger par1, BigInteger par2, BigInteger par3,
            byte[] hash) {
        this.par1 = par1;
        this.par2 = par2;
        this.par3 = par3;
        this.hash = hash;
        bytes1 = toUnsignedByteArray(this.par1);
        bytes2 = toUnsignedByteArray(this.par2);
        length = 4 + bytes1.length + bytes2.length;
        if (hash != null) {
            length += 2 + hash.length;
        }
        if (par3 == null) {
            bytes3 = null;
            return;
        }
        bytes3 = toUnsignedByteArray(this.par3);
        length += 2 + bytes3.length;
    }
    public static byte[] toUnsignedByteArray(BigInteger bi) {
        if (bi == null) {
            return null;
        }
        byte[] bb = bi.toByteArray();
        if (bb[0] == 0) {
            byte[] noZero = new byte[bb.length - 1];
            System.arraycopy(bb, 1, noZero, 0, noZero.length);
            return noZero;
        } else {
            return bb;
        }
    }
    public ServerKeyExchange(HandshakeIODataStream in, int length,
            int keyExchange) throws IOException {
        int size = in.readUint16();
        bytes1 = in.read(size);
        par1 = new BigInteger(1, bytes1);
        this.length = 2 + bytes1.length;
        size = in.readUint16();
        bytes2 = in.read(size);
        par2 = new BigInteger(1, bytes2);
        this.length += 2 + bytes2.length;
        if (keyExchange != CipherSuite.KeyExchange_RSA_EXPORT) {
            size = in.readUint16();
            bytes3 = in.read(size);
            par3 = new BigInteger(1, bytes3);
            this.length += 2 + bytes3.length;
        } else {
            par3 = null;
            bytes3 = null;
        }
        if (keyExchange != CipherSuite.KeyExchange_DH_anon_EXPORT
                && keyExchange != CipherSuite.KeyExchange_DH_anon) {
            size = in.readUint16();
            hash = in.read(size);
            this.length += 2 + hash.length;
        } else {
            hash = null;
        }
        if (this.length != length) {
            fatalAlert(AlertProtocol.DECODE_ERROR,
                    "DECODE ERROR: incorrect ServerKeyExchange");
        }
    }
    @Override
    public void send(HandshakeIODataStream out) {
        out.writeUint16(bytes1.length);
        out.write(bytes1);
        out.writeUint16(bytes2.length);
        out.write(bytes2);
        if (bytes3 != null) {
            out.writeUint16(bytes3.length);
            out.write(bytes3);
        }
        if (hash != null) {
            out.writeUint16(hash.length);
            out.write(hash);
        }
    }
    public RSAPublicKey getRSAPublicKey() {
        if (key != null) {
            return key;
        }
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            key = (RSAPublicKey) kf.generatePublic(new RSAPublicKeySpec(par1,
                    par2));
        } catch (Exception e) {
            return null;
        }
        return key;
    }
    @Override
    public int getType() {
        return Handshake.SERVER_KEY_EXCHANGE;
    }
}
