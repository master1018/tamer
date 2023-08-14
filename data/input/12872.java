public class CharTrie extends Trie
{
    public CharTrie(InputStream inputStream,
                    DataManipulate dataManipulate) throws IOException
    {
        super(inputStream, dataManipulate);
        if (!isCharTrie()) {
            throw new IllegalArgumentException(
                               "Data given does not belong to a char trie.");
        }
        m_friendAgent_ = new FriendAgent();
    }
    public CharTrie(int initialValue, int leadUnitValue, DataManipulate dataManipulate) {
        super(new char[BMP_INDEX_LENGTH+SURROGATE_BLOCK_COUNT], HEADER_OPTIONS_LATIN1_IS_LINEAR_MASK_, dataManipulate);
        int dataLength, latin1Length, i, limit;
        char block;
        dataLength=latin1Length= INDEX_STAGE_1_SHIFT_<=8 ? 256 : DATA_BLOCK_LENGTH;
        if(leadUnitValue!=initialValue) {
            dataLength+=DATA_BLOCK_LENGTH;
        }
        m_data_=new char[dataLength];
        m_dataLength_=dataLength;
        m_initialValue_=(char)initialValue;
        for(i=0; i<latin1Length; ++i) {
            m_data_[i]=(char)initialValue;
        }
        if(leadUnitValue!=initialValue) {
            block=(char)(latin1Length>>INDEX_STAGE_2_SHIFT_);
            i=0xd800>>INDEX_STAGE_1_SHIFT_;
            limit=0xdc00>>INDEX_STAGE_1_SHIFT_;
            for(; i<limit; ++i) {
                m_index_[i]=block;
            }
            limit=latin1Length+DATA_BLOCK_LENGTH;
            for(i=latin1Length; i<limit; ++i) {
                m_data_[i]=(char)leadUnitValue;
            }
        }
        m_friendAgent_ = new FriendAgent();
    }
    public class FriendAgent
    {
        public char[] getPrivateIndex()
        {
            return m_index_;
        }
        public char[] getPrivateData()
        {
            return m_data_;
        }
        public int getPrivateInitialValue()
        {
            return m_initialValue_;
        }
    }
    public void putIndexData(UCharacterProperty friend)
    {
        friend.setIndexData(m_friendAgent_);
    }
    public final char getCodePointValue(int ch)
    {
        int offset;
        if(0 <= ch && ch < UTF16.LEAD_SURROGATE_MIN_VALUE) {
            offset = (m_index_[ch >> INDEX_STAGE_1_SHIFT_] << INDEX_STAGE_2_SHIFT_)
                    + (ch & INDEX_STAGE_3_MASK_);
            return m_data_[offset];
        }
        offset = getCodePointOffset(ch);
        return (offset >= 0) ? m_data_[offset] : m_initialValue_;
    }
    public final char getLeadValue(char ch)
    {
       return m_data_[getLeadOffset(ch)];
    }
    public final char getSurrogateValue(char lead, char trail)
    {
        int offset = getSurrogateOffset(lead, trail);
        if (offset > 0) {
            return m_data_[offset];
        }
        return m_initialValue_;
    }
    public final char getTrailValue(int leadvalue, char trail)
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
        DataInputStream input = new DataInputStream(inputStream);
        int indexDataLength = m_dataOffset_ + m_dataLength_;
        m_index_ = new char[indexDataLength];
        for (int i = 0; i < indexDataLength; i ++) {
            m_index_[i] = input.readChar();
        }
        m_data_           = m_index_;
        m_initialValue_   = m_data_[m_dataOffset_];
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
    private char m_initialValue_;
    private char m_data_[];
    private FriendAgent m_friendAgent_;
}
