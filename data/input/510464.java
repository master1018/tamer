final class InstrProcessorST extends InstrProcessor
                             implements IAppErrorCodes
{
    public final void handleArchiveStart (final File parentDir, final File archive, final Manifest manifest)
    {
        final Logger log = m_log;
        if (log.atTRACE2 ()) log.trace2 ("handleArchiveStart", "[" + parentDir + "] [" + archive + "]");
        if (DO_DEPENDS_CHECKING)
        {
            final File fullArchiveFile = Files.newFile (parentDir, archive);
            m_currentArchiveTS = fullArchiveFile.lastModified ();
            if ($assert.ENABLED) $assert.ASSERT (m_currentArchiveTS > 0, "invalid ts: " + m_currentArchiveTS);
        }
        if ((m_outMode == OutMode.OUT_MODE_FULLCOPY) || (m_outMode == OutMode.OUT_MODE_OVERWRITE))
        {
            final Manifest outManifest = manifest != null
                ? new Manifest (manifest) 
                : new Manifest (); 
            final Attributes mainAttrs = outManifest.getMainAttributes ();
            if (manifest == null) mainAttrs.put (Attributes.Name.MANIFEST_VERSION,  "1.0");
            mainAttrs.put (new Attributes.Name ("Created-By"), IAppConstants.APP_NAME + " v" + IAppConstants.APP_VERSION_WITH_BUILD_ID_AND_TAG);
            mainAttrs.put (Attributes.Name.IMPLEMENTATION_TITLE,  "instrumented version of [" + archive.getAbsolutePath () + "]");
            mainAttrs.put (Attributes.Name.SPECIFICATION_TITLE,  "instrumented on " + new Date (m_timeStamp) + " [" + Property.getSystemFingerprint () + "]");
            if (m_outMode == OutMode.OUT_MODE_FULLCOPY)
            {
                try
                {
                    final OutputStream out = new FileOutputStream (getFullOutFile (parentDir, archive, IN_LIB));
                    m_archiveOut = outManifest != null ? new JarOutputStream (out, outManifest) : new JarOutputStream (out);
                }
                catch (IOException ioe)
                {
                    throw new EMMARuntimeException (ioe);
                }
            }
            else if (m_outMode == OutMode.OUT_MODE_OVERWRITE)
            {
                m_origArchiveFile = Files.newFile (parentDir, archive);
                final String archiveName = Files.getFileName (archive) + IAppConstants.APP_NAME_LC;
                final String archiveExt = EMMAProperties.PROPERTY_TEMP_FILE_EXT;
                try
                {
                    m_tempArchiveFile = Files.createTempFile (parentDir, archiveName, archiveExt);
                    if (log.atTRACE2 ()) log.trace2 ("handleArchiveStart", "created temp archive [" + m_tempArchiveFile.getAbsolutePath () + "]");
                    final OutputStream out = new FileOutputStream (m_tempArchiveFile);
                    m_archiveOut = outManifest != null ? new JarOutputStream (out, outManifest) : new JarOutputStream (out);
                }
                catch (IOException ioe)
                {
                    throw new EMMARuntimeException (ioe);
                }
            }
        }
    }
    public final void handleArchiveEntry (final JarInputStream in, final ZipEntry entry)
    {
        final Logger log = m_log;
        if (log.atTRACE2 ()) log.trace2 ("handleArchiveEntry", "[" + entry.getName () + "]");
        final String name = entry.getName ();
        final String lcName = name.toLowerCase ();
        final boolean notcopymode = (m_outMode == OutMode.OUT_MODE_FULLCOPY) || (m_outMode == OutMode.OUT_MODE_OVERWRITE);
        boolean copyEntry = false;
        if (lcName.endsWith (".class"))
        {
            final String className = name.substring (0, name.length () - 6).replace ('/', '.');
            if ((m_coverageFilter == null) || m_coverageFilter.included (className))
            {
                InputStream clsin = null;
                try
                {
                    File outFile = null;
                    File fullOutFile = null;
                    if (DO_DEPENDS_CHECKING)
                    {
                        if (m_outMode == OutMode.OUT_MODE_COPY)
                        {
                            outFile = new File (className.replace ('.', File.separatorChar).concat (".class"));
                            fullOutFile = getFullOutFile (null, outFile, IN_CLASSES);
                            if (m_mdata.hasDescriptor (Descriptors.javaNameToVMName (className)))
                                return;
                            final long outTimeStamp = fullOutFile.lastModified (); 
                            if (outTimeStamp > 0)
                            {
                                long inTimeStamp = entry.getTime (); 
                                if (inTimeStamp < 0) inTimeStamp = m_currentArchiveTS; 
                                if ($assert.ENABLED) $assert.ASSERT (inTimeStamp > 0);
                                if (inTimeStamp <= outTimeStamp)
                                {
                                    if (log.atVERBOSE ()) log.verbose ("destination file [" + outFile + "] skipped: more recent than the source");
                                    return;
                                }
                            }
                        }
                    }
                    readZipEntry (in, entry);
                    final ClassDef clsDef = ClassDefParser.parseClass (m_readbuf, m_readpos);
                    m_visitor.process (clsDef, m_outMode == OutMode.OUT_MODE_OVERWRITE, true, true, m_instrResult);
                    if (m_instrResult.m_instrumented)
                    {
                        if ($assert.ENABLED) $assert.ASSERT (m_instrResult.m_descriptor != null, "no descriptor created for an instrumented class");
                        ++ m_classInstrs;
                        m_mdata.add (m_instrResult.m_descriptor, false);
                        m_baos.reset ();
                        ClassWriter.writeClassTable (clsDef, m_baos);
                        if (notcopymode)
                        {
                            entry.setTime (m_timeStamp);
                            addJob (new EntryWriteJob (m_archiveOut, m_baos.copyByteArray (), entry, false));
                        }
                        else 
                        {
                            if (! DO_DEPENDS_CHECKING) 
                            {
                                outFile = new File (className.replace ('.', File.separatorChar).concat (".class"));
                                fullOutFile = getFullOutFile (null, outFile, IN_CLASSES);
                            }
                            addJob (new FileWriteJob (fullOutFile, m_baos.copyByteArray (), true));
                        }
                    }   
                    else if (notcopymode)
                    {
                        final byte [] data = new byte [m_readpos];
                        System.arraycopy (m_readbuf, 0, data, 0, data.length);
                        ++ m_classCopies;
                        entry.setTime (m_timeStamp);
                        addJob (new EntryWriteJob (m_archiveOut, data, entry, true));
                    }
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
                        }
                        catch (Exception e)
                        {
                            throw new EMMARuntimeException (e);
                        }
                }
            }
            else
            {
                copyEntry = notcopymode;
            }
        }
        else
        {
            copyEntry = notcopymode;
            if (copyEntry && name.equalsIgnoreCase ("META-INF/"))
                copyEntry = false;
            if (copyEntry && name.equalsIgnoreCase (JarFile.MANIFEST_NAME))
                copyEntry = false;
        }
        if (copyEntry)
        {
            try
            {
                readZipEntry (in, entry);
                final byte [] data = new byte [m_readpos];
                System.arraycopy (m_readbuf, 0, data, 0, data.length);
                ++ m_classCopies;
                entry.setTime (m_timeStamp);
                addJob (new EntryWriteJob (m_archiveOut, data, entry, true));
            }
            catch (IOException ioe)
            {
                throw new EMMARuntimeException (ioe);
            }
        }
    }
    public final void handleArchiveEnd (final File parentDir, final File archive)
    {
        final Logger log = m_log;
        if (log.atTRACE2 ()) log.trace2 ("handleArchiveEnd", "[" + parentDir + "] [" + archive + "]");
        m_currentArchiveTS = Long.MAX_VALUE;
        if ((m_outMode == OutMode.OUT_MODE_FULLCOPY) || (m_outMode == OutMode.OUT_MODE_OVERWRITE))
        {
            try
            {
                drainJobQueue (); 
                m_archiveOut.flush ();
                m_archiveOut.close ();
                m_archiveOut = null;
            }
            catch (IOException ioe)
            {
                throw new EMMARuntimeException (ioe);
            }
            if (m_outMode == OutMode.OUT_MODE_OVERWRITE)
            {
                if (! Files.renameFile (m_tempArchiveFile, m_origArchiveFile, true)) 
                {
                    throw new EMMARuntimeException ("could not rename temporary file [" + m_tempArchiveFile + "] to [" + m_origArchiveFile + "]: make sure the original file is not locked and can be deleted");
                }
                else
                {
                    if (log.atTRACE2 ()) log.trace2 ("handleArchiveEnd", "renamed temp archive [" + m_tempArchiveFile.getAbsolutePath () + "] to [" + m_origArchiveFile + "]");
                    m_origArchiveFile = m_tempArchiveFile = null;
                }
            }
        }
    }
    public final void handleDirStart (final File pathDir, final File dir)
    {
        final Logger log = m_log;
        if (log.atTRACE2 ()) log.trace2 ("handleDirStart", "[" + pathDir + "] [" + dir + "]");
        if (m_outMode == OutMode.OUT_MODE_FULLCOPY)
        {
            final File saveDir = new File (getFullOutDir (pathDir, IN_CLASSES), dir.getPath ());
            createDir (saveDir, true);
        }
    }
    public final void handleFile (final File pathDir, final File file)
    {
        final Logger log = m_log;
        if (log.atTRACE2 ()) log.trace2 ("handleFile", "[" + pathDir + "] [" + file + "]");
        final String name = file.getPath ();
        final String lcName = name.toLowerCase ();
        final boolean fullcopymode = (m_outMode == OutMode.OUT_MODE_FULLCOPY);
        final boolean mkdir = (m_outMode == OutMode.OUT_MODE_COPY);
        boolean copyFile = false;
        if (lcName.endsWith (".class"))
        {
            final String className = name.substring (0, name.length () - 6).replace (File.separatorChar, '.');
            if ((m_coverageFilter == null) || m_coverageFilter.included (className))
            {
                InputStream clsin = null;
                try
                {
                    final File inFile = Files.newFile (pathDir, file.getPath ());
                    final File fullOutFile = getFullOutFile (pathDir, file, IN_CLASSES);
                    if (DO_DEPENDS_CHECKING)
                    {
                        if (m_outMode == OutMode.OUT_MODE_COPY)
                        {
                            if (m_mdata.hasDescriptor (Descriptors.javaNameToVMName (className)))
                                return;
                            final long outTimeStamp = fullOutFile.lastModified (); 
                            if (outTimeStamp > 0)
                            {
                                final long inTimeStamp = inFile.lastModified ();
                                if (inTimeStamp <= outTimeStamp)
                                {
                                    if (log.atVERBOSE ()) log.verbose ("destination file [" + fullOutFile + "] skipped: more recent that the source file");
                                    return;
                                }
                            }
                        }
                    }
                    readFile (inFile);
                    ClassDef clsDef = ClassDefParser.parseClass (m_readbuf, m_readpos);
                    m_visitor.process (clsDef, m_outMode == OutMode.OUT_MODE_OVERWRITE, true, true, m_instrResult);
                    if (m_instrResult.m_instrumented)
                    {
                        if ($assert.ENABLED) $assert.ASSERT (m_instrResult.m_descriptor != null, "no descriptor created for an instrumented class");
                        ++ m_classInstrs;
                        m_mdata.add (m_instrResult.m_descriptor, false);
                        m_baos.reset ();
                        ClassWriter.writeClassTable (clsDef, m_baos);
                        clsDef = null;
                        final byte [] outdata = m_baos.copyByteArray ();
                        addJob (new FileWriteJob (fullOutFile, outdata, mkdir));
                    }   
                    else if (fullcopymode)
                    {
                        clsDef = null;
                        final byte [] outdata = new byte [m_readpos];
                        System.arraycopy (m_readbuf, 0, outdata, 0, m_readpos);
                        ++ m_classCopies;
                        addJob (new FileWriteJob (fullOutFile, outdata, mkdir));
                    }
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
                        }
                        catch (Exception e)
                        {
                            throw new EMMARuntimeException (e);
                        }
                }
            }
            else
            {
                copyFile = fullcopymode;
            }
        }
        else
        {
            copyFile = fullcopymode;
        }
        if (copyFile)
        {
            try
            {
                final File inFile = Files.newFile (pathDir, file.getPath ());
                readFile (inFile);
                final byte [] data = new byte [m_readpos];
                System.arraycopy (m_readbuf, 0, data, 0, data.length);
                ++ m_classCopies;
                final File outFile = getFullOutFile (pathDir, file, IN_CLASSES);
                addJob (new FileWriteJob (outFile, data, mkdir));
            }
            catch (IOException ioe)
            {
                throw new EMMARuntimeException (ioe);
            }
        }
    }
    public final void handleDirEnd (final File pathDir, final File dir)
    {
        final Logger log = m_log;
        if (log.atTRACE2 ()) log.trace2 ("handleDirEnd", "[" + pathDir + "] [" + dir + "]");
        if (m_outMode == OutMode.OUT_MODE_OVERWRITE)
        {
            try
            {
                drainJobQueue ();
            }
            catch (IOException ioe)
            {
                throw new EMMARuntimeException (ioe);
            }
        }
    }
    protected void reset ()
    {
        m_visitor = null;
        m_mdata = null;
        m_readbuf = null;
        m_baos = null;
        for (int j = 0; j < m_jobs.length; ++ j) m_jobs [j] = null;
        if (CLEANUP_TEMP_ARCHIVE_ON_ERRORS)
        {
            if (m_archiveOut != null) 
                try { m_archiveOut.close (); } catch (Exception ignore) {} 
            if (m_tempArchiveFile != null)
                m_tempArchiveFile.delete ();
        }
        m_archiveOut = null;
        m_origArchiveFile = null;
        m_tempArchiveFile = null;
        super.reset ();
    }
    protected void _run (final IProperties toolProperties)
    {
        final Logger log = m_log;
        final boolean verbose = log.atVERBOSE ();
        if (verbose)
        {
            log.verbose (IAppConstants.APP_VERBOSE_BUILD_ID);
            log.verbose ("instrumentation path:");
            log.verbose ("{");
            for (int p = 0; p < m_instrPath.length; ++ p)
            {
                final File f = m_instrPath [p];
                final String nonexistent = f.exists () ? "" : "{nonexistent} ";
                log.verbose ("  " + nonexistent + f.getAbsolutePath ());
            }
            log.verbose ("}");
            log.verbose ("instrumentation output mode: " + m_outMode);
        }
        else
        {
            log.info ("processing instrumentation path ...");
        }
        RuntimeException failure = null;
        try
        {
            long start = System.currentTimeMillis ();
            m_timeStamp = start;
            final IPathEnumerator enumerator = IPathEnumerator.Factory.create (m_instrPath, m_canonical, this);
            {
                if (m_outMode != OutMode.OUT_MODE_OVERWRITE) createDir (m_outDir, true);
                if ((m_outMode == OutMode.OUT_MODE_FULLCOPY))
                {
                    final File classesDir = Files.newFile (m_outDir, CLASSES);
                    createDir (classesDir, false); 
                    final File libDir = Files.newFile (m_outDir, LIB);
                    createDir (libDir, false); 
                }
            }
            File mdataOutFile = m_mdataOutFile;
            Boolean mdataOutMerge = m_mdataOutMerge;
            {
                if (mdataOutFile == null)
                    mdataOutFile = new File (toolProperties.getProperty (EMMAProperties.PROPERTY_META_DATA_OUT_FILE,
                                                                         EMMAProperties.DEFAULT_META_DATA_OUT_FILE));
                if (mdataOutMerge == null)
                {
                    final String _dataOutMerge = toolProperties.getProperty (EMMAProperties.PROPERTY_META_DATA_OUT_MERGE,
                                                                             EMMAProperties.DEFAULT_META_DATA_OUT_MERGE.toString ());
                    mdataOutMerge = Property.toBoolean (_dataOutMerge) ? Boolean.TRUE : Boolean.FALSE;
                } 
            }
            if (verbose)
            {
                log.verbose ("metadata output file: " + mdataOutFile.getAbsolutePath ());
                log.verbose ("metadata output merge mode: " + mdataOutMerge);
            }
            m_readbuf = new byte [BUF_SIZE]; 
            m_readpos = 0;
            m_baos = new ByteArrayOStream (BUF_SIZE); 
            m_jobPos = 0;
            m_currentArchiveTS = Long.MAX_VALUE;
            final CoverageOptions options = CoverageOptionsFactory.create (toolProperties);            
            m_visitor = new InstrVisitor (options); 
            m_mdata = DataFactory.newMetaData (options);
            try
            {
                enumerator.enumerate ();
                drainJobQueue ();
            }
            catch (IOException ioe)
            {
                throw new EMMARuntimeException (INSTR_IO_FAILURE, ioe);
            }
            if (log.atINFO ())
            {
                final long end = System.currentTimeMillis ();
                log.info ("instrumentation path processed in " + (end - start) + " ms");
                log.info ("[" + m_classInstrs + " class(es) instrumented, " + m_classCopies + " resource(s) copied]");
            }
            try
            {
                if ($assert.ENABLED) $assert.ASSERT (mdataOutFile != null, "m_metadataOutFile is null");
                if (verbose)
                {
                    if (m_mdata != null)
                    {
                        log.verbose ("metadata contains " + m_mdata.size () + " entries");
                    }
                }
                if (m_mdata.isEmpty ())
                {
                    log.info ("no output created: metadata is empty");
                }
                else
                {
                    start = System.currentTimeMillis ();
                    DataFactory.persist (m_mdata, mdataOutFile, mdataOutMerge.booleanValue ());
                    final long end = System.currentTimeMillis ();
                    if (log.atINFO ())
                    {
                        log.info ("metadata " + (mdataOutMerge.booleanValue () ? "merged into" : "written to") + " [" + mdataOutFile.getAbsolutePath () + "] {in " + (end - start) + " ms}");
                    }
                }
            }
            catch (IOException ioe)
            {
                throw new EMMARuntimeException (OUT_IO_FAILURE, new Object [] {mdataOutFile.getAbsolutePath ()}, ioe);
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
    InstrProcessorST ()
    {
        m_jobs = new Job [JOB_QUEUE_SIZE];
        m_instrResult = new InstrVisitor.InstrResult ();
    }
    static void writeFile (final byte [] data, final File outFile, final boolean mkdirs)
        throws IOException
    {
        RandomAccessFile raf = null;
        try
        {
            if (mkdirs)
            {
                final File parent = outFile.getParentFile ();
                if (parent != null) parent.mkdirs (); 
            }
            raf = new RandomAccessFile (outFile, "rw");
            if (DO_RAF_EXTENSION) raf.setLength (data.length);
            raf.write (data);
        } 
        finally
        {
            if (raf != null) raf.close (); 
        }
    }
    static void writeZipEntry (final byte [] data, final ZipOutputStream out, final ZipEntry entry, final boolean isCopy)
        throws IOException
    {
        if (isCopy)
        {
            out.putNextEntry (entry); 
            try
            {
                out.write (data);
            }
            finally
            {
                out.closeEntry ();
            }
        }
        else
        {
            final ZipEntry entryCopy = new ZipEntry (entry.getName ());
            entryCopy.setTime (entry.getTime ()); 
            entryCopy.setMethod (ZipOutputStream.STORED);
            entryCopy.setSize (data.length);
            entryCopy.setCompressedSize (data.length);
            final CRC32 crc = new CRC32 ();
            crc.update (data);
            entryCopy.setCrc (crc.getValue ());
            out.putNextEntry (entryCopy);
            try
            {
                out.write (data);
            }
            finally
            {
                out.closeEntry ();
            }
        }
    }
    private static abstract class Job
    {
        protected abstract void run () throws IOException;
    } 
    private static final class FileWriteJob extends Job
    {
        protected void run () throws IOException
        {
            writeFile (m_data, m_outFile, m_mkdirs);
            m_data = null;
        }
        FileWriteJob (final File outFile, final byte [] data, final boolean mkdirs)
        {
            m_outFile = outFile;
            m_data = data;
            m_mkdirs = mkdirs;
        }
        final File m_outFile;
        final boolean m_mkdirs;
        byte [] m_data;
    } 
    private static final class EntryWriteJob extends Job
    {
        protected void run () throws IOException
        {
            writeZipEntry (m_data, m_out, m_entry, m_isCopy);
            m_data = null;
        }
        EntryWriteJob (final ZipOutputStream out, final byte [] data, final ZipEntry entry, final boolean isCopy)
        {
            m_out = out;
            m_data = data;
            m_entry = entry;
            m_isCopy = isCopy;
        }
        final ZipOutputStream m_out;
        byte [] m_data;
        final ZipEntry m_entry;
        final boolean m_isCopy;
    } 
    private void addJob (final Job job)
        throws FileNotFoundException, IOException
    {
        if (m_jobPos == JOB_QUEUE_SIZE) drainJobQueue ();
        m_jobs [m_jobPos ++] = job;
    }
    private void drainJobQueue ()
        throws IOException
    {
        for (int j = 0; j < m_jobPos; ++ j)
        {
            final Job job = m_jobs [j];
            if (job != null) 
            {
                m_jobs [j] = null;
                job.run ();
            }
        }
        m_jobPos = 0;
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
    private final Job [] m_jobs;
    private final InstrVisitor.InstrResult m_instrResult;
    private InstrVisitor m_visitor;
    private IMetaData m_mdata;
    private byte [] m_readbuf;
    private int m_readpos;
    private ByteArrayOStream m_baos; 
    private int m_jobPos;
    private long m_currentArchiveTS;
    private File m_origArchiveFile, m_tempArchiveFile;
    private JarOutputStream m_archiveOut;
    private long m_timeStamp;
    private static final int BUF_SIZE = 32 * 1024;
    private static final int JOB_QUEUE_SIZE = 128; 
    private static final boolean CLEANUP_TEMP_ARCHIVE_ON_ERRORS = true;
    private static final boolean DO_RAF_EXTENSION = true;
    private static final boolean DO_DEPENDS_CHECKING = true;
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
