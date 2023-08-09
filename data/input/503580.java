public class FooBar<K,V,L> extends Foo<V> implements Bar<K> {
    public class C
    {
    }
    public class CI extends C implements Iface
    {
    }
    public FooBar(K k) {
        super(null);
        throw new RuntimeException("!");
    }
    public K bar(K arg) {
        return null;
    }
    public FooBar<K,? extends Foo,L> a(K arg) {
        return null;
    }
    public FooBar<V,K,L> b(Bar<? extends K> arg) {
        return null;
    }
    public <L extends C & Iface> void f(L arg) {
    }
    public V v;
}
