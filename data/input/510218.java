final class ConstantCollection implements IConstantCollection
{
    public CONSTANT_info get (final int index)
    {
        final Object result = m_constants.get (index - 1);
        if (result == null)
            throw new IllegalStateException ("assertion failure: dereferencing an invalid constant pool slot " + index);
        return (CONSTANT_info) result;
    }
    public IConstantCollection.IConstantIterator iterator ()
    {
        return new ConstantIterator (m_constants);
    }
    public int find (final int type, final IConstantComparator comparator)
    {
        if (comparator == null)
            throw new IllegalArgumentException ("null input: comparator");
        for (int i = 0; i < m_constants.size (); ++ i)
        {
            final CONSTANT_info constant = (CONSTANT_info) m_constants.get (i);
            if ((constant != null) && (constant.tag () == type) && comparator.equals (constant))
                return i  + 1; 
        }
        return -1;
    }
    public int findCONSTANT_Utf8 (final String value)
    {
        if (value == null)
            throw new IllegalArgumentException ("null input: value");
        final ObjectIntMap index = getCONSTANT_Utf8_index ();
        final int [] result = new int [1];
        if (index.get (value, result))
            return result [0]  + 1;
        else
            return -1;
    }
    public int size ()
    {
        return m_size;
    }
    public Object clone ()
    {
        try
        {
            final ConstantCollection _clone = (ConstantCollection) super.clone ();
            final int constants_count = m_constants.size ();
            _clone.m_constants = new ArrayList (constants_count);
            for (int c = 0; c < constants_count; ++ c)
            {
                final CONSTANT_info constant = (CONSTANT_info) m_constants.get (c);
                _clone.m_constants.add (constant == null ? null : constant.clone ());
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
        final int constant_pool_count = m_constants.size (); 
        out.writeU2 (constant_pool_count + 1);
        final ConstantIterator i = new ConstantIterator (m_constants);
        for (CONSTANT_info entry; (entry = i.nextConstant ()) != null; )
        {
            entry.writeInClassFormat (out);
        }
    }
    public void accept (final IClassDefVisitor visitor, final Object ctx)
    {
        visitor.visit (this, ctx);
    }
    public CONSTANT_info set (final int index, final CONSTANT_info constant)
    {
        final int zindex = index - 1;
        final CONSTANT_info result = (CONSTANT_info) m_constants.get (zindex);
        if (result == null)
            throw new IllegalStateException ("assertion failure: dereferencing an invalid constant pool slot " + index);
        if (result.width () != constant.width ())
            throw new IllegalArgumentException ("assertion failure: can't set entry of type [" + result.getClass ().getName () + "] to an entry of type [" + result.getClass ().getName () + "] at pool slot " + index);
        m_constants.set (zindex, constant);
        if (m_CONSTANT_Utf8_index != null)
        {
            if (result instanceof CONSTANT_Utf8_info)
            {
                final String mapKey = ((CONSTANT_Utf8_info) result).m_value;
                final int [] out = new int [1];
                if (m_CONSTANT_Utf8_index.get (mapKey, out) && (out [0] == zindex))
                    m_CONSTANT_Utf8_index.remove (mapKey);
            }
            if (constant instanceof CONSTANT_Utf8_info)
                m_CONSTANT_Utf8_index.put (((CONSTANT_Utf8_info) constant).m_value, zindex);
        }
        return result;
    }
    public int add (final CONSTANT_info constant)
    {
        m_constants.add (constant);
        ++ m_size; 
        final int result = m_constants.size ();
        for (int width = 1; width < constant.width (); ++ width)
        {
            ++ m_size;
            m_constants.add (null); 
        }
        if ((m_CONSTANT_Utf8_index != null) && (constant instanceof CONSTANT_Utf8_info))
            m_CONSTANT_Utf8_index.put (((CONSTANT_Utf8_info) constant).m_value, result  - 1);
        return result;
    }    
    ConstantCollection (final int capacity)
    {
        m_constants = capacity < 0 ? new ArrayList () : new ArrayList (capacity);
    }
    private static final class ConstantIterator implements IConstantCollection.IConstantIterator
    {
        ConstantIterator (final List constants)
        {
            m_constants = constants;
            m_next_index = 1;
            shift ();
        }
        public int nextIndex ()
        {
            final int result = m_index;
            shift ();
            return result;
        }
        public CONSTANT_info nextConstant ()
        {
            final int nextIndex = nextIndex ();
            if (nextIndex < 0)
                return null;
            else
                return (CONSTANT_info) m_constants.get (nextIndex - 1);
        }
        public CONSTANT_info set (final CONSTANT_info constant)
        {
            final int zindex = m_prev_index - 1;
            final CONSTANT_info result = (CONSTANT_info) m_constants.get (zindex);
            if (result == null) 
                throw new IllegalStateException ("assertion failure: dereferencing an invalid constant pool slot " + m_prev_index);
            if (result.width () != constant.width ())
                throw new IllegalArgumentException ("assertion failure: can't set entry of type [" + result.getClass ().getName () + "] to an entry of type [" + result.getClass ().getName () + "] at pool slot " + m_prev_index);
            m_constants.set (zindex, constant);
            return result;
        }
        private void shift ()
        {
            m_prev_index = m_index;
            m_index = m_next_index;
            if (m_index > 0)
            {
                try
                {
                    final CONSTANT_info entry = (CONSTANT_info) m_constants.get (m_index - 1);
                    m_next_index += entry.width ();
                    if (m_next_index > m_constants.size ()) m_next_index = -1;
                }
                catch (IndexOutOfBoundsException ioobe) 
                {
                    m_index = m_next_index = -1;
                }
            }
        }
        private int m_index, m_prev_index, m_next_index;
        private List m_constants;
    } 
    private ObjectIntMap getCONSTANT_Utf8_index ()
    {
        if (m_CONSTANT_Utf8_index == null)
        {
            final ObjectIntMap index = new ObjectIntMap (m_size);
            for (int i = 0; i < m_constants.size (); ++ i)
            {
                final CONSTANT_info constant = (CONSTANT_info) m_constants.get (i);
                if ((constant != null) && (constant.tag () == CONSTANT_Utf8_info.TAG))
                {
                    index.put (((CONSTANT_Utf8_info) constant).m_value, i); 
                }
            }
            m_CONSTANT_Utf8_index = index;
        }
        return m_CONSTANT_Utf8_index;
    }
    private List m_constants; 
    private int m_size;
    private transient ObjectIntMap  m_CONSTANT_Utf8_index;
} 
