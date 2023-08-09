 final class BluetoothInputStream extends InputStream {
    private BluetoothSocket mSocket;
     BluetoothInputStream(BluetoothSocket s) {
        mSocket = s;
    }
    public int available() throws IOException {
        return mSocket.available();
    }
    public void close() throws IOException {
        mSocket.close();
    }
    public int read() throws IOException {
        byte b[] = new byte[1];
        int ret = mSocket.read(b, 0, 1);
        if (ret == 1) {
            return (int)b[0] & 0xff;
        } else {
            return -1;
        }
    }
    public int read(byte[] b, int offset, int length) throws IOException {
        if (b == null) {
            throw new NullPointerException("byte array is null");
        }
        if ((offset | length) < 0 || length > b.length - offset) {
            throw new ArrayIndexOutOfBoundsException("invalid offset or length");
        }
        return mSocket.read(b, offset, length);
    }
}
