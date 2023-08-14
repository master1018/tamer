public class DummyObjectFactory implements ObjectFactory {
    public DummyObjectFactory() {
    }
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                Hashtable<?,?> environment) throws Exception {
        return new DummyContext(environment);
    }
}
