final class ElementList implements IElementList
{
    public ElementList ()
    {
        m_contents = new ArrayList ();
    }
    public void emit (final HTMLWriter out)
    {
        for (Iterator c = m_contents.iterator (); c.hasNext (); )
        {
            final IContent content = (IContent) c.next ();
            content.emit (out);
        }
    }
    public IElementList add (final IContent content)
    {
        if (content != null)
        {
            m_contents.add (content);
        }
        return this;
    }
    public IElementList add (final int index, final IContent content)
    {
        if (content != null)
        {
            m_contents.add (index, content);
        }
        return this;
    }
    public int size ()
    {
        return m_contents.size ();
    }
    private final List  m_contents;
} 
