 final class BluetoothOutputStream extends OutputStream {
    private BluetoothSocket mSocket;
     BluetoothOutputStream(BluetoothSocket s) {
        mSocket = s;
    }
    public void close() throws IOException {
        mSocket.close();
    }
    public void write(int oneByte) throws IOException {
        byte b[] = new byte[1];
        b[0] = (byte)oneByte;
        mSocket.write(b, 0, 1);
    }
    public void write(byte[] b, int offset, int count) throws IOException {
        if (b == null) {
            throw new NullPointerException("buffer is null");
        }
        if ((offset | count) < 0 || count > b.length - offset) {
            throw new IndexOutOfBoundsException("invalid offset or length");
        }
        mSocket.write(b, offset, count);
    }
}
