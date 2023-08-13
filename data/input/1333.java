class UnixDirectoryStream
    implements DirectoryStream<Path>
{
    private final UnixPath dir;
    private final long dp;
    private final DirectoryStream.Filter<? super Path> filter;
    private final ReentrantReadWriteLock streamLock =
        new ReentrantReadWriteLock(true);
    private volatile boolean isClosed;
    private Iterator<Path> iterator;
    UnixDirectoryStream(UnixPath dir, long dp, DirectoryStream.Filter<? super Path> filter) {
        this.dir = dir;
        this.dp = dp;
        this.filter = filter;
    }
    protected final UnixPath directory() {
        return dir;
    }
    protected final Lock readLock() {
        return streamLock.readLock();
    }
    protected final Lock writeLock() {
        return streamLock.writeLock();
    }
    protected final boolean isOpen() {
        return !isClosed;
    }
    protected final boolean closeImpl() throws IOException {
        if (!isClosed) {
            isClosed = true;
            try {
                closedir(dp);
            } catch (UnixException x) {
                throw new IOException(x.errorString());
            }
            return true;
        } else {
            return false;
        }
    }
    @Override
    public void close()
        throws IOException
    {
        writeLock().lock();
        try {
            closeImpl();
        } finally {
            writeLock().unlock();
        }
    }
    protected final Iterator<Path> iterator(DirectoryStream<Path> ds) {
        if (isClosed) {
            throw new IllegalStateException("Directory stream is closed");
        }
        synchronized (this) {
            if (iterator != null)
                throw new IllegalStateException("Iterator already obtained");
            iterator = new UnixDirectoryIterator(ds);
            return iterator;
        }
    }
    @Override
    public Iterator<Path> iterator() {
        return iterator(this);
    }
    private class UnixDirectoryIterator implements Iterator<Path> {
        private final DirectoryStream<Path> stream;
        private boolean atEof;
        private Path nextEntry;
        UnixDirectoryIterator(DirectoryStream<Path> stream) {
            atEof = false;
            this.stream = stream;
        }
        private boolean isSelfOrParent(byte[] nameAsBytes) {
            if (nameAsBytes[0] == '.') {
                if ((nameAsBytes.length == 1) ||
                    (nameAsBytes.length == 2 && nameAsBytes[1] == '.')) {
                    return true;
                }
            }
            return false;
        }
        private Path readNextEntry() {
            assert Thread.holdsLock(this);
            for (;;) {
                byte[] nameAsBytes = null;
                readLock().lock();
                try {
                    if (isOpen()) {
                        nameAsBytes = readdir(dp);
                    }
                } catch (UnixException x) {
                    IOException ioe = x.asIOException(dir);
                    throw new DirectoryIteratorException(ioe);
                } finally {
                    readLock().unlock();
                }
                if (nameAsBytes == null) {
                    atEof = true;
                    return null;
                }
                if (!isSelfOrParent(nameAsBytes)) {
                    Path entry = dir.resolve(nameAsBytes);
                    try {
                        if (filter == null || filter.accept(entry))
                            return entry;
                    } catch (IOException ioe) {
                        throw new DirectoryIteratorException(ioe);
                    }
                }
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
            Path result;
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
