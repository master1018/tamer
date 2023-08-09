class NativeStart implements Runnable {
    private NativeStart() {}
    private static native void main(String[] dummy);
    public native void run();
}
