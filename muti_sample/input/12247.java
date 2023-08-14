final class ECDHClientKeyExchange extends HandshakeMessage {
    int messageType() {
        return ht_client_key_exchange;
    }
    private byte[] encodedPoint;
    byte[] getEncodedPoint() {
        return encodedPoint;
    }
    ECDHClientKeyExchange(PublicKey publicKey) {
        ECPublicKey ecKey = (ECPublicKey)publicKey;
        ECPoint point = ecKey.getW();
        ECParameterSpec params = ecKey.getParams();
        encodedPoint = JsseJce.encodePoint(point, params.getCurve());
    }
    ECDHClientKeyExchange(HandshakeInStream input) throws IOException {
        encodedPoint = input.getBytes8();
    }
    int messageLength() {
        return encodedPoint.length + 1;
    }
    void send(HandshakeOutStream s) throws IOException {
        s.putBytes8(encodedPoint);
    }
    void print(PrintStream s) throws IOException {
        s.println("*** ECDHClientKeyExchange");
        if (debug != null && Debug.isOn("verbose")) {
            Debug.println(s, "ECDH Public value", encodedPoint);
        }
    }
}
