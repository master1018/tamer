abstract class Attribute_info implements Cloneable, IClassFormatOutput
{
    public static final String ATTRIBUTE_CODE               = "Code";
    public static final String ATTRIBUTE_CONSTANT_VALUE     = "ConstantValue";
    public static final String ATTRIBUTE_LINE_NUMBER_TABLE  = "LineNumberTable";
    public static final String ATTRIBUTE_EXCEPTIONS         = "Exceptions";
    public static final String ATTRIBUTE_SYNTHETIC          = "Synthetic";
    public static final String ATTRIBUTE_BRIDGE             = "Bridge";
    public static final String ATTRIBUTE_SOURCEFILE         = "SourceFile";
    public static final String ATTRIBUTE_INNERCLASSES       = "InnerClasses";
    public int m_name_index;
    public String getName (final ClassDef cls)
    {
        return ((CONSTANT_Utf8_info) cls.getConstants ().get (m_name_index)).m_value;
    }
    public abstract long length (); 
    public abstract void accept (IAttributeVisitor visitor, Object ctx); 
    public abstract String toString ();
    public static Attribute_info new_Attribute_info (final IConstantCollection constants,
                                                     final UDataInputStream bytes)
        throws IOException
    {
        final int attribute_name_index = bytes.readU2 ();
        final long attribute_length = bytes.readU4 ();
        final CONSTANT_Utf8_info attribute_name = (CONSTANT_Utf8_info) constants.get (attribute_name_index);
        final String name = attribute_name.m_value;
        if (ATTRIBUTE_CODE.equals (name))
        {
            return new CodeAttribute_info (constants, attribute_name_index, attribute_length, bytes);
        }
        else if (ATTRIBUTE_CONSTANT_VALUE.equals (name))
        {
            return new ConstantValueAttribute_info (attribute_name_index, attribute_length, bytes);
        }
        else if (ATTRIBUTE_EXCEPTIONS.equals (name))
        {
            return new ExceptionsAttribute_info (attribute_name_index, attribute_length, bytes);
        }
        else if (ATTRIBUTE_INNERCLASSES.equals (name))
        {
            return new InnerClassesAttribute_info (attribute_name_index, attribute_length, bytes);
        }
        else if (ATTRIBUTE_SYNTHETIC.equals (name))
        {
            return new SyntheticAttribute_info (attribute_name_index, attribute_length);
        }
        else if (ATTRIBUTE_BRIDGE.equals (name))
        {
            return new BridgeAttribute_info (attribute_name_index, attribute_length);
        }
        else if (ATTRIBUTE_LINE_NUMBER_TABLE.equals (name))
        {
            return new LineNumberTableAttribute_info (attribute_name_index, attribute_length, bytes);
        }
        else if (ATTRIBUTE_SOURCEFILE.equals (name))
        {
            return new SourceFileAttribute_info (attribute_name_index, attribute_length, bytes);
        }
        else
        {
            return new GenericAttribute_info (attribute_name_index, attribute_length, bytes);
        }
    }
    public Object clone ()
    {
        try
        {
            return super.clone ();
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError (e.toString ());
        }
    }
    public void writeInClassFormat (UDataOutputStream out) throws IOException
    {   
        out.writeU2 (m_name_index);
        out.writeU4 (length () - 6); 
    }
    protected Attribute_info (final int attribute_name_index, final long attribute_length)
    {
        m_name_index = attribute_name_index;
        m_attribute_length = attribute_length;
    }
    protected long m_attribute_length; 
} 
