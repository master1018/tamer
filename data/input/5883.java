public class Test4896879 {
    public static void main(String[] args) {
        test(A.class);
        test(B.class);
    }
    private static void test(Class type) {
        if (BeanUtils.getEventSetDescriptors(type).length != 0) {
            throw new Error("Should not have any EventSetDescriptors");
        }
    }
    public static class A implements EventListener {
        public void addB(B a) {
        }
        public void removeB(B b) {
        }
    }
    public static class B implements EventListener {
        public void addA(A a) {
        }
        public void removeA(A a) {
        }
    }
}
