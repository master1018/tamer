public class TypeParameters<E> implements SubInterface<E> {
    public E methodThatUsesTypeParameter(E param) {
        return param;
    }
    public <T extends List, V> String[] methodThatHasTypeParameters(T param1,
        V param2) { return null;}
    public <A> void methodThatHasTypeParmaters(A... a) {}
}
