class Foo {
}
public class LookupAnyInvocation {
    public static final void main(String[] args) {
        ObjectStreamClass descs = ObjectStreamClass.lookupAny(Foo.class);
        if (descs == null) {
            throw new Error();
        }
    }
}
