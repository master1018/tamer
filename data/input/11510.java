public class TestParameterizedType {
    public <T> T genericMethod0() {
        return null;
    }
    public <T> Set<T> genericMethod1() {
        return null;
    }
    public <T> Set<T> genericMethod2() {
        return null;
    }
    public <S> List<S> genericMethod3() {
        return null;
    }
    public <X, Y> Map<X, Y> genericMethod4() {
        return null;
    }
    public <T> T[] genericMethod5() {
        return null;
    }
    public <T> T[] genericMethod6() {
        return null;
    }
    public Set<? extends Cloneable> genericMethod7() {
        return null;
    }
    public Set<? super Number> genericMethod8() {
        return null;
    }
    public Set<?> genericMethod9() {
        return null;
    }
    static List<Type> createTypes() throws Exception {
        List<Type> typeList = new ArrayList<Type>(3);
        String[] methodNames = {"genericMethod0",
                                "genericMethod1",
                                "genericMethod2",
                                "genericMethod3",
                                "genericMethod4",
                                "genericMethod5",
                                "genericMethod6",
                                "genericMethod7",
                                "genericMethod8",
                                "genericMethod9",
        };
        for(String s : methodNames) {
            Type t = TestParameterizedType.class.getDeclaredMethod(s).getGenericReturnType();
            typeList.add(t);
        }
        return typeList;
    }
    static boolean testReflexes(List<Type> typeList) {
        for(Type t : typeList) {
            if (! t.equals(t) ) {
                System.err.printf("Bad reflexes for%s %s%n", t, t.getClass());
                return true;
            }
        }
        return false;
    }
    public static void main(String[] argv) throws Exception {
        boolean failed = false;
        List<Type> take1 = createTypes();
        List<Type> take2 = createTypes();
        failed = failed | testReflexes(take1);
        failed = failed | testReflexes(take2);
        for(int i = 0; i < take1.size(); i++) {
            Type type1 = take1.get(i);
            for(int j = 0; j < take2.size(); j++) {
                Type type2 = take2.get(j);
                if (i == j) {
                    if (!type1.equals(type2) ) {
                        failed = true;
                        System.err.printf("Unexpected inequality: [%d, %d] %n\t%s%n\t%s%n",
                                          i, j, type1, type2);
                    }
                } else {
                    if (type1.equals(type2) ) {
                        failed = true;
                        System.err.printf("Unexpected equality: [%d, %d] %n\t%s%n\t%s%n",
                                          i, j, type1, type2);
                    }
                }
            }
        }
        if (failed)
            throw new RuntimeException("Bad equality on ParameterizedTypes");
    }
}
