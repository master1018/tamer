class WindowsUserDefinedFileAttributeView
    extends AbstractUserDefinedFileAttributeView
{
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private String join(String file, String name) {
        if (name == null)
            throw new NullPointerException("'name' is null");
        return file + ":" + name;
    }
    private String join(WindowsPath file, String name) throws WindowsException {
        return join(file.getPathForWin32Calls(), name);
    }
    private final WindowsPath file;
    private final boolean followLinks;
    WindowsUserDefinedFileAttributeView(WindowsPath file, boolean followLinks) {
        this.file = file;
        this.followLinks = followLinks;
    }
    private List<String> listUsingStreamEnumeration() throws IOException {
        List<String> list = new ArrayList<>();
        try {
            FirstStream first = FindFirstStream(file.getPathForWin32Calls());
            if (first != null) {
                long handle = first.handle();
                try {
                    String name = first.name();
                    if (!name.equals("::$DATA")) {
                        String[] segs = name.split(":");
                        list.add(segs[1]);
                    }
                    while ((name = FindNextStream(handle)) != null) {
                        String[] segs = name.split(":");
                        list.add(segs[1]);
                    }
                } finally {
                    FindClose(handle);
                }
            }
        } catch (WindowsException x) {
            x.rethrowAsIOException(file);
        }
        return Collections.unmodifiableList(list);
    }
    private List<String> listUsingBackupRead() throws IOException {
        long handle = -1L;
        try {
            int flags = FILE_FLAG_BACKUP_SEMANTICS;
            if (!followLinks && file.getFileSystem().supportsLinks())
                flags |= FILE_FLAG_OPEN_REPARSE_POINT;
            handle = CreateFile(file.getPathForWin32Calls(),
                                GENERIC_READ,
                                FILE_SHARE_READ, 
                                OPEN_EXISTING,
                                flags);
        } catch (WindowsException x) {
            x.rethrowAsIOException(file);
        }
        final int BUFFER_SIZE = 4096;
        NativeBuffer buffer = null;
        final List<String> list = new ArrayList<>();
        try {
            buffer = NativeBuffers.getNativeBuffer(BUFFER_SIZE);
            long address = buffer.address();
            final int SIZEOF_STREAM_HEADER      = 20;
            final int OFFSETOF_STREAM_ID        = 0;
            final int OFFSETOF_STREAM_SIZE      = 8;
            final int OFFSETOF_STREAM_NAME_SIZE = 16;
            long context = 0L;
            try {
                for (;;) {
                    BackupResult result = BackupRead(handle, address,
                       SIZEOF_STREAM_HEADER, false, context);
                    context = result.context();
                    if (result.bytesTransferred() == 0)
                        break;
                    int streamId = unsafe.getInt(address + OFFSETOF_STREAM_ID);
                    long streamSize = unsafe.getLong(address + OFFSETOF_STREAM_SIZE);
                    int nameSize = unsafe.getInt(address + OFFSETOF_STREAM_NAME_SIZE);
                    if (nameSize > 0) {
                        result = BackupRead(handle, address, nameSize, false, context);
                        if (result.bytesTransferred() != nameSize)
                            break;
                    }
                    if (streamId == BACKUP_ALTERNATE_DATA) {
                        char[] nameAsArray = new char[nameSize/2];
                        unsafe.copyMemory(null, address, nameAsArray,
                            Unsafe.ARRAY_CHAR_BASE_OFFSET, nameSize);
                        String[] segs = new String(nameAsArray).split(":");
                        if (segs.length == 3)
                            list.add(segs[1]);
                    }
                    if (streamId == BACKUP_SPARSE_BLOCK) {
                        throw new IOException("Spare blocks not handled");
                    }
                    if (streamSize > 0L) {
                        BackupSeek(handle, streamSize, context);
                    }
                }
            } catch (WindowsException x) {
                throw new IOException(x.errorString());
            } finally {
                if (context != 0L) {
                   try {
                       BackupRead(handle, 0L, 0, true, context);
                   } catch (WindowsException ignore) { }
                }
            }
        } finally {
            if (buffer != null)
                buffer.release();
            CloseHandle(handle);
        }
        return Collections.unmodifiableList(list);
    }
    @Override
    public List<String> list() throws IOException  {
        if (System.getSecurityManager() != null)
            checkAccess(file.getPathForPermissionCheck(), true, false);
        if (file.getFileSystem().supportsStreamEnumeration()) {
            return listUsingStreamEnumeration();
        } else {
            return listUsingBackupRead();
        }
    }
    @Override
    public int size(String name) throws IOException  {
        if (System.getSecurityManager() != null)
            checkAccess(file.getPathForPermissionCheck(), true, false);
        FileChannel fc = null;
        try {
            Set<OpenOption> opts = new HashSet<>();
            opts.add(READ);
            if (!followLinks)
                opts.add(WindowsChannelFactory.OPEN_REPARSE_POINT);
            fc = WindowsChannelFactory
                .newFileChannel(join(file, name), null, opts, 0L);
        } catch (WindowsException x) {
            x.rethrowAsIOException(join(file.getPathForPermissionCheck(), name));
        }
        try {
            long size = fc.size();
            if (size > Integer.MAX_VALUE)
                throw new ArithmeticException("Stream too large");
            return (int)size;
        } finally {
            fc.close();
        }
    }
    @Override
    public int read(String name, ByteBuffer dst) throws IOException {
        if (System.getSecurityManager() != null)
            checkAccess(file.getPathForPermissionCheck(), true, false);
        FileChannel fc = null;
        try {
            Set<OpenOption> opts = new HashSet<>();
            opts.add(READ);
            if (!followLinks)
                opts.add(WindowsChannelFactory.OPEN_REPARSE_POINT);
            fc = WindowsChannelFactory
                .newFileChannel(join(file, name), null, opts, 0L);
        } catch (WindowsException x) {
            x.rethrowAsIOException(join(file.getPathForPermissionCheck(), name));
        }
        try {
            if (fc.size() > dst.remaining())
                throw new IOException("Stream too large");
            int total = 0;
            while (dst.hasRemaining()) {
                int n = fc.read(dst);
                if (n < 0)
                    break;
                total += n;
            }
            return total;
        } finally {
            fc.close();
        }
    }
    @Override
    public int write(String name, ByteBuffer src) throws IOException {
        if (System.getSecurityManager() != null)
            checkAccess(file.getPathForPermissionCheck(), false, true);
        long handle = -1L;
        try {
            int flags = FILE_FLAG_BACKUP_SEMANTICS;
            if (!followLinks)
                flags |= FILE_FLAG_OPEN_REPARSE_POINT;
            handle = CreateFile(file.getPathForWin32Calls(),
                                GENERIC_READ,
                                (FILE_SHARE_READ | FILE_SHARE_WRITE | FILE_SHARE_DELETE),
                                OPEN_EXISTING,
                                flags);
        } catch (WindowsException x) {
            x.rethrowAsIOException(file);
        }
        try {
            Set<OpenOption> opts = new HashSet<>();
            if (!followLinks)
                opts.add(WindowsChannelFactory.OPEN_REPARSE_POINT);
            opts.add(CREATE);
            opts.add(WRITE);
            opts.add(StandardOpenOption.TRUNCATE_EXISTING);
            FileChannel named = null;
            try {
                named = WindowsChannelFactory
                    .newFileChannel(join(file, name), null, opts, 0L);
            } catch (WindowsException x) {
                x.rethrowAsIOException(join(file.getPathForPermissionCheck(), name));
            }
            try {
                int rem = src.remaining();
                while (src.hasRemaining()) {
                    named.write(src);
                }
                return rem;
            } finally {
                named.close();
            }
        } finally {
            CloseHandle(handle);
        }
    }
    @Override
    public void delete(String name) throws IOException {
        if (System.getSecurityManager() != null)
            checkAccess(file.getPathForPermissionCheck(), false, true);
        String path = WindowsLinkSupport.getFinalPath(file, followLinks);
        String toDelete = join(path, name);
        try {
            DeleteFile(toDelete);
        } catch (WindowsException x) {
            x.rethrowAsIOException(toDelete);
        }
    }
}
