final class LockManager {
    private final Comparator<FileLock> lockComparator = new Comparator<FileLock>() {
        public int compare(FileLock lock1, FileLock lock2) {
            long position1 = lock1.position();
            long position2 = lock2.position();
            return position1 > position2 ? 1 : (position1 < position2 ? -1 : 0);
        }
    };
    private final SortedSet<FileLock> locks = new TreeSet<FileLock>(
            lockComparator);
    protected LockManager() {
        super();
    }
    synchronized void addLock(FileLock lock)
            throws OverlappingFileLockException {
        long lockEnd = lock.position() + lock.size();
        for (Iterator<FileLock> keyItr = locks.iterator(); keyItr.hasNext();) {
            FileLock existingLock = keyItr.next();
            if (existingLock.position() > lockEnd) {
                break;
            }
            if (existingLock.overlaps(lock.position(), lock.size())) {
                throw new OverlappingFileLockException();
            }
        }
        locks.add(lock);
    }
    synchronized void removeLock(FileLock lock) {
        locks.remove(lock);
    }
}
