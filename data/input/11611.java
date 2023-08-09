class IncompatibleTypes1<V> {
    <T extends Integer & Runnable> IncompatibleTypes1<T> m() {
        return null;
    }
    IncompatibleTypes1<? super String> o = m();
}
