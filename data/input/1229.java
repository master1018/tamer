final class DHClientKeyExchange extends HandshakeMessage {
    int messageType() {
        return ht_client_key_exchange;
    }
    private byte dh_Yc[];               
    BigInteger getClientPublicKey() {
        return new BigInteger(1, dh_Yc);
    }
    DHClientKeyExchange(BigInteger publicKey) {
        dh_Yc = toByteArray(publicKey);
    }
    DHClientKeyExchange() {
        dh_Yc = null;
    }
    DHClientKeyExchange(HandshakeInStream input) throws IOException {
        dh_Yc = input.getBytes16();
    }
    int messageLength() {
        if (dh_Yc == null) {
            return 0;
        } else {
            return dh_Yc.length + 2;
        }
    }
    void send(HandshakeOutStream s) throws IOException {
        s.putBytes16(dh_Yc);
    }
    void print(PrintStream s) throws IOException {
        s.println("*** ClientKeyExchange, DH");
        if (debug != null && Debug.isOn("verbose")) {
            Debug.println(s, "DH Public key", dh_Yc);
        }
    }
}
