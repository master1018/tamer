public class UnsyncByteArrayOutputStream extends OutputStream  {
    private static final int INITIAL_SIZE = 8192;
    private static ThreadLocal bufCache = new ThreadLocal() {
        protected synchronized Object initialValue() {
            return new byte[INITIAL_SIZE];
        }
    };
    private byte[] buf;
    private int size = INITIAL_SIZE;
    private int pos = 0;
    public UnsyncByteArrayOutputStream() {
        buf = (byte[])bufCache.get();
    }
    public void write(byte[] arg0) {
        int newPos = pos + arg0.length;
        if (newPos > size) {
            expandSize(newPos);
        }
        System.arraycopy(arg0, 0, buf, pos, arg0.length);
        pos = newPos;
    }
    public void write(byte[] arg0, int arg1, int arg2) {
        int newPos = pos + arg2;
        if (newPos > size) {
            expandSize(newPos);
        }
        System.arraycopy(arg0, arg1, buf, pos, arg2);
        pos = newPos;
    }
    public void write(int arg0) {
        int newPos = pos + 1;
        if (newPos > size) {
            expandSize(newPos);
        }
        buf[pos++] = (byte)arg0;
    }
    public byte[] toByteArray() {
        byte result[] = new byte[pos];
        System.arraycopy(buf, 0, result, 0, pos);
        return result;
    }
    public void reset() {
        pos = 0;
    }
    private void expandSize(int newPos) {
        int newSize = size;
        while (newPos > newSize) {
            newSize = newSize<<2;
        }
        byte newBuf[] = new byte[newSize];
        System.arraycopy(buf, 0, newBuf, 0, pos);
        buf = newBuf;
        size = newSize;
    }
}
