abstract class FileLockTable {
    protected FileLockTable() {
    }
    public static FileLockTable newSharedFileLockTable(Channel channel,
                                                       FileDescriptor fd)
        throws IOException
    {
        return new SharedFileLockTable(channel, fd);
    }
    public abstract void add(FileLock fl) throws OverlappingFileLockException;
    public abstract void remove(FileLock fl);
    public abstract List<FileLock> removeAll();
    public abstract void replace(FileLock fl1, FileLock fl2);
}
class SharedFileLockTable extends FileLockTable {
    private static class FileLockReference extends WeakReference<FileLock> {
        private FileKey fileKey;
        FileLockReference(FileLock referent,
                          ReferenceQueue<FileLock> queue,
                          FileKey key) {
            super(referent, queue);
            this.fileKey = key;
        }
        FileKey fileKey() {
            return fileKey;
        }
    }
    private static ConcurrentHashMap<FileKey, List<FileLockReference>> lockMap =
        new ConcurrentHashMap<FileKey, List<FileLockReference>>();
    private static ReferenceQueue<FileLock> queue = new ReferenceQueue<FileLock>();
    private final Channel channel;
    private final FileKey fileKey;
    SharedFileLockTable(Channel channel, FileDescriptor fd) throws IOException {
        this.channel = channel;
        this.fileKey = FileKey.create(fd);
    }
    @Override
    public void add(FileLock fl) throws OverlappingFileLockException {
        List<FileLockReference> list = lockMap.get(fileKey);
        for (;;) {
            if (list == null) {
                list = new ArrayList<FileLockReference>(2);
                List<FileLockReference> prev;
                synchronized (list) {
                    prev = lockMap.putIfAbsent(fileKey, list);
                    if (prev == null) {
                        list.add(new FileLockReference(fl, queue, fileKey));
                        break;
                    }
                }
                list = prev;
            }
            synchronized (list) {
                List<FileLockReference> current = lockMap.get(fileKey);
                if (list == current) {
                    checkList(list, fl.position(), fl.size());
                    list.add(new FileLockReference(fl, queue, fileKey));
                    break;
                }
                list = current;
            }
        }
        removeStaleEntries();
    }
    private void removeKeyIfEmpty(FileKey fk, List<FileLockReference> list) {
        assert Thread.holdsLock(list);
        assert lockMap.get(fk) == list;
        if (list.isEmpty()) {
            lockMap.remove(fk);
        }
    }
    @Override
    public void remove(FileLock fl) {
        assert fl != null;
        List<FileLockReference> list = lockMap.get(fileKey);
        if (list == null) return;
        synchronized (list) {
            int index = 0;
            while (index < list.size()) {
                FileLockReference ref = list.get(index);
                FileLock lock = ref.get();
                if (lock == fl) {
                    assert (lock != null) && (lock.acquiredBy() == channel);
                    ref.clear();
                    list.remove(index);
                    break;
                }
                index++;
            }
        }
    }
    @Override
    public List<FileLock> removeAll() {
        List<FileLock> result = new ArrayList<FileLock>();
        List<FileLockReference> list = lockMap.get(fileKey);
        if (list != null) {
            synchronized (list) {
                int index = 0;
                while (index < list.size()) {
                    FileLockReference ref = list.get(index);
                    FileLock lock = ref.get();
                    if (lock != null && lock.acquiredBy() == channel) {
                        ref.clear();
                        list.remove(index);
                        result.add(lock);
                    } else {
                        index++;
                    }
                }
                removeKeyIfEmpty(fileKey, list);
            }
        }
        return result;
    }
    @Override
    public void replace(FileLock fromLock, FileLock toLock) {
        List<FileLockReference> list = lockMap.get(fileKey);
        assert list != null;
        synchronized (list) {
            for (int index=0; index<list.size(); index++) {
                FileLockReference ref = list.get(index);
                FileLock lock = ref.get();
                if (lock == fromLock) {
                    ref.clear();
                    list.set(index, new FileLockReference(toLock, queue, fileKey));
                    break;
                }
            }
        }
    }
    private void checkList(List<FileLockReference> list, long position, long size)
        throws OverlappingFileLockException
    {
        assert Thread.holdsLock(list);
        for (FileLockReference ref: list) {
            FileLock fl = ref.get();
            if (fl != null && fl.overlaps(position, size))
                throw new OverlappingFileLockException();
        }
    }
    private void removeStaleEntries() {
        FileLockReference ref;
        while ((ref = (FileLockReference)queue.poll()) != null) {
            FileKey fk = ref.fileKey();
            List<FileLockReference> list = lockMap.get(fk);
            if (list != null) {
                synchronized (list) {
                    list.remove(ref);
                    removeKeyIfEmpty(fk, list);
                }
            }
        }
    }
}
