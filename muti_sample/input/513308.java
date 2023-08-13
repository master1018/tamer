final class CodeAttribute_info extends Attribute_info
{
    public static final byte [] EMPTY_BYTE_ARRAY = new byte [0];
    public int m_max_stack, m_max_locals;
    public CodeAttribute_info (final int attribute_name_index,
                               final int max_stack, int max_locals,
                               final byte [] code, 
                               final IExceptionHandlerTable exceptionHandlerTable,
                               final IAttributeCollection attributes)
    {
        super (attribute_name_index, 8 + (code != null ? code.length : 0) + exceptionHandlerTable.length () + attributes.length ());
        m_max_stack = max_stack;
        m_max_locals = max_locals;
        m_code = (code != null ? code : EMPTY_BYTE_ARRAY);
        m_codeSize = m_code.length;
        m_exceptionHandlerTable = exceptionHandlerTable;
        m_attributes = attributes;
    }
    public final byte [] getCode ()
    {
        return m_code;
    }
    public final int getCodeSize ()
    {
        return m_codeSize;
    }
    public IAttributeCollection getAttributes ()
    {
        return m_attributes;
    }
    public IExceptionHandlerTable getExceptionTable ()
    {
        return m_exceptionHandlerTable;
    }
    public long length ()
    {
        return 14 + m_codeSize + m_exceptionHandlerTable.length () + m_attributes.length ();
    }
    public void accept (final IAttributeVisitor visitor, final Object ctx)
    {
        visitor.visit (this, ctx);
    }
    public String toString ()
    {
        String eol = System.getProperty ("line.separator");
        StringBuffer s = new StringBuffer ();
        s.append ("CodeAttribute_info: [attribute_name_index = " + m_name_index + ", attribute_length = " + m_attribute_length + "]" + eol);
        s.append ("    max_stack/max_locals = " + m_max_stack + '/' + m_max_locals + eol);
        s.append ("    code [length " + m_codeSize + "]" + eol);
        for (int a = 0; a < m_attributes.size (); ++ a)
        {
            s.append ("         " + m_attributes.get (a) + eol);
        }
        return s.toString ();
    }
    public Object clone ()
    {
        final CodeAttribute_info _clone = (CodeAttribute_info) super.clone ();
        _clone.m_code = (m_codeSize == 0 ? EMPTY_BYTE_ARRAY : (byte []) m_code.clone ()); 
        _clone.m_exceptionHandlerTable = (IExceptionHandlerTable) m_exceptionHandlerTable.clone ();
        _clone.m_attributes = (IAttributeCollection) m_attributes.clone ();
        return _clone;
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        super.writeInClassFormat (out);
        out.writeU2 (m_max_stack);
        out.writeU2 (m_max_locals);
        out.writeU4 (m_codeSize);
        out.write (m_code, 0, m_codeSize); 
        m_exceptionHandlerTable.writeInClassFormat (out);
        m_attributes.writeInClassFormat (out);
    }
    public void setCode (final byte [] code, final int codeSize)
    {
        m_code = code;
        m_codeSize = codeSize;
    }
    CodeAttribute_info (final IConstantCollection constants,
                        final int attribute_name_index, final long attribute_length,
                        final UDataInputStream bytes)
        throws IOException
    {
        super (attribute_name_index, attribute_length);
        m_max_stack = bytes.readU2 ();
        m_max_locals = bytes.readU2 ();
        final long code_length = bytes.readU4 ();
        m_code = new byte [(int) code_length];
        bytes.readFully (m_code);
        m_codeSize = (int) code_length;
        final int exception_table_length = bytes.readU2 ();
        m_exceptionHandlerTable = AttributeElementFactory.newExceptionHandlerTable (exception_table_length);
        for (int i = 0; i < exception_table_length; ++ i)
        {
            Exception_info exception_info = new Exception_info (bytes);
            if (DEBUG) System.out.println ("\t[" + i + "] exception: " + exception_info);
            m_exceptionHandlerTable.add (exception_info);
        }
        final int attributes_count = bytes.readU2 ();
        m_attributes = ElementFactory.newAttributeCollection (attributes_count);
        for (int i = 0; i < attributes_count; ++ i)
        {
            Attribute_info attribute_info = Attribute_info.new_Attribute_info (constants, bytes);
            if (DEBUG) System.out.println ("\t[" + i + "] attribute: " + attribute_info);
            m_attributes.add (attribute_info);
        }
    }
    private byte [] m_code; 
    private int m_codeSize;
    private IExceptionHandlerTable m_exceptionHandlerTable; 
    private IAttributeCollection m_attributes; 
    private static final boolean DEBUG = false;
} 
