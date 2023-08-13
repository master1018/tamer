public final class Signal {
    private static Hashtable handlers = new Hashtable(4);
    private static Hashtable signals = new Hashtable(4);
    private int number;
    private String name;
    public int getNumber() {
        return number;
    }
    public String getName() {
        return name;
    }
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || !(other instanceof Signal)) {
            return false;
        }
        Signal other1 = (Signal)other;
        return name.equals(other1.name) && (number == other1.number);
    }
    public int hashCode() {
        return number;
    }
    public String toString() {
        return "SIG" + name;
    }
    public Signal(String name) {
        number = findSignal(name);
        this.name = name;
        if (number < 0) {
            throw new IllegalArgumentException("Unknown signal: " + name);
        }
    }
    public static synchronized SignalHandler handle(Signal sig,
                                                    SignalHandler handler)
        throws IllegalArgumentException {
        long newH = (handler instanceof NativeSignalHandler) ?
                      ((NativeSignalHandler)handler).getHandler() : 2;
        long oldH = handle0(sig.number, newH);
        if (oldH == -1) {
            throw new IllegalArgumentException
                ("Signal already used by VM or OS: " + sig);
        }
        signals.put(new Integer(sig.number), sig);
        synchronized (handlers) {
            SignalHandler oldHandler = (SignalHandler)handlers.get(sig);
            handlers.remove(sig);
            if (newH == 2) {
                handlers.put(sig, handler);
            }
            if (oldH == 0) {
                return SignalHandler.SIG_DFL;
            } else if (oldH == 1) {
                return SignalHandler.SIG_IGN;
            } else if (oldH == 2) {
                return oldHandler;
            } else {
                return new NativeSignalHandler(oldH);
            }
        }
    }
    public static void raise(Signal sig) throws IllegalArgumentException {
        if (handlers.get(sig) == null) {
            throw new IllegalArgumentException("Unhandled signal: " + sig);
        }
        raise0(sig.number);
    }
    private static void dispatch(final int number) {
        final Signal sig = (Signal)signals.get(new Integer(number));
        final SignalHandler handler = (SignalHandler)handlers.get(sig);
        Runnable runnable = new Runnable () {
            public void run() {
                handler.handle(sig);
            }
        };
        if (handler != null) {
            new Thread(runnable, sig + " handler").start();
        }
    }
    private static native int findSignal(String sigName);
    private static native long handle0(int sig, long nativeH);
    private static native void raise0(int sig);
}
