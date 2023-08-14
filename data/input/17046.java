public class Test6723447 {
    public static void main(String[] args) {
        test(Test6723447.class);
        test(BigDecimal.class);
    }
    private static void test(Class<?> type) {
        for (PropertyDescriptor pd : getPropertyDescriptors(type)) {
            test(pd.getWriteMethod());
            if (pd instanceof IndexedPropertyDescriptor) {
                IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
                test(ipd.getIndexedWriteMethod());
            }
        }
    }
    private static void test(Method method) {
        if (method != null) {
            Class<?> type = method.getReturnType();
            if (!type.equals(void.class)) {
                throw new Error("unexpected return type: " + type);
            }
        }
    }
    private static PropertyDescriptor[] getPropertyDescriptors(Class<?> type) {
        try {
            return Introspector.getBeanInfo(type).getPropertyDescriptors();
        }
        catch (IntrospectionException exception) {
            throw new Error("unexpected exception", exception);
        }
    }
    public Object getValue() {
        return null;
    }
    public Object setValue(Object value) {
        return value;
    }
    public Object getValues(int index) {
        return null;
    }
    public Object setValues(int index, Object value) {
        return value;
    }
}
