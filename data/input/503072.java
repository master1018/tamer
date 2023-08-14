final class Field_info implements Cloneable, IAccessFlags
{
    public int m_name_index;
    public int m_descriptor_index;
    public Field_info (final int access_flags,
                       final int name_index, final int descriptor_index,
                       final IAttributeCollection attributes)
    {
        m_access_flags = access_flags;
        m_name_index = name_index;
        m_descriptor_index = descriptor_index;
        m_attributes = attributes;
    }
    public Field_info (final IConstantCollection constants,
                       final UDataInputStream bytes)
        throws IOException
    {
        m_access_flags = bytes.readU2 ();
        m_name_index = bytes.readU2 ();
        m_descriptor_index = bytes.readU2 ();
        final int attributes_count = bytes.readU2 ();
        m_attributes = ElementFactory.newAttributeCollection (attributes_count);
        for (int i = 0; i < attributes_count; i++)
        {
            final Attribute_info attribute_info = Attribute_info.new_Attribute_info (constants, bytes);
            if (DEBUG) System.out.println ("\t[" + i + "] attribute: " + attribute_info);
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
    public boolean isSynthetic ()
    {
        return m_attributes.hasSynthetic ();
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
        return "field_info: [modifiers: 0x" + Integer.toHexString(m_access_flags) + ", name_index = " + m_name_index + ", descriptor_index = " + m_descriptor_index + ']';
    }
    public Object clone ()
    {
        try
        {
            final Field_info _clone = (Field_info) super.clone ();
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
    private static final boolean DEBUG = false;
} 
