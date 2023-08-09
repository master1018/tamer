final class AttributeCollection implements IAttributeCollection
{
    public final Attribute_info get (final int offset)
    {
        return (Attribute_info) m_attributes.get (offset);
    }
    public final boolean hasSynthetic ()
    {
        return m_syntheticRefCount > 0;
    }
    public final boolean hasBridge ()
    {
        return m_bridgeRefCount > 0;
    }
    public final InnerClassesAttribute_info getInnerClassesAttribute ()
    {
        final int innerClassesAttributeOffset = m_innerClassesAttributeOffset;
        if (innerClassesAttributeOffset < 0)
            return null;
        else
            return (InnerClassesAttribute_info) get (innerClassesAttributeOffset);
    }
    public final int size ()
    {
        return m_attributes.size (); 
    }
    public final long length ()
    {
        long result = 2;
        int _attributes_count = m_attributes.size (); 
        for (int i = 0; i < _attributes_count; i++) result += get (i).length ();
        return result;
    }
    public Object clone ()
    {
        try
        {
            final AttributeCollection _clone = (AttributeCollection) super.clone ();
            final int attributes_count = m_attributes.size (); 
            _clone.m_attributes = new ArrayList (attributes_count);
            for (int a = 0; a < attributes_count; ++ a)
            {
                _clone.m_attributes.add (((Attribute_info) m_attributes.get (a)).clone ());
            }
            return _clone;
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError (e.toString ());
        }        
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        int attributes_count = size ();
        out.writeU2 (attributes_count);
        for (int i = 0; i < attributes_count; i++)
        {
            get (i).writeInClassFormat (out);
        }
    }
    public void accept (final IClassDefVisitor visitor, final Object ctx)
    {
        visitor.visit (this, ctx);
    }
    public int add (final Attribute_info attribute)
    {
        final List attributes = m_attributes;
        final int result = attributes.size ();
        attributes.add (attribute);
        if (attribute instanceof SyntheticAttribute_info)
            ++ m_syntheticRefCount;
        else if (attribute instanceof InnerClassesAttribute_info)
        {
            if (m_innerClassesAttributeOffset >= 0)
                throw new IllegalArgumentException ("this attribute collection already has an InnerClasses attribute");
            m_innerClassesAttributeOffset = result;
        }
        else if (attribute instanceof BridgeAttribute_info)
            ++ m_bridgeRefCount;
        if (DISALLOW_MULTIPLE_SYNTHETIC_ATTRIBUTES && $assert.ENABLED)
            $assert.ASSERT (m_syntheticRefCount >= 0 && m_syntheticRefCount <= 1,
            "bad synthetic attribute count: " + m_syntheticRefCount);
        return result;
    }
    public Attribute_info set (final int offset, final Attribute_info attribute)
    {
        final Attribute_info result = (Attribute_info) m_attributes.set (offset, attribute);
        if (result instanceof SyntheticAttribute_info)
            -- m_syntheticRefCount;
        else if (result instanceof InnerClassesAttribute_info)
            m_innerClassesAttributeOffset = -1;
        else if (result instanceof BridgeAttribute_info)
            -- m_bridgeRefCount;
        if (attribute instanceof SyntheticAttribute_info)
            ++ m_syntheticRefCount;
        else if (attribute instanceof InnerClassesAttribute_info)
            m_innerClassesAttributeOffset = offset;
        else if (attribute instanceof BridgeAttribute_info)
            ++ m_bridgeRefCount;
        if (DISALLOW_MULTIPLE_SYNTHETIC_ATTRIBUTES && $assert.ENABLED)
            $assert.ASSERT (m_syntheticRefCount >= 0 && m_syntheticRefCount <= 1,
            "bad synthetic attribute count: " + m_syntheticRefCount);
        return result; 
    }
    public Attribute_info remove (final int offset)
    {
        final Attribute_info result = (Attribute_info) m_attributes.remove (offset);
        if (result instanceof SyntheticAttribute_info)
            -- m_syntheticRefCount;
        else if (result instanceof InnerClassesAttribute_info)
            m_innerClassesAttributeOffset = -1;
        else if (result instanceof BridgeAttribute_info)
            -- m_bridgeRefCount;
        if (DISALLOW_MULTIPLE_SYNTHETIC_ATTRIBUTES && $assert.ENABLED)
            $assert.ASSERT (m_syntheticRefCount >= 0 && m_syntheticRefCount <= 1,
            "bad synthetic attribute count: " + m_syntheticRefCount);
        return result;
    }
    AttributeCollection (final int capacity)
    {
        m_attributes = capacity < 0 ? new ArrayList () : new ArrayList (capacity);
        m_innerClassesAttributeOffset = -1;
    }
    private List m_attributes; 
    private transient int m_syntheticRefCount, m_bridgeRefCount;
    private transient int m_innerClassesAttributeOffset;
    private static final boolean DISALLOW_MULTIPLE_SYNTHETIC_ATTRIBUTES = false;
} 
