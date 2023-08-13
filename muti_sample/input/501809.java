public final class NullOutputReceiver implements IShellOutputReceiver {
    private static NullOutputReceiver sReceiver = new NullOutputReceiver();
    public static IShellOutputReceiver getReceiver() {
        return sReceiver;
    }
    public void addOutput(byte[] data, int offset, int length) {
    }
    public void flush() {
    }
    public boolean isCancelled() {
        return false;
    }
}
