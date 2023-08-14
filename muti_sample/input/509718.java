public class MutabilityControl {
    private boolean mutable;
    public MutabilityControl() {
        mutable = true;
    }
    public MutabilityControl(boolean mutable) {
        this.mutable = mutable;
    }
    public void setImmutable() {
        mutable = false;
    }
    public final boolean isImmutable() {
        return !mutable;
    }
    public final boolean isMutable() {
        return mutable;
    }
    public final void throwIfImmutable() {
        if (!mutable) {
            throw new MutabilityException("immutable instance");
        }
    }
    public final void throwIfMutable() {
        if (mutable) {
            throw new MutabilityException("mutable instance");
        }
    }
}
