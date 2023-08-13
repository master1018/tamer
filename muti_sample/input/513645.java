class ModalContext {
    private boolean running = false;
    private final Toolkit toolkit;
    ModalContext() {
        toolkit = Toolkit.getDefaultToolkit();
    }
    void runModalLoop() {
        running = true;
        toolkit.dispatchThread.runModalLoop(this);
    }
    void endModalLoop() {
        running = false;
    }
    boolean isModalLoopRunning() {
        return running;
    }
}
