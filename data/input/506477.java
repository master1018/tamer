abstract class FilterTask extends NestedTask
{
    public void init () throws BuildException
    {
        super.init ();
        m_filterCfg = new FilterCfg (this);
    }
    public final void setFilter (final String filter)
    {
        m_filterCfg.setFilter (filter);
    }
    public final filterElement createFilter ()
    {
        return m_filterCfg.createFilter ();
    }
    protected FilterTask (final SuppressableTask parent)
    {
        super (parent);
    }
    protected final String [] getFilterSpecs ()
    {
        return m_filterCfg.getFilterSpecs ();
    }
    protected static final String COMMA               = ",";
    protected static final String COMMA_DELIMITERS    = COMMA + Strings.WHITE_SPACE;
    protected static final String PATH_DELIMITERS     = COMMA.concat (File.pathSeparator);
    private  FilterCfg m_filterCfg;    
} 
