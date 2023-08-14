final class ClassPathProcessorST implements IPathEnumerator.IPathHandler, IAppErrorCodes
{
    public void run ()
    {
        long start = System.currentTimeMillis ();
        final IPathEnumerator enumerator = IPathEnumerator.Factory.create (m_path, m_canonical, this);
        m_readbuf = new byte [BUF_SIZE]; 
        m_readpos = 0;
        m_baos = new ByteArrayOStream (BUF_SIZE); 
        if (m_log.atINFO ())
        {
            m_log.info ("processing classpath ...");
        }
        try
        {
            enumerator.enumerate ();
        }
        catch (IOException ioe)
        {
            throw new EMMARuntimeException (INSTR_IO_FAILURE, ioe);
        }
        if (m_log.atINFO ())
        {
            final long end = System.currentTimeMillis ();
            m_log.info ("[" + m_classCount + " class(es) processed in " + (end - start) + " ms]");
        }
    }
    public void handleArchiveStart (final File parentDir, final File archive, final Manifest manifest)
    {
        m_archiveFile = Files.newFile (parentDir, archive.getPath ());
    }
    public void handleArchiveEntry (final JarInputStream in, final ZipEntry entry)
    {
        if (m_log.atTRACE2 ()) m_log.trace2 ("handleArchiveEntry", "[" + entry.getName () + "]");
        final String name = entry.getName ();
        final String lcName = name.toLowerCase ();
        if (lcName.endsWith (".class"))
        {
            final String className = name.substring (0, name.length () - 6).replace ('/', '.');
            if ((m_coverageFilter == null) || m_coverageFilter.included (className))
            {
                String srcURL = null;
                InputStream clsin = null;
                try
                {
                    readZipEntry (in, entry);
                    srcURL = "jar:".concat (m_archiveFile.toURL ().toExternalForm ()).concat ("!/").concat (name);
                }
                catch (FileNotFoundException fnfe)
                {
                    if ($assert.ENABLED)
                    {
                        fnfe.printStackTrace (System.out);
                    }
                }
                catch (IOException ioe)
                {
                    throw new EMMARuntimeException (ioe);
                }
                finally
                {
                    if (clsin != null)
                        try
                        {
                            clsin.close ();
                            clsin = null;
                        }
                        catch (Exception e)
                        {
                            throw new EMMARuntimeException (e);
                        }
                }
                try
                {
                    ClassDef clsDef = ClassDefParser.parseClass (m_readbuf, m_readpos);
                    if (! clsDef.isInterface ()) ++ m_classCount;
                    m_visitor.process (clsDef, false, false, true, m_instrResult); 
                    clsDef = null;
                    boolean cacheClassDef = true;
                    if (m_instrResult.m_descriptor != null)
                    {
                        if (! m_mdata.add (m_instrResult.m_descriptor, false))
                           cacheClassDef = false; 
                    }
                    if (cacheClassDef && (m_cache != null))
                    {
                        final byte [] bytes = new byte [m_readpos];
                        System.arraycopy (m_readbuf, 0, bytes, 0, m_readpos);
                        m_cache.put (className, new ClassPathCacheEntry (bytes, srcURL));
                    }
                }
                catch (IOException ioe)
                {
                    throw new EMMARuntimeException (ioe);
                }
            }
        }
    }
    public void handleArchiveEnd (final File parentDir, final File archive)
    {
        m_archiveFile = null;
    }
    public void handleDirStart (final File pathDir, final File dir)
    {
    }
    public void handleFile (final File pathDir, final File file)
    {
        if (m_log.atTRACE2 ()) m_log.trace2 ("handleFile", "[" + pathDir + "] [" + file + "]");
        final String name = file.getPath ();
        final String lcName = name.toLowerCase ();
        if (lcName.endsWith (".class"))
        {
            final String className = name.substring (0, name.length () - 6).replace (File.separatorChar, '.');
            if ((m_coverageFilter == null) || m_coverageFilter.included (className))
            {
                String srcURL = null;
                InputStream clsin = null;
                try
                {
                    final File inFile = Files.newFile (pathDir, file.getPath ());
                    readFile (inFile);
                    srcURL = inFile.toURL ().toExternalForm ();
                }
                catch (FileNotFoundException fnfe)
                {
                    if ($assert.ENABLED)
                    {
                        fnfe.printStackTrace (System.out);
                    }
                }
                catch (IOException ioe)
                {
                    throw new EMMARuntimeException (ioe);
                }
                finally
                {
                    if (clsin != null)
                        try
                        {
                            clsin.close ();
                            clsin = null;
                        }
                        catch (Exception e)
                        {
                            throw new EMMARuntimeException (e);
                        }
                }
                try
                {
                    ClassDef clsDef = ClassDefParser.parseClass (m_readbuf, m_readpos);
                    if (! clsDef.isInterface ()) ++ m_classCount;
                    m_visitor.process (clsDef, false, false, true, m_instrResult); 
                    clsDef = null;
                    boolean cacheClassDef = true;
                    if (m_instrResult.m_descriptor != null)
                    {
                        if (! m_mdata.add (m_instrResult.m_descriptor, false))
                           cacheClassDef = false; 
                    }
                    if (cacheClassDef && (m_cache != null))
                    {
                        final byte [] bytes = new byte [m_readpos];
                        System.arraycopy (m_readbuf, 0, bytes, 0, m_readpos);
                        m_cache.put (className, new ClassPathCacheEntry (bytes, srcURL));
                    }
                }
                catch (IOException ioe)
                {
                    throw new EMMARuntimeException (ioe);
                }
            }
        }
    }
    public void handleDirEnd (final File pathDir, final File dir)
    {
    }
    ClassPathProcessorST (final File [] path, final boolean canonical,
                          final IMetaData mdata, final IInclExclFilter filter,
                          final Map cache)
    {
        if (path == null) throw new IllegalArgumentException ("null input: path");
        if (mdata == null) throw new IllegalArgumentException ("null input: mdata");
        m_path = path;
        m_canonical = canonical;
        m_mdata = mdata;
        m_coverageFilter = filter;
        m_cache = cache; 
        m_visitor = new InstrVisitor (mdata.getOptions ());
        m_instrResult = new InstrVisitor.InstrResult ();
        m_log = Logger.getLogger ();
    }
    private void readFile (final File file)
        throws IOException
    {
        final int length = (int) file.length ();
        ensureReadCapacity (length);
        InputStream in = null;
        try
        {
            in = new FileInputStream (file);
            int totalread = 0;
            for (int read;
                 (totalread < length) && (read = in.read (m_readbuf, totalread, length - totalread)) >= 0;
                 totalread += read);
            m_readpos = totalread;
        } 
        finally
        {
            if (in != null) try { in.close (); } catch (Exception ignore) {} 
        }
    }
    private void readZipEntry (final ZipInputStream in, final ZipEntry entry)
        throws IOException
    {
        final int length = (int) entry.getSize (); 
        if (length >= 0)
        {
            ensureReadCapacity (length);
            int totalread = 0;
            for (int read;
                 (totalread < length) && (read = in.read (m_readbuf, totalread, length - totalread)) >= 0;
                 totalread += read);
            m_readpos = totalread;
        }
        else
        {
            ensureReadCapacity (BUF_SIZE);
            m_baos.reset ();
            for (int read; (read = in.read (m_readbuf)) >= 0; m_baos.write (m_readbuf, 0, read));
            m_readbuf = m_baos.copyByteArray ();
            m_readpos = m_readbuf.length;
        }
    }   
    private void ensureReadCapacity (final int capacity)
    {
        if (m_readbuf.length < capacity)
        {
            final int readbuflen = m_readbuf.length;
            m_readbuf = null;
            m_readbuf = new byte [Math.max (readbuflen << 1, capacity)];
        }
    }
    private final File [] m_path; 
    private final boolean m_canonical;
    private final IMetaData m_mdata; 
    private final IInclExclFilter m_coverageFilter; 
    private final InstrVisitor m_visitor;
    private final InstrVisitor.InstrResult m_instrResult;
    private final Map  m_cache; 
    private final Logger m_log; 
    private int m_classCount;
    private byte [] m_readbuf;
    private int m_readpos;
    private ByteArrayOStream m_baos; 
    private File m_archiveFile;
    private static final int BUF_SIZE = 32 * 1024;
} 
