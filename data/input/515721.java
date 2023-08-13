public final class ElemDesc
{
    private int m_flags;
    private StringToIntTable m_attrs = null;
    static final int EMPTY = (1 << 1);
    private static final int FLOW = (1 << 2);
    static final int BLOCK = (1 << 3);
    static final int BLOCKFORM = (1 << 4);
    static final int BLOCKFORMFIELDSET = (1 << 5);
    private static final int CDATA = (1 << 6);
    private static final int PCDATA = (1 << 7);
    static final int RAW = (1 << 8);
    private static final int INLINE = (1 << 9);
    private static final int INLINEA = (1 << 10);
    static final int INLINELABEL = (1 << 11);
    static final int FONTSTYLE = (1 << 12);
    static final int PHRASE = (1 << 13);
    static final int FORMCTRL = (1 << 14);
    static final int SPECIAL = (1 << 15);
    static final int ASPECIAL = (1 << 16);
    static final int HEADMISC = (1 << 17);
    static final int HEAD = (1 << 18);
    static final int LIST = (1 << 19);
    static final int PREFORMATTED = (1 << 20);
    static final int WHITESPACESENSITIVE = (1 << 21);
    static final int HEADELEM = (1 << 22);
    static final int HTMLELEM = (1 << 23);
    public static final int ATTRURL = (1 << 1);
    public static final int ATTREMPTY = (1 << 2);
    ElemDesc(int flags)
    {
        m_flags = flags;
    }
    private boolean is(int flags)
    {
        return (m_flags & flags) != 0;
    }
    int getFlags() {
        return m_flags;
    }
    void setAttr(String name, int flags)
    {
        if (null == m_attrs)
            m_attrs = new StringToIntTable();
        m_attrs.put(name, flags);
    }
    public boolean isAttrFlagSet(String name, int flags)
    {
        return (null != m_attrs)
            ? ((m_attrs.getIgnoreCase(name) & flags) != 0)
            : false;
    }
}
