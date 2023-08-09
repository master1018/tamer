abstract class InstrProcessor extends Processor
                              implements IPathEnumerator.IPathHandler
{
    public static final String PROPERTY_EXCLUDE_SYNTHETIC_METHODS   = "instr.exclude_synthetic_methods";
    public static final String PROPERTY_EXCLUDE_BRIDGE_METHODS      = "instr.exclude_bridge_methods";
    public static final String PROPERTY_DO_SUID_COMPENSATION        = "instr.do_suid_compensation";
    public static final String DEFAULT_EXCLUDE_SYNTHETIC_METHODS    = "true";
    public static final String DEFAULT_EXCLUDE_BRIDGE_METHODS       = "true";
    public static final String DEFAULT_DO_SUID_COMPENSATION         = "true";
    public static InstrProcessor create ()
    {
        return new InstrProcessorST ();
    }
    public synchronized final void setInstrPath (final String [] path, final boolean canonical)
    {
        if ((path == null) || (path.length == 0))
            m_instrPath = IConstants.EMPTY_FILE_ARRAY;
        else
            m_instrPath = Files.pathToFiles (path, canonical);
        m_canonical = canonical;
    }
    public synchronized final void setDependsMode (final boolean enable)
    {
        m_dependsMode = enable;
    } 
    public synchronized final void setInclExclFilter (final String [] specs)
    {
        if (specs == null)
            m_coverageFilter = null;
        else
            m_coverageFilter = IInclExclFilter.Factory.create (specs);
    }
    public synchronized final void setMetaOutFile (final String fileName)
    {
        if (fileName == null)
            m_mdataOutFile = null;
        else
        {
            final File _file = new File (fileName);
            if (_file.exists () && ! _file.isFile ())
                throw new IllegalArgumentException ("not a file: [" + _file.getAbsolutePath () + "]");
            m_mdataOutFile = _file;
        }
    }
    public synchronized final void setMetaOutMerge (final Boolean merge)
    {
        m_mdataOutMerge = merge;
    }
    public synchronized final void setInstrOutDir (final String dir)
    {
        if (dir == null)
            m_outDir = null;
        else
        {
            final File _outDir = new File (dir);
            if (_outDir.exists () && ! _outDir.isDirectory ())
                throw new IllegalArgumentException ("not a directory: [" + _outDir.getAbsolutePath () + "]");
            m_outDir = _outDir;
        }
    }
    public synchronized final void setOutMode (final OutMode mode)
    {
        if (mode == null)
            throw new IllegalArgumentException ("null input: mode");
        m_outMode = mode;
    }
    protected InstrProcessor ()
    {
        m_dependsMode = true;
    }
    protected void validateState ()
    {
        super.validateState ();
        if ((m_instrPath == null) || (m_instrPath.length == 0))
            throw new IllegalStateException ("instrumentation path not set");
        if (m_outMode == null)
            throw new IllegalStateException ("output mode not set");
        if (m_outMode != OutMode.OUT_MODE_OVERWRITE)
        { 
            if (m_outDir == null)
                throw new IllegalStateException ("output directory not set");
            if ($assert.ENABLED)
            {
                $assert.ASSERT (m_outDir != null, "m_outDir = null");
                $assert.ASSERT (m_instrPath != null, "m_instrPath = null");
            }
            final File canonicalOutDir = Files.canonicalizeFile (m_outDir);
            final File [] canonicalInstrPath;
            if (m_canonical)
                canonicalInstrPath = m_instrPath;
            else
            {
                canonicalInstrPath = new File [m_instrPath.length];
                for (int ip = 0; ip < canonicalInstrPath.length; ++ ip)
                {
                    canonicalInstrPath [ip] = Files.canonicalizeFile (m_instrPath [ip]);
                }
            }
            final int instrPathLength = canonicalInstrPath.length;
            for (File dir = canonicalOutDir; dir != null; dir = dir.getParentFile ()) 
            {
                for (int ip = 0; ip < instrPathLength; ++ ip)
                {
                    if (dir.equals (canonicalInstrPath [ip]))
                        throw new IllegalStateException ("output directory [" + canonicalOutDir + "] cannot be one of the instrumentation path directories (or a child thereof)");
                }
            }
        }
    }
    protected void reset ()
    {
        m_classCopies = m_classInstrs = 0;
    }  
    protected final void createDir (final File dir, final boolean mkall)
        throws EMMARuntimeException
    {
        if (mkall)
        {
            if (! dir.mkdirs () && ! dir.exists ())
                throw new EMMARuntimeException (IAppErrorCodes.OUT_MKDIR_FAILURE, new Object [] {dir.getAbsolutePath ()}); 
        }
        else
        {
            if (! dir.mkdir () && ! dir.exists ())
                throw new EMMARuntimeException (IAppErrorCodes.OUT_MKDIR_FAILURE, new Object [] {dir.getAbsolutePath ()});
        } 
    }
    protected final File getFullOutDir (final File pathDir, final boolean isClass)
    {
        if (m_outMode == OutMode.OUT_MODE_OVERWRITE)
        {
            return pathDir;
        }
        else if (m_outMode == OutMode.OUT_MODE_COPY)
        {
            return m_outDir;
        }
        else if (m_outMode == OutMode.OUT_MODE_FULLCOPY)
        {
            return isClass ? Files.newFile (m_outDir, CLASSES) : Files.newFile (m_outDir, LIB);
        }
        else throw new IllegalStateException ("invalid out mode state: " + m_outMode);
    }
    protected final File getFullOutFile (final File pathDir, final File file, final boolean isClass)
    {
        return Files.newFile (getFullOutDir (pathDir, isClass), file.getPath ());
    } 
    protected File [] m_instrPath; 
    protected boolean m_dependsMode;
    protected boolean m_canonical;
    protected IInclExclFilter m_coverageFilter; 
    protected OutMode m_outMode; 
    protected File m_outDir; 
    protected File m_mdataOutFile; 
    protected Boolean m_mdataOutMerge; 
    protected int m_classCopies, m_classInstrs;
    protected static final String CLASSES   = "classes";
    protected static final String LIB       = "lib";
    protected static final boolean IN_CLASSES   = true;
    protected static final boolean IN_LIB       = ! IN_CLASSES;
    public static final class OutMode
    {
        public static final OutMode OUT_MODE_COPY = new OutMode ("copy");
        public static final OutMode OUT_MODE_FULLCOPY = new OutMode ("fullcopy");
        public static final OutMode OUT_MODE_OVERWRITE = new OutMode ("overwrite");
        public String getName ()
        {
            return m_name;
        }
        public String toString ()
        {
            return m_name;
        }
        public static OutMode nameToMode (final String name)
        {
            if (OUT_MODE_COPY.m_name.equals (name))
                return OUT_MODE_COPY;
            else if (OUT_MODE_FULLCOPY.m_name.equals (name))
                return OUT_MODE_FULLCOPY;
            else if (OUT_MODE_OVERWRITE.m_name.equals (name))
                return OUT_MODE_OVERWRITE;
            return null;
        }
        private OutMode (final String name)
        {
            m_name = name;
        }
        private final String m_name;
    } 
} 
