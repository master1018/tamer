class WindowsChannelFactory {
    private static final JavaIOFileDescriptorAccess fdAccess =
        SharedSecrets.getJavaIOFileDescriptorAccess();
    private WindowsChannelFactory() { }
    static final OpenOption OPEN_REPARSE_POINT = new OpenOption() { };
    private static class Flags {
        boolean read;
        boolean write;
        boolean append;
        boolean truncateExisting;
        boolean create;
        boolean createNew;
        boolean deleteOnClose;
        boolean sparse;
        boolean overlapped;
        boolean sync;
        boolean dsync;
        boolean shareRead = true;
        boolean shareWrite = true;
        boolean shareDelete = true;
        boolean noFollowLinks;
        boolean openReparsePoint;
        static Flags toFlags(Set<? extends OpenOption> options) {
            Flags flags = new Flags();
            for (OpenOption option: options) {
                if (option instanceof StandardOpenOption) {
                    switch ((StandardOpenOption)option) {
                        case READ : flags.read = true; break;
                        case WRITE : flags.write = true; break;
                        case APPEND : flags.append = true; break;
                        case TRUNCATE_EXISTING : flags.truncateExisting = true; break;
                        case CREATE : flags.create = true; break;
                        case CREATE_NEW : flags.createNew = true; break;
                        case DELETE_ON_CLOSE : flags.deleteOnClose = true; break;
                        case SPARSE : flags.sparse = true; break;
                        case SYNC : flags.sync = true; break;
                        case DSYNC : flags.dsync = true; break;
                        default: throw new UnsupportedOperationException();
                    }
                    continue;
                }
                if (option instanceof ExtendedOpenOption) {
                    switch ((ExtendedOpenOption)option) {
                        case NOSHARE_READ : flags.shareRead = false; break;
                        case NOSHARE_WRITE : flags.shareWrite = false; break;
                        case NOSHARE_DELETE : flags.shareDelete = false; break;
                        default: throw new UnsupportedOperationException();
                    }
                    continue;
                }
                if (option == LinkOption.NOFOLLOW_LINKS) {
                    flags.noFollowLinks = true;
                    continue;
                }
                if (option == OPEN_REPARSE_POINT) {
                    flags.openReparsePoint = true;
                    continue;
                }
                if (option == null)
                    throw new NullPointerException();
                throw new UnsupportedOperationException();
            }
            return flags;
        }
    }
    static FileChannel newFileChannel(String pathForWindows,
                                      String pathToCheck,
                                      Set<? extends OpenOption> options,
                                      long pSecurityDescriptor)
        throws WindowsException
    {
        Flags flags = Flags.toFlags(options);
        if (!flags.read && !flags.write) {
            if (flags.append) {
                flags.write = true;
            } else {
                flags.read = true;
            }
        }
        if (flags.read && flags.append)
            throw new IllegalArgumentException("READ + APPEND not allowed");
        if (flags.append && flags.truncateExisting)
            throw new IllegalArgumentException("APPEND + TRUNCATE_EXISTING not allowed");
        FileDescriptor fdObj = open(pathForWindows, pathToCheck, flags, pSecurityDescriptor);
        return FileChannelImpl.open(fdObj, flags.read, flags.write, flags.append, null);
    }
    static AsynchronousFileChannel newAsynchronousFileChannel(String pathForWindows,
                                                              String pathToCheck,
                                                              Set<? extends OpenOption> options,
                                                              long pSecurityDescriptor,
                                                              ThreadPool pool)
        throws IOException
    {
        Flags flags = Flags.toFlags(options);
        flags.overlapped = true;
        if (!flags.read && !flags.write) {
            flags.read = true;
        }
        if (flags.append)
            throw new UnsupportedOperationException("APPEND not allowed");
        FileDescriptor fdObj;
        try {
            fdObj = open(pathForWindows, pathToCheck, flags, pSecurityDescriptor);
        } catch (WindowsException x) {
            x.rethrowAsIOException(pathForWindows);
            return null;
        }
        try {
            return WindowsAsynchronousFileChannelImpl.open(fdObj, flags.read, flags.write, pool);
        } catch (IOException x) {
            long handle = fdAccess.getHandle(fdObj);
            CloseHandle(handle);
            throw x;
        }
    }
    private static FileDescriptor open(String pathForWindows,
                                       String pathToCheck,
                                       Flags flags,
                                       long pSecurityDescriptor)
        throws WindowsException
    {
        boolean truncateAfterOpen = false;
        int dwDesiredAccess = 0;
        if (flags.read)
            dwDesiredAccess |= GENERIC_READ;
        if (flags.write)
            dwDesiredAccess |= GENERIC_WRITE;
        int dwShareMode = 0;
        if (flags.shareRead)
            dwShareMode |= FILE_SHARE_READ;
        if (flags.shareWrite)
            dwShareMode |= FILE_SHARE_WRITE;
        if (flags.shareDelete)
            dwShareMode |= FILE_SHARE_DELETE;
        int dwFlagsAndAttributes = FILE_ATTRIBUTE_NORMAL;
        int dwCreationDisposition = OPEN_EXISTING;
        if (flags.write) {
            if (flags.createNew) {
                dwCreationDisposition = CREATE_NEW;
                dwFlagsAndAttributes |= FILE_FLAG_OPEN_REPARSE_POINT;
            } else {
                if (flags.create)
                    dwCreationDisposition = OPEN_ALWAYS;
                if (flags.truncateExisting) {
                    if (dwCreationDisposition == OPEN_ALWAYS) {
                        truncateAfterOpen = true;
                    } else {
                        dwCreationDisposition = TRUNCATE_EXISTING;
                    }
                }
            }
        }
        if (flags.dsync || flags.sync)
            dwFlagsAndAttributes |= FILE_FLAG_WRITE_THROUGH;
        if (flags.overlapped)
            dwFlagsAndAttributes |= FILE_FLAG_OVERLAPPED;
        if (flags.deleteOnClose)
            dwFlagsAndAttributes |= FILE_FLAG_DELETE_ON_CLOSE;
        boolean okayToFollowLinks = true;
        if (dwCreationDisposition != CREATE_NEW &&
            (flags.noFollowLinks ||
             flags.openReparsePoint ||
             flags.deleteOnClose))
        {
            if (flags.noFollowLinks || flags.deleteOnClose)
                okayToFollowLinks = false;
            dwFlagsAndAttributes |= FILE_FLAG_OPEN_REPARSE_POINT;
        }
        if (pathToCheck != null) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                if (flags.read)
                    sm.checkRead(pathToCheck);
                if (flags.write)
                    sm.checkWrite(pathToCheck);
                if (flags.deleteOnClose)
                    sm.checkDelete(pathToCheck);
            }
        }
        long handle = CreateFile(pathForWindows,
                                 dwDesiredAccess,
                                 dwShareMode,
                                 pSecurityDescriptor,
                                 dwCreationDisposition,
                                 dwFlagsAndAttributes);
        if (!okayToFollowLinks) {
            try {
                if (WindowsFileAttributes.readAttributes(handle).isSymbolicLink())
                    throw new WindowsException("File is symbolic link");
            } catch (WindowsException x) {
                CloseHandle(handle);
                throw x;
            }
        }
        if (truncateAfterOpen) {
            try {
                SetEndOfFile(handle);
            } catch (WindowsException x) {
                CloseHandle(handle);
                throw x;
            }
        }
        if (dwCreationDisposition == CREATE_NEW && flags.sparse) {
            try {
                DeviceIoControlSetSparse(handle);
            } catch (WindowsException x) {
            }
        }
        FileDescriptor fdObj = new FileDescriptor();
        fdAccess.setHandle(fdObj, handle);
        return fdObj;
    }
}
