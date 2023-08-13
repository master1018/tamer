public class KrbDataInputStream extends BufferedInputStream{
    private boolean bigEndian = true;
    public void setNativeByteOrder() {
        if (java.nio.ByteOrder.nativeOrder().
                equals(java.nio.ByteOrder.BIG_ENDIAN)) {
            bigEndian = true;
        } else {
            bigEndian = false;
        }
    }
    public KrbDataInputStream(InputStream is){
        super(is);
    }
    public int read(int num) throws IOException{
        byte[] bytes = new byte[num];
        read(bytes, 0, num);
        int result = 0;
        for (int i = 0; i < num; i++) {
            if (bigEndian) {
                result |= (bytes[i] & 0xff) << (num - i - 1) * 8;
            } else {
                result |= (bytes[i] & 0xff) << i * 8;
            }
        }
        return result;
    }
    public int readVersion() throws IOException {
        int result = (read() & 0xff) << 8;
        return result | (read() & 0xff);
    }
}
