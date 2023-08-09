final class SourcePathCache
{
    public SourcePathCache (final String [] sourcepath, final boolean removeNonExistent)
    {
        if (sourcepath == null) throw new IllegalArgumentException ("null input: sourcepath");
        final List _sourcepath = new ArrayList (sourcepath.length);
        for (int i = 0; i < sourcepath.length; ++ i)
        {
            final File dir = new File (sourcepath [i]);
            if (! removeNonExistent || (dir.isDirectory () && dir.exists ()))
                _sourcepath.add (dir);
        }
        m_sourcepath = new File [_sourcepath.size ()];
        _sourcepath.toArray (m_sourcepath);
        m_packageCache = new HashMap ();
    }
    public SourcePathCache (final File [] sourcepath, final boolean removeNonExistent)
    {
        if (sourcepath == null) throw new IllegalArgumentException ("null input: sourcepath");
        final List _sourcepath = new ArrayList (sourcepath.length);
        for (int i = 0; i < sourcepath.length; ++ i)
        {
            final File dir = sourcepath [i];
            if (! removeNonExistent || (dir.isDirectory () && dir.exists ()))
                _sourcepath.add (dir);
        }
        m_sourcepath = new File [_sourcepath.size ()];
        _sourcepath.toArray (m_sourcepath);
        m_packageCache = new HashMap ();
    }
    public synchronized File find (final String packageVMName, final String name)
    {
        if (packageVMName == null) throw new IllegalArgumentException ("null input: packageVMName");
        if (name == null) throw new IllegalArgumentException ("null input: name");
        if (m_sourcepath.length == 0) return null;
        CacheEntry entry = (CacheEntry) m_packageCache.get (packageVMName);
        if (entry == null)
        {
            entry = new CacheEntry (m_sourcepath.length);
            m_packageCache.put (packageVMName, entry);
        }
        final Set [] listings = entry.m_listings;
        for (int p = 0; p < listings.length; ++ p)
        {
            Set listing = listings [p];
            if (listing == null)
            {
                listing = faultListing (m_sourcepath [p], packageVMName);
                listings [p] = listing;
            }
            if (listing.contains (name))
            {
                final File relativeFile = new File (packageVMName.replace ('/', File.separatorChar), name);
                return new File (m_sourcepath [p], relativeFile.getPath ()).getAbsoluteFile ();
            }
        }
        return null;
    }
    private static final class CacheEntry
    {
        CacheEntry (final int size)
        {
            m_listings = new Set [size];
        }
        final Set  [] m_listings;
    } 
    private static final class FileExtensionFilter implements FileFilter
    {
        public boolean accept (final File file)
        {
            if ($assert.ENABLED) $assert.ASSERT (file != null, "file = null");
            if (file.isDirectory ()) return false; 
            final String name = file.getName ();
            final int lastDot = name.lastIndexOf ('.');
            if (lastDot <= 0) return false;
            return m_extension.equals (name.substring (lastDot));
        }
        public String toString ()
        {
            return super.toString () + ", extension = [" + m_extension + "]";
        }
        FileExtensionFilter (final String extension)
        {
            if (extension == null)
                throw new IllegalArgumentException ("null input: extension");
            final String canonical = canonicalizeExtension (extension); 
            if (extension.length () <= 1)
                throw new IllegalArgumentException ("empty input: extension");
            m_extension = canonical;
        }
        private static String canonicalizeExtension (final String extension)
        {
            if (extension.charAt (0) != '.')
                return ".".concat (extension);
            else
                return extension;
        }
        private final String m_extension;
    } 
    private Set  faultListing (final File dir, final String packageVMName)
    {
        if ($assert.ENABLED) $assert.ASSERT (dir != null, "dir = null");
        if ($assert.ENABLED) $assert.ASSERT (packageVMName != null, "packageVMName = null");
        final File packageFile = new File (dir, packageVMName.replace ('/', File.separatorChar));
        final File [] listing = packageFile.listFiles (FILE_EXTENSION_FILTER);
        if ((listing == null) || (listing.length == 0))
            return Collections.EMPTY_SET;
        else
        {
            final Set result = new HashSet (listing.length);
            for (int f = 0; f < listing.length; ++ f)
            {
                result.add (listing [f].getName ());
            }
            return result;
        }
    }
    private final File [] m_sourcepath; 
    private final Map  m_packageCache; 
    private static final FileExtensionFilter FILE_EXTENSION_FILTER; 
    static
    {
        FILE_EXTENSION_FILTER = new FileExtensionFilter (".java");
    }
} 
