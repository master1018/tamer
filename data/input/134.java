public class ZipFileSystem extends FileSystem {
    private final ZipFileSystemProvider provider;
    private final ZipPath defaultdir;
    private boolean readOnly = false;
    private final Path zfpath;
    private final ZipCoder zc;
    private final String  defaultDir;    
    private final String  nameEncoding;  
    private final boolean useTempFile;   
    private final boolean createNew;     
    private static final boolean isWindows =
        System.getProperty("os.name").startsWith("Windows");
    ZipFileSystem(ZipFileSystemProvider provider,
                  Path zfpath,
                  Map<String, ?> env)
        throws IOException
    {
        this.createNew    = "true".equals(env.get("create"));
        this.nameEncoding = env.containsKey("encoding") ?
                            (String)env.get("encoding") : "UTF-8";
        this.useTempFile  = TRUE.equals(env.get("useTempFile"));
        this.defaultDir   = env.containsKey("default.dir") ?
                            (String)env.get("default.dir") : "/";
        if (this.defaultDir.charAt(0) != '/')
            throw new IllegalArgumentException("default dir should be absolute");
        this.provider = provider;
        this.zfpath = zfpath;
        if (Files.notExists(zfpath)) {
            if (createNew) {
                try (OutputStream os = Files.newOutputStream(zfpath, CREATE_NEW, WRITE)) {
                    new END().write(os, 0);
                }
            } else {
                throw new FileSystemNotFoundException(zfpath.toString());
            }
        }
        zfpath.getFileSystem().provider().checkAccess(zfpath, AccessMode.READ);
        if (!Files.isWritable(zfpath))
            this.readOnly = true;
        this.zc = ZipCoder.get(nameEncoding);
        this.defaultdir = new ZipPath(this, getBytes(defaultDir));
        this.ch = Files.newByteChannel(zfpath, READ);
        this.cen = initCEN();
    }
    @Override
    public FileSystemProvider provider() {
        return provider;
    }
    @Override
    public String getSeparator() {
        return "/";
    }
    @Override
    public boolean isOpen() {
        return isOpen;
    }
    @Override
    public boolean isReadOnly() {
        return readOnly;
    }
    private void checkWritable() throws IOException {
        if (readOnly)
            throw new ReadOnlyFileSystemException();
    }
    @Override
    public Iterable<Path> getRootDirectories() {
        ArrayList<Path> pathArr = new ArrayList<>();
        pathArr.add(new ZipPath(this, new byte[]{'/'}));
        return pathArr;
    }
    ZipPath getDefaultDir() {  
        return defaultdir;
    }
    @Override
    public ZipPath getPath(String first, String... more) {
        String path;
        if (more.length == 0) {
            path = first;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(first);
            for (String segment: more) {
                if (segment.length() > 0) {
                    if (sb.length() > 0)
                        sb.append('/');
                    sb.append(segment);
                }
            }
            path = sb.toString();
        }
        return new ZipPath(this, getBytes(path));
    }
    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        throw new UnsupportedOperationException();
    }
    @Override
    public WatchService newWatchService() {
        throw new UnsupportedOperationException();
    }
    FileStore getFileStore(ZipPath path) {
        return new ZipFileStore(path);
    }
    @Override
    public Iterable<FileStore> getFileStores() {
        ArrayList<FileStore> list = new ArrayList<>(1);
        list.add(new ZipFileStore(new ZipPath(this, new byte[]{'/'})));
        return list;
    }
    private static final Set<String> supportedFileAttributeViews =
            Collections.unmodifiableSet(
                new HashSet<String>(Arrays.asList("basic", "zip")));
    @Override
    public Set<String> supportedFileAttributeViews() {
        return supportedFileAttributeViews;
    }
    @Override
    public String toString() {
        return zfpath.toString();
    }
    Path getZipFile() {
        return zfpath;
    }
    private static final String GLOB_SYNTAX = "glob";
    private static final String REGEX_SYNTAX = "regex";
    @Override
    public PathMatcher getPathMatcher(String syntaxAndInput) {
        int pos = syntaxAndInput.indexOf(':');
        if (pos <= 0 || pos == syntaxAndInput.length()) {
            throw new IllegalArgumentException();
        }
        String syntax = syntaxAndInput.substring(0, pos);
        String input = syntaxAndInput.substring(pos + 1);
        String expr;
        if (syntax.equals(GLOB_SYNTAX)) {
            expr = toRegexPattern(input);
        } else {
            if (syntax.equals(REGEX_SYNTAX)) {
                expr = input;
            } else {
                throw new UnsupportedOperationException("Syntax '" + syntax +
                    "' not recognized");
            }
        }
        final Pattern pattern = Pattern.compile(expr);
        return new PathMatcher() {
            @Override
            public boolean matches(Path path) {
                return pattern.matcher(path.toString()).matches();
            }
        };
    }
    @Override
    public void close() throws IOException {
        beginWrite();
        try {
            if (!isOpen)
                return;
            isOpen = false;             
        } finally {
            endWrite();
        }
        if (!streams.isEmpty()) {       
            Set<InputStream> copy = new HashSet<>(streams);
            for (InputStream is: copy)
                is.close();
        }
        beginWrite();                   
        try {
            sync();
            ch.close();                 
        } finally {                     
            endWrite();
        }
        synchronized (inflaters) {
            for (Inflater inf : inflaters)
                inf.end();
        }
        synchronized (deflaters) {
            for (Deflater def : deflaters)
                def.end();
        }
        IOException ioe = null;
        synchronized (tmppaths) {
            for (Path p: tmppaths) {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException x) {
                    if (ioe == null)
                        ioe = x;
                    else
                        ioe.addSuppressed(x);
                }
            }
        }
        provider.removeFileSystem(zfpath, this);
        if (ioe != null)
           throw ioe;
    }
    ZipFileAttributes getFileAttributes(byte[] path)
        throws IOException
    {
        Entry e;
        beginRead();
        try {
            ensureOpen();
            e = getEntry0(path);
            if (e == null) {
                IndexNode inode = getInode(path);
                if (inode == null)
                    return null;
                e = new Entry(inode.name);       
                e.method = METHOD_STORED;        
                e.mtime = e.atime = e.ctime = -1;
            }
        } finally {
            endRead();
        }
        return new ZipFileAttributes(e);
    }
    void setTimes(byte[] path, FileTime mtime, FileTime atime, FileTime ctime)
        throws IOException
    {
        checkWritable();
        beginWrite();
        try {
            ensureOpen();
            Entry e = getEntry0(path);    
            if (e == null)
                throw new NoSuchFileException(getString(path));
            if (e.type == Entry.CEN)
                e.type = Entry.COPY;      
            if (mtime != null)
                e.mtime = mtime.toMillis();
            if (atime != null)
                e.atime = atime.toMillis();
            if (ctime != null)
                e.ctime = ctime.toMillis();
            update(e);
        } finally {
            endWrite();
        }
    }
    boolean exists(byte[] path)
        throws IOException
    {
        beginRead();
        try {
            ensureOpen();
            return getInode(path) != null;
        } finally {
            endRead();
        }
    }
    boolean isDirectory(byte[] path)
        throws IOException
    {
        beginRead();
        try {
            IndexNode n = getInode(path);
            return n != null && n.isDir();
        } finally {
            endRead();
        }
    }
    private ZipPath toZipPath(byte[] path) {
        byte[] p = new byte[path.length + 1];
        p[0] = '/';
        System.arraycopy(path, 0, p, 1, path.length);
        return new ZipPath(this, p);
    }
    Iterator<Path> iteratorOf(byte[] path,
                              DirectoryStream.Filter<? super Path> filter)
        throws IOException
    {
        beginWrite();    
        try {
            ensureOpen();
            IndexNode inode = getInode(path);
            if (inode == null)
                throw new NotDirectoryException(getString(path));
            List<Path> list = new ArrayList<>();
            IndexNode child = inode.child;
            while (child != null) {
                ZipPath zp = toZipPath(child.name);
                if (filter == null || filter.accept(zp))
                    list.add(zp);
                child = child.sibling;
            }
            return list.iterator();
        } finally {
            endWrite();
        }
    }
    void createDirectory(byte[] dir, FileAttribute<?>... attrs)
        throws IOException
    {
        checkWritable();
        dir = toDirectoryPath(dir);
        beginWrite();
        try {
            ensureOpen();
            if (dir.length == 0 || exists(dir))  
                throw new FileAlreadyExistsException(getString(dir));
            checkParents(dir);
            Entry e = new Entry(dir, Entry.NEW);
            e.method = METHOD_STORED;            
            update(e);
        } finally {
            endWrite();
        }
    }
    void copyFile(boolean deletesrc, byte[]src, byte[] dst, CopyOption... options)
        throws IOException
    {
        checkWritable();
        if (Arrays.equals(src, dst))
            return;    
        beginWrite();
        try {
            ensureOpen();
            Entry eSrc = getEntry0(src);  
            if (eSrc == null)
                throw new NoSuchFileException(getString(src));
            if (eSrc.isDir()) {    
                createDirectory(dst);
                return;
            }
            boolean hasReplace = false;
            boolean hasCopyAttrs = false;
            for (CopyOption opt : options) {
                if (opt == REPLACE_EXISTING)
                    hasReplace = true;
                else if (opt == COPY_ATTRIBUTES)
                    hasCopyAttrs = true;
            }
            Entry eDst = getEntry0(dst);
            if (eDst != null) {
                if (!hasReplace)
                    throw new FileAlreadyExistsException(getString(dst));
            } else {
                checkParents(dst);
            }
            Entry u = new Entry(eSrc, Entry.COPY);    
            u.name(dst);                              
            if (eSrc.type == Entry.NEW || eSrc.type == Entry.FILECH)
            {
                u.type = eSrc.type;    
                if (!deletesrc) {      
                    if (eSrc.bytes != null)
                        u.bytes = Arrays.copyOf(eSrc.bytes, eSrc.bytes.length);
                    else if (eSrc.file != null) {
                        u.file = getTempPathForEntry(null);
                        Files.copy(eSrc.file, u.file, REPLACE_EXISTING);
                    }
                }
            }
            if (!hasCopyAttrs)
                u.mtime = u.atime= u.ctime = System.currentTimeMillis();
            update(u);
            if (deletesrc)
                updateDelete(eSrc);
        } finally {
            endWrite();
        }
    }
    OutputStream newOutputStream(byte[] path, OpenOption... options)
        throws IOException
    {
        checkWritable();
        boolean hasCreateNew = false;
        boolean hasCreate = false;
        boolean hasAppend = false;
        for (OpenOption opt: options) {
            if (opt == READ)
                throw new IllegalArgumentException("READ not allowed");
            if (opt == CREATE_NEW)
                hasCreateNew = true;
            if (opt == CREATE)
                hasCreate = true;
            if (opt == APPEND)
                hasAppend = true;
        }
        beginRead();                 
        try {                        
            ensureOpen();            
            Entry e = getEntry0(path);
            if (e != null) {
                if (e.isDir() || hasCreateNew)
                    throw new FileAlreadyExistsException(getString(path));
                if (hasAppend) {
                    InputStream is = getInputStream(e);
                    OutputStream os = getOutputStream(new Entry(e, Entry.NEW));
                    copyStream(is, os);
                    is.close();
                    return os;
                }
                return getOutputStream(new Entry(e, Entry.NEW));
            } else {
                if (!hasCreate && !hasCreateNew)
                    throw new NoSuchFileException(getString(path));
                checkParents(path);
                return getOutputStream(new Entry(path, Entry.NEW));
            }
        } finally {
            endRead();
        }
    }
    InputStream newInputStream(byte[] path) throws IOException {
        beginRead();
        try {
            ensureOpen();
            Entry e = getEntry0(path);
            if (e == null)
                throw new NoSuchFileException(getString(path));
            if (e.isDir())
                throw new FileSystemException(getString(path), "is a directory", null);
            return getInputStream(e);
        } finally {
            endRead();
        }
    }
    private void checkOptions(Set<? extends OpenOption> options) {
        for (OpenOption option : options) {
            if (option == null)
                throw new NullPointerException();
            if (!(option instanceof StandardOpenOption))
                throw new IllegalArgumentException();
        }
    }
    SeekableByteChannel newByteChannel(byte[] path,
                                       Set<? extends OpenOption> options,
                                       FileAttribute<?>... attrs)
        throws IOException
    {
        checkOptions(options);
        if (options.contains(StandardOpenOption.WRITE) ||
            options.contains(StandardOpenOption.APPEND)) {
            checkWritable();
            beginRead();
            try {
                final WritableByteChannel wbc = Channels.newChannel(
                    newOutputStream(path, options.toArray(new OpenOption[0])));
                long leftover = 0;
                if (options.contains(StandardOpenOption.APPEND)) {
                    Entry e = getEntry0(path);
                    if (e != null && e.size >= 0)
                        leftover = e.size;
                }
                final long offset = leftover;
                return new SeekableByteChannel() {
                    long written = offset;
                    public boolean isOpen() {
                        return wbc.isOpen();
                    }
                    public long position() throws IOException {
                        return written;
                    }
                    public SeekableByteChannel position(long pos)
                        throws IOException
                    {
                        throw new UnsupportedOperationException();
                    }
                    public int read(ByteBuffer dst) throws IOException {
                        throw new UnsupportedOperationException();
                    }
                    public SeekableByteChannel truncate(long size)
                        throws IOException
                    {
                        throw new UnsupportedOperationException();
                    }
                    public int write(ByteBuffer src) throws IOException {
                        int n = wbc.write(src);
                        written += n;
                        return n;
                    }
                    public long size() throws IOException {
                        return written;
                    }
                    public void close() throws IOException {
                        wbc.close();
                    }
                };
            } finally {
                endRead();
            }
        } else {
            beginRead();
            try {
                ensureOpen();
                Entry e = getEntry0(path);
                if (e == null || e.isDir())
                    throw new NoSuchFileException(getString(path));
                final ReadableByteChannel rbc =
                    Channels.newChannel(getInputStream(e));
                final long size = e.size;
                return new SeekableByteChannel() {
                    long read = 0;
                    public boolean isOpen() {
                        return rbc.isOpen();
                    }
                    public long position() throws IOException {
                        return read;
                    }
                    public SeekableByteChannel position(long pos)
                        throws IOException
                    {
                        throw new UnsupportedOperationException();
                    }
                    public int read(ByteBuffer dst) throws IOException {
                        return rbc.read(dst);
                    }
                    public SeekableByteChannel truncate(long size)
                    throws IOException
                    {
                        throw new NonWritableChannelException();
                    }
                    public int write (ByteBuffer src) throws IOException {
                        throw new NonWritableChannelException();
                    }
                    public long size() throws IOException {
                        return size;
                    }
                    public void close() throws IOException {
                        rbc.close();
                    }
                };
            } finally {
                endRead();
            }
        }
    }
    FileChannel newFileChannel(byte[] path,
                               Set<? extends OpenOption> options,
                               FileAttribute<?>... attrs)
        throws IOException
    {
        checkOptions(options);
        final  boolean forWrite = (options.contains(StandardOpenOption.WRITE) ||
                                   options.contains(StandardOpenOption.APPEND));
        beginRead();
        try {
            ensureOpen();
            Entry e = getEntry0(path);
            if (forWrite) {
                checkWritable();
                if (e == null) {
                if (!options.contains(StandardOpenOption.CREATE_NEW))
                    throw new NoSuchFileException(getString(path));
                } else {
                    if (options.contains(StandardOpenOption.CREATE_NEW))
                        throw new FileAlreadyExistsException(getString(path));
                    if (e.isDir())
                        throw new FileAlreadyExistsException("directory <"
                            + getString(path) + "> exists");
                }
                options.remove(StandardOpenOption.CREATE_NEW); 
            } else if (e == null || e.isDir()) {
                throw new NoSuchFileException(getString(path));
            }
            final boolean isFCH = (e != null && e.type == Entry.FILECH);
            final Path tmpfile = isFCH ? e.file : getTempPathForEntry(path);
            final FileChannel fch = tmpfile.getFileSystem()
                                           .provider()
                                           .newFileChannel(tmpfile, options, attrs);
            final Entry u = isFCH ? e : new Entry(path, tmpfile, Entry.FILECH);
            if (forWrite) {
                u.flag = FLAG_DATADESCR;
                u.method = METHOD_DEFLATED;
            }
            return new FileChannel() {
                public int write(ByteBuffer src) throws IOException {
                    return fch.write(src);
                }
                public long write(ByteBuffer[] srcs, int offset, int length)
                    throws IOException
                {
                    return fch.write(srcs, offset, length);
                }
                public long position() throws IOException {
                    return fch.position();
                }
                public FileChannel position(long newPosition)
                    throws IOException
                {
                    fch.position(newPosition);
                    return this;
                }
                public long size() throws IOException {
                    return fch.size();
                }
                public FileChannel truncate(long size)
                    throws IOException
                {
                    fch.truncate(size);
                    return this;
                }
                public void force(boolean metaData)
                    throws IOException
                {
                    fch.force(metaData);
                }
                public long transferTo(long position, long count,
                                       WritableByteChannel target)
                    throws IOException
                {
                    return fch.transferTo(position, count, target);
                }
                public long transferFrom(ReadableByteChannel src,
                                         long position, long count)
                    throws IOException
                {
                    return fch.transferFrom(src, position, count);
                }
                public int read(ByteBuffer dst) throws IOException {
                    return fch.read(dst);
                }
                public int read(ByteBuffer dst, long position)
                    throws IOException
                {
                    return fch.read(dst, position);
                }
                public long read(ByteBuffer[] dsts, int offset, int length)
                    throws IOException
                {
                    return fch.read(dsts, offset, length);
                }
                public int write(ByteBuffer src, long position)
                    throws IOException
                    {
                   return fch.write(src, position);
                }
                public MappedByteBuffer map(MapMode mode,
                                            long position, long size)
                    throws IOException
                {
                    throw new UnsupportedOperationException();
                }
                public FileLock lock(long position, long size, boolean shared)
                    throws IOException
                {
                    return fch.lock(position, size, shared);
                }
                public FileLock tryLock(long position, long size, boolean shared)
                    throws IOException
                {
                    return fch.tryLock(position, size, shared);
                }
                protected void implCloseChannel() throws IOException {
                    fch.close();
                    if (forWrite) {
                        u.mtime = System.currentTimeMillis();
                        u.size = Files.size(u.file);
                        update(u);
                    } else {
                        if (!isFCH)    
                            removeTempPathForEntry(tmpfile);
                    }
               }
            };
        } finally {
            endRead();
        }
    }
    private Set<InputStream> streams =
        Collections.synchronizedSet(new HashSet<InputStream>());
    private Set<ExChannelCloser> exChClosers = new HashSet<>();
    private Set<Path> tmppaths = Collections.synchronizedSet(new HashSet<Path>());
    private Path getTempPathForEntry(byte[] path) throws IOException {
        Path tmpPath = createTempFileInSameDirectoryAs(zfpath);
        if (path != null) {
            Entry e = getEntry0(path);
            if (e != null) {
                try (InputStream is = newInputStream(path)) {
                    Files.copy(is, tmpPath, REPLACE_EXISTING);
                }
            }
        }
        return tmpPath;
    }
    private void removeTempPathForEntry(Path path) throws IOException {
        Files.delete(path);
        tmppaths.remove(path);
    }
    private void checkParents(byte[] path) throws IOException {
        beginRead();
        try {
            while ((path = getParent(path)) != null && path.length != 0) {
                if (!inodes.containsKey(IndexNode.keyOf(path))) {
                    throw new NoSuchFileException(getString(path));
                }
            }
        } finally {
            endRead();
        }
    }
    private static byte[] ROOTPATH = new byte[0];
    private static byte[] getParent(byte[] path) {
        int off = path.length - 1;
        if (off > 0 && path[off] == '/')  
            off--;
        while (off > 0 && path[off] != '/') { off--; }
        if (off <= 0)
            return ROOTPATH;
        return Arrays.copyOf(path, off + 1);
    }
    private final void beginWrite() {
        rwlock.writeLock().lock();
    }
    private final void endWrite() {
        rwlock.writeLock().unlock();
    }
    private final void beginRead() {
        rwlock.readLock().lock();
    }
    private final void endRead() {
        rwlock.readLock().unlock();
    }
    private volatile boolean isOpen = true;
    private final SeekableByteChannel ch; 
    final byte[]  cen;     
    private END  end;
    private long locpos;   
    private final ReadWriteLock rwlock = new ReentrantReadWriteLock();
    private LinkedHashMap<IndexNode, IndexNode> inodes;
    final byte[] getBytes(String name) {
        return zc.getBytes(name);
    }
    final String getString(byte[] name) {
        return zc.toString(name);
    }
    protected void finalize() throws IOException {
        close();
    }
    private long getDataPos(Entry e) throws IOException {
        if (e.locoff == -1) {
            Entry e2 = getEntry0(e.name);
            if (e2 == null)
                throw new ZipException("invalid loc for entry <" + e.name + ">");
            e.locoff = e2.locoff;
        }
        byte[] buf = new byte[LOCHDR];
        if (readFullyAt(buf, 0, buf.length, e.locoff) != buf.length)
            throw new ZipException("invalid loc for entry <" + e.name + ">");
        return locpos + e.locoff + LOCHDR + LOCNAM(buf) + LOCEXT(buf);
    }
    final long readFullyAt(byte[] buf, int off, long len, long pos)
        throws IOException
    {
        ByteBuffer bb = ByteBuffer.wrap(buf);
        bb.position(off);
        bb.limit((int)(off + len));
        return readFullyAt(bb, pos);
    }
    private final long readFullyAt(ByteBuffer bb, long pos)
        throws IOException
    {
        synchronized(ch) {
            return ch.position(pos).read(bb);
        }
    }
    private END findEND() throws IOException
    {
        byte[] buf = new byte[READBLOCKSZ];
        long ziplen = ch.size();
        long minHDR = (ziplen - END_MAXLEN) > 0 ? ziplen - END_MAXLEN : 0;
        long minPos = minHDR - (buf.length - ENDHDR);
        for (long pos = ziplen - buf.length; pos >= minPos; pos -= (buf.length - ENDHDR))
        {
            int off = 0;
            if (pos < 0) {
                off = (int)-pos;
                Arrays.fill(buf, 0, off, (byte)0);
            }
            int len = buf.length - off;
            if (readFullyAt(buf, off, len, pos + off) != len)
                zerror("zip END header not found");
            for (int i = buf.length - ENDHDR; i >= 0; i--) {
                if (buf[i+0] == (byte)'P'    &&
                    buf[i+1] == (byte)'K'    &&
                    buf[i+2] == (byte)'\005' &&
                    buf[i+3] == (byte)'\006' &&
                    (pos + i + ENDHDR + ENDCOM(buf, i) == ziplen)) {
                    buf = Arrays.copyOfRange(buf, i, i + ENDHDR);
                    END end = new END();
                    end.endsub = ENDSUB(buf);
                    end.centot = ENDTOT(buf);
                    end.cenlen = ENDSIZ(buf);
                    end.cenoff = ENDOFF(buf);
                    end.comlen = ENDCOM(buf);
                    end.endpos = pos + i;
                    if (end.cenlen == ZIP64_MINVAL ||
                        end.cenoff == ZIP64_MINVAL ||
                        end.centot == ZIP64_MINVAL32)
                    {
                        byte[] loc64 = new byte[ZIP64_LOCHDR];
                        if (readFullyAt(loc64, 0, loc64.length, end.endpos - ZIP64_LOCHDR)
                            != loc64.length) {
                            return end;
                        }
                        long end64pos = ZIP64_LOCOFF(loc64);
                        byte[] end64buf = new byte[ZIP64_ENDHDR];
                        if (readFullyAt(end64buf, 0, end64buf.length, end64pos)
                            != end64buf.length) {
                            return end;
                        }
                        end.cenlen = ZIP64_ENDSIZ(end64buf);
                        end.cenoff = ZIP64_ENDOFF(end64buf);
                        end.centot = (int)ZIP64_ENDTOT(end64buf); 
                        end.endpos = end64pos;
                    }
                    return end;
                }
            }
        }
        zerror("zip END header not found");
        return null; 
    }
    private byte[] initCEN() throws IOException {
        end = findEND();
        if (end.endpos == 0) {
            inodes = new LinkedHashMap<>(10);
            locpos = 0;
            buildNodeTree();
            return null;         
        }
        if (end.cenlen > end.endpos)
            zerror("invalid END header (bad central directory size)");
        long cenpos = end.endpos - end.cenlen;     
        locpos = cenpos - end.cenoff;
        if (locpos < 0)
            zerror("invalid END header (bad central directory offset)");
        byte[] cen = new byte[(int)(end.cenlen + ENDHDR)];
        if (readFullyAt(cen, 0, cen.length, cenpos) != end.cenlen + ENDHDR) {
            zerror("read CEN tables failed");
        }
        inodes = new LinkedHashMap<>(end.centot + 1);
        int pos = 0;
        int limit = cen.length - ENDHDR;
        while (pos < limit) {
            if (CENSIG(cen, pos) != CENSIG)
                zerror("invalid CEN header (bad signature)");
            int method = CENHOW(cen, pos);
            int nlen   = CENNAM(cen, pos);
            int elen   = CENEXT(cen, pos);
            int clen   = CENCOM(cen, pos);
            if ((CENFLG(cen, pos) & 1) != 0)
                zerror("invalid CEN header (encrypted entry)");
            if (method != METHOD_STORED && method != METHOD_DEFLATED)
                zerror("invalid CEN header (unsupported compression method: " + method + ")");
            if (pos + CENHDR + nlen > limit)
                zerror("invalid CEN header (bad header size)");
            byte[] name = Arrays.copyOfRange(cen, pos + CENHDR, pos + CENHDR + nlen);
            IndexNode inode = new IndexNode(name, pos);
            inodes.put(inode, inode);
            pos += (CENHDR + nlen + elen + clen);
        }
        if (pos + ENDHDR != cen.length) {
            zerror("invalid CEN header (bad header size)");
        }
        buildNodeTree();
        return cen;
    }
    private void ensureOpen() throws IOException {
        if (!isOpen)
            throw new ClosedFileSystemException();
    }
    private Path createTempFileInSameDirectoryAs(Path path)
        throws IOException
    {
        Path parent = path.toAbsolutePath().getParent();
        String dir = (parent == null)? "." : parent.toString();
        Path tmpPath = File.createTempFile("zipfstmp", null, new File(dir)).toPath();
        tmppaths.add(tmpPath);
        return tmpPath;
    }
    private boolean hasUpdate = false;
    private final IndexNode LOOKUPKEY = IndexNode.keyOf(null);
    private void updateDelete(IndexNode inode) {
        beginWrite();
        try {
            removeFromTree(inode);
            inodes.remove(inode);
            hasUpdate = true;
        } finally {
             endWrite();
        }
    }
    private void update(Entry e) {
        beginWrite();
        try {
            IndexNode old = inodes.put(e, e);
            if (old != null) {
                removeFromTree(old);
            }
            if (e.type == Entry.NEW || e.type == Entry.FILECH) {
                IndexNode parent = inodes.get(LOOKUPKEY.as(getParent(e.name)));
                e.sibling = parent.child;
                parent.child = e;
            }
            hasUpdate = true;
        } finally {
            endWrite();
        }
    }
    private long copyLOCEntry(Entry e, boolean updateHeader,
                              OutputStream os,
                              long written, byte[] buf)
        throws IOException
    {
        long locoff = e.locoff;  
        e.locoff = written;      
        long size = 0;
        if ((e.flag & FLAG_DATADESCR) != 0) {
            if (e.size >= ZIP64_MINVAL || e.csize >= ZIP64_MINVAL)
                size = 24;
            else
                size = 16;
        }
        if (readFullyAt(buf, 0, LOCHDR , locoff) != LOCHDR)
            throw new ZipException("loc: reading failed");
        if (updateHeader) {
            locoff += LOCHDR + LOCNAM(buf) + LOCEXT(buf);  
            size += e.csize;
            written = e.writeLOC(os) + size;
        } else {
            os.write(buf, 0, LOCHDR);    
            locoff += LOCHDR;
            size += LOCNAM(buf) + LOCEXT(buf) + e.csize;
            written = LOCHDR + size;
        }
        int n;
        while (size > 0 &&
            (n = (int)readFullyAt(buf, 0, buf.length, locoff)) != -1)
        {
            if (size < n)
                n = (int)size;
            os.write(buf, 0, n);
            size -= n;
            locoff += n;
        }
        return written;
    }
    private void sync() throws IOException {
        if (!exChClosers.isEmpty()) {
            for (ExChannelCloser ecc : exChClosers) {
                if (ecc.streams.isEmpty()) {
                    ecc.ch.close();
                    Files.delete(ecc.path);
                    exChClosers.remove(ecc);
                }
            }
        }
        if (!hasUpdate)
            return;
        Path tmpFile = createTempFileInSameDirectoryAs(zfpath);
        OutputStream os = Files.newOutputStream(tmpFile, WRITE);
        ArrayList<Entry> elist = new ArrayList<>(inodes.size());
        long written = 0;
        byte[] buf = new byte[8192];
        Entry e = null;
        for (IndexNode inode : inodes.values()) {
            if (inode instanceof Entry) {    
                e = (Entry)inode;
                try {
                    if (e.type == Entry.COPY) {
                        written += copyLOCEntry(e, true, os, written, buf);
                    } else {                          
                        e.locoff = written;
                        written += e.writeLOC(os);    
                        if (e.bytes != null) {        
                            os.write(e.bytes);        
                            written += e.bytes.length;
                        } else if (e.file != null) {  
                            try (InputStream is = Files.newInputStream(e.file)) {
                                int n;
                                if (e.type == Entry.NEW) {  
                                    while ((n = is.read(buf)) != -1) {
                                        os.write(buf, 0, n);
                                        written += n;
                                    }
                                } else if (e.type == Entry.FILECH) {
                                    try (OutputStream os2 = new EntryOutputStream(e, os)) {
                                        while ((n = is.read(buf)) != -1) {
                                            os2.write(buf, 0, n);
                                        }
                                    }
                                    written += e.csize;
                                    if ((e.flag & FLAG_DATADESCR) != 0)
                                        written += e.writeEXT(os);
                                }
                            }
                            Files.delete(e.file);
                            tmppaths.remove(e.file);
                        } else {
                        }
                    }
                    elist.add(e);
                } catch (IOException x) {
                    x.printStackTrace();    
                }
            } else {                        
                if (inode.pos == -1) {
                    continue;               
                }
                e = Entry.readCEN(this, inode.pos);
                try {
                    written += copyLOCEntry(e, false, os, written, buf);
                    elist.add(e);
                } catch (IOException x) {
                    x.printStackTrace();    
                }
            }
        }
        end.cenoff = written;
        for (Entry entry : elist) {
            written += entry.writeCEN(os);
        }
        end.centot = elist.size();
        end.cenlen = written - end.cenoff;
        end.write(os, written);
        os.close();
        if (!streams.isEmpty()) {
            ExChannelCloser ecc = new ExChannelCloser(
                                      createTempFileInSameDirectoryAs(zfpath),
                                      ch,
                                      streams);
            Files.move(zfpath, ecc.path, REPLACE_EXISTING);
            exChClosers.add(ecc);
            streams = Collections.synchronizedSet(new HashSet<InputStream>());
        } else {
            ch.close();
            Files.delete(zfpath);
        }
        Files.move(tmpFile, zfpath, REPLACE_EXISTING);
        hasUpdate = false;    
    }
    private IndexNode getInode(byte[] path) {
        if (path == null)
            throw new NullPointerException("path");
        IndexNode key = IndexNode.keyOf(path);
        IndexNode inode = inodes.get(key);
        if (inode == null &&
            (path.length == 0 || path[path.length -1] != '/')) {
            path = Arrays.copyOf(path, path.length + 1);
            path[path.length - 1] = '/';
            inode = inodes.get(key.as(path));
        }
        return inode;
    }
    private Entry getEntry0(byte[] path) throws IOException {
        IndexNode inode = getInode(path);
        if (inode instanceof Entry)
            return (Entry)inode;
        if (inode == null || inode.pos == -1)
            return null;
        return Entry.readCEN(this, inode.pos);
    }
    public void deleteFile(byte[] path, boolean failIfNotExists)
        throws IOException
    {
        checkWritable();
        IndexNode inode = getInode(path);
        if (inode == null) {
            if (path != null && path.length == 0)
                throw new ZipException("root directory </> can't not be delete");
            if (failIfNotExists)
                throw new NoSuchFileException(getString(path));
        } else {
            if (inode.isDir() && inode.child != null)
                throw new DirectoryNotEmptyException(getString(path));
            updateDelete(inode);
        }
    }
    private static void copyStream(InputStream is, OutputStream os)
        throws IOException
    {
        byte[] copyBuf = new byte[8192];
        int n;
        while ((n = is.read(copyBuf)) != -1) {
            os.write(copyBuf, 0, n);
        }
    }
    private OutputStream getOutputStream(Entry e) throws IOException {
        if (e.mtime == -1)
            e.mtime = System.currentTimeMillis();
        if (e.method == -1)
            e.method = METHOD_DEFLATED;  
        e.flag = 0;
        if (zc.isUTF8())
            e.flag |= FLAG_EFS;
        OutputStream os;
        if (useTempFile) {
            e.file = getTempPathForEntry(null);
            os = Files.newOutputStream(e.file, WRITE);
        } else {
            os = new ByteArrayOutputStream((e.size > 0)? (int)e.size : 8192);
        }
        return new EntryOutputStream(e, os);
    }
    private InputStream getInputStream(Entry e)
        throws IOException
    {
        InputStream eis = null;
        if (e.type == Entry.NEW) {
            if (e.bytes != null)
                eis = new ByteArrayInputStream(e.bytes);
            else if (e.file != null)
                eis = Files.newInputStream(e.file);
            else
                throw new ZipException("update entry data is missing");
        } else if (e.type == Entry.FILECH) {
            eis = Files.newInputStream(e.file);
            return eis;
        } else {  
            eis = new EntryInputStream(e, ch);
        }
        if (e.method == METHOD_DEFLATED) {
            long bufSize = e.size + 2; 
            if (bufSize > 65536)
                bufSize = 8192;
            final long size = e.size;
            eis = new InflaterInputStream(eis, getInflater(), (int)bufSize) {
                private boolean isClosed = false;
                public void close() throws IOException {
                    if (!isClosed) {
                        releaseInflater(inf);
                        this.in.close();
                        isClosed = true;
                        streams.remove(this);
                    }
                }
                protected void fill() throws IOException {
                    if (eof) {
                        throw new EOFException(
                            "Unexpected end of ZLIB input stream");
                    }
                    len = this.in.read(buf, 0, buf.length);
                    if (len == -1) {
                        buf[0] = 0;
                        len = 1;
                        eof = true;
                    }
                    inf.setInput(buf, 0, len);
                }
                private boolean eof;
                public int available() throws IOException {
                    if (isClosed)
                        return 0;
                    long avail = size - inf.getBytesWritten();
                    return avail > (long) Integer.MAX_VALUE ?
                        Integer.MAX_VALUE : (int) avail;
                }
            };
        } else if (e.method == METHOD_STORED) {
        } else {
            throw new ZipException("invalid compression method");
        }
        streams.add(eis);
        return eis;
    }
    private class EntryInputStream extends InputStream {
        private final SeekableByteChannel zfch; 
        private   long pos;               
        protected long rem;               
        protected final long size;        
        EntryInputStream(Entry e, SeekableByteChannel zfch)
            throws IOException
        {
            this.zfch = zfch;
            rem = e.csize;
            size = e.size;
            pos = getDataPos(e);
        }
        public int read(byte b[], int off, int len) throws IOException {
            ensureOpen();
            if (rem == 0) {
                return -1;
            }
            if (len <= 0) {
                return 0;
            }
            if (len > rem) {
                len = (int) rem;
            }
            long n = 0;
            ByteBuffer bb = ByteBuffer.wrap(b);
            bb.position(off);
            bb.limit(off + len);
            synchronized(zfch) {
                n = zfch.position(pos).read(bb);
            }
            if (n > 0) {
                pos += n;
                rem -= n;
            }
            if (rem == 0) {
                close();
            }
            return (int)n;
        }
        public int read() throws IOException {
            byte[] b = new byte[1];
            if (read(b, 0, 1) == 1) {
                return b[0] & 0xff;
            } else {
                return -1;
            }
        }
        public long skip(long n) throws IOException {
            ensureOpen();
            if (n > rem)
                n = rem;
            pos += n;
            rem -= n;
            if (rem == 0) {
                close();
            }
            return n;
        }
        public int available() {
            return rem > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) rem;
        }
        public long size() {
            return size;
        }
        public void close() {
            rem = 0;
            streams.remove(this);
        }
    }
    class EntryOutputStream extends DeflaterOutputStream
    {
        private CRC32 crc;
        private Entry e;
        private long written;
        EntryOutputStream(Entry e, OutputStream os)
            throws IOException
        {
            super(os, getDeflater());
            if (e == null)
                throw new NullPointerException("Zip entry is null");
            this.e = e;
            crc = new CRC32();
        }
        @Override
        public void write(byte b[], int off, int len) throws IOException {
            if (e.type != Entry.FILECH)    
                ensureOpen();
            if (off < 0 || len < 0 || off > b.length - len) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return;
            }
            switch (e.method) {
            case METHOD_DEFLATED:
                super.write(b, off, len);
                break;
            case METHOD_STORED:
                written += len;
                out.write(b, off, len);
                break;
            default:
                throw new ZipException("invalid compression method");
            }
            crc.update(b, off, len);
        }
        @Override
        public void close() throws IOException {
            switch (e.method) {
            case METHOD_DEFLATED:
                finish();
                e.size  = def.getBytesRead();
                e.csize = def.getBytesWritten();
                e.crc = crc.getValue();
                break;
            case METHOD_STORED:
                e.size = e.csize = written;
                e.crc = crc.getValue();
                break;
            default:
                throw new ZipException("invalid compression method");
            }
            if (out instanceof ByteArrayOutputStream)
                e.bytes = ((ByteArrayOutputStream)out).toByteArray();
            if (e.type == Entry.FILECH) {
                releaseDeflater(def);
                return;
            }
            super.close();
            releaseDeflater(def);
            update(e);
        }
    }
    static void zerror(String msg) {
        throw new ZipError(msg);
    }
    private final int MAX_FLATER = 20;
    private final List<Inflater> inflaters = new ArrayList<>();
    private Inflater getInflater() {
        synchronized (inflaters) {
            int size = inflaters.size();
            if (size > 0) {
                Inflater inf = (Inflater)inflaters.remove(size - 1);
                return inf;
            } else {
                return new Inflater(true);
            }
        }
    }
    private void releaseInflater(Inflater inf) {
        synchronized (inflaters) {
            if (inflaters.size() < MAX_FLATER) {
                inf.reset();
                inflaters.add(inf);
            } else {
                inf.end();
            }
        }
    }
    private final List<Deflater> deflaters = new ArrayList<>();
    private Deflater getDeflater() {
        synchronized (deflaters) {
            int size = deflaters.size();
            if (size > 0) {
                Deflater def = (Deflater)deflaters.remove(size - 1);
                return def;
            } else {
                return new Deflater(Deflater.DEFAULT_COMPRESSION, true);
            }
        }
    }
    private void releaseDeflater(Deflater def) {
        synchronized (deflaters) {
            if (inflaters.size() < MAX_FLATER) {
               def.reset();
               deflaters.add(def);
            } else {
               def.end();
            }
        }
    }
    static class END {
        int  disknum;
        int  sdisknum;
        int  endsub;     
        int  centot;     
        long cenlen;     
        long cenoff;     
        int  comlen;     
        byte[] comment;
        int diskNum;
        long endpos;
        int disktot;
        void write(OutputStream os, long offset) throws IOException {
            boolean hasZip64 = false;
            long xlen = cenlen;
            long xoff = cenoff;
            if (xlen >= ZIP64_MINVAL) {
                xlen = ZIP64_MINVAL;
                hasZip64 = true;
            }
            if (xoff >= ZIP64_MINVAL) {
                xoff = ZIP64_MINVAL;
                hasZip64 = true;
            }
            int count = centot;
            if (count >= ZIP64_MINVAL32) {
                count = ZIP64_MINVAL32;
                hasZip64 = true;
            }
            if (hasZip64) {
                long off64 = offset;
                writeInt(os, ZIP64_ENDSIG);       
                writeLong(os, ZIP64_ENDHDR - 12); 
                writeShort(os, 45);               
                writeShort(os, 45);               
                writeInt(os, 0);                  
                writeInt(os, 0);                  
                writeLong(os, centot);            
                writeLong(os, centot);            
                writeLong(os, cenlen);            
                writeLong(os, cenoff);            
                writeInt(os, ZIP64_LOCSIG);       
                writeInt(os, 0);                  
                writeLong(os, off64);             
                writeInt(os, 1);                  
            }
            writeInt(os, ENDSIG);                 
            writeShort(os, 0);                    
            writeShort(os, 0);                    
            writeShort(os, count);                
            writeShort(os, count);                
            writeInt(os, xlen);                   
            writeInt(os, xoff);                   
            if (comment != null) {            
                writeShort(os, comment.length);
                writeBytes(os, comment);
            } else {
                writeShort(os, 0);
            }
        }
    }
    static class IndexNode {
        byte[] name;
        int    hashcode;  
        int    pos = -1;  
        IndexNode(byte[] name, int pos) {
            name(name);
            this.pos = pos;
        }
        final static IndexNode keyOf(byte[] name) { 
            return new IndexNode(name, -1);
        }
        final void name(byte[] name) {
            this.name = name;
            this.hashcode = Arrays.hashCode(name);
        }
        final IndexNode as(byte[] name) {           
            name(name);                             
            return this;
        }
        boolean isDir() {
            return name != null &&
                   (name.length == 0 || name[name.length - 1] == '/');
        }
        public boolean equals(Object other) {
            if (!(other instanceof IndexNode)) {
                return false;
            }
            return Arrays.equals(name, ((IndexNode)other).name);
        }
        public int hashCode() {
            return hashcode;
        }
        IndexNode() {}
        IndexNode sibling;
        IndexNode child;  
    }
    static class Entry extends IndexNode {
        static final int CEN    = 1;    
        static final int NEW    = 2;    
        static final int FILECH = 3;    
        static final int COPY   = 4;    
        byte[] bytes;      
        Path   file;       
        int    type = CEN; 
        int    version;
        int    flag;
        int    method = -1;    
        long   mtime  = -1;    
        long   atime  = -1;    
        long   ctime  = -1;    
        long   crc    = -1;    
        long   csize  = -1;    
        long   size   = -1;    
        byte[] extra;
        int    versionMade;
        int    disk;
        int    attrs;
        long   attrsEx;
        long   locoff;
        byte[] comment;
        Entry() {}
        Entry(byte[] name) {
            name(name);
            this.mtime  = System.currentTimeMillis();
            this.crc    = 0;
            this.size   = 0;
            this.csize  = 0;
            this.method = METHOD_DEFLATED;
        }
        Entry(byte[] name, int type) {
            this(name);
            this.type = type;
        }
        Entry (Entry e, int type) {
            name(e.name);
            this.version   = e.version;
            this.ctime     = e.ctime;
            this.atime     = e.atime;
            this.mtime     = e.mtime;
            this.crc       = e.crc;
            this.size      = e.size;
            this.csize     = e.csize;
            this.method    = e.method;
            this.extra     = e.extra;
            this.versionMade = e.versionMade;
            this.disk      = e.disk;
            this.attrs     = e.attrs;
            this.attrsEx   = e.attrsEx;
            this.locoff    = e.locoff;
            this.comment   = e.comment;
            this.type      = type;
        }
        Entry (byte[] name, Path file, int type) {
            this(name, type);
            this.file = file;
            this.method = METHOD_STORED;
        }
        int version() throws ZipException {
            if (method == METHOD_DEFLATED)
                return 20;
            else if (method == METHOD_STORED)
                return 10;
            throw new ZipException("unsupported compression method");
        }
        static Entry readCEN(ZipFileSystem zipfs, int pos)
            throws IOException
        {
            return new Entry().cen(zipfs, pos);
        }
        private Entry cen(ZipFileSystem zipfs, int pos)
            throws IOException
        {
            byte[] cen = zipfs.cen;
            if (CENSIG(cen, pos) != CENSIG)
                zerror("invalid CEN header (bad signature)");
            versionMade = CENVEM(cen, pos);
            version     = CENVER(cen, pos);
            flag        = CENFLG(cen, pos);
            method      = CENHOW(cen, pos);
            mtime       = dosToJavaTime(CENTIM(cen, pos));
            crc         = CENCRC(cen, pos);
            csize       = CENSIZ(cen, pos);
            size        = CENLEN(cen, pos);
            int nlen    = CENNAM(cen, pos);
            int elen    = CENEXT(cen, pos);
            int clen    = CENCOM(cen, pos);
            disk        = CENDSK(cen, pos);
            attrs       = CENATT(cen, pos);
            attrsEx     = CENATX(cen, pos);
            locoff      = CENOFF(cen, pos);
            pos += CENHDR;
            name(Arrays.copyOfRange(cen, pos, pos + nlen));
            pos += nlen;
            if (elen > 0) {
                extra = Arrays.copyOfRange(cen, pos, pos + elen);
                pos += elen;
                readExtra(zipfs);
            }
            if (clen > 0) {
                comment = Arrays.copyOfRange(cen, pos, pos + clen);
            }
            return this;
        }
        int writeCEN(OutputStream os) throws IOException
        {
            int written  = CENHDR;
            int version0 = version();
            long csize0  = csize;
            long size0   = size;
            long locoff0 = locoff;
            int elen64   = 0;                
            int elenNTFS = 0;                
            int elenEXTT = 0;                
            int nlen = (name != null) ? name.length : 0;
            int elen = (extra != null) ? extra.length : 0;
            int clen = (comment != null) ? comment.length : 0;
            if (csize >= ZIP64_MINVAL) {
                csize0 = ZIP64_MINVAL;
                elen64 += 8;                 
            }
            if (size >= ZIP64_MINVAL) {
                size0 = ZIP64_MINVAL;        
                elen64 += 8;
            }
            if (locoff >= ZIP64_MINVAL) {
                locoff0 = ZIP64_MINVAL;
                elen64 += 8;                 
            }
            if (elen64 != 0)
                elen64 += 4;                 
            if (atime != -1) {
                if (isWindows)               
                    elenNTFS = 36;           
                else                         
                    elenEXTT = 9;            
            }
            writeInt(os, CENSIG);            
            if (elen64 != 0) {
                writeShort(os, 45);          
                writeShort(os, 45);
            } else {
                writeShort(os, version0);    
                writeShort(os, version0);    
            }
            writeShort(os, flag);            
            writeShort(os, method);          
            writeInt(os, (int)javaToDosTime(mtime));
            writeInt(os, crc);               
            writeInt(os, csize0);            
            writeInt(os, size0);             
            writeShort(os, name.length);
            writeShort(os, elen + elen64 + elenNTFS + elenEXTT);
            if (comment != null) {
                writeShort(os, Math.min(clen, 0xffff));
            } else {
                writeShort(os, 0);
            }
            writeShort(os, 0);              
            writeShort(os, 0);              
            writeInt(os, 0);                
            writeInt(os, locoff0);          
            writeBytes(os, name);
            if (elen64 != 0) {
                writeShort(os, EXTID_ZIP64);
                writeShort(os, elen64);     
                if (size0 == ZIP64_MINVAL)
                    writeLong(os, size);
                if (csize0 == ZIP64_MINVAL)
                    writeLong(os, csize);
                if (locoff0 == ZIP64_MINVAL)
                    writeLong(os, locoff);
            }
            if (elenNTFS != 0) {
                writeShort(os, EXTID_NTFS);
                writeShort(os, elenNTFS - 4);
                writeInt(os, 0);            
                writeShort(os, 0x0001);     
                writeShort(os, 24);
                writeLong(os, javaToWinTime(mtime));
                writeLong(os, javaToWinTime(atime));
                writeLong(os, javaToWinTime(ctime));
            }
            if (elenEXTT != 0) {
                writeShort(os, EXTID_EXTT);
                writeShort(os, elenEXTT - 4);
                if (ctime == -1)
                    os.write(0x3);          
                else
                    os.write(0x7);          
                writeInt(os, javaToUnixTime(mtime));
            }
            if (extra != null)              
                writeBytes(os, extra);
            if (comment != null)            
                writeBytes(os, comment);
            return CENHDR + nlen + elen + clen + elen64 + elenNTFS + elenEXTT;
        }
        static Entry readLOC(ZipFileSystem zipfs, long pos)
            throws IOException
        {
            return readLOC(zipfs, pos, new byte[1024]);
        }
        static Entry readLOC(ZipFileSystem zipfs, long pos, byte[] buf)
            throws IOException
        {
            return new Entry().loc(zipfs, pos, buf);
        }
        Entry loc(ZipFileSystem zipfs, long pos, byte[] buf)
            throws IOException
        {
            assert (buf.length >= LOCHDR);
            if (zipfs.readFullyAt(buf, 0, LOCHDR , pos) != LOCHDR)
                throw new ZipException("loc: reading failed");
            if (LOCSIG(buf) != LOCSIG)
                throw new ZipException("loc: wrong sig ->"
                                       + Long.toString(LOCSIG(buf), 16));
            version  = LOCVER(buf);
            flag     = LOCFLG(buf);
            method   = LOCHOW(buf);
            mtime    = dosToJavaTime(LOCTIM(buf));
            crc      = LOCCRC(buf);
            csize    = LOCSIZ(buf);
            size     = LOCLEN(buf);
            int nlen = LOCNAM(buf);
            int elen = LOCEXT(buf);
            name = new byte[nlen];
            if (zipfs.readFullyAt(name, 0, nlen, pos + LOCHDR) != nlen) {
                throw new ZipException("loc: name reading failed");
            }
            if (elen > 0) {
                extra = new byte[elen];
                if (zipfs.readFullyAt(extra, 0, elen, pos + LOCHDR + nlen)
                    != elen) {
                    throw new ZipException("loc: ext reading failed");
                }
            }
            pos += (LOCHDR + nlen + elen);
            if ((flag & FLAG_DATADESCR) != 0) {
                Entry e = zipfs.getEntry0(name);  
                if (e == null)
                    throw new ZipException("loc: name not found in cen");
                size = e.size;
                csize = e.csize;
                pos += (method == METHOD_STORED ? size : csize);
                if (size >= ZIP64_MINVAL || csize >= ZIP64_MINVAL)
                    pos += 24;
                else
                    pos += 16;
            } else {
                if (extra != null &&
                    (size == ZIP64_MINVAL || csize == ZIP64_MINVAL)) {
                    int off = 0;
                    while (off + 20 < elen) {    
                        int sz = SH(extra, off + 2);
                        if (SH(extra, off) == EXTID_ZIP64 && sz == 16) {
                            size = LL(extra, off + 4);
                            csize = LL(extra, off + 12);
                            break;
                        }
                        off += (sz + 4);
                    }
                }
                pos += (method == METHOD_STORED ? size : csize);
            }
            return this;
        }
        int writeLOC(OutputStream os)
            throws IOException
        {
            writeInt(os, LOCSIG);               
            int version = version();
            int nlen = (name != null) ? name.length : 0;
            int elen = (extra != null) ? extra.length : 0;
            int elen64 = 0;
            int elenEXTT = 0;
            if ((flag & FLAG_DATADESCR) != 0) {
                writeShort(os, version());      
                writeShort(os, flag);           
                writeShort(os, method);         
                writeInt(os, (int)javaToDosTime(mtime));
                writeInt(os, 0);
                writeInt(os, 0);
                writeInt(os, 0);
            } else {
                if (csize >= ZIP64_MINVAL || size >= ZIP64_MINVAL) {
                    elen64 = 20;    
                    writeShort(os, 45);         
                } else {
                    writeShort(os, version());  
                }
                writeShort(os, flag);           
                writeShort(os, method);         
                writeInt(os, (int)javaToDosTime(mtime));
                writeInt(os, crc);              
                if (elen64 != 0) {
                    writeInt(os, ZIP64_MINVAL);
                    writeInt(os, ZIP64_MINVAL);
                } else {
                    writeInt(os, csize);        
                    writeInt(os, size);         
                }
            }
            if (atime != -1 && !isWindows) {    
                if (ctime == -1)
                    elenEXTT = 13;
                else
                    elenEXTT = 17;
            }
            writeShort(os, name.length);
            writeShort(os, elen + elen64 + elenEXTT);
            writeBytes(os, name);
            if (elen64 != 0) {
                writeShort(os, EXTID_ZIP64);
                writeShort(os, 16);
                writeLong(os, size);
                writeLong(os, csize);
            }
            if (elenEXTT != 0) {
                writeShort(os, EXTID_EXTT);
                writeShort(os, elenEXTT - 4);
                if (ctime == -1)
                    os.write(0x3);           
                else
                    os.write(0x7);           
                writeInt(os, javaToUnixTime(mtime));
                writeInt(os, javaToUnixTime(atime));
                if (ctime != -1)
                    writeInt(os, javaToUnixTime(ctime));
            }
            if (extra != null) {
                writeBytes(os, extra);
            }
            return LOCHDR + name.length + elen + elen64 + elenEXTT;
        }
        int writeEXT(OutputStream os)
            throws IOException
        {
            writeInt(os, EXTSIG);           
            writeInt(os, crc);              
            if (csize >= ZIP64_MINVAL || size >= ZIP64_MINVAL) {
                writeLong(os, csize);
                writeLong(os, size);
                return 24;
            } else {
                writeInt(os, csize);        
                writeInt(os, size);         
                return 16;
            }
        }
        void readExtra(ZipFileSystem zipfs) throws IOException {
            if (extra == null)
                return;
            int elen = extra.length;
            int off = 0;
            int newOff = 0;
            while (off + 4 < elen) {
                int pos = off;
                int tag = SH(extra, pos);
                int sz = SH(extra, pos + 2);
                pos += 4;
                if (pos + sz > elen)         
                    break;
                switch (tag) {
                case EXTID_ZIP64 :
                    if (size == ZIP64_MINVAL) {
                        if (pos + 8 > elen)  
                            break;           
                        size = LL(extra, pos);
                        pos += 8;
                    }
                    if (csize == ZIP64_MINVAL) {
                        if (pos + 8 > elen)
                            break;
                        csize = LL(extra, pos);
                        pos += 8;
                    }
                    if (locoff == ZIP64_MINVAL) {
                        if (pos + 8 > elen)
                            break;
                        locoff = LL(extra, pos);
                        pos += 8;
                    }
                    break;
                case EXTID_NTFS:
                    pos += 4;    
                    if (SH(extra, pos) !=  0x0001)
                        break;
                    if (SH(extra, pos + 2) != 24)
                        break;
                    mtime  = winToJavaTime(LL(extra, pos + 4));
                    atime  = winToJavaTime(LL(extra, pos + 12));
                    ctime  = winToJavaTime(LL(extra, pos + 20));
                    break;
                case EXTID_EXTT:
                    byte[] buf = new byte[LOCHDR];
                    if (zipfs.readFullyAt(buf, 0, buf.length , locoff)
                        != buf.length)
                        throw new ZipException("loc: reading failed");
                    if (LOCSIG(buf) != LOCSIG)
                        throw new ZipException("loc: wrong sig ->"
                                           + Long.toString(LOCSIG(buf), 16));
                    int locElen = LOCEXT(buf);
                    if (locElen < 9)    
                        break;
                    int locNlen = LOCNAM(buf);
                    buf = new byte[locElen];
                    if (zipfs.readFullyAt(buf, 0, buf.length , locoff + LOCHDR + locNlen)
                        != buf.length)
                        throw new ZipException("loc extra: reading failed");
                    int locPos = 0;
                    while (locPos + 4 < buf.length) {
                        int locTag = SH(buf, locPos);
                        int locSZ  = SH(buf, locPos + 2);
                        locPos += 4;
                        if (locTag  != EXTID_EXTT) {
                            locPos += locSZ;
                             continue;
                        }
                        int flag = CH(buf, locPos++);
                        if ((flag & 0x1) != 0) {
                            mtime = unixToJavaTime(LG(buf, locPos));
                            locPos += 4;
                        }
                        if ((flag & 0x2) != 0) {
                            atime = unixToJavaTime(LG(buf, locPos));
                            locPos += 4;
                        }
                        if ((flag & 0x4) != 0) {
                            ctime = unixToJavaTime(LG(buf, locPos));
                            locPos += 4;
                        }
                        break;
                    }
                    break;
                default:    
                    System.arraycopy(extra, off, extra, newOff, sz + 4);
                    newOff += (sz + 4);
                }
                off += (sz + 4);
            }
            if (newOff != 0 && newOff != extra.length)
                extra = Arrays.copyOf(extra, newOff);
            else
                extra = null;
        }
    }
    private static class ExChannelCloser  {
        Path path;
        SeekableByteChannel ch;
        Set<InputStream> streams;
        ExChannelCloser(Path path,
                        SeekableByteChannel ch,
                        Set<InputStream> streams)
        {
            this.path = path;
            this.ch = ch;
            this.streams = streams;
        }
    }
    private IndexNode root;
    private void addToTree(IndexNode inode, HashSet<IndexNode> dirs) {
        if (dirs.contains(inode)) {
            return;
        }
        IndexNode parent;
        byte[] name = inode.name;
        byte[] pname = getParent(name);
        if (inodes.containsKey(LOOKUPKEY.as(pname))) {
            parent = inodes.get(LOOKUPKEY);
        } else {    
            parent = new IndexNode(pname, -1);
            inodes.put(parent, parent);
        }
        addToTree(parent, dirs);
        inode.sibling = parent.child;
        parent.child = inode;
        if (name[name.length -1] == '/')
            dirs.add(inode);
    }
    private void removeFromTree(IndexNode inode) {
        IndexNode parent = inodes.get(LOOKUPKEY.as(getParent(inode.name)));
        IndexNode child = parent.child;
        if (child == inode) {
            parent.child = child.sibling;
        } else {
            IndexNode last = child;
            while ((child = child.sibling) != null) {
                if (child == inode) {
                    last.sibling = child.sibling;
                    break;
                } else {
                    last = child;
                }
            }
        }
    }
    private void buildNodeTree() throws IOException {
        beginWrite();
        try {
            HashSet<IndexNode> dirs = new HashSet<>();
            IndexNode root = new IndexNode(ROOTPATH, -1);
            inodes.put(root, root);
            dirs.add(root);
            for (IndexNode node : inodes.keySet().toArray(new IndexNode[0])) {
                addToTree(node, dirs);
            }
        } finally {
            endWrite();
        }
    }
}
