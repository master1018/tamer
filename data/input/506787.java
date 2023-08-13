final class ReportProcessor extends Processor
                            implements IAppErrorCodes
{
    public static ReportProcessor create ()
    {
        return new ReportProcessor ();
    }
    public synchronized final void setDataPath (final String [] path)
    {
        if ((path == null) || (path.length == 0))
            m_dataPath = IConstants.EMPTY_FILE_ARRAY;
        else
            m_dataPath = Files.pathToFiles (path, true);
    }
    public synchronized void setSourcePath (final String [] path)
    {
        if (path == null)
            m_sourcePath = null;
        else
            m_sourcePath = Files.pathToFiles (path, true); 
    }
    public synchronized void setReportTypes (final String [] types)
    {
        if (types == null) throw new IllegalArgumentException ("null input: types");
        final String [] reportTypes = Strings.removeDuplicates (types, true);
        if (reportTypes.length == 0) throw new IllegalArgumentException ("empty input: types");
        if ($assert.ENABLED) $assert.ASSERT (reportTypes != null && reportTypes.length  > 0);
        final IReportGenerator [] reportGenerators = new IReportGenerator [reportTypes.length];
        for (int t = 0; t < reportTypes.length; ++ t)
        {
            reportGenerators [t] = AbstractReportGenerator.create (reportTypes [t]);
        }
        m_reportGenerators = reportGenerators;
    }
    protected void validateState ()
    {
        super.validateState ();
        if (m_dataPath == null)
            throw new IllegalStateException ("data path not set");
        if ((m_reportGenerators == null) || (m_reportGenerators.length == 0))
            throw new IllegalStateException ("report types not set");
    }
    protected void _run (final IProperties toolProperties)
    {
        final Logger log = m_log;
        final boolean verbose = m_log.atVERBOSE ();
        if (verbose)
        {
            log.verbose (IAppConstants.APP_VERBOSE_BUILD_ID);
            log.verbose ("input data path:");
            log.verbose ("{");
            for (int p = 0; p < m_dataPath.length; ++ p)
            {
                final File f = m_dataPath [p];
                final String nonexistent = f.exists () ? "" : "{nonexistent} ";
                log.verbose ("  " + nonexistent + f.getAbsolutePath ());
            }
            log.verbose ("}");
            if ((m_sourcePath == null) || (m_sourcePath.length == 0))
            {
                log.verbose ("source path not set");
            }
            else
            {
                log.verbose ("source path:");
                log.verbose ("{");
                for (int p = 0; p < m_sourcePath.length; ++ p)
                {
                    final File f = m_sourcePath [p];
                    final String nonexistent = f.exists () ? "" : "{nonexistent} ";
                    log.verbose ("  " + nonexistent + f.getAbsolutePath ());
                }
                log.verbose ("}");
            }
        }
        else
        {
            log.info ("processing input files ...");
        }
        RuntimeException failure = null;
        try
        {
            final long start = log.atINFO () ? System.currentTimeMillis () : 0;
            IMetaData mdata = null;
            ICoverageData cdata = null;
            try
            {
                for (int f = 0; f < m_dataPath.length; ++ f)
                {
                    final File dataFile = m_dataPath [f];
                    if (verbose) log.verbose ("processing input file [" + dataFile.getAbsolutePath () + "] ...");
                    final IMergeable [] fileData = DataFactory.load (dataFile);
                    final IMetaData _mdata = (IMetaData) fileData [DataFactory.TYPE_METADATA];
                    if (_mdata != null)
                    {
                        if (verbose) log.verbose ("  loaded " + _mdata.size () + " metadata entries");
                        if (mdata == null)
                            mdata = _mdata;
                        else
                            mdata = (IMetaData) mdata.merge (_mdata); 
                    }
                    final ICoverageData _cdata = (ICoverageData) fileData [DataFactory.TYPE_COVERAGEDATA];
                    if (_cdata != null)
                    {
                        if (verbose) log.verbose ("  loaded " + _cdata.size () + " coverage data entries");
                        if (cdata == null)
                            cdata = _cdata;
                        else
                            cdata = (ICoverageData) cdata.merge (_cdata); 
                    }
                    ++ m_dataFileCount;
                }
                if (log.atINFO ())
                {
                    final long end = System.currentTimeMillis ();
                    log.info (m_dataFileCount + " file(s) read and merged in " + (end - start) + " ms");
                }
                if ((mdata == null) || mdata.isEmpty ())
                {
                    log.warning ("nothing to do: no metadata found in any of the data files");
                    return;
                }
                if (cdata == null)
                {
                    log.warning ("nothing to do: no runtime coverage data found in any of the data files");
                    return;
                }
                if (cdata.isEmpty ())
                {
                    log.warning ("no collected coverage data found in any of the data files [all reports will be empty]");
                }
                if (verbose)
                {
                    if (mdata != null)
                    {
                        log.verbose ("  merged metadata contains " + mdata.size () + " entries");
                    }
                    if (cdata != null)
                    {
                        log.verbose ("  merged coverage data contains " + cdata.size () + " entries");
                    }
                }
                SourcePathCache srcpathCache = null;
                if (m_sourcePath != null) srcpathCache = new SourcePathCache (m_sourcePath, true); 
                for (int g = 0; g < m_reportGenerators.length; ++ g)
                {
                    final IReportGenerator generator = m_reportGenerators [g];
                    try
                    {
                        generator.process (mdata, cdata, srcpathCache, toolProperties);
                    }
                    catch (Throwable t)
                    {
                        t.printStackTrace (System.out);
                        break;
                    }
                    finally
                    {
                        try { generator.cleanup (); } catch (Throwable ignore) {}
                    }
                }
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace (System.out);
            }
        }
        catch (SecurityException se)
        {
            failure = new EMMARuntimeException (SECURITY_RESTRICTION, new String [] {IAppConstants.APP_NAME}, se);
        }
        catch (RuntimeException re)
        {
            failure = re;
        }
        finally
        {
            reset ();
        }
        if (failure != null)
        {
            if (Exceptions.unexpectedFailure (failure, EXPECTED_FAILURES))
            {
                throw new EMMARuntimeException (UNEXPECTED_FAILURE,
                                                new Object [] {failure.toString (), IAppConstants.APP_BUG_REPORT_LINK},
                                                failure);
            }
            else
                throw failure;
        }
    }
    private ReportProcessor ()
    {
        m_dataPath = IConstants.EMPTY_FILE_ARRAY;
    }
    private void reset ()
    {
        m_dataFileCount = 0;
    }
    private File [] m_dataPath;     
    private File [] m_sourcePath;   
    private IReportGenerator [] m_reportGenerators; 
    private int m_dataFileCount;    
    private static final Class [] EXPECTED_FAILURES; 
    static
    {
        EXPECTED_FAILURES = new Class []
        {
            EMMARuntimeException.class,
            IllegalArgumentException.class,
            IllegalStateException.class,
        };
    }
} 
