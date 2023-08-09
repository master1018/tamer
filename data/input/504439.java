final class instrTask extends FilterTask
{
    public static final class ModeAttribute extends EnumeratedAttribute
    {
        public String [] getValues ()
        {
            return VALUES;
        }
        private static final String [] VALUES = new String [] {"copy", "overwrite", "fullcopy"};
    } 
    public instrTask (final SuppressableTask parent)
    {
        super (parent);
        m_outMode = InstrProcessor.OutMode.OUT_MODE_COPY; 
    }
    public void execute () throws BuildException
    {
        if (isEnabled ())
        {
            if (m_instrpath == null)
                throw (BuildException) newBuildException (getTaskName ()
                    + ": instrumentation path must be specified", location).fillInStackTrace ();
            if ((m_outMode != InstrProcessor.OutMode.OUT_MODE_OVERWRITE) && (m_outDir == null))
                throw (BuildException) newBuildException (getTaskName ()
                    + ": output directory must be specified for '" + m_outMode + "' output mode", location).fillInStackTrace ();
            InstrProcessor processor = InstrProcessor.create ();
            $assert.ASSERT (m_instrpath != null, "m_instrpath not set");
            processor.setInstrPath (m_instrpath.list (), true); 
            processor.setInclExclFilter (getFilterSpecs ());
            $assert.ASSERT (m_outMode != null, "m_outMode not set");
            processor.setOutMode (m_outMode);
            processor.setInstrOutDir (m_outDir != null ? m_outDir.getAbsolutePath () : null);
            processor.setMetaOutFile (m_outFile != null ? m_outFile.getAbsolutePath () : null);
            processor.setMetaOutMerge (m_outFileMerge);
            processor.setPropertyOverrides (getTaskSettings ());
            processor.run ();
        }
    }
    public void setInstrpath (final Path path)
    {
        if (m_instrpath == null)
            m_instrpath = path;
        else
            m_instrpath.append (path);
    }
    public void setInstrpathRef (final Reference ref)
    {
        createInstrpath ().setRefid (ref);
    }
    public Path createInstrpath ()
    {
        if (m_instrpath == null)
            m_instrpath = new Path (project);
        return m_instrpath.createPath ();
    }
    public void setOutdir (final File dir)
    {
        if (m_outDir != null)
            throw (BuildException) newBuildException (getTaskName ()
                + ": outdir|destdir attribute already set", location).fillInStackTrace ();
        m_outDir = dir;
    }
    public void setDestdir (final File dir)
    {
        if (m_outDir != null)
            throw (BuildException) newBuildException (getTaskName ()
                + ": outdir|destdir attribute already set", location).fillInStackTrace ();
        m_outDir = dir;
    }
    public void setMetadatafile (final File file)
    {
        if (m_outFile != null)
            throw (BuildException) newBuildException (getTaskName ()
                + ": metadata file attribute already set", location).fillInStackTrace ();
        m_outFile = file;
    }
    public void setOutfile (final File file)
    {
        if (m_outFile != null)
            throw (BuildException) newBuildException (getTaskName ()
                + ": metadata file attribute already set", location).fillInStackTrace ();
        m_outFile = file;
    }
    public void setMerge (final boolean merge)
    {
        m_outFileMerge = merge ? Boolean.TRUE : Boolean.FALSE;       
    }
    public void setMode (final ModeAttribute mode)
    {        
        final InstrProcessor.OutMode outMode = InstrProcessor.OutMode.nameToMode (mode.getValue ());
        if (outMode == null)
            throw (BuildException) newBuildException (getTaskName ()
                + ": invalid output mode: " + mode.getValue (), location).fillInStackTrace ();
        m_outMode = outMode;
    }
    private Path m_instrpath;
    private InstrProcessor.OutMode m_outMode;
    private File m_outDir;
    private File m_outFile;
    private Boolean m_outFileMerge;
} 
