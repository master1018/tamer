public class DummyScope implements Scope {
    private static DummyScope singleton = new DummyScope();
    private DummyScope(){}
    public static DummyScope make() {
        return singleton;
    }
    public TypeVariable<?> lookup(String name) {return null;}
}
