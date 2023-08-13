abstract class CONSTANT_info implements Cloneable, IClassFormatOutput
{
    public abstract byte tag ();
    public abstract Object accept (ICONSTANTVisitor visitor, Object ctx); 
    public abstract String toString ();
    public int width ()
    {
        return 1;
    }
    public static CONSTANT_info new_CONSTANT_info (final UDataInputStream bytes)
        throws IOException
    {
        byte tag = bytes.readByte ();                                                                                   
        switch (tag)
        {
        case CONSTANT_Utf8_info.TAG:
            return new CONSTANT_Utf8_info (bytes);
        case CONSTANT_Integer_info.TAG:
            return new CONSTANT_Integer_info (bytes);
        case CONSTANT_Float_info.TAG:
            return new CONSTANT_Float_info (bytes);
        case CONSTANT_Long_info.TAG:
            return new CONSTANT_Long_info (bytes);
        case CONSTANT_Double_info.TAG:
            return new CONSTANT_Double_info (bytes);
        case CONSTANT_Class_info.TAG:
            return new CONSTANT_Class_info (bytes);
        case CONSTANT_String_info.TAG:
            return new CONSTANT_String_info (bytes);
        case CONSTANT_Fieldref_info.TAG:
            return new CONSTANT_Fieldref_info (bytes);
        case CONSTANT_Methodref_info.TAG:
            return new CONSTANT_Methodref_info (bytes);
        case CONSTANT_InterfaceMethodref_info.TAG:
            return new CONSTANT_InterfaceMethodref_info (bytes);
        case CONSTANT_NameAndType_info.TAG:
            return new CONSTANT_NameAndType_info (bytes);
        default: throw new IllegalStateException ("CONSTANT_info: invalid tag value [" + tag + ']');
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
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {    
        out.writeByte (tag ());
    }
    public static String tagToString (final CONSTANT_info constant)
    {
        switch (constant.tag ())
        {
        case CONSTANT_Utf8_info.TAG:
            return "CONSTANT_Utf8";
        case CONSTANT_Integer_info.TAG:
            return "CONSTANT_Integer";
        case CONSTANT_Float_info.TAG:
            return "CONSTANT_Float";
        case CONSTANT_Long_info.TAG:
            return "CONSTANT_Long";
        case CONSTANT_Double_info.TAG:
            return "CONSTANT_Double";
        case CONSTANT_Class_info.TAG:
            return "CONSTANT_Class";
        case CONSTANT_String_info.TAG:
            return "CONSTANT_String";
        case CONSTANT_Fieldref_info.TAG:
            return "CONSTANT_Fieldref";
        case CONSTANT_Methodref_info.TAG:
            return "CONSTANT_Methodref";
        case CONSTANT_InterfaceMethodref_info.TAG:
            return "CONSTANT_InterfaceMethodref";
        case CONSTANT_NameAndType_info.TAG:
            return "CONSTANT_NameAndType";
        default: throw new IllegalStateException ("CONSTANT_info: invalid tag value [" + constant.tag () + ']');
        } 
    }
    protected CONSTANT_info ()
    {
    }
} 
