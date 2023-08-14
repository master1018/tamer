final class HTMLDocument extends IElement.Factory.ElementImpl
{
    public HTMLDocument ()
    {
        this (null, null);
    }
    public HTMLDocument (final String title, final String encoding)
    {
        super (Tag.HTML, AttributeSet.create ());
        super.add (m_head = IElement.Factory.create (Tag.HEAD));
        super.add (m_body = IElement.Factory.create (Tag.BODY));
        if ((encoding != null) && (encoding.length () != 0))
        {
            final ISimpleElement meta = ISimpleElement.Factory.create (Tag.META);
            meta.getAttributes ()
            .set (Attribute.HTTP_EQUIV, "Content-Type")
            .set (Attribute.CONTENT, "text/html; charset=" + encoding);
            m_head.add (meta);
        }
        if (title != null)
        {
            final IElement titleElement = IElement.Factory.create (Tag.TITLE).setText (title, false);
            m_head.add (titleElement);            
        }
        m_title = title;
    }
    public String getTitle ()
    {
        return m_title;
    }
    public IElement getHead ()
    {
        return m_head; 
    }
    public IElement getBody ()
    {
        return m_body;
    }
    public IContent getHeader ()
    {
        return m_header;
    }
    public IContent getFooter ()
    {
        return m_footer;
    }
    public void setHeader (final IContent header)
    {
        if (header != null) m_header = header;
    }
    public void setFooter (final IContent footer)
    {
        if (footer != null) m_footer = footer;
    }
    public void emit (HTMLWriter out)
    {
        if (m_header != null) m_body.add (0, m_header);
        if (m_footer != null) m_body.add (m_body.size (), m_footer);
        super.emit(out);
    }
    public IElementList add (final IContent content)
    {
        m_body.add (content);
        return this;
    }
    public void addStyle (final String css)
    {
        if (css != null)
        {
            final IElement style = IElement.Factory.create (Tag.STYLE);
            style.getAttributes ().set (Attribute.TYPE, "text/css");
            final StringBuffer def = new StringBuffer ("<!--");
            def.append (IConstants.EOL);
            style.setText (css, false);
            def.append (IConstants.EOL);
            def.append ("-->");
            m_head.add (style);
        }
    }
    public void addLINK (final String type, final String href)
    {
        final ISimpleElement link = ISimpleElement.Factory.create (Tag.LINK);
        link.getAttributes ().set (Attribute.TYPE, type); 
        link.getAttributes ().set (Attribute.HREF, href); 
        link.getAttributes ().set (Attribute.SRC, href); 
        m_head.add (link);
    }
    public void addH (final int level, final String text, final String classID)
    {
        final Tag Hl = Tag.Hs [level];
        final IElement h = IElement.Factory.create (Hl);
        h.setText (text, true);
        h.setClass (classID);
        add (h);
    }
    public void addH (final int level, final IContent text, final String classID)
    {
        final Tag Hl = Tag.Hs [level];
        final IElement h = IElement.Factory.create (Hl);
        h.add (text);
        h.setClass (classID);
        add (h);
    }
    public void addHR (final int size)
    {
        final IElement hr = IElement.Factory.create (Tag.HR);
        hr.getAttributes ().set (Attribute.SIZE, size);
        add (hr);
    }
    public void addEmptyP ()
    {
        add (IElement.Factory.create (Tag.P));
    }
    private final String m_title;
    private final IElement m_head;
    private final IElement m_body;
    private IContent m_header, m_footer;
} 
