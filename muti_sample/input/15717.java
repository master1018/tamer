public class MediaTracker implements java.io.Serializable {
    Component target;
    MediaEntry head;
    private static final long serialVersionUID = -483174189758638095L;
    public MediaTracker(Component comp) {
        target = comp;
    }
    public void addImage(Image image, int id) {
        addImage(image, id, -1, -1);
    }
    public synchronized void addImage(Image image, int id, int w, int h) {
        head = MediaEntry.insert(head,
                                 new ImageMediaEntry(this, image, id, w, h));
    }
    public static final int LOADING = 1;
    public static final int ABORTED = 2;
    public static final int ERRORED = 4;
    public static final int COMPLETE = 8;
    static final int DONE = (ABORTED | ERRORED | COMPLETE);
    public boolean checkAll() {
        return checkAll(false, true);
    }
    public boolean checkAll(boolean load) {
        return checkAll(load, true);
    }
    private synchronized boolean checkAll(boolean load, boolean verify) {
        MediaEntry cur = head;
        boolean done = true;
        while (cur != null) {
            if ((cur.getStatus(load, verify) & DONE) == 0) {
                done = false;
            }
            cur = cur.next;
        }
        return done;
    }
    public synchronized boolean isErrorAny() {
        MediaEntry cur = head;
        while (cur != null) {
            if ((cur.getStatus(false, true) & ERRORED) != 0) {
                return true;
            }
            cur = cur.next;
        }
        return false;
    }
    public synchronized Object[] getErrorsAny() {
        MediaEntry cur = head;
        int numerrors = 0;
        while (cur != null) {
            if ((cur.getStatus(false, true) & ERRORED) != 0) {
                numerrors++;
            }
            cur = cur.next;
        }
        if (numerrors == 0) {
            return null;
        }
        Object errors[] = new Object[numerrors];
        cur = head;
        numerrors = 0;
        while (cur != null) {
            if ((cur.getStatus(false, false) & ERRORED) != 0) {
                errors[numerrors++] = cur.getMedia();
            }
            cur = cur.next;
        }
        return errors;
    }
    public void waitForAll() throws InterruptedException {
        waitForAll(0);
    }
    public synchronized boolean waitForAll(long ms)
        throws InterruptedException
    {
        long end = System.currentTimeMillis() + ms;
        boolean first = true;
        while (true) {
            int status = statusAll(first, first);
            if ((status & LOADING) == 0) {
                return (status == COMPLETE);
            }
            first = false;
            long timeout;
            if (ms == 0) {
                timeout = 0;
            } else {
                timeout = end - System.currentTimeMillis();
                if (timeout <= 0) {
                    return false;
                }
            }
            wait(timeout);
        }
    }
    public int statusAll(boolean load) {
        return statusAll(load, true);
    }
    private synchronized int statusAll(boolean load, boolean verify) {
        MediaEntry cur = head;
        int status = 0;
        while (cur != null) {
            status = status | cur.getStatus(load, verify);
            cur = cur.next;
        }
        return status;
    }
    public boolean checkID(int id) {
        return checkID(id, false, true);
    }
    public boolean checkID(int id, boolean load) {
        return checkID(id, load, true);
    }
    private synchronized boolean checkID(int id, boolean load, boolean verify)
    {
        MediaEntry cur = head;
        boolean done = true;
        while (cur != null) {
            if (cur.getID() == id
                && (cur.getStatus(load, verify) & DONE) == 0)
            {
                done = false;
            }
            cur = cur.next;
        }
        return done;
    }
    public synchronized boolean isErrorID(int id) {
        MediaEntry cur = head;
        while (cur != null) {
            if (cur.getID() == id
                && (cur.getStatus(false, true) & ERRORED) != 0)
            {
                return true;
            }
            cur = cur.next;
        }
        return false;
    }
    public synchronized Object[] getErrorsID(int id) {
        MediaEntry cur = head;
        int numerrors = 0;
        while (cur != null) {
            if (cur.getID() == id
                && (cur.getStatus(false, true) & ERRORED) != 0)
            {
                numerrors++;
            }
            cur = cur.next;
        }
        if (numerrors == 0) {
            return null;
        }
        Object errors[] = new Object[numerrors];
        cur = head;
        numerrors = 0;
        while (cur != null) {
            if (cur.getID() == id
                && (cur.getStatus(false, false) & ERRORED) != 0)
            {
                errors[numerrors++] = cur.getMedia();
            }
            cur = cur.next;
        }
        return errors;
    }
    public void waitForID(int id) throws InterruptedException {
        waitForID(id, 0);
    }
    public synchronized boolean waitForID(int id, long ms)
        throws InterruptedException
    {
        long end = System.currentTimeMillis() + ms;
        boolean first = true;
        while (true) {
            int status = statusID(id, first, first);
            if ((status & LOADING) == 0) {
                return (status == COMPLETE);
            }
            first = false;
            long timeout;
            if (ms == 0) {
                timeout = 0;
            } else {
                timeout = end - System.currentTimeMillis();
                if (timeout <= 0) {
                    return false;
                }
            }
            wait(timeout);
        }
    }
    public int statusID(int id, boolean load) {
        return statusID(id, load, true);
    }
    private synchronized int statusID(int id, boolean load, boolean verify) {
        MediaEntry cur = head;
        int status = 0;
        while (cur != null) {
            if (cur.getID() == id) {
                status = status | cur.getStatus(load, verify);
            }
            cur = cur.next;
        }
        return status;
    }
    public synchronized void removeImage(Image image) {
        MediaEntry cur = head;
        MediaEntry prev = null;
        while (cur != null) {
            MediaEntry next = cur.next;
            if (cur.getMedia() == image) {
                if (prev == null) {
                    head = next;
                } else {
                    prev.next = next;
                }
                cur.cancel();
            } else {
                prev = cur;
            }
            cur = next;
        }
        notifyAll();    
    }
    public synchronized void removeImage(Image image, int id) {
        MediaEntry cur = head;
        MediaEntry prev = null;
        while (cur != null) {
            MediaEntry next = cur.next;
            if (cur.getID() == id && cur.getMedia() == image) {
                if (prev == null) {
                    head = next;
                } else {
                    prev.next = next;
                }
                cur.cancel();
            } else {
                prev = cur;
            }
            cur = next;
        }
        notifyAll();    
    }
    public synchronized void removeImage(Image image, int id,
                                         int width, int height) {
        MediaEntry cur = head;
        MediaEntry prev = null;
        while (cur != null) {
            MediaEntry next = cur.next;
            if (cur.getID() == id && cur instanceof ImageMediaEntry
                && ((ImageMediaEntry) cur).matches(image, width, height))
            {
                if (prev == null) {
                    head = next;
                } else {
                    prev.next = next;
                }
                cur.cancel();
            } else {
                prev = cur;
            }
            cur = next;
        }
        notifyAll();    
    }
    synchronized void setDone() {
        notifyAll();
    }
}
abstract class MediaEntry {
    MediaTracker tracker;
    int ID;
    MediaEntry next;
    int status;
    boolean cancelled;
    MediaEntry(MediaTracker mt, int id) {
        tracker = mt;
        ID = id;
    }
    abstract Object getMedia();
    static MediaEntry insert(MediaEntry head, MediaEntry me) {
        MediaEntry cur = head;
        MediaEntry prev = null;
        while (cur != null) {
            if (cur.ID > me.ID) {
                break;
            }
            prev = cur;
            cur = cur.next;
        }
        me.next = cur;
        if (prev == null) {
            head = me;
        } else {
            prev.next = me;
        }
        return head;
    }
    int getID() {
        return ID;
    }
    abstract void startLoad();
    void cancel() {
        cancelled = true;
    }
    static final int LOADING = MediaTracker.LOADING;
    static final int ABORTED = MediaTracker.ABORTED;
    static final int ERRORED = MediaTracker.ERRORED;
    static final int COMPLETE = MediaTracker.COMPLETE;
    static final int LOADSTARTED = (LOADING | ERRORED | COMPLETE);
    static final int DONE = (ABORTED | ERRORED | COMPLETE);
    synchronized int getStatus(boolean doLoad, boolean doVerify) {
        if (doLoad && ((status & LOADSTARTED) == 0)) {
            status = (status & ~ABORTED) | LOADING;
            startLoad();
        }
        return status;
    }
    void setStatus(int flag) {
        synchronized (this) {
            status = flag;
        }
        tracker.setDone();
    }
}
class ImageMediaEntry extends MediaEntry implements ImageObserver,
java.io.Serializable {
    Image image;
    int width;
    int height;
    private static final long serialVersionUID = 4739377000350280650L;
    ImageMediaEntry(MediaTracker mt, Image img, int c, int w, int h) {
        super(mt, c);
        image = img;
        width = w;
        height = h;
    }
    boolean matches(Image img, int w, int h) {
        return (image == img && width == w && height == h);
    }
    Object getMedia() {
        return image;
    }
    synchronized int getStatus(boolean doLoad, boolean doVerify) {
        if (doVerify) {
            int flags = tracker.target.checkImage(image, width, height, null);
            int s = parseflags(flags);
            if (s == 0) {
                if ((status & (ERRORED | COMPLETE)) != 0) {
                    setStatus(ABORTED);
                }
            } else if (s != status) {
                setStatus(s);
            }
        }
        return super.getStatus(doLoad, doVerify);
    }
    void startLoad() {
        if (tracker.target.prepareImage(image, width, height, this)) {
            setStatus(COMPLETE);
        }
    }
    int parseflags(int infoflags) {
        if ((infoflags & ERROR) != 0) {
            return ERRORED;
        } else if ((infoflags & ABORT) != 0) {
            return ABORTED;
        } else if ((infoflags & (ALLBITS | FRAMEBITS)) != 0) {
            return COMPLETE;
        }
        return 0;
    }
    public boolean imageUpdate(Image img, int infoflags,
                               int x, int y, int w, int h) {
        if (cancelled) {
            return false;
        }
        int s = parseflags(infoflags);
        if (s != 0 && s != status) {
            setStatus(s);
        }
        return ((status & LOADING) != 0);
    }
}
