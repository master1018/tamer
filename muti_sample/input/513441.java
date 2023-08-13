public class ServerHello extends Message {
    byte[] server_version = new byte[2];
    byte[] random = new byte[32];
    byte[] session_id;
    CipherSuite cipher_suite;
    byte compression_method;
    public ServerHello(SecureRandom sr, byte[] server_version,
            byte[] session_id, CipherSuite cipher_suite, byte compression_method) {
        long gmt_unix_time = new java.util.Date().getTime() / 1000;
        sr.nextBytes(random);
        random[0] = (byte) ((gmt_unix_time & 0xFF000000) >>> 24);
        random[1] = (byte) ((gmt_unix_time & 0xFF0000) >>> 16);
        random[2] = (byte) ((gmt_unix_time & 0xFF00) >>> 8);
        random[3] = (byte) (gmt_unix_time & 0xFF);
        this.session_id = session_id;
        this.cipher_suite = cipher_suite;
        this.compression_method = compression_method;
        this.server_version = server_version;
        length = 38 + session_id.length;
    }
    public ServerHello(HandshakeIODataStream in, int length) throws IOException {
        server_version[0] = (byte) in.read();
        server_version[1] = (byte) in.read();
        in.read(random, 0, 32);
        int size = in.readUint8();
        session_id = new byte[size];
        in.read(session_id, 0, size);
        byte b0 = (byte) in.read();
        byte b1 = (byte) in.read();
        cipher_suite = CipherSuite.getByCode(b0, b1);
        compression_method = (byte) in.read();
        this.length = 38 + session_id.length;
        if (this.length != length) {
            fatalAlert(AlertProtocol.DECODE_ERROR, "DECODE ERROR: incorrect ServerHello");
        }
    }
    @Override
    public void send(HandshakeIODataStream out) {
        out.write(server_version);
        out.write(random);
        out.writeUint8(session_id.length);
        out.write(session_id);
        out.write(cipher_suite.toBytes());
        out.write(compression_method);
        length = 38 + session_id.length;
    }
    public byte[] getRandom() {
        return random;
    }
    @Override
    public int getType() {
        return Handshake.SERVER_HELLO;
    }
}
