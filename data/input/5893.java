final class AWTLockAccess {
    static native void awtLock();
    static native void awtUnlock();
    static void awtWait() { awtWait(0); }
    static native void awtWait(long timeout);
    static native void awtNotifyAll();
}
