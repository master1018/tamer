public class ListOfTypes {
    static final ListOfTypes empty = new ListOfTypes(0);
    ArrayList<Type> list;
    private Type[] resolvedTypes;
    void add(Type elem) {
        if (elem == null) {
            throw new RuntimeException("Adding null type is not allowed!");
        }
        list.add(elem);
    }
    ListOfTypes(int capacity) {
        list = new ArrayList<Type>(capacity);
    }
    ListOfTypes(Type[] types) {
        list = new ArrayList<Type>();
        for(Type t : types) {
            list.add(t);
        }
    }
    int length() {
        return list.size();
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Type t : list) {
            if (i != 0) { sb.append(", "); }
            sb.append(t.toString());
        }
        return sb.toString();
    }
    public Type[] getResolvedTypes() {
        if (resolvedTypes == null) {
            resolvedTypes = new Type[list.size()];
            int i = 0;
            for (Type t : list) {
                try { 
                    resolvedTypes[i] = ((ImplForType)t).getResolvedType();
                } catch (ClassCastException e) { 
                    resolvedTypes[i] = t; 
                }
                i++;
            }
            list = null;
        }
        return resolvedTypes;
    }
}
