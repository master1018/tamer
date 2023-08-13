final class Text implements IContent
{
    public Text (final String text, final boolean nbsp)
    {
        m_text = text;
        m_nbsp = nbsp;
    }
    public void emit (final HTMLWriter out)
    {
        if (m_text != null)
        {
            if (m_nbsp)
                out.write (Strings.HTMLEscapeSP (m_text));
            else
                out.write (Strings.HTMLEscape (m_text));
        }
    }
    private final String m_text;
    private final boolean m_nbsp;
} 
