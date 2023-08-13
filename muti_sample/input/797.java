public class ClassUseTest3 <T extends ParamTest2<List<? extends Foo4>>> {
    public ClassUseTest3(Set<Foo4> p) {}
    public <T extends ParamTest2<List<? extends Foo4>>> ParamTest2<List<? extends Foo4>> method(T t) {
        return null;
    }
    public void method(Set<Foo4> p) {}
}
