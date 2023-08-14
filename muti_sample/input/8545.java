class UndeterminedType1<V> {
    <T extends Integer & Runnable> UndeterminedType1<T> m() {
        return null;
    }
    UndeterminedType1<? extends String> c2 = m();
}
