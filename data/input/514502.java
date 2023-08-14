public class BluetoothPbapRfcommTransport implements ObexTransport {
    private BluetoothSocket mSocket = null;
    public BluetoothPbapRfcommTransport(BluetoothSocket rfs) {
        super();
        this.mSocket = rfs;
    }
    public void close() throws IOException {
        mSocket.close();
    }
    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }
    public DataOutputStream openDataOutputStream() throws IOException {
        return new DataOutputStream(openOutputStream());
    }
    public InputStream openInputStream() throws IOException {
        return mSocket.getInputStream();
    }
    public OutputStream openOutputStream() throws IOException {
        return mSocket.getOutputStream();
    }
    public void connect() throws IOException {
    }
    public void create() throws IOException {
    }
    public void disconnect() throws IOException {
    }
    public void listen() throws IOException {
    }
    public boolean isConnected() throws IOException {
        return true;
    }
}
