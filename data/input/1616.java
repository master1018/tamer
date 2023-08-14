public abstract class ImageWatched {
    public static Link endlink = new Link();
    public Link watcherList;
    public ImageWatched() {
        watcherList = endlink;
    }
    public static class Link {
        public boolean isWatcher(ImageObserver iw) {
            return false;  
        }
        public Link removeWatcher(ImageObserver iw) {
            return this;  
        }
        public boolean newInfo(Image img, int info,
                               int x, int y, int w, int h)
        {
            return false;  
        }
    }
    public static class WeakLink extends Link {
        private WeakReference<ImageObserver> myref;
        private Link next;
        public WeakLink(ImageObserver obs, Link next) {
            myref = new WeakReference<ImageObserver>(obs);
            this.next = next;
        }
        public boolean isWatcher(ImageObserver iw) {
            return (myref.get() == iw || next.isWatcher(iw));
        }
        public Link removeWatcher(ImageObserver iw) {
            ImageObserver myiw = myref.get();
            if (myiw == null) {
                return next.removeWatcher(iw);
            }
            if (myiw == iw) {
                return next;
            }
            next = next.removeWatcher(iw);
            return this;
        }
        public boolean newInfo(Image img, int info,
                               int x, int y, int w, int h)
        {
            boolean ret = next.newInfo(img, info, x, y, w, h);
            ImageObserver myiw = myref.get();
            if (myiw == null) {
                ret = true;
            } else if (myiw.imageUpdate(img, info, x, y, w, h) == false) {
                myref.clear();
                ret = true;
            }
            return ret;
        }
    }
    public synchronized void addWatcher(ImageObserver iw) {
        if (iw != null && !isWatcher(iw)) {
            watcherList = new WeakLink(iw, watcherList);
        }
    }
    public synchronized boolean isWatcher(ImageObserver iw) {
        return watcherList.isWatcher(iw);
    }
    public void removeWatcher(ImageObserver iw) {
        synchronized (this) {
            watcherList = watcherList.removeWatcher(iw);
        }
        if (watcherList == endlink) {
            notifyWatcherListEmpty();
        }
    }
    public boolean isWatcherListEmpty() {
        synchronized (this) {
            watcherList = watcherList.removeWatcher(null);
        }
        return (watcherList == endlink);
    }
    public void newInfo(Image img, int info, int x, int y, int w, int h) {
        if (watcherList.newInfo(img, info, x, y, w, h)) {
            removeWatcher(null);
        }
    }
    protected abstract void notifyWatcherListEmpty();
}
