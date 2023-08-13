class WindowsDirectoryStream
    implements DirectoryStream<Path>
{
    private final WindowsPath dir;
    private final DirectoryStream.Filter<? super Path> filter;
    private final long handle;
    private final String firstName;
    private final NativeBuffer findDataBuffer;
    private final Object closeLock = new Object();
    private boolean isOpen = true;
    private Iterator<Path> iterator;
    WindowsDirectoryStream(WindowsPath dir, DirectoryStream.Filter<? super Path> filter)
        throws IOException
    {
        this.dir = dir;
        this.filter = filter;
        try {
            String search = dir.getPathForWin32Calls();
            char last = search.charAt(search.length() -1);
            if (last == ':' || last == '\\') {
                search += "*";
            } else {
                search += "\\*";
            }
            FirstFile first = FindFirstFile(search);
            this.handle = first.handle();
            this.firstName = first.name();
            this.findDataBuffer = WindowsFileAttributes.getBufferForFindData();
        } catch (WindowsException x) {
            if (x.lastError() == ERROR_DIRECTORY) {
                throw new NotDirectoryException(dir.getPathForExceptionMessage());
            }
            x.rethrowAsIOException(dir);
            throw new AssertionError();
        }
    }
    @Override
    public void close()
        throws IOException
    {
        synchronized (closeLock) {
            if (!isOpen)
                return;
            isOpen = false;
        }
        findDataBuffer.release();
        try {
            FindClose(handle);
        } catch (WindowsException x) {
            x.rethrowAsIOException(dir);
        }
    }
    @Override
    public Iterator<Path> iterator() {
        if (!isOpen) {
            throw new IllegalStateException("Directory stream is closed");
        }
        synchronized (this) {
            if (iterator != null)
                throw new IllegalStateException("Iterator already obtained");
            iterator = new WindowsDirectoryIterator(firstName);
            return iterator;
        }
    }
    private class WindowsDirectoryIterator implements Iterator<Path> {
        private boolean atEof;
        private String first;
        private Path nextEntry;
        WindowsDirectoryIterator(String first) {
            atEof = false;
            this.first = first;
        }
        private Path acceptEntry(String s, BasicFileAttributes attrs) {
            if (s.equals(".") || s.equals(".."))
                return null;
            if (dir.needsSlashWhenResolving()) {
                StringBuilder sb = new StringBuilder(dir.toString());
                sb.append('\\');
                sb.append(s);
                s = sb.toString();
            } else {
                s = dir + s;
            }
            Path entry = WindowsPath
                .createFromNormalizedPath(dir.getFileSystem(), s, attrs);
            try {
                if (filter.accept(entry))
                    return entry;
            } catch (IOException ioe) {
                throw new DirectoryIteratorException(ioe);
            }
            return null;
        }
        private Path readNextEntry() {
            if (first != null) {
                nextEntry = acceptEntry(first, null);
                first = null;
                if (nextEntry != null)
                    return nextEntry;
            }
            for (;;) {
                String name = null;
                WindowsFileAttributes attrs;
                synchronized (closeLock) {
                    try {
                        if (isOpen) {
                            name = FindNextFile(handle, findDataBuffer.address());
                        }
                    } catch (WindowsException x) {
                        IOException ioe = x.asIOException(dir);
                        throw new DirectoryIteratorException(ioe);
                    }
                    if (name == null) {
                        atEof = true;
                        return null;
                    }
                    attrs = WindowsFileAttributes
                        .fromFindData(findDataBuffer.address());
                }
                Path entry = acceptEntry(name, attrs);
                if (entry != null)
                    return entry;
            }
        }
        @Override
        public synchronized boolean hasNext() {
            if (nextEntry == null && !atEof)
                nextEntry = readNextEntry();
            return nextEntry != null;
        }
        @Override
        public synchronized Path next() {
            Path result = null;
            if (nextEntry == null && !atEof) {
                result = readNextEntry();
            } else {
                result = nextEntry;
                nextEntry = null;
            }
            if (result == null)
                throw new NoSuchElementException();
            return result;
        }
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
