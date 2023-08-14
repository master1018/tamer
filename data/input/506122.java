public class Finished extends Message {
    private byte[] data;
    public Finished(byte[] bytes) {
        data = bytes;
        length = data.length;
    }
    public Finished(HandshakeIODataStream in, int length)  
            throws IOException {
        if (length == 12 || length == 36) {
            data = in.read(length);
            length = data.length;
        } else {
            fatalAlert(AlertProtocol.DECODE_ERROR, "DECODE ERROR: incorrect Finished");
        }
    }
    @Override
    public void send(HandshakeIODataStream out) {
        out.write(data);
    }
    @Override
    public int getType() {
        return Handshake.FINISHED;
    }
    public byte[] getData() {
        return data;
    }
}
