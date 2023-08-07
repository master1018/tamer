public final class TxScope {
    TxType type;
    String serverName;
    TxIsolation isolation;
    boolean readOnly;
    ArrayList<Class<? extends Throwable>> rollbackFor;
    ArrayList<Class<? extends Throwable>> noRollbackFor;
    public static TxScope required() {
        return new TxScope(TxType.REQUIRED);
    }
    public static TxScope requiresNew() {
        return new TxScope(TxType.REQUIRES_NEW);
    }
    public static TxScope mandatory() {
        return new TxScope(TxType.MANDATORY);
    }
    public static TxScope supports() {
        return new TxScope(TxType.SUPPORTS);
    }
    public static TxScope notSupported() {
        return new TxScope(TxType.NOT_SUPPORTED);
    }
    public static TxScope never() {
        return new TxScope(TxType.NEVER);
    }
    public TxScope() {
        this.type = TxType.REQUIRED;
    }
    public TxScope(TxType type) {
        this.type = type;
    }
    public String toString() {
        return "TxScope[" + type + "] readOnly[" + readOnly + "] isolation[" + isolation + "] serverName[" + serverName + "] rollbackFor[" + rollbackFor + "] noRollbackFor[" + noRollbackFor + "]";
    }
    public TxType getType() {
        return type;
    }
    public TxScope setType(TxType type) {
        this.type = type;
        return this;
    }
    public boolean isReadonly() {
        return readOnly;
    }
    public TxScope setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }
    public TxIsolation getIsolation() {
        return isolation;
    }
    public TxScope setIsolation(TxIsolation isolation) {
        this.isolation = isolation;
        return this;
    }
    public String getServerName() {
        return serverName;
    }
    public TxScope setServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }
    public ArrayList<Class<? extends Throwable>> getRollbackFor() {
        return rollbackFor;
    }
    public TxScope setRollbackFor(Class<? extends Throwable> rollbackThrowable) {
        if (rollbackFor == null) {
            rollbackFor = new ArrayList<Class<? extends Throwable>>(2);
        }
        rollbackFor.add(rollbackThrowable);
        return this;
    }
    @SuppressWarnings("unchecked")
    public TxScope setRollbackFor(Class<?>[] rollbackThrowables) {
        if (rollbackFor == null) {
            rollbackFor = new ArrayList<Class<? extends Throwable>>(rollbackThrowables.length);
        }
        for (int i = 0; i < rollbackThrowables.length; i++) {
            rollbackFor.add((Class<? extends Throwable>) rollbackThrowables[i]);
        }
        return this;
    }
    public ArrayList<Class<? extends Throwable>> getNoRollbackFor() {
        return noRollbackFor;
    }
    public TxScope setNoRollbackFor(Class<? extends Throwable> noRollback) {
        if (noRollbackFor == null) {
            noRollbackFor = new ArrayList<Class<? extends Throwable>>(2);
        }
        this.noRollbackFor.add(noRollback);
        return this;
    }
    @SuppressWarnings("unchecked")
    public TxScope setNoRollbackFor(Class<?>[] noRollbacks) {
        if (noRollbackFor == null) {
            noRollbackFor = new ArrayList<Class<? extends Throwable>>(noRollbacks.length);
        }
        for (int i = 0; i < noRollbacks.length; i++) {
            noRollbackFor.add((Class<? extends Throwable>) noRollbacks[i]);
        }
        return this;
    }
}
