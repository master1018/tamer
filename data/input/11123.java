public class FooBarBeanInfo extends SimpleBeanInfo {
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(FooBar.class);
        bd.setValue("test", Boolean.TRUE);
        return bd;
    }
}
