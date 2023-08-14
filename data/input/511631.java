public abstract class BackgroundThread extends Thread {
    private boolean mQuit = false;
    public final void quit() {
        mQuit = true;
        Log.d("ddms", "Waiting for BackgroundThread to quit");
        try {
            this.join();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }
    protected final boolean isQuitting() {
        return mQuit;
    }
}
