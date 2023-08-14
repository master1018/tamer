class WindowsLinkSupport {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private WindowsLinkSupport() {
    }
    static String readLink(WindowsPath path) throws IOException {
        long handle = 0L;
        try {
            handle = path.openForReadAttributeAccess(false); 
        } catch (WindowsException x) {
            x.rethrowAsIOException(path);
        }
        try {
            return readLinkImpl(handle);
        } finally {
            CloseHandle(handle);
        }
    }
    private static String getFinalPath(WindowsPath input) throws IOException {
        long h = 0;
        try {
            h = input.openForReadAttributeAccess(true);
        } catch (WindowsException x) {
            x.rethrowAsIOException(input);
        }
        try {
            return stripPrefix(GetFinalPathNameByHandle(h));
        } catch (WindowsException x) {
            if (x.lastError() != ERROR_INVALID_LEVEL)
                x.rethrowAsIOException(input);
        } finally {
            CloseHandle(h);
        }
        return null;
    }
    static String getFinalPath(WindowsPath input, boolean followLinks)
        throws IOException
    {
        WindowsFileSystem fs = input.getFileSystem();
        try {
            if (!followLinks || !fs.supportsLinks())
                return input.getPathForWin32Calls();
            if (!WindowsFileAttributes.get(input, false).isSymbolicLink()) {
                return input.getPathForWin32Calls();
            }
        } catch (WindowsException x) {
            x.rethrowAsIOException(input);
        }
        String result = getFinalPath(input);
        if (result != null)
            return result;
        WindowsPath target = input;
        int linkCount = 0;
        do {
            try {
                WindowsFileAttributes attrs =
                    WindowsFileAttributes.get(target, false);
                if (!attrs.isSymbolicLink()) {
                    return target.getPathForWin32Calls();
                }
            } catch (WindowsException x) {
                x.rethrowAsIOException(target);
            }
            WindowsPath link = WindowsPath
                .createFromNormalizedPath(fs, readLink(target));
            WindowsPath parent = target.getParent();
            if (parent == null) {
                final WindowsPath t = target;
                target = AccessController
                    .doPrivileged(new PrivilegedAction<WindowsPath>() {
                        @Override
                        public WindowsPath run() {
                            return t.toAbsolutePath();
                        }});
                parent = target.getParent();
            }
            target = parent.resolve(link);
        } while (++linkCount < 32);
        throw new FileSystemException(input.getPathForExceptionMessage(), null,
            "Too many links");
    }
    static String getRealPath(WindowsPath input, boolean resolveLinks)
        throws IOException
    {
        WindowsFileSystem fs = input.getFileSystem();
        if (resolveLinks && !fs.supportsLinks())
            resolveLinks = false;
        String path = null;
        try {
            path = input.toAbsolutePath().toString();
        } catch (IOError x) {
            throw (IOException)(x.getCause());
        }
        if (path.indexOf('.') >= 0) {
            try {
                path = GetFullPathName(path);
            } catch (WindowsException x) {
                x.rethrowAsIOException(input);
            }
        }
        StringBuilder sb = new StringBuilder(path.length());
        int start;
        char c0 = path.charAt(0);
        char c1 = path.charAt(1);
        if ((c0 <= 'z' && c0 >= 'a' || c0 <= 'Z' && c0 >= 'A') &&
            c1 == ':' && path.charAt(2) == '\\') {
            sb.append(Character.toUpperCase(c0));
            sb.append(":\\");
            start = 3;
        } else if (c0 == '\\' && c1 == '\\') {
            int last = path.length() - 1;
            int pos = path.indexOf('\\', 2);
            if (pos == -1 || (pos == last)) {
                throw new FileSystemException(input.getPathForExceptionMessage(),
                    null, "UNC has invalid share");
            }
            pos = path.indexOf('\\', pos+1);
            if (pos < 0) {
                pos = last;
                sb.append(path).append("\\");
            } else {
                sb.append(path, 0, pos+1);
            }
            start = pos + 1;
        } else {
            throw new AssertionError("path type not recognized");
        }
        if (start >= path.length()) {
            String result = sb.toString();
            try {
                GetFileAttributes(result);
            } catch (WindowsException x) {
                x.rethrowAsIOException(path);
            }
            return result;
        }
        int curr = start;
        while (curr < path.length()) {
            int next = path.indexOf('\\', curr);
            int end = (next == -1) ? path.length() : next;
            String search = sb.toString() + path.substring(curr, end);
            try {
                FirstFile fileData = FindFirstFile(addLongPathPrefixIfNeeded(search));
                FindClose(fileData.handle());
                if (resolveLinks &&
                    WindowsFileAttributes.isReparsePoint(fileData.attributes()))
                {
                    String result = getFinalPath(input);
                    if (result == null) {
                        WindowsPath resolved = resolveAllLinks(
                            WindowsPath.createFromNormalizedPath(fs, path));
                        result = getRealPath(resolved, false);
                    }
                    return result;
                }
                sb.append(fileData.name());
                if (next != -1) {
                    sb.append('\\');
                }
            } catch (WindowsException e) {
                e.rethrowAsIOException(path);
            }
            curr = end + 1;
        }
        return sb.toString();
    }
    private static String readLinkImpl(long handle) throws IOException {
        int size = MAXIMUM_REPARSE_DATA_BUFFER_SIZE;
        NativeBuffer buffer = NativeBuffers.getNativeBuffer(size);
        try {
            try {
                DeviceIoControlGetReparsePoint(handle, buffer.address(), size);
            } catch (WindowsException x) {
                if (x.lastError() == ERROR_NOT_A_REPARSE_POINT)
                    throw new NotLinkException(null, null, x.errorString());
                x.rethrowAsIOException((String)null);
            }
            final short OFFSETOF_REPARSETAG = 0;
            final short OFFSETOF_PATHOFFSET = 8;
            final short OFFSETOF_PATHLENGTH = 10;
            final short OFFSETOF_PATHBUFFER = 16 + 4;   
            int tag = (int)unsafe.getLong(buffer.address() + OFFSETOF_REPARSETAG);
            if (tag != IO_REPARSE_TAG_SYMLINK) {
                throw new NotLinkException(null, null, "Reparse point is not a symbolic link");
            }
            short nameOffset = unsafe.getShort(buffer.address() + OFFSETOF_PATHOFFSET);
            short nameLengthInBytes = unsafe.getShort(buffer.address() + OFFSETOF_PATHLENGTH);
            if ((nameLengthInBytes % 2) != 0)
                throw new FileSystemException(null, null, "Symbolic link corrupted");
            char[] name = new char[nameLengthInBytes/2];
            unsafe.copyMemory(null, buffer.address() + OFFSETOF_PATHBUFFER + nameOffset,
                name, Unsafe.ARRAY_CHAR_BASE_OFFSET, nameLengthInBytes);
            String target = stripPrefix(new String(name));
            if (target.length() == 0) {
                throw new IOException("Symbolic link target is invalid");
            }
            return target;
        } finally {
            buffer.release();
        }
    }
    private static WindowsPath resolveAllLinks(WindowsPath path)
        throws IOException
    {
        assert path.isAbsolute();
        WindowsFileSystem fs = path.getFileSystem();
        int linkCount = 0;
        int elem = 0;
        while (elem < path.getNameCount()) {
            WindowsPath current = path.getRoot().resolve(path.subpath(0, elem+1));
            WindowsFileAttributes attrs = null;
            try {
                attrs = WindowsFileAttributes.get(current, false);
            } catch (WindowsException x) {
                x.rethrowAsIOException(current);
            }
            if (attrs.isSymbolicLink()) {
                linkCount++;
                if (linkCount > 32)
                    throw new IOException("Too many links");
                WindowsPath target = WindowsPath
                    .createFromNormalizedPath(fs, readLink(current));
                WindowsPath remainder = null;
                int count = path.getNameCount();
                if ((elem+1) < count) {
                    remainder = path.subpath(elem+1, count);
                }
                path = current.getParent().resolve(target);
                try {
                    String full = GetFullPathName(path.toString());
                    if (!full.equals(path.toString())) {
                        path = WindowsPath.createFromNormalizedPath(fs, full);
                    }
                } catch (WindowsException x) {
                    x.rethrowAsIOException(path);
                }
                if (remainder != null) {
                    path = path.resolve(remainder);
                }
                elem = 0;
            } else {
                elem++;
            }
        }
        return path;
    }
    private static String addLongPathPrefixIfNeeded(String path) {
        if (path.length() > 248) {
            if (path.startsWith("\\\\")) {
                path = "\\\\?\\UNC" + path.substring(1, path.length());
            } else {
                path = "\\\\?\\" + path;
            }
        }
        return path;
    }
    private static String stripPrefix(String path) {
        if (path.startsWith("\\\\?\\")) {
            if (path.startsWith("\\\\?\\UNC\\")) {
                path = "\\" + path.substring(7);
            } else {
                path = path.substring(4);
            }
            return path;
        }
        if (path.startsWith("\\??\\")) {
            if (path.startsWith("\\??\\UNC\\")) {
                path = "\\" + path.substring(7);
            } else {
                path = path.substring(4);
            }
            return path;
        }
        return path;
    }
}
