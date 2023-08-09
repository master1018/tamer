final class reportTask extends FileTask implements IReportProperties, IReportEnums
{
    public reportTask (final SuppressableTask parent)
    {
        super (parent);
    }
    public void init () throws BuildException
    {
        super.init ();
        m_reportCfg = new ReportCfg (getProject (), this);
    }
    public void execute () throws BuildException
    {
        if (isEnabled ())
        {
            final String [] reportTypes = m_reportCfg.getReportTypes ();
            if ((reportTypes == null) || (reportTypes.length == 0)) 
                throw (BuildException) newBuildException (getTaskName ()
                    + ": no report types specified: provide at least one of <txt>, <html>, <lcov>, <xml> nested elements", location).fillInStackTrace ();
            String [] files = getDataPath (true);
            if ((files == null) || (files.length == 0))
                throw (BuildException) newBuildException (getTaskName ()
                    + ": no valid input data files have been specified", location).fillInStackTrace ();
            final Path srcpath = m_reportCfg.getSourcepath ();
            final IProperties settings; 
            {
                final IProperties taskSettings = getTaskSettings ();
                final IProperties reportSettings = m_reportCfg.getReportSettings ();
                settings = IProperties.Factory.combine (reportSettings, taskSettings);
            }
            final ReportProcessor processor = ReportProcessor.create ();
            processor.setDataPath (files); files = null;
            processor.setSourcePath (srcpath != null ? srcpath.list () : null);
            processor.setReportTypes (reportTypes);
            processor.setPropertyOverrides (settings);        
            processor.run ();
        }
    }
    public void setSourcepath (final Path path)
    {
        m_reportCfg.setSourcepath (path);
    }
    public void setSourcepathRef (final Reference ref)
    {
        m_reportCfg.setSourcepathRef (ref);
    }
    public Path createSourcepath ()
    {
        return m_reportCfg.createSourcepath ();
    }
    public Element_TXT createTxt ()
    {
        return m_reportCfg.createTxt ();
    }
    public Element_LCOV createLcov ()
    {
        return m_reportCfg.createLcov ();
    }
    public Element_HTML createHtml ()
    {
        return m_reportCfg.createHtml ();
    }
    public Element_XML createXml ()
    {
        return m_reportCfg.createXml ();
    }
    public void setUnits (final UnitsTypeAttribute units)
    {
        m_reportCfg.setUnits (units);
    }    
    public void setDepth (final DepthAttribute depth)
    {
        m_reportCfg.setDepth (depth);
    }
    public void setColumns (final String columns)
    {
        m_reportCfg.setColumns (columns);
    }
    public void setSort (final String sort)
    {
        m_reportCfg.setSort (sort);
    }
    public void setMetrics (final String metrics)
    {
        m_reportCfg.setMetrics (metrics);
    }
    public void setEncoding (final String encoding)
    {
        m_reportCfg.setEncoding (encoding);
    }
    private ReportCfg m_reportCfg;   
} 
