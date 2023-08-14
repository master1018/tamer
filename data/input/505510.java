    ISimpleElement setClass (String classID);
    AttributeSet getAttributes ();     
    abstract class Factory
    {
        public static ISimpleElement create (final Tag tag)
        {
            return new SimpleElementImpl (tag, AttributeSet.create ());
        }
        public static ISimpleElement create (final Tag tag, final AttributeSet attrs)
        {
            return new SimpleElementImpl (tag, attrs);
        }
        static class SimpleElementImpl implements ISimpleElement
        {
            public String toString ()
            {
                return "<" + m_tag.getName () + "/>";
            }
            public Tag getTag ()
            {
                return m_tag;
            }
            public ISimpleElement setClass (final String classID)
            {
                if ((classID != null) && (classID.length () > 0))
                {
                    getAttributes ().set (Attribute.CLASS, classID);
                }
                return this;
            }
            public AttributeSet getAttributes ()
            {
                return m_attrs;
            }
            public void emit (final HTMLWriter out)
            {
                out.write ('<');
                out.write (m_tag.getName ());
                if (! m_attrs.isEmpty ())
                {
                    out.write (' ');
                    m_attrs.emit (out);
                }
                out.write ("/>");
            }
            SimpleElementImpl (final Tag tag, final AttributeSet attrs)
            {
                if ($assert.ENABLED) $assert.ASSERT (tag != null, "tag = null");
                if ($assert.ENABLED) $assert.ASSERT (attrs != null, "attrs = null");
                m_tag = tag;
                m_attrs = attrs;
            }
            protected final Tag m_tag;
            protected final AttributeSet m_attrs;
        } 
    } 
} 
