final class SelectorImpl extends AbstractSelector {
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final FileDescriptor[] EMPTY_FILE_DESCRIPTORS_ARRAY
            = new FileDescriptor[0];
    private static final SelectionKeyImpl[] EMPTY_SELECTION_KEY_IMPLS_ARRAY
            = new SelectionKeyImpl[0];
    private static final int CONNECT_OR_WRITE = OP_CONNECT | OP_WRITE;
    private static final int ACCEPT_OR_READ = OP_ACCEPT | OP_READ;
    private static final int MOCK_WRITEBUF_SIZE = 1;
    private static final int MOCK_READBUF_SIZE = 8;
    private static final int NA = 0;
    private static final int READABLE = 1;
    private static final int WRITEABLE = 2;
    private static final int SELECT_BLOCK = -1;
    private static final int SELECT_NOW = 0;
    private static class KeysLock {}
    final Object keysLock = new KeysLock();
    private final Set<SelectionKeyImpl> mutableKeys = new HashSet<SelectionKeyImpl>();
    private Set<SelectionKey> unmodifiableKeys = Collections
            .<SelectionKey>unmodifiableSet(mutableKeys);
    private final Set<SelectionKey> mutableSelectedKeys = new HashSet<SelectionKey>();
    private final Set<SelectionKey> selectedKeys
            = new UnaddableSet<SelectionKey>(mutableSelectedKeys);
    private FileDescriptor[] readableFDs = EMPTY_FILE_DESCRIPTORS_ARRAY;
    private FileDescriptor[] writableFDs = EMPTY_FILE_DESCRIPTORS_ARRAY;
    private SelectionKeyImpl[] readyKeys = EMPTY_SELECTION_KEY_IMPLS_ARRAY;
    private int[] flags = EMPTY_INT_ARRAY;
    private Pipe.SinkChannel sink;
    private Pipe.SourceChannel source;
    private FileDescriptor sourcefd;
    public SelectorImpl(SelectorProvider selectorProvider) {
        super(selectorProvider);
        try {
            Pipe mockSelector = selectorProvider.openPipe();
            sink = mockSelector.sink();
            source = mockSelector.source();
            sourcefd = ((FileDescriptorHandler) source).getFD();
            source.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void implCloseSelector() throws IOException {
        wakeup();
        synchronized (this) {
            synchronized (unmodifiableKeys) {
                synchronized (selectedKeys) {
                    doCancel();
                    for (SelectionKey sk : mutableKeys) {
                        deregister((AbstractSelectionKey) sk);
                    }
                }
            }
        }
    }
    @Override
    protected SelectionKey register(AbstractSelectableChannel channel,
            int operations, Object attachment) {
        if (!provider().equals(channel.provider())) {
            throw new IllegalSelectorException();
        }
        synchronized (this) {
            synchronized (unmodifiableKeys) {
                SelectionKeyImpl selectionKey = new SelectionKeyImpl(
                        channel, operations, attachment, this);
                mutableKeys.add(selectionKey);
                return selectionKey;
            }
        }
    }
    @Override
    public synchronized Set<SelectionKey> keys() {
        closeCheck();
        return unmodifiableKeys;
    }
    private void closeCheck() {
        if (!isOpen()) {
            throw new ClosedSelectorException();
        }
    }
    @Override
    public int select() throws IOException {
        return selectInternal(SELECT_BLOCK);
    }
    @Override
    public int select(long timeout) throws IOException {
        if (timeout < 0) {
            throw new IllegalArgumentException();
        }
        return selectInternal((0 == timeout) ? SELECT_BLOCK : timeout);
    }
    @Override
    public int selectNow() throws IOException {
        return selectInternal(SELECT_NOW);
    }
    private int selectInternal(long timeout) throws IOException {
        closeCheck();
        synchronized (this) {
            synchronized (unmodifiableKeys) {
                synchronized (selectedKeys) {
                    doCancel();
                    boolean isBlock = (SELECT_NOW != timeout);
                    int readableKeysCount = 1; 
                    int writableKeysCount = 0;
                    synchronized (keysLock) {
                        for (SelectionKeyImpl key : mutableKeys) {
                            int ops = key.interestOpsNoCheck();
                            if ((ACCEPT_OR_READ & ops) != 0) {
                                readableKeysCount++;
                            }
                            if ((CONNECT_OR_WRITE & ops) != 0) {
                                writableKeysCount++;
                            }
                        }
                        prepareChannels(readableKeysCount, writableKeysCount);
                    }
                    boolean success;
                    try {
                        if (isBlock) {
                            begin();
                        }
                        success = Platform.getNetworkSystem().select(
                                readableFDs, writableFDs, readableKeysCount, writableKeysCount, timeout, flags);
                    } finally {
                        if (isBlock) {
                            end();
                        }
                    }
                    int selected = success ? processSelectResult() : 0;
                    Arrays.fill(readableFDs, null);
                    Arrays.fill(writableFDs, null);
                    Arrays.fill(readyKeys, null);
                    Arrays.fill(flags, 0);
                    selected -= doCancel();
                    return selected;
                }
            }
        }
    }
    private void prepareChannels(int numReadable, int numWritable) {
        if (readableFDs.length < numReadable) {
            int newSize = Math.max((int) (readableFDs.length * 1.5f), numReadable);
            readableFDs = new FileDescriptor[newSize];
        }
        if (writableFDs.length < numWritable) {
            int newSize = Math.max((int) (writableFDs.length * 1.5f), numWritable);
            writableFDs = new FileDescriptor[newSize];
        }
        int total = numReadable + numWritable;
        if (readyKeys.length < total) {
            int newSize = Math.max((int) (readyKeys.length * 1.5f), total);
            readyKeys = new SelectionKeyImpl[newSize];
            flags = new int[newSize];
        }
        readableFDs[0] = sourcefd;
        int r = 1;
        int w = 0;
        for (SelectionKeyImpl key : mutableKeys) {
            int interestOps = key.interestOpsNoCheck();
            if ((ACCEPT_OR_READ & interestOps) != 0) {
                readableFDs[r] = ((FileDescriptorHandler) key.channel()).getFD();
                readyKeys[r] = key;
                r++;
            }
            if ((CONNECT_OR_WRITE & interestOps) != 0) {
                writableFDs[w] = ((FileDescriptorHandler) key.channel()).getFD();
                readyKeys[w + numReadable] = key;
                w++;
            }
        }
    }
    private int processSelectResult() throws IOException {
        if (READABLE == flags[0]) {
            ByteBuffer readbuf = ByteBuffer.allocate(MOCK_READBUF_SIZE);
            while (source.read(readbuf) > 0) {
                readbuf.flip();
            }
        }
        int selected = 0;
        for (int i = 1; i < flags.length; i++) {
            if (flags[i] == NA) {
                continue;
            }
            SelectionKeyImpl key = readyKeys[i];
            int ops = key.interestOpsNoCheck();
            int selectedOp = 0;
            switch (flags[i]) {
                case READABLE:
                    selectedOp = ACCEPT_OR_READ & ops;
                    break;
                case WRITEABLE:
                    if (key.isConnected()) {
                        selectedOp = OP_WRITE & ops;
                    } else {
                        selectedOp = OP_CONNECT & ops;
                    }
                    break;
            }
            if (0 != selectedOp) {
                boolean wasSelected = mutableSelectedKeys.contains(key);
                if (wasSelected && key.readyOps() != selectedOp) {
                    key.setReadyOps(key.readyOps() | selectedOp);
                    selected++;
                } else if (!wasSelected) {
                    key.setReadyOps(selectedOp);
                    mutableSelectedKeys.add(key);
                    selected++;
                }
            }
        }
        return selected;
    }
    @Override
    public synchronized Set<SelectionKey> selectedKeys() {
        closeCheck();
        return selectedKeys;
    }
    private int doCancel() {
        int deselected = 0;
        Set<SelectionKey> cancelledKeys = cancelledKeys();
        synchronized (cancelledKeys) {
            if (cancelledKeys.size() > 0) {
                for (SelectionKey currentkey : cancelledKeys) {
                    mutableKeys.remove(currentkey);
                    deregister((AbstractSelectionKey) currentkey);
                    if (mutableSelectedKeys.remove(currentkey)) {
                        deselected++;
                    }
                }
                cancelledKeys.clear();
            }
        }
        return deselected;
    }
    @Override
    public Selector wakeup() {
        try {
            sink.write(ByteBuffer.allocate(MOCK_WRITEBUF_SIZE));
        } catch (IOException e) {
        }
        return this;
    }
    private static class UnaddableSet<E> implements Set<E> {
        private final Set<E> set;
        UnaddableSet(Set<E> set) {
            this.set = set;
        }
        @Override
        public boolean equals(Object object) {
            return set.equals(object);
        }
        @Override
        public int hashCode() {
            return set.hashCode();
        }
        public boolean add(E object) {
            throw new UnsupportedOperationException();
        }
        public boolean addAll(Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }
        public void clear() {
            set.clear();
        }
        public boolean contains(Object object) {
            return set.contains(object);
        }
        public boolean containsAll(Collection<?> c) {
            return set.containsAll(c);
        }
        public boolean isEmpty() {
            return set.isEmpty();
        }
        public Iterator<E> iterator() {
            return set.iterator();
        }
        public boolean remove(Object object) {
            return set.remove(object);
        }
        public boolean removeAll(Collection<?> c) {
            return set.removeAll(c);
        }
        public boolean retainAll(Collection<?> c) {
            return set.retainAll(c);
        }
        public int size() {
            return set.size();
        }
        public Object[] toArray() {
            return set.toArray();
        }
        public <T> T[] toArray(T[] a) {
            return set.toArray(a);
        }
    }
}
