final class CONSTANT_NameAndType_info extends CONSTANT_info
{
    public static final byte TAG = 12;
    public int m_name_index;
    public int m_descriptor_index;
    public CONSTANT_NameAndType_info (final int name_index, final int descriptor_index)
    {
        m_name_index = name_index;
        m_descriptor_index = descriptor_index;
    }
    public final byte tag ()
    {
        return TAG;
    }
    public String getName (final ClassDef cls)
    {
        return ((CONSTANT_Utf8_info) cls.getConstants ().get (m_name_index)).m_value;
    }
    public String getDescriptor (final ClassDef cls)
    {
        return ((CONSTANT_Utf8_info) cls.getConstants ().get (m_descriptor_index)).m_value;
    }
    public Object accept (final ICONSTANTVisitor visitor, final Object ctx)
    {
        return visitor.visit (this, ctx);
    }
    public String toString ()
    {
        return "CONSTANT_NameAndType: [name_index = " + m_name_index + ", descriptor_index = " + m_descriptor_index + ']';
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        super.writeInClassFormat (out);
        out.writeU2 (m_name_index);
        out.writeU2 (m_descriptor_index);
    }
    protected CONSTANT_NameAndType_info (final UDataInputStream bytes) throws IOException
    {
        m_name_index = bytes.readU2 ();
        m_descriptor_index = bytes.readU2 ();
    }
} 
