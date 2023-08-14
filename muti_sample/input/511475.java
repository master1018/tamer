class ReportCfg implements IReportProperties
{
    public static abstract class Element implements IReportEnums, IReportProperties
    {
        public void setUnits (final UnitsTypeAttribute units)
        {
            m_settings.setProperty (m_prefix.concat (UNITS_TYPE), units.getValue ());
        }
        public void setDepth (final DepthAttribute depth)
        {
            m_settings.setProperty (m_prefix.concat (DEPTH), depth.getValue ());
        }
        public void setColumns (final String columns)
        {
            m_settings.setProperty (m_prefix.concat (COLUMNS), columns);
        }
        public void setSort (final String sort)
        {
            m_settings.setProperty (m_prefix.concat (SORT), sort);
        }
        public void setMetrics (final String metrics)
        {
            m_settings.setProperty (m_prefix.concat (METRICS), metrics);
        }
        public void setOutfile (final String fileName)
        {
            m_settings.setProperty (m_prefix.concat (OUT_FILE), fileName);
        }
        public void setEncoding (final String encoding)
        {
            m_settings.setProperty (m_prefix.concat (OUT_ENCODING), encoding);
        }
        public PropertyElement createProperty ()
        {
            final PropertyElement property = new PropertyElement ();
            m_genericSettings.add (property);
            return property;
        }
        protected abstract String getType ();
        Element (final Task task, final IProperties settings)
        {
            if (task == null)
                throw new IllegalArgumentException ("null input: task");
            if (settings == null)
                throw new IllegalArgumentException ("null input: settings");
            m_task = task;
            m_settings = settings;
            m_prefix = PREFIX.concat (getType ()).concat (".");
            m_genericSettings = new ArrayList ();
        }
        void processGenericSettings ()
        {
            for (Iterator i = m_genericSettings.iterator (); i.hasNext (); )
            {
                final PropertyElement property = (PropertyElement) i.next ();
                final String name = property.getName ();
                final String value = property.getValue () != null ? property.getValue () : "";
                if (name != null)
                {
                    final String prefixedName = m_prefix.concat (name);
                    if (! m_settings.isOverridden (prefixedName))
                        m_settings.setProperty (prefixedName, value);
                }
            }
        }
        protected final Task m_task; 
        protected final String m_prefix; 
        protected final IProperties m_settings; 
        protected final List  m_genericSettings; 
    } 
    public static class Element_HTML extends Element
    {
        protected final String getType ()
        {
            return TYPE;
        }
        Element_HTML (final Task task, final IProperties settings)
        {
            super (task, settings);
        }
        static final String TYPE = "html";
    } 
    public static class Element_TXT extends Element
    {
        protected final String getType ()
        {
            return TYPE;
        }
        Element_TXT (final Task task, final IProperties settings)
        {
            super (task, settings);
        }
        static final String TYPE = "txt";
    } 
    public static class Element_LCOV extends Element
    {
        protected final String getType ()
        {
            return TYPE;
        }
        Element_LCOV (final Task task, final IProperties settings)
        {
            super (task, settings);
        }
        static final String TYPE = "lcov";
    } 
    public static class Element_XML extends Element
    {
        protected final String getType ()
        {
            return TYPE;
        }
        Element_XML (final Task task, final IProperties settings)
        {
            super (task, settings);
        }
        static final String TYPE = "xml";
    } 
    public ReportCfg (final Project project, final Task task)
    {
        m_project = project;
        m_task = task;
        m_reportTypes = new ArrayList (4);
        m_cfgList = new ArrayList (4);
        m_settings = EMMAProperties.wrap (new Properties ());
    }
    public Path getSourcepath ()
    {
        return m_srcpath;
    }
    public String [] getReportTypes ()
    {
        final BuildException failure = getFailure ();
        if (failure != null)
            throw failure;
        else
        {
            if (m_reportTypes.isEmpty ())
                return IConstants.EMPTY_STRING_ARRAY;
            else
            {
                final String [] result = new String [m_reportTypes.size ()];
                m_reportTypes.toArray (result);
                return result;
            }
        }
    }
    public IProperties getReportSettings ()
    {
        final BuildException failure = getFailure ();
        if (failure != null)
            throw failure;
        else
        {
            if (! m_processed)
            {
                for (Iterator i = m_cfgList.iterator (); i.hasNext (); )
                {
                    final Element cfg = (Element) i.next ();
                    cfg.processGenericSettings ();
                }
                m_processed = true;
            }
            return m_settings; 
        }
    }
    public void setSourcepath (final Path path)
    {
        if (m_srcpath == null)
            m_srcpath = path;
        else
            m_srcpath.append (path);
    }
    public void setSourcepathRef (final Reference ref)
    {
        createSourcepath ().setRefid (ref);
    }
    public Path createSourcepath ()
    {
        if (m_srcpath == null)
            m_srcpath = new Path (m_project);
        return m_srcpath.createPath ();
    }
    public Element_TXT createTxt ()
    {
        return (Element_TXT) addCfgElement (Element_TXT.TYPE,
                                                     new Element_TXT (m_task, m_settings));
    }
    public Element_LCOV createLcov ()
    {
        return (Element_LCOV) addCfgElement (Element_LCOV.TYPE,
                                                     new Element_LCOV (m_task, m_settings));
    }
    public Element_HTML createHtml ()
    {
        return (Element_HTML) addCfgElement (Element_HTML.TYPE,
                                                      new Element_HTML (m_task, m_settings));
    }
    public Element_XML createXml ()
    {
        return (Element_XML) addCfgElement (Element_XML.TYPE,
                                                     new Element_XML (m_task, m_settings));
    }
    public void setUnits (final UnitsTypeAttribute units)
    {
        m_settings.setProperty (PREFIX.concat (UNITS_TYPE), units.getValue ());
    }    
    public void setDepth (final DepthAttribute depth)
    {
        m_settings.setProperty (PREFIX.concat (DEPTH), depth.getValue ());
    }
    public void setColumns (final String columns)
    {
        m_settings.setProperty (PREFIX.concat (COLUMNS), columns);
    }
    public void setSort (final String sort)
    {
        m_settings.setProperty (PREFIX.concat (SORT), sort);
    }
    public void setMetrics (final String metrics)
    {
        m_settings.setProperty (PREFIX.concat (METRICS), metrics);
    }
    public void setOutfile (final String fileName)
    {
        m_settings.setProperty (PREFIX.concat (OUT_FILE), fileName);
    }
    public void setEncoding (final String encoding)
    {
        m_settings.setProperty (PREFIX.concat (OUT_ENCODING), encoding);
    }
    protected Element addCfgElement (final String type, final Element cfg)
    {
        if (m_reportTypes.contains (type))
        {
            setFailure ((BuildException) SuppressableTask.newBuildException (m_task.getTaskName ()
                + ": duplicate configuration for report type [" + type + "]" ,
                m_task.getLocation ()).fillInStackTrace ());
        }
        else
        {
            m_reportTypes.add (type);
            m_cfgList.add (cfg);
        }
        return cfg;
    }
    private void setFailure (final BuildException failure)
    {
        if (m_settingsFailure == null) m_settingsFailure = failure; 
    }
    private BuildException getFailure ()
    {
        return m_settingsFailure;
    }
    private final Project m_project;
    private final Task m_task;
    private final List  m_reportTypes; 
    private final List  m_cfgList;
    private final IProperties m_settings; 
    private Path m_srcpath;
    private transient BuildException m_settingsFailure; 
    private transient boolean m_processed;
} 
