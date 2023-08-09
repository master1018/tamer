class WindowsWatchService
    extends AbstractWatchService
{
    private final Unsafe unsafe = Unsafe.getUnsafe();
    private final Poller poller;
    WindowsWatchService(WindowsFileSystem fs) throws IOException {
        long port = 0L;
        try {
            port = CreateIoCompletionPort(INVALID_HANDLE_VALUE, 0, 0);
        } catch (WindowsException x) {
            throw new IOException(x.getMessage());
        }
        this.poller = new Poller(fs, this, port);
        this.poller.start();
    }
    @Override
    WatchKey register(Path path,
                      WatchEvent.Kind<?>[] events,
                      WatchEvent.Modifier... modifiers)
         throws IOException
    {
        return poller.register(path, events, modifiers);
    }
    @Override
    void implClose() throws IOException {
        poller.close();
    }
    private class WindowsWatchKey extends AbstractWatchKey {
        private FileKey fileKey;
        private volatile long handle = INVALID_HANDLE_VALUE;
        private Set<? extends WatchEvent.Kind<?>> events;
        private boolean watchSubtree;
        private NativeBuffer buffer;
        private long countAddress;
        private long overlappedAddress;
        private int completionKey;
        WindowsWatchKey(Path dir,
                        AbstractWatchService watcher,
                        FileKey fileKey)
        {
            super(dir, watcher);
            this.fileKey = fileKey;
        }
        WindowsWatchKey init(long handle,
                             Set<? extends WatchEvent.Kind<?>> events,
                             boolean watchSubtree,
                             NativeBuffer buffer,
                             long countAddress,
                             long overlappedAddress,
                             int completionKey)
        {
            this.handle = handle;
            this.events = events;
            this.watchSubtree = watchSubtree;
            this.buffer = buffer;
            this.countAddress = countAddress;
            this.overlappedAddress = overlappedAddress;
            this.completionKey = completionKey;
            return this;
        }
        long handle() {
            return handle;
        }
        Set<? extends WatchEvent.Kind<?>> events() {
            return events;
        }
        void setEvents(Set<? extends WatchEvent.Kind<?>> events) {
            this.events = events;
        }
        boolean watchSubtree() {
            return watchSubtree;
        }
        NativeBuffer buffer() {
            return buffer;
        }
        long countAddress() {
            return countAddress;
        }
        long overlappedAddress() {
            return overlappedAddress;
        }
        FileKey fileKey() {
            return fileKey;
        }
        int completionKey() {
            return completionKey;
        }
        void releaseResources() {
            CloseHandle(handle);
            buffer.cleaner().clean();
        }
        void invalidate() {
            releaseResources();
            handle = INVALID_HANDLE_VALUE;
            buffer = null;
            countAddress = 0;
            overlappedAddress = 0;
        }
        @Override
        public boolean isValid() {
            return handle != INVALID_HANDLE_VALUE;
        }
        @Override
        public void cancel() {
            if (isValid()) {
                poller.cancel(this);
            }
        }
    }
    private static class FileKey {
        private final int volSerialNumber;
        private final int fileIndexHigh;
        private final int fileIndexLow;
        FileKey(int volSerialNumber, int fileIndexHigh, int fileIndexLow) {
            this.volSerialNumber = volSerialNumber;
            this.fileIndexHigh = fileIndexHigh;
            this.fileIndexLow = fileIndexLow;
        }
        @Override
        public int hashCode() {
            return volSerialNumber ^ fileIndexHigh ^ fileIndexLow;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof FileKey))
                return false;
            FileKey other = (FileKey)obj;
            if (this.volSerialNumber != other.volSerialNumber) return false;
            if (this.fileIndexHigh != other.fileIndexHigh) return false;
            if (this.fileIndexLow != other.fileIndexLow) return false;
            return true;
        }
    }
    private static final int ALL_FILE_NOTIFY_EVENTS =
        FILE_NOTIFY_CHANGE_FILE_NAME |
        FILE_NOTIFY_CHANGE_DIR_NAME |
        FILE_NOTIFY_CHANGE_ATTRIBUTES  |
        FILE_NOTIFY_CHANGE_SIZE |
        FILE_NOTIFY_CHANGE_LAST_WRITE |
        FILE_NOTIFY_CHANGE_CREATION |
        FILE_NOTIFY_CHANGE_SECURITY;
    private class Poller extends AbstractPoller {
        private static final short SIZEOF_DWORD         = 4;
        private static final short SIZEOF_OVERLAPPED    = 32; 
        private static final short OFFSETOF_NEXTENTRYOFFSET = 0;
        private static final short OFFSETOF_ACTION          = 4;
        private static final short OFFSETOF_FILENAMELENGTH  = 8;
        private static final short OFFSETOF_FILENAME        = 12;
        private static final int CHANGES_BUFFER_SIZE    = 16 * 1024;
        private final WindowsFileSystem fs;
        private final WindowsWatchService watcher;
        private final long port;
        private final Map<Integer,WindowsWatchKey> int2key;
        private final Map<FileKey,WindowsWatchKey> fk2key;
        private int lastCompletionKey;
        Poller(WindowsFileSystem fs, WindowsWatchService watcher, long port) {
            this.fs = fs;
            this.watcher = watcher;
            this.port = port;
            this.int2key = new HashMap<Integer,WindowsWatchKey>();
            this.fk2key = new HashMap<FileKey,WindowsWatchKey>();
            this.lastCompletionKey = 0;
        }
        @Override
        void wakeup() throws IOException {
            try {
                PostQueuedCompletionStatus(port, 0);
            } catch (WindowsException x) {
                throw new IOException(x.getMessage());
            }
        }
        @Override
        Object implRegister(Path obj,
                            Set<? extends WatchEvent.Kind<?>> events,
                            WatchEvent.Modifier... modifiers)
        {
            WindowsPath dir = (WindowsPath)obj;
            boolean watchSubtree = false;
            for (WatchEvent.Modifier modifier: modifiers) {
                if (modifier == ExtendedWatchEventModifier.FILE_TREE) {
                    watchSubtree = true;
                    continue;
                } else {
                    if (modifier == null)
                        return new NullPointerException();
                    if (modifier instanceof com.sun.nio.file.SensitivityWatchEventModifier)
                        continue; 
                    return new UnsupportedOperationException("Modifier not supported");
                }
            }
            long handle = -1L;
            try {
                handle = CreateFile(dir.getPathForWin32Calls(),
                                    FILE_LIST_DIRECTORY,
                                    (FILE_SHARE_READ | FILE_SHARE_WRITE | FILE_SHARE_DELETE),
                                    OPEN_EXISTING,
                                    FILE_FLAG_BACKUP_SEMANTICS | FILE_FLAG_OVERLAPPED);
            } catch (WindowsException x) {
                return x.asIOException(dir);
            }
            boolean registered = false;
            try {
                WindowsFileAttributes attrs = null;
                try {
                    attrs = WindowsFileAttributes.readAttributes(handle);
                } catch (WindowsException x) {
                    return x.asIOException(dir);
                }
                if (!attrs.isDirectory()) {
                    return new NotDirectoryException(dir.getPathForExceptionMessage());
                }
                FileKey fk = new FileKey(attrs.volSerialNumber(),
                                         attrs.fileIndexHigh(),
                                         attrs.fileIndexLow());
                WindowsWatchKey existing = fk2key.get(fk);
                if (existing != null && watchSubtree == existing.watchSubtree()) {
                    existing.setEvents(events);
                    return existing;
                }
                int completionKey = ++lastCompletionKey;
                if (completionKey == 0)
                    completionKey = ++lastCompletionKey;
                try {
                    CreateIoCompletionPort(handle, port, completionKey);
                } catch (WindowsException x) {
                    return new IOException(x.getMessage());
                }
                int size = CHANGES_BUFFER_SIZE + SIZEOF_DWORD + SIZEOF_OVERLAPPED;
                NativeBuffer buffer = NativeBuffers.getNativeBuffer(size);
                long bufferAddress = buffer.address();
                long overlappedAddress = bufferAddress + size - SIZEOF_OVERLAPPED;
                long countAddress = overlappedAddress - SIZEOF_DWORD;
                try {
                    ReadDirectoryChangesW(handle,
                                          bufferAddress,
                                          CHANGES_BUFFER_SIZE,
                                          watchSubtree,
                                          ALL_FILE_NOTIFY_EVENTS,
                                          countAddress,
                                          overlappedAddress);
                } catch (WindowsException x) {
                    buffer.release();
                    return new IOException(x.getMessage());
                }
                WindowsWatchKey watchKey;
                if (existing == null) {
                    watchKey = new WindowsWatchKey(dir, watcher, fk)
                        .init(handle, events, watchSubtree, buffer, countAddress,
                              overlappedAddress, completionKey);
                    fk2key.put(fk, watchKey);
                } else {
                    int2key.remove(existing.completionKey());
                    existing.releaseResources();
                    watchKey = existing.init(handle, events, watchSubtree, buffer,
                        countAddress, overlappedAddress, completionKey);
                }
                int2key.put(completionKey, watchKey);
                registered = true;
                return watchKey;
            } finally {
                if (!registered) CloseHandle(handle);
            }
        }
        @Override
        void implCancelKey(WatchKey obj) {
            WindowsWatchKey key = (WindowsWatchKey)obj;
            if (key.isValid()) {
                fk2key.remove(key.fileKey());
                int2key.remove(key.completionKey());
                key.invalidate();
            }
        }
        @Override
        void implCloseAll() {
            for (Map.Entry<Integer,WindowsWatchKey> entry: int2key.entrySet()) {
                entry.getValue().invalidate();
            }
            fk2key.clear();
            int2key.clear();
            CloseHandle(port);
        }
        private WatchEvent.Kind<?> translateActionToEvent(int action)
        {
            switch (action) {
                case FILE_ACTION_MODIFIED :
                    return StandardWatchEventKinds.ENTRY_MODIFY;
                case FILE_ACTION_ADDED :
                case FILE_ACTION_RENAMED_NEW_NAME :
                    return StandardWatchEventKinds.ENTRY_CREATE;
                case FILE_ACTION_REMOVED :
                case FILE_ACTION_RENAMED_OLD_NAME :
                    return StandardWatchEventKinds.ENTRY_DELETE;
                default :
                    return null;  
            }
        }
        private void processEvents(WindowsWatchKey key, int size) {
            long address = key.buffer().address();
            int nextOffset;
            do {
                int action = unsafe.getInt(address + OFFSETOF_ACTION);
                WatchEvent.Kind<?> kind = translateActionToEvent(action);
                if (key.events().contains(kind)) {
                    int nameLengthInBytes = unsafe.getInt(address + OFFSETOF_FILENAMELENGTH);
                    if ((nameLengthInBytes % 2) != 0) {
                        throw new AssertionError("FileNameLength.FileNameLength is not a multiple of 2");
                    }
                    char[] nameAsArray = new char[nameLengthInBytes/2];
                    unsafe.copyMemory(null, address + OFFSETOF_FILENAME, nameAsArray,
                        Unsafe.ARRAY_CHAR_BASE_OFFSET, nameLengthInBytes);
                    WindowsPath name = WindowsPath
                        .createFromNormalizedPath(fs, new String(nameAsArray));
                    key.signalEvent(kind, name);
                }
                nextOffset = unsafe.getInt(address + OFFSETOF_NEXTENTRYOFFSET);
                address += (long)nextOffset;
            } while (nextOffset != 0);
        }
        @Override
        public void run() {
            for (;;) {
                CompletionStatus info = null;
                try {
                    info = GetQueuedCompletionStatus(port);
                } catch (WindowsException x) {
                    x.printStackTrace();
                    return;
                }
                if (info.completionKey() == 0) {
                    boolean shutdown = processRequests();
                    if (shutdown) {
                        return;
                    }
                    continue;
                }
                WindowsWatchKey key = int2key.get(info.completionKey());
                if (key == null) {
                    continue;
                }
                if (info.error() != 0) {
                    if (info.error() == ERROR_NOTIFY_ENUM_DIR) {
                        key.signalEvent(StandardWatchEventKinds.OVERFLOW, null);
                    } else {
                        implCancelKey(key);
                        key.signal();
                    }
                    continue;
                }
                if (info.bytesTransferred() > 0) {
                    processEvents(key, info.bytesTransferred());
                } else {
                    key.signalEvent(StandardWatchEventKinds.OVERFLOW, null);
                }
                try {
                    ReadDirectoryChangesW(key.handle(),
                                          key.buffer().address(),
                                          CHANGES_BUFFER_SIZE,
                                          key.watchSubtree(),
                                          ALL_FILE_NOTIFY_EVENTS,
                                          key.countAddress(),
                                          key.overlappedAddress());
                } catch (WindowsException x) {
                    implCancelKey(key);
                    key.signal();
                }
            }
        }
    }
}
