public class LongArrayWrapper {
    private long[] m_long;
    public LongArrayWrapper(long[] arg){
        m_long = arg;
    }
    public long getLong(int index){
        return m_long[index];
    }
    public int getLength(){
        return m_long.length;
    }
}
