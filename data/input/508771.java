final class FileLockImpl extends FileLock {
    private boolean isReleased = false;
    public FileLockImpl(FileChannel channel, long position, long size,
            boolean shared) {
        super(channel, position, size, shared);
    }
    @Override
    public boolean isValid() {
        return !isReleased && channel().isOpen();
    }
    @Override
    public void release() throws IOException {
        if (!channel().isOpen()) {
            throw new ClosedChannelException();
        }
        if (!isReleased) {
            ((FileChannelImpl) channel()).release(this);
            isReleased = true;
        }
    }
}
