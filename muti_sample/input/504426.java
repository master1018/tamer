final class mergeTask extends FileTask 
{
    public mergeTask (final SuppressableTask parent)
    {
        super (parent);
    }
    public void execute () throws BuildException
    {
        if (isEnabled ())
        {
            String [] files = getDataPath (true);
            if ((files == null) || (files.length == 0))
                throw (BuildException) newBuildException (getTaskName ()
                    + ": no valid input data files have been specified", location).fillInStackTrace ();
            final MergeProcessor processor = MergeProcessor.create ();
            processor.setDataPath (files); files = null;
            processor.setSessionOutFile (m_outFile != null ? m_outFile.getAbsolutePath () : null);
            processor.setPropertyOverrides (getTaskSettings ());
            processor.run ();
        }
    }
    public void setMergefile (final File file)
    {
        if (m_outFile != null)
            throw (BuildException) newBuildException (getTaskName ()
                + ": merge data file attribute already set", location).fillInStackTrace ();
        m_outFile = file;
    }
    public void setOutfile (final File file)
    {
        if (m_outFile != null)
            throw (BuildException) newBuildException (getTaskName ()
                + ": merge data file attribute already set", location).fillInStackTrace ();
        m_outFile = file;
    }
    public void setTofile (final File file)
    {
        if (m_outFile != null)
            throw (BuildException) newBuildException (getTaskName ()
                + ": merge data file attribute already set", location).fillInStackTrace ();
        m_outFile = file;
    }
    public void setFile (final File file)
    {
        if (m_outFile != null)
            throw (BuildException) newBuildException (getTaskName ()
                + ": merge data file attribute already set", location).fillInStackTrace ();
        m_outFile = file;
    }
    private File m_outFile;
} 
