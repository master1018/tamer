final class UCharacterPropertyReader implements ICUBinary.Authenticate
{
    public boolean isDataVersionAcceptable(byte version[])
    {
        return version[0] == DATA_FORMAT_VERSION_[0]
               && version[2] == DATA_FORMAT_VERSION_[2]
               && version[3] == DATA_FORMAT_VERSION_[3];
    }
    protected UCharacterPropertyReader(InputStream inputStream)
                                                        throws IOException
    {
        m_unicodeVersion_ = ICUBinary.readHeader(inputStream, DATA_FORMAT_ID_,
                                                 this);
        m_dataInputStream_ = new DataInputStream(inputStream);
    }
    protected void read(UCharacterProperty ucharppty) throws IOException
    {
        int count = INDEX_SIZE_;
        m_propertyOffset_          = m_dataInputStream_.readInt();
        count --;
        m_exceptionOffset_         = m_dataInputStream_.readInt();
        count --;
        m_caseOffset_              = m_dataInputStream_.readInt();
        count --;
        m_additionalOffset_        = m_dataInputStream_.readInt();
        count --;
        m_additionalVectorsOffset_ = m_dataInputStream_.readInt();
        count --;
        m_additionalColumnsCount_  = m_dataInputStream_.readInt();
        count --;
        m_reservedOffset_          = m_dataInputStream_.readInt();
        count --;
        m_dataInputStream_.skipBytes(3 << 2);
        count -= 3;
        ucharppty.m_maxBlockScriptValue_ = m_dataInputStream_.readInt();
        count --; 
        ucharppty.m_maxJTGValue_ = m_dataInputStream_.readInt();
        count --; 
        m_dataInputStream_.skipBytes(count << 2);
        ucharppty.m_trie_ = new CharTrie(m_dataInputStream_, null);
        int size = m_exceptionOffset_ - m_propertyOffset_;
        m_dataInputStream_.skipBytes(size * 4);
        size = m_caseOffset_ - m_exceptionOffset_;
        m_dataInputStream_.skipBytes(size * 4);
        size = (m_additionalOffset_ - m_caseOffset_) << 1;
        m_dataInputStream_.skipBytes(size * 2);
        if(m_additionalColumnsCount_ > 0) {
            ucharppty.m_additionalTrie_ = new CharTrie(m_dataInputStream_, null);
            size = m_reservedOffset_ - m_additionalVectorsOffset_;
            ucharppty.m_additionalVectors_ = new int[size];
            for (int i = 0; i < size; i ++) {
                ucharppty.m_additionalVectors_[i] = m_dataInputStream_.readInt();
            }
        }
        m_dataInputStream_.close();
        ucharppty.m_additionalColumnsCount_ = m_additionalColumnsCount_;
        ucharppty.m_unicodeVersion_ = VersionInfo.getInstance(
                         (int)m_unicodeVersion_[0], (int)m_unicodeVersion_[1],
                         (int)m_unicodeVersion_[2], (int)m_unicodeVersion_[3]);
    }
    private static final int INDEX_SIZE_ = 16;
    private DataInputStream m_dataInputStream_;
    private int m_propertyOffset_;
    private int m_exceptionOffset_;
    private int m_caseOffset_;
    private int m_additionalOffset_;
    private int m_additionalVectorsOffset_;
    private int m_additionalColumnsCount_;
    private int m_reservedOffset_;
    private byte m_unicodeVersion_[];
    private static final byte DATA_FORMAT_ID_[] = {(byte)0x55, (byte)0x50,
                                                    (byte)0x72, (byte)0x6F};
    private static final byte DATA_FORMAT_VERSION_[] = {(byte)0x5, (byte)0,
                                             (byte)Trie.INDEX_STAGE_1_SHIFT_,
                                             (byte)Trie.INDEX_STAGE_2_SHIFT_};
}
