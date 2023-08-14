public class MacOutputStream extends ByteArrayOutputStream {
    private final Mac mac;
    public MacOutputStream(Mac mac) {
        this.mac = mac;
    }
    public void write(byte[] arg0)  {
        super.write(arg0, 0, arg0.length);
        mac.update(arg0);
    }
    public void write(int arg0) {
        super.write(arg0);
        mac.update((byte) arg0);
    }
    public void write(byte[] arg0, int arg1, int arg2) {
        super.write(arg0, arg1, arg2);
        mac.update(arg0, arg1, arg2);
    }
}
