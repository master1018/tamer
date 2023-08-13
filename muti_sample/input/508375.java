abstract class Files
{
    public static String [] readFileList (final File atfile)
        throws IOException
    {
        if (atfile == null) throw new IllegalArgumentException ("null input: atfile");
        List _result = null;
        BufferedReader in = null;
        try
        {
            in = new BufferedReader (new FileReader (atfile), 8 * 1024); 
            _result = new LinkedList ();
            for (String line; (line = in.readLine ()) != null; )
            {
                line = line.trim ();
                if ((line.length () == 0) || (line.charAt (0) == '#')) continue;
                _result.add (line);
            }
        }
        finally
        {
            if (in != null) try { in.close (); } catch (Exception ignore) {}
        }
        if ((_result == null) || _result.isEmpty ())
            return IConstants.EMPTY_STRING_ARRAY;
        else
        {
            final String [] result = new String [_result.size ()];
            _result.toArray (result);
            return result;
        }
    }
    public static File [] pathToFiles (final String [] path, final boolean canonical)
    {
        if (path == null) throw new IllegalArgumentException ("null input: path");
        if (path.length == 0) return IConstants.EMPTY_FILE_ARRAY;
        final List  _result = new ArrayList (path.length);
        final Set  pathnames = new HashSet (path.length);
        final String separators = ",".concat (File.pathSeparator);
        for (int i = 0; i < path.length; ++ i)
        {
            String segment = path [i];
            if (segment == null) throw new IllegalArgumentException ("null input: path[" + i + "]");
            final StringTokenizer tokenizer = new StringTokenizer (segment, separators);
            while (tokenizer.hasMoreTokens ())
            {
                String pathname = tokenizer.nextToken ();
                if (canonical) pathname = canonicalizePathname (pathname);
                if (pathnames.add (pathname))
                {
                    _result.add (new File (pathname));
                }
            }
        }
        final File [] result = new File [_result.size ()];
        _result.toArray (result);
        return result;
    }
    public static String canonicalizePathname (final String pathname)
    {
        if (pathname == null) throw new IllegalArgumentException ("null input: pathname");
        try
        {
            return new File (pathname).getCanonicalPath ();
        }
        catch (Exception e)
        {
            return new File (pathname).getAbsolutePath ();
        }
    }
    public static File canonicalizeFile (final File file)
    {
        if (file == null) throw new IllegalArgumentException ("null input: file");
        try
        {
            return file.getCanonicalFile ();
        }
        catch (Exception e)
        {
            return file.getAbsoluteFile ();
        }
    }
    public static String getFileName (final File file)
    {
        if (file == null) throw new IllegalArgumentException ("null input: file");
        final String name = file.getName ();
        int lastDot = name.lastIndexOf ('.');
        if (lastDot < 0) return name;
        return name.substring (0, lastDot);
    }
    public static String getFileExtension (final File file)
    {
        if (file == null) throw new IllegalArgumentException ("null input: file");
        final String name = file.getName ();
        int lastDot = name.lastIndexOf ('.');
        if (lastDot < 0) return "";
        return name.substring (lastDot);
    }
    public static File newFile (final File dir, final File file)
    {
        if (file == null) throw new IllegalArgumentException ("null input: file");
        if ((dir == null) || file.isAbsolute ()) return file;
        return new File (dir, file.getPath ());
    }
    public static File newFile (final File dir, final String file)
    {
        if (file == null) throw new IllegalArgumentException ("null input: file");
        final File fileFile  = new File (file);
        if ((dir == null) || fileFile.isAbsolute ()) return fileFile;
        return new File (dir, file);
    }
    public static File newFile (final String dir, final String file)
    {
        if (file == null) throw new IllegalArgumentException ("null input: file");
        final File fileFile  = new File (file);
        if ((dir == null) || fileFile.isAbsolute ()) return fileFile;
        return new File (dir, file);
    }
    public static boolean renameFile (final File source, final File target, final boolean overwrite)
    {
        if ((source == null) || ! source.exists ())
            throw new IllegalArgumentException ("invalid input source: [" + source + "]");
        if (target == null)
            throw new IllegalArgumentException ("null input: target");
        final boolean targetExists;
        if (! (targetExists = target.exists ()) || overwrite)
        {
            if (targetExists)
            {
                target.delete (); 
            }
            else
            {
                final File targetDir = target.getParentFile ();
                if ((targetDir != null) && ! targetDir.equals (source.getParentFile ()))
                    targetDir.mkdirs (); 
            }
            return source.renameTo (target);
        }
        return false;
    }
    public static File createTempFile (final File parentDir, final String prefix, String extension)
        throws IOException
    {
        if ((parentDir == null) || ! parentDir.exists ())
            throw new IllegalArgumentException ("invalid parent directory: [" + parentDir + "]");
        if ((prefix == null) || (prefix.length () < 3))
            throw new IllegalArgumentException ("null or less than 3 chars long: " + prefix);
        if (extension == null) extension = ".tmp";
        else if (extension.charAt (0) != '.') extension = ".".concat (extension);
        return File.createTempFile (prefix, extension, parentDir);
    }
    private Files () {} 
} 
