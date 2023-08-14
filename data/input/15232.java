public class Test6976577 {
    public static void main(String[] args) throws Exception {
        Class<?> bt = Accessor.getBeanType();
        Class<?> lt = Accessor.getListenerType();
        PropertyDescriptor pd = new PropertyDescriptor("boolean", bt);
        test(pd.getReadMethod());
        test(pd.getWriteMethod());
        IndexedPropertyDescriptor ipd = new IndexedPropertyDescriptor("indexed", bt);
        test(ipd.getReadMethod());
        test(ipd.getWriteMethod());
        test(ipd.getIndexedReadMethod());
        test(ipd.getIndexedWriteMethod());
        EventSetDescriptor esd = new EventSetDescriptor(bt, "test", lt, "process");
        test(esd.getAddListenerMethod());
        test(esd.getRemoveListenerMethod());
        test(esd.getGetListenerMethod());
        test(esd.getListenerMethods());
    }
    private static void test(Method... methods) {
        for (Method method : methods) {
            if (method == null) {
                throw new Error("public method is not found");
            }
        }
    }
}
