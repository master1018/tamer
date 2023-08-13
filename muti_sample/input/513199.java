final class CONSTANT_Fieldref_info extends CONSTANT_ref_info
{
    public static final byte TAG = 9;
    public CONSTANT_Fieldref_info (final int class_index, final int name_and_type_index)
    {
        super (class_index, name_and_type_index);
    }
    public final byte tag ()
    {
        return TAG;
    }
    public Object accept (final ICONSTANTVisitor visitor, final Object ctx)
    {
        return visitor.visit (this, ctx);
    }
    public String toString ()
    {
        return "CONSTANT_Fieldref: [class_index = " + m_class_index + ", name_and_type_index = " + m_name_and_type_index + ']';
    }
    protected CONSTANT_Fieldref_info (final UDataInputStream bytes) throws IOException
    {
        super (bytes);
    }
} 
