final class reportCommand extends Command
{
    public reportCommand (final String usageToolName, final String [] args)
    {
        super (usageToolName, args);
    }
    public synchronized void run ()
    {
        ClassLoader loader;
        try
        {
            loader = ClassLoaderResolver.getClassLoader ();
        }
        catch (Throwable t)
        {
            loader = getClass ().getClassLoader ();
        }
        try
        {
            {
                final IOptsParser parser = getOptParser (loader);
                final IOptsParser.IOpts parsedopts = parser.parse (m_args);
                {
                    final int usageRequestLevel = parsedopts.usageRequestLevel ();
                    if (usageRequestLevel > 0)
                    {
                        usageexit (parser, usageRequestLevel, null);
                        return;
                    }
                }
                final IOptsParser.IOpt [] opts = parsedopts.getOpts ();
                if (opts == null) 
                {
                    parsedopts.error (m_out, STDOUT_WIDTH);
                    usageexit (parser, IOptsParser.SHORT_USAGE, null);
                    return;
                }
                try
                {
                    for (int o = 0; o < opts.length; ++ o)
                    {
                        final IOptsParser.IOpt opt = opts [o];
                        final String on = opt.getCanonicalName ();
                        if (! processOpt (opt))
                        {
                            if ("in".equals (on))
                            {
                                m_datapath = getListOptValue (opt, PATH_DELIMITERS, true);
                            }
                            else if ("sp".equals (on))
                            {
                                m_srcpath = getListOptValue (opt, PATH_DELIMITERS, true);
                            }
                            else if ("r".equals (on))
                            {
                                m_reportTypes = Strings.merge (opt.getValues (), COMMA_DELIMITERS, true);
                            }
                        }
                    }
                    if (! processFilePropertyOverrides ()) return;
                    processCmdPropertyOverrides (parsedopts);
                }
                catch (IOException ioe)
                {
                    throw new EMMARuntimeException (IAppErrorCodes.ARGS_IO_FAILURE, ioe);
                }
                {
                }
            }
            {
                final ReportProcessor processor = ReportProcessor.create ();
                processor.setAppName (IAppConstants.APP_NAME); 
                processor.setDataPath (m_datapath);
                processor.setSourcePath (m_srcpath);
                if ($assert.ENABLED) $assert.ASSERT (m_reportTypes != null, "m_reportTypes no set");
                processor.setReportTypes (m_reportTypes); 
                processor.setPropertyOverrides (m_propertyOverrides);
                processor.run ();
            }
        }
        catch (EMMARuntimeException yre)
        {
            exit (true, yre.getMessage (), yre, RC_UNEXPECTED); 
            return;
        }
        catch (Throwable t)
        {
            exit (true, "unexpected failure: ", t, RC_UNEXPECTED); 
            return;
        }
        exit (false, null, null, RC_OK);
    }    
    protected void initialize ()
    {
        super.initialize ();                
    }
    protected String usageArgsMsg ()
    {
        return "[options]";
    }
    private String [] m_datapath; 
    private String [] m_srcpath;
    private String [] m_reportTypes;
} 
