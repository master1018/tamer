public abstract class Trie
{
    public static interface DataManipulate
    {
        public int getFoldingOffset(int value);
    }
    private static class DefaultGetFoldingOffset implements DataManipulate {
        public int getFoldingOffset(int value) {
            return value;
        }
    }
    protected Trie(InputStream inputStream,
                   DataManipulate  dataManipulate) throws IOException
    {
        DataInputStream input = new DataInputStream(inputStream);
        int signature = input.readInt();
        m_options_    = input.readInt();
        if (!checkHeader(signature)) {
            throw new IllegalArgumentException("ICU data file error: Trie header authentication failed, please check if you have the most updated ICU data file");
        }
        if(dataManipulate != null) {
            m_dataManipulate_ = dataManipulate;
        } else {
            m_dataManipulate_ = new DefaultGetFoldingOffset();
        }
        m_isLatin1Linear_ = (m_options_ &
                             HEADER_OPTIONS_LATIN1_IS_LINEAR_MASK_) != 0;
        m_dataOffset_     = input.readInt();
        m_dataLength_     = input.readInt();
        unserialize(inputStream);
    }
    protected Trie(char index[], int options, DataManipulate dataManipulate)
    {
        m_options_ = options;
        if(dataManipulate != null) {
            m_dataManipulate_ = dataManipulate;
        } else {
            m_dataManipulate_ = new DefaultGetFoldingOffset();
        }
        m_isLatin1Linear_ = (m_options_ &
                             HEADER_OPTIONS_LATIN1_IS_LINEAR_MASK_) != 0;
        m_index_ = index;
        m_dataOffset_ = m_index_.length;
    }
    protected static final int LEAD_INDEX_OFFSET_ = 0x2800 >> 5;
    protected static final int INDEX_STAGE_1_SHIFT_ = 5;
    protected static final int INDEX_STAGE_2_SHIFT_ = 2;
    protected static final int DATA_BLOCK_LENGTH=1<<INDEX_STAGE_1_SHIFT_;
    protected static final int INDEX_STAGE_3_MASK_ = DATA_BLOCK_LENGTH - 1;
    protected static final int SURROGATE_BLOCK_BITS=10-INDEX_STAGE_1_SHIFT_;
    protected static final int SURROGATE_BLOCK_COUNT=(1<<SURROGATE_BLOCK_BITS);
    protected static final int BMP_INDEX_LENGTH=0x10000>>INDEX_STAGE_1_SHIFT_;
    protected static final int SURROGATE_MASK_ = 0x3FF;
    protected char m_index_[];
    protected DataManipulate m_dataManipulate_;
    protected int m_dataOffset_;
    protected int m_dataLength_;
    protected abstract int getSurrogateOffset(char lead, char trail);
    protected abstract int getValue(int index);
    protected abstract int getInitialValue();
    protected final int getRawOffset(int offset, char ch)
    {
        return (m_index_[offset + (ch >> INDEX_STAGE_1_SHIFT_)]
                << INDEX_STAGE_2_SHIFT_)
                + (ch & INDEX_STAGE_3_MASK_);
    }
    protected final int getBMPOffset(char ch)
    {
        return (ch >= UTF16.LEAD_SURROGATE_MIN_VALUE
                && ch <= UTF16.LEAD_SURROGATE_MAX_VALUE)
                ? getRawOffset(LEAD_INDEX_OFFSET_, ch)
                : getRawOffset(0, ch);
    }
    protected final int getLeadOffset(char ch)
    {
       return getRawOffset(0, ch);
    }
    protected final int getCodePointOffset(int ch)
    {
        if (ch < 0) {
            return -1;
        } else if (ch < UTF16.LEAD_SURROGATE_MIN_VALUE) {
            return getRawOffset(0, (char)ch);
        } else if (ch < UTF16.SUPPLEMENTARY_MIN_VALUE) {
            return getBMPOffset((char)ch);
        } else if (ch <= UCharacter.MAX_VALUE) {
            return getSurrogateOffset(UTF16.getLeadSurrogate(ch),
                                      (char)(ch & SURROGATE_MASK_));
        } else {
            return -1;
        }
    }
    protected void unserialize(InputStream inputStream) throws IOException
    {
        m_index_              = new char[m_dataOffset_];
        DataInputStream input = new DataInputStream(inputStream);
        for (int i = 0; i < m_dataOffset_; i ++) {
             m_index_[i] = input.readChar();
        }
    }
    protected final boolean isIntTrie()
    {
        return (m_options_ & HEADER_OPTIONS_DATA_IS_32_BIT_) != 0;
    }
    protected final boolean isCharTrie()
    {
        return (m_options_ & HEADER_OPTIONS_DATA_IS_32_BIT_) == 0;
    }
    protected static final int HEADER_OPTIONS_LATIN1_IS_LINEAR_MASK_ = 0x200;
    protected static final int HEADER_SIGNATURE_ = 0x54726965;
    private static final int HEADER_OPTIONS_SHIFT_MASK_ = 0xF;
    protected static final int HEADER_OPTIONS_INDEX_SHIFT_ = 4;
    protected static final int HEADER_OPTIONS_DATA_IS_32_BIT_ = 0x100;
    private boolean m_isLatin1Linear_;
    private int m_options_;
    private final boolean checkHeader(int signature)
    {
        if (signature != HEADER_SIGNATURE_) {
            return false;
        }
        if ((m_options_ & HEADER_OPTIONS_SHIFT_MASK_) !=
                                                    INDEX_STAGE_1_SHIFT_ ||
            ((m_options_ >> HEADER_OPTIONS_INDEX_SHIFT_) &
                                                HEADER_OPTIONS_SHIFT_MASK_)
                                                 != INDEX_STAGE_2_SHIFT_) {
            return false;
        }
        return true;
    }
}
