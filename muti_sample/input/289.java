class DebugGraphicsObserver implements ImageObserver {
    int lastInfo;
    synchronized boolean allBitsPresent() {
        return (lastInfo & ImageObserver.ALLBITS) != 0;
    }
    synchronized boolean imageHasProblem() {
        return ((lastInfo & ImageObserver.ERROR) != 0 ||
                (lastInfo & ImageObserver.ABORT) != 0);
    }
    public synchronized boolean imageUpdate(Image img, int infoflags,
                                            int x, int y,
                                            int width, int height) {
        lastInfo = infoflags;
        return true;
    }
}
