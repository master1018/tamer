public class BluetoothOppRfcommTransport implements ObexTransport {
    private final BluetoothSocket mSocket;
    public BluetoothOppRfcommTransport(BluetoothSocket socket) {
        super();
        this.mSocket = socket;
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
    public String getRemoteAddress() {
        if (mSocket == null)
            return null;
        return mSocket.getRemoteDevice().getAddress();
    }
}
