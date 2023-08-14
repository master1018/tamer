public class ClassUseTest1 <T extends Foo & Foo2> {
    public <T extends Foo & Foo2> T method(T t) {
        return null;
    }
}
