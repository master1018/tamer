public final class UCharacter
{
    public static interface NumericType
    {
        public static final int DECIMAL = 1;
    }
    public static final int MIN_VALUE = UTF16.CODEPOINT_MIN_VALUE;
    public static final int MAX_VALUE = UTF16.CODEPOINT_MAX_VALUE;
    public static final int SUPPLEMENTARY_MIN_VALUE =
        UTF16.SUPPLEMENTARY_MIN_VALUE;
    public static int digit(int ch, int radix)
    {
        int props = getProperty(ch);
        int value;
        if (getNumericType(props) == NumericType.DECIMAL) {
            value = UCharacterProperty.getUnsignedValue(props);
        } else {
            value = getEuropeanDigit(ch);
        }
        return (0 <= value && value < radix) ? value : -1;
    }
    public static int getDirection(int ch)
    {
        return gBdp.getClass(ch);
    }
    public static int getCodePoint(char lead, char trail)
    {
        if (UTF16.isLeadSurrogate(lead) && UTF16.isTrailSurrogate(trail)) {
            return UCharacterProperty.getRawSupplementary(lead, trail);
        }
        throw new IllegalArgumentException("Illegal surrogate characters");
    }
    public static VersionInfo getAge(int ch)
    {
        if (ch < MIN_VALUE || ch > MAX_VALUE) {
        throw new IllegalArgumentException("Codepoint out of bounds");
        }
        return PROPERTY_.getAge(ch);
    }
    private static final UCharacterProperty PROPERTY_;
    private static final char[] PROPERTY_TRIE_INDEX_;
    private static final char[] PROPERTY_TRIE_DATA_;
    private static final int PROPERTY_INITIAL_VALUE_;
    private static final UBiDiProps gBdp;
    static
    {
        try
        {
            PROPERTY_ = UCharacterProperty.getInstance();
            PROPERTY_TRIE_INDEX_ = PROPERTY_.m_trieIndex_;
            PROPERTY_TRIE_DATA_ = PROPERTY_.m_trieData_;
            PROPERTY_INITIAL_VALUE_ = PROPERTY_.m_trieInitialValue_;
        }
        catch (Exception e)
        {
            throw new MissingResourceException(e.getMessage(),"","");
        }
        UBiDiProps bdp;
        try {
            bdp=UBiDiProps.getSingleton();
        } catch(IOException e) {
            bdp=UBiDiProps.getDummy();
        }
        gBdp=bdp;
    }
    private static final int NUMERIC_TYPE_SHIFT_ = 5;
    private static final int NUMERIC_TYPE_MASK_ = 0x7 << NUMERIC_TYPE_SHIFT_;
    private static int getEuropeanDigit(int ch) {
        if ((ch > 0x7a && ch < 0xff21)
            || ch < 0x41 || (ch > 0x5a && ch < 0x61)
            || ch > 0xff5a || (ch > 0xff3a && ch < 0xff41)) {
            return -1;
        }
        if (ch <= 0x7a) {
            return ch + 10 - ((ch <= 0x5a) ? 0x41 : 0x61);
        }
        if (ch <= 0xff3a) {
            return ch + 10 - 0xff21;
        }
        return ch + 10 - 0xff41;
    }
    private static int getNumericType(int props)
    {
        return (props & NUMERIC_TYPE_MASK_) >> NUMERIC_TYPE_SHIFT_;
    }
    private static final int getProperty(int ch)
    {
        if (ch < UTF16.LEAD_SURROGATE_MIN_VALUE
            || (ch > UTF16.LEAD_SURROGATE_MAX_VALUE
                && ch < UTF16.SUPPLEMENTARY_MIN_VALUE)) {
            try { 
                return PROPERTY_TRIE_DATA_[
                              (PROPERTY_TRIE_INDEX_[ch >> 5] << 2)
                              + (ch & 0x1f)];
            } catch (ArrayIndexOutOfBoundsException e) {
                return PROPERTY_INITIAL_VALUE_;
            }
        }
        if (ch <= UTF16.LEAD_SURROGATE_MAX_VALUE) {
            return PROPERTY_TRIE_DATA_[
                              (PROPERTY_TRIE_INDEX_[(0x2800 >> 5) + (ch >> 5)] << 2)
                              + (ch & 0x1f)];
        }
        if (ch <= UTF16.CODEPOINT_MAX_VALUE) {
            return PROPERTY_.m_trie_.getSurrogateValue(
                                      UTF16.getLeadSurrogate(ch),
                                      (char)(ch & 0x3ff));
        }
        return PROPERTY_INITIAL_VALUE_;
    }
}
