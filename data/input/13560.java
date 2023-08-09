public final class AWTAutoShutdown implements Runnable {
    private static final AWTAutoShutdown theInstance = new AWTAutoShutdown();
    private final Object mainLock = new Object();
    private final Object activationLock = new Object();
    private final HashSet busyThreadSet = new HashSet(7);
    private boolean toolkitThreadBusy = false;
    private final Map peerMap = new IdentityHashMap();
    private Thread blockerThread = null;
    private boolean timeoutPassed = false;
    private static final int SAFETY_TIMEOUT = 1000;
    private AWTAutoShutdown() {}
    public static AWTAutoShutdown getInstance() {
        return theInstance;
    }
    public static void notifyToolkitThreadBusy() {
        getInstance().setToolkitBusy(true);
    }
    public static void notifyToolkitThreadFree() {
        getInstance().setToolkitBusy(false);
    }
    public void notifyThreadBusy(final Thread thread) {
        if (thread == null) {
            return;
        }
        synchronized (activationLock) {
            synchronized (mainLock) {
                if (blockerThread == null) {
                    activateBlockerThread();
                } else if (isReadyToShutdown()) {
                    mainLock.notifyAll();
                    timeoutPassed = false;
                }
                busyThreadSet.add(thread);
            }
        }
    }
    public void notifyThreadFree(final Thread thread) {
        if (thread == null) {
            return;
        }
        synchronized (activationLock) {
            synchronized (mainLock) {
                busyThreadSet.remove(thread);
                if (isReadyToShutdown()) {
                    mainLock.notifyAll();
                    timeoutPassed = false;
                }
            }
        }
    }
    void notifyPeerMapUpdated() {
        synchronized (activationLock) {
            synchronized (mainLock) {
                if (!isReadyToShutdown() && blockerThread == null) {
                    activateBlockerThread();
                } else {
                    mainLock.notifyAll();
                    timeoutPassed = false;
                }
            }
        }
    }
    private boolean isReadyToShutdown() {
        return (!toolkitThreadBusy &&
                 peerMap.isEmpty() &&
                 busyThreadSet.isEmpty());
    }
    private void setToolkitBusy(final boolean busy) {
        if (busy != toolkitThreadBusy) {
            synchronized (activationLock) {
                synchronized (mainLock) {
                    if (busy != toolkitThreadBusy) {
                        if (busy) {
                            if (blockerThread == null) {
                                activateBlockerThread();
                            } else if (isReadyToShutdown()) {
                                mainLock.notifyAll();
                                timeoutPassed = false;
                            }
                            toolkitThreadBusy = busy;
                        } else {
                            toolkitThreadBusy = busy;
                            if (isReadyToShutdown()) {
                                mainLock.notifyAll();
                                timeoutPassed = false;
                            }
                        }
                    }
                }
            }
        }
    }
    public void run() {
        Thread currentThread = Thread.currentThread();
        boolean interrupted = false;
        synchronized (mainLock) {
            try {
                mainLock.notifyAll();
                while (blockerThread == currentThread) {
                    mainLock.wait();
                    timeoutPassed = false;
                    while (isReadyToShutdown()) {
                        if (timeoutPassed) {
                            timeoutPassed = false;
                            blockerThread = null;
                            break;
                        }
                        timeoutPassed = true;
                        mainLock.wait(SAFETY_TIMEOUT);
                    }
                }
            } catch (InterruptedException e) {
                interrupted = true;
            } finally {
                if (blockerThread == currentThread) {
                    blockerThread = null;
                }
            }
        }
        if (!interrupted) {
            AppContext.stopEventDispatchThreads();
        }
    }
    static AWTEvent getShutdownEvent() {
        return new AWTEvent(getInstance(), 0) {};
    }
    private void activateBlockerThread() {
        Thread thread = new Thread(this, "AWT-Shutdown");
        thread.setDaemon(false);
        blockerThread = thread;
        thread.start();
        try {
            mainLock.wait();
        } catch (InterruptedException e) {
            System.err.println("AWT blocker activation interrupted:");
            e.printStackTrace();
        }
    }
    final void registerPeer(final Object target, final Object peer) {
        synchronized (activationLock) {
            synchronized (mainLock) {
                peerMap.put(target, peer);
                notifyPeerMapUpdated();
            }
        }
    }
    final void unregisterPeer(final Object target, final Object peer) {
        synchronized (activationLock) {
            synchronized (mainLock) {
                if (peerMap.get(target) == peer) {
                    peerMap.remove(target);
                    notifyPeerMapUpdated();
                }
            }
        }
    }
    final Object getPeer(final Object target) {
        synchronized (activationLock) {
            synchronized (mainLock) {
                return peerMap.get(target);
            }
        }
    }
    final void dumpPeers(final PlatformLogger aLog) {
        synchronized (activationLock) {
            synchronized (mainLock) {
                aLog.fine("Mapped peers:");
                for (Object key : peerMap.keySet()) {
                    aLog.fine(key + "->" + peerMap.get(key));
                }
            }
        }
    }
} 
