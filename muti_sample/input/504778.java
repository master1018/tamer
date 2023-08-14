public class MessageQueue {
    Message mMessages;
    private final ArrayList mIdleHandlers = new ArrayList();
    private boolean mQuiting = false;
    boolean mQuitAllowed = true;
    public static interface IdleHandler {
        boolean queueIdle();
    }
    public final void addIdleHandler(IdleHandler handler) {
        if (handler == null) {
            throw new NullPointerException("Can't add a null IdleHandler");
        }
        synchronized (this) {
            mIdleHandlers.add(handler);
        }
    }
    public final void removeIdleHandler(IdleHandler handler) {
        synchronized (this) {
            mIdleHandlers.remove(handler);
        }
    }
    MessageQueue() {
    }
    final Message next() {
        boolean tryIdle = true;
        while (true) {
            long now;
            Object[] idlers = null;
            synchronized (this) {
                now = SystemClock.uptimeMillis();
                Message msg = pullNextLocked(now);
                if (msg != null) return msg;
                if (tryIdle && mIdleHandlers.size() > 0) {
                    idlers = mIdleHandlers.toArray();
                }
            }
            boolean didIdle = false;
            if (idlers != null) {
                for (Object idler : idlers) {
                    boolean keep = false;
                    try {
                        didIdle = true;
                        keep = ((IdleHandler)idler).queueIdle();
                    } catch (Throwable t) {
                        Log.wtf("MessageQueue", "IdleHandler threw exception", t);
                    }
                    if (!keep) {
                        synchronized (this) {
                            mIdleHandlers.remove(idler);
                        }
                    }
                }
            }
            if (didIdle) {
                tryIdle = false;
                continue;
            }
            synchronized (this) {
                try {
                    if (mMessages != null) {
                        if (mMessages.when-now > 0) {
                            Binder.flushPendingCommands();
                            this.wait(mMessages.when-now);
                        }
                    } else {
                        Binder.flushPendingCommands();
                        this.wait();
                    }
                }
                catch (InterruptedException e) {
                }
            }
        }
    }
    final Message pullNextLocked(long now) {
        Message msg = mMessages;
        if (msg != null) {
            if (now >= msg.when) {
                mMessages = msg.next;
                if (Config.LOGV) Log.v(
                    "MessageQueue", "Returning message: " + msg);
                return msg;
            }
        }
        return null;
    }
    final boolean enqueueMessage(Message msg, long when) {
        if (msg.when != 0) {
            throw new AndroidRuntimeException(msg
                    + " This message is already in use.");
        }
        if (msg.target == null && !mQuitAllowed) {
            throw new RuntimeException("Main thread not allowed to quit");
        }
        synchronized (this) {
            if (mQuiting) {
                RuntimeException e = new RuntimeException(
                    msg.target + " sending message to a Handler on a dead thread");
                Log.w("MessageQueue", e.getMessage(), e);
                return false;
            } else if (msg.target == null) {
                mQuiting = true;
            }
            msg.when = when;
            Message p = mMessages;
            if (p == null || when == 0 || when < p.when) {
                msg.next = p;
                mMessages = msg;
                this.notify();
            } else {
                Message prev = null;
                while (p != null && p.when <= when) {
                    prev = p;
                    p = p.next;
                }
                msg.next = prev.next;
                prev.next = msg;
                this.notify();
            }
        }
        return true;
    }
    final boolean removeMessages(Handler h, int what, Object object,
            boolean doRemove) {
        synchronized (this) {
            Message p = mMessages;
            boolean found = false;
            while (p != null && p.target == h && p.what == what
                   && (object == null || p.obj == object)) {
                if (!doRemove) return true;
                found = true;
                Message n = p.next;
                mMessages = n;
                p.recycle();
                p = n;
            }
            while (p != null) {
                Message n = p.next;
                if (n != null) {
                    if (n.target == h && n.what == what
                        && (object == null || n.obj == object)) {
                        if (!doRemove) return true;
                        found = true;
                        Message nn = n.next;
                        n.recycle();
                        p.next = nn;
                        continue;
                    }
                }
                p = n;
            }
            return found;
        }
    }
    final void removeMessages(Handler h, Runnable r, Object object) {
        if (r == null) {
            return;
        }
        synchronized (this) {
            Message p = mMessages;
            while (p != null && p.target == h && p.callback == r
                   && (object == null || p.obj == object)) {
                Message n = p.next;
                mMessages = n;
                p.recycle();
                p = n;
            }
            while (p != null) {
                Message n = p.next;
                if (n != null) {
                    if (n.target == h && n.callback == r
                        && (object == null || n.obj == object)) {
                        Message nn = n.next;
                        n.recycle();
                        p.next = nn;
                        continue;
                    }
                }
                p = n;
            }
        }
    }
    final void removeCallbacksAndMessages(Handler h, Object object) {
        synchronized (this) {
            Message p = mMessages;
            while (p != null && p.target == h
                    && (object == null || p.obj == object)) {
                Message n = p.next;
                mMessages = n;
                p.recycle();
                p = n;
            }
            while (p != null) {
                Message n = p.next;
                if (n != null) {
                    if (n.target == h && (object == null || n.obj == object)) {
                        Message nn = n.next;
                        n.recycle();
                        p.next = nn;
                        continue;
                    }
                }
                p = n;
            }
        }
    }
    void poke()
    {
        synchronized (this) {
            this.notify();
        }
    }
}
