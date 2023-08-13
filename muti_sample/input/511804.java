final class Method_info implements Cloneable, IAccessFlags
{
    public int m_name_index;
    public int m_descriptor_index;
    public Method_info (int access_flags, int name_index, int descriptor_index, IAttributeCollection attributes)
    {
        m_access_flags = access_flags;
        m_name_index = name_index;
        m_descriptor_index = descriptor_index;
        m_attributes = attributes;
    }
    public Method_info (final IConstantCollection constants,
                        final UDataInputStream bytes)
        throws IOException
    {
        m_access_flags = bytes.readU2 ();
        m_name_index = bytes.readU2 ();
        m_descriptor_index = bytes.readU2 ();
        final int attributes_count = bytes.readU2 ();        
        m_attributes = ElementFactory.newAttributeCollection (attributes_count);
        for (int i = 0; i < attributes_count; ++ i)
        {
            final Attribute_info attribute_info = Attribute_info.new_Attribute_info (constants, bytes);
            m_attributes.add (attribute_info);
        }
    }
    public String getName (final ClassDef cls)
    {
        return ((CONSTANT_Utf8_info) cls.getConstants ().get (m_name_index)).m_value;
    }
    public String getDescriptor (final ClassDef cls)
    {
        return ((CONSTANT_Utf8_info) cls.getConstants ().get (m_descriptor_index)).m_value;
    }
    public boolean isNative ()
    {
        return (m_access_flags & ACC_NATIVE) != 0;
    }
    public boolean isAbstract ()
    {
        return (m_access_flags & ACC_ABSTRACT) != 0;
    }
    public boolean isSynthetic ()
    {
        return m_attributes.hasSynthetic ();
    }
    public boolean isBridge ()
    {
        return ((m_access_flags & ACC_BRIDGE) != 0) || m_attributes.hasBridge ();
    }
    public final void setAccessFlags (final int flags)
    {
        m_access_flags = flags;
    }
    public final int getAccessFlags ()
    {
        return m_access_flags;
    }
    public IAttributeCollection getAttributes ()
    {
        return m_attributes;
    }
    public String toString ()
    {
        StringBuffer s = new StringBuffer ();
        s.append ("method_info: [modifiers: 0x" + Integer.toHexString(m_access_flags) + ", name_index = " + m_name_index + ", descriptor_index = " + m_descriptor_index + "]\n");
        for (int i = 0; i < m_attributes.size (); i++)
        {
            Attribute_info attribute_info = m_attributes.get (i);
            s.append ("\t[" + i + "] attribute: " + attribute_info + "\n");
        }
        return s.toString ();
    }
    public Object clone ()
    {
        try
        {
            final Method_info _clone = (Method_info) super.clone ();
            _clone.m_attributes = (IAttributeCollection) m_attributes.clone ();
            return _clone;
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError (e.toString ());
        }
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        out.writeU2 (m_access_flags);
        out.writeU2 (m_name_index);
        out.writeU2 (m_descriptor_index);
        m_attributes.writeInClassFormat (out);
    }
    private int m_access_flags;
    private IAttributeCollection m_attributes;
} 
