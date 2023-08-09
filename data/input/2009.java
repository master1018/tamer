public class Wildcards {
    public void methodWithWildCardParam(TypeParameters<? super String> a,
        TypeParameters<? extends StringBuffer> b, TypeParameters c) {}
}
