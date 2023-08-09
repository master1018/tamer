public final class UCharacterProperty
{
    public CharTrie m_trie_;
    public char[] m_trieIndex_;
    public char[] m_trieData_;
    public int m_trieInitialValue_;
    public VersionInfo m_unicodeVersion_;
    public static final int SRC_PROPSVEC=2;
    public static final int SRC_COUNT=9;
    public void setIndexData(CharTrie.FriendAgent friendagent)
    {
        m_trieIndex_ = friendagent.getPrivateIndex();
        m_trieData_ = friendagent.getPrivateData();
        m_trieInitialValue_ = friendagent.getPrivateInitialValue();
    }
    public final int getProperty(int ch)
    {
        if (ch < UTF16.LEAD_SURROGATE_MIN_VALUE
            || (ch > UTF16.LEAD_SURROGATE_MAX_VALUE
                && ch < UTF16.SUPPLEMENTARY_MIN_VALUE)) {
            try { 
                return m_trieData_[
                    (m_trieIndex_[ch >> Trie.INDEX_STAGE_1_SHIFT_]
                          << Trie.INDEX_STAGE_2_SHIFT_)
                    + (ch & Trie.INDEX_STAGE_3_MASK_)];
            } catch (ArrayIndexOutOfBoundsException e) {
                return m_trieInitialValue_;
            }
        }
        if (ch <= UTF16.LEAD_SURROGATE_MAX_VALUE) {
            return m_trieData_[
                    (m_trieIndex_[Trie.LEAD_INDEX_OFFSET_
                                  + (ch >> Trie.INDEX_STAGE_1_SHIFT_)]
                          << Trie.INDEX_STAGE_2_SHIFT_)
                    + (ch & Trie.INDEX_STAGE_3_MASK_)];
        }
        if (ch <= UTF16.CODEPOINT_MAX_VALUE) {
            return m_trie_.getSurrogateValue(
                                          UTF16.getLeadSurrogate(ch),
                                          (char)(ch & Trie.SURROGATE_MASK_));
        }
        return m_trieInitialValue_;
    }
    public static int getUnsignedValue(int prop)
    {
        return (prop >> VALUE_SHIFT_) & UNSIGNED_VALUE_MASK_AFTER_SHIFT_;
    }
       public int getAdditional(int codepoint, int column) {
        if (column == -1) {
            return getProperty(codepoint);
        }
           if (column < 0 || column >= m_additionalColumnsCount_) {
           return 0;
       }
       return m_additionalVectors_[
                     m_additionalTrie_.getCodePointValue(codepoint) + column];
       }
    public VersionInfo getAge(int codepoint)
    {
        int version = getAdditional(codepoint, 0) >> AGE_SHIFT_;
        return VersionInfo.getInstance(
                           (version >> FIRST_NIBBLE_SHIFT_) & LAST_NIBBLE_MASK_,
                           version & LAST_NIBBLE_MASK_, 0, 0);
    }
    public static int getRawSupplementary(char lead, char trail)
    {
        return (lead << LEAD_SURROGATE_SHIFT_) + trail + SURROGATE_OFFSET_;
    }
    public static UCharacterProperty getInstance()
    {
        if(INSTANCE_ == null) {
            try {
                INSTANCE_ = new UCharacterProperty();
            }
            catch (Exception e) {
                throw new MissingResourceException(e.getMessage(),"","");
            }
        }
        return INSTANCE_;
    }
    public static boolean isRuleWhiteSpace(int c)
    {
        return (c >= 0x0009 && c <= 0x2029 &&
                (c <= 0x000D || c == 0x0020 || c == 0x0085 ||
                 c == 0x200E || c == 0x200F || c >= 0x2028));
    }
    CharTrie m_additionalTrie_;
    int m_additionalVectors_[];
    int m_additionalColumnsCount_;
    int m_maxBlockScriptValue_;
     int m_maxJTGValue_;
    private static UCharacterProperty INSTANCE_ = null;
    private static final String DATA_FILE_NAME_ = "/sun/text/resources/uprops.icu";
    private static final int DATA_BUFFER_SIZE_ = 25000;
    private static final int VALUE_SHIFT_ = 8;
    private static final int UNSIGNED_VALUE_MASK_AFTER_SHIFT_ = 0xFF;
    private static final int LEAD_SURROGATE_SHIFT_ = 10;
    private static final int SURROGATE_OFFSET_ =
                           UTF16.SUPPLEMENTARY_MIN_VALUE -
                           (UTF16.SURROGATE_MIN_VALUE <<
                           LEAD_SURROGATE_SHIFT_) -
                           UTF16.TRAIL_SURROGATE_MIN_VALUE;
    private static final int FIRST_NIBBLE_SHIFT_ = 0x4;
    private static final int LAST_NIBBLE_MASK_ = 0xF;
    private static final int AGE_SHIFT_ = 24;
    private UCharacterProperty() throws IOException
    {
        InputStream is = ICUData.getRequiredStream(DATA_FILE_NAME_);
        BufferedInputStream b = new BufferedInputStream(is, DATA_BUFFER_SIZE_);
        UCharacterPropertyReader reader = new UCharacterPropertyReader(b);
        reader.read(this);
        b.close();
        m_trie_.putIndexData(this);
    }
    public void upropsvec_addPropertyStarts(UnicodeSet set) {
        if(m_additionalColumnsCount_>0) {
            TrieIterator propsVectorsIter = new TrieIterator(m_additionalTrie_);
            RangeValueIterator.Element propsVectorsResult = new RangeValueIterator.Element();
            while(propsVectorsIter.next(propsVectorsResult)){
                set.add(propsVectorsResult.start);
            }
        }
    }
}
