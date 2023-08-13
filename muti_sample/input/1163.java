class HttpOutputStream extends ByteArrayOutputStream {
    protected OutputStream out;
    boolean responseSent = false;
    public HttpOutputStream(OutputStream out) {
        super();
        this.out = out;
    }
    public synchronized void close() throws IOException {
        if (!responseSent) {
            if (size() == 0)
                write(emptyData);
            DataOutputStream dos = new DataOutputStream(out);
            dos.writeBytes("Content-type: application/octet-stream\r\n");
            dos.writeBytes("Content-length: " + size() + "\r\n");
            dos.writeBytes("\r\n");
            writeTo(dos);
            dos.flush();
            reset(); 
            responseSent = true;
        }
    }
    private static byte[] emptyData = { 0 };
}
