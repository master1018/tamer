class WindowsFileCopy {
    private WindowsFileCopy() {
    }
    static void copy(final WindowsPath source,
                     final WindowsPath target,
                     CopyOption... options)
        throws IOException
    {
        boolean replaceExisting = false;
        boolean copyAttributes = false;
        boolean followLinks = true;
        boolean interruptible = false;
        for (CopyOption option: options) {
            if (option == StandardCopyOption.REPLACE_EXISTING) {
                replaceExisting = true;
                continue;
            }
            if (option == LinkOption.NOFOLLOW_LINKS) {
                followLinks = false;
                continue;
            }
            if (option == StandardCopyOption.COPY_ATTRIBUTES) {
                copyAttributes = true;
                continue;
            }
            if (option == ExtendedCopyOption.INTERRUPTIBLE) {
                interruptible = true;
                continue;
            }
            if (option == null)
                throw new NullPointerException();
            throw new UnsupportedOperationException("Unsupported copy option");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            source.checkRead();
            target.checkWrite();
        }
        WindowsFileAttributes sourceAttrs = null;
        WindowsFileAttributes targetAttrs = null;
        long sourceHandle = 0L;
        try {
            sourceHandle = source.openForReadAttributeAccess(followLinks);
        } catch (WindowsException x) {
            x.rethrowAsIOException(source);
        }
        try {
            try {
                sourceAttrs = WindowsFileAttributes.readAttributes(sourceHandle);
            } catch (WindowsException x) {
                x.rethrowAsIOException(source);
            }
            long targetHandle = 0L;
            try {
                targetHandle = target.openForReadAttributeAccess(false);
                try {
                    targetAttrs = WindowsFileAttributes.readAttributes(targetHandle);
                    if (WindowsFileAttributes.isSameFile(sourceAttrs, targetAttrs)) {
                        return;
                    }
                    if (!replaceExisting) {
                        throw new FileAlreadyExistsException(
                            target.getPathForExceptionMessage());
                    }
                } finally {
                    CloseHandle(targetHandle);
                }
            } catch (WindowsException x) {
            }
        } finally {
            CloseHandle(sourceHandle);
        }
        if (sm != null && sourceAttrs.isSymbolicLink()) {
            sm.checkPermission(new LinkPermission("symbolic"));
        }
        final String sourcePath = asWin32Path(source);
        final String targetPath = asWin32Path(target);
        if (targetAttrs != null) {
            try {
                if (targetAttrs.isDirectory() || targetAttrs.isDirectoryLink()) {
                    RemoveDirectory(targetPath);
                } else {
                    DeleteFile(targetPath);
                }
            } catch (WindowsException x) {
                if (targetAttrs.isDirectory()) {
                    if (x.lastError() == ERROR_DIR_NOT_EMPTY ||
                        x.lastError() == ERROR_ALREADY_EXISTS)
                    {
                        throw new DirectoryNotEmptyException(
                            target.getPathForExceptionMessage());
                    }
                }
                x.rethrowAsIOException(target);
            }
        }
        if (!sourceAttrs.isDirectory() && !sourceAttrs.isDirectoryLink()) {
            final int flags =
                (source.getFileSystem().supportsLinks() && !followLinks) ?
                COPY_FILE_COPY_SYMLINK : 0;
            if (interruptible) {
                Cancellable copyTask = new Cancellable() {
                    @Override
                    public int cancelValue() {
                        return 1;  
                    }
                    @Override
                    public void implRun() throws IOException {
                        try {
                            CopyFileEx(sourcePath, targetPath, flags,
                                       addressToPollForCancel());
                        } catch (WindowsException x) {
                            x.rethrowAsIOException(source, target);
                        }
                    }
                };
                try {
                    Cancellable.runInterruptibly(copyTask);
                } catch (ExecutionException e) {
                    Throwable t = e.getCause();
                    if (t instanceof IOException)
                        throw (IOException)t;
                    throw new IOException(t);
                }
            } else {
                try {
                    CopyFileEx(sourcePath, targetPath, flags, 0L);
                } catch (WindowsException x) {
                    x.rethrowAsIOException(source, target);
                }
            }
            if (copyAttributes) {
                try {
                    copySecurityAttributes(source, target, followLinks);
                } catch (IOException x) {
                }
            }
            return;
        }
        try {
            if (sourceAttrs.isDirectory()) {
                CreateDirectory(targetPath, 0L);
            } else {
                String linkTarget = WindowsLinkSupport.readLink(source);
                int flags = SYMBOLIC_LINK_FLAG_DIRECTORY;
                CreateSymbolicLink(targetPath,
                                   addPrefixIfNeeded(linkTarget),
                                   flags);
            }
        } catch (WindowsException x) {
            x.rethrowAsIOException(target);
        }
        if (copyAttributes) {
            WindowsFileAttributeViews.Dos view =
                WindowsFileAttributeViews.createDosView(target, false);
            try {
                view.setAttributes(sourceAttrs);
            } catch (IOException x) {
                if (sourceAttrs.isDirectory()) {
                    try {
                        RemoveDirectory(targetPath);
                    } catch (WindowsException ignore) { }
                }
            }
            try {
                copySecurityAttributes(source, target, followLinks);
            } catch (IOException ignore) { }
        }
    }
    static void move(WindowsPath source, WindowsPath target, CopyOption... options)
        throws IOException
    {
        boolean atomicMove = false;
        boolean replaceExisting = false;
        for (CopyOption option: options) {
            if (option == StandardCopyOption.ATOMIC_MOVE) {
                atomicMove = true;
                continue;
            }
            if (option == StandardCopyOption.REPLACE_EXISTING) {
                replaceExisting = true;
                continue;
            }
            if (option == LinkOption.NOFOLLOW_LINKS) {
                continue;
            }
            if (option == null) throw new NullPointerException();
            throw new UnsupportedOperationException("Unsupported copy option");
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            source.checkWrite();
            target.checkWrite();
        }
        final String sourcePath = asWin32Path(source);
        final String targetPath = asWin32Path(target);
        if (atomicMove) {
            try {
                MoveFileEx(sourcePath, targetPath, MOVEFILE_REPLACE_EXISTING);
            } catch (WindowsException x) {
                if (x.lastError() == ERROR_NOT_SAME_DEVICE) {
                    throw new AtomicMoveNotSupportedException(
                        source.getPathForExceptionMessage(),
                        target.getPathForExceptionMessage(),
                        x.errorString());
                }
                x.rethrowAsIOException(source, target);
            }
            return;
        }
        WindowsFileAttributes sourceAttrs = null;
        WindowsFileAttributes targetAttrs = null;
        long sourceHandle = 0L;
        try {
            sourceHandle = source.openForReadAttributeAccess(false);
        } catch (WindowsException x) {
            x.rethrowAsIOException(source);
        }
        try {
            try {
                sourceAttrs = WindowsFileAttributes.readAttributes(sourceHandle);
            } catch (WindowsException x) {
                x.rethrowAsIOException(source);
            }
            long targetHandle = 0L;
            try {
                targetHandle = target.openForReadAttributeAccess(false);
                try {
                    targetAttrs = WindowsFileAttributes.readAttributes(targetHandle);
                    if (WindowsFileAttributes.isSameFile(sourceAttrs, targetAttrs)) {
                        return;
                    }
                    if (!replaceExisting) {
                        throw new FileAlreadyExistsException(
                            target.getPathForExceptionMessage());
                    }
                } finally {
                    CloseHandle(targetHandle);
                }
            } catch (WindowsException x) {
            }
        } finally {
            CloseHandle(sourceHandle);
        }
        if (targetAttrs != null) {
            try {
                if (targetAttrs.isDirectory() || targetAttrs.isDirectoryLink()) {
                    RemoveDirectory(targetPath);
                } else {
                    DeleteFile(targetPath);
                }
            } catch (WindowsException x) {
                if (targetAttrs.isDirectory()) {
                    if (x.lastError() == ERROR_DIR_NOT_EMPTY ||
                        x.lastError() == ERROR_ALREADY_EXISTS)
                    {
                        throw new DirectoryNotEmptyException(
                            target.getPathForExceptionMessage());
                    }
                }
                x.rethrowAsIOException(target);
            }
        }
        try {
            MoveFileEx(sourcePath, targetPath, 0);
            return;
        } catch (WindowsException x) {
            if (x.lastError() != ERROR_NOT_SAME_DEVICE)
                x.rethrowAsIOException(source, target);
        }
        if (!sourceAttrs.isDirectory() && !sourceAttrs.isDirectoryLink()) {
            try {
                MoveFileEx(sourcePath, targetPath, MOVEFILE_COPY_ALLOWED);
            } catch (WindowsException x) {
                x.rethrowAsIOException(source, target);
            }
            try {
                copySecurityAttributes(source, target, false);
            } catch (IOException x) {
            }
            return;
        }
        assert sourceAttrs.isDirectory() || sourceAttrs.isDirectoryLink();
        try {
            if (sourceAttrs.isDirectory()) {
                CreateDirectory(targetPath, 0L);
            } else {
                String linkTarget = WindowsLinkSupport.readLink(source);
                CreateSymbolicLink(targetPath,
                                   addPrefixIfNeeded(linkTarget),
                                   SYMBOLIC_LINK_FLAG_DIRECTORY);
            }
        } catch (WindowsException x) {
            x.rethrowAsIOException(target);
        }
        WindowsFileAttributeViews.Dos view =
                WindowsFileAttributeViews.createDosView(target, false);
        try {
            view.setAttributes(sourceAttrs);
        } catch (IOException x) {
            try {
                RemoveDirectory(targetPath);
            } catch (WindowsException ignore) { }
            throw x;
        }
        try {
            copySecurityAttributes(source, target, false);
        } catch (IOException ignore) { }
        try {
            RemoveDirectory(sourcePath);
        } catch (WindowsException x) {
            try {
                RemoveDirectory(targetPath);
            } catch (WindowsException ignore) { }
            if (x.lastError() == ERROR_DIR_NOT_EMPTY ||
                x.lastError() == ERROR_ALREADY_EXISTS)
            {
                throw new DirectoryNotEmptyException(
                    target.getPathForExceptionMessage());
            }
            x.rethrowAsIOException(source);
        }
    }
    private static String asWin32Path(WindowsPath path) throws IOException {
        try {
            return path.getPathForWin32Calls();
        } catch (WindowsException x) {
            x.rethrowAsIOException(path);
            return null;
        }
    }
    private static void copySecurityAttributes(WindowsPath source,
                                               WindowsPath target,
                                               boolean followLinks)
        throws IOException
    {
        String path = WindowsLinkSupport.getFinalPath(source, followLinks);
        WindowsSecurity.Privilege priv =
            WindowsSecurity.enablePrivilege("SeRestorePrivilege");
        try {
            int request = (DACL_SECURITY_INFORMATION |
                OWNER_SECURITY_INFORMATION | GROUP_SECURITY_INFORMATION);
            NativeBuffer buffer =
                WindowsAclFileAttributeView.getFileSecurity(path, request);
            try {
                try {
                    SetFileSecurity(target.getPathForWin32Calls(), request,
                        buffer.address());
                } catch (WindowsException x) {
                    x.rethrowAsIOException(target);
                }
            } finally {
                buffer.release();
            }
        } finally {
            priv.drop();
        }
    }
    private static String addPrefixIfNeeded(String path) {
        if (path.length() > 248) {
            if (path.startsWith("\\\\")) {
                path = "\\\\?\\UNC" + path.substring(1, path.length());
            } else {
                path = "\\\\?\\" + path;
            }
        }
        return path;
    }
}
