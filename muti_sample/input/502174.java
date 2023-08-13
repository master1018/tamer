final class InstrClassLoadHook implements IClassLoadHook
{
    public InstrClassLoadHook (final IInclExclFilter filter, final IMetaData mdata)
    {
        if (mdata == null) throw new IllegalArgumentException ("null input: mdata");
        m_filter = filter; 
        m_metadata = mdata;
        final CoverageOptions options = mdata.getOptions ();
        m_classDefProcessor = new InstrVisitor (options);
        m_instrResult = new InstrVisitor.InstrResult ();
    }
    public boolean processClassDef (final String className,
                                    final byte [] bytes, final int length,
                                    ByteArrayOStream out)
        throws IOException
    {
        if ($assert.ENABLED)
        {
            $assert.ASSERT (className != null, "className is null");
            $assert.ASSERT (bytes != null, "bytes is null");
            $assert.ASSERT (bytes != null, "out is null");
        }
        final IInclExclFilter filter = m_filter;
        if ((filter == null) || filter.included (className))
        {
            final ClassDef clsDef = ClassDefParser.parseClass (bytes, length);
            final String classVMName = Descriptors.javaNameToVMName (className);
            final Object lock = m_metadata.lock ();
            final boolean metadataExists;
            synchronized (lock)
            {
                metadataExists = m_metadata.hasDescriptor (classVMName);
            }
            m_classDefProcessor.process (clsDef, false, true, ! metadataExists, m_instrResult);
            boolean useOurs = m_instrResult.m_instrumented;
            if (m_instrResult.m_descriptor != null) 
            {
                synchronized (lock)
                {
                    if (! m_metadata.add (m_instrResult.m_descriptor, false))
                         useOurs = false; 
                }
            }
            if (useOurs)
            {                        
                ClassWriter.writeClassTable (clsDef, out);
                return true;
            }
        }
        return false;
    }
    private final IInclExclFilter m_filter; 
    private final IMetaData m_metadata; 
    private final InstrVisitor m_classDefProcessor; 
    private final InstrVisitor.InstrResult m_instrResult;
} 
