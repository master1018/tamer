class UnixChannelFactory {
    private static final JavaIOFileDescriptorAccess fdAccess =
        SharedSecrets.getJavaIOFileDescriptorAccess();
    protected UnixChannelFactory() {
    }
    protected static class Flags {
        boolean read;
        boolean write;
        boolean append;
        boolean truncateExisting;
        boolean noFollowLinks;
        boolean create;
        boolean createNew;
        boolean deleteOnClose;
        boolean sync;
        boolean dsync;
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
                        case SPARSE :  break;
                        case SYNC : flags.sync = true; break;
                        case DSYNC : flags.dsync = true; break;
                        default: throw new UnsupportedOperationException();
                    }
                    continue;
                }
                if (option == LinkOption.NOFOLLOW_LINKS) {
                    flags.noFollowLinks = true;
                    continue;
                }
                if (option == null)
                    throw new NullPointerException();
               throw new UnsupportedOperationException();
            }
            return flags;
        }
    }
    static FileChannel newFileChannel(int fd, boolean reading, boolean writing) {
        FileDescriptor fdObj = new FileDescriptor();
        fdAccess.set(fdObj, fd);
        return FileChannelImpl.open(fdObj, reading, writing, null);
    }
    static FileChannel newFileChannel(int dfd,
                                      UnixPath path,
                                      String pathForPermissionCheck,
                                      Set<? extends OpenOption> options,
                                      int mode)
        throws UnixException
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
        FileDescriptor fdObj = open(dfd, path, pathForPermissionCheck, flags, mode);
        return FileChannelImpl.open(fdObj, flags.read, flags.write, flags.append, null);
    }
    static FileChannel newFileChannel(UnixPath path,
                                      Set<? extends OpenOption> options,
                                      int mode)
        throws UnixException
    {
        return newFileChannel(-1, path, null, options, mode);
    }
    static AsynchronousFileChannel newAsynchronousFileChannel(UnixPath path,
                                                              Set<? extends OpenOption> options,
                                                              int mode,
                                                              ThreadPool pool)
        throws UnixException
    {
        Flags flags = Flags.toFlags(options);
        if (!flags.read && !flags.write) {
            flags.read = true;
        }
        if (flags.append)
            throw new UnsupportedOperationException("APPEND not allowed");
        FileDescriptor fdObj = open(-1, path, null, flags, mode);
        return SimpleAsynchronousFileChannelImpl.open(fdObj, flags.read, flags.write, pool);
    }
    protected static FileDescriptor open(int dfd,
                                         UnixPath path,
                                         String pathForPermissionCheck,
                                         Flags flags,
                                         int mode)
        throws UnixException
    {
        int oflags;
        if (flags.read && flags.write) {
            oflags = O_RDWR;
        } else {
            oflags = (flags.write) ? O_WRONLY : O_RDONLY;
        }
        if (flags.write) {
            if (flags.truncateExisting)
                oflags |= O_TRUNC;
            if (flags.append)
                oflags |= O_APPEND;
            if (flags.createNew) {
                byte[] pathForSysCall = path.asByteArray();
                if ((pathForSysCall[pathForSysCall.length-1] == '.') &&
                    (pathForSysCall.length == 1 ||
                    (pathForSysCall[pathForSysCall.length-2] == '/')))
                {
                    throw new UnixException(EEXIST);
                }
                oflags |= (O_CREAT | O_EXCL);
            } else {
                if (flags.create)
                    oflags |= O_CREAT;
            }
        }
        boolean followLinks = true;
        if (!flags.createNew && (flags.noFollowLinks || flags.deleteOnClose)) {
            followLinks = false;
            oflags |= O_NOFOLLOW;
        }
        if (flags.dsync)
            oflags |= O_DSYNC;
        if (flags.sync)
            oflags |= O_SYNC;
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            if (pathForPermissionCheck == null)
                pathForPermissionCheck = path.getPathForPermissionCheck();
            if (flags.read)
                sm.checkRead(pathForPermissionCheck);
            if (flags.write)
                sm.checkWrite(pathForPermissionCheck);
            if (flags.deleteOnClose)
                sm.checkDelete(pathForPermissionCheck);
        }
        int fd;
        try {
            if (dfd >= 0) {
                fd = openat(dfd, path.asByteArray(), oflags, mode);
            } else {
                fd = UnixNativeDispatcher.open(path, oflags, mode);
            }
        } catch (UnixException x) {
            if (flags.createNew && (x.errno() == EISDIR)) {
                x.setError(EEXIST);
            }
            if (!followLinks && (x.errno() == ELOOP)) {
                x = new UnixException(x.getMessage() + " (NOFOLLOW_LINKS specified)");
            }
            throw x;
        }
        if (flags.deleteOnClose) {
            try {
                if (dfd >= 0) {
                    unlinkat(dfd, path.asByteArray(), 0);
                } else {
                    unlink(path);
                }
            } catch (UnixException ignore) {
            }
        }
        FileDescriptor fdObj = new FileDescriptor();
        fdAccess.set(fdObj, fd);
        return fdObj;
    }
}
