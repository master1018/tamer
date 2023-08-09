abstract class SuppressableTask extends Task
{
    public void init () throws BuildException
    {
        super.init ();
        m_verbosityCfg = new VerbosityCfg ();
        m_genericCfg = new GenericCfg (this);
    }
    public final void setEnabled (final boolean enabled)
    {
        m_enabled = enabled;
    }
    public final boolean isEnabled ()
    {
        return m_enabled;
    }
    public void setVerbosity (final VerbosityCfg.VerbosityAttribute verbosity)
    {
        m_verbosityCfg.setVerbosity (verbosity);
    }
    public void setVerbosityfilter (final String filter)
    {
        m_verbosityCfg.setVerbosityfilter (filter);
    }
    public final void setProperties (final File file)
    {
        m_genericCfg.setProperties (file);
    }
    public final PropertyElement createProperty ()
    {
        return m_genericCfg.createProperty ();
    }
    public static BuildException newBuildException (final String msg, final Location location)
    {
        final String prefixedMsg = ((msg == null) || (msg.length () == 0))
            ? msg
            : IAppConstants.APP_THROWABLE_BUILD_ID + " " + msg;
        return new BuildException (prefixedMsg, location); 
    }
    public static BuildException newBuildException (final String msg, final Throwable cause, final Location location)
    {
        final String prefixedMsg = ((msg == null) || (msg.length () == 0))
            ? msg
            : IAppConstants.APP_THROWABLE_BUILD_ID + " " + msg;
        return new BuildException (prefixedMsg, cause, location); 
    }
    protected SuppressableTask ()
    {
        m_enabled = true; 
    }
    protected IProperties getTaskSettings ()
    {
        final IProperties fileSettings = m_genericCfg.getFileSettings ();
        final IProperties genericSettings = m_genericCfg.getGenericSettings (); 
        final IProperties verbositySettings = m_verbosityCfg.getSettings ();
        return IProperties.Factory.combine (verbositySettings,
               IProperties.Factory.combine (genericSettings,
                                            fileSettings));
    }    
    private  VerbosityCfg m_verbosityCfg;
    private  GenericCfg m_genericCfg;
    private boolean m_enabled;
} 
