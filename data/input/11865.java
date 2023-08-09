class SolarisWatchService
    extends AbstractWatchService
{
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static int addressSize = unsafe.addressSize();
    private static int dependsArch(int value32, int value64) {
        return (addressSize == 4) ? value32 : value64;
    }
    private static final int SIZEOF_PORT_EVENT  = dependsArch(16, 24);
    private static final int OFFSETOF_EVENTS    = 0;
    private static final int OFFSETOF_SOURCE    = 4;
    private static final int OFFSETOF_OBJECT    = 8;
    private static final int SIZEOF_FILEOBJ    = dependsArch(40, 80);
    private static final int OFFSET_FO_NAME    = dependsArch(36, 72);
    private static final short PORT_SOURCE_USER     = 3;
    private static final short PORT_SOURCE_FILE     = 7;
    private static final int FILE_MODIFIED      = 0x00000002;
    private static final int FILE_ATTRIB        = 0x00000004;
    private static final int FILE_NOFOLLOW      = 0x10000000;
    private static final int FILE_DELETE        = 0x00000010;
    private static final int FILE_RENAME_TO     = 0x00000020;
    private static final int FILE_RENAME_FROM   = 0x00000040;
    private static final int UNMOUNTED          = 0x20000000;
    private static final int MOUNTEDOVER        = 0x40000000;
    private final Poller poller;
    SolarisWatchService(UnixFileSystem fs) throws IOException {
        int port = -1;
        try {
            port = portCreate();
        } catch (UnixException x) {
            throw new IOException(x.errorString());
        }
        this.poller = new Poller(fs, this, port);
        this.poller.start();
    }
    @Override
    WatchKey register(Path dir,
                      WatchEvent.Kind<?>[] events,
                      WatchEvent.Modifier... modifiers)
         throws IOException
    {
        return poller.register(dir, events, modifiers);
    }
    @Override
    void implClose() throws IOException {
        poller.close();
    }
    private class SolarisWatchKey extends AbstractWatchKey
        implements DirectoryNode
    {
        private final UnixFileKey fileKey;
        private final long object;
        private volatile Set<? extends WatchEvent.Kind<?>> events;
        private Map<Path,EntryNode> children;
        SolarisWatchKey(SolarisWatchService watcher,
                        UnixPath dir,
                        UnixFileKey fileKey,
                        long object,
                        Set<? extends WatchEvent.Kind<?>> events)
        {
            super(dir, watcher);
            this.fileKey = fileKey;
            this.object = object;
            this.events = events;
        }
        UnixPath getDirectory() {
            return (UnixPath)watchable();
        }
        UnixFileKey getFileKey() {
            return fileKey;
        }
        @Override
        public long object() {
            return object;
        }
        void invalidate() {
            events = null;
        }
        Set<? extends WatchEvent.Kind<?>> events() {
            return events;
        }
        void setEvents(Set<? extends WatchEvent.Kind<?>> events) {
            this.events = events;
        }
        @Override
        public boolean isValid() {
            return events != null;
        }
        @Override
        public void cancel() {
            if (isValid()) {
                poller.cancel(this);
            }
        }
        @Override
        public void addChild(Path name, EntryNode node) {
            if (children == null)
                children = new HashMap<Path,EntryNode>();
            children.put(name, node);
        }
        @Override
        public void removeChild(Path name) {
            children.remove(name);
        }
        @Override
        public EntryNode getChild(Path name) {
            if (children != null)
                return children.get(name);
            return null;
        }
    }
    private class Poller extends AbstractPoller {
        private static final int MAX_EVENT_COUNT            = 128;
        private static final int FILE_REMOVED =
            (FILE_DELETE|FILE_RENAME_TO|FILE_RENAME_FROM);
        private static final int FILE_EXCEPTION =
            (FILE_REMOVED|UNMOUNTED|MOUNTEDOVER);
        private final long bufferAddress;
        private final SolarisWatchService watcher;
        private final int port;
        private final Map<UnixFileKey,SolarisWatchKey> fileKey2WatchKey;
        private final Map<Long,Node> object2Node;
        Poller(UnixFileSystem fs, SolarisWatchService watcher, int port) {
            this.watcher = watcher;
            this.port = port;
            this.bufferAddress =
                unsafe.allocateMemory(SIZEOF_PORT_EVENT * MAX_EVENT_COUNT);
            this.fileKey2WatchKey = new HashMap<UnixFileKey,SolarisWatchKey>();
            this.object2Node = new HashMap<Long,Node>();
        }
        @Override
        void wakeup() throws IOException {
            try {
                portSend(port, 0);
            } catch (UnixException x) {
                throw new IOException(x.errorString());
            }
        }
        @Override
        Object implRegister(Path obj,
                            Set<? extends WatchEvent.Kind<?>> events,
                            WatchEvent.Modifier... modifiers)
        {
            if (modifiers.length > 0) {
                for (WatchEvent.Modifier modifier: modifiers) {
                    if (modifier == null)
                        return new NullPointerException();
                    if (modifier instanceof com.sun.nio.file.SensitivityWatchEventModifier)
                        continue; 
                    return new UnsupportedOperationException("Modifier not supported");
                }
            }
            UnixPath dir = (UnixPath)obj;
            UnixFileAttributes attrs = null;
            try {
                attrs = UnixFileAttributes.get(dir, true);
            } catch (UnixException x) {
                return x.asIOException(dir);
            }
            if (!attrs.isDirectory()) {
                return new NotDirectoryException(dir.getPathForExecptionMessage());
            }
            UnixFileKey fileKey = attrs.fileKey();
            SolarisWatchKey watchKey = fileKey2WatchKey.get(fileKey);
            if (watchKey != null) {
                updateEvents(watchKey, events);
                return watchKey;
            }
            long object = 0L;
            try {
                object = registerImpl(dir, (FILE_MODIFIED | FILE_ATTRIB));
            } catch (UnixException x) {
                return x.asIOException(dir);
            }
            watchKey = new SolarisWatchKey(watcher, dir, fileKey, object, events);
            object2Node.put(object, watchKey);
            fileKey2WatchKey.put(fileKey, watchKey);
            registerChildren(dir, watchKey, false);
            return watchKey;
        }
        @Override
        void implCancelKey(WatchKey obj) {
           SolarisWatchKey key = (SolarisWatchKey)obj;
           if (key.isValid()) {
               fileKey2WatchKey.remove(key.getFileKey());
               if (key.children != null) {
                   for (Map.Entry<Path,EntryNode> entry: key.children.entrySet()) {
                       EntryNode node = entry.getValue();
                       long object = node.object();
                       object2Node.remove(object);
                       releaseObject(object, true);
                   }
               }
               long object = key.object();
               object2Node.remove(object);
               releaseObject(object, true);
               key.invalidate();
           }
        }
        @Override
        void implCloseAll() {
            for (Long object: object2Node.keySet()) {
                releaseObject(object, true);
            }
            for (Map.Entry<UnixFileKey,SolarisWatchKey> entry: fileKey2WatchKey.entrySet()) {
                entry.getValue().invalidate();
            }
            object2Node.clear();
            fileKey2WatchKey.clear();
            unsafe.freeMemory(bufferAddress);
            UnixNativeDispatcher.close(port);
        }
        @Override
        public void run() {
            try {
                for (;;) {
                    int n = portGetn(port, bufferAddress, MAX_EVENT_COUNT);
                    assert n > 0;
                    long address = bufferAddress;
                    for (int i=0; i<n; i++) {
                        boolean shutdown = processEvent(address);
                        if (shutdown)
                            return;
                        address += SIZEOF_PORT_EVENT;
                    }
                }
            } catch (UnixException x) {
                x.printStackTrace();
            }
        }
        boolean processEvent(long address) {
            short source = unsafe.getShort(address + OFFSETOF_SOURCE);
            long object = unsafe.getAddress(address + OFFSETOF_OBJECT);
            int events = unsafe.getInt(address + OFFSETOF_EVENTS);
            if (source != PORT_SOURCE_FILE) {
                if (source == PORT_SOURCE_USER) {
                    boolean shutdown = processRequests();
                    if (shutdown)
                        return true;
                }
                return false;
            }
            Node node = object2Node.get(object);
            if (node == null) {
                return false;
            }
            boolean reregister = true;
            boolean isDirectory = (node instanceof SolarisWatchKey);
            if (isDirectory) {
                processDirectoryEvents((SolarisWatchKey)node, events);
            } else {
                boolean ignore = processEntryEvents((EntryNode)node, events);
                if (ignore)
                    reregister = false;
            }
            if (reregister) {
                try {
                    events = FILE_MODIFIED | FILE_ATTRIB;
                    if (!isDirectory) events |= FILE_NOFOLLOW;
                    portAssociate(port,
                                  PORT_SOURCE_FILE,
                                  object,
                                  events);
                } catch (UnixException x) {
                    reregister = false;
                }
            }
            if (!reregister) {
                object2Node.remove(object);
                releaseObject(object, false);
                if (isDirectory) {
                    SolarisWatchKey key = (SolarisWatchKey)node;
                    fileKey2WatchKey.remove( key.getFileKey() );
                    key.invalidate();
                    key.signal();
                } else {
                    EntryNode entry = (EntryNode)node;
                    SolarisWatchKey key = (SolarisWatchKey)entry.parent();
                    key.removeChild(entry.name());
                }
            }
            return false;
        }
        void processDirectoryEvents(SolarisWatchKey key, int mask) {
            if ((mask & (FILE_MODIFIED | FILE_ATTRIB)) != 0) {
                registerChildren(key.getDirectory(), key,
                    key.events().contains(StandardWatchEventKinds.ENTRY_CREATE));
            }
        }
        boolean processEntryEvents(EntryNode node, int mask) {
            SolarisWatchKey key = (SolarisWatchKey)node.parent();
            Set<? extends WatchEvent.Kind<?>> events = key.events();
            if (events == null) {
                return true;
            }
            if (((mask & (FILE_MODIFIED | FILE_ATTRIB)) != 0) &&
                events.contains(StandardWatchEventKinds.ENTRY_MODIFY))
            {
                key.signalEvent(StandardWatchEventKinds.ENTRY_MODIFY, node.name());
            }
            if (((mask & (FILE_REMOVED)) != 0) &&
                events.contains(StandardWatchEventKinds.ENTRY_DELETE))
            {
                boolean removed = true;
                try {
                    UnixFileAttributes
                        .get(key.getDirectory().resolve(node.name()), false);
                    removed = false;
                } catch (UnixException x) { }
                if (removed)
                    key.signalEvent(StandardWatchEventKinds.ENTRY_DELETE, node.name());
            }
            return false;
        }
        void registerChildren(UnixPath dir,
                              SolarisWatchKey parent,
                              boolean sendEvents)
        {
            int events = FILE_NOFOLLOW;
            if (parent.events().contains(StandardWatchEventKinds.ENTRY_MODIFY))
                events |= (FILE_MODIFIED | FILE_ATTRIB);
            DirectoryStream<Path> stream = null;
            try {
                stream = Files.newDirectoryStream(dir);
            } catch (IOException x) {
                return;
            }
            try {
                for (Path entry: stream) {
                    Path name = entry.getFileName();
                    if (parent.getChild(name) != null)
                        continue;
                    if (sendEvents) {
                        parent.signalEvent(StandardWatchEventKinds.ENTRY_CREATE, name);
                    }
                    long object = 0L;
                    try {
                        object = registerImpl((UnixPath)entry, events);
                    } catch (UnixException x) {
                        continue;
                    }
                    EntryNode node = new EntryNode(object, entry.getFileName(), parent);
                    parent.addChild(entry.getFileName(), node);
                    object2Node.put(object, node);
                }
            } catch (ConcurrentModificationException x) {
            } finally {
                try {
                    stream.close();
                } catch (IOException x) { }
            }
        }
        void updateEvents(SolarisWatchKey key, Set<? extends WatchEvent.Kind<?>> events) {
            boolean wasModifyEnabled = key.events()
                .contains(StandardWatchEventKinds.ENTRY_MODIFY);
            key.setEvents(events);
            boolean isModifyEnabled = events
                .contains(StandardWatchEventKinds.ENTRY_MODIFY);
            if (wasModifyEnabled == isModifyEnabled) {
                return;
            }
            if (key.children != null) {
                int ev = FILE_NOFOLLOW;
                if (isModifyEnabled)
                    ev |= (FILE_MODIFIED | FILE_ATTRIB);
                for (Map.Entry<Path,EntryNode> entry: key.children.entrySet()) {
                    long object = entry.getValue().object();
                    try {
                        portAssociate(port,
                                      PORT_SOURCE_FILE,
                                      object,
                                      ev);
                    } catch (UnixException x) {
                    }
                }
            }
        }
        long registerImpl(UnixPath dir, int events)
            throws UnixException
        {
            byte[] path = dir.getByteArrayForSysCalls();
            int len = path.length;
            long name = unsafe.allocateMemory(len+1);
            unsafe.copyMemory(path, Unsafe.ARRAY_BYTE_BASE_OFFSET, null,
                name, (long)len);
            unsafe.putByte(name + len, (byte)0);
            long object = unsafe.allocateMemory(SIZEOF_FILEOBJ);
            unsafe.setMemory(null, object, SIZEOF_FILEOBJ, (byte)0);
            unsafe.putAddress(object + OFFSET_FO_NAME, name);
            try {
                portAssociate(port,
                              PORT_SOURCE_FILE,
                              object,
                              events);
            } catch (UnixException x) {
                if (x.errno() == EAGAIN) {
                    System.err.println("The maximum number of objects associated "+
                        "with the port has been reached");
                }
                unsafe.freeMemory(name);
                unsafe.freeMemory(object);
                throw x;
            }
            return object;
        }
        void releaseObject(long object, boolean dissociate) {
            if (dissociate) {
                try {
                    portDissociate(port, PORT_SOURCE_FILE, object);
                } catch (UnixException x) {
                }
            }
            long name = unsafe.getAddress(object + OFFSET_FO_NAME);
            unsafe.freeMemory(name);
            unsafe.freeMemory(object);
        }
    }
    private static interface Node {
        long object();
    }
    private static interface DirectoryNode extends Node {
        void addChild(Path name, EntryNode node);
        void removeChild(Path name);
        EntryNode getChild(Path name);
    }
    private static class EntryNode implements Node {
        private final long object;
        private final Path name;
        private final DirectoryNode parent;
        EntryNode(long object, Path name, DirectoryNode parent) {
            this.object = object;
            this.name = name;
            this.parent = parent;
        }
        @Override
        public long object() {
            return object;
        }
        Path name() {
            return name;
        }
        DirectoryNode parent() {
            return parent;
        }
    }
    private static native void init();
    private static native int portCreate() throws UnixException;
    private static native void portAssociate(int port, int source, long object, int events)
        throws UnixException;
    private static native void portDissociate(int port, int source, long object)
        throws UnixException;
    private static native void portSend(int port, int events)
        throws UnixException;
    private static native int portGetn(int port, long address, int max)
        throws UnixException;
    static {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                System.loadLibrary("nio");
                return null;
        }});
        init();
    }
}
