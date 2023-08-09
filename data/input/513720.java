abstract class CONSTANT_ref_info extends CONSTANT_info
{
    public int m_class_index;
    public int m_name_and_type_index;
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        super.writeInClassFormat (out);
        out.writeU2 (m_class_index);
        out.writeU2 (m_name_and_type_index);
    }
    protected CONSTANT_ref_info (final UDataInputStream bytes)
        throws IOException
    {
        m_class_index = bytes.readU2 ();
        m_name_and_type_index = bytes.readU2 ();
    }
    protected CONSTANT_ref_info (final int class_index, final int name_and_type_index)
    {
        m_class_index = class_index;
        m_name_and_type_index = name_and_type_index;
    }
} 
