class FilterCfg
{
    public static final class filterElement extends StringValue
    {
        public filterElement (final Task task)
        {
            super (task);
        }
        public void setValue (final String value)
        {
            final String [] specs = Strings.merge (new String [] {value}, COMMA_DELIMITERS, true);
            for (int i = 0; i < specs.length; ++ i)
            {
                final String spec = specs [i];
                if (spec.startsWith (IInclExclFilter.INCLUSION_PREFIX_STRING) ||
                    spec.startsWith (IInclExclFilter.EXCLUSION_PREFIX_STRING))
                {
                    appendValue (spec, COMMA);
                }
                else
                {
                    appendValue (IInclExclFilter.INCLUSION_PREFIX + spec, COMMA); 
                }
            }
        }
        public void setFile (final File file)
        {
            appendValue ("@".concat (file.getAbsolutePath ()), COMMA); 
        }
        public void setIncludes (final String value)
        {
            final String [] specs = Strings.merge (new String [] {value}, COMMA_DELIMITERS, true);
            for (int i = 0; i < specs.length; ++ i)
            {
                final String spec = specs [i];
                if (spec.startsWith (IInclExclFilter.INCLUSION_PREFIX_STRING))
                {
                    appendValue (spec, COMMA);
                }
                else
                {
                    if (spec.startsWith (IInclExclFilter.EXCLUSION_PREFIX_STRING))
                        appendValue (IInclExclFilter.INCLUSION_PREFIX + spec.substring (1), COMMA); 
                    else
                        appendValue (IInclExclFilter.INCLUSION_PREFIX + spec, COMMA);
                }
            }
        }
        public void setExcludes (final String value)
        {
            final String [] specs = Strings.merge (new String [] {value}, COMMA_DELIMITERS, true);
            for (int i = 0; i < specs.length; ++ i)
            {
                final String spec = specs [i];
                if (spec.startsWith (IInclExclFilter.EXCLUSION_PREFIX_STRING))
                {
                    appendValue (spec, COMMA);
                }
                else
                {
                    if (spec.startsWith (IInclExclFilter.INCLUSION_PREFIX_STRING))
                        appendValue (IInclExclFilter.EXCLUSION_PREFIX + spec.substring (1), COMMA); 
                    else
                        appendValue (IInclExclFilter.EXCLUSION_PREFIX + spec, COMMA);
                }
            }
        }
    } 
    public FilterCfg (final Task task)
    {
        if (task == null) throw new IllegalArgumentException ("null input: task");
        m_task = task;
        m_filterList = new ArrayList ();
    }
    public void setFilter (final String filter)
    {
        createFilter ().appendValue (filter, COMMA);
    }
    public filterElement createFilter ()
    {
        final filterElement result = new filterElement (m_task);
        m_filterList.add (result);
        return result;
    }
    public String [] getFilterSpecs ()
    {
        if (m_specs != null)
            return m_specs;
        else
        {           
            if ((m_filterList == null) || m_filterList.isEmpty ())
            {
                m_specs = IConstants.EMPTY_STRING_ARRAY;
            }
            else
            {
                final String [] values = new String [m_filterList.size ()];
                int j = 0;
                for (Iterator i = m_filterList.iterator (); i.hasNext (); ++ j)
                    values [j] = ((StringValue) i.next ()).getValue ();
                try
                {
                    m_specs = Strings.mergeAT (values, COMMA_DELIMITERS, true);
                }
                catch (IOException ioe)
                {
                    throw (BuildException) SuppressableTask.newBuildException (m_task.getTaskName ()
                        + ": I/O exception while processing input" , ioe, m_task.getLocation ()).fillInStackTrace ();
                }
            }
            return m_specs;
        }
    }
    protected static final String COMMA               = ",";
    protected static final String COMMA_DELIMITERS    = COMMA + Strings.WHITE_SPACE;
    protected static final String PATH_DELIMITERS     = COMMA.concat (File.pathSeparator);
    private final Task m_task; 
    private final List  m_filterList; 
    private transient String [] m_specs;
} 
