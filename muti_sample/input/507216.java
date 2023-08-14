class XProperties extends Properties
{
    public XProperties ()
    {
    }
    public XProperties (final Properties base)
    {
        super (base);
    }
    public void list (final PrintStream out)
    {
        final Set  _propertyNames = new TreeSet ();
        for (Enumeration propertyNames = propertyNames (); propertyNames.hasMoreElements (); )
        {
            _propertyNames.add (propertyNames.nextElement ());
        }
        for (Iterator i = _propertyNames.iterator (); i.hasNext (); )
        {
            final String n = (String) i.next ();
            final String v = getProperty (n);
            out.println (n + ":\t[" + v + "]");
        }
    }
    public void list (final PrintWriter out)
    {
        final Set  _propertyNames = new TreeSet ();
        for (Enumeration propertyNames = propertyNames (); propertyNames.hasMoreElements (); )
        {
            _propertyNames.add (propertyNames.nextElement ());
        }
        for (Iterator i = _propertyNames.iterator (); i.hasNext (); )
        {
            final String n = (String) i.next ();
            final String v = getProperty (n);
            out.println (n + ":\t[" + v + "]");
        }
    }
} 
