public abstract class AbstractRepository<T extends Tree> {
    private GenericsFactory factory;
    private T tree; 
    private GenericsFactory getFactory() { return factory;}
    protected T getTree(){ return tree;}
    protected Reifier getReifier(){return Reifier.make(getFactory());}
    protected AbstractRepository(String rawSig, GenericsFactory f) {
        tree = parse(rawSig);
        factory = f;
    }
    protected abstract T parse(String s);
}
