public class ListTypeCheckTest {
    public static void main(String[] args) throws Exception {
        Class[] classes = {
            AttributeList.class, RoleList.class, RoleUnresolvedList.class,
        };
        for (Class c : classes)
            test((Class<? extends ArrayList>) c);
    }
    private static void test(Class<? extends ArrayList> c) throws Exception {
        System.out.println("Testing " + c.getName());
        ArrayList al = c.newInstance();
        test(al);
    }
    private static void test(ArrayList al) throws Exception {
        test(al, true);
        al.clear();
        Method m = al.getClass().getMethod("asList");
        m.invoke(al);
        test(al, false);
    }
    private static void test(ArrayList al, boolean allowsBogus) throws Exception {
        for (int i = 0; i < 5; i++) {
            try {
                switch (i) {
                    case 0:
                        al.add("yo");
                        break;
                    case 1:
                        al.add(0, "yo");
                        break;
                    case 2:
                        al.addAll(Arrays.asList("foo", "bar"));
                        break;
                    case 3:
                        al.addAll(0, Arrays.asList("foo", "bar"));
                        break;
                    case 4:
                        al.set(0, "foo");
                        break;
                    default:
                        throw new Exception("test wrong");
                }
                if (!allowsBogus)
                    throw new Exception("op allowed but should fail");
            } catch (IllegalArgumentException e) {
                if (allowsBogus)
                    throw new Exception("got exception but should not", e);
            }
        }
    }
}
