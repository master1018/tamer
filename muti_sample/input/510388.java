public abstract class RegisterMapper {
    public abstract int getNewRegisterCount();
    public abstract RegisterSpec map(RegisterSpec registerSpec);
    public final RegisterSpecList map(RegisterSpecList sources) {
        int sz = sources.size();
        RegisterSpecList newSources = new RegisterSpecList(sz);
        for (int i = 0; i < sz; i++) {
            newSources.set(i, map(sources.get(i)));
        }
        newSources.setImmutable();
        return newSources.equals(sources) ? sources : newSources;
    }
}
