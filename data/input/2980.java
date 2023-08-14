public class HandshakeOutStream extends OutputStream {
    private SSLSocketImpl socket;
    private SSLEngineImpl engine;
    OutputRecord r;
    HandshakeOutStream(ProtocolVersion protocolVersion,
            ProtocolVersion helloVersion, HandshakeHash handshakeHash,
            SSLSocketImpl socket) {
        this.socket = socket;
        r = new OutputRecord(Record.ct_handshake);
        init(protocolVersion, helloVersion, handshakeHash);
    }
    HandshakeOutStream(ProtocolVersion protocolVersion,
            ProtocolVersion helloVersion, HandshakeHash handshakeHash,
            SSLEngineImpl engine) {
        this.engine = engine;
        r = new EngineOutputRecord(Record.ct_handshake, engine);
        init(protocolVersion, helloVersion, handshakeHash);
    }
    private void init(ProtocolVersion protocolVersion,
            ProtocolVersion helloVersion, HandshakeHash handshakeHash) {
        r.setVersion(protocolVersion);
        r.setHelloVersion(helloVersion);
        r.setHandshakeHash(handshakeHash);
    }
    void doHashes() {
        r.doHashes();
    }
    public void write(byte buf[], int off, int len) throws IOException {
        while (len > 0) {
            int howmuch = Math.min(len, r.availableDataBytes());
            if (howmuch == 0) {
                flush();
            } else {
                r.write(buf, off, howmuch);
                off += howmuch;
                len -= howmuch;
            }
        }
    }
    public void write(int i) throws IOException {
        if (r.availableDataBytes() < 1) {
            flush();
        }
        r.write(i);
    }
    public void flush() throws IOException {
        if (socket != null) {
            try {
                socket.writeRecord(r);
            } catch (IOException e) {
                socket.waitForClose(true);
                throw e;
            }
        } else {  
            engine.writeRecord((EngineOutputRecord)r);
        }
    }
    void setFinishedMsg() {
        assert(socket == null);
        ((EngineOutputRecord)r).setFinishedMsg();
    }
    void putInt8(int i) throws IOException {
        r.write(i);
    }
    void putInt16(int i) throws IOException {
        if (r.availableDataBytes() < 2) {
            flush();
        }
        r.write(i >> 8);
        r.write(i);
    }
    void putInt24(int i) throws IOException {
        if (r.availableDataBytes() < 3) {
            flush();
        }
        r.write(i >> 16);
        r.write(i >> 8);
        r.write(i);
    }
    void putInt32(int i) throws IOException {
        if (r.availableDataBytes() < 4) {
            flush();
        }
        r.write(i >> 24);
        r.write(i >> 16);
        r.write(i >> 8);
        r.write(i);
    }
    void putBytes8(byte b[]) throws IOException {
        if (b == null) {
            putInt8(0);
            return;
        }
        putInt8(b.length);
        write(b, 0, b.length);
    }
    public void putBytes16(byte b[]) throws IOException {
        if (b == null) {
            putInt16(0);
            return;
        }
        putInt16(b.length);
        write(b, 0, b.length);
    }
    void putBytes24(byte b[]) throws IOException {
        if (b == null) {
            putInt24(0);
            return;
        }
        putInt24(b.length);
        write(b, 0, b.length);
    }
}
