class emmajavaTask extends Java
{
    public void init () throws BuildException
    {
        super.init ();
        m_verbosityCfg = new VerbosityCfg ();
        m_genericCfg = new GenericCfg (this);
        m_filterCfg = new FilterCfg (this);
        m_reportCfg = new ReportCfg (project, this);
        setEnabled (true);        
    }
    public void execute () throws BuildException
    {
        log (IAppConstants.APP_VERBOSE_BUILD_ID, Project.MSG_VERBOSE);
        if (getClasspath () == null)
            throw (BuildException) SuppressableTask.newBuildException (getTaskName ()
                + ": this task requires 'classpath' attribute to be set", location).fillInStackTrace ();
        if (isEnabled ())
        {
            if (m_forkUserOverride && ! m_fork)
                log (getTaskName () + ": 'fork=\"false\"' attribute setting ignored (this task always forks)", Project.MSG_WARN);
            super.setFork (true); 
            final Path libClasspath = m_libClasspath;
            if ((libClasspath != null) && (libClasspath.size () > 0))
            {
                super.createClasspath ().append (libClasspath);
            }
            super.setClassname ("emmarun");
            {
                {
                    String reportTypes = Strings.toListForm (m_reportCfg.getReportTypes (), ',');
                    if ((reportTypes == null) || (reportTypes.length () == 0)) reportTypes = "txt";
                    super.createArg ().setValue ("-r");
                    super.createArg ().setValue (reportTypes);
                }
                {
                    if (m_scanCoveragePath)
                    {
                        super.createArg ().setValue ("-f");
                    }
                }
                {
                    if (m_dumpSessionData)
                    {
                        super.createArg ().setValue ("-raw");
                        if (m_outFile != null)
                        {
                            super.createArg ().setValue ("-out");
                            super.createArg ().setValue (m_outFile.getAbsolutePath ());
                        }
                        if (m_outFileMerge != null)
                        {
                            super.createArg ().setValue ("-merge");
                            super.createArg ().setValue (m_outFileMerge.booleanValue () ? "y" : "n");
                        }
                    }
                    else
                    {
                        if (m_outFile != null)
                            log (getTaskName () + ": output file attribute ignored ('fullmetadata=\"true\"' not specified)", Project.MSG_WARN);
                        if (m_outFileMerge != null)
                            log (getTaskName () + ": merge attribute setting ignored ('fullmetadata=\"true\"' not specified)", Project.MSG_WARN);
                    }
                } 
                {
                    final String [] specs = m_filterCfg.getFilterSpecs ();
                    if ((specs != null) && (specs.length > 0))
                    {
                        super.createArg ().setValue ("-ix");
                        super.createArg ().setValue (Strings.toListForm (specs, ','));
                    }
                }
                {
                    final Path srcpath = m_reportCfg.getSourcepath ();
                    if (srcpath != null)
                    {
                        super.createArg ().setValue ("-sp");
                        super.createArg ().setValue (Strings.toListForm (srcpath.list (), ','));
                    }
                }
                {
                    final IProperties reportSettings = m_reportCfg.getReportSettings ();
                    final IProperties genericSettings = m_genericCfg.getGenericSettings ();
                    final IProperties fileSettings = m_genericCfg.getFileSettings ();
                    final IProperties verbositySettings = m_verbosityCfg.getSettings ();
                    final IProperties settings = IProperties.Factory.combine (reportSettings,
                                                 IProperties.Factory.combine (verbositySettings,
                                                 IProperties.Factory.combine (genericSettings,
                                                                              fileSettings)));
                    final String [] argForm = settings.toAppArgsForm ("-D");
                    if (argForm.length > 0)
                    {
                        for (int a = 0; a < argForm.length; ++ a)
                            super.createArg ().setValue (argForm [a]);
                    }
                }
            }
            super.createArg ().setValue ("-cp");
            super.createArg ().setPath (getClasspath ());
            if (getClassname () != null)
                super.createArg ().setValue (getClassname ());
            else if (getJar () != null)
            {
                super.createArg ().setValue ("-jar");
                super.createArg ().setValue (getJar ().getAbsolutePath ());
            }
            else
                throw (BuildException) SuppressableTask.newBuildException (getTaskName ()
                    + "either 'jar' or 'classname' attribute must be set", location).fillInStackTrace ();
            if (m_appArgs != null)
            {
                final String [] args = m_appArgs.getArguments ();
                for (int a = 0; a < args.length; ++ a)
                {
                    super.createArg ().setValue (args [a]); 
                }
            }
        }
        else
        {
            super.setFork (m_fork);
            super.createClasspath ().append (getClasspath ()); 
            if (getClassname () != null)
                super.setClassname (getClassname ());
            else if (getJar () != null)
                super.setJar (getJar ());
            else
                throw (BuildException) SuppressableTask.newBuildException (getTaskName ()
                    + "either 'jar' or 'classname' attribute must be set", location).fillInStackTrace ();
            if (m_appArgs != null)
            {
                final String [] args = m_appArgs.getArguments ();
                for (int a = 0; a < args.length; ++ a)
                {
                    super.createArg ().setValue (args [a]); 
                }
            }    
        }
        super.execute ();
    }
    public void setClassname (final String classname)
    {
        if (getJar () != null)
            throw (BuildException) SuppressableTask.newBuildException (getTaskName ()
                + "'jar' and 'classname' attributes cannot be set at the same time", location).fillInStackTrace ();
        m_classname = classname;
    }
    public void setJar (final File file)
    {
        if (getClassname () != null)
            throw (BuildException) SuppressableTask.newBuildException (getTaskName ()
                + "'jar' and 'classname' attributes cannot be set at the same time", location).fillInStackTrace ();
        m_jar = file;
    }
    public void setClasspath (final Path path)
    {
        if (m_classpath == null)
            m_classpath = path;
        else
            m_classpath.append (path);
    }
    public void setClasspathRef (final Reference ref)
    {
        createClasspath ().setRefid (ref);
    }
    public Path createClasspath ()
    {
        if (m_classpath == null)
            m_classpath = new Path (project);
        return m_classpath.createPath ();
    }
    public void setArgs (final String args)
    {
        throw (BuildException) SuppressableTask.newBuildException (getTaskName ()
            + ": disallows using <java>'s deprecated 'args' attribute", location).fillInStackTrace ();
    }
    public final void setFork (final boolean fork)
    {
        m_fork = fork;
        m_forkUserOverride = true;
    }
    public final Commandline.Argument createArg ()
    {
        if (m_appArgs == null)
            m_appArgs = new Commandline ();
        return m_appArgs.createArgument ();
    }
    public void setEnabled (final boolean enabled)
    {
        m_enabled = enabled;
    }
    public final void setProperties (final File file)
    {
        m_genericCfg.setProperties (file);
    }
    public final PropertyElement createProperty ()
    {
        return m_genericCfg.createProperty ();
    }
    public void setVerbosity (final VerbosityCfg.VerbosityAttribute verbosity)
    {
        m_verbosityCfg.setVerbosity (verbosity);
    }
    public void setVerbosityfilter (final String filter)
    {
        m_verbosityCfg.setVerbosityfilter (filter);
    }
    public final void setLibclasspath (final Path classpath)
    {
        if (m_libClasspath == null)
            m_libClasspath = classpath;
        else
            m_libClasspath.append (classpath);
    }
    public final void setLibclasspathRef (final Reference ref)
    {
        if (m_libClasspath == null)
            m_libClasspath = new Path (project);
        m_libClasspath.createPath ().setRefid (ref);
    }
    public void setFullmetadata (final boolean full)
    {
        m_scanCoveragePath = full; 
    }
    public void setDumpsessiondata (final boolean dump)
    {
        m_dumpSessionData = dump;
    }
    public void setSessiondatafile (final File file)
    {
        if (m_outFile != null)
            throw (BuildException) SuppressableTask.newBuildException (getTaskName ()
                + ": session data file attribute already set", location).fillInStackTrace ();
        m_outFile = file;
    }
    public void setOutfile (final File file)
    {
        if (m_outFile != null)
            throw (BuildException) SuppressableTask.newBuildException (getTaskName ()
                + ": session data file attribute already set", location).fillInStackTrace ();
        m_outFile = file;
    }
    public void setMerge (final boolean merge)
    {
        m_outFileMerge = merge ? Boolean.TRUE : Boolean.FALSE;       
    }
    public final void setFilter (final String filter)
    {
        m_filterCfg.setFilter (filter);
    }
    public final filterElement createFilter ()
    {
        return m_filterCfg.createFilter ();
    }
    public final void setSourcepath (final Path path)
    {
        m_reportCfg.setSourcepath (path);
    }
    public final void setSourcepathRef (final Reference ref)
    {
        m_reportCfg.setSourcepathRef (ref);
    }
    public final Path createSourcepath ()
    {
        return m_reportCfg.createSourcepath ();
    }
    public final Element_TXT createTxt ()
    {
        return m_reportCfg.createTxt ();
    }
    public final Element_LCOV createLcov ()
    {
        return m_reportCfg.createLcov ();
    }
    public final Element_HTML createHtml ()
    {
        return m_reportCfg.createHtml ();
    }
    public final Element_XML createXml ()
    {
        return m_reportCfg.createXml ();
    }
    public final void setUnits (final UnitsTypeAttribute units)
    {
        m_reportCfg.setUnits (units);
    }    
    public final void setDepth (final DepthAttribute depth)
    {
        m_reportCfg.setDepth (depth);
    }
    public final void setColumns (final String columns)
    {
        m_reportCfg.setColumns (columns);
    }
    public final void setSort (final String sort)
    {
        m_reportCfg.setSort (sort);
    }
    public final void setMetrics (final String metrics)
    {
        m_reportCfg.setMetrics (metrics);
    }
    public void setEncoding (final String encoding)
    {
        m_reportCfg.setEncoding (encoding);
    }
    protected String getClassname ()
    {
        return m_classname;
    }
    protected File getJar ()
    {
        return m_jar;
    }
    protected Path getClasspath ()
    {
        return m_classpath;
    }
    protected boolean isEnabled ()
    {
        return m_enabled;
    }
    private Path m_classpath;
    private String m_classname;
    private File m_jar;
    private Commandline m_appArgs;
    private boolean m_fork, m_forkUserOverride;
    private boolean m_enabled;
    private Path m_libClasspath;
    private  VerbosityCfg m_verbosityCfg;
    private  GenericCfg m_genericCfg;
    private  FilterCfg m_filterCfg;
    private  ReportCfg m_reportCfg;
    private boolean m_scanCoveragePath; 
    private boolean m_dumpSessionData; 
    private File m_outFile;
    private Boolean m_outFileMerge;
} 
