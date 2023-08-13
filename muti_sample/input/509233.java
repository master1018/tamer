    public static abstract class Factory
    {
        public static IPathEnumerator create (final File [] path, final boolean canonical, final IPathHandler handler)
        {
            return new PathEnumerator (path, canonical, handler);
        }
        private static final class PathEnumerator implements IPathEnumerator
        {
            public void enumerate () throws IOException
            {
                final IPathHandler handler = m_handler;
                for (m_pathIndex = 0; m_pathIndex < m_path.size (); ++ m_pathIndex) 
                {
                    final File f = (File) m_path.get (m_pathIndex);
                    if (! f.exists ())
                    {
                        if (IGNORE_INVALID_ENTRIES)
                            continue;
                        else
                            throw new IllegalArgumentException ("path entry does not exist: [" + f + "]");
                    }
                    if (f.isDirectory ())
                    {
                        if (m_verbose) m_log.verbose ("processing dir path entry [" + f.getAbsolutePath () + "] ...");
                        m_currentPathDir = f;
                        enumeratePathDir (null);
                    }
                    else
                    {
                        final String name = f.getName ();
                        final String lcName = name.toLowerCase ();
                        if (lcName.endsWith (".zip") || lcName.endsWith (".jar"))
                        {
                            if (m_verbose) m_log.verbose ("processing archive path entry [" + f.getAbsolutePath () + "] ...");
                            final File parent = f.getParentFile (); 
                            final File archive = new File (name);
                            m_currentPathDir = parent;
                            enumeratePathArchive (name);
                            handler.handleArchiveEnd (parent, archive); 
                        }
                        else if (! IGNORE_INVALID_ENTRIES)
                        {
                            throw new IllegalArgumentException ("path entry is not a directory or an archive: [" + f + "]");
                        }
                    }
                }
            }
            PathEnumerator (final File [] path, final boolean canonical, final IPathHandler handler)
            {
                m_path = new ArrayList (path.length);
                for (int p = 0; p < path.length; ++ p) m_path.add (path [p]);
                m_canonical = canonical;
                if (handler == null) throw new IllegalArgumentException ("null input: handler");
                m_handler = handler;
                m_processManifest = true; 
                if (m_processManifest)
                {
                    m_pathSet = new HashSet (path.length);
                    for (int p = 0; p < path.length; ++ p)
                    {
                        m_pathSet.add (path [p].getPath ()); 
                    }
                }
                else
                {
                    m_pathSet = null;
                }
                m_log = Logger.getLogger (); 
                m_verbose = m_log.atVERBOSE ();
                m_trace1 = m_log.atTRACE1 ();
            }
            private void enumeratePathDir (final String dir)
                throws IOException
            {
                final boolean trace1 = m_trace1;
                final File currentPathDir = m_currentPathDir;
                final File fullDir = dir != null ? new File (currentPathDir, dir) : currentPathDir;
                final String [] children = fullDir.list ();
                final IPathHandler handler = m_handler;
                for (int c = 0, cLimit = children.length; c < cLimit; ++ c)
                {
                    final String childName = children [c];
                    final File child = dir != null ? new File (dir, childName) : new File (childName);
                    final File fullChild = new File (fullDir, childName);
                    if (fullChild.isDirectory ())
                    {
                        handler.handleDirStart (currentPathDir, child);
                        if (trace1) m_log.trace1 ("enumeratePathDir", "recursing into [" + child.getName () + "] ...");
                        enumeratePathDir (child.getPath ());
                        handler.handleDirEnd (currentPathDir, child);
                    }
                    else
                    {
                        {
                            if (trace1) m_log.trace1 ("enumeratePathDir", "processing file [" + child.getName () + "] ...");
                            handler.handleFile (currentPathDir, child);
                        }
                    }
                }
            }
            private void enumeratePathArchive (final String archive)
                throws IOException
            {
                final boolean trace1 = m_trace1;
                final File fullArchive = new File (m_currentPathDir, archive);
                JarInputStream in = null;
                try
                {
                    in = new JarInputStream (new BufferedInputStream (new FileInputStream (fullArchive), 32 * 1024));
                    final IPathHandler handler = m_handler;
                    Manifest manifest = in.getManifest (); 
                    if (manifest == null) manifest = readManifestViaJarFile (fullArchive); 
                    handler.handleArchiveStart (m_currentPathDir, new File (archive), manifest);
                    for (ZipEntry entry; (entry = in.getNextEntry ()) != null; )
                    {
                        if (trace1) m_log.trace1 ("enumeratePathArchive", "processing archive entry [" + entry.getName () + "] ...");
                        handler.handleArchiveEntry (in, entry);
                        in.closeEntry ();
                    }
                    if (m_processManifest)
                    {
                        if (manifest == null) manifest = in.getManifest ();
                        if (manifest != null)
                        {
                            final Attributes attributes = manifest.getMainAttributes ();
                            if (attributes != null)
                            {
                                final String jarClassPath = attributes.getValue (Attributes.Name.CLASS_PATH);
                                if (jarClassPath != null)
                                {
                                    final StringTokenizer tokenizer = new StringTokenizer (jarClassPath);
                                    for (int p = 1; tokenizer.hasMoreTokens (); )
                                    {
                                        final String relPath = tokenizer.nextToken ();
                                        final File archiveParent = fullArchive.getParentFile ();
                                        final File path = archiveParent != null ? new File (archiveParent, relPath) : new File (relPath);
                                        final String fullPath = m_canonical ? Files.canonicalizePathname (path.getPath ()) : path.getPath ();
                                        if (m_pathSet.add (fullPath))
                                        {
                                            if (m_verbose) m_log.verbose ("  added manifest Class-Path entry [" + path + "]");
                                            m_path.add (m_pathIndex + (p ++), path); 
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                catch (FileNotFoundException fnfe) 
                {
                    if ($assert.ENABLED) throw fnfe;
                }
                finally
                {
                    if (in != null) try { in.close (); } catch (Exception ignore) {}
                }
            }
            private static Manifest readManifestViaJarFile (final File archive)
            {
                Manifest result = null;
                JarFile jarfile = null;
                try
                {
                    jarfile = new JarFile (archive, false); 
                    result = jarfile.getManifest ();
                }
                catch (IOException ignore)
                {
                }
                finally
                {
                    if (jarfile != null) try { jarfile.close (); } catch (IOException ignore) {} 
                }
                return result;
            } 
            private final ArrayList  m_path;
            private final boolean m_canonical;
            private final Set  m_pathSet;
            private final IPathHandler m_handler;
            private final boolean m_processManifest;
            private final Logger m_log;
            private boolean m_verbose, m_trace1;
            private int m_pathIndex;
            private File m_currentPathDir;
            private static final boolean IGNORE_INVALID_ENTRIES = true; 
        } 
    } 
} 
