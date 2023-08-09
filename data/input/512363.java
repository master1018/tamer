class ListOfVariables {
    public static final TypeVariable[] empty = new ImplForVariable[0];
    ArrayList<TypeVariable<?>> array = new ArrayList<TypeVariable<?>>();
    int n = 0;
    void add (TypeVariable<?> elem) {
        array.add(elem);
    }
    TypeVariable<?>[] getArray() {
        TypeVariable<?>[] a = new TypeVariable[array.size()];
        return array.toArray(a);
    }
}
