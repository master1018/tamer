public class HelloRequest extends Message {
    public HelloRequest() {
    }
    public HelloRequest(HandshakeIODataStream in, int length)  
            throws IOException {
        if (length != 0) {
            fatalAlert(AlertProtocol.DECODE_ERROR, "DECODE ERROR: incorrect HelloRequest");
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
        return Handshake.HELLO_REQUEST;
    }
}
