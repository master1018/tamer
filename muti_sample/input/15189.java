public class WindowsFileSystemProvider
    extends AbstractFileSystemProvider
{
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final String USER_DIR = "user.dir";
    private final WindowsFileSystem theFileSystem;
    public WindowsFileSystemProvider() {
        theFileSystem = new WindowsFileSystem(this, System.getProperty(USER_DIR));
    }
    @Override
    public String getScheme() {
        return "file";
    }
    private void checkUri(URI uri) {
        if (!uri.getScheme().equalsIgnoreCase(getScheme()))
            throw new IllegalArgumentException("URI does not match this provider");
        if (uri.getAuthority() != null)
            throw new IllegalArgumentException("Authority component present");
        if (uri.getPath() == null)
            throw new IllegalArgumentException("Path component is undefined");
        if (!uri.getPath().equals("/"))
            throw new IllegalArgumentException("Path component should be '/'");
        if (uri.getQuery() != null)
            throw new IllegalArgumentException("Query component present");
        if (uri.getFragment() != null)
            throw new IllegalArgumentException("Fragment component present");
    }
    @Override
    public FileSystem newFileSystem(URI uri, Map<String,?> env)
        throws IOException
    {
        checkUri(uri);
        throw new FileSystemAlreadyExistsException();
    }
    @Override
    public final FileSystem getFileSystem(URI uri) {
        checkUri(uri);
        return theFileSystem;
    }
    @Override
    public Path getPath(URI uri) {
        return WindowsUriSupport.fromUri(theFileSystem, uri);
    }
    @Override
    public FileChannel newFileChannel(Path path,
                                      Set<? extends OpenOption> options,
                                      FileAttribute<?>... attrs)
        throws IOException
    {
        if (path == null)
            throw new NullPointerException();
        if (!(path instanceof WindowsPath))
            throw new ProviderMismatchException();
        WindowsPath file = (WindowsPath)path;
        WindowsSecurityDescriptor sd = WindowsSecurityDescriptor.fromAttribute(attrs);
        try {
            return WindowsChannelFactory
                .newFileChannel(file.getPathForWin32Calls(),
                                file.getPathForPermissionCheck(),
                                options,
                                sd.address());
        } catch (WindowsException x) {
            x.rethrowAsIOException(file);
            return null;
        } finally {
            if (sd != null)
                sd.release();
        }
    }
    @Override
    public AsynchronousFileChannel newAsynchronousFileChannel(Path path,
                                                              Set<? extends OpenOption> options,
                                                              ExecutorService executor,
                                                              FileAttribute<?>... attrs)
        throws IOException
    {
        if (path == null)
            throw new NullPointerException();
        if (!(path instanceof WindowsPath))
            throw new ProviderMismatchException();
        WindowsPath file = (WindowsPath)path;
        ThreadPool pool = (executor == null) ? null : ThreadPool.wrap(executor, 0);
        WindowsSecurityDescriptor sd =
            WindowsSecurityDescriptor.fromAttribute(attrs);
        try {
            return WindowsChannelFactory
                .newAsynchronousFileChannel(file.getPathForWin32Calls(),
                                            file.getPathForPermissionCheck(),
                                            options,
                                            sd.address(),
                                            pool);
        } catch (WindowsException x) {
            x.rethrowAsIOException(file);
            return null;
        } finally {
            if (sd != null)
                sd.release();
        }
    }
    @Override
    @SuppressWarnings("unchecked")
    public <V extends FileAttributeView> V
        getFileAttributeView(Path obj, Class<V> view, LinkOption... options)
    {
        WindowsPath file = WindowsPath.toWindowsPath(obj);
        if (view == null)
            throw new NullPointerException();
        boolean followLinks = Util.followLinks(options);
        if (view == BasicFileAttributeView.class)
            return (V) WindowsFileAttributeViews.createBasicView(file, followLinks);
        if (view == DosFileAttributeView.class)
            return (V) WindowsFileAttributeViews.createDosView(file, followLinks);
        if (view == AclFileAttributeView.class)
            return (V) new WindowsAclFileAttributeView(file, followLinks);
        if (view == FileOwnerAttributeView.class)
            return (V) new FileOwnerAttributeViewImpl(
                new WindowsAclFileAttributeView(file, followLinks));
        if (view == UserDefinedFileAttributeView.class)
            return (V) new WindowsUserDefinedFileAttributeView(file, followLinks);
        return (V) null;
    }
    @Override
    @SuppressWarnings("unchecked")
    public <A extends BasicFileAttributes> A readAttributes(Path file,
                                                            Class<A> type,
                                                            LinkOption... options)
        throws IOException
    {
        Class<? extends BasicFileAttributeView> view;
        if (type == BasicFileAttributes.class)
            view = BasicFileAttributeView.class;
        else if (type == DosFileAttributes.class)
            view = DosFileAttributeView.class;
        else if (type == null)
            throw new NullPointerException();
        else
            throw new UnsupportedOperationException();
        return (A) getFileAttributeView(file, view, options).readAttributes();
    }
    @Override
    public DynamicFileAttributeView getFileAttributeView(Path obj, String name, LinkOption... options) {
        WindowsPath file = WindowsPath.toWindowsPath(obj);
        boolean followLinks = Util.followLinks(options);
        if (name.equals("basic"))
            return WindowsFileAttributeViews.createBasicView(file, followLinks);
        if (name.equals("dos"))
            return WindowsFileAttributeViews.createDosView(file, followLinks);
        if (name.equals("acl"))
            return new WindowsAclFileAttributeView(file, followLinks);
        if (name.equals("owner"))
            return new FileOwnerAttributeViewImpl(
                new WindowsAclFileAttributeView(file, followLinks));
        if (name.equals("user"))
            return new WindowsUserDefinedFileAttributeView(file, followLinks);
        return null;
    }
    @Override
    public SeekableByteChannel newByteChannel(Path obj,
                                              Set<? extends OpenOption> options,
                                              FileAttribute<?>... attrs)
         throws IOException
    {
        WindowsPath file = WindowsPath.toWindowsPath(obj);
        WindowsSecurityDescriptor sd =
            WindowsSecurityDescriptor.fromAttribute(attrs);
        try {
            return WindowsChannelFactory
                .newFileChannel(file.getPathForWin32Calls(),
                                file.getPathForPermissionCheck(),
                                options,
                                sd.address());
        } catch (WindowsException x) {
            x.rethrowAsIOException(file);
            return null;  
        } finally {
            sd.release();
        }
    }
    @Override
    boolean implDelete(Path obj, boolean failIfNotExists) throws IOException {
        WindowsPath file = WindowsPath.toWindowsPath(obj);
        file.checkDelete();
        WindowsFileAttributes attrs = null;
        try {
             attrs = WindowsFileAttributes.get(file, false);
             if (attrs.isDirectory() || attrs.isDirectoryLink()) {
                RemoveDirectory(file.getPathForWin32Calls());
             } else {
                DeleteFile(file.getPathForWin32Calls());
             }
             return true;
        } catch (WindowsException x) {
            if (!failIfNotExists &&
                (x.lastError() == ERROR_FILE_NOT_FOUND ||
                 x.lastError() == ERROR_PATH_NOT_FOUND)) return false;
            if (attrs != null && attrs.isDirectory()) {
                if (x.lastError() == ERROR_DIR_NOT_EMPTY ||
                    x.lastError() == ERROR_ALREADY_EXISTS)
                {
                    throw new DirectoryNotEmptyException(
                        file.getPathForExceptionMessage());
                }
            }
            x.rethrowAsIOException(file);
            return false;
        }
    }
    @Override
    public void copy(Path source, Path target, CopyOption... options)
        throws IOException
    {
        WindowsFileCopy.copy(WindowsPath.toWindowsPath(source),
                             WindowsPath.toWindowsPath(target),
                             options);
    }
    @Override
    public void move(Path source, Path target, CopyOption... options)
        throws IOException
    {
        WindowsFileCopy.move(WindowsPath.toWindowsPath(source),
                             WindowsPath.toWindowsPath(target),
                             options);
    }
    private static NativeBuffer getUserInfo(WindowsPath file) throws IOException {
        try {
            long hToken = WindowsSecurity.processTokenWithQueryAccess;
            int size = GetTokenInformation(hToken, TokenUser, 0L, 0);
            assert size > 0;
            NativeBuffer buffer = NativeBuffers.getNativeBuffer(size);
            try {
                int newsize = GetTokenInformation(hToken, TokenUser,
                                                  buffer.address(), size);
                if (newsize != size)
                    throw new AssertionError();
                return buffer;
            } catch (WindowsException x) {
                buffer.release();
                throw x;
            }
        } catch (WindowsException x) {
            throw new IOException(x.getMessage());
        }
    }
    private static int getEffectiveAccess(WindowsPath file) throws IOException {
        String target = WindowsLinkSupport.getFinalPath(file, true);
        NativeBuffer aclBuffer = WindowsAclFileAttributeView
            .getFileSecurity(target, DACL_SECURITY_INFORMATION);
        long pAcl = GetSecurityDescriptorDacl(aclBuffer.address());
        try {
            NativeBuffer userBuffer = getUserInfo(file);
            try {
                try {
                    long pSid = unsafe.getAddress(userBuffer.address());
                    long pTrustee = BuildTrusteeWithSid(pSid);
                    try {
                        return GetEffectiveRightsFromAcl(pAcl, pTrustee);
                    } finally {
                        LocalFree(pTrustee);
                    }
                } catch (WindowsException x) {
                    throw new IOException("Unable to get effective rights from ACL: " +
                        x.getMessage());
                }
            } finally {
                userBuffer.release();
            }
        } finally {
            aclBuffer.release();
        }
    }
    @Override
    public void checkAccess(Path obj, AccessMode... modes) throws IOException {
        WindowsPath file = WindowsPath.toWindowsPath(obj);
        if (modes.length == 0) {
            file.checkRead();
            try {
                WindowsFileAttributes.get(file, true);
            } catch (WindowsException exc) {
                exc.rethrowAsIOException(file);
            }
            return;
        }
        boolean r = false;
        boolean w = false;
        boolean x = false;
        for (AccessMode mode: modes) {
            switch (mode) {
                case READ : r = true; break;
                case WRITE : w = true; break;
                case EXECUTE : x = true; break;
                default: throw new AssertionError("Should not get here");
            }
        }
        int mask = 0;
        if (r) {
            file.checkRead();
            mask |= FILE_READ_DATA;
        }
        if (w) {
            file.checkWrite();
            mask |= FILE_WRITE_DATA;
        }
        if (x) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null)
                sm.checkExec(file.getPathForPermissionCheck());
            mask |= FILE_EXECUTE;
        }
        if ((getEffectiveAccess(file) & mask) == 0)
            throw new AccessDeniedException(
                file.getPathForExceptionMessage(), null,
                "Effective permissions does not allow requested access");
        if (w) {
            try {
                WindowsFileAttributes attrs = WindowsFileAttributes.get(file, true);
                if (!attrs.isDirectory() && attrs.isReadOnly())
                    throw new AccessDeniedException(
                        file.getPathForExceptionMessage(), null,
                        "DOS readonly attribute is set");
            } catch (WindowsException exc) {
                exc.rethrowAsIOException(file);
            }
            if (WindowsFileStore.create(file).isReadOnly()) {
                throw new AccessDeniedException(
                    file.getPathForExceptionMessage(), null, "Read-only file system");
            }
            return;
        }
    }
    @Override
    public boolean isSameFile(Path obj1, Path obj2) throws IOException {
        WindowsPath file1 = WindowsPath.toWindowsPath(obj1);
        if (file1.equals(obj2))
            return true;
        if (obj2 == null)
            throw new NullPointerException();
        if (!(obj2 instanceof WindowsPath))
            return false;
        WindowsPath file2 = (WindowsPath)obj2;
        file1.checkRead();
        file2.checkRead();
        long h1 = 0L;
        try {
            h1 = file1.openForReadAttributeAccess(true);
        } catch (WindowsException x) {
            x.rethrowAsIOException(file1);
        }
        try {
            WindowsFileAttributes attrs1 = null;
            try {
                attrs1 = WindowsFileAttributes.readAttributes(h1);
            } catch (WindowsException x) {
                x.rethrowAsIOException(file1);
            }
            long h2 = 0L;
            try {
                h2 = file2.openForReadAttributeAccess(true);
            } catch (WindowsException x) {
                x.rethrowAsIOException(file2);
            }
            try {
                WindowsFileAttributes attrs2 = null;
                try {
                    attrs2 = WindowsFileAttributes.readAttributes(h2);
                } catch (WindowsException x) {
                    x.rethrowAsIOException(file2);
                }
                return WindowsFileAttributes.isSameFile(attrs1, attrs2);
            } finally {
                CloseHandle(h2);
            }
        } finally {
            CloseHandle(h1);
        }
    }
    @Override
    public boolean isHidden(Path obj) throws IOException {
        WindowsPath file = WindowsPath.toWindowsPath(obj);
        file.checkRead();
        WindowsFileAttributes attrs = null;
        try {
            attrs = WindowsFileAttributes.get(file, true);
        } catch (WindowsException x) {
            x.rethrowAsIOException(file);
        }
        if (attrs.isDirectory())
            return false;
        return attrs.isHidden();
    }
    @Override
    public FileStore getFileStore(Path obj) throws IOException {
        WindowsPath file = WindowsPath.toWindowsPath(obj);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("getFileStoreAttributes"));
            file.checkRead();
        }
        return WindowsFileStore.create(file);
    }
    @Override
    public void createDirectory(Path obj, FileAttribute<?>... attrs)
        throws IOException
    {
        WindowsPath dir = WindowsPath.toWindowsPath(obj);
        dir.checkWrite();
        WindowsSecurityDescriptor sd = WindowsSecurityDescriptor.fromAttribute(attrs);
        try {
            CreateDirectory(dir.getPathForWin32Calls(), sd.address());
        } catch (WindowsException x) {
            x.rethrowAsIOException(dir);
        } finally {
            sd.release();
        }
    }
    @Override
    public DirectoryStream<Path> newDirectoryStream(Path obj, DirectoryStream.Filter<? super Path> filter)
        throws IOException
    {
        WindowsPath dir = WindowsPath.toWindowsPath(obj);
        dir.checkRead();
        if (filter == null)
            throw new NullPointerException();
        return new WindowsDirectoryStream(dir, filter);
    }
    @Override
    public void createSymbolicLink(Path obj1, Path obj2, FileAttribute<?>... attrs)
        throws IOException
    {
        WindowsPath link = WindowsPath.toWindowsPath(obj1);
        WindowsPath target = WindowsPath.toWindowsPath(obj2);
        if (!link.getFileSystem().supportsLinks()) {
            throw new UnsupportedOperationException("Symbolic links not supported "
                + "on this operating system");
        }
        if (attrs.length > 0) {
            WindowsSecurityDescriptor.fromAttribute(attrs);  
            throw new UnsupportedOperationException("Initial file attributes" +
                "not supported when creating symbolic link");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new LinkPermission("symbolic"));
            link.checkWrite();
        }
        if (target.type() == WindowsPathType.DRIVE_RELATIVE) {
            throw new IOException("Cannot create symbolic link to working directory relative target");
        }
        WindowsPath resolvedTarget;
        if (target.type() == WindowsPathType.RELATIVE) {
            WindowsPath parent = link.getParent();
            resolvedTarget = (parent == null) ? target : parent.resolve(target);
        } else {
            resolvedTarget = link.resolve(target);
        }
        int flags = 0;
        try {
            WindowsFileAttributes wattrs = WindowsFileAttributes.get(resolvedTarget, false);
            if (wattrs.isDirectory() || wattrs.isDirectoryLink())
                flags |= SYMBOLIC_LINK_FLAG_DIRECTORY;
        } catch (WindowsException x) {
        }
        try {
            CreateSymbolicLink(link.getPathForWin32Calls(),
                               WindowsPath.addPrefixIfNeeded(target.toString()),
                               flags);
        } catch (WindowsException x) {
            if (x.lastError() == ERROR_INVALID_REPARSE_DATA) {
                x.rethrowAsIOException(link, target);
            } else {
                x.rethrowAsIOException(link);
            }
        }
    }
    @Override
    public void createLink(Path obj1, Path obj2) throws IOException {
        WindowsPath link = WindowsPath.toWindowsPath(obj1);
        WindowsPath existing = WindowsPath.toWindowsPath(obj2);
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new LinkPermission("hard"));
            link.checkWrite();
            existing.checkWrite();
        }
        try {
            CreateHardLink(link.getPathForWin32Calls(),
                           existing.getPathForWin32Calls());
        } catch (WindowsException x) {
            x.rethrowAsIOException(link, existing);
        }
    }
    @Override
    public Path readSymbolicLink(Path obj1) throws IOException {
        WindowsPath link = WindowsPath.toWindowsPath(obj1);
        WindowsFileSystem fs = link.getFileSystem();
        if (!fs.supportsLinks()) {
            throw new UnsupportedOperationException("symbolic links not supported");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            FilePermission perm = new FilePermission(link.getPathForPermissionCheck(),
                SecurityConstants.FILE_READLINK_ACTION);
            AccessController.checkPermission(perm);
        }
        String target = WindowsLinkSupport.readLink(link);
        return WindowsPath.createFromNormalizedPath(fs, target);
    }
}
