abstract class FileTask extends NestedTask
{
    public final void addInfileset (final XFileSet set)
    {
        if (set != null) m_dataFileSets.add (set);
    }
    public final void addFileset (final XFileSet set)
    {
        if (set != null) m_dataFileSets.add (set);
    }
    protected FileTask (final SuppressableTask parent)
    {
        super (parent);
        m_dataFileSets = new ArrayList ();
    }
    protected final String [] getDataPath (final boolean removeNonexistent)
    {
        final List  _files = new ArrayList ();
        for (Iterator i = m_dataFileSets.iterator (); i.hasNext (); )
        {
            final FileSet set = (FileSet) i.next ();
            final DirectoryScanner ds = set.getDirectoryScanner (project);
            final File dsBaseDir = ds.getBasedir ();
            final String [] dsfiles = ds.getIncludedFiles ();
            for (int f = 0; f < dsfiles.length; ++ f)
            {
                _files.add (new File (dsBaseDir, dsfiles [f]).getAbsolutePath ());
            }
        }
        if (_files.size () == 0)
            return EMPTY_STRING_ARRAY;
        else
        {            
            final String [] files = new String [_files.size ()];
            _files.toArray (files);
            return files;
        }
    }
    private final List  m_dataFileSets; 
    private static final String [] EMPTY_STRING_ARRAY = new String [0];
} 
