class SortAlgorithm {
    private SortItem parent;
    protected boolean stopRequested = false;
    public void setParent(SortItem p) {
        parent = p;
    }
    protected void pause() throws Exception {
        if (stopRequested) {
            throw new Exception("Sort Algorithm");
        }
        parent.pause(parent.h1, parent.h2);
    }
    protected void pause(int H1) throws Exception {
        if (stopRequested) {
            throw new Exception("Sort Algorithm");
        }
        parent.pause(H1, parent.h2);
    }
    protected void pause(int H1, int H2) throws Exception {
        if (stopRequested) {
            throw new Exception("Sort Algorithm");
        }
        parent.pause(H1, H2);
    }
    public void stop() {
        stopRequested = true;
    }
    public void init() {
        stopRequested = false;
    }
    void sort(int a[]) throws Exception {
    }
}
