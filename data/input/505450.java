public class ClientKeyExchange extends Message {
    final byte[] exchange_keys;
    boolean isTLS;
    final boolean isRSA;
    public ClientKeyExchange(byte[] encrypted_pre_master_secret, boolean isTLS) {
        this.exchange_keys = encrypted_pre_master_secret;    
        length = this.exchange_keys.length;
        if (isTLS) {
            length += 2;
        }
        this.isTLS = isTLS;
        isRSA = true;
    }
    public ClientKeyExchange(BigInteger dh_Yc) {
        byte[] bb = dh_Yc.toByteArray();
        if (bb[0] == 0) {
            exchange_keys = new byte[bb.length-1]; 
            System.arraycopy(bb, 1, exchange_keys, 0, exchange_keys.length);
        } else {
            exchange_keys = bb;
        }
        length = exchange_keys.length +2;
        isRSA = false;
    }
    public ClientKeyExchange() {
        exchange_keys = new byte[0];
        length = 0;
        isRSA = false;
    }
    public ClientKeyExchange(HandshakeIODataStream in, int length, boolean isTLS, boolean isRSA)
            throws IOException {
        this.isTLS = isTLS;
        this.isRSA = isRSA;
        if (length == 0) {
            this.length = 0;
            exchange_keys = new byte[0];
        } else {
            int size;
            if (isRSA && !isTLS) {
                size = length;
                this.length = size;
            } else { 
                size = in.readUint16();
                this.length = 2 + size;
            }
            exchange_keys = new byte[size];
            in.read(exchange_keys, 0, size);
            if (this.length != length) {
                fatalAlert(AlertProtocol.DECODE_ERROR, "DECODE ERROR: incorrect ClientKeyExchange");
            }
        }
    }
    @Override
    public void send(HandshakeIODataStream out) {
        if (exchange_keys.length != 0) {    
            if (!isRSA || isTLS) {
                out.writeUint16(exchange_keys.length);
            }
            out.write(exchange_keys);
        }
    }
    @Override
    public int getType() {
        return Handshake.CLIENT_KEY_EXCHANGE;
    }
    public boolean isEmpty() {
        return (exchange_keys.length == 0);
    }
}
