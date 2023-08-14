abstract class CodeGen implements IOpcodes
{
    public static void load_local_object_var (final ByteArrayOStream out, final int index)
    {
        if (index <= 3)
        {
            out.write (_aload_0 + index); 
        }
        else if (index <= 0xFF)
        {
            out.write2 (_aload,
                        index);  
        }
        else
        {
            out.write4 (_wide,
                        _aload,
                        index >>> 8,    
                        index);         
        }
    }
    public static void store_local_object_var (final ByteArrayOStream out, final int index)
    {
        if (index <= 3)
        {
            out.write (_astore_0 + index); 
        }
        else if (index <= 0xFF)
        {
            out.write2 (_astore,
                        index);  
        }
        else
        {
            out.write4 (_wide,
                        _astore,
                        index >>> 8,    
                        index);         
        }
    }
    public static void push_int_value (final ByteArrayOStream out, final ClassDef cls, final int value)
    {
        if ((-1 <= value) && (value <= 5))
        {
            out.write (_iconst_0 + value);
        }
        else if ((-128 <= value) && (value <= 127))
        {
            out.write2 (_bipush,
                        value); 
        }
        else if ((-32768 <= value) && (value <= 32767))
        {
            out.write3 (_sipush,
                        value >>> 8,    
                        value);         
        }
        else 
        {
            final int index = cls.getConstants ().add (new CONSTANT_Integer_info (value));
            if (index <= 0xFF)
            {
                out.write2 (_ldc,
                            index);  
            }
            else 
            {
                out.write3 (_ldc_w,
                            index >>> 8,  
                            index);       
            }
        }
    }
    public static void push_constant_index (final ByteArrayOStream out, final int index)
    {
        if (index <= 0xFF)
        {
            out.write2 (_ldc,
                       index);  
        }
        else
        {
            out.write3 (_ldc_w,
                        index >>> 8,     
                        index);          
        }
    }
    private CodeGen () {} 
} 
