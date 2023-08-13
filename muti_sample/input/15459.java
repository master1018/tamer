public class HashCodeTest {
    interface Edge<N extends Node<? extends Edge<N>>> {
        void setEndNode(N n);
    }
    interface Node<E extends Edge<? extends Node<E>>> {
        E getOutEdge();
    }
    public static void main(String argv[]) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        Set<TypeVariable> typeVariables = new HashSet<TypeVariable>();
        classes.add(java.lang.Class.class);
        classes.add(java.util.Map.class);
        classes.add(java.lang.Enum.class); 
        classes.add(Edge.class);
        classes.add(Node.class);
        for(Class<?> clazz: classes) {
            System.out.println(clazz);
            for (TypeVariable<?> tv : clazz.getTypeParameters()) {
                int hc = tv.hashCode();
                typeVariables.add(tv);
                System.out.printf("\t%s 0x%x (%d)%n", tv.getName(), hc, hc);
            }
        }
        int count = 0;
        for(Class<?> clazz: classes) {
            for (TypeVariable<?> tv : clazz.getTypeParameters()) {
                if (!typeVariables.remove(tv))
                    throw new RuntimeException("Type variable " + tv + " not found.");
            }
        }
        if (typeVariables.size() != 0 )
            throw new RuntimeException("Unexpected number of type variables.");
    }
}
