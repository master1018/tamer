public class IntTrie extends Trie
{
    public IntTrie(InputStream inputStream, DataManipulate datamanipulate)
                                                    throws IOException
    {
        super(inputStream, datamanipulate);
        if (!isIntTrie()) {
            throw new IllegalArgumentException(
                               "Data given does not belong to a int trie.");
        }
    }
    public final int getCodePointValue(int ch)
    {
        int offset = getCodePointOffset(ch);
        return (offset >= 0) ? m_data_[offset] : m_initialValue_;
    }
    public final int getLeadValue(char ch)
    {
        return m_data_[getLeadOffset(ch)];
    }
    public final int getTrailValue(int leadvalue, char trail)
    {
        if (m_dataManipulate_ == null) {
            throw new NullPointerException(
                             "The field DataManipulate in this Trie is null");
        }
        int offset = m_dataManipulate_.getFoldingOffset(leadvalue);
        if (offset > 0) {
            return m_data_[getRawOffset(offset,
                                         (char)(trail & SURROGATE_MASK_))];
        }
        return m_initialValue_;
    }
    protected final void unserialize(InputStream inputStream)
                                                    throws IOException
    {
        super.unserialize(inputStream);
        m_data_               = new int[m_dataLength_];
        DataInputStream input = new DataInputStream(inputStream);
        for (int i = 0; i < m_dataLength_; i ++) {
            m_data_[i] = input.readInt();
        }
        m_initialValue_ = m_data_[0];
    }
    protected final int getSurrogateOffset(char lead, char trail)
    {
        if (m_dataManipulate_ == null) {
            throw new NullPointerException(
                             "The field DataManipulate in this Trie is null");
        }
        int offset = m_dataManipulate_.getFoldingOffset(getLeadValue(lead));
        if (offset > 0) {
            return getRawOffset(offset, (char)(trail & SURROGATE_MASK_));
        }
        return -1;
    }
    protected final int getValue(int index)
    {
      return m_data_[index];
    }
    protected final int getInitialValue()
    {
        return m_initialValue_;
    }
    IntTrie(char index[], int data[], int initialvalue, int options,
            DataManipulate datamanipulate)
    {
        super(index, options, datamanipulate);
        m_data_ = data;
        m_dataLength_ = m_data_.length;
        m_initialValue_ = initialvalue;
    }
    private int m_initialValue_;
    private int m_data_[];
}
