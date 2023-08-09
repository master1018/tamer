final class instrCommand extends Command
{
    public instrCommand (final String usageToolName, final String [] args)
    {
        super (usageToolName, args);
        m_outMode = InstrProcessor.OutMode.OUT_MODE_COPY; 
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
                            if ("ip".equals (on))
                            {
                                m_instrpath = getListOptValue (opt, PATH_DELIMITERS, true);
                            }
                            else if ("d".equals (on))
                            {
                                m_outDirName = opt.getFirstValue ();
                            }
                            else if ("out".equals (on))
                            {
                                m_outFileName = opt.getFirstValue ();
                            }
                            else if ("merge".equals (on))
                            {
                                m_outDataMerge = getOptionalBooleanOptValue (opt) ? Boolean.TRUE : Boolean.FALSE; 
                            }
                            else if ("ix".equals (on))
                            {
                                m_ixpath = getListOptValue (opt, COMMA_DELIMITERS, true);
                            }
                            else if ("m".equals (on))
                            {
                                final String ov = opt.getFirstValue ();
                                final InstrProcessor.OutMode outMode = InstrProcessor.OutMode.nameToMode (ov);
                                if (outMode == null)
                                {
                                    usageexit (parser, IOptsParser.SHORT_USAGE,
                                        "invalid '" + opts [o].getName () + "' option value: " + ov);
                                    return;
                                }
                                m_outMode = outMode;
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
                    if ($assert.ENABLED) $assert.ASSERT (m_outMode != null, "m_outMode not set");
                    if ((m_outMode != InstrProcessor.OutMode.OUT_MODE_OVERWRITE) && (m_outDirName == null))
                    {
                        usageexit (parser, IOptsParser.SHORT_USAGE,
                            "output directory must be specified for '" + m_outMode + "' output mode");
                        return;
                    }
                }
            }
            {
                final InstrProcessor processor = InstrProcessor.create ();
                processor.setAppName (IAppConstants.APP_NAME); 
                processor.setInstrPath (m_instrpath, true); 
                processor.setInclExclFilter (m_ixpath);
                $assert.ASSERT (m_outMode != null, "m_outMode not set");
                processor.setOutMode (m_outMode);
                processor.setInstrOutDir (m_outDirName);
                processor.setMetaOutFile (m_outFileName);
                processor.setMetaOutMerge (m_outDataMerge);
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
    private String [] m_instrpath;
    private String [] m_ixpath;
    private String m_outDirName;
    private String m_outFileName;
    private Boolean m_outDataMerge;
    private InstrProcessor.OutMode m_outMode;
} 
