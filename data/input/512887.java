final class TextContent implements IContent
{
    public TextContent (final String text)
    {
        m_text = text;
    }   
    public void emit (final HTMLWriter out)
    {
        if (m_text != null)
        {
            out.write (m_text);
        }
    }
    private final String m_text;
} 
