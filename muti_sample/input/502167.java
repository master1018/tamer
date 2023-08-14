public class StringArrayWrapper {
    private String[] m_string;
    public StringArrayWrapper(String[] arg){
        m_string = arg;
    }
    public String getString(int index){
        return m_string[index];
    }
    public int getLength(){
        return m_string.length;
    }
}
