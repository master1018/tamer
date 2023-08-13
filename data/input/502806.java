final class MergeProcessor extends Processor
                           implements IAppErrorCodes
{
    public static MergeProcessor create ()
    {
        return new MergeProcessor ();
    }
    public synchronized final void setDataPath (final String [] path)
    {
        if ((path == null) || (path.length == 0))
            m_dataPath = IConstants.EMPTY_FILE_ARRAY;
        else
            m_dataPath = Files.pathToFiles (path, true);
    }
    public synchronized final void setSessionOutFile (final String fileName)
    {
        if (fileName == null)
            m_sdataOutFile = null;
        else
        {
            final File _file = new File (fileName);
            if (_file.exists () && ! _file.isFile ())
                throw new IllegalArgumentException ("not a file: [" + _file.getAbsolutePath () + "]");
            m_sdataOutFile = _file;
        }
    }
    protected void validateState ()
    {
        super.validateState ();
        if (m_dataPath == null)
            throw new IllegalStateException ("data path not set");
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
        }
        else
        {
            log.info ("processing input files ...");
        }
        File sdataOutFile = m_sdataOutFile;
        {
            if (sdataOutFile == null)
                sdataOutFile = new File (toolProperties.getProperty (EMMAProperties.PROPERTY_SESSION_DATA_OUT_FILE,
                                                                     EMMAProperties.DEFAULT_SESSION_DATA_OUT_FILE));
        }
        RuntimeException failure = null;
        try
        {
            IMetaData mdata = null;
            ICoverageData cdata = null;
            try
            {
                final long start = log.atINFO () ? System.currentTimeMillis () : 0;
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
                if (((mdata == null) || mdata.isEmpty ()) && ((cdata == null) || cdata.isEmpty ()))
                {
                    log.warning ("nothing to do: no metadata or coverage data found in any of the input files");
                    return;
                }
            }
            catch (IOException ioe)
            {
                ioe.printStackTrace (System.out);
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
            {
                $assert.ASSERT (sdataOutFile != null, "sdataOutFile not null");
                boolean rename = false;
                File tempDataOutFile = null;
                final File canonicalDataOutFile = Files.canonicalizeFile (sdataOutFile);
                for (int f = 0; f < m_dataPath.length; ++ f)
                {
                    final File canonicalDataFile = Files.canonicalizeFile (m_dataPath [f]);
                    if (canonicalDataOutFile.equals (canonicalDataFile))
                    {
                        rename = true;
                        break;
                    }
                }
                if (rename) 
                {
                    File tempFileDir = canonicalDataOutFile.getParentFile ();
                    if (tempFileDir == null) tempFileDir = new File ("");
                    final String tempFileName = Files.getFileName (canonicalDataOutFile) + IAppConstants.APP_NAME_LC;
                    final String tempFileExt = EMMAProperties.PROPERTY_TEMP_FILE_EXT;
                    try
                    {
                        tempDataOutFile = Files.createTempFile (tempFileDir, tempFileName, tempFileExt);
                    }
                    catch (IOException ioe)
                    {
                        throw new EMMARuntimeException (ioe);
                    }
                    log.warning ("the specified output file is one of the input files [" + canonicalDataOutFile + "]");
                    log.warning ("all merged data will be written to a temp file first [" + tempDataOutFile.getAbsolutePath ()  + "]");
                }
                {
                    final long start = log.atINFO () ? System.currentTimeMillis () : 0;
                    File persistFile = null;
                    try
                    {
                        persistFile = tempDataOutFile != null ? tempDataOutFile : canonicalDataOutFile;
                        if ((mdata == null) || mdata.isEmpty ())
                            DataFactory.persist (cdata, persistFile, false); 
                        else if ((cdata == null) || cdata.isEmpty ())
                            DataFactory.persist (mdata, persistFile, false); 
                        else
                            DataFactory.persist (new SessionData (mdata, cdata), persistFile, false); 
                    }
                    catch (IOException ioe)
                    {
                        if (persistFile != null) persistFile.delete ();
                        throw new EMMARuntimeException (ioe);
                    }
                    catch (Error e)
                    {
                        if (persistFile != null) persistFile.delete ();
                        throw e; 
                    }
                    if (rename) 
                    {
                        if (! Files.renameFile (tempDataOutFile, canonicalDataOutFile, true)) 
                        {
                            throw new EMMARuntimeException ("could not rename temporary file [" + tempDataOutFile.getAbsolutePath () + "] to [" + canonicalDataOutFile + "]: make sure the original file is not locked and can be deleted");
                        }
                    }
                    if (log.atINFO ())
                    {
                        final long end = System.currentTimeMillis ();
                        log.info ("merged/compacted data written to [" + canonicalDataOutFile + "] {in " + (end - start) + " ms}");
                    }
                }
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
    private MergeProcessor ()
    {
        m_dataPath = IConstants.EMPTY_FILE_ARRAY;
    }
    private void reset ()
    {
        m_dataFileCount = 0;
    }
    private File [] m_dataPath; 
    private File m_sdataOutFile; 
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
