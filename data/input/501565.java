public abstract class LocalsArray extends MutabilityControl implements ToHuman {
    protected LocalsArray(boolean mutable) {
        super(mutable);
    }
    public abstract LocalsArray copy();
    public abstract void annotate(ExceptionWithContext ex);
    public abstract void makeInitialized(Type type);
    public abstract int getMaxLocals();
    public abstract void set(int idx, TypeBearer type);
    public abstract void set(RegisterSpec spec);
    public abstract void invalidate(int idx);
    public abstract TypeBearer getOrNull(int idx);
    public abstract TypeBearer get(int idx);
    public abstract TypeBearer getCategory1(int idx);
    public abstract TypeBearer getCategory2(int idx);
    public abstract LocalsArray merge(LocalsArray other);
    public abstract LocalsArraySet mergeWithSubroutineCaller
            (LocalsArray other, int predLabel);
    protected abstract OneLocalsArray getPrimary();
}
