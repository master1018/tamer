final class InterfaceCollection implements IInterfaceCollection
{
    public int get (final int offset)
    {
        return m_interfaces.get (offset);
    }
    public int size ()
    {
        return m_interfaces.size ();
    }
    public Object clone ()
    {
        try
        {
            final InterfaceCollection _clone = (InterfaceCollection) super.clone ();
            _clone.m_interfaces = (IntVector) m_interfaces.clone ();
            return _clone;
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError (e.toString ());
        }        
    }
    public void writeInClassFormat (final UDataOutputStream out) throws IOException
    {
        int _interfaces_count = m_interfaces.size (); 
        out.writeU2 (_interfaces_count);
        for (int i = 0; i < _interfaces_count; i++)
        {
            out.writeU2 (get (i));
        }
    }
    public void accept (final IClassDefVisitor visitor, final Object ctx)
    {
        visitor.visit (this, ctx);
    }
    public int add (final int interface_index)
    {
        final int newoffset = m_interfaces.size (); 
        m_interfaces.add (interface_index);
        return newoffset;
    }
    public int set (final int offset, final int interface_index)
    {
        return m_interfaces.set (offset, interface_index);
    }
    InterfaceCollection (final int capacity)
    {
        m_interfaces = capacity < 0 ? new IntVector () : new IntVector (capacity);
    }
    private IntVector m_interfaces; 
} 
