class A<T> {
    Object o = new Object() {
        public T t;
    };
}
public class T4992170 {
    public static void main(String[] args) throws NoSuchFieldException {
        Type t = new A<Integer>().o.getClass().getField("t").getGenericType();
        if (!(t instanceof TypeVariable))
            throw new Error("" + t);
    }
}
