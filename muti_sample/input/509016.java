public class ServerHelloDone extends Message {
    public ServerHelloDone() {    
    }
    public ServerHelloDone(HandshakeIODataStream in, int length)
            throws IOException {
        if (length != 0) {
            fatalAlert(AlertProtocol.DECODE_ERROR, "DECODE ERROR: incorrect ServerHelloDone");
        }
    }
    @Override
    public void send(HandshakeIODataStream out) {
    }
    @Override
    public int length() {
        return 0;
    }
    @Override
    public int getType() {
        return Handshake.SERVER_HELLO_DONE;
    }
}
