public abstract class LazyReflectiveObjectGenerator {
    private GenericsFactory factory; 
    protected LazyReflectiveObjectGenerator(GenericsFactory f) {
        factory = f;
    }
    private GenericsFactory getFactory() {
        return factory;
    }
    protected Reifier getReifier(){return Reifier.make(getFactory());}
}
