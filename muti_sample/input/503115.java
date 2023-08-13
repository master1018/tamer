public final class EncodingInfo extends Object
{
    private final char m_highCharInContiguousGroup;
    final String name;
    final String javaName;
    private InEncoding m_encoding;
    public boolean isInEncoding(char ch) {
        if (m_encoding == null) {
            m_encoding = new EncodingImpl();
        }
        return m_encoding.isInEncoding(ch); 
    }
    public boolean isInEncoding(char high, char low) {
        if (m_encoding == null) {
            m_encoding = new EncodingImpl();
        }
        return m_encoding.isInEncoding(high, low); 
    }
    public EncodingInfo(String name, String javaName, char highChar)
    {
        this.name = name;
        this.javaName = javaName;
        this.m_highCharInContiguousGroup = highChar;
    }
    private interface InEncoding {
        public boolean isInEncoding(char ch);
        public boolean isInEncoding(char high, char low);
    }
    private class EncodingImpl implements InEncoding {
        public boolean isInEncoding(char ch1) {
            final boolean ret;
            int codePoint = Encodings.toCodePoint(ch1);
            if (codePoint < m_explFirst) {
                if (m_before == null)
                    m_before =
                        new EncodingImpl(
                            m_encoding,
                            m_first,
                            m_explFirst - 1,
                            codePoint);
                ret = m_before.isInEncoding(ch1);
            } else if (m_explLast < codePoint) {
                if (m_after == null)
                    m_after =
                        new EncodingImpl(
                            m_encoding,
                            m_explLast + 1,
                            m_last,
                            codePoint);
                ret = m_after.isInEncoding(ch1);
            } else {
                final int idx = codePoint - m_explFirst;
                if (m_alreadyKnown[idx])
                    ret = m_isInEncoding[idx];
                else {
                    ret = inEncoding(ch1, m_encoding);
                    m_alreadyKnown[idx] = true;
                    m_isInEncoding[idx] = ret;
                }
            }
            return ret;
        }
        public boolean isInEncoding(char high, char low) {
            final boolean ret;
            int codePoint = Encodings.toCodePoint(high,low);
            if (codePoint < m_explFirst) {
                if (m_before == null)
                    m_before =
                        new EncodingImpl(
                            m_encoding,
                            m_first,
                            m_explFirst - 1,
                            codePoint);
                ret = m_before.isInEncoding(high,low);
            } else if (m_explLast < codePoint) {
                if (m_after == null)
                    m_after =
                        new EncodingImpl(
                            m_encoding,
                            m_explLast + 1,
                            m_last,
                            codePoint);
                ret = m_after.isInEncoding(high,low);
            } else {
                final int idx = codePoint - m_explFirst;
                if (m_alreadyKnown[idx])
                    ret = m_isInEncoding[idx];
                else {
                    ret = inEncoding(high, low, m_encoding);
                    m_alreadyKnown[idx] = true;
                    m_isInEncoding[idx] = ret;
                }
            }
            return ret;
        }
        final private String m_encoding;
        final private int m_first;
        final private int m_explFirst;
        final private int m_explLast;
        final private int m_last;
        private InEncoding m_before;
        private InEncoding m_after;
        private static final int RANGE = 128;
        final private boolean m_alreadyKnown[] = new boolean[RANGE];
        final private boolean m_isInEncoding[] = new boolean[RANGE];
        private EncodingImpl() {
            this(javaName, 0, Integer.MAX_VALUE, (char) 0);
        }
        private EncodingImpl(String encoding, int first, int last, int codePoint) {
            m_first = first;
            m_last = last;  
            m_explFirst = codePoint;
            m_explLast = codePoint + (RANGE-1);  
            m_encoding = encoding;
            if (javaName != null)
            {
                if (0 <= m_explFirst && m_explFirst <= 127) {
                    if ("UTF8".equals(javaName)
                        || "UTF-16".equals(javaName)
                        || "ASCII".equals(javaName)
                        || "US-ASCII".equals(javaName)
                        || "Unicode".equals(javaName)
                        || "UNICODE".equals(javaName)
                        || javaName.startsWith("ISO8859")) {
                        for (int unicode = 1; unicode < 127; unicode++) {
                            final int idx = unicode - m_explFirst;
                            if (0 <= idx && idx < RANGE) {
                                m_alreadyKnown[idx] = true;
                                m_isInEncoding[idx] = true;
                            }
                        }
                    }
                }
                if (javaName == null) {
                    for (int idx = 0; idx < m_alreadyKnown.length; idx++) {
                        m_alreadyKnown[idx] = true;
                        m_isInEncoding[idx] = true;
                    }
                }
            }
        }
    }
    private static boolean inEncoding(char ch, String encoding) {
        boolean isInEncoding;
        try {
            char cArray[] = new char[1];
            cArray[0] = ch;
            String s = new String(cArray);
            byte[] bArray = s.getBytes(encoding);
            isInEncoding = inEncoding(ch, bArray);
        } catch (Exception e) {
            isInEncoding = false;
            if (encoding == null)
            	isInEncoding = true;
        }
        return isInEncoding;
    }
    private static boolean inEncoding(char high, char low, String encoding) {
        boolean isInEncoding;
        try {
            char cArray[] = new char[2];
            cArray[0] = high;
            cArray[1] = low;
            String s = new String(cArray);
            byte[] bArray = s.getBytes(encoding);
            isInEncoding = inEncoding(high,bArray);
        } catch (Exception e) {
            isInEncoding = false;
        }
        return isInEncoding;
    } 
    private static boolean inEncoding(char ch, byte[] data) {
        final boolean isInEncoding;
        if (data==null || data.length == 0) {
            isInEncoding = false;
        }
        else {
            if (data[0] == 0)
                isInEncoding = false;
            else if (data[0] == '?' && ch != '?')
                isInEncoding = false;
            else {
                isInEncoding = true; 
            }
        }
        return isInEncoding;
    }
    public final char getHighChar() {
        return m_highCharInContiguousGroup;
    }
}
