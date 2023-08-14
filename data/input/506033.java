class GlobalCanvasDragInfo {
    private static final GlobalCanvasDragInfo sInstance = new GlobalCanvasDragInfo();
    private String mCurrentFqcn = null;
    private GlobalCanvasDragInfo() {
    }
    public static GlobalCanvasDragInfo getInstance() {
        return sInstance;
    }
    public void startDrag(String fqcn) {
        mCurrentFqcn = fqcn;
    }
    public void stopDrag() {
        mCurrentFqcn = null;
    }
    public String getCurrentFqcn() {
        return mCurrentFqcn;
    }
}
