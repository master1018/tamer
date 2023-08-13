public class Test6852078 {
    public Test6852078(String [] args) {
        int capacity = 128;
        ByteBuffer bb = ByteBuffer.allocateDirect(capacity);
        ByteBufferWithInfo bbwi = new ByteBufferWithInfo( CorbaUtils.getOrb(null, -1, new Hashtable()), bb);
        byte[] tmpBuf;
        tmpBuf = new byte[bbwi.buflen];
        for (int i = 0; i < capacity; i++)
            tmpBuf[i] = bbwi.byteBuffer.get(i);
    }
    public static void main(String [] args) {
        for (int i=0; i<2000; i++) {
            Test6852078 t = new Test6852078(args);
        }
    }
}
