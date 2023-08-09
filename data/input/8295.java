class OrbReuseTracker {
    int referenceCnt;
    ORB orb;
    private static final boolean debug = false;
    OrbReuseTracker(ORB orb) {
        this.orb = orb;
        referenceCnt++;
        if (debug) {
             System.out.println("New OrbReuseTracker created");
        }
    }
    synchronized void incRefCount() {
        referenceCnt++;
        if (debug) {
             System.out.println("Increment orb ref count to:" + referenceCnt);
        }
    }
    synchronized void decRefCount() {
        referenceCnt--;
        if (debug) {
             System.out.println("Decrement orb ref count to:" + referenceCnt);
        }
        if ((referenceCnt == 0)) {
            if (debug) {
                System.out.println("Destroying the ORB");
            }
            orb.destroy();
        }
    }
}
